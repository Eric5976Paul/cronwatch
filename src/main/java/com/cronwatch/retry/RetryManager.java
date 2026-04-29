package com.cronwatch.retry;

import com.cronwatch.alert.AlertNotifier;
import com.cronwatch.model.CronJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages retry logic for cron jobs that have exceeded their expected duration
 * or encountered execution failures.
 */
public class RetryManager {

    private static final Logger logger = LoggerFactory.getLogger(RetryManager.class);

    private final AlertNotifier alertNotifier;
    private final Map<String, RetryState> retryStates = new ConcurrentHashMap<>();

    public RetryManager(AlertNotifier alertNotifier) {
        this.alertNotifier = alertNotifier;
    }

    /**
     * Determines whether a job should be retried based on its policy and current state.
     *
     * @return true if a retry should be scheduled, false if retries are exhausted or not configured
     */
    public boolean shouldRetry(CronJob job) {
        RetryPolicy policy = job.getRetryPolicy();
        if (policy == null || policy.getMaxAttempts() == 0) {
            return false;
        }
        RetryState state = retryStates.computeIfAbsent(job.getId(), RetryState::new);
        if (state.isExhausted()) {
            return false;
        }
        return state.getAttemptCount() < policy.getMaxAttempts();
    }

    /**
     * Records a retry attempt for the given job and returns the delay to wait before retrying.
     */
    public long recordRetryAttempt(CronJob job) {
        RetryState state = retryStates.computeIfAbsent(job.getId(), RetryState::new);
        state.recordAttempt();
        RetryPolicy policy = job.getRetryPolicy();
        long delay = policy.getDelayForAttempt(state.getAttemptCount());
        logger.info("Job '{}' retry attempt {}/{} scheduled in {}ms",
                job.getId(), state.getAttemptCount(), policy.getMaxAttempts(), delay);
        if (state.getAttemptCount() >= policy.getMaxAttempts()) {
            state.markExhausted();
            logger.warn("Job '{}' has exhausted all {} retry attempts", job.getId(), policy.getMaxAttempts());
            alertNotifier.sendAlert("RETRY_EXHAUSTED",
                    "Job '" + job.getId() + "' exhausted all " + policy.getMaxAttempts() + " retry attempts.");
        }
        return delay;
    }

    public void resetRetryState(String jobId) {
        RetryState state = retryStates.get(jobId);
        if (state != null) {
            state.reset();
            logger.debug("Retry state reset for job '{}'.", jobId);
        }
    }

    public RetryState getRetryState(String jobId) {
        return retryStates.get(jobId);
    }
}
