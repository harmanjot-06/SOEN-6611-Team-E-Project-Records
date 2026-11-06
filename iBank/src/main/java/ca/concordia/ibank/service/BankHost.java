package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Card;
import ca.concordia.ibank.model.Client;
import ca.concordia.ibank.model.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * Core banking system and payment network seen by the ABM (D1 actor Bank Host System).
 * The ABM depends only on this interface, so a real host can replace the simulated one.
 */
public interface BankHost {

  Optional<Card> findCard(String cardNumber);

  Optional<Client> findClient(String clientId);

  /** Stores a completed transaction. */
  void record(Transaction transaction);

  /** Returns all transactions made with a card, oldest first. */
  List<Transaction> transactionsFor(String cardNumber);

  /** Returns a new unique transaction reference. */
  String nextReference();
}
