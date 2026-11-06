package ca.concordia.ibank;

import ca.concordia.ibank.data.SampleData;
import ca.concordia.ibank.service.AbmConfig;
import ca.concordia.ibank.service.AbmDevice;
import ca.concordia.ibank.service.AuditLog;
import ca.concordia.ibank.service.AuthenticationService;
import ca.concordia.ibank.service.ComplianceMonitor;
import ca.concordia.ibank.service.InMemoryBankHost;
import ca.concordia.ibank.service.OperatorService;
import ca.concordia.ibank.service.TransactionService;
import ca.concordia.ibank.ui.AbmController;
import ca.concordia.ibank.ui.IbankFrame;
import java.time.Clock;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point: wires the simulated bank, the ABM services and the Swing user interface. */
public final class IbankApp {

  private IbankApp() {
  }

  /** Starts iBank. */
  public static void main(String[] args) {
    AbmConfig config = AbmConfig.defaults();
    AuditLog audit = new AuditLog();
    InMemoryBankHost host = new InMemoryBankHost();
    SampleData.populate(host);

    AbmDevice device = new AbmDevice("ABM-MTL-0042");
    ComplianceMonitor compliance = new ComplianceMonitor(config.getReportableCashThreshold());
    OperatorService operators = new OperatorService(device, compliance, audit);
    SampleData.registerOperators(operators);
    operators.replenish(20, 200);

    AuthenticationService auth = new AuthenticationService(host, config, audit);
    TransactionService transactions =
        new TransactionService(host, device, config, compliance, audit, Clock.systemDefaultZone());

    Thread.setDefaultUncaughtExceptionHandler(
        (thread, error) -> audit.error("Uncaught error in " + thread.getName(), error));
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
        audit.error("Look and feel not available; using default", e);
      }
      AbmController controller =
          new AbmController(auth, transactions, operators, config, audit);
      new IbankFrame(controller).setVisible(true);
    });
  }
}
