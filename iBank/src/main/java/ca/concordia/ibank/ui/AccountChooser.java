package ca.concordia.ibank.ui;

import ca.concordia.ibank.i18n.Messages;
import ca.concordia.ibank.model.Account;
import java.awt.Component;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

/** Combo box that lists accounts as "Chequing ****-001 (available $1,234.00)". Reusable. */
public class AccountChooser extends JComboBox<Account> {

  private static final long serialVersionUID = 1L;

  /** Creates a chooser for the given accounts. */
  public AccountChooser(List<Account> accounts, Messages messages, boolean showBalance) {
    super(accounts.toArray(new Account[0]));
    setFont(UiKit.BODY_FONT);
    getAccessibleContext().setAccessibleName(messages.get("field.account"));
    setRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
          boolean selected, boolean focus) {
        super.getListCellRendererComponent(list, value, index, selected, focus);
        if (value instanceof Account) {
          setText(describe((Account) value, messages, showBalance));
        }
        return this;
      }
    });
  }

  /** Returns the number of the selected account. */
  public String selectedNumber() {
    return ((Account) getSelectedItem()).getAccountNumber();
  }

  static String describe(Account a, Messages messages, boolean showBalance) {
    String text = messages.get(a.getType().getMessageKey()) + " " + a.maskedNumber();
    if (showBalance) {
      text += "  (" + messages.get("label.available") + " "
          + a.getAvailableBalance().format(messages.getLanguage().getLocale()) + ")";
    }
    return text;
  }
}
