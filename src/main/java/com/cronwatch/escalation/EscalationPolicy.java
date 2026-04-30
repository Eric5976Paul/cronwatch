package com.cronwatch.escalation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines escalation tiers for repeated or prolonged alert conditions.
 * Each tier specifies a threshold (number of occurrences or elapsed time)
 * and the notification level that should be triggered.
 */
public class EscalationPolicy {

    public enum Level {
        INFO, WARNING, CRITICAL, EMERGENCY
    }

    public static class Tier {
        private final int occurrenceThreshold;
        private final Duration durationThreshold;
        private final Level level;

        public Tier(int occurrenceThreshold, Duration durationThreshold, Level level) {
            if (occurrenceThreshold < 1) throw new IllegalArgumentException("occurrenceThreshold must be >= 1");
            if (durationThreshold == null || durationThreshold.isNegative()) {
                throw new IllegalArgumentException("durationThreshold must be non-negative");
            }
            this.occurrenceThreshold = occurrenceThreshold;
            this.durationThreshold = durationThreshold;
            this.level = level;
        }

        public int getOccurrenceThreshold() { return occurrenceThreshold; }
        public Duration getDurationThreshold() { return durationThreshold; }
        public Level getLevel() { return level; }
    }

    private final String jobName;
    private final List<Tier> tiers;

    public EscalationPolicy(String jobName, List<Tier> tiers) {
        if (jobName == null || jobName.isBlank()) throw new IllegalArgumentException("jobName must not be blank");
        if (tiers == null || tiers.isEmpty()) throw new IllegalArgumentException("tiers must not be empty");
        this.jobName = jobName;
        this.tiers = Collections.unmodifiableList(new ArrayList<>(tiers));
    }

    public String getJobName() { return jobName; }
    public List<Tier> getTiers() { return tiers; }

    /**
     * Resolves the highest applicable escalation level given current occurrence count
     * and how long the alert condition has been active.
     */
    public Level resolve(int occurrences, Duration activeFor) {
        Level resolved = Level.INFO;
        for (Tier tier : tiers) {
            if (occurrences >= tier.getOccurrenceThreshold() &&
                    !activeFor.minus(tier.getDurationThreshold()).isNegative()) {
                resolved = tier.getLevel();
            }
        }
        return resolved;
    }
}
