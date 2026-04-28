package com.cronwatch.schedule;

import java.util.Objects;

/**
 * Immutable value object representing a parsed cron job schedule definition.
 */
public final class ParsedSchedule {

    private final String jobName;
    private final String cronExpression;
    private final long expectedDurationSeconds;

    public ParsedSchedule(String jobName, String cronExpression, long expectedDurationSeconds) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName must not be blank");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("cronExpression must not be blank");
        }
        if (expectedDurationSeconds <= 0) {
            throw new IllegalArgumentException("expectedDurationSeconds must be positive");
        }
        this.jobName = jobName;
        this.cronExpression = cronExpression;
        this.expectedDurationSeconds = expectedDurationSeconds;
    }

    public String getJobName() {
        return jobName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public long getExpectedDurationSeconds() {
        return expectedDurationSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParsedSchedule)) return false;
        ParsedSchedule that = (ParsedSchedule) o;
        return expectedDurationSeconds == that.expectedDurationSeconds
                && Objects.equals(jobName, that.jobName)
                && Objects.equals(cronExpression, that.cronExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, cronExpression, expectedDurationSeconds);
    }

    @Override
    public String toString() {
        return String.format("ParsedSchedule{jobName='%s', expression='%s', expectedDuration=%ds}",
                jobName, cronExpression, expectedDurationSeconds);
    }
}
