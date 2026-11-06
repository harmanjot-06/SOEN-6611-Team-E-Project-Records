package ca.concordia.ibank.exception;

/** Raised when a client tries a service their card does not allow. */
public class ServiceNotAllowedException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public ServiceNotAllowedException(String service) {
    super("error.tx.notAllowed", "Service not allowed for this client: " + service, service);
  }
}
