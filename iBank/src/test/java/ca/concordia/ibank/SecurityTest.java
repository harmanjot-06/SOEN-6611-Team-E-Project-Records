package ca.concordia.ibank;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.concordia.ibank.exception.InvalidPinFormatException;
import ca.concordia.ibank.security.PinHasher;
import ca.concordia.ibank.security.PinPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SecurityTest {

  @Test
  void hashesAreSaltedAndVerifiable() {
    String h1 = PinHasher.hash("2468");
    String h2 = PinHasher.hash("2468");
    assertNotEquals(h1, h2, "same PIN must give different hashes (salt)");
    assertFalse(h1.contains("2468"));
    assertTrue(PinHasher.verify("2468".toCharArray(), h1));
    assertFalse(PinHasher.verify("2469".toCharArray(), h1));
    assertFalse(PinHasher.verify("2468".toCharArray(), "corrupted"));
  }

  @ParameterizedTest
  @CsvSource({
      "12,12,error.pin.length", "1234567,1234567,error.pin.length",
      "12a4,12a4,error.pin.digits", "1111,1111,error.pin.trivial",
      "1234,1234,error.pin.trivial", "9876,9876,error.pin.trivial",
      "2580,2581,error.pin.mismatch"})
  void rejectsWeakOrMistypedPins(String pin, String confirm, String key) {
    InvalidPinFormatException e = assertThrows(InvalidPinFormatException.class,
        () -> PinPolicy.validate(pin.toCharArray(), confirm.toCharArray()));
    assertEquals(key, e.getMessageKey());
  }

  @Test
  void acceptsGoodPin() {
    assertDoesNotThrow(() -> PinPolicy.validate("2580".toCharArray(), "2580".toCharArray()));
  }
}
