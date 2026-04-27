package com.cronwatch.history;

import java.time.Instant;
import java.time.Duration;

/**
 * Represents a single execution record for a cron job,
 * capturing start time, end time, and outcome.
 */
public class JobExecutionRecord {

    public enum Status { RUNNING, SUCCESS, FAILED, TIMEOUT }

    private final String jobId;
    private final Instant startTime;
    private Instant endTime;
    private Status status;
    private String message;

    public JobExecutionRecord(String jobId, Instant startTime) {
        this.jobId = jobId;
        this.startTime = startTime;
        this.status = Status.RUNNING;
    }

    public void complete(Status status, String message) {
        this.endTime = Instant.now();
        this.status = status;
        this.message = message;
    }

    public Duration getDuration() {
        Instant end = (endTime != null) ? endTime : Instant.now();
        return Duration.between(startTime, end);
    }

    public String getJobId() { return jobId; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("JobExecutionRecord{jobId='%s', status=%s, duration=%s}",
                jobId, status, getDuration());
    }
}
