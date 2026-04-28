package com.cronwatch.schedule;

import com.cronwatch.config.CronWatchConfig;
import com.cronwatch.model.CronJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleLoaderTest {

    private CronWatchConfig config;
    private CronScheduleParser parser;
    private ScheduleLoader loader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = mock(CronWatchConfig.class);
        parser = new CronScheduleParser();
        loader = new ScheduleLoader(config, parser);
    }

    @Test
    void testLoadJobs_fileNotFound() {
        when(config.getProperty("cronwatch.schedule.file", "schedules/jobs.cron"))
                .thenReturn("/nonexistent/path/jobs.cron");
        List<CronJob> jobs = loader.loadJobs();
        assertTrue(jobs.isEmpty());
    }

    @Test
    void testLoadJobs_validFile() throws IOException {
        Path scheduleFile = tempDir.resolve("jobs.cron");
        Files.writeString(scheduleFile,
                "# daily backup\n" +
                "backup 0 2 * * * 300\n" +
                "cleanup */10 * * * * 45\n");
        when(config.getProperty("cronwatch.schedule.file", "schedules/jobs.cron"))
                .thenReturn(scheduleFile.toString());
        List<CronJob> jobs = loader.loadJobs();
        assertEquals(2, jobs.size());
        assertEquals("backup", jobs.get(0).getName());
        assertEquals(300L, jobs.get(0).getExpectedDurationSeconds());
    }

    @Test
    void testLoadJobs_emptyFile() throws IOException {
        Path scheduleFile = tempDir.resolve("empty.cron");
        Files.writeString(scheduleFile, "# only comments\n");
        when(config.getProperty("cronwatch.schedule.file", "schedules/jobs.cron"))
                .thenReturn(scheduleFile.toString());
        List<CronJob> jobs = loader.loadJobs();
        assertTrue(jobs.isEmpty());
    }

    @Test
    void testLoadJobs_returnsUnmodifiableList() throws IOException {
        Path scheduleFile = tempDir.resolve("jobs.cron");
        Files.writeString(scheduleFile, "report 0 8 * * * 120\n");
        when(config.getProperty("cronwatch.schedule.file", "schedules/jobs.cron"))
                .thenReturn(scheduleFile.toString());
        List<CronJob> jobs = loader.loadJobs();
        assertThrows(UnsupportedOperationException.class, () -> jobs.add(null));
    }
}
