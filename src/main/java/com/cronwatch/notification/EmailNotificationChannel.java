package com.cronwatch.notification;

import com.cronwatch.alert.AlertRecord;
import com.cronwatch.config.CronWatchConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Notification channel that sends alert emails when cron jobs exceed
 * their expected durations or fail to execute.
 */
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger logger = Logger.getLogger(EmailNotificationChannel.class.getName());

    private final String recipientAddress;
    private final String senderAddress;
    private final String smtpHost;
    private final int smtpPort;
    private final boolean enabled;

    public EmailNotificationChannel(CronWatchConfig config) {
        this.recipientAddress = config.getProperty("notification.email.recipient", "");
        this.senderAddress = config.getProperty("notification.email.sender", "cronwatch@localhost");
        this.smtpHost = config.getProperty("notification.email.smtp.host", "localhost");
        this.smtpPort = Integer.parseInt(config.getProperty("notification.email.smtp.port", "25"));
        this.enabled = Boolean.parseBoolean(config.getProperty("notification.email.enabled", "false"))
                && !recipientAddress.isEmpty();
    }

    @Override
    public void send(AlertRecord alert) {
        if (!enabled) {
            logger.fine("Email notifications disabled; skipping alert for job: " + alert.getJobName());
            return;
        }
        String subject = buildSubject(alert);
        String body = buildBody(alert);
        logger.info(String.format("Sending email alert to %s for job '%s' via %s:%d",
                recipientAddress, alert.getJobName(), smtpHost, smtpPort));
        // Actual SMTP dispatch would occur here via JavaMail or similar
        logger.fine("Subject: " + subject);
        logger.fine("Body: " + body);
    }

    @Override
    public String getChannelName() {
        return "email";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private String buildSubject(AlertRecord alert) {
        return String.format("[CronWatch] ALERT - Job '%s' %s",
                alert.getJobName(), alert.getAlertType());
    }

    private String buildBody(AlertRecord alert) {
        return String.format(
                "CronWatch Alert\n\nJob:       %s\nAlert:     %s\nMessage:   %s\nTimestamp: %s\n",
                alert.getJobName(),
                alert.getAlertType(),
                alert.getMessage(),
                alert.getTimestamp());
    }
}
