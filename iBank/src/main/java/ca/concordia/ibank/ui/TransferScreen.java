package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.Account;
import ca.concordia.ibank.model.Money;
import java.util.List;
import javax.swing.JTextField;

/** UC4 Transfer Funds between the client's own accounts. */
public class TransferScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public TransferScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.transfer";
  }

  @Override
  protected void build() {
    List<Account> accounts = controller.session().getClient().getAccounts();
    AccountChooser from = new AccountChooser(accounts, messages(), true);
    AccountChooser to = new AccountChooser(accounts, messages(), false);
    if (accounts.size() > 1) {
      to.setSelectedIndex(1);
    }
    JTextField amount = UiKit.field(new JTextField(8), text("field.amount"));
    addRow(UiKit.label(text("field.fromAccount")), from);
    addRow(UiKit.label(text("field.toAccount")), to);
    addRow(UiKit.label(text("field.amount")), amount);
    addButton(UiKit.primaryButton(text("button.transfer"), e -> controller.perform(
        s -> controller.transactions().transfer(s, from.selectedNumber(), to.selectedNumber(),
            Money.parse(amount.getText())))));
    addBackToMenu();
  }
}
