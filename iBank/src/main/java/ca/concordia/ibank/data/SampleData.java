package ca.concordia.ibank.data;

import ca.concordia.ibank.model.Card;
import ca.concordia.ibank.model.CardIssuer;
import ca.concordia.ibank.model.ChequingAccount;
import ca.concordia.ibank.model.Client;
import ca.concordia.ibank.model.Language;
import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.model.Payee;
import ca.concordia.ibank.model.SavingsAccount;
import ca.concordia.ibank.security.PinHasher;
import ca.concordia.ibank.service.InMemoryBankHost;
import ca.concordia.ibank.service.OperatorService;

/**
 * Representative, fictional data used to run and demonstrate iBank. No real person, card or bank
 * is represented.
 */
public final class SampleData {

  public static final String ALICE_CARD = "4520000000001234";
  public static final String BRUNO_CARD = "4520000000005678";
  public static final String VISITOR_CARD = "5100000000009012";
  public static final String LOCKED_CARD = "4520000000003456";
  public static final String OPERATOR_ID = "CUST01";

  private SampleData() {
  }

  /** Adds clients, accounts, payees and cards to the host. */
  public static void populate(InMemoryBankHost host) {
    Client alice = new Client("C001", "Alice Tremblay", Language.ENGLISH);
    alice.addAccount(new ChequingAccount("1001-001", Money.ofDollars(2_450), Money.ofDollars(500)));
    alice.addAccount(new SavingsAccount("1001-002", Money.ofDollars(8_000)));
    alice.addPayee(new Payee("HQ", "Hydro-Quebec", "301 555 019"));
    alice.addPayee(new Payee("BELL", "Bell Canada", "514 555 0101"));
    host.addClient(alice);
    host.addCard(new Card(ALICE_CARD, "C001", CardIssuer.IBANK, PinHasher.hash("2468")));

    Client bruno = new Client("C002", "Bruno Gagnon", Language.FRENCH);
    bruno.addAccount(new ChequingAccount("2002-001", Money.ofCents(15_075), Money.ZERO));
    bruno.addAccount(new SavingsAccount("2002-002", Money.ofDollars(300)));
    bruno.addPayee(new Payee("VIDEO", "Videotron", "88 777 666"));
    host.addClient(bruno);
    host.addCard(new Card(BRUNO_CARD, "C002", CardIssuer.IBANK, PinHasher.hash("1357")));

    Client visitor = new Client("X900", "Other-bank client", Language.ENGLISH);
    visitor.addAccount(new ChequingAccount("OB-77-001", Money.ofDollars(900), Money.ZERO));
    host.addClient(visitor);
    host.addCard(new Card(VISITOR_CARD, "X900", CardIssuer.OTHER_BANK, PinHasher.hash("8642")));

    Client dana = new Client("C003", "Dana Roy", Language.ENGLISH);
    dana.addAccount(new ChequingAccount("3003-001", Money.ofDollars(50), Money.ZERO));
    host.addClient(dana);
    Card locked = new Card(LOCKED_CARD, "C003", CardIssuer.IBANK, PinHasher.hash("9753"));
    locked.lock();
    host.addCard(locked);
  }

  /** Registers the demonstration operator. */
  public static void registerOperators(OperatorService operators) {
    operators.registerOperator(OPERATOR_ID, PinHasher.hash("7391"));
  }
}
