package com.cronwatch;

import com.cronwatch.alert.AlertNotifier;
import com.cronwatch.config.CronWatchConfig;
import com.cronwatch.monitor.DurationAlertHandler;
import com.cronwatch.monitor.JobMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for the CronWatch daemon.
 *
 * <p>Initializes configuration, wires together the monitoring pipeline,
 * and starts the background polling loop that checks for overdue cron jobs.
 *
 * <p>Shutdown is handled gracefully via a JVM shutdown hook so that
 * in-flight checks are allowed to complete before the process exits.
 */
public class CronWatchDaemon {

    private static final Logger log = LoggerFactory.getLogger(CronWatchDaemon.class);

    private final CronWatchConfig config;
    private final JobMonitor jobMonitor;
    private final AlertNotifier alertNotifier;
    private final DurationAlertHandler alertHandler;
    private final ScheduledExecutorService scheduler;

    public CronWatchDaemon(CronWatchConfig config) {
        this.config = config;
        this.alertNotifier = new AlertNotifier(config);
        this.alertHandler = new DurationAlertHandler(alertNotifier);
        this.jobMonitor = new JobMonitor(config, alertHandler);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "cronwatch-monitor");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the daemon's polling loop.
     *
     * <p>The monitor runs at the interval defined by {@code monitor.poll.interval.seconds}
     * in {@code cronwatch.properties} (default: 60 seconds).
     */
    public void start() {
        int pollIntervalSeconds = config.getPollIntervalSeconds();
        log.info("Starting CronWatch daemon — poll interval: {}s", pollIntervalSeconds);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                log.debug("Running scheduled job monitor check");
                jobMonitor.checkAll();
            } catch (Exception e) {
                log.error("Unexpected error during monitor check", e);
            }
        }, 0, pollIntervalSeconds, TimeUnit.SECONDS);

        registerShutdownHook();
        log.info("CronWatch daemon started successfully");
    }

    /**
     * Stops the daemon, waiting up to 10 seconds for pending work to finish.
     */
    public void stop() {
        log.info("Shutting down CronWatch daemon…");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Scheduler did not terminate cleanly; forcing shutdown");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
        log.info("CronWatch daemon stopped");
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutdown signal received");
            stop();
        }, "cronwatch-shutdown"));
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        log.info("Initialising CronWatch…");
        try {
            CronWatchConfig config = CronWatchConfig.load();
            CronWatchDaemon daemon = new CronWatchDaemon(config);
            daemon.start();

            // Keep the main thread alive; actual work happens on the daemon thread.
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Main thread interrupted — exiting");
        } catch (Exception e) {
            log.error("Fatal error during startup", e);
            System.exit(1);
        }
    }
}
