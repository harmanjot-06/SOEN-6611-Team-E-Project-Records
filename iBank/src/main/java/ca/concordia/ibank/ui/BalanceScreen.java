package ca.concordia.ibank.ui;

/** UC3 Check Balance. */
public class BalanceScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public BalanceScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.balance";
  }

  @Override
  protected void build() {
    AccountChooser account = new AccountChooser(
        controller.session().getClient().getAccounts(), messages(), false);
    addRow(UiKit.label(text("field.account")), account);
    addButton(UiKit.primaryButton(text("button.ok"), e -> controller.perform(
        s -> controller.transactions().checkBalance(s, account.selectedNumber()))));
    addBackToMenu();
  }
}
