package com.cronwatch.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads and provides access to cronwatch configuration properties.
 * Reads from cronwatch.properties on the classpath.
 */
public class CronWatchConfig {

    private static final Logger logger = Logger.getLogger(CronWatchConfig.class.getName());
    private static final String CONFIG_FILE = "cronwatch.properties";

    private final Properties properties;

    public CronWatchConfig() {
        this.properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warning("Config file not found: " + CONFIG_FILE + ". Using defaults.");
                applyDefaults();
                return;
            }
            properties.load(input);
            logger.info("Loaded configuration from " + CONFIG_FILE);
        } catch (IOException e) {
            logger.severe("Failed to load config: " + e.getMessage());
            applyDefaults();
        }
    }

    private void applyDefaults() {
        properties.setProperty("alert.threshold.multiplier", "1.5");
        properties.setProperty("monitor.poll.interval.seconds", "30");
        properties.setProperty("alert.email.enabled", "false");
        properties.setProperty("alert.log.enabled", "true");
    }

    public double getAlertThresholdMultiplier() {
        return Double.parseDouble(properties.getProperty("alert.threshold.multiplier", "1.5"));
    }

    public int getPollIntervalSeconds() {
        return Integer.parseInt(properties.getProperty("monitor.poll.interval.seconds", "30"));
    }

    public boolean isEmailAlertEnabled() {
        return Boolean.parseBoolean(properties.getProperty("alert.email.enabled", "false"));
    }

    public boolean isLogAlertEnabled() {
        return Boolean.parseBoolean(properties.getProperty("alert.log.enabled", "true"));
    }

    public Map<String, String> getAllProperties() {
        Map<String, String> result = new HashMap<>();
        for (String name : properties.stringPropertyNames()) {
            result.put(name, properties.getProperty(name));
        }
        return result;
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
