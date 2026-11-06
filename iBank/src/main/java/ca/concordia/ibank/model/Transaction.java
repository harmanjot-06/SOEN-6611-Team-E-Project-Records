package ca.concordia.ibank.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Immutable record of one completed transaction, used for receipts and the audit log. */
public final class Transaction {

  private final String reference;
  private final TransactionType type;
  private final String cardNumber;
  private final String fromAccount;
  private final String toAccount;
  private final Money amount;
  private final Money balanceAfter;
  private final LocalDateTime timestamp;

  /** Creates a transaction record; account fields may be empty when not applicable. */
  public Transaction(String reference, TransactionType type, String cardNumber, String fromAccount,
      String toAccount, Money amount, Money balanceAfter, LocalDateTime timestamp) {
    this.reference = Objects.requireNonNull(reference);
    this.type = Objects.requireNonNull(type);
    this.cardNumber = Objects.requireNonNull(cardNumber);
    this.fromAccount = fromAccount == null ? "" : fromAccount;
    this.toAccount = toAccount == null ? "" : toAccount;
    this.amount = Objects.requireNonNull(amount);
    this.balanceAfter = Objects.requireNonNull(balanceAfter);
    this.timestamp = Objects.requireNonNull(timestamp);
  }

  public String getReference() {
    return reference;
  }

  public TransactionType getType() {
    return type;
  }

  public String getCardNumber() {
    return cardNumber;
  }

  public String getFromAccount() {
    return fromAccount;
  }

  public String getToAccount() {
    return toAccount;
  }

  public Money getAmount() {
    return amount;
  }

  public Money getBalanceAfter() {
    return balanceAfter;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
