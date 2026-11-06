package ca.concordia.ibank.exception;

/** Raised when a client transaction is attempted while the ABM is out of service. */
public class OutOfServiceException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public OutOfServiceException() {
    super("error.abm.outOfService", "ABM is out of service");
  }
}
