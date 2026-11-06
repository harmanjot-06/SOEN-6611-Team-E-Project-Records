package ca.concordia.ibank.service;

import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.security.PinHasher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Staff functions: UC16 Authenticate Operator, UC13 Replenish Cash, UC14 Collect Deposits and
 * UC15 Run Diagnostics.
 */
public class OperatorService {

  private final AbmDevice device;
  private final ComplianceMonitor compliance;
  private final AuditLog audit;
  private final Map<String, String> operatorPinHashes = new HashMap<>();

  /** Creates the service with no registered operators. */
  public OperatorService(AbmDevice device, ComplianceMonitor compliance, AuditLog audit) {
    this.device = Objects.requireNonNull(device);
    this.compliance = Objects.requireNonNull(compliance);
    this.audit = Objects.requireNonNull(audit);
  }

  /** Registers a cash custodian or service technician. */
  public void registerOperator(String operatorId, String pinHash) {
    operatorPinHashes.put(operatorId, pinHash);
  }

  /**
   * UC16 Authenticate Operator.
   *
   * @throws AuthenticationException if the identifier or PIN is wrong
   */
  public void authenticate(String operatorId, char[] pin) throws AuthenticationException {
    String stored = operatorPinHashes.get(operatorId);
    if (stored == null || !PinHasher.verify(pin, stored)) {
      audit.security("Failed operator login: " + PinHasher.fingerprint(String.valueOf(operatorId)));
      throw new AuthenticationException();
    }
    audit.info("Operator logged in: " + operatorId);
  }

  /** UC13 Replenish Cash. */
  public void replenish(int dollarValue, int count) {
    device.getDispenser().load(dollarValue, count);
    audit.info("Loaded " + count + " x $" + dollarValue);
  }

  /** UC14 Collect Deposits. */
  public Money collectDeposits() {
    Money collected = device.getDepositBin().collect();
    audit.info("Deposits collected: " + collected);
    return collected;
  }

  /** Puts the ABM in or out of service. */
  public void setInService(boolean inService) {
    device.setInService(inService);
    audit.info("ABM " + (inService ? "in service" : "out of service"));
  }

  public boolean isInService() {
    return device.isInService();
  }

  /** UC15 Run Diagnostics: returns one line per check. */
  public List<String> runDiagnostics() {
    List<String> lines = new ArrayList<>();
    lines.add("Terminal: " + device.getTerminalId());
    lines.add("In service: " + device.isInService());
    lines.add("Cash total: " + device.getDispenser().total());
    device.getDispenser().inventory()
        .forEach((value, count) -> lines.add("  $" + value + " notes: " + count));
    lines.add("Deposit bin: " + device.getDepositBin().getItems() + " items, "
        + device.getDepositBin().getTotal());
    lines.add("Compliance reports: " + compliance.getReports().size());
    compliance.getReports().forEach(r -> lines.add("  " + r));
    lines.add("Cash status: " + (device.getDispenser().total().compareTo(Money.ofDollars(200)) < 0
        ? "LOW - replenish" : "OK"));
    return lines;
  }
}
