package ca.concordia.ibank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.concordia.ibank.data.SampleData;
import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.exception.CardLockedException;
import ca.concordia.ibank.exception.CardNotFoundException;
import ca.concordia.ibank.service.Session;
import org.junit.jupiter.api.Test;

class AuthenticationServiceTest {

  private final TestFixture f = new TestFixture();

  @Test
  void correctPinOpensSession() throws Exception {
    Session s = f.login("4520 0000 0000 1234", "2468");
    assertTrue(s.isOpen());
    assertEquals("Alice Tremblay", s.getClient().getName());
  }

  @Test
  void wrongPinTellsAttemptsLeftThenLocksCard() {
    AuthenticationException first = assertThrows(AuthenticationException.class,
        () -> f.login(SampleData.ALICE_CARD, "0000"));
    assertEquals(2, first.getArguments()[0]);
    assertThrows(AuthenticationException.class, () -> f.login(SampleData.ALICE_CARD, "0000"));
    assertThrows(CardLockedException.class, () -> f.login(SampleData.ALICE_CARD, "0000"));
    assertThrows(CardLockedException.class, () -> f.login(SampleData.ALICE_CARD, "2468"),
        "a locked card stays locked even with the right PIN");
  }

  @Test
  void successfulLoginResetsFailureCounter() throws Exception {
    assertThrows(AuthenticationException.class, () -> f.login(SampleData.ALICE_CARD, "0000"));
    assertThrows(AuthenticationException.class, () -> f.login(SampleData.ALICE_CARD, "0000"));
    f.login(SampleData.ALICE_CARD, "2468");
    AuthenticationException again = assertThrows(AuthenticationException.class,
        () -> f.login(SampleData.ALICE_CARD, "0000"));
    assertEquals(2, again.getArguments()[0]);
  }

  @Test
  void unknownAndLockedCardsAreRefused() {
    assertThrows(CardNotFoundException.class, () -> f.auth.identifyCard("9999000011112222"));
    assertThrows(CardNotFoundException.class, () -> f.auth.identifyCard(""));
    assertThrows(CardLockedException.class, () -> f.auth.identifyCard(SampleData.LOCKED_CARD));
  }

  @Test
  void auditLogNeverContainsPinsOrFullCardNumbers() {
    assertThrows(AuthenticationException.class, () -> f.login(SampleData.ALICE_CARD, "0000"));
    for (String entry : f.audit.entries()) {
      assertTrue(!entry.contains("0000") && !entry.contains(SampleData.ALICE_CARD), entry);
    }
  }
}
