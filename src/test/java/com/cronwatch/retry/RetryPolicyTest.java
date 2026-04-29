package com.cronwatch.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void constructor_throwsOnNegativeMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(-1, 1000, false));
    }

    @Test
    void constructor_throwsOnNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(3, -1, false));
    }

    @Test
    void noRetry_returnsZeroAttempts() {
        RetryPolicy policy = RetryPolicy.noRetry();
        assertEquals(0, policy.getMaxAttempts());
        assertEquals(0, policy.getRetryDelayMillis());
        assertFalse(policy.isExponentialBackoff());
    }

    @Test
    void getDelayForAttempt_linearPolicy_returnsConstantDelay() {
        RetryPolicy policy = new RetryPolicy(5, 2000, false);
        assertEquals(2000L, policy.getDelayForAttempt(1));
        assertEquals(2000L, policy.getDelayForAttempt(3));
        assertEquals(2000L, policy.getDelayForAttempt(5));
    }

    @Test
    void getDelayForAttempt_exponentialPolicy_doublesEachAttempt() {
        RetryPolicy policy = new RetryPolicy(5, 500, true);
        assertEquals(500L, policy.getDelayForAttempt(1));
        assertEquals(1000L, policy.getDelayForAttempt(2));
        assertEquals(2000L, policy.getDelayForAttempt(3));
        assertEquals(4000L, policy.getDelayForAttempt(4));
    }

    @Test
    void toString_containsKeyFields() {
        RetryPolicy policy = new RetryPolicy(3, 1000, true);
        String str = policy.toString();
        assertTrue(str.contains("maxAttempts=3"));
        assertTrue(str.contains("retryDelayMillis=1000"));
        assertTrue(str.contains("exponentialBackoff=true"));
    }
}
