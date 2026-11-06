package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Card;
import ca.concordia.ibank.model.Client;
import ca.concordia.ibank.model.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Simulated bank host that keeps all data in memory (D1 assumption A6). */
public class InMemoryBankHost implements BankHost {

  private final Map<String, Card> cards = new HashMap<>();
  private final Map<String, Client> clients = new HashMap<>();
  private final List<Transaction> journal = new ArrayList<>();
  private final AtomicLong sequence = new AtomicLong(100_000);

  public void addClient(Client client) {
    clients.put(client.getClientId(), client);
  }

  public void addCard(Card card) {
    cards.put(card.getCardNumber(), card);
  }

  @Override
  public Optional<Card> findCard(String cardNumber) {
    return Optional.ofNullable(cards.get(cardNumber));
  }

  @Override
  public Optional<Client> findClient(String clientId) {
    return Optional.ofNullable(clients.get(clientId));
  }

  @Override
  public synchronized void record(Transaction transaction) {
    journal.add(transaction);
  }

  @Override
  public synchronized List<Transaction> transactionsFor(String cardNumber) {
    List<Transaction> result = new ArrayList<>();
    for (Transaction t : journal) {
      if (t.getCardNumber().equals(cardNumber)) {
        result.add(t);
      }
    }
    return Collections.unmodifiableList(result);
  }

  @Override
  public String nextReference() {
    return "TX" + sequence.incrementAndGet();
  }
}
