package ca.concordia.ibank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.concordia.ibank.exception.InvalidAmountException;
import ca.concordia.ibank.model.Money;
import java.util.Locale;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @ParameterizedTest
  @CsvSource({"120,12000", "120.50,12050", "'120,5',12050", "$40,4000", " 0.01 ,1"})
  void parsesValidUserInput(String text, long cents) throws Exception {
    assertEquals(cents, Money.parse(text).cents());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "abc", "-20", "0", "10.001", "1e3x"})
  void rejectsInvalidUserInput(String text) {
    assertThrows(InvalidAmountException.class, () -> Money.parse(text));
  }

  @Test
  void reportsTheRuleThatFailed() {
    InvalidAmountException e = assertThrows(InvalidAmountException.class,
        () -> Money.parse("-5"));
    assertEquals("error.amount.notPositive", e.getMessageKey());
  }

  @Test
  void arithmeticAndMultiples() {
    Money a = Money.ofDollars(60);
    assertEquals(Money.ofDollars(80), a.plus(Money.ofDollars(20)));
    assertTrue(a.isMultipleOf(Money.ofDollars(20)));
    assertFalse(Money.ofDollars(50).isMultipleOf(Money.ofDollars(20)));
    assertTrue(Money.ofDollars(10).minus(a).isNegative());
  }

  @Test
  void formatsInBothOfficialLanguages() {
    assertEquals("$1,234.50", Money.ofCents(123_450).format(Locale.CANADA));
    String french = Money.ofCents(123_450).format(Locale.CANADA_FRENCH);
    assertTrue(french.startsWith("1") && french.contains("234,50") && french.endsWith("$"),
        french);
  }
}
