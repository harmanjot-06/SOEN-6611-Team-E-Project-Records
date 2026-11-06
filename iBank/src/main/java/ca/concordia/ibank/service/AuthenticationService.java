package ca.concordia.ibank.service;

import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.exception.CardLockedException;
import ca.concordia.ibank.exception.CardNotFoundException;
import ca.concordia.ibank.exception.IbankException;
import ca.concordia.ibank.model.Card;
import ca.concordia.ibank.model.Client;
import ca.concordia.ibank.model.Language;
import ca.concordia.ibank.security.PinHasher;
import java.util.Objects;

/** Implements use cases UC8 Authenticate Client and UC11 Lock Card. */
public class AuthenticationService {

  private final BankHost host;
  private final AbmConfig config;
  private final AuditLog audit;

  /** Creates the service. */
  public AuthenticationService(BankHost host, AbmConfig config, AuditLog audit) {
    this.host = Objects.requireNonNull(host);
    this.config = Objects.requireNonNull(config);
    this.audit = Objects.requireNonNull(audit);
  }

  /**
   * Checks that a card exists and can be used, before the PIN is asked for.
   *
   * @throws CardNotFoundException if the card is unknown
   * @throws CardLockedException if the card is already locked
   */
  public Card identifyCard(String cardNumber) throws IbankException {
    String normalized = normalize(cardNumber);
    Card card = host.findCard(normalized)
        .orElseThrow(() -> new CardNotFoundException(mask(normalized)));
    if (card.isLocked()) {
      throw new CardLockedException(card.maskedNumber());
    }
    return card;
  }

  /**
   * Verifies the PIN and opens a session. The card is locked after the configured number of
   * consecutive wrong PINs.
   *
   * @throws AuthenticationException if the PIN is wrong but attempts remain
   * @throws CardLockedException if the card is or becomes locked
   */
  public Session authenticate(String cardNumber, char[] pin, Language language)
      throws IbankException {
    Card card = identifyCard(cardNumber);
    if (!PinHasher.verify(pin, card.getPinHash())) {
      int failures = card.recordFailedAttempt();
      audit.security("Wrong PIN for " + card.maskedNumber() + " (" + failures + ")");
      if (failures >= config.getMaxPinAttempts()) {
        card.lock();
        audit.security("Card locked and retained: " + card.maskedNumber());
        throw new CardLockedException(card.maskedNumber());
      }
      throw new AuthenticationException(config.getMaxPinAttempts() - failures);
    }
    card.resetFailedAttempts();
    Client client = host.findClient(card.getClientId())
        .orElseThrow(() -> new CardNotFoundException(card.maskedNumber()));
    audit.info("Session opened for " + card.maskedNumber());
    return new Session(card, client, language);
  }

  /** Removes spaces and dashes that users often type in card numbers. */
  static String normalize(String cardNumber) {
    return cardNumber == null ? "" : cardNumber.replaceAll("[\\s-]", "");
  }

  private static String mask(String cardNumber) {
    int length = cardNumber.length();
    return length <= 4 ? "****" : "****" + cardNumber.substring(length - 4);
  }
}
