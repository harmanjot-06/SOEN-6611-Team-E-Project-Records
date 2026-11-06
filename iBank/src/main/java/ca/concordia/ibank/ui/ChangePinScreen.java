package ca.concordia.ibank.ui;

import java.util.Arrays;
import javax.swing.JPasswordField;

/** UC7 Change PIN. */
public class ChangePinScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public ChangePinScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "menu.change_pin";
  }

  @Override
  protected void build() {
    JPasswordField current = UiKit.field(new JPasswordField(6), text("field.currentPin"));
    JPasswordField fresh = UiKit.field(new JPasswordField(6), text("field.newPin"));
    JPasswordField confirm = UiKit.field(new JPasswordField(6), text("field.confirmPin"));
    addRow(UiKit.label(text("field.currentPin")), current);
    addRow(UiKit.label(text("field.newPin")), fresh);
    addRow(UiKit.label(text("field.confirmPin")), confirm);
    addRow(null, UiKit.hint(text("pin.rules")));
    addButton(UiKit.primaryButton(text("button.ok"), e -> {
      char[] a = current.getPassword();
      char[] b = fresh.getPassword();
      char[] c = confirm.getPassword();
      try {
        controller.perform(s -> controller.transactions().changePin(s, a, b, c));
      } finally {
        Arrays.fill(a, '\0');
        Arrays.fill(b, '\0');
        Arrays.fill(c, '\0');
      }
    }));
    addBackToMenu();
  }
}
