package ca.concordia.ibank.model;

/** Kinds of transactions; each maps to a use case of the D1 use case model. */
public enum TransactionType {
  WITHDRAWAL("tx.withdrawal"),
  BALANCE_INQUIRY("tx.balance"),
  TRANSFER("tx.transfer"),
  DEPOSIT_CASH("tx.depositCash"),
  DEPOSIT_CHEQUE("tx.depositCheque"),
  BILL_PAYMENT("tx.billPayment"),
  PIN_CHANGE("tx.pinChange");

  private final String messageKey;

  TransactionType(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getMessageKey() {
    return messageKey;
  }

  /** Returns true if the transaction moves physical cash through the ABM. */
  public boolean isCash() {
    return this == WITHDRAWAL || this == DEPOSIT_CASH;
  }
}
