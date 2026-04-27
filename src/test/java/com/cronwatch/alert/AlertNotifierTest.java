package com.cronwatch.alert;

import com.cronwatch.config.CronWatchConfig;
import com.cronwatch.model.CronJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertNotifierTest {

    private CronWatchConfig config;
    private AlertNotifier notifier;
    private CronJob job;

    @BeforeEach
    void setUp() {
        config = mock(CronWatchConfig.class);
        when(config.isEmailAlertsEnabled()).thenReturn(false);
        when(config.getAlertEmailRecipient()).thenReturn("ops@example.com");

        notifier = new AlertNotifier(config);

        job = mock(CronJob.class);
        when(job.getName()).thenReturn("backup-job");
    }

    @Test
    void sendAlert_recordsAlertInHistory() {
        notifier.sendAlert(job, 120L, 60L);

        List<AlertRecord> history = notifier.getAlertHistory();
        assertEquals(1, history.size());
        assertEquals("backup-job", history.get(0).getJobName());
    }

    @Test
    void sendAlert_messageContainsJobNameAndDurations() {
        notifier.sendAlert(job, 120L, 60L);

        String message = notifier.getAlertHistory().get(0).getMessage();
        assertTrue(message.contains("backup-job"));
        assertTrue(message.contains("120"));
        assertTrue(message.contains("60"));
    }

    @Test
    void sendAlert_multipleTimes_allRecorded() {
        notifier.sendAlert(job, 90L, 60L);
        notifier.sendAlert(job, 150L, 60L);

        assertEquals(2, notifier.getAlertHistory().size());
    }

    @Test
    void getAlertHistory_returnsUnmodifiableList() {
        notifier.sendAlert(job, 90L, 60L);
        List<AlertRecord> history = notifier.getAlertHistory();

        assertThrows(UnsupportedOperationException.class, () -> history.add(null));
    }

    @Test
    void alertRecord_timestampIsSet() {
        notifier.sendAlert(job, 90L, 60L);
        AlertRecord record = notifier.getAlertHistory().get(0);

        assertNotNull(record.getTimestamp());
    }
}
