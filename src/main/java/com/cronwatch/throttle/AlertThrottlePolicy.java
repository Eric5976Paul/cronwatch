package com.cronwatch.throttle;

import java.time.Duration;

/**
 * Defines throttling rules for alert notifications to prevent alert storms.
 */
public class AlertThrottlePolicy {

    private final Duration suppressionWindow;
    private final int maxAlertsPerWindow;
    private final boolean enabled;

    public AlertThrottlePolicy(Duration suppressionWindow, int maxAlertsPerWindow, boolean enabled) {
        if (suppressionWindow == null || suppressionWindow.isNegative()) {
            throw new IllegalArgumentException("Suppression window must be a positive duration");
        }
        if (maxAlertsPerWindow < 1) {
            throw new IllegalArgumentException("maxAlertsPerWindow must be at least 1");
        }
        this.suppressionWindow = suppressionWindow;
        this.maxAlertsPerWindow = maxAlertsPerWindow;
        this.enabled = enabled;
    }

    public Duration getSuppressionWindow() {
        return suppressionWindow;
    }

    public int getMaxAlertsPerWindow() {
        return maxAlertsPerWindow;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static AlertThrottlePolicy defaultPolicy() {
        return new AlertThrottlePolicy(Duration.ofMinutes(15), 3, true);
    }

    public static AlertThrottlePolicy disabled() {
        return new AlertThrottlePolicy(Duration.ofMinutes(1), Integer.MAX_VALUE, false);
    }

    @Override
    public String toString() {
        return String.format("AlertThrottlePolicy{window=%s, maxAlerts=%d, enabled=%b}",
                suppressionWindow, maxAlertsPerWindow, enabled);
    }
}
