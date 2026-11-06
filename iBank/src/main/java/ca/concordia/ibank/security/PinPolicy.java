package ca.concordia.ibank.security;

import ca.concordia.ibank.exception.InvalidPinFormatException;
import java.util.Arrays;

/** Rules a new PIN must satisfy. */
public final class PinPolicy {

  public static final int MIN_LENGTH = 4;
  public static final int MAX_LENGTH = 6;

  private PinPolicy() {
  }

  /**
   * Checks a proposed PIN.
   *
   * @throws InvalidPinFormatException naming the first rule that fails
   */
  public static void validate(char[] newPin, char[] confirmation) throws InvalidPinFormatException {
    if (newPin.length < MIN_LENGTH || newPin.length > MAX_LENGTH) {
      throw new InvalidPinFormatException("error.pin.length");
    }
    for (char c : newPin) {
      if (c < '0' || c > '9') {
        throw new InvalidPinFormatException("error.pin.digits");
      }
    }
    if (isTrivial(newPin)) {
      throw new InvalidPinFormatException("error.pin.trivial");
    }
    if (!Arrays.equals(newPin, confirmation)) {
      throw new InvalidPinFormatException("error.pin.mismatch");
    }
  }

  /** Returns true for PINs such as 1111, 1234 or 9876 that are easy to guess. */
  static boolean isTrivial(char[] pin) {
    boolean allSame = true;
    boolean ascending = true;
    boolean descending = true;
    for (int i = 1; i < pin.length; i++) {
      int step = pin[i] - pin[i - 1];
      allSame &= step == 0;
      ascending &= step == 1;
      descending &= step == -1;
    }
    return allSame || ascending || descending;
  }
}
