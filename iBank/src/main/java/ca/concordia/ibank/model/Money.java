package ca.concordia.ibank.model;

import ca.concordia.ibank.exception.InvalidAmountException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable amount of Canadian dollars, stored in cents to avoid floating-point rounding errors.
 */
public final class Money implements Comparable<Money> {

  public static final Money ZERO = new Money(0L);
  private static final int CENTS_PER_DOLLAR = 100;

  private final long cents;

  private Money(long cents) {
    this.cents = cents;
  }

  /** Returns an amount of the given number of cents. */
  public static Money ofCents(long cents) {
    return new Money(cents);
  }

  /** Returns an amount of the given number of whole dollars. */
  public static Money ofDollars(long dollars) {
    return new Money(Math.multiplyExact(dollars, CENTS_PER_DOLLAR));
  }

  /**
   * Parses text typed by a user, such as {@code "120"}, {@code "120.50"} or {@code "120,50"}.
   *
   * @throws InvalidAmountException if the text is empty, not a number, not positive, or has more
   *     than two decimals
   */
  public static Money parse(String text) throws InvalidAmountException {
    if (text == null || text.isBlank()) {
      throw new InvalidAmountException("error.amount.empty");
    }
    String normalized = text.trim().replace(',', '.').replace("$", "");
    BigDecimal value;
    try {
      value = new BigDecimal(normalized);
    } catch (NumberFormatException e) {
      throw new InvalidAmountException("error.amount.notNumber", text.trim());
    }
    if (value.signum() <= 0) {
      throw new InvalidAmountException("error.amount.notPositive");
    }
    if (value.scale() > 2) {
      throw new InvalidAmountException("error.amount.tooPrecise");
    }
    try {
      return new Money(value.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY)
          .longValueExact());
    } catch (ArithmeticException e) {
      throw new InvalidAmountException("error.amount.tooLarge");
    }
  }

  public long cents() {
    return cents;
  }

  public Money plus(Money other) {
    return new Money(Math.addExact(cents, other.cents));
  }

  public Money minus(Money other) {
    return new Money(Math.subtractExact(cents, other.cents));
  }

  public boolean isGreaterThan(Money other) {
    return cents > other.cents;
  }

  public boolean isNegative() {
    return cents < 0;
  }

  /** Returns true if this amount is a whole multiple of {@code unit}. */
  public boolean isMultipleOf(Money unit) {
    return unit.cents != 0 && cents % unit.cents == 0;
  }

  /** Returns the smaller of two amounts. */
  public static Money min(Money a, Money b) {
    return a.compareTo(b) <= 0 ? a : b;
  }

  /** Formats this amount for a locale, for example {@code $1,234.50} or {@code 1 234,50 $}. */
  public String format(Locale locale) {
    NumberFormat format = NumberFormat.getCurrencyInstance(locale);
    format.setCurrency(java.util.Currency.getInstance("CAD"));
    return format.format(BigDecimal.valueOf(cents, 2));
  }

  @Override
  public int compareTo(Money other) {
    return Long.compare(cents, other.cents);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Money && ((Money) o).cents == cents;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cents);
  }

  @Override
  public String toString() {
    return format(Locale.CANADA);
  }
}
