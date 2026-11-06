package ca.concordia.ibank.model;

/**
 * Who issued a card. iBank clients may use every client service; holders of another bank's card
 * may only withdraw cash and check a balance (D1, actor Other-Bank Client).
 */
public enum CardIssuer {
  IBANK,
  OTHER_BANK;

  /** Returns true if a holder of a card from this issuer may use the service. */
  public boolean allows(TransactionType service) {
    return this == IBANK || service == TransactionType.WITHDRAWAL
        || service == TransactionType.BALANCE_INQUIRY;
  }
}
