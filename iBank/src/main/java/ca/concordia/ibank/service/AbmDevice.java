package ca.concordia.ibank.service;

/** Physical parts of one ABM and whether it is in service. */
public class AbmDevice {

  private final String terminalId;
  private final CashDispenser dispenser;
  private final DepositBin depositBin;
  private volatile boolean inService = true;

  /** Creates a device with empty cassettes and an empty deposit bin. */
  public AbmDevice(String terminalId) {
    this.terminalId = terminalId;
    this.dispenser = new CashDispenser();
    this.depositBin = new DepositBin();
  }

  public String getTerminalId() {
    return terminalId;
  }

  public CashDispenser getDispenser() {
    return dispenser;
  }

  public DepositBin getDepositBin() {
    return depositBin;
  }

  public boolean isInService() {
    return inService;
  }

  public void setInService(boolean inService) {
    this.inService = inService;
  }
}
