package com.cronwatch.retry;

import com.cronwatch.alert.AlertNotifier;
import com.cronwatch.model.CronJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RetryManagerTest {

    private AlertNotifier alertNotifier;
    private RetryManager retryManager;
    private CronJob mockJob;

    @BeforeEach
    void setUp() {
        alertNotifier = Mockito.mock(AlertNotifier.class);
        retryManager = new RetryManager(alertNotifier);
        mockJob = Mockito.mock(CronJob.class);
        when(mockJob.getId()).thenReturn("job-001");
    }

    @Test
    void shouldRetry_returnsTrue_whenAttemptsRemaining() {
        RetryPolicy policy = new RetryPolicy(3, 1000, false);
        when(mockJob.getRetryPolicy()).thenReturn(policy);

        assertTrue(retryManager.shouldRetry(mockJob));
    }

    @Test
    void shouldRetry_returnsFalse_whenNoRetryPolicy() {
        when(mockJob.getRetryPolicy()).thenReturn(RetryPolicy.noRetry());

        assertFalse(retryManager.shouldRetry(mockJob));
    }

    @Test
    void shouldRetry_returnsFalse_afterAttemptsExhausted() {
        RetryPolicy policy = new RetryPolicy(2, 500, false);
        when(mockJob.getRetryPolicy()).thenReturn(policy);

        retryManager.recordRetryAttempt(mockJob);
        retryManager.recordRetryAttempt(mockJob);

        assertFalse(retryManager.shouldRetry(mockJob));
    }

    @Test
    void recordRetryAttempt_sendsAlertWhenExhausted() {
        RetryPolicy policy = new RetryPolicy(1, 500, false);
        when(mockJob.getRetryPolicy()).thenReturn(policy);

        retryManager.recordRetryAttempt(mockJob);

        verify(alertNotifier, times(1)).sendAlert(eq("RETRY_EXHAUSTED"), anyString());
    }

    @Test
    void resetRetryState_allowsRetryAfterReset() {
        RetryPolicy policy = new RetryPolicy(1, 500, false);
        when(mockJob.getRetryPolicy()).thenReturn(policy);

        retryManager.recordRetryAttempt(mockJob);
        assertFalse(retryManager.shouldRetry(mockJob));

        retryManager.resetRetryState("job-001");
        assertTrue(retryManager.shouldRetry(mockJob));
    }

    @Test
    void recordRetryAttempt_returnsExponentialDelay() {
        RetryPolicy policy = new RetryPolicy(4, 1000, true);
        when(mockJob.getRetryPolicy()).thenReturn(policy);

        long delay1 = retryManager.recordRetryAttempt(mockJob);
        long delay2 = retryManager.recordRetryAttempt(mockJob);

        assertEquals(1000L, delay1);
        assertEquals(2000L, delay2);
    }
}
