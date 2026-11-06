package ca.concordia.ibank.exception;

/**
 * Raised when an amount is empty, not a number, not positive, too precise, or not a multiple of
 * the smallest note. The message key names the rule that failed.
 */
public class InvalidAmountException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception for the rule identified by {@code messageKey}. */
  public InvalidAmountException(String messageKey, Object... arguments) {
    super(messageKey, "Invalid amount: " + messageKey, arguments);
  }
}
