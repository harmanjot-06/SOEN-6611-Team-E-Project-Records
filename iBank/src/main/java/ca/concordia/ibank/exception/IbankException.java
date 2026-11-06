package ca.concordia.ibank.exception;

/**
 * Root of all checked exceptions raised by iBank.
 *
 * <p>Each exception carries a message key that the user interface translates into a helpful,
 * bilingual error message, plus optional arguments that are inserted into that message.
 */
public class IbankException extends Exception {

  private static final long serialVersionUID = 1L;

  private final String messageKey;
  private final transient Object[] arguments;

  /**
   * Creates an exception.
   *
   * @param messageKey key of the user-facing message in the message bundle
   * @param technicalDetail detail intended for logs and developers
   * @param arguments values inserted into the user-facing message
   */
  public IbankException(String messageKey, String technicalDetail, Object... arguments) {
    super(technicalDetail);
    this.messageKey = messageKey;
    this.arguments = arguments.clone();
  }

  public String getMessageKey() {
    return messageKey;
  }

  public Object[] getArguments() {
    return arguments.clone();
  }
}
