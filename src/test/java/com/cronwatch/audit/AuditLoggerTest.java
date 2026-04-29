package com.cronwatch.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();
    }

    @Test
    void logEvent_shouldStoreEvent() {
        auditLogger.log("backup-job", AuditEvent.EventType.JOB_STARTED, "Job started normally");
        List<AuditEvent> events = auditLogger.getEvents();
        assertEquals(1, events.size());
        assertEquals("backup-job", events.get(0).getJobName());
        assertEquals(AuditEvent.EventType.JOB_STARTED, events.get(0).getEventType());
    }

    @Test
    void logMultipleEvents_shouldReturnAll() {
        auditLogger.log("job-a", AuditEvent.EventType.JOB_STARTED, "started");
        auditLogger.log("job-a", AuditEvent.EventType.JOB_COMPLETED, "completed in 5s");
        auditLogger.log("job-b", AuditEvent.EventType.ALERT_SENT, "alert dispatched");
        assertEquals(3, auditLogger.getEvents().size());
    }

    @Test
    void getEventsForJob_shouldFilterByJobName() {
        auditLogger.log("job-a", AuditEvent.EventType.JOB_STARTED, "started");
        auditLogger.log("job-b", AuditEvent.EventType.JOB_FAILED, "failed");
        auditLogger.log("job-a", AuditEvent.EventType.THRESHOLD_EXCEEDED, "exceeded");

        List<AuditEvent> jobAEvents = auditLogger.getEventsForJob("job-a");
        assertEquals(2, jobAEvents.size());
        assertTrue(jobAEvents.stream().allMatch(e -> e.getJobName().equals("job-a")));
    }

    @Test
    void clear_shouldRemoveAllEvents() {
        auditLogger.log("job-x", AuditEvent.EventType.JOB_STARTED, "start");
        auditLogger.clear();
        assertTrue(auditLogger.getEvents().isEmpty());
    }

    @Test
    void auditEvent_withExplicitTimestamp_shouldPreserveIt() {
        Instant fixed = Instant.parse("2024-06-01T10:00:00Z");
        AuditEvent event = new AuditEvent("report-job", AuditEvent.EventType.RETRY_TRIGGERED, fixed, "retry #1");
        auditLogger.log(event);
        assertEquals(fixed, auditLogger.getEvents().get(0).getTimestamp());
    }

    @Test
    void getEvents_shouldReturnUnmodifiableList() {
        auditLogger.log("job-z", AuditEvent.EventType.JOB_COMPLETED, "done");
        List<AuditEvent> events = auditLogger.getEvents();
        assertThrows(UnsupportedOperationException.class, () -> events.add(
                new AuditEvent("job-z", AuditEvent.EventType.JOB_STARTED, "start")));
    }
}
