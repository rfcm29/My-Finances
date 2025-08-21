package com.example.myfinances.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for collecting application metrics
 */
@Service
@Slf4j
public class MetricsService {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> timers = new ConcurrentHashMap<>();

    public void incrementCounter(String name) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void recordTime(String name, long timeInMs) {
        timers.computeIfAbsent(name, k -> new AtomicLong(0)).addAndGet(timeInMs);
    }

    public long getCounter(String name) {
        return counters.getOrDefault(name, new AtomicLong(0)).get();
    }

    public long getTotalTime(String name) {
        return timers.getOrDefault(name, new AtomicLong(0)).get();
    }

    public void logMetrics() {
        log.info("=== Application Metrics ===");
        
        counters.forEach((name, value) -> 
            log.info("Counter {}: {}", name, value.get()));
        
        timers.forEach((name, value) -> 
            log.info("Timer {}: {}ms total", name, value.get()));
        
        log.info("=== End Metrics ===");
    }

    // Business-specific metrics
    public void recordUserLogin(String email) {
        incrementCounter("user.login.count");
        log.info("User login recorded: {}", email);
    }

    public void recordInvestmentCreated() {
        incrementCounter("investment.created.count");
        log.info("Investment creation recorded");
    }

    public void recordTransactionCreated() {
        incrementCounter("transaction.created.count");
        log.info("Transaction creation recorded");
    }

    public void recordAccountCreated() {
        incrementCounter("account.created.count");
        log.info("Account creation recorded");
    }

    public void recordDatabaseQuery(String queryType, long timeMs) {
        recordTime("database.query." + queryType, timeMs);
        if (timeMs > 1000) {
            log.warn("Slow database query detected: {} took {}ms", queryType, timeMs);
        }
    }

    public void recordApiCall(String apiName, long timeMs) {
        recordTime("api.call." + apiName, timeMs);
        if (timeMs > 5000) {
            log.warn("Slow API call detected: {} took {}ms", apiName, timeMs);
        }
    }
}