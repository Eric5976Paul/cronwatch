package com.cronwatch.snapshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotManagerTest {

    private SnapshotManager manager;

    @BeforeEach
    void setUp() {
        manager = new SnapshotManager();
    }

    private JobStatusSnapshot makeSnapshot(String id, JobStatusSnapshot.Status status,
                                           long durationMs, long maxMs, Instant capturedAt) {
        return new JobStatusSnapshot(id, "job-" + id, status, capturedAt, durationMs, maxMs, "msg");
    }

    @Test
    void recordAndRetrieveLatest() {
        JobStatusSnapshot snap = makeSnapshot("j1", JobStatusSnapshot.Status.RUNNING, 1000, 5000, Instant.now());
        manager.record(snap);
        Optional<JobStatusSnapshot> result = manager.getLatest("j1");
        assertTrue(result.isPresent());
        assertEquals(JobStatusSnapshot.Status.RUNNING, result.get().getStatus());
    }

    @Test
    void recordOverwritesPreviousSnapshot() {
        Instant t1 = Instant.now().minusSeconds(10);
        Instant t2 = Instant.now();
        manager.record(makeSnapshot("j2", JobStatusSnapshot.Status.RUNNING, 1000, 5000, t1));
        manager.record(makeSnapshot("j2", JobStatusSnapshot.Status.COMPLETED, 3000, 5000, t2));
        Optional<JobStatusSnapshot> result = manager.getLatest("j2");
        assertTrue(result.isPresent());
        assertEquals(JobStatusSnapshot.Status.COMPLETED, result.get().getStatus());
    }

    @Test
    void getOverdueJobsReturnsOnlyOverdue() {
        manager.record(makeSnapshot("j3", JobStatusSnapshot.Status.RUNNING, 6000, 5000, Instant.now()));
        manager.record(makeSnapshot("j4", JobStatusSnapshot.Status.RUNNING, 2000, 5000, Instant.now()));
        List<JobStatusSnapshot> overdue = manager.getOverdueJobs();
        assertEquals(1, overdue.size());
        assertEquals("j3", overdue.get(0).getJobId());
    }

    @Test
    void getByStatusFiltersCorrectly() {
        manager.record(makeSnapshot("j5", JobStatusSnapshot.Status.FAILED, 1000, 5000, Instant.now()));
        manager.record(makeSnapshot("j6", JobStatusSnapshot.Status.COMPLETED, 1000, 5000, Instant.now()));
        List<JobStatusSnapshot> failed = manager.getByStatus(JobStatusSnapshot.Status.FAILED);
        assertEquals(1, failed.size());
        assertEquals("j5", failed.get(0).getJobId());
    }

    @Test
    void evictRemovesSnapshot() {
        manager.record(makeSnapshot("j7", JobStatusSnapshot.Status.RUNNING, 500, 5000, Instant.now()));
        manager.evict("j7");
        assertFalse(manager.getLatest("j7").isPresent());
    }

    @Test
    void evictStaleBeforeRemovesOldSnapshots() {
        Instant old = Instant.now().minusSeconds(120);
        Instant recent = Instant.now();
        manager.record(makeSnapshot("j8", JobStatusSnapshot.Status.COMPLETED, 1000, 5000, old));
        manager.record(makeSnapshot("j9", JobStatusSnapshot.Status.COMPLETED, 1000, 5000, recent));
        manager.evictStaleBefore(Instant.now().minusSeconds(60));
        assertFalse(manager.getLatest("j8").isPresent());
        assertTrue(manager.getLatest("j9").isPresent());
    }

    @Test
    void getAllSnapshotsReturnsAll() {
        manager.record(makeSnapshot("j10", JobStatusSnapshot.Status.RUNNING, 1000, 5000, Instant.now()));
        manager.record(makeSnapshot("j11", JobStatusSnapshot.Status.RUNNING, 1000, 5000, Instant.now()));
        assertEquals(2, manager.getAllSnapshots().size());
    }
}
