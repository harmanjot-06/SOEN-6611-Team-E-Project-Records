package ca.concordia.ibank.model;

/** Kinds of accounts a client can hold. */
public enum AccountType {
  CHEQUING("account.chequing"),
  SAVINGS("account.savings");

  private final String messageKey;

  AccountType(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
