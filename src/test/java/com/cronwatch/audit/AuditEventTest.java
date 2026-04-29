package com.cronwatch.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventTest {

    @Test
    void constructor_shouldSetFieldsCorrectly() {
        AuditEvent event = new AuditEvent("cleanup-job", AuditEvent.EventType.JOB_FAILED, "exit code 1");
        assertEquals("cleanup-job", event.getJobName());
        assertEquals(AuditEvent.EventType.JOB_FAILED, event.getEventType());
        assertEquals("exit code 1", event.getDetails());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void constructor_withTimestamp_shouldUseProvidedTimestamp() {
        Instant ts = Instant.parse("2024-03-15T08:30:00Z");
        AuditEvent event = new AuditEvent("sync-job", AuditEvent.EventType.ALERT_SENT, ts, "email sent");
        assertEquals(ts, event.getTimestamp());
    }

    @Test
    void constructor_nullJobName_shouldThrow() {
        assertThrows(NullPointerException.class, () ->
                new AuditEvent(null, AuditEvent.EventType.JOB_STARTED, "details"));
    }

    @Test
    void constructor_nullEventType_shouldThrow() {
        assertThrows(NullPointerException.class, () ->
                new AuditEvent("job", null, "details"));
    }

    @Test
    void toString_shouldContainKeyFields() {
        AuditEvent event = new AuditEvent("index-job", AuditEvent.EventType.THRESHOLD_EXCEEDED, "took 120s");
        String str = event.toString();
        assertTrue(str.contains("index-job"));
        assertTrue(str.contains("THRESHOLD_EXCEEDED"));
        assertTrue(str.contains("took 120s"));
    }

    @Test
    void eventTypes_shouldCoverAllLifecycleStages() {
        AuditEvent.EventType[] types = AuditEvent.EventType.values();
        assertEquals(6, types.length);
    }
}
