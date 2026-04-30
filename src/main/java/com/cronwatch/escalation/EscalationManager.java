package com.cronwatch.escalation;

import com.cronwatch.alert.AlertNotifier;
import com.cronwatch.alert.AlertRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Tracks alert escalation state per job and triggers the appropriate
 * notification level when thresholds defined in an {@link EscalationPolicy} are met.
 */
public class EscalationManager {

    private static final Logger LOG = Logger.getLogger(EscalationManager.class.getName());

    private static class State {
        int occurrences = 0;
        Instant firstSeen = Instant.now();
        EscalationPolicy.Level lastLevel = null;
    }

    private final Map<String, EscalationPolicy> policies = new ConcurrentHashMap<>();
    private final Map<String, State> states = new ConcurrentHashMap<>();
    private final AlertNotifier alertNotifier;

    public EscalationManager(AlertNotifier alertNotifier) {
        if (alertNotifier == null) throw new IllegalArgumentException("alertNotifier must not be null");
        this.alertNotifier = alertNotifier;
    }

    public void registerPolicy(EscalationPolicy policy) {
        policies.put(policy.getJobName(), policy);
        LOG.info("Registered escalation policy for job: " + policy.getJobName());
    }

    /**
     * Called each time an alert condition is detected for the given job.
     * Increments occurrence count and evaluates whether the escalation level has changed.
     */
    public void recordAlert(String jobName, String message) {
        EscalationPolicy policy = policies.get(jobName);
        if (policy == null) {
            LOG.fine("No escalation policy for job: " + jobName + "; skipping escalation.");
            return;
        }

        State state = states.computeIfAbsent(jobName, k -> new State());
        state.occurrences++;
        Duration activeFor = Duration.between(state.firstSeen, Instant.now());
        EscalationPolicy.Level level = policy.resolve(state.occurrences, activeFor);

        if (level != state.lastLevel) {
            state.lastLevel = level;
            AlertRecord record = new AlertRecord(jobName, level.name() + ": " + message, Instant.now());
            alertNotifier.send(record);
            LOG.warning("Escalation level changed to " + level + " for job: " + jobName);
        }
    }

    /** Clears escalation state when a job completes successfully. */
    public void clearState(String jobName) {
        if (states.remove(jobName) != null) {
            LOG.info("Cleared escalation state for job: " + jobName);
        }
    }

    public int getOccurrenceCount(String jobName) {
        State s = states.get(jobName);
        return s == null ? 0 : s.occurrences;
    }
}
