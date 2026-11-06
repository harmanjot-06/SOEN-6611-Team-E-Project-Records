package ca.concordia.ibank.ui;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

/**
 * On-screen numeric keypad (reusable): types digits into a target field, like the tactile keypad
 * of a real ABM. The physical keyboard also works.
 */
public class KeypadPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  /** Creates a keypad that types into {@code target}; {@code clearLabel} names the clear key. */
  public KeypadPanel(JTextComponent target, String clearLabel) {
    super(new GridLayout(4, 3, 6, 6));
    setOpaque(false);
    String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", clearLabel, "0", "⌫"};
    for (String key : keys) {
      JButton button = new JButton(key);
      button.setFont(UiKit.BUTTON_FONT);
      button.getAccessibleContext().setAccessibleName(key.equals("⌫") ? "Backspace" : key);
      button.addActionListener(e -> press(target, key, clearLabel));
      add(button);
    }
  }

  private static void press(JTextComponent target, String key, String clearLabel) {
    String current = target.getText();
    if (key.equals(clearLabel)) {
      target.setText("");
    } else if (key.equals("⌫")) {
      target.setText(current.isEmpty() ? "" : current.substring(0, current.length() - 1));
    } else {
      target.setText(current + key);
    }
    target.requestFocusInWindow();
  }
}
