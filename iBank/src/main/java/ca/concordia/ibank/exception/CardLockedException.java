package ca.concordia.ibank.exception;

/** Raised when a card is locked after too many wrong PINs. */
public class CardLockedException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public CardLockedException(String maskedCard) {
    super("error.auth.cardLocked", "Card locked: " + maskedCard);
  }
}
