package com.cronwatch.audit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Records audit events to an in-memory log and optionally to a file.
 */
public class AuditLogger {

    private static final Logger LOG = Logger.getLogger(AuditLogger.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final List<AuditEvent> events = new CopyOnWriteArrayList<>();
    private final String auditFilePath;
    private final boolean fileLoggingEnabled;

    public AuditLogger(String auditFilePath) {
        this.auditFilePath = auditFilePath;
        this.fileLoggingEnabled = auditFilePath != null && !auditFilePath.isBlank();
    }

    public AuditLogger() {
        this(null);
    }

    public void log(AuditEvent event) {
        events.add(event);
        LOG.info(() -> "AUDIT: " + event);
        if (fileLoggingEnabled) {
            appendToFile(event);
        }
    }

    public void log(String jobName, AuditEvent.EventType type, String details) {
        log(new AuditEvent(jobName, type, details));
    }

    public List<AuditEvent> getEvents() {
        return Collections.unmodifiableList(new ArrayList<>(events));
    }

    public List<AuditEvent> getEventsForJob(String jobName) {
        List<AuditEvent> result = new ArrayList<>();
        for (AuditEvent e : events) {
            if (e.getJobName().equals(jobName)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public void clear() {
        events.clear();
    }

    private void appendToFile(AuditEvent event) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(auditFilePath, true))) {
            writer.write(FORMATTER.format(event.getTimestamp()) + " | " +
                    event.getEventType() + " | " +
                    event.getJobName() + " | " +
                    event.getDetails());
            writer.newLine();
        } catch (IOException e) {
            LOG.warning("Failed to write audit event to file: " + e.getMessage());
        }
    }
}
