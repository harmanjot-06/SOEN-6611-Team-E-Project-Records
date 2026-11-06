package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.Transaction;
import ca.concordia.ibank.model.TransactionType;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/** Shows the outcome of a transaction and offers UC10 Print Receipt. */
public class ResultScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public ResultScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "result.title";
  }

  @Override
  protected void build() {
    Transaction tx = controller.lastTransaction();
    if (tx == null) {
      return;
    }
    Locale locale = messages().getLanguage().getLocale();
    addRow(null, UiKit.label(summary(tx, locale)));
    if (tx.getType() == TransactionType.WITHDRAWAL) {
      addRow(null, UiKit.hint(text("result.takeCash")));
    }
    if (tx.getType() == TransactionType.DEPOSIT_CHEQUE) {
      addRow(null, UiKit.hint(text("result.chequeHold")));
    }
    if (tx.getType() != TransactionType.PIN_CHANGE) {
      addButton(UiKit.secondaryButton(text("button.receipt"), e -> printReceipt(tx, locale)));
    }
    addButton(UiKit.primaryButton(text("button.another"), e -> controller.show(ScreenId.MENU)));
    addButton(UiKit.secondaryButton(text("button.finish"), e -> controller.endSession()));
  }

  private String summary(Transaction tx, Locale locale) {
    String amount = tx.getAmount().format(locale);
    String balance = tx.getBalanceAfter().format(locale);
    switch (tx.getType()) {
      case BALANCE_INQUIRY:
        return text("result.balance", balance);
      case PIN_CHANGE:
        return text("result.pinChanged");
      default:
        return text("result.done", text(tx.getType().getMessageKey()), amount, balance);
    }
  }

  private void printReceipt(Transaction tx, Locale locale) {
    JTextArea receipt = new JTextArea(ReceiptFormatter.format(tx, messages()));
    receipt.setFont(UiKit.MONO_FONT);
    receipt.setEditable(false);
    JOptionPane.showMessageDialog(this, receipt, text("button.receipt"),
        JOptionPane.PLAIN_MESSAGE);
  }
}
