package ca.concordia.ibank.model;

/** Day-to-day account that may have an overdraft protection limit. */
public class ChequingAccount extends Account {

  private final Money overdraft;

  public ChequingAccount(String accountNumber, Money openingBalance, Money overdraft) {
    super(accountNumber, AccountType.CHEQUING, openingBalance);
    this.overdraft = overdraft;
  }

  @Override
  protected Money overdraftLimit() {
    return overdraft;
  }
}
