package ca.concordia.ibank.exception;

/** Raised when an account or payee cannot be found. */
public class AccountNotFoundException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Creates the exception. */
  public AccountNotFoundException(String accountId) {
    super("error.tx.accountNotFound", "Account not found: " + accountId);
  }
}
