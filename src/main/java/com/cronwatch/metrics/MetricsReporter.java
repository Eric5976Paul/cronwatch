package com.cronwatch.metrics;

import java.io.PrintStream;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Formats and outputs collected job metrics to a given PrintStream.
 */
public class MetricsReporter {

    private final JobMetricsCollector collector;
    private final PrintStream output;

    public MetricsReporter(JobMetricsCollector collector, PrintStream output) {
        if (collector == null || output == null) {
            throw new IllegalArgumentException("Collector and output must not be null");
        }
        this.collector = collector;
        this.output = output;
    }

    public void printReport() {
        Map<String, JobMetricsSummary> summaries = collector.getAllSummaries();
        output.println("=== CronWatch Metrics Report — " + Instant.now() + " ===");
        if (summaries.isEmpty()) {
            output.println("No metrics collected yet.");
            return;
        }
        List<JobMetricsSummary> sorted = summaries.values().stream()
                .sorted(Comparator.comparing(JobMetricsSummary::getJobName))
                .toList();
        output.printf("%-30s %6s %8s %8s %8s %8s%n",
                "Job", "Runs", "Success%", "Avg(ms)", "Min(ms)", "Max(ms)");
        output.println("-".repeat(76));
        for (JobMetricsSummary s : sorted) {
            output.printf("%-30s %6d %7.1f%% %8.1f %8d %8d%n",
                    s.getJobName(),
                    s.getTotalRuns(),
                    s.getSuccessRate() * 100,
                    s.getAverageDurationMillis(),
                    s.getMinDurationMillis(),
                    s.getMaxDurationMillis());
        }
        output.println("=== End of Report ===");
    }
}
