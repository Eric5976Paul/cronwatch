package com.cronwatch.throttle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AlertThrottleManagerTest {

    private AlertThrottleManager manager;
    private static final String JOB = "nightly-backup";

    @BeforeEach
    void setUp() {
        AlertThrottlePolicy policy = new AlertThrottlePolicy(Duration.ofMinutes(10), 2, true);
        manager = new AlertThrottleManager(policy);
    }

    @Test
    void firstAlertIsAllowed() {
        assertTrue(manager.shouldSendAlert(JOB, Instant.now()));
    }

    @Test
    void alertsWithinLimitAreAllowed() {
        Instant base = Instant.now();
        assertTrue(manager.shouldSendAlert(JOB, base));
        assertTrue(manager.shouldSendAlert(JOB, base.plusSeconds(30)));
    }

    @Test
    void alertBeyondLimitIsSuppressed() {
        Instant base = Instant.now();
        manager.shouldSendAlert(JOB, base);
        manager.shouldSendAlert(JOB, base.plusSeconds(30));
        assertFalse(manager.shouldSendAlert(JOB, base.plusSeconds(60)));
    }

    @Test
    void suppressedCountIsTracked() {
        Instant base = Instant.now();
        manager.shouldSendAlert(JOB, base);
        manager.shouldSendAlert(JOB, base.plusSeconds(10));
        manager.shouldSendAlert(JOB, base.plusSeconds(20)); // suppressed
        manager.shouldSendAlert(JOB, base.plusSeconds(30)); // suppressed

        AlertThrottleState state = manager.getState(JOB);
        assertNotNull(state);
        assertEquals(2, state.getTotalSuppressed());
    }

    @Test
    void alertsAfterWindowExpireAreAllowed() {
        Instant base = Instant.now();
        manager.shouldSendAlert(JOB, base);
        manager.shouldSendAlert(JOB, base.plusSeconds(10));
        // Advance past the 10-minute window
        assertTrue(manager.shouldSendAlert(JOB, base.plusSeconds(700)));
    }

    @Test
    void disabledPolicyAlwaysAllows() {
        AlertThrottleManager disabledManager = new AlertThrottleManager(AlertThrottlePolicy.disabled());
        Instant base = Instant.now();
        for (int i = 0; i < 20; i++) {
            assertTrue(disabledManager.shouldSendAlert(JOB, base.plusSeconds(i)));
        }
    }

    @Test
    void resetClearsState() {
        Instant base = Instant.now();
        manager.shouldSendAlert(JOB, base);
        manager.shouldSendAlert(JOB, base.plusSeconds(5));
        manager.reset(JOB);
        assertNull(manager.getState(JOB));
        assertTrue(manager.shouldSendAlert(JOB, base.plusSeconds(10)));
    }
}
