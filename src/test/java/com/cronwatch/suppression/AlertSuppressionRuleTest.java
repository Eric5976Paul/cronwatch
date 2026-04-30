package com.cronwatch.suppression;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlertSuppressionRuleTest {

    private AlertSuppressionRule buildRule(String pattern, LocalTime start, LocalTime end, Set<DayOfWeek> days) {
        return new AlertSuppressionRule("r1", pattern, start, end, days, "maintenance");
    }

    @Test
    void matchesWhenJobNameDayAndTimeAllMatch() {
        AlertSuppressionRule rule = buildRule("backup-*",
                LocalTime.of(2, 0), LocalTime.of(4, 0),
                Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        assertTrue(rule.matches("backup-daily", DayOfWeek.SATURDAY, LocalTime.of(3, 0)));
    }

    @Test
    void doesNotMatchWhenJobNameMismatch() {
        AlertSuppressionRule rule = buildRule("backup-*",
                LocalTime.of(2, 0), LocalTime.of(4, 0),
                Set.of(DayOfWeek.SATURDAY));
        assertFalse(rule.matches("report-daily", DayOfWeek.SATURDAY, LocalTime.of(3, 0)));
    }

    @Test
    void doesNotMatchWhenDayNotInSet() {
        AlertSuppressionRule rule = buildRule("backup-*",
                LocalTime.of(2, 0), LocalTime.of(4, 0),
                Set.of(DayOfWeek.SATURDAY));
        assertFalse(rule.matches("backup-daily", DayOfWeek.MONDAY, LocalTime.of(3, 0)));
    }

    @Test
    void doesNotMatchWhenTimeOutsideWindow() {
        AlertSuppressionRule rule = buildRule("*",
                LocalTime.of(2, 0), LocalTime.of(4, 0),
                Set.of(DayOfWeek.MONDAY));
        assertFalse(rule.matches("any-job", DayOfWeek.MONDAY, LocalTime.of(5, 0)));
    }

    @Test
    void matchesOvernightWindow() {
        AlertSuppressionRule rule = buildRule("*",
                LocalTime.of(22, 0), LocalTime.of(6, 0),
                Set.of(DayOfWeek.FRIDAY));
        assertTrue(rule.matches("any-job", DayOfWeek.FRIDAY, LocalTime.of(23, 30)));
        assertTrue(rule.matches("any-job", DayOfWeek.FRIDAY, LocalTime.of(5, 0)));
        assertFalse(rule.matches("any-job", DayOfWeek.FRIDAY, LocalTime.of(12, 0)));
    }

    @Test
    void constructorRejectsNullOrBlankRuleId() {
        assertThrows(IllegalArgumentException.class, () ->
                new AlertSuppressionRule(null, "*", LocalTime.NOON, LocalTime.MIDNIGHT,
                        Set.of(DayOfWeek.MONDAY), "test"));
    }

    @Test
    void constructorRejectsEmptyActiveDays() {
        assertThrows(IllegalArgumentException.class, () ->
                new AlertSuppressionRule("r1", "*", LocalTime.NOON, LocalTime.MIDNIGHT,
                        Set.of(), "test"));
    }
}
