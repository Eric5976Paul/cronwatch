package com.cronwatch.retry;

import java.time.Instant;

/**
 * Tracks the current retry state for a specific cron job.
 */
public class RetryState {

    private final String jobId;
    private int attemptCount;
    private Instant lastAttemptTime;
    private boolean exhausted;

    public RetryState(String jobId) {
        this.jobId = jobId;
        this.attemptCount = 0;
        this.exhausted = false;
    }

    public String getJobId() {
        return jobId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getLastAttemptTime() {
        return lastAttemptTime;
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public void recordAttempt() {
        this.attemptCount++;
        this.lastAttemptTime = Instant.now();
    }

    public void markExhausted() {
        this.exhausted = true;
    }

    public void reset() {
        this.attemptCount = 0;
        this.lastAttemptTime = null;
        this.exhausted = false;
    }

    @Override
    public String toString() {
        return String.format("RetryState{jobId='%s', attemptCount=%d, exhausted=%b}",
                jobId, attemptCount, exhausted);
    }
}
