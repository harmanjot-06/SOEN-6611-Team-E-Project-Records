package ca.concordia.ibank.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Audit trail of security and transaction events, built on {@link java.util.logging}. Entries
 * never contain PINs or full card numbers.
 */
public class AuditLog {

  private static final Logger LOGGER = Logger.getLogger("ca.concordia.ibank.audit");
  private final List<String> entries = new ArrayList<>();

  public synchronized void info(String message) {
    entries.add("INFO " + message);
    LOGGER.info(message);
  }

  public synchronized void security(String message) {
    entries.add("SECURITY " + message);
    LOGGER.warning(message);
  }

  /** Logs an unexpected error with its stack trace for developers. */
  public synchronized void error(String message, Throwable cause) {
    entries.add("ERROR " + message);
    LOGGER.log(Level.SEVERE, message, cause);
  }

  public synchronized List<String> entries() {
    return Collections.unmodifiableList(new ArrayList<>(entries));
  }
}
