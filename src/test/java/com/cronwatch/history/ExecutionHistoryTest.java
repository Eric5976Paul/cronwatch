package com.cronwatch.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionHistoryTest {

    private ExecutionHistoryStore store;
    private HistoryReporter reporter;

    @BeforeEach
    void setUp() {
        store = new ExecutionHistoryStore(5);
        reporter = new HistoryReporter(store);
    }

    private JobExecutionRecord finishedRecord(String jobId, JobExecutionRecord.Status status) {
        JobExecutionRecord rec = new JobExecutionRecord(jobId, Instant.now().minusSeconds(10));
        rec.complete(status, "test");
        return rec;
    }

    @Test
    void testRecordAndRetrieve() {
        JobExecutionRecord rec = finishedRecord("job-1", JobExecutionRecord.Status.SUCCESS);
        store.record(rec);
        List<JobExecutionRecord> history = store.getHistory("job-1");
        assertEquals(1, history.size());
        assertEquals(JobExecutionRecord.Status.SUCCESS, history.get(0).getStatus());
    }

    @Test
    void testMaxRecordsEnforced() {
        for (int i = 0; i < 7; i++) {
            store.record(finishedRecord("job-2", JobExecutionRecord.Status.SUCCESS));
        }
        assertEquals(5, store.size("job-2"));
    }

    @Test
    void testFailureCount() {
        store.record(finishedRecord("job-3", JobExecutionRecord.Status.SUCCESS));
        store.record(finishedRecord("job-3", JobExecutionRecord.Status.FAILED));
        store.record(finishedRecord("job-3", JobExecutionRecord.Status.TIMEOUT));
        assertEquals(2, reporter.failureCount("job-3"));
    }

    @Test
    void testAverageDuration() {
        store.record(finishedRecord("job-4", JobExecutionRecord.Status.SUCCESS));
        store.record(finishedRecord("job-4", JobExecutionRecord.Status.SUCCESS));
        double avg = reporter.averageDurationSeconds("job-4");
        assertTrue(avg >= 9.0 && avg <= 11.0, "Expected ~10s average, got: " + avg);
    }

    @Test
    void testSummarizeEmpty() {
        String summary = reporter.summarize("unknown-job");
        assertTrue(summary.contains("No history"));
    }

    @Test
    void testClearHistory() {
        store.record(finishedRecord("job-5", JobExecutionRecord.Status.SUCCESS));
        store.clear("job-5");
        assertEquals(0, store.size("job-5"));
    }
}
