package ca.concordia.ibank.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/** Shared look of iBank: colours, fonts and factory methods for common widgets. */
public final class UiKit {

  public static final Color BRAND = new Color(0x91, 0x23, 0x38);
  public static final Color BRAND_DARK = new Color(0x5E, 0x16, 0x24);
  public static final Color SURFACE = Color.WHITE;
  public static final Color MUTED = new Color(0x55, 0x55, 0x55);
  public static final Color ERROR = new Color(0xB0, 0x00, 0x20);

  public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 26);
  public static final Font BODY_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
  public static final Font BUTTON_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);
  public static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 15);

  private UiKit() {
  }

  /** Large, high-contrast button for main actions; accessible by keyboard. */
  public static JButton primaryButton(String text, ActionListener action) {
    JButton button = baseButton(text, action);
    button.setBackground(BRAND);
    button.setForeground(Color.WHITE);
    return button;
  }

  /** Button for secondary actions such as Cancel or Back. */
  public static JButton secondaryButton(String text, ActionListener action) {
    JButton button = baseButton(text, action);
    button.setBackground(new Color(0xEE, 0xEE, 0xEE));
    button.setForeground(BRAND_DARK);
    return button;
  }

  private static JButton baseButton(String text, ActionListener action) {
    JButton button = new JButton(text);
    button.setFont(BUTTON_FONT);
    button.setFocusPainted(true);
    Dimension size = button.getPreferredSize();
    button.setPreferredSize(new Dimension(Math.max(220, size.width + 32), 52));
    button.getAccessibleContext().setAccessibleName(text);
    button.addActionListener(action);
    return button;
  }

  /** Screen title in the brand colour. */
  public static JLabel title(String text) {
    JLabel label = new JLabel(text, SwingConstants.CENTER);
    label.setFont(TITLE_FONT);
    label.setForeground(Color.WHITE);
    label.setOpaque(true);
    label.setBackground(BRAND);
    label.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
    return label;
  }

  /** Body text label. */
  public static JLabel label(String text) {
    JLabel label = new JLabel(text);
    label.setFont(BODY_FONT);
    return label;
  }

  /** Small grey hint under a field. */
  public static JLabel hint(String text) {
    JLabel label = new JLabel(text);
    label.setFont(BODY_FONT.deriveFont(14f));
    label.setForeground(MUTED);
    return label;
  }

  /** Applies the body font to an input component and labels it for screen readers. */
  public static <T extends JComponent> T field(T component, String accessibleName) {
    component.setFont(BODY_FONT);
    component.getAccessibleContext().setAccessibleName(accessibleName);
    return component;
  }

  /** Wraps a message so that long text breaks into lines in dialogs instead of being cut. */
  public static String wrap(String message) {
    String escaped = message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    return "<html><body style='width: 360px; font-size: 14pt'>" + escaped + "</body></html>";
  }

  public static Border padding(int size) {
    return BorderFactory.createEmptyBorder(size, size, size, size);
  }
}
