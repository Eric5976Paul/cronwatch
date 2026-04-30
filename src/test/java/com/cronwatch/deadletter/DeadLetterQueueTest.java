package com.cronwatch.deadletter;

import com.cronwatch.alert.AlertRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeadLetterQueueTest {

    private DeadLetterQueue queue;
    private AlertRecord mockAlert;

    @BeforeEach
    void setUp() {
        queue = new DeadLetterQueue(3);
        mockAlert = mock(AlertRecord.class);
        when(mockAlert.getJobName()).thenReturn("backup-job");
    }

    @Test
    void constructor_negativeCapacity_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new DeadLetterQueue(-1));
    }

    @Test
    void constructor_zeroCapacity_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new DeadLetterQueue(0));
    }

    @Test
    void enqueue_nullAlert_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null, "timeout"));
    }

    @Test
    void enqueue_singleAlert_sizeIsOne() {
        queue.enqueue(mockAlert, "SMTP failure");
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
    }

    @Test
    void enqueue_atCapacity_evictsOldestEntry() {
        AlertRecord first = mock(AlertRecord.class);
        when(first.getJobName()).thenReturn("first-job");
        AlertRecord second = mock(AlertRecord.class);
        when(second.getJobName()).thenReturn("second-job");
        AlertRecord third = mock(AlertRecord.class);
        when(third.getJobName()).thenReturn("third-job");
        AlertRecord fourth = mock(AlertRecord.class);
        when(fourth.getJobName()).thenReturn("fourth-job");

        queue.enqueue(first, "r1");
        queue.enqueue(second, "r2");
        queue.enqueue(third, "r3");
        queue.enqueue(fourth, "r4");

        assertEquals(3, queue.size());
        List<DeadLetterEntry> entries = queue.peek();
        assertEquals("second-job", entries.get(0).getAlert().getJobName());
    }

    @Test
    void drainAll_returnsAllEntriesAndClearsQueue() {
        queue.enqueue(mockAlert, "network error");
        AlertRecord another = mock(AlertRecord.class);
        when(another.getJobName()).thenReturn("cleanup-job");
        queue.enqueue(another, "auth failure");

        List<DeadLetterEntry> drained = queue.drainAll();

        assertEquals(2, drained.size());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void peek_doesNotRemoveEntries() {
        queue.enqueue(mockAlert, "timeout");
        queue.peek();
        assertEquals(1, queue.size());
    }

    @Test
    void deadLetterEntry_storesFailureReasonAndTimestamp() {
        queue.enqueue(mockAlert, "connection refused");
        List<DeadLetterEntry> entries = queue.peek();
        assertEquals(1, entries.size());
        DeadLetterEntry entry = entries.get(0);
        assertEquals("connection refused", entry.getFailureReason());
        assertNotNull(entry.getFailedAt());
        assertSame(mockAlert, entry.getAlert());
    }
}
