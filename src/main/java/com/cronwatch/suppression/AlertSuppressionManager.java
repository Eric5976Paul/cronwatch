package com.cronwatch.suppression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages a collection of {@link AlertSuppressionRule}s and evaluates whether
 * an alert for a given job should be suppressed at the current moment.
 */
public class AlertSuppressionManager {

    private static final Logger log = LoggerFactory.getLogger(AlertSuppressionManager.class);

    private final List<AlertSuppressionRule> rules = new ArrayList<>();

    public void addRule(AlertSuppressionRule rule) {
        if (rule == null) throw new IllegalArgumentException("rule must not be null");
        rules.add(rule);
        log.info("Registered suppression rule: {}", rule);
    }

    public boolean removeRule(String ruleId) {
        boolean removed = rules.removeIf(r -> r.getRuleId().equals(ruleId));
        if (removed) log.info("Removed suppression rule with id='{}'", ruleId);
        return removed;
    }

    /**
     * Returns true if any active rule suppresses alerts for the given job at the given time.
     */
    public boolean isSuppressed(String jobName, LocalDateTime at) {
        DayOfWeek day = at.getDayOfWeek();
        LocalTime time = at.toLocalTime();
        Optional<AlertSuppressionRule> matched = rules.stream()
                .filter(r -> r.matches(jobName, day, time))
                .findFirst();
        matched.ifPresent(r -> log.debug("Alert for job '{}' suppressed by rule '{}': {}",
                jobName, r.getRuleId(), r.getReason()));
        return matched.isPresent();
    }

    public List<AlertSuppressionRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public void clearRules() {
        rules.clear();
        log.info("All suppression rules cleared");
    }
}
