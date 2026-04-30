package com.cronwatch.suppression;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

/**
 * Defines a rule for suppressing alerts during specific time windows or conditions.
 */
public class AlertSuppressionRule {

    private final String ruleId;
    private final String jobPattern;
    private final LocalTime windowStart;
    private final LocalTime windowEnd;
    private final Set<DayOfWeek> activeDays;
    private final String reason;

    public AlertSuppressionRule(String ruleId, String jobPattern,
                                LocalTime windowStart, LocalTime windowEnd,
                                Set<DayOfWeek> activeDays, String reason) {
        if (ruleId == null || ruleId.isBlank()) throw new IllegalArgumentException("ruleId must not be blank");
        if (jobPattern == null || jobPattern.isBlank()) throw new IllegalArgumentException("jobPattern must not be blank");
        if (windowStart == null || windowEnd == null) throw new IllegalArgumentException("Window times must not be null");
        if (activeDays == null || activeDays.isEmpty()) throw new IllegalArgumentException("activeDays must not be empty");
        this.ruleId = ruleId;
        this.jobPattern = jobPattern;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.activeDays = Set.copyOf(activeDays);
        this.reason = reason != null ? reason : "";
    }

    public boolean matches(String jobName, DayOfWeek day, LocalTime time) {
        if (!jobName.matches(jobPattern.replace("*", ".*"))) return false;
        if (!activeDays.contains(day)) return false;
        if (windowStart.isBefore(windowEnd)) {
            return !time.isBefore(windowStart) && !time.isAfter(windowEnd);
        } else {
            // overnight window e.g. 22:00 - 06:00
            return !time.isBefore(windowStart) || !time.isAfter(windowEnd);
        }
    }

    public String getRuleId() { return ruleId; }
    public String getJobPattern() { return jobPattern; }
    public LocalTime getWindowStart() { return windowStart; }
    public LocalTime getWindowEnd() { return windowEnd; }
    public Set<DayOfWeek> getActiveDays() { return activeDays; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return String.format("AlertSuppressionRule{id='%s', pattern='%s', window=%s-%s, days=%s}",
                ruleId, jobPattern, windowStart, windowEnd, activeDays);
    }
}
