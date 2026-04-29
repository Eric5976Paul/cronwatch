package com.cronwatch.notification;

import com.cronwatch.alert.AlertRecord;
import com.cronwatch.config.CronWatchConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationChannelTest {

    @Mock
    private CronWatchConfig config;

    @Mock
    private AlertRecord alertRecord;

    @BeforeEach
    void setUp() {
        when(alertRecord.getJobName()).thenReturn("backup-job");
        when(alertRecord.getAlertType()).thenReturn("DURATION_EXCEEDED");
        when(alertRecord.getMessage()).thenReturn("Job ran for 120s, expected max 60s");
        when(alertRecord.getTimestamp()).thenReturn(Instant.now());
    }

    private void setupEnabledConfig() {
        when(config.getProperty("notification.email.enabled", "false")).thenReturn("true");
        when(config.getProperty("notification.email.recipient", "")).thenReturn("ops@example.com");
        when(config.getProperty("notification.email.sender", "cronwatch@localhost")).thenReturn("cronwatch@example.com");
        when(config.getProperty("notification.email.smtp.host", "localhost")).thenReturn("smtp.example.com");
        when(config.getProperty("notification.email.smtp.port", "25")).thenReturn("587");
    }

    private void setupDisabledConfig() {
        when(config.getProperty("notification.email.enabled", "false")).thenReturn("false");
        when(config.getProperty("notification.email.recipient", "")).thenReturn("");
        when(config.getProperty("notification.email.sender", "cronwatch@localhost")).thenReturn("cronwatch@localhost");
        when(config.getProperty("notification.email.smtp.host", "localhost")).thenReturn("localhost");
        when(config.getProperty("notification.email.smtp.port", "25")).thenReturn("25");
    }

    @Test
    void isEnabled_whenConfiguredWithRecipient_returnsTrue() {
        setupEnabledConfig();
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        assertTrue(channel.isEnabled());
    }

    @Test
    void isEnabled_whenDisabledInConfig_returnsFalse() {
        setupDisabledConfig();
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        assertFalse(channel.isEnabled());
    }

    @Test
    void isEnabled_whenEnabledButNoRecipient_returnsFalse() {
        when(config.getProperty("notification.email.enabled", "false")).thenReturn("true");
        when(config.getProperty("notification.email.recipient", "")).thenReturn("");
        when(config.getProperty("notification.email.sender", "cronwatch@localhost")).thenReturn("cronwatch@localhost");
        when(config.getProperty("notification.email.smtp.host", "localhost")).thenReturn("localhost");
        when(config.getProperty("notification.email.smtp.port", "25")).thenReturn("25");
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        assertFalse(channel.isEnabled());
    }

    @Test
    void getChannelName_returnsEmail() {
        setupDisabledConfig();
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        assertEquals("email", channel.getChannelName());
    }

    @Test
    void send_whenEnabled_doesNotThrow() {
        setupEnabledConfig();
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        assertDoesNotThrow(() -> channel.send(alertRecord));
    }

    @Test
    void send_whenDisabled_skipsWithoutInteractingWithAlert() {
        setupDisabledConfig();
        EmailNotificationChannel channel = new EmailNotificationChannel(config);
        channel.send(alertRecord);
        verify(alertRecord, never()).getAlertType();
    }
}
