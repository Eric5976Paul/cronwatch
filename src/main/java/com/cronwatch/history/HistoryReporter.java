package com.cronwatch.history;

import com.cronwatch.model.CronJob;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Generates summary reports from execution history for a given cron job.
 */
public class HistoryReporter {

    private final ExecutionHistoryStore historyStore;

    public HistoryReporter(ExecutionHistoryStore historyStore) {
        if (historyStore == null) {
            throw new IllegalArgumentException("historyStore must not be null");
        }
        this.historyStore = historyStore;
    }

    /**
     * Returns a summary report for the given job over the specified lookback window.
     *
     * @param job          the cron job to report on
     * @param lookbackDays number of days to look back in history
     * @return a {@link JobHistorySummary} containing aggregated metrics
     */
    public JobHistorySummary summarize(CronJob job, int lookbackDays) {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null");
        }
        if (lookbackDays <= 0) {
            throw new IllegalArgumentException("lookbackDays must be positive");
        }

        Instant cutoff = Instant.now().minus(Duration.ofDays(lookbackDays));
        List<JobExecutionRecord> records = historyStore.getRecords(job.getId()).stream()
                .filter(r -> r.getStartTime() != null && r.getStartTime().isAfter(cutoff))
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            return new JobHistorySummary(job.getId(), 0, 0, 0.0, 0.0, 0.0);
        }

        long totalRuns = records.size();
        long exceededCount = records.stream()
                .filter(JobExecutionRecord::isExceededThreshold)
                .count();

        OptionalDouble avgMs = records.stream()
                .filter(r -> r.getDurationMillis() >= 0)
                .mapToLong(JobExecutionRecord::getDurationMillis)
                .average();

        long maxMs = records.stream()
                .mapToLong(JobExecutionRecord::getDurationMillis)
                .max()
                .orElse(0L);

        long minMs = records.stream()
                .mapToLong(JobExecutionRecord::getDurationMillis)
                .min()
                .orElse(0L);

        return new JobHistorySummary(
                job.getId(),
                totalRuns,
                exceededCount,
                avgMs.orElse(0.0),
                (double) maxMs,
                (double) minMs
        );
    }
}
