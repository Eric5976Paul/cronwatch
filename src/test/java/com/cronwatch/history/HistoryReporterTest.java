package com.cronwatch.history;

import com.cronwatch.model.CronJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoryReporterTest {

    private ExecutionHistoryStore historyStore;
    private HistoryReporter reporter;
    private CronJob job;

    @BeforeEach
    void setUp() {
        historyStore = mock(ExecutionHistoryStore.class);
        reporter = new HistoryReporter(historyStore);
        job = new CronJob("backup-job", "0 2 * * *", 300_000L);
    }

    @Test
    void summarize_noRecords_returnsZeroSummary() {
        when(historyStore.getRecords("backup-job")).thenReturn(Collections.emptyList());

        JobHistorySummary summary = reporter.summarize(job, 7);

        assertEquals("backup-job", summary.getJobId());
        assertEquals(0, summary.getTotalRuns());
        assertEquals(0, summary.getExceededCount());
        assertEquals(0.0, summary.getAverageDurationMs(), 0.001);
    }

    @Test
    void summarize_withRecords_computesCorrectAverageAndMax() {
        Instant recent = Instant.now().minusSeconds(3600);
        JobExecutionRecord r1 = new JobExecutionRecord("backup-job", recent, 200_000L, false);
        JobExecutionRecord r2 = new JobExecutionRecord("backup-job", recent, 400_000L, true);
        JobExecutionRecord r3 = new JobExecutionRecord("backup-job", recent, 300_000L, false);

        when(historyStore.getRecords("backup-job")).thenReturn(Arrays.asList(r1, r2, r3));

        JobHistorySummary summary = reporter.summarize(job, 7);

        assertEquals(3, summary.getTotalRuns());
        assertEquals(1, summary.getExceededCount());
        assertEquals(300_000.0, summary.getAverageDurationMs(), 0.001);
        assertEquals(400_000.0, summary.getMaxDurationMs(), 0.001);
        assertEquals(200_000.0, summary.getMinDurationMs(), 0.001);
    }

    @Test
    void summarize_filtersOutOldRecords() {
        Instant old = Instant.now().minusSeconds(86400 * 30); // 30 days ago
        Instant recent = Instant.now().minusSeconds(3600);
        JobExecutionRecord oldRecord = new JobExecutionRecord("backup-job", old, 500_000L, true);
        JobExecutionRecord recentRecord = new JobExecutionRecord("backup-job", recent, 100_000L, false);

        when(historyStore.getRecords("backup-job")).thenReturn(Arrays.asList(oldRecord, recentRecord));

        JobHistorySummary summary = reporter.summarize(job, 7);

        assertEquals(1, summary.getTotalRuns());
        assertEquals(0, summary.getExceededCount());
        assertEquals(100_000.0, summary.getAverageDurationMs(), 0.001);
    }

    @Test
    void summarize_nullJob_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> reporter.summarize(null, 7));
    }

    @Test
    void summarize_invalidLookback_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> reporter.summarize(job, 0));
        assertThrows(IllegalArgumentException.class, () -> reporter.summarize(job, -5));
    }

    @Test
    void constructor_nullStore_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HistoryReporter(null));
    }
}
