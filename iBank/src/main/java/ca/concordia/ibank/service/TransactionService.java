package ca.concordia.ibank.service;

import ca.concordia.ibank.exception.AccountNotFoundException;
import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.exception.CashUnavailableException;
import ca.concordia.ibank.exception.IbankException;
import ca.concordia.ibank.exception.InvalidAmountException;
import ca.concordia.ibank.exception.InvalidPinFormatException;
import ca.concordia.ibank.exception.LimitExceededException;
import ca.concordia.ibank.exception.OutOfServiceException;
import ca.concordia.ibank.exception.ServiceNotAllowedException;
import ca.concordia.ibank.model.Account;
import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.model.Payee;
import ca.concordia.ibank.model.Transaction;
import ca.concordia.ibank.model.TransactionType;
import ca.concordia.ibank.security.PinHasher;
import ca.concordia.ibank.security.PinPolicy;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Client transactions of iBank: use cases UC2 to UC7, with UC9 Authorize Transaction and UC12
 * Flag Reportable Transaction applied to every request.
 */
public class TransactionService {

  private final BankHost host;
  private final AbmDevice device;
  private final AbmConfig config;
  private final ComplianceMonitor compliance;
  private final AuditLog audit;
  private final Clock clock;

  /** Creates the service; {@code clock} is injected so that daily limits can be tested. */
  public TransactionService(BankHost host, AbmDevice device, AbmConfig config,
      ComplianceMonitor compliance, AuditLog audit, Clock clock) {
    this.host = Objects.requireNonNull(host);
    this.device = Objects.requireNonNull(device);
    this.config = Objects.requireNonNull(config);
    this.compliance = Objects.requireNonNull(compliance);
    this.audit = Objects.requireNonNull(audit);
    this.clock = Objects.requireNonNull(clock);
  }

  /** UC3 Check Balance. The returned transaction holds the available balance. */
  public Transaction checkBalance(Session session, String accountNumber) throws IbankException {
    requireUsable(session, TransactionType.BALANCE_INQUIRY);
    Account account = account(session, accountNumber);
    return complete(session, TransactionType.BALANCE_INQUIRY, account.getAccountNumber(), null,
        Money.ZERO, account.getAvailableBalance());
  }

  /** UC2 Withdraw Cash. */
  public Transaction withdraw(Session session, String accountNumber, Money amount)
      throws IbankException {
    requireUsable(session, TransactionType.WITHDRAWAL);
    final Account account = account(session, accountNumber);
    if (!amount.isMultipleOf(config.getWithdrawalUnit())) {
      throw new InvalidAmountException("error.amount.notMultiple",
          config.getWithdrawalUnit().format(session.getLanguage().getLocale()));
    }
    if (amount.isGreaterThan(config.getPerWithdrawalLimit())) {
      throw new LimitExceededException(format(session, config.getPerWithdrawalLimit()));
    }
    Money remainingToday = remainingDailyLimit(session);
    if (amount.isGreaterThan(remainingToday)) {
      throw new LimitExceededException(format(session, remainingToday));
    }
    if (!device.getDispenser().canDispense(amount)) {
      throw new CashUnavailableException(format(session, amount));
    }
    account.debit(amount);
    try {
      device.getDispenser().dispense(amount);
    } catch (CashUnavailableException e) {
      account.credit(amount);
      audit.error("Dispense failed; withdrawal reversed", e);
      throw e;
    }
    return complete(session, TransactionType.WITHDRAWAL, accountNumber, null, amount,
        account.getAvailableBalance());
  }

  /** UC5 Deposit Funds. Cheques are credited with a hold above the immediate availability. */
  public Transaction deposit(Session session, String accountNumber, Money amount, boolean cheque)
      throws IbankException {
    TransactionType type = cheque ? TransactionType.DEPOSIT_CHEQUE : TransactionType.DEPOSIT_CASH;
    requireUsable(session, type);
    Account account = account(session, accountNumber);
    if (cheque) {
      Money available = Money.min(amount, config.getChequeImmediateAvailability());
      account.creditWithHold(amount, amount.minus(available));
    } else {
      account.credit(amount);
    }
    device.getDepositBin().accept(amount);
    return complete(session, type, null, accountNumber, amount, account.getAvailableBalance());
  }

  /** UC4 Transfer Funds between two of the client's own accounts. */
  public Transaction transfer(Session session, String fromNumber, String toNumber, Money amount)
      throws IbankException {
    requireUsable(session, TransactionType.TRANSFER);
    if (fromNumber.equals(toNumber)) {
      throw new InvalidAmountException("error.transfer.sameAccount");
    }
    Account from = account(session, fromNumber);
    Account to = account(session, toNumber);
    from.debit(amount);
    to.credit(amount);
    return complete(session, TransactionType.TRANSFER, fromNumber, toNumber, amount,
        from.getAvailableBalance());
  }

  /** UC6 Pay Bill to a registered payee. */
  public Transaction payBill(Session session, String payeeId, String fromNumber, Money amount)
      throws IbankException {
    requireUsable(session, TransactionType.BILL_PAYMENT);
    Payee payee = session.getClient().getPayees().stream()
        .filter(p -> p.getPayeeId().equals(payeeId)).findFirst()
        .orElseThrow(() -> new AccountNotFoundException(payeeId));
    Account from = account(session, fromNumber);
    from.debit(amount);
    return complete(session, TransactionType.BILL_PAYMENT, fromNumber, payee.getName(), amount,
        from.getAvailableBalance());
  }

  /** UC7 Change PIN after verifying the current PIN. */
  public Transaction changePin(Session session, char[] currentPin, char[] newPin,
      char[] confirmation) throws IbankException {
    requireUsable(session, TransactionType.PIN_CHANGE);
    if (!PinHasher.verify(currentPin, session.getCard().getPinHash())) {
      throw new AuthenticationException();
    }
    PinPolicy.validate(newPin, confirmation);
    if (PinHasher.verify(newPin, session.getCard().getPinHash())) {
      throw new InvalidPinFormatException("error.pin.same");
    }
    session.getCard().setPinHash(PinHasher.hash(newPin));
    audit.security("PIN changed for " + session.getCard().maskedNumber());
    return complete(session, TransactionType.PIN_CHANGE, null, null, Money.ZERO, Money.ZERO);
  }

  /** Returns how much more the card may withdraw today. */
  public Money remainingDailyLimit(Session session) {
    LocalDate today = LocalDate.now(clock);
    Money used = Money.ZERO;
    for (Transaction t : host.transactionsFor(session.getCard().getCardNumber())) {
      if (t.getType() == TransactionType.WITHDRAWAL
          && t.getTimestamp().toLocalDate().equals(today)) {
        used = used.plus(t.getAmount());
      }
    }
    Money remaining = config.getDailyWithdrawalLimit().minus(used);
    return remaining.isNegative() ? Money.ZERO : remaining;
  }

  private void requireUsable(Session session, TransactionType type) throws IbankException {
    if (!session.isOpen()) {
      throw new IbankException("error.session.closed", "Session is closed");
    }
    if (!device.isInService()) {
      throw new OutOfServiceException();
    }
    if (!session.allows(type)) {
      throw new ServiceNotAllowedException(type.name());
    }
  }

  private Account account(Session session, String accountNumber) throws AccountNotFoundException {
    return session.getClient().findAccount(accountNumber)
        .orElseThrow(() -> new AccountNotFoundException(accountNumber));
  }

  private Transaction complete(Session session, TransactionType type, String from, String to,
      Money amount, Money balanceAfter) {
    Transaction tx = new Transaction(host.nextReference(), type,
        session.getCard().getCardNumber(), from, to, amount, balanceAfter,
        LocalDateTime.now(clock));
    host.record(tx);
    audit.info(tx.getReference() + " " + type + " " + amount + " by "
        + session.getCard().maskedNumber());
    if (compliance.observe(session.getClient().getClientId(), tx)) {
      audit.security("Reportable cash activity flagged at " + tx.getReference());
    }
    return tx;
  }

  private static String format(Session session, Money amount) {
    return amount.format(session.getLanguage().getLocale());
  }
}
