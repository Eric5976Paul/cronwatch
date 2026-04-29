package com.cronwatch.metrics;

import com.cronwatch.model.CronJob;
import com.cronwatch.history.JobExecutionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobMetricsCollectorTest {

    private JobMetricsCollector collector;
    private CronJob mockJob;
    private JobExecutionRecord mockRecord;

    @BeforeEach
    void setUp() {
        collector = new JobMetricsCollector();
        mockJob = mock(CronJob.class);
        mockRecord = mock(JobExecutionRecord.class);
        when(mockJob.getName()).thenReturn("backup-job");
    }

    @Test
    void recordAndRetrieveSummary() {
        when(mockRecord.getDurationMillis()).thenReturn(1200L);
        when(mockRecord.isSucceeded()).thenReturn(true);
        collector.record(mockJob, mockRecord);
        JobMetricsSummary summary = collector.getSummary("backup-job");
        assertNotNull(summary);
        assertEquals(1, summary.getTotalRuns());
        assertEquals(1200.0, summary.getAverageDurationMillis());
        assertEquals(1, summary.getSuccessCount());
    }

    @Test
    void multipleRecordsAccumulate() {
        when(mockRecord.getDurationMillis()).thenReturn(1000L).thenReturn(3000L);
        when(mockRecord.isSucceeded()).thenReturn(true).thenReturn(false);
        collector.record(mockJob, mockRecord);
        collector.record(mockJob, mockRecord);
        JobMetricsSummary s = collector.getSummary("backup-job");
        assertEquals(2, s.getTotalRuns());
        assertEquals(2000.0, s.getAverageDurationMillis());
        assertEquals(1, s.getFailureCount());
    }

    @Test
    void getJobsExceedingThreshold() {
        when(mockRecord.getDurationMillis()).thenReturn(5000L);
        when(mockRecord.isSucceeded()).thenReturn(true);
        collector.record(mockJob, mockRecord);
        List<String> over = collector.getJobsExceedingThreshold(Duration.ofSeconds(3));
        assertTrue(over.contains("backup-job"));
        List<String> notOver = collector.getJobsExceedingThreshold(Duration.ofSeconds(10));
        assertTrue(notOver.isEmpty());
    }

    @Test
    void nullJobThrows() {
        assertThrows(IllegalArgumentException.class, () -> collector.record(null, mockRecord));
    }

    @Test
    void resetClearsSummary() {
        when(mockRecord.getDurationMillis()).thenReturn(500L);
        when(mockRecord.isSucceeded()).thenReturn(true);
        collector.record(mockJob, mockRecord);
        collector.reset("backup-job");
        assertNull(collector.getSummary("backup-job"));
    }
}
