package com.cronwatch.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Holds aggregated metrics for a single cron job across multiple executions.
 */
public class JobMetricsSummary {

    private final String jobName;
    private final AtomicLong totalRuns = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final AtomicLong totalDurationMillis = new AtomicLong(0);
    private volatile long minDurationMillis = Long.MAX_VALUE;
    private volatile long maxDurationMillis = Long.MIN_VALUE;

    public JobMetricsSummary(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("Job name must not be blank");
        }
        this.jobName = jobName;
    }

    public synchronized void addSample(long durationMillis, boolean succeeded) {
        totalRuns.incrementAndGet();
        totalDurationMillis.addAndGet(durationMillis);
        if (succeeded) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
        if (durationMillis < minDurationMillis) minDurationMillis = durationMillis;
        if (durationMillis > maxDurationMillis) maxDurationMillis = durationMillis;
    }

    public String getJobName() { return jobName; }
    public long getTotalRuns() { return totalRuns.get(); }
    public long getSuccessCount() { return successCount.get(); }
    public long getFailureCount() { return failureCount.get(); }
    public long getMinDurationMillis() { return totalRuns.get() == 0 ? 0 : minDurationMillis; }
    public long getMaxDurationMillis() { return totalRuns.get() == 0 ? 0 : maxDurationMillis; }

    public double getAverageDurationMillis() {
        long runs = totalRuns.get();
        return runs == 0 ? 0.0 : (double) totalDurationMillis.get() / runs;
    }

    public double getSuccessRate() {
        long runs = totalRuns.get();
        return runs == 0 ? 0.0 : (double) successCount.get() / runs;
    }

    @Override
    public String toString() {
        return String.format("JobMetricsSummary{job='%s', runs=%d, successRate=%.2f, avgMs=%.2f, minMs=%d, maxMs=%d}",
                jobName, getTotalRuns(), getSuccessRate(), getAverageDurationMillis(),
                getMinDurationMillis(), getMaxDurationMillis());
    }
}
