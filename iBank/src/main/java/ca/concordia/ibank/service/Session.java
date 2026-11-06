package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Card;
import ca.concordia.ibank.model.Client;
import ca.concordia.ibank.model.Language;
import ca.concordia.ibank.model.TransactionType;
import java.util.Objects;

/** An authenticated client session, from a successful PIN entry until the card is returned. */
public final class Session {

  private final Card card;
  private final Client client;
  private final Language language;
  private boolean open = true;

  Session(Card card, Client client, Language language) {
    this.card = Objects.requireNonNull(card);
    this.client = Objects.requireNonNull(client);
    this.language = Objects.requireNonNull(language);
  }

  public Card getCard() {
    return card;
  }

  public Client getClient() {
    return client;
  }

  public Language getLanguage() {
    return language;
  }

  public boolean isOpen() {
    return open;
  }

  /** Ends the session; later transactions with it are refused. */
  public void close() {
    open = false;
  }

  /** Returns true if this client may use the service at all. */
  public boolean allows(TransactionType service) {
    return card.getIssuer().allows(service);
  }
}
