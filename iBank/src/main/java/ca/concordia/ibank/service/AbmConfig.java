package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Money;
import java.time.Duration;

/**
 * Configurable operating rules of one ABM. The defaults are the D1 assumptions A2 to A5.
 */
public final class AbmConfig {

  private final Money perWithdrawalLimit;
  private final Money dailyWithdrawalLimit;
  private final Money withdrawalUnit;
  private final int maxPinAttempts;
  private final Duration sessionTimeout;
  private final Money chequeImmediateAvailability;
  private final Money reportableCashThreshold;

  private AbmConfig(Builder b) {
    this.perWithdrawalLimit = b.perWithdrawalLimit;
    this.dailyWithdrawalLimit = b.dailyWithdrawalLimit;
    this.withdrawalUnit = b.withdrawalUnit;
    this.maxPinAttempts = b.maxPinAttempts;
    this.sessionTimeout = b.sessionTimeout;
    this.chequeImmediateAvailability = b.chequeImmediateAvailability;
    this.reportableCashThreshold = b.reportableCashThreshold;
  }

  /** Returns the default configuration. */
  public static AbmConfig defaults() {
    return new Builder().build();
  }

  public Money getPerWithdrawalLimit() {
    return perWithdrawalLimit;
  }

  public Money getDailyWithdrawalLimit() {
    return dailyWithdrawalLimit;
  }

  public Money getWithdrawalUnit() {
    return withdrawalUnit;
  }

  public int getMaxPinAttempts() {
    return maxPinAttempts;
  }

  public Duration getSessionTimeout() {
    return sessionTimeout;
  }

  public Money getChequeImmediateAvailability() {
    return chequeImmediateAvailability;
  }

  public Money getReportableCashThreshold() {
    return reportableCashThreshold;
  }

  /** Builder so that a bank can change any rule without touching the code that uses it. */
  public static final class Builder {
    private Money perWithdrawalLimit = Money.ofDollars(500);
    private Money dailyWithdrawalLimit = Money.ofDollars(1_000);
    private Money withdrawalUnit = Money.ofDollars(20);
    private int maxPinAttempts = 3;
    private Duration sessionTimeout = Duration.ofSeconds(30);
    private Money chequeImmediateAvailability = Money.ofDollars(100);
    private Money reportableCashThreshold = Money.ofDollars(10_000);

    public Builder perWithdrawalLimit(Money value) {
      perWithdrawalLimit = value;
      return this;
    }

    public Builder dailyWithdrawalLimit(Money value) {
      dailyWithdrawalLimit = value;
      return this;
    }

    public Builder withdrawalUnit(Money value) {
      withdrawalUnit = value;
      return this;
    }

    public Builder maxPinAttempts(int value) {
      maxPinAttempts = value;
      return this;
    }

    public Builder sessionTimeout(Duration value) {
      sessionTimeout = value;
      return this;
    }

    public Builder chequeImmediateAvailability(Money value) {
      chequeImmediateAvailability = value;
      return this;
    }

    public Builder reportableCashThreshold(Money value) {
      reportableCashThreshold = value;
      return this;
    }

    public AbmConfig build() {
      return new AbmConfig(this);
    }
  }
}
