package com.cronwatch.monitor;

import com.cronwatch.alert.AlertNotifier;
import com.cronwatch.alert.AlertRecord;
import com.cronwatch.model.CronJob;
import com.cronwatch.history.JobExecutionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DurationAlertHandler.
 * Verifies that alerts are triggered correctly when job durations exceed thresholds.
 */
@ExtendWith(MockitoExtension.class)
class DurationAlertHandlerTest {

    @Mock
    private AlertNotifier alertNotifier;

    private DurationAlertHandler handler;

    private CronJob sampleJob;

    @BeforeEach
    void setUp() {
        handler = new DurationAlertHandler(alertNotifier);

        sampleJob = new CronJob();
        sampleJob.setJobId("backup-job");
        sampleJob.setName("Nightly Backup");
        sampleJob.setExpectedMaxDurationSeconds(120); // 2 minutes threshold
    }

    @Test
    void shouldNotSendAlertWhenDurationIsWithinThreshold() {
        Instant start = Instant.now().minusSeconds(90);
        Instant end = Instant.now();
        JobExecutionRecord record = new JobExecutionRecord(sampleJob.getJobId(), start, end);

        handler.evaluate(sampleJob, record);

        verify(alertNotifier, never()).sendAlert(any(AlertRecord.class));
    }

    @Test
    void shouldSendAlertWhenDurationExceedsThreshold() {
        Instant start = Instant.now().minusSeconds(200);
        Instant end = Instant.now();
        JobExecutionRecord record = new JobExecutionRecord(sampleJob.getJobId(), start, end);

        handler.evaluate(sampleJob, record);

        verify(alertNotifier, times(1)).sendAlert(any(AlertRecord.class));
    }

    @Test
    void alertRecordShouldContainCorrectJobId() {
        Instant start = Instant.now().minusSeconds(300);
        Instant end = Instant.now();
        JobExecutionRecord record = new JobExecutionRecord(sampleJob.getJobId(), start, end);

        ArgumentCaptor<AlertRecord> captor = ArgumentCaptor.forClass(AlertRecord.class);
        handler.evaluate(sampleJob, record);

        verify(alertNotifier).sendAlert(captor.capture());
        AlertRecord alert = captor.getValue();
        assertThat(alert.getJobId()).isEqualTo("backup-job");
    }

    @Test
    void alertRecordShouldContainActualAndExpectedDuration() {
        Instant start = Instant.now().minusSeconds(250);
        Instant end = Instant.now();
        JobExecutionRecord record = new JobExecutionRecord(sampleJob.getJobId(), start, end);

        ArgumentCaptor<AlertRecord> captor = ArgumentCaptor.forClass(AlertRecord.class);
        handler.evaluate(sampleJob, record);

        verify(alertNotifier).sendAlert(captor.capture());
        AlertRecord alert = captor.getValue();
        assertThat(alert.getActualDurationSeconds()).isGreaterThan(120L);
        assertThat(alert.getExpectedMaxDurationSeconds()).isEqualTo(120L);
    }

    @Test
    void shouldNotSendAlertWhenDurationExactlyMatchesThreshold() {
        Instant start = Instant.now().minusSeconds(120);
        Instant end = Instant.now();
        JobExecutionRecord record = new JobExecutionRecord(sampleJob.getJobId(), start, end);

        handler.evaluate(sampleJob, record);

        verify(alertNotifier, never()).sendAlert(any(AlertRecord.class));
    }

    @Test
    void shouldHandleJobWithZeroThresholdGracefully() {
        sampleJob.setExpectedMaxDurationSeconds(0);
        Instant start = Instant.now().minusSeconds(1);
        Instant end = Instant.now();
        JobExecutionRecord record = new JobExecutionRecord(sampleJob.getJobId(), start, end);

        // Any positive duration should trigger alert when threshold is 0
        handler.evaluate(sampleJob, record);

        verify(alertNotifier, times(1)).sendAlert(any(AlertRecord.class));
    }
}
