package ca.concordia.ibank.model;

import java.util.Locale;

/** Languages offered by iBank (English and French, as required for use in Canada). */
public enum Language {
  ENGLISH(Locale.CANADA, "English"),
  FRENCH(Locale.CANADA_FRENCH, "Français");

  private final Locale locale;
  private final String nativeName;

  Language(Locale locale, String nativeName) {
    this.locale = locale;
    this.nativeName = nativeName;
  }

  public Locale getLocale() {
    return locale;
  }

  public String getNativeName() {
    return nativeName;
  }
}
