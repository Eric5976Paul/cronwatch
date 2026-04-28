package com.cronwatch.schedule;

import com.cronwatch.config.CronWatchConfig;
import com.cronwatch.model.CronJob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Loads cron job schedule definitions from the configured schedule file
 * and converts them into {@link CronJob} instances.
 */
public class ScheduleLoader {

    private static final Logger logger = Logger.getLogger(ScheduleLoader.class.getName());

    private final CronWatchConfig config;
    private final CronScheduleParser parser;

    public ScheduleLoader(CronWatchConfig config, CronScheduleParser parser) {
        this.config = config;
        this.parser = parser;
    }

    /**
     * Reads the schedule file and returns a list of {@link CronJob} objects.
     *
     * @return unmodifiable list of loaded cron jobs; empty if file is missing or unreadable
     */
    public List<CronJob> loadJobs() {
        String scheduleFile = config.getProperty("cronwatch.schedule.file", "schedules/jobs.cron");
        Path path = Paths.get(scheduleFile);
        if (!Files.exists(path)) {
            logger.warning("Schedule file not found: " + path.toAbsolutePath());
            return Collections.emptyList();
        }
        try {
            List<String> lines = Files.readAllLines(path);
            List<ParsedSchedule> parsed = parser.parseJobDefinitions(lines);
            List<CronJob> jobs = new ArrayList<>();
            for (ParsedSchedule ps : parsed) {
                CronJob job = new CronJob(
                        ps.getJobName(),
                        ps.getCronExpression(),
                        ps.getExpectedDurationSeconds()
                );
                jobs.add(job);
                logger.fine("Loaded job: " + job);
            }
            logger.info("Loaded " + jobs.size() + " cron job(s) from " + scheduleFile);
            return Collections.unmodifiableList(jobs);
        } catch (IOException e) {
            logger.severe("Failed to read schedule file '" + scheduleFile + "': " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
