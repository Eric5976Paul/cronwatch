package com.cronwatch.deadletter;

import com.cronwatch.alert.AlertRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Stores alert records that could not be delivered after all retry attempts.
 * Provides inspection and replay capabilities for undelivered alerts.
 */
public class DeadLetterQueue {

    private static final Logger LOGGER = Logger.getLogger(DeadLetterQueue.class.getName());

    private final int maxCapacity;
    private final Queue<DeadLetterEntry> entries;

    public DeadLetterQueue(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("maxCapacity must be positive");
        }
        this.maxCapacity = maxCapacity;
        this.entries = new LinkedList<>();
    }

    /**
     * Enqueues a failed alert record into the dead letter queue.
     * If the queue is at capacity, the oldest entry is evicted.
     */
    public synchronized void enqueue(AlertRecord alert, String failureReason) {
        if (alert == null) {
            throw new IllegalArgumentException("alert must not be null");
        }
        if (entries.size() >= maxCapacity) {
            DeadLetterEntry evicted = entries.poll();
            LOGGER.warning("Dead letter queue at capacity; evicting oldest entry for job: "
                    + (evicted != null ? evicted.getAlert().getJobName() : "unknown"));
        }
        entries.add(new DeadLetterEntry(alert, failureReason, Instant.now()));
        LOGGER.info("Alert for job '" + alert.getJobName() + "' added to dead letter queue. Reason: " + failureReason);
    }

    /**
     * Drains all current entries for replay or inspection, clearing the queue.
     */
    public synchronized List<DeadLetterEntry> drainAll() {
        List<DeadLetterEntry> drained = new ArrayList<>(entries);
        entries.clear();
        return Collections.unmodifiableList(drained);
    }

    /**
     * Returns a snapshot of current entries without removing them.
     */
    public synchronized List<DeadLetterEntry> peek() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized boolean isEmpty() {
        return entries.isEmpty();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}
