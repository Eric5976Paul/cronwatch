package com.cronwatch.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for job execution history, keyed by job ID.
 * Retains up to a configurable maximum number of records per job.
 */
public class ExecutionHistoryStore {

    private static final int DEFAULT_MAX_RECORDS = 100;

    private final int maxRecordsPerJob;
    private final Map<String, List<JobExecutionRecord>> history = new ConcurrentHashMap<>();

    public ExecutionHistoryStore() {
        this(DEFAULT_MAX_RECORDS);
    }

    public ExecutionHistoryStore(int maxRecordsPerJob) {
        this.maxRecordsPerJob = maxRecordsPerJob;
    }

    public void record(JobExecutionRecord record) {
        history.compute(record.getJobId(), (id, records) -> {
            if (records == null) records = new ArrayList<>();
            records.add(record);
            if (records.size() > maxRecordsPerJob) {
                records.remove(0);
            }
            return records;
        });
    }

    public List<JobExecutionRecord> getHistory(String jobId) {
        return Collections.unmodifiableList(
                history.getOrDefault(jobId, Collections.emptyList()));
    }

    public List<JobExecutionRecord> getRecentFailures(String jobId, int limit) {
        return getHistory(jobId).stream()
                .filter(r -> r.getStatus() == JobExecutionRecord.Status.FAILED
                          || r.getStatus() == JobExecutionRecord.Status.TIMEOUT)
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void clear(String jobId) {
        history.remove(jobId);
    }

    public int size(String jobId) {
        return history.getOrDefault(jobId, Collections.emptyList()).size();
    }
}
