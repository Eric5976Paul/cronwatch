package com.cronwatch.retry;

/**
 * Defines the retry policy for a cron job that has exceeded its expected duration
 * or failed to complete successfully.
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final long retryDelayMillis;
    private final boolean exponentialBackoff;

    public RetryPolicy(int maxAttempts, long retryDelayMillis, boolean exponentialBackoff) {
        if (maxAttempts < 0) throw new IllegalArgumentException("maxAttempts must be >= 0");
        if (retryDelayMillis < 0) throw new IllegalArgumentException("retryDelayMillis must be >= 0");
        this.maxAttempts = maxAttempts;
        this.retryDelayMillis = retryDelayMillis;
        this.exponentialBackoff = exponentialBackoff;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getRetryDelayMillis() {
        return retryDelayMillis;
    }

    public boolean isExponentialBackoff() {
        return exponentialBackoff;
    }

    /**
     * Calculates the delay before the given attempt number (1-based).
     */
    public long getDelayForAttempt(int attempt) {
        if (!exponentialBackoff || attempt <= 1) {
            return retryDelayMillis;
        }
        return retryDelayMillis * (1L << (attempt - 1));
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(0, 0, false);
    }

    @Override
    public String toString() {
        return String.format("RetryPolicy{maxAttempts=%d, retryDelayMillis=%d, exponentialBackoff=%b}",
                maxAttempts, retryDelayMillis, exponentialBackoff);
    }
}
