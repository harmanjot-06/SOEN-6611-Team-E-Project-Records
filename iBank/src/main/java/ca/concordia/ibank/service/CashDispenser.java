package ca.concordia.ibank.service;

import ca.concordia.ibank.exception.CashUnavailableException;
import ca.concordia.ibank.model.Money;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Note cassettes of the ABM. Works for any set of denominations: it searches for a combination
 * of the notes actually loaded, largest notes first.
 */
public class CashDispenser {

  private final NavigableMap<Integer, Integer> notesByDollarValue = new TreeMap<>();

  /** Loads {@code count} notes of {@code dollarValue} (use case UC13 Replenish Cash). */
  public synchronized void load(int dollarValue, int count) {
    if (dollarValue <= 0 || count <= 0) {
      throw new IllegalArgumentException("Denomination and count must be positive");
    }
    notesByDollarValue.merge(dollarValue, count, Integer::sum);
  }

  /** Returns the total cash held. */
  public synchronized Money total() {
    long dollars = 0;
    for (Map.Entry<Integer, Integer> e : notesByDollarValue.entrySet()) {
      dollars += (long) e.getKey() * e.getValue();
    }
    return Money.ofDollars(dollars);
  }

  /** Returns a read-only view of notes per denomination, largest first. */
  public synchronized Map<Integer, Integer> inventory() {
    return Collections.unmodifiableMap(new TreeMap<>(notesByDollarValue.descendingMap()));
  }

  /** Returns true if the amount can be made from the notes currently loaded. */
  public synchronized boolean canDispense(Money amount) {
    return plan(amount) != null;
  }

  /**
   * Removes the notes for {@code amount} and returns how many of each were dispensed.
   *
   * @throws CashUnavailableException if the notes loaded cannot make up the amount
   */
  public synchronized Map<Integer, Integer> dispense(Money amount) throws CashUnavailableException {
    Map<Integer, Integer> plan = plan(amount);
    if (plan == null) {
      throw new CashUnavailableException(amount.format(Locale.CANADA));
    }
    plan.forEach((value, count) -> notesByDollarValue.merge(value, -count, Integer::sum));
    notesByDollarValue.values().removeIf(c -> c == 0);
    return plan;
  }

  private Map<Integer, Integer> plan(Money amount) {
    if (amount.cents() % 100 != 0) {
      return null;
    }
    Integer[] values = notesByDollarValue.descendingKeySet().toArray(new Integer[0]);
    Map<Integer, Integer> result = new TreeMap<>(Collections.reverseOrder());
    return search(values, 0, amount.cents() / 100, result) ? result : null;
  }

  private boolean search(Integer[] values, int index, long remaining,
      Map<Integer, Integer> result) {
    if (remaining == 0) {
      return true;
    }
    if (index == values.length) {
      return false;
    }
    int value = values[index];
    long most = Math.min(remaining / value, notesByDollarValue.get(value));
    for (long count = most; count >= 0; count--) {
      if (count > 0) {
        result.put(value, (int) count);
      } else {
        result.remove(value);
      }
      if (search(values, index + 1, remaining - count * value, result)) {
        return true;
      }
    }
    return false;
  }
}
