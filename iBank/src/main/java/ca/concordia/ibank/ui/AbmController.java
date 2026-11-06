package ca.concordia.ibank.ui;

import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.exception.IbankException;
import ca.concordia.ibank.i18n.Messages;
import ca.concordia.ibank.model.Language;
import ca.concordia.ibank.model.Transaction;
import ca.concordia.ibank.service.AbmConfig;
import ca.concordia.ibank.service.AuditLog;
import ca.concordia.ibank.service.AuthenticationService;
import ca.concordia.ibank.service.OperatorService;
import ca.concordia.ibank.service.Session;
import ca.concordia.ibank.service.TransactionService;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.Timer;

/**
 * Mediates between the screens and the services: keeps the session, turns exceptions into helpful
 * messages, and ends idle sessions (D1 assumption A5).
 */
public class AbmController {

  /** A client transaction run by the controller on behalf of a screen. */
  @FunctionalInterface
  public interface TransactionAction {
    Transaction run(Session session) throws IbankException;
  }

  private final AuthenticationService auth;
  private final TransactionService transactions;
  private final OperatorService operators;
  private final AuditLog audit;
  private final Timer idleTimer;
  private Navigator navigator;
  private Messages messages = new Messages(Language.ENGLISH);
  private String pendingCardNumber;
  private Session session;
  private Transaction lastTransaction;
  private boolean operatorMode;

  /** Creates the controller. */
  public AbmController(AuthenticationService auth, TransactionService transactions,
      OperatorService operators, AbmConfig config, AuditLog audit) {
    this.auth = Objects.requireNonNull(auth);
    this.transactions = Objects.requireNonNull(transactions);
    this.operators = Objects.requireNonNull(operators);
    this.audit = Objects.requireNonNull(audit);
    int timeoutMillis = (int) config.getSessionTimeout().toMillis();
    this.idleTimer = new Timer(timeoutMillis, e -> onIdleTimeout());
    this.idleTimer.setRepeats(false);
    Toolkit.getDefaultToolkit().addAWTEventListener(e -> restartIdleTimer(),
        AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
  }

  public void setNavigator(Navigator navigator) {
    this.navigator = navigator;
  }

  public Messages messages() {
    return messages;
  }

  public TransactionService transactions() {
    return transactions;
  }

  public OperatorService operators() {
    return operators;
  }

  public Session session() {
    return session;
  }

  public Transaction lastTransaction() {
    return lastTransaction;
  }

  /** UC1 Select Language. */
  public void selectLanguage(Language language) {
    messages = new Messages(language);
    show(ScreenId.WELCOME);
  }

  public void show(ScreenId screen) {
    navigator.show(screen);
  }

  /** First step of UC8: the card is inserted, tapped or scanned. */
  public void presentCard(String cardNumber) {
    try {
      pendingCardNumber = auth.identifyCard(cardNumber).getCardNumber();
      show(ScreenId.PIN);
    } catch (IbankException e) {
      navigator.showError(messages.describe(e));
    }
  }

  /** Second step of UC8: PIN entry. The PIN array is wiped after use. */
  public void submitPin(char[] pin) {
    try {
      session = auth.authenticate(pendingCardNumber, pin, messages.getLanguage());
      idleTimer.restart();
      show(ScreenId.MENU);
    } catch (AuthenticationException e) {
      navigator.showError(messages.describe(e));
    } catch (IbankException e) {
      navigator.showError(messages.describe(e));
      endSession();
    } finally {
      Arrays.fill(pin, '\0');
    }
  }

  /** Runs a transaction and shows its result, or a helpful message if it fails. */
  public void perform(TransactionAction action) {
    if (session == null || !session.isOpen()) {
      endSession();
      return;
    }
    try {
      lastTransaction = action.run(session);
      show(ScreenId.RESULT);
    } catch (IbankException e) {
      navigator.showError(messages.describe(e));
    } catch (RuntimeException e) {
      audit.error("Unexpected error during transaction", e);
      navigator.showError(messages.get("error.unexpected"));
    }
  }

  /** Ends the client session, returns the card and goes back to the welcome screen. */
  public void endSession() {
    if (session != null) {
      session.close();
      audit.info("Session closed for " + session.getCard().maskedNumber());
    }
    session = null;
    pendingCardNumber = null;
    lastTransaction = null;
    operatorMode = false;
    idleTimer.stop();
    show(ScreenId.WELCOME);
  }

  /** UC16 Authenticate Operator. */
  public void operatorLogin(String operatorId, char[] pin) {
    try {
      operators.authenticate(operatorId.trim(), pin);
      operatorMode = true;
      idleTimer.restart();
      show(ScreenId.OPERATOR);
    } catch (IbankException e) {
      navigator.showError(messages.describe(e));
    } finally {
      Arrays.fill(pin, '\0');
    }
  }

  public boolean isOperatorMode() {
    return operatorMode;
  }

  public void showInfo(String message) {
    navigator.showInfo(message);
  }

  public void showError(String message) {
    navigator.showError(message);
  }

  private void restartIdleTimer() {
    if (idleTimer.isRunning()) {
      idleTimer.restart();
    }
  }

  private void onIdleTimeout() {
    if (session != null || operatorMode) {
      audit.info("Session timed out");
      endSession();
      navigator.showInfo(messages.get("info.timeout"));
    }
  }
}
