package ca.concordia.ibank.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A bank client with accounts and registered bill payees. */
public class Client {

  private final String clientId;
  private final String name;
  private final Language preferredLanguage;
  private final List<Account> accounts = new ArrayList<>();
  private final List<Payee> payees = new ArrayList<>();

  /** Creates a client with no accounts or payees. */
  public Client(String clientId, String name, Language preferredLanguage) {
    this.clientId = Objects.requireNonNull(clientId);
    this.name = Objects.requireNonNull(name);
    this.preferredLanguage = Objects.requireNonNull(preferredLanguage);
  }

  public String getClientId() {
    return clientId;
  }

  public String getName() {
    return name;
  }

  public Language getPreferredLanguage() {
    return preferredLanguage;
  }

  public void addAccount(Account account) {
    accounts.add(Objects.requireNonNull(account));
  }

  public void addPayee(Payee payee) {
    payees.add(Objects.requireNonNull(payee));
  }

  public List<Account> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  public List<Payee> getPayees() {
    return Collections.unmodifiableList(payees);
  }

  /** Finds one of this client's accounts by number. */
  public Optional<Account> findAccount(String accountNumber) {
    return accounts.stream().filter(a -> a.getAccountNumber().equals(accountNumber)).findFirst();
  }
}
