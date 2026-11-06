package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.Money;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/** UC5 Deposit Funds (cash or cheque). */
public class DepositScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public DepositScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.deposit";
  }

  @Override
  protected void build() {
    AccountChooser account = new AccountChooser(
        controller.session().getClient().getAccounts(), messages(), false);
    addRow(UiKit.label(text("field.toAccount")), account);

    JRadioButton cash = UiKit.field(new JRadioButton(text("tx.depositCash"), true),
        text("tx.depositCash"));
    JRadioButton cheque = UiKit.field(new JRadioButton(text("tx.depositCheque")),
        text("tx.depositCheque"));
    ButtonGroup group = new ButtonGroup();
    group.add(cash);
    group.add(cheque);
    JPanel kind = new JPanel();
    kind.setOpaque(false);
    kind.add(cash);
    kind.add(cheque);
    addRow(UiKit.label(text("field.depositKind")), kind);

    JTextField amount = UiKit.field(new JTextField(8), text("field.amount"));
    addRow(UiKit.label(text("field.amount")), amount);
    addRow(null, UiKit.hint(text("deposit.hint")));

    addButton(UiKit.primaryButton(text("button.deposit"), e -> controller.perform(
        s -> controller.transactions().deposit(s, account.selectedNumber(),
            Money.parse(amount.getText()), cheque.isSelected()))));
    addBackToMenu();
  }
}
