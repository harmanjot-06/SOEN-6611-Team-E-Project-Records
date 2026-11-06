package ca.concordia.ibank.ui;

import ca.concordia.ibank.i18n.Messages;
import ca.concordia.ibank.model.Transaction;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formats a transaction as a receipt. Card and account numbers are masked. */
public final class ReceiptFormatter {

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private ReceiptFormatter() {
  }

  /** Returns the receipt text for a transaction in the language of {@code messages}. */
  public static String format(Transaction tx, Messages messages) {
    StringBuilder sb = new StringBuilder();
    sb.append("iBank Smart ABM\n");
    sb.append(TIME.format(tx.getTimestamp())).append('\n');
    appendIfPresent(sb, messages.get("receipt.reference"), tx.getReference());
    sb.append(messages.get("receipt.card")).append(": ****")
        .append(tx.getCardNumber().substring(tx.getCardNumber().length() - 4)).append('\n');
    sb.append(messages.get(tx.getType().getMessageKey())).append('\n');
    appendIfPresent(sb, messages.get("receipt.from"), mask(tx.getFromAccount()));
    appendIfPresent(sb, messages.get("receipt.to"), mask(tx.getToAccount()));
    final Locale locale = messages.getLanguage().getLocale();
    sb.append(messages.get("receipt.amount")).append(": ").append(tx.getAmount().format(locale))
        .append('\n');
    sb.append(messages.get("label.available")).append(": ")
        .append(tx.getBalanceAfter().format(locale)).append('\n');
    sb.append(messages.get("receipt.thanks"));
    return sb.toString();
  }

  private static void appendIfPresent(StringBuilder sb, String label, String value) {
    if (!value.isEmpty()) {
      sb.append(label).append(": ").append(value).append('\n');
    }
  }

  /** Masks account numbers but keeps payee names readable. */
  static String mask(String value) {
    if (value.isEmpty() || !Character.isDigit(value.charAt(0))) {
      return value;
    }
    return "****" + value.substring(Math.max(0, value.length() - 3));
  }
}
