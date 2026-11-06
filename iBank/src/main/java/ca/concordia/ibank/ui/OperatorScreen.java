package ca.concordia.ibank.ui;

import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.service.OperatorService;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/** Staff console: UC13 Replenish Cash, UC14 Collect Deposits, UC15 Run Diagnostics. */
public class OperatorScreen extends Screen {

  private static final long serialVersionUID = 1L;
  private static final Integer[] DENOMINATIONS = {5, 10, 20, 50, 100};

  public OperatorScreen(AbmController controller) {
    super(controller);
  }

  @Override
  protected String titleKey() {
    return "operator.title";
  }

  @Override
  protected void build() {
    if (!controller.isOperatorMode()) {
      return;
    }
    OperatorService ops = controller.operators();
    JTextArea report = new JTextArea(String.join("\n", ops.runDiagnostics()), 10, 50);
    report.setFont(UiKit.MONO_FONT);
    report.setEditable(false);
    addRow(null, new JScrollPane(report));

    JComboBox<Integer> denomination = UiKit.field(new JComboBox<>(DENOMINATIONS),
        text("field.denomination"));
    denomination.setSelectedItem(20);
    JSpinner count = UiKit.field(new JSpinner(new SpinnerNumberModel(100, 1, 2_000, 10)),
        text("field.count"));
    addRow(UiKit.label(text("field.denomination")), denomination);
    addRow(UiKit.label(text("field.count")), count);

    addButton(UiKit.primaryButton(text("operator.replenish"), e -> {
      ops.replenish((Integer) denomination.getSelectedItem(), (Integer) count.getValue());
      refresh();
    }));
    addButton(UiKit.secondaryButton(text("operator.collect"), e -> {
      Money collected = ops.collectDeposits();
      controller.showInfo(text("operator.collected",
          collected.format(messages().getLanguage().getLocale())));
      refresh();
    }));
    addButton(UiKit.secondaryButton(text("operator.toggle"), e -> {
      ops.setInService(!ops.isInService());
      refresh();
    }));
    addButton(UiKit.secondaryButton(text("button.logout"), e -> controller.endSession()));
  }
}
