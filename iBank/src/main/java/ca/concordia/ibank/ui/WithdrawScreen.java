package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.Money;
import java.awt.GridLayout;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** UC2 Withdraw Cash, with quick amounts and the remaining daily limit shown up front. */
public class WithdrawScreen extends Screen {

  private static final long serialVersionUID = 1L;
  private static final int[] QUICK_AMOUNTS = {20, 60, 100, 200};

  public WithdrawScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.withdraw";
  }

  @Override
  protected void build() {
    Locale locale = messages().getLanguage().getLocale();
    AccountChooser account = new AccountChooser(
        controller.session().getClient().getAccounts(), messages(), true);
    addRow(UiKit.label(text("field.account")), account);

    JPanel quick = new JPanel(new GridLayout(1, 0, 8, 8));
    quick.setOpaque(false);
    for (int dollars : QUICK_AMOUNTS) {
      Money amount = Money.ofDollars(dollars);
      quick.add(UiKit.primaryButton(amount.format(locale), e -> withdraw(account, amount)));
    }
    addRow(UiKit.label(text("field.quickAmount")), quick);

    JTextField other = UiKit.field(new JTextField(8), text("field.otherAmount"));
    addRow(UiKit.label(text("field.otherAmount")), other);
    Money remaining = controller.transactions().remainingDailyLimit(controller.session());
    addRow(null, UiKit.hint(text("withdraw.hint", remaining.format(locale))));

    addButton(UiKit.primaryButton(text("button.withdraw"), e -> controller.perform(
        s -> controller.transactions().withdraw(s, account.selectedNumber(),
            Money.parse(other.getText())))));
    addBackToMenu();
  }

  private void withdraw(AccountChooser account, Money amount) {
    controller.perform(
        s -> controller.transactions().withdraw(s, account.selectedNumber(), amount));
  }
}
