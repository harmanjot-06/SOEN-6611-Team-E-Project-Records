package ca.concordia.ibank.exception;

/** Raised when the ABM does not hold the notes needed for a withdrawal. */
public class CashUnavailableException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public CashUnavailableException(String amount) {
    super("error.tx.cashUnavailable", "ABM cannot dispense " + amount, amount);
  }
}
