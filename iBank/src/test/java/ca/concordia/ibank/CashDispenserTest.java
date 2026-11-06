package ca.concordia.ibank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.concordia.ibank.exception.CashUnavailableException;
import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.service.CashDispenser;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CashDispenserTest {

  @Test
  void dispensesUsingLargestNotesFirst() throws Exception {
    CashDispenser d = new CashDispenser();
    d.load(20, 10);
    d.load(50, 2);
    Map<Integer, Integer> notes = d.dispense(Money.ofDollars(140));
    assertEquals(Map.of(50, 2, 20, 2), notes);
    assertEquals(Money.ofDollars(160), d.total());
  }

  @Test
  void backtracksWhenGreedyChoiceFails() throws Exception {
    CashDispenser d = new CashDispenser();
    d.load(50, 1);
    d.load(20, 3);
    assertEquals(Map.of(20, 3), d.dispense(Money.ofDollars(60)));
  }

  @Test
  void refusesAmountsItCannotMake() {
    CashDispenser d = new CashDispenser();
    d.load(20, 2);
    assertFalse(d.canDispense(Money.ofDollars(60)));
    assertFalse(d.canDispense(Money.ofDollars(30)));
    assertTrue(d.canDispense(Money.ofDollars(40)));
    assertThrows(CashUnavailableException.class, () -> d.dispense(Money.ofDollars(60)));
    assertEquals(Money.ofDollars(40), d.total());
  }

  @Test
  void rejectsInvalidLoads() {
    CashDispenser d = new CashDispenser();
    assertThrows(IllegalArgumentException.class, () -> d.load(0, 5));
    assertThrows(IllegalArgumentException.class, () -> d.load(20, -1));
  }
}
