package com.cronwatch.alert;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record representing a single alert event.
 */
public class AlertRecord {

    private final String jobName;
    private final String message;
    private final Instant timestamp;

    public AlertRecord(String jobName, String message, Instant timestamp) {
        this.jobName = Objects.requireNonNull(jobName, "jobName must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public String getJobName() {
        return jobName;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AlertRecord{jobName='" + jobName + "', timestamp=" + timestamp + ", message='" + message + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertRecord)) return false;
        AlertRecord that = (AlertRecord) o;
        return Objects.equals(jobName, that.jobName)
            && Objects.equals(message, that.message)
            && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, message, timestamp);
    }
}
