package com.cronwatch.history;

import java.time.Duration;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Generates summary statistics and reports from execution history
 * for a given job, used by the monitor and alert subsystems.
 */
public class HistoryReporter {

    private final ExecutionHistoryStore store;

    public HistoryReporter(ExecutionHistoryStore store) {
        this.store = store;
    }

    public double averageDurationSeconds(String jobId) {
        List<JobExecutionRecord> records = store.getHistory(jobId);
        OptionalDouble avg = records.stream()
                .filter(r -> r.getStatus() == JobExecutionRecord.Status.SUCCESS
                          || r.getStatus() == JobExecutionRecord.Status.FAILED)
                .mapToLong(r -> r.getDuration().getSeconds())
                .average();
        return avg.orElse(0.0);
    }

    public long failureCount(String jobId) {
        return store.getHistory(jobId).stream()
                .filter(r -> r.getStatus() == JobExecutionRecord.Status.FAILED
                          || r.getStatus() == JobExecutionRecord.Status.TIMEOUT)
                .count();
    }

    public Duration maxDuration(String jobId) {
        return store.getHistory(jobId).stream()
                .map(JobExecutionRecord::getDuration)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    public String summarize(String jobId) {
        List<JobExecutionRecord> records = store.getHistory(jobId);
        if (records.isEmpty()) return "No history for job: " + jobId;
        return String.format(
                "Job '%s': runs=%d, failures=%d, avgDuration=%.1fs, maxDuration=%s",
                jobId, records.size(), failureCount(jobId),
                averageDurationSeconds(jobId), maxDuration(jobId));
    }
}
