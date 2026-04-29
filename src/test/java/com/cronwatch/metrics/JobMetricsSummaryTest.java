package com.cronwatch.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobMetricsSummaryTest {

    @Test
    void initialStateIsZero() {
        JobMetricsSummary s = new JobMetricsSummary("nightly-sync");
        assertEquals(0, s.getTotalRuns());
        assertEquals(0.0, s.getAverageDurationMillis());
        assertEquals(0.0, s.getSuccessRate());
        assertEquals(0, s.getMinDurationMillis());
        assertEquals(0, s.getMaxDurationMillis());
    }

    @Test
    void singleSampleMetrics() {
        JobMetricsSummary s = new JobMetricsSummary("nightly-sync");
        s.addSample(800L, true);
        assertEquals(1, s.getTotalRuns());
        assertEquals(800.0, s.getAverageDurationMillis());
        assertEquals(800L, s.getMinDurationMillis());
        assertEquals(800L, s.getMaxDurationMillis());
        assertEquals(1.0, s.getSuccessRate());
    }

    @Test
    void minMaxTrackedCorrectly() {
        JobMetricsSummary s = new JobMetricsSummary("nightly-sync");
        s.addSample(300L, true);
        s.addSample(1500L, false);
        s.addSample(900L, true);
        assertEquals(300L, s.getMinDurationMillis());
        assertEquals(1500L, s.getMaxDurationMillis());
        assertEquals(2.0 / 3.0, s.getSuccessRate(), 0.001);
    }

    @Test
    void blankNameThrows() {
        assertThrows(IllegalArgumentException.class, () -> new JobMetricsSummary(""));
        assertThrows(IllegalArgumentException.class, () -> new JobMetricsSummary("   "));
        assertThrows(IllegalArgumentException.class, () -> new JobMetricsSummary(null));
    }

    @Test
    void toStringContainsJobName() {
        JobMetricsSummary s = new JobMetricsSummary("report-gen");
        assertTrue(s.toString().contains("report-gen"));
    }
}
