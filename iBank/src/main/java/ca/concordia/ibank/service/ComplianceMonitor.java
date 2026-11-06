package ca.concordia.ibank.service;

import ca.concordia.ibank.model.Money;
import ca.concordia.ibank.model.Transaction;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Flags cash activity that reaches the reporting threshold within 24 hours for one client
 * (D1 actor Compliance and Fraud Monitor). Each client is reported at most once per window.
 */
public class ComplianceMonitor {

  private final Money threshold;
  private final Map<String, Deque<Transaction>> cashByClient = new HashMap<>();
  private final Set<String> reportedClients = new HashSet<>();
  private final List<ComplianceReport> reports = new ArrayList<>();

  public ComplianceMonitor(Money threshold) {
    this.threshold = threshold;
  }

  /** Observes a completed transaction; returns true if it caused a new report. */
  public synchronized boolean observe(String clientId, Transaction tx) {
    if (!tx.getType().isCash()) {
      return false;
    }
    Deque<Transaction> window = cashByClient.computeIfAbsent(clientId, k -> new ArrayDeque<>());
    window.addLast(tx);
    LocalDateTime cutoff = tx.getTimestamp().minusHours(24);
    while (!window.isEmpty() && window.peekFirst().getTimestamp().isBefore(cutoff)) {
      window.removeFirst();
      reportedClients.remove(clientId);
    }
    Money sum = Money.ZERO;
    for (Transaction t : window) {
      sum = sum.plus(t.getAmount());
    }
    if (sum.compareTo(threshold) >= 0 && reportedClients.add(clientId)) {
      reports.add(new ComplianceReport(clientId, sum, tx.getReference(), tx.getTimestamp()));
      return true;
    }
    return false;
  }

  public synchronized List<ComplianceReport> getReports() {
    return Collections.unmodifiableList(new ArrayList<>(reports));
  }
}
