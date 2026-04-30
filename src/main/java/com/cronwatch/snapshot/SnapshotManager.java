package com.cronwatch.snapshot;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages in-memory snapshots of current job statuses.
 * Allows querying the latest known state of all monitored cron jobs.
 */
public class SnapshotManager {

    private static final Logger logger = Logger.getLogger(SnapshotManager.class.getName());

    private final ConcurrentHashMap<String, JobStatusSnapshot> latestSnapshots = new ConcurrentHashMap<>();

    public void record(JobStatusSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        latestSnapshots.put(snapshot.getJobId(), snapshot);
        logger.fine(() -> "Recorded snapshot: " + snapshot);
    }

    public Optional<JobStatusSnapshot> getLatest(String jobId) {
        return Optional.ofNullable(latestSnapshots.get(jobId));
    }

    public List<JobStatusSnapshot> getAllSnapshots() {
        return Collections.unmodifiableList(new ArrayList<>(latestSnapshots.values()));
    }

    public List<JobStatusSnapshot> getOverdueJobs() {
        return latestSnapshots.values().stream()
                .filter(JobStatusSnapshot::isOverdue)
                .collect(Collectors.toList());
    }

    public List<JobStatusSnapshot> getByStatus(JobStatusSnapshot.Status status) {
        Objects.requireNonNull(status, "status must not be null");
        return latestSnapshots.values().stream()
                .filter(s -> s.getStatus() == status)
                .collect(Collectors.toList());
    }

    public void evict(String jobId) {
        latestSnapshots.remove(jobId);
        logger.fine(() -> "Evicted snapshot for jobId=" + jobId);
    }

    public void evictStaleBefore(Instant threshold) {
        Objects.requireNonNull(threshold, "threshold must not be null");
        latestSnapshots.entrySet().removeIf(entry -> {
            boolean stale = entry.getValue().getCapturedAt().isBefore(threshold);
            if (stale) {
                logger.fine(() -> "Evicting stale snapshot for jobId=" + entry.getKey());
            }
            return stale;
        });
    }

    public int size() {
        return latestSnapshots.size();
    }
}
