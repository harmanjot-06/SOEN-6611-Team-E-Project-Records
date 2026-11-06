package ca.concordia.ibank.ui;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** UC16 Authenticate Operator. */
public class OperatorLoginScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public OperatorLoginScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "operator.loginTitle";
  }

  @Override
  protected void build() {
    JTextField id = UiKit.field(new JTextField(10), text("field.operatorId"));
    JPasswordField pin = UiKit.field(new JPasswordField(6), text("field.pin"));
    addRow(UiKit.label(text("field.operatorId")), id);
    addRow(UiKit.label(text("field.pin")), pin);
    addButton(UiKit.primaryButton(text("button.ok"),
        e -> controller.operatorLogin(id.getText(), pin.getPassword())));
    addButton(UiKit.secondaryButton(text("button.cancel"), e -> controller.endSession()));
  }
}
