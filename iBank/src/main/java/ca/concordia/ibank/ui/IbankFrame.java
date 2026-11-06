package ca.concordia.ibank.ui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/** Main window. Holds one {@link Screen} per {@link ScreenId} in a card layout. */
public class IbankFrame extends JFrame implements Navigator {

  private static final long serialVersionUID = 1L;

  private final transient AbmController controller;
  private final CardLayout cards = new CardLayout();
  private final JPanel deck = new JPanel(cards);
  private final Map<ScreenId, Screen> screens = new EnumMap<>(ScreenId.class);

  /** Creates the window and all screens. */
  public IbankFrame(AbmController controller) {
    super("iBank Smart ABM");
    this.controller = controller;
    controller.setNavigator(this);
    register(ScreenId.WELCOME, new WelcomeScreen(controller));
    register(ScreenId.PIN, new PinScreen(controller));
    register(ScreenId.MENU, new MenuScreen(controller));
    register(ScreenId.BALANCE, new BalanceScreen(controller));
    register(ScreenId.WITHDRAW, new WithdrawScreen(controller));
    register(ScreenId.DEPOSIT, new DepositScreen(controller));
    register(ScreenId.TRANSFER, new TransferScreen(controller));
    register(ScreenId.PAY_BILL, new PayBillScreen(controller));
    register(ScreenId.CHANGE_PIN, new ChangePinScreen(controller));
    register(ScreenId.RESULT, new ResultScreen(controller));
    register(ScreenId.OPERATOR_LOGIN, new OperatorLoginScreen(controller));
    register(ScreenId.OPERATOR, new OperatorScreen(controller));
    setContentPane(deck);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(820, 620));
    setLocationRelativeTo(null);
    show(ScreenId.WELCOME);
  }

  private void register(ScreenId id, Screen screen) {
    screens.put(id, screen);
    deck.add(screen, id.name());
  }

  @Override
  public void show(ScreenId screen) {
    screens.get(screen).refresh();
    cards.show(deck, screen.name());
  }

  @Override
  public void showError(String message) {
    String title = controller.messages().get("dialog.error");
    JOptionPane.showMessageDialog(this, UiKit.wrap(message), title, JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void showInfo(String message) {
    String title = controller.messages().get("dialog.info");
    JOptionPane.showMessageDialog(this, UiKit.wrap(message), title, JOptionPane.INFORMATION_MESSAGE);
  }
}
