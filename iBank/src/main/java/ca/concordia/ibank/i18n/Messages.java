package ca.concordia.ibank.i18n;

import ca.concordia.ibank.exception.IbankException;
import ca.concordia.ibank.model.Language;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Bilingual user-facing text, reusing {@link ResourceBundle} and {@link MessageFormat}. All text
 * lives in {@code i18n/messages_*.properties}, so no message is hard-coded in the user interface.
 */
public final class Messages {

  private static final String BUNDLE = "i18n.messages";

  private final Language language;
  private final ResourceBundle bundle;

  /** Loads the messages for a language. */
  public Messages(Language language) {
    this.language = language;
    this.bundle = ResourceBundle.getBundle(BUNDLE, language.getLocale());
  }

  public Language getLanguage() {
    return language;
  }

  /** Returns the text for {@code key}, with {@code arguments} inserted; never throws. */
  public String get(String key, Object... arguments) {
    String pattern;
    try {
      pattern = bundle.getString(key);
    } catch (MissingResourceException e) {
      return "[" + key + "]";
    }
    return new MessageFormat(pattern, language.getLocale()).format(arguments);
  }

  /** Returns the helpful message for an iBank exception. */
  public String describe(IbankException e) {
    return get(e.getMessageKey(), e.getArguments());
  }
}
