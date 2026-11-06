package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Money;

/** Holds cash and cheques deposited since the last collection (use case UC14). */
public class DepositBin {

  private int items;
  private Money total = Money.ZERO;

  /** Accepts one deposit. */
  public synchronized void accept(Money amount) {
    items++;
    total = total.plus(amount);
  }

  public synchronized int getItems() {
    return items;
  }

  public synchronized Money getTotal() {
    return total;
  }

  /** Empties the bin and returns the total collected. */
  public synchronized Money collect() {
    Money collected = total;
    items = 0;
    total = Money.ZERO;
    return collected;
  }
}
