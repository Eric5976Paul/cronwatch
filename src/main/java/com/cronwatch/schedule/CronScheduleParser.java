package com.cronwatch.schedule;

import com.cronwatch.model.CronJob;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Parses cron expressions and computes next scheduled execution times.
 */
public class CronScheduleParser {

    private static final Logger logger = Logger.getLogger(CronScheduleParser.class.getName());
    private static final int CRON_FIELD_COUNT = 5;

    /**
     * Validates a standard 5-field cron expression.
     *
     * @param expression the cron expression (minute hour dom month dow)
     * @return true if the expression is syntactically valid
     */
    public boolean isValid(String expression) {
        if (expression == null || expression.isBlank()) {
            return false;
        }
        String[] fields = expression.trim().split("\\s+");
        if (fields.length != CRON_FIELD_COUNT) {
            return false;
        }
        int[] mins  = {0, 0, 1, 1, 0};
        int[] maxes = {59, 23, 31, 12, 7};
        for (int i = 0; i < CRON_FIELD_COUNT; i++) {
            if (!isFieldValid(fields[i], mins[i], maxes[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isFieldValid(String field, int min, int max) {
        if ("*".equals(field)) return true;
        try {
            int val = Integer.parseInt(field);
            return val >= min && val <= max;
        } catch (NumberFormatException e) {
            // step values like */5 or ranges like 1-5
            if (field.startsWith("*/")) {
                int step = Integer.parseInt(field.substring(2));
                return step > 0 && step <= max;
            }
            if (field.contains("-")) {
                String[] parts = field.split("-");
                int lo = Integer.parseInt(parts[0]);
                int hi = Integer.parseInt(parts[1]);
                return lo >= min && hi <= max && lo <= hi;
            }
            return false;
        }
    }

    /**
     * Returns a human-readable description of the cron expression.
     */
    public String describe(String expression) {
        if (!isValid(expression)) {
            return "Invalid cron expression";
        }
        String[] f = expression.trim().split("\\s+");
        return String.format("At minute %s of hour %s, day-of-month %s, month %s, day-of-week %s",
                f[0], f[1], f[2], f[3], f[4]);
    }

    /**
     * Parses all cron jobs from a list of raw definition strings.
     * Expected format: "<name> <expression> <expectedDurationSeconds>"
     */
    public List<ParsedSchedule> parseJobDefinitions(List<String> definitions) {
        List<ParsedSchedule> result = new ArrayList<>();
        for (String def : definitions) {
            String trimmed = def.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            String[] parts = trimmed.split("\\s+", 7);
            if (parts.length < 7) {
                logger.warning("Skipping malformed job definition: " + def);
                continue;
            }
            String name = parts[0];
            String expr = String.join(" ", parts[1], parts[2], parts[3], parts[4], parts[5]);
            long expectedSeconds = Long.parseLong(parts[6]);
            if (!isValid(expr)) {
                logger.warning("Invalid cron expression for job '" + name + "': " + expr);
                continue;
            }
            result.add(new ParsedSchedule(name, expr, expectedSeconds));
        }
        return result;
    }
}
