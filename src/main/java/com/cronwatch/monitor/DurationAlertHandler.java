package com.cronwatch.monitor;

import com.cronwatch.model.CronJob;

import java.time.Duration;
import java.util.logging.Logger;

/**
 * Handles alerts when a cron job exceeds its expected maximum duration.
 */
public class DurationAlertHandler {

    private static final Logger logger = Logger.getLogger(DurationAlertHandler.class.getName());

    /**
     * Called when a job's actual execution time exceeds the configured threshold.
     *
     * @param job the job that exceeded its expected duration
     */
    public void onDurationExceeded(CronJob job) {
        Duration expected = job.getExpectedMaxDuration();
        Duration actual = job.getActualDuration();

        String message = buildAlertMessage(job, expected, actual);
        logger.warning(message);
        sendAlert(job.getName(), message);
    }

    protected void sendAlert(String jobName, String message) {
        // Default implementation logs to stderr; extend to send email, Slack, PagerDuty, etc.
        System.err.println("[CRONWATCH ALERT] " + message);
    }

    private String buildAlertMessage(CronJob job, Duration expected, Duration actual) {
        return String.format(
                "Job '%s' exceeded expected duration. Expected: %s, Actual: %s. Started: %s, Ended: %s",
                job.getName(),
                formatDuration(expected),
                actual != null ? formatDuration(actual) : "unknown",
                job.getLastStartTime(),
                job.getLastEndTime()
        );
    }

    private String formatDuration(Duration d) {
        long minutes = d.toMinutes();
        long seconds = d.toSecondsPart();
        return String.format("%dm %ds", minutes, seconds);
    }
}
