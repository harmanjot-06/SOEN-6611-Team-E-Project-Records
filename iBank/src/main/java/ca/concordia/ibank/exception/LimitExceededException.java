package ca.concordia.ibank.exception;

/** Raised when a per-transaction or daily limit would be exceeded. */
public class LimitExceededException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public LimitExceededException(String remaining) {
    super("error.tx.limitExceeded", "Limit exceeded; remaining " + remaining, remaining);
  }
}
