package com.cronwatch.throttle;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages alert throttling across all monitored cron jobs.
 * Thread-safe; intended to be used as a singleton within the daemon.
 */
public class AlertThrottleManager {

    private static final Logger logger = Logger.getLogger(AlertThrottleManager.class.getName());

    private final AlertThrottlePolicy policy;
    private final Map<String, AlertThrottleState> stateMap;

    public AlertThrottleManager(AlertThrottlePolicy policy) {
        this.policy = policy;
        this.stateMap = new ConcurrentHashMap<>();
    }

    /**
     * Determines whether an alert for the given job should be allowed through.
     * If allowed, the alert timestamp is recorded. Otherwise, the suppression counter is incremented.
     *
     * @param jobName   the name of the cron job triggering the alert
     * @param alertTime the time the alert was generated
     * @return true if the alert should be sent, false if it should be suppressed
     */
    public boolean shouldSendAlert(String jobName, Instant alertTime) {
        if (!policy.isEnabled()) {
            return true;
        }

        AlertThrottleState state = stateMap.computeIfAbsent(jobName, AlertThrottleState::new);

        synchronized (state) {
            Instant windowStart = alertTime.minus(policy.getSuppressionWindow());
            state.evictBefore(windowStart);

            if (state.alertCountInWindow() < policy.getMaxAlertsPerWindow()) {
                state.recordAlert(alertTime);
                logger.fine(() -> String.format("Alert allowed for job '%s' (%d/%d in window)",
                        jobName, state.alertCountInWindow(), policy.getMaxAlertsPerWindow()));
                return true;
            } else {
                state.incrementSuppressed();
                logger.warning(() -> String.format(
                        "Alert suppressed for job '%s' — %d alerts already sent in window. Total suppressed: %d",
                        jobName, state.alertCountInWindow(), state.getTotalSuppressed()));
                return false;
            }
        }
    }

    public AlertThrottleState getState(String jobName) {
        return stateMap.get(jobName);
    }

    public void reset(String jobName) {
        stateMap.remove(jobName);
    }

    public AlertThrottlePolicy getPolicy() {
        return policy;
    }
}
