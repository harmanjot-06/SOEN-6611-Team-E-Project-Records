package ca.concordia.ibank.ui;

import javax.swing.JLabel;
import javax.swing.JPasswordField;

/** UC8 Authenticate Client: PIN entry, shielded and never shown. */
public class PinScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public PinScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "pin.title";
  }

  @Override
  protected void build() {
    JPasswordField pin = UiKit.field(new JPasswordField(6), text("field.pin"));
    JLabel label = UiKit.label(text("field.pin"));
    label.setLabelFor(pin);
    addRow(null, UiKit.label(text("pin.prompt")));
    addRow(label, pin);
    addRow(null, new KeypadPanel(pin, text("button.clear")));
    pin.addActionListener(e -> submit(pin));
    addButton(UiKit.primaryButton(text("button.ok"), e -> submit(pin)));
    addButton(UiKit.secondaryButton(text("button.cancel"), e -> controller.endSession()));
    javax.swing.SwingUtilities.invokeLater(pin::requestFocusInWindow);
  }

  private void submit(JPasswordField pin) {
    char[] entered = pin.getPassword();
    pin.setText("");
    controller.submitPin(entered);
  }
}
