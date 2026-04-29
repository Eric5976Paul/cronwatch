package com.cronwatch.metrics;

import com.cronwatch.model.CronJob;
import com.cronwatch.history.JobExecutionRecord;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Collects and aggregates runtime metrics for monitored cron jobs.
 */
public class JobMetricsCollector {

    private final Map<String, JobMetricsSummary> summaryMap = new ConcurrentHashMap<>();

    public void record(CronJob job, JobExecutionRecord record) {
        if (job == null || record == null) {
            throw new IllegalArgumentException("Job and record must not be null");
        }
        summaryMap.compute(job.getName(), (name, existing) -> {
            JobMetricsSummary summary = existing != null ? existing : new JobMetricsSummary(name);
            summary.addSample(record.getDurationMillis(), record.isSucceeded());
            return summary;
        });
    }

    public JobMetricsSummary getSummary(String jobName) {
        return summaryMap.get(jobName);
    }

    public Map<String, JobMetricsSummary> getAllSummaries() {
        return Map.copyOf(summaryMap);
    }

    public List<String> getJobsExceedingThreshold(Duration threshold) {
        return summaryMap.values().stream()
                .filter(s -> s.getAverageDurationMillis() > threshold.toMillis())
                .map(JobMetricsSummary::getJobName)
                .collect(Collectors.toList());
    }

    public void reset(String jobName) {
        summaryMap.remove(jobName);
    }

    public void resetAll() {
        summaryMap.clear();
    }
}
