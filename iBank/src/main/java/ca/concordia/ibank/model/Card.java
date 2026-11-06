package ca.concordia.ibank.model;

import java.util.Objects;

/**
 * Debit card (or the card token of a mobile wallet). Only a salted hash of the PIN is stored.
 */
public class Card {

  private final String cardNumber;
  private final String clientId;
  private final CardIssuer issuer;
  private String pinHash;
  private int failedAttempts;
  private boolean locked;

  /** Creates a card; {@code pinHash} must come from {@code PinHasher.hash}. */
  public Card(String cardNumber, String clientId, CardIssuer issuer, String pinHash) {
    this.cardNumber = Objects.requireNonNull(cardNumber);
    this.clientId = Objects.requireNonNull(clientId);
    this.issuer = Objects.requireNonNull(issuer);
    this.pinHash = Objects.requireNonNull(pinHash);
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public String getClientId() {
    return clientId;
  }

  public CardIssuer getIssuer() {
    return issuer;
  }

  public String getPinHash() {
    return pinHash;
  }

  public void setPinHash(String pinHash) {
    this.pinHash = Objects.requireNonNull(pinHash);
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }

  /** Records a wrong PIN and returns the new number of consecutive failures. */
  public int recordFailedAttempt() {
    failedAttempts++;
    return failedAttempts;
  }

  public void resetFailedAttempts() {
    failedAttempts = 0;
  }

  public boolean isLocked() {
    return locked;
  }

  public void lock() {
    locked = true;
  }

  /** Returns the card number with only the last four digits visible. */
  public String maskedNumber() {
    return "**** **** **** " + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
  }
}
