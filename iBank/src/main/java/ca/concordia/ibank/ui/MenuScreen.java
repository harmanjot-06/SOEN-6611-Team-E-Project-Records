package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.TransactionType;
import ca.concordia.ibank.service.Session;
import java.awt.GridLayout;
import javax.swing.JPanel;

/** Main menu. Only the services the client's card allows are offered. */
public class MenuScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public MenuScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.title";
  }

  @Override
  protected void build() {
    Session session = controller.session();
    if (session == null) {
      return;
    }
    addRow(null, UiKit.label(text("menu.greeting", session.getClient().getName())));
    JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
    grid.setOpaque(false);
    addService(grid, session, TransactionType.WITHDRAWAL, ScreenId.WITHDRAW);
    addService(grid, session, TransactionType.BALANCE_INQUIRY, ScreenId.BALANCE);
    addService(grid, session, TransactionType.DEPOSIT_CASH, ScreenId.DEPOSIT);
    addService(grid, session, TransactionType.TRANSFER, ScreenId.TRANSFER);
    addService(grid, session, TransactionType.BILL_PAYMENT, ScreenId.PAY_BILL);
    addService(grid, session, TransactionType.PIN_CHANGE, ScreenId.CHANGE_PIN);
    addRow(null, grid);
    if (!session.allows(TransactionType.TRANSFER)) {
      addRow(null, UiKit.hint(text("menu.otherBank")));
    }
    addButton(UiKit.secondaryButton(text("button.finish"), e -> controller.endSession()));
  }

  private void addService(JPanel grid, Session session, TransactionType type, ScreenId target) {
    if (session.allows(type)) {
      grid.add(UiKit.primaryButton(text("menu." + target.name().toLowerCase()),
          e -> controller.show(target)));
    }
  }
}
