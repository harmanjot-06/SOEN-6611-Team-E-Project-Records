package ca.concordia.ibank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.concordia.ibank.data.SampleData;
import ca.concordia.ibank.exception.AccountNotFoundException;
import ca.concordia.ibank.exception.AuthenticationException;
import ca.concordia.ibank.exception.CashUnavailableException;
import ca.concordia.ibank.exception.IbankException;
import ca.concordia.ibank.exception.InsufficientFundsException;
import ca.concordia.ibank.exception.InvalidAmountException;
import ca.concordia.ibank.exception.InvalidPinFormatException;
import ca.concordia.ibank.exception.LimitExceededException;
import ca.concordia.ibank.exception.OutOfServiceException;
import ca.concordia.ibank.exception.ServiceNotAllowedException;
import ca.concordia.ibank.model.Account;
import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.model.Transaction;
import ca.concordia.ibank.model.TransactionType;
import ca.concordia.ibank.service.Session;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Representative scenarios for every client use case, main flows and extensions. */
class TransactionServiceTest {

  private static final String ALICE_CHQ = "1001-001";
  private static final String ALICE_SAV = "1001-002";

  private TestFixture f;
  private Session alice;

  @BeforeEach
  void setUp() throws Exception {
    f = new TestFixture();
    alice = f.login(SampleData.ALICE_CARD, "2468");
  }

  private Account account(Session s, String number) {
    return s.getClient().findAccount(number).orElseThrow();
  }

  @Test
  void balanceInquiryIncludesOverdraft() throws Exception {
    Transaction t = f.tx.checkBalance(alice, ALICE_CHQ);
    assertEquals(Money.ofDollars(2_950), t.getBalanceAfter());
    assertEquals(TransactionType.BALANCE_INQUIRY, t.getType());
  }

  @Test
  void withdrawalDebitsAccountAndDispensesNotes() throws Exception {
    Transaction t = f.tx.withdraw(alice, ALICE_CHQ, Money.ofDollars(120));
    assertEquals(Money.ofDollars(2_330), account(alice, ALICE_CHQ).getBalance());
    assertEquals(Money.ofDollars(3_880), f.device.getDispenser().total());
    assertEquals(1, f.host.transactionsFor(SampleData.ALICE_CARD).size());
    assertTrue(t.getReference().startsWith("TX"));
  }

  @Test
  void withdrawalRulesProduceSpecificErrors() {
    assertKey("error.amount.notMultiple", InvalidAmountException.class,
        () -> f.tx.withdraw(alice, ALICE_CHQ, Money.ofDollars(30)));
    assertThrows(LimitExceededException.class,
        () -> f.tx.withdraw(alice, ALICE_CHQ, Money.ofDollars(520)));
    assertThrows(AccountNotFoundException.class,
        () -> f.tx.withdraw(alice, "9999-999", Money.ofDollars(20)));
  }

  @Test
  void dailyLimitIsEnforcedAndResetsNextDay() throws Exception {
    f.tx.withdraw(alice, ALICE_CHQ, Money.ofDollars(500));
    f.tx.withdraw(alice, ALICE_SAV, Money.ofDollars(400));
    assertEquals(Money.ofDollars(100), f.tx.remainingDailyLimit(alice));
    assertThrows(LimitExceededException.class,
        () -> f.tx.withdraw(alice, ALICE_CHQ, Money.ofDollars(120)));
    f.clock.advance(Duration.ofDays(1));
    assertEquals(Money.ofDollars(1_000), f.tx.remainingDailyLimit(alice));
  }

  @Test
  void insufficientFundsLeavesBalanceUnchanged() throws Exception {
    Session bruno = f.login(SampleData.BRUNO_CARD, "1357");
    assertThrows(InsufficientFundsException.class,
        () -> f.tx.withdraw(bruno, "2002-001", Money.ofDollars(160)));
    assertEquals(Money.ofCents(15_075), account(bruno, "2002-001").getBalance());
    assertEquals(Money.ofDollars(4_000), f.device.getDispenser().total());
  }

  @Test
  void emptyDispenserRefusesWithoutDebiting() throws Exception {
    f.device.getDispenser().dispense(Money.ofDollars(3_960));
    assertThrows(CashUnavailableException.class,
        () -> f.tx.withdraw(alice, ALICE_CHQ, Money.ofDollars(60)));
    assertEquals(Money.ofDollars(2_450), account(alice, ALICE_CHQ).getBalance());
  }

  @Test
  void otherBankCardIsLimitedToWithdrawAndBalance() throws Exception {
    Session visitor = f.login(SampleData.VISITOR_CARD, "8642");
    f.tx.withdraw(visitor, "OB-77-001", Money.ofDollars(40));
    f.tx.checkBalance(visitor, "OB-77-001");
    assertThrows(ServiceNotAllowedException.class,
        () -> f.tx.deposit(visitor, "OB-77-001", Money.ofDollars(10), false));
    assertThrows(ServiceNotAllowedException.class,
        () -> f.tx.changePin(visitor, "8642".toCharArray(), "2580".toCharArray(),
            "2580".toCharArray()));
  }

  @Test
  void cashDepositIsAvailableAtOnce() throws Exception {
    f.tx.deposit(alice, ALICE_SAV, Money.parse("250.25"), false);
    assertEquals(Money.ofCents(825_025), account(alice, ALICE_SAV).getAvailableBalance());
    assertEquals(1, f.device.getDepositBin().getItems());
  }

  @Test
  void chequeDepositHoldsAmountAboveImmediateAvailability() throws Exception {
    f.tx.deposit(alice, ALICE_SAV, Money.ofDollars(1_000), true);
    Account sav = account(alice, ALICE_SAV);
    assertEquals(Money.ofDollars(9_000), sav.getBalance());
    assertEquals(Money.ofDollars(900), sav.getOnHold());
    assertEquals(Money.ofDollars(8_100), sav.getAvailableBalance());
  }

  @Test
  void transferMovesMoneyBetweenOwnAccounts() throws Exception {
    f.tx.transfer(alice, ALICE_SAV, ALICE_CHQ, Money.ofDollars(1_000));
    assertEquals(Money.ofDollars(7_000), account(alice, ALICE_SAV).getBalance());
    assertEquals(Money.ofDollars(3_450), account(alice, ALICE_CHQ).getBalance());
    assertKey("error.transfer.sameAccount", InvalidAmountException.class,
        () -> f.tx.transfer(alice, ALICE_CHQ, ALICE_CHQ, Money.ofDollars(1)));
    assertThrows(InsufficientFundsException.class,
        () -> f.tx.transfer(alice, ALICE_SAV, ALICE_CHQ, Money.ofDollars(7_001)));
  }

  @Test
  void billPaymentToRegisteredPayeeOnly() throws Exception {
    Transaction t = f.tx.payBill(alice, "HQ", ALICE_CHQ, Money.parse("87.40"));
    assertEquals("Hydro-Quebec", t.getToAccount());
    assertEquals(Money.ofCents(236_260), account(alice, ALICE_CHQ).getBalance());
    assertThrows(AccountNotFoundException.class,
        () -> f.tx.payBill(alice, "VIDEO", ALICE_CHQ, Money.ofDollars(10)));
  }

  @Test
  void changePinVerifiesOldPinAndAppliesPolicy() throws Exception {
    assertThrows(AuthenticationException.class, () -> f.tx.changePin(alice,
        "1111".toCharArray(), "2580".toCharArray(), "2580".toCharArray()));
    assertKey("error.pin.trivial", InvalidPinFormatException.class, () -> f.tx.changePin(alice,
        "2468".toCharArray(), "1234".toCharArray(), "1234".toCharArray()));
    assertKey("error.pin.same", InvalidPinFormatException.class, () -> f.tx.changePin(alice,
        "2468".toCharArray(), "2468".toCharArray(), "2468".toCharArray()));
    f.tx.changePin(alice, "2468".toCharArray(), "2580".toCharArray(), "2580".toCharArray());
    assertTrue(f.login(SampleData.ALICE_CARD, "2580").isOpen());
  }

  @Test
  void closedSessionAndOutOfServiceAbmAreRefused() throws Exception {
    f.operators.setInService(false);
    assertThrows(OutOfServiceException.class, () -> f.tx.checkBalance(alice, ALICE_CHQ));
    f.operators.setInService(true);
    alice.close();
    assertKey("error.session.closed", IbankException.class,
        () -> f.tx.checkBalance(alice, ALICE_CHQ));
  }

  @Test
  void largeCashDepositIsFlaggedForCompliance() throws Exception {
    f.tx.deposit(alice, ALICE_SAV, Money.ofDollars(6_000), false);
    assertEquals(0, f.compliance.getReports().size());
    f.tx.deposit(alice, ALICE_SAV, Money.ofDollars(4_000), false);
    assertEquals(1, f.compliance.getReports().size(), "24-hour aggregate reaches $10,000");
    f.tx.deposit(alice, ALICE_SAV, Money.ofDollars(100), false);
    assertEquals(1, f.compliance.getReports().size(), "reported once per window");
    f.tx.deposit(alice, ALICE_SAV, Money.ofDollars(5_000), true);
    assertEquals(1, f.compliance.getReports().size(), "cheques are not cash");
  }

  private static void assertKey(String key, Class<? extends IbankException> type,
      org.junit.jupiter.api.function.Executable action) {
    IbankException e = assertThrows(type, action);
    assertEquals(key, e.getMessageKey());
  }
}
