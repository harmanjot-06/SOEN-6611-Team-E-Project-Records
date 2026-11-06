package ca.concordia.ibank.exception;

/** Raised when an account does not hold enough available funds. */
public class InsufficientFundsException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public InsufficientFundsException(String available) {
    super("error.tx.insufficientFunds", "Insufficient funds; available " + available, available);
  }
}
