package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.model.Payee;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;

/** UC6 Pay Bill to a registered payee. */
public class PayBillScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public PayBillScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.pay_bill";
  }

  @Override
  protected void build() {
    List<Payee> payees = controller.session().getClient().getPayees();
    if (payees.isEmpty()) {
      addRow(null, UiKit.label(text("paybill.none")));
      addBackToMenu();
      return;
    }
    JComboBox<Payee> payee = UiKit.field(new JComboBox<>(payees.toArray(new Payee[0])),
        text("field.payee"));
    AccountChooser from = new AccountChooser(
        controller.session().getClient().getAccounts(), messages(), true);
    JTextField amount = UiKit.field(new JTextField(8), text("field.amount"));
    addRow(UiKit.label(text("field.payee")), payee);
    addRow(UiKit.label(text("field.fromAccount")), from);
    addRow(UiKit.label(text("field.amount")), amount);
    addButton(UiKit.primaryButton(text("button.pay"), e -> controller.perform(
        s -> controller.transactions().payBill(s, ((Payee) payee.getSelectedItem()).getPayeeId(),
            from.selectedNumber(), Money.parse(amount.getText())))));
    addBackToMenu();
  }
}
