package ca.concordia.ibank;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.concordia.ibank.data.SampleData;
import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.exception.InsufficientFundsException;
import ca.concordia.ibank.i18n.Messages;
import ca.concordia.ibank.model.Language;
import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.ui.ReceiptFormatter;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class OperatorAndMessagesTest {

  private final TestFixture f = new TestFixture();

  @Test
  void operatorMustAuthenticate() {
    assertDoesNotThrow(() -> f.operators.authenticate(SampleData.OPERATOR_ID, "7391".toCharArray()));
    assertThrows(AuthenticationException.class,
        () -> f.operators.authenticate(SampleData.OPERATOR_ID, "0000".toCharArray()));
    assertThrows(AuthenticationException.class,
        () -> f.operators.authenticate("NOBODY", "7391".toCharArray()));
  }

  @Test
  void replenishCollectAndDiagnose() throws Exception {
    f.operators.replenish(50, 10);
    assertEquals(Money.ofDollars(4_500), f.device.getDispenser().total());
    f.tx.deposit(f.login(SampleData.ALICE_CARD, "2468"), "1001-001", Money.ofDollars(75), false);
    assertEquals(Money.ofDollars(75), f.operators.collectDeposits());
    assertEquals(0, f.device.getDepositBin().getItems());
    String report = String.join("\n", f.operators.runDiagnostics());
    assertTrue(report.contains("$50 notes: 10") && report.contains("Cash status: OK"), report);
  }

  @Test
  void everyEnglishMessageHasAFrenchTranslation() throws Exception {
    Properties en = load("/i18n/messages.properties");
    Properties fr = load("/i18n/messages_fr.properties");
    assertEquals(en.keySet(), fr.keySet());
  }

  @Test
  void exceptionsBecomeHelpfulMessagesInBothLanguages() {
    InsufficientFundsException e = new InsufficientFundsException("$40.00");
    String english = new Messages(Language.ENGLISH).describe(e);
    String french = new Messages(Language.FRENCH).describe(e);
    assertTrue(english.contains("$40.00") && english.startsWith("Not enough"), english);
    assertTrue(french.startsWith("Fonds insuffisants") && french.contains("jusqu'à"), french);
    assertFalse(new Messages(Language.ENGLISH).get("no.such.key").isEmpty());
  }

  @Test
  void receiptMasksCardAndAccountNumbers() throws Exception {
    var tx = f.tx.withdraw(f.login(SampleData.ALICE_CARD, "2468"), "1001-001",
        Money.ofDollars(60));
    String receipt = ReceiptFormatter.format(tx, new Messages(Language.FRENCH));
    assertFalse(receipt.contains(SampleData.ALICE_CARD) || receipt.contains("1001-001"), receipt);
    assertTrue(receipt.contains("****1234") && receipt.contains("Retrait"), receipt);
  }

  private static Properties load(String path) throws Exception {
    Properties p = new Properties();
    try (InputStream in = OperatorAndMessagesTest.class.getResourceAsStream(path)) {
      p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
    }
    return p;
  }
}
