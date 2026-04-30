package com.cronwatch.snapshot;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable snapshot of a cron job's status at a point in time.
 */
public class JobStatusSnapshot {

    public enum Status {
        RUNNING, COMPLETED, FAILED, OVERDUE, UNKNOWN
    }

    private final String jobId;
    private final String jobName;
    private final Status status;
    private final Instant capturedAt;
    private final long durationMillis;
    private final long expectedMaxMillis;
    private final String lastMessage;

    public JobStatusSnapshot(String jobId, String jobName, Status status,
                             Instant capturedAt, long durationMillis,
                             long expectedMaxMillis, String lastMessage) {
        this.jobId = Objects.requireNonNull(jobId, "jobId must not be null");
        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt must not be null");
        this.durationMillis = durationMillis;
        this.expectedMaxMillis = expectedMaxMillis;
        this.lastMessage = lastMessage;
    }

    public String getJobId() { return jobId; }
    public String getJobName() { return jobName; }
    public Status getStatus() { return status; }
    public Instant getCapturedAt() { return capturedAt; }
    public long getDurationMillis() { return durationMillis; }
    public long getExpectedMaxMillis() { return expectedMaxMillis; }
    public String getLastMessage() { return lastMessage; }

    public boolean isOverdue() {
        return expectedMaxMillis > 0 && durationMillis > expectedMaxMillis;
    }

    @Override
    public String toString() {
        return String.format("JobStatusSnapshot{jobId='%s', jobName='%s', status=%s, " +
                "durationMillis=%d, expectedMaxMillis=%d, capturedAt=%s}",
                jobId, jobName, status, durationMillis, expectedMaxMillis, capturedAt);
    }
}
