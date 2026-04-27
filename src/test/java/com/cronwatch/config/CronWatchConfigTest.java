package com.cronwatch.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CronWatchConfigTest {

    private CronWatchConfig config;

    @BeforeEach
    void setUp() {
        // Uses cronwatch.properties from src/test/resources (or src/main/resources)
        config = new CronWatchConfig();
    }

    @Test
    void testAlertThresholdMultiplierLoaded() {
        double multiplier = config.getAlertThresholdMultiplier();
        assertTrue(multiplier > 0, "Multiplier should be positive");
    }

    @Test
    void testPollIntervalSecondsLoaded() {
        int interval = config.getPollIntervalSeconds();
        assertTrue(interval > 0, "Poll interval should be positive");
    }

    @Test
    void testLogAlertEnabledByDefault() {
        assertTrue(config.isLogAlertEnabled(), "Log alerting should be enabled by default");
    }

    @Test
    void testEmailAlertDisabledByDefault() {
        assertFalse(config.isEmailAlertEnabled(), "Email alerting should be disabled by default");
    }

    @Test
    void testGetAllPropertiesNotEmpty() {
        Map<String, String> props = config.getAllProperties();
        assertNotNull(props);
        assertFalse(props.isEmpty(), "Properties map should not be empty");
    }

    @Test
    void testGetWithDefaultFallback() {
        String value = config.get("nonexistent.key", "fallback");
        assertEquals("fallback", value);
    }

    @Test
    void testGetExistingKey() {
        String value = config.get("alert.log.enabled", "false");
        assertEquals("true", value);
    }
}
