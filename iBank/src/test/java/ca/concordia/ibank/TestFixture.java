package ca.concordia.ibank;

import ca.concordia.ibank.data.SampleData;
import ca.concordia.ibank.model.Language;
import ca.concordia.ibank.service.AbmConfig;
import ca.concordia.ibank.service.AbmDevice;
import ca.concordia.ibank.service.AuditLog;
import ca.concordia.ibank.service.AuthenticationService;
import ca.concordia.ibank.service.ComplianceMonitor;
import ca.concordia.ibank.service.InMemoryBankHost;
import ca.concordia.ibank.service.OperatorService;
import ca.concordia.ibank.service.Session;
import ca.concordia.ibank.service.TransactionService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/** Builds a complete iBank with the representative sample data and a controllable clock. */
public final class TestFixture {

  public final MutableClock clock = new MutableClock(Instant.parse("2026-11-02T14:00:00Z"));
  public final AbmConfig config = AbmConfig.defaults();
  public final AuditLog audit = new AuditLog();
  public final InMemoryBankHost host = new InMemoryBankHost();
  public final AbmDevice device = new AbmDevice("ABM-TEST-01");
  public final ComplianceMonitor compliance =
      new ComplianceMonitor(config.getReportableCashThreshold());
  public final OperatorService operators = new OperatorService(device, compliance, audit);
  public final AuthenticationService auth = new AuthenticationService(host, config, audit);
  public final TransactionService tx =
      new TransactionService(host, device, config, compliance, audit, clock);

  /** Creates the fixture with 200 x $20 notes loaded. */
  public TestFixture() {
    SampleData.populate(host);
    SampleData.registerOperators(operators);
    operators.replenish(20, 200);
  }

  public Session login(String card, String pin) throws Exception {
    return auth.authenticate(card, pin.toCharArray(), Language.ENGLISH);
  }

  /** Clock whose time the test can move forward. */
  public static final class MutableClock extends Clock {
    private Instant now;

    MutableClock(Instant start) {
      now = start;
    }

    public void advance(Duration d) {
      now = now.plus(d);
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("America/Montreal");
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }
}
