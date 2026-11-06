package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.AccessMethod;
import ca.concordia.ibank.model.Language;
import java.awt.FlowLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** UC1 Select Language and the start of UC8: present a card, phone or QR code. */
public class WelcomeScreen extends Screen {

  private static final long serialVersionUID = 1L;

  public WelcomeScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "welcome.title";
  }

  @Override
  protected void build() {
    JPanel languages = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    languages.setOpaque(false);
    for (Language language : Language.values()) {
      languages.add(UiKit.secondaryButton(language.getNativeName(),
          e -> controller.selectLanguage(language)));
    }
    addRow(null, UiKit.label(text("welcome.prompt")));
    addRow(UiKit.label(text("field.language")), languages);

    JComboBox<AccessMethod> method = UiKit.field(new JComboBox<>(AccessMethod.values()),
        text("field.accessMethod"));
    method.setRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
          Object value, int index, boolean selected, boolean focus) {
        super.getListCellRendererComponent(list, value, index, selected, focus);
        setText(text(((AccessMethod) value).getMessageKey()));
        return this;
      }
    });
    addRow(UiKit.label(text("field.accessMethod")), method);

    JTextField cardNumber = UiKit.field(new JTextField(19), text("field.cardNumber"));
    JLabel cardLabel = UiKit.label(text("field.cardNumber"));
    cardLabel.setLabelFor(cardNumber);
    addRow(cardLabel, cardNumber);
    addRow(null, UiKit.hint(text("welcome.hint")));

    cardNumber.addActionListener(e -> controller.presentCard(cardNumber.getText()));
    addButton(UiKit.primaryButton(text("button.continue"),
        e -> controller.presentCard(cardNumber.getText())));
    addButton(UiKit.secondaryButton(text("button.staff"),
        e -> controller.show(ScreenId.OPERATOR_LOGIN)));
  }
}
