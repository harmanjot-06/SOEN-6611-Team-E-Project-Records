package ca.concordia.ibank.exception;

/** Raised when a card number is not known to the bank host. */
public class CardNotFoundException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public CardNotFoundException(String maskedCard) {
    super("error.auth.unknownCard", "Unknown card: " + maskedCard);
  }
}
