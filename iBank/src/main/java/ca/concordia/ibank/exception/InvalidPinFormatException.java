package ca.concordia.ibank.exception;

/** Raised when a new PIN does not meet the PIN rules (4 to 6 digits, not trivial, confirmed). */
public class InvalidPinFormatException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception with the message key of the specific rule that failed. */
  public InvalidPinFormatException(String messageKey) {
    super(messageKey, "PIN rule failed: " + messageKey);
  }
}
