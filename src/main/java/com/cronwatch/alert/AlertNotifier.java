package com.cronwatch.alert;

import com.cronwatch.model.CronJob;
import com.cronwatch.config.CronWatchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles dispatching alerts when a cron job exceeds its expected duration.
 */
public class AlertNotifier {

    private static final Logger logger = LoggerFactory.getLogger(AlertNotifier.class);

    private final CronWatchConfig config;
    private final List<AlertRecord> alertHistory;

    public AlertNotifier(CronWatchConfig config) {
        this.config = config;
        this.alertHistory = new ArrayList<>();
    }

    /**
     * Sends an alert for a job that has exceeded its expected duration.
     *
     * @param job             the cron job that triggered the alert
     * @param actualSeconds   how long the job has been running (seconds)
     * @param expectedSeconds the configured maximum duration (seconds)
     */
    public void sendAlert(CronJob job, long actualSeconds, long expectedSeconds) {
        String message = buildAlertMessage(job, actualSeconds, expectedSeconds);
        AlertRecord record = new AlertRecord(job.getName(), message, Instant.now());
        alertHistory.add(record);

        if (config.isEmailAlertsEnabled()) {
            dispatchEmail(config.getAlertEmailRecipient(), message);
        } else {
            logger.warn("[ALERT] {}", message);
        }
    }

    private String buildAlertMessage(CronJob job, long actual, long expected) {
        return String.format(
            "CronWatch Alert: job '%s' has been running for %ds, exceeding the expected %ds threshold.",
            job.getName(), actual, expected
        );
    }

    private void dispatchEmail(String recipient, String message) {
        // Placeholder: integrate with JavaMail or an SMTP client
        logger.info("Sending alert email to {}: {}", recipient, message);
    }

    public List<AlertRecord> getAlertHistory() {
        return Collections.unmodifiableList(alertHistory);
    }
}
