package ca.concordia.ibank.exception;

/** Raised when a PIN or operator credential does not match. */
public class AuthenticationException extends IbankException {

  private static final long serialVersionUID = 1L;

  /** Wrong client PIN; tells the client how many attempts remain before the card is locked. */
  public AuthenticationException(int attemptsLeft) {
    super("error.auth.wrongPin", "Wrong PIN; attempts left: " + attemptsLeft, attemptsLeft);
  }

  /** Wrong credentials where no attempt counter applies (current PIN check, operator login). */
  public AuthenticationException() {
    super("error.auth.wrongCredentials", "Wrong credentials");
  }
}
