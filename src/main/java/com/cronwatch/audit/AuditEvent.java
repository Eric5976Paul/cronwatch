package com.cronwatch.audit;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a single audit event recorded during cron job lifecycle.
 */
public class AuditEvent {

    public enum EventType {
        JOB_STARTED,
        JOB_COMPLETED,
        JOB_FAILED,
        ALERT_SENT,
        THRESHOLD_EXCEEDED,
        RETRY_TRIGGERED
    }

    private final String jobName;
    private final EventType eventType;
    private final Instant timestamp;
    private final String details;

    public AuditEvent(String jobName, EventType eventType, String details) {
        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.timestamp = Instant.now();
        this.details = details;
    }

    public AuditEvent(String jobName, EventType eventType, Instant timestamp, String details) {
        this.jobName = Objects.requireNonNull(jobName);
        this.eventType = Objects.requireNonNull(eventType);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.details = details;
    }

    public String getJobName() { return jobName; }
    public EventType getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return String.format("AuditEvent{job='%s', type=%s, time=%s, details='%s'}",
                jobName, eventType, timestamp, details);
    }
}
