package ca.concordia.ibank.model;

import ca.concordia.ibank.exception.InsufficientFundsException;
import java.util.Locale;
import java.util.Objects;

/**
 * Bank account with a ledger balance and an amount on hold (for example, a recently deposited
 * cheque). Subclasses decide how far below zero the available balance may go.
 */
public abstract class Account {

  private final String accountNumber;
  private final AccountType type;
  private Money balance;
  private Money onHold = Money.ZERO;

  protected Account(String accountNumber, AccountType type, Money openingBalance) {
    this.accountNumber = Objects.requireNonNull(accountNumber);
    this.type = Objects.requireNonNull(type);
    this.balance = Objects.requireNonNull(openingBalance);
  }

  /** Returns the most this account may be overdrawn; zero for no overdraft. */
  protected abstract Money overdraftLimit();

  public String getAccountNumber() {
    return accountNumber;
  }

  public AccountType getType() {
    return type;
  }

  public Money getBalance() {
    return balance;
  }

  public Money getOnHold() {
    return onHold;
  }

  /** Returns the amount the client can use now: balance minus holds plus any overdraft. */
  public Money getAvailableBalance() {
    return balance.minus(onHold).plus(overdraftLimit());
  }

  /** Adds immediately available funds. */
  public void credit(Money amount) {
    balance = balance.plus(amount);
  }

  /** Adds funds of which {@code held} is not available until the hold is released. */
  public void creditWithHold(Money amount, Money held) {
    balance = balance.plus(amount);
    onHold = onHold.plus(held);
  }

  /**
   * Removes funds.
   *
   * @throws InsufficientFundsException if the amount exceeds the available balance
   */
  public void debit(Money amount) throws InsufficientFundsException {
    if (amount.isGreaterThan(getAvailableBalance())) {
      throw new InsufficientFundsException(getAvailableBalance().format(Locale.CANADA));
    }
    balance = balance.minus(amount);
  }

  /** Returns the account number with all but the last four digits hidden. */
  public String maskedNumber() {
    int visible = Math.min(4, accountNumber.length());
    return "****" + accountNumber.substring(accountNumber.length() - visible);
  }
}
