package com.cronwatch.throttle;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AlertThrottlePolicyTest {

    @Test
    void defaultPolicyHasExpectedValues() {
        AlertThrottlePolicy policy = AlertThrottlePolicy.defaultPolicy();
        assertEquals(Duration.ofMinutes(15), policy.getSuppressionWindow());
        assertEquals(3, policy.getMaxAlertsPerWindow());
        assertTrue(policy.isEnabled());
    }

    @Test
    void disabledPolicyIsNotEnabled() {
        AlertThrottlePolicy policy = AlertThrottlePolicy.disabled();
        assertFalse(policy.isEnabled());
    }

    @Test
    void constructorRejectsNegativeWindow() {
        assertThrows(IllegalArgumentException.class,
                () -> new AlertThrottlePolicy(Duration.ofMinutes(-1), 3, true));
    }

    @Test
    void constructorRejectsZeroMaxAlerts() {
        assertThrows(IllegalArgumentException.class,
                () -> new AlertThrottlePolicy(Duration.ofMinutes(5), 0, true));
    }

    @Test
    void toStringContainsKeyFields() {
        AlertThrottlePolicy policy = new AlertThrottlePolicy(Duration.ofMinutes(5), 2, true);
        String str = policy.toString();
        assertTrue(str.contains("PT5M") || str.contains("5"));
        assertTrue(str.contains("2"));
        assertTrue(str.contains("true"));
    }
}
