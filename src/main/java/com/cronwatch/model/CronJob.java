package com.cronwatch.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a monitored cron job with its expected and actual execution durations.
 */
public class CronJob {

    private final String name;
    private final String schedule;
    private final Duration expectedMaxDuration;
    private Instant lastStartTime;
    private Instant lastEndTime;
    private JobStatus status;

    public enum JobStatus {
        IDLE, RUNNING, COMPLETED, EXCEEDED, FAILED
    }

    public CronJob(String name, String schedule, Duration expectedMaxDuration) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Job name must not be null or blank");
        }
        if (expectedMaxDuration == null || expectedMaxDuration.isNegative() || expectedMaxDuration.isZero()) {
            throw new IllegalArgumentException("Expected max duration must be positive");
        }
        this.name = name;
        this.schedule = schedule;
        this.expectedMaxDuration = expectedMaxDuration;
        this.status = JobStatus.IDLE;
    }

    public void markStarted(Instant startTime) {
        this.lastStartTime = startTime;
        this.status = JobStatus.RUNNING;
    }

    public void markCompleted(Instant endTime) {
        this.lastEndTime = endTime;
        if (lastStartTime != null) {
            Duration actual = Duration.between(lastStartTime, endTime);
            this.status = actual.compareTo(expectedMaxDuration) > 0
                    ? JobStatus.EXCEEDED
                    : JobStatus.COMPLETED;
        } else {
            this.status = JobStatus.COMPLETED;
        }
    }

    /**
     * Marks the job as failed with the given end time.
     *
     * @param endTime the time at which the failure was detected
     */
    public void markFailed(Instant endTime) {
        this.lastEndTime = endTime;
        this.status = JobStatus.FAILED;
    }

    public Duration getActualDuration() {
        if (lastStartTime == null || lastEndTime == null) return null;
        return Duration.between(lastStartTime, lastEndTime);
    }

    public boolean isExceeded() {
        return status == JobStatus.EXCEEDED;
    }

    public String getName() { return name; }
    public String getSchedule() { return schedule; }
    public Duration getExpectedMaxDuration() { return expectedMaxDuration; }
    public Instant getLastStartTime() { return lastStartTime; }
    public Instant getLastEndTime() { return lastEndTime; }
    public JobStatus getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("CronJob{name='%s', schedule='%s', status=%s}", name, schedule, status);
    }
}
