package com.cronwatch.monitor;

import com.cronwatch.model.CronJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JobMonitorTest {

    private List<CronJob> alertedJobs;
    private JobMonitor monitor;

    @BeforeEach
    void setUp() {
        alertedJobs = new ArrayList<>();
        DurationAlertHandler handler = new DurationAlertHandler() {
            @Override
            protected void sendAlert(String jobName, String message) {
                // capture alerts silently for assertions
            }

            @Override
            public void onDurationExceeded(CronJob job) {
                alertedJobs.add(job);
                super.onDurationExceeded(job);
            }
        };
        monitor = new JobMonitor(handler);
    }

    @Test
    void jobCompletesWithinExpectedDuration_noAlertFired() {
        CronJob job = new CronJob("backup", "0 2 * * *", Duration.ofMinutes(10));
        monitor.register(job);

        Instant start = Instant.now();
        monitor.onJobStarted("backup", start);
        monitor.onJobCompleted("backup", start.plusSeconds(300)); // 5 minutes

        assertEquals(CronJob.JobStatus.COMPLETED, job.getStatus());
        assertTrue(alertedJobs.isEmpty());
    }

    @Test
    void jobExceedsExpectedDuration_alertFired() {
        CronJob job = new CronJob("report", "0 6 * * *", Duration.ofMinutes(5));
        monitor.register(job);

        Instant start = Instant.now();
        monitor.onJobStarted("report", start);
        monitor.onJobCompleted("report", start.plusSeconds(600)); // 10 minutes

        assertEquals(CronJob.JobStatus.EXCEEDED, job.getStatus());
        assertEquals(1, alertedJobs.size());
        assertEquals("report", alertedJobs.get(0).getName());
    }

    @Test
    void unknownJobStart_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> monitor.onJobStarted("nonexistent", Instant.now()));
    }

    @Test
    void registeredJobIsRetrievable() {
        CronJob job = new CronJob("cleanup", "@daily", Duration.ofMinutes(2));
        monitor.register(job);

        assertTrue(monitor.getJob("cleanup").isPresent());
        assertFalse(monitor.getJob("missing").isPresent());
    }
}
