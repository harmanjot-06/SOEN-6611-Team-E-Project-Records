package ca.concordia.ibank.ui;

import ca.concordia.ibank.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Base class of every screen (template method): a title bar, a form in the centre and a row of
 * buttons at the bottom. Screens are rebuilt each time they are shown, so a language change
 * applies everywhere without extra code.
 */
public abstract class Screen extends JPanel {

  private static final long serialVersionUID = 1L;

  protected final transient AbmController controller;
  private JPanel form;
  private JPanel buttons;
  private int row;

  protected Screen(AbmController controller) {
    super(new BorderLayout());
    this.controller = controller;
    setBackground(UiKit.SURFACE);
  }

  /** Rebuilds the screen with fresh data and the current language. */
  public final void refresh() {
    removeAll();
    row = 0;
    form = new JPanel(new GridBagLayout());
    form.setBackground(UiKit.SURFACE);
    form.setBorder(UiKit.padding(24));
    buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
    buttons.setBackground(UiKit.SURFACE);
    add(UiKit.title(text(titleKey())), BorderLayout.NORTH);
    add(form, BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);
    build();
    revalidate();
    repaint();
  }

  /** Returns the message key of the screen title. */
  protected abstract String titleKey();

  /** Adds this screen's rows and buttons using {@link #addRow} and {@link #addButton}. */
  protected abstract void build();

  /** Adds a labelled row to the form; {@code label} may be null for a full-width row. */
  protected void addRow(JComponent label, JComponent component) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = row++;
    c.insets = new Insets(8, 8, 8, 8);
    c.anchor = GridBagConstraints.WEST;
    if (label != null) {
      c.gridx = 0;
      form.add(label, c);
      c.gridx = 1;
    } else {
      c.gridx = 0;
      c.gridwidth = 2;
    }
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    form.add(component, c);
  }

  protected void addButton(JButton button) {
    buttons.add(button);
  }

  /** Adds the standard Cancel button that returns to the main menu. */
  protected void addBackToMenu() {
    addButton(UiKit.secondaryButton(text("button.cancel"), e -> controller.show(ScreenId.MENU)));
  }

  protected Messages messages() {
    return controller.messages();
  }

  protected String text(String key, Object... args) {
    return controller.messages().get(key, args);
  }
}
