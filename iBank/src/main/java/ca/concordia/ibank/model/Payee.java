package ca.concordia.ibank.model;

import java.util.Objects;

/** A company a client has registered for bill payments, such as a utility. */
public final class Payee {

  private final String payeeId;
  private final String name;
  private final String referenceNumber;

  /** Creates a payee with the client's reference number at that company. */
  public Payee(String payeeId, String name, String referenceNumber) {
    this.payeeId = Objects.requireNonNull(payeeId);
    this.name = Objects.requireNonNull(name);
    this.referenceNumber = Objects.requireNonNull(referenceNumber);
  }

  public String getPayeeId() {
    return payeeId;
  }

  public String getName() {
    return name;
  }

  public String getReferenceNumber() {
    return referenceNumber;
  }

  @Override
  public String toString() {
    return name + " (" + referenceNumber + ")";
  }
}
