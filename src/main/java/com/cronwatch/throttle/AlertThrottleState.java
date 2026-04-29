package com.cronwatch.throttle;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks per-job alert emission timestamps to support throttle decisions.
 */
public class AlertThrottleState {

    private final String jobName;
    private final Deque<Instant> recentAlerts;
    private int totalSuppressed;

    public AlertThrottleState(String jobName) {
        this.jobName = jobName;
        this.recentAlerts = new ArrayDeque<>();
        this.totalSuppressed = 0;
    }

    public String getJobName() {
        return jobName;
    }

    public void recordAlert(Instant timestamp) {
        recentAlerts.addLast(timestamp);
    }

    public void evictBefore(Instant cutoff) {
        while (!recentAlerts.isEmpty() && recentAlerts.peekFirst().isBefore(cutoff)) {
            recentAlerts.pollFirst();
        }
    }

    public int alertCountInWindow() {
        return recentAlerts.size();
    }

    public void incrementSuppressed() {
        totalSuppressed++;
    }

    public int getTotalSuppressed() {
        return totalSuppressed;
    }

    public Instant oldestAlertInWindow() {
        return recentAlerts.peekFirst();
    }

    @Override
    public String toString() {
        return String.format("AlertThrottleState{job='%s', windowAlerts=%d, suppressed=%d}",
                jobName, recentAlerts.size(), totalSuppressed);
    }
}
