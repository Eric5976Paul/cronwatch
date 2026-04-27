package com.cronwatch.monitor;

import com.cronwatch.model.CronJob;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Tracks cron job start/end events and detects duration violations.
 */
public class JobMonitor {

    private static final Logger logger = Logger.getLogger(JobMonitor.class.getName());

    private final Map<String, CronJob> registry = new ConcurrentHashMap<>();
    private final DurationAlertHandler alertHandler;

    public JobMonitor(DurationAlertHandler alertHandler) {
        this.alertHandler = alertHandler;
    }

    public void register(CronJob job) {
        registry.put(job.getName(), job);
        logger.info("Registered job: " + job.getName());
    }

    public void onJobStarted(String jobName, Instant startTime) {
        CronJob job = getJobOrThrow(jobName);
        job.markStarted(startTime);
        logger.fine("Job started: " + jobName + " at " + startTime);
    }

    public void onJobCompleted(String jobName, Instant endTime) {
        CronJob job = getJobOrThrow(jobName);
        job.markCompleted(endTime);
        logger.fine("Job completed: " + jobName + " at " + endTime);

        if (job.isExceeded()) {
            logger.warning("Job exceeded expected duration: " + jobName);
            alertHandler.onDurationExceeded(job);
        }
    }

    public Optional<CronJob> getJob(String jobName) {
        return Optional.ofNullable(registry.get(jobName));
    }

    public Collection<CronJob> getAllJobs() {
        return Collections.unmodifiableCollection(registry.values());
    }

    private CronJob getJobOrThrow(String jobName) {
        CronJob job = registry.get(jobName);
        if (job == null) {
            throw new IllegalArgumentException("Unknown job: " + jobName);
        }
        return job;
    }
}
