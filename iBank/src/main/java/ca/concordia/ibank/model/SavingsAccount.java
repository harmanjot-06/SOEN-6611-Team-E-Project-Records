package ca.concordia.ibank.model;

/** Savings account; it can never be overdrawn. */
public class SavingsAccount extends Account {

  public SavingsAccount(String accountNumber, Money openingBalance) {
    super(accountNumber, AccountType.SAVINGS, openingBalance);
  }

  @Override
  protected Money overdraftLimit() {
    return Money.ZERO;
  }
}
