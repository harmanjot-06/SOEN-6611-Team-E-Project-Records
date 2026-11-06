package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Money;
import java.time.LocalDateTime;

/**
 * A large-cash event to be reported under the PCMLTFA (use case UC12). Aggregates cash of one
 * client over 24 hours, following the 24-hour rule.
 */
public final class ComplianceReport {

  private final String clientId;
  private final Money aggregateCash;
  private final String triggeringReference;
  private final LocalDateTime createdAt;

  ComplianceReport(String clientId, Money aggregateCash, String triggeringReference,
      LocalDateTime createdAt) {
    this.clientId = clientId;
    this.aggregateCash = aggregateCash;
    this.triggeringReference = triggeringReference;
    this.createdAt = createdAt;
  }

  public String getClientId() {
    return clientId;
  }

  public Money getAggregateCash() {
    return aggregateCash;
  }

  public String getTriggeringReference() {
    return triggeringReference;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return createdAt + "  client " + clientId + "  cash " + aggregateCash + "  ref "
        + triggeringReference;
  }
}
