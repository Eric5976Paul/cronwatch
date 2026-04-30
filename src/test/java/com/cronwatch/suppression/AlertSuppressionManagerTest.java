package com.cronwatch.suppression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlertSuppressionManagerTest {

    private AlertSuppressionManager manager;

    @BeforeEach
    void setUp() {
        manager = new AlertSuppressionManager();
    }

    private AlertSuppressionRule maintenanceRule() {
        return new AlertSuppressionRule("maint-1", "backup-*",
                LocalTime.of(1, 0), LocalTime.of(5, 0),
                Set.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), "weekend maintenance");
    }

    @Test
    void suppressesAlertWhenRuleMatches() {
        manager.addRule(maintenanceRule());
        LocalDateTime saturday3am = LocalDateTime.of(2024, 6, 1, 3, 0); // Saturday
        assertTrue(manager.isSuppressed("backup-daily", saturday3am));
    }

    @Test
    void doesNotSuppressWhenNoRuleMatches() {
        manager.addRule(maintenanceRule());
        LocalDateTime monday3am = LocalDateTime.of(2024, 6, 3, 3, 0); // Monday
        assertFalse(manager.isSuppressed("backup-daily", monday3am));
    }

    @Test
    void doesNotSuppressUnrelatedJob() {
        manager.addRule(maintenanceRule());
        LocalDateTime saturday3am = LocalDateTime.of(2024, 6, 1, 3, 0);
        assertFalse(manager.isSuppressed("report-weekly", saturday3am));
    }

    @Test
    void removeRuleStopsSuppressionForMatchingJob() {
        manager.addRule(maintenanceRule());
        LocalDateTime saturday3am = LocalDateTime.of(2024, 6, 1, 3, 0);
        assertTrue(manager.isSuppressed("backup-daily", saturday3am));
        manager.removeRule("maint-1");
        assertFalse(manager.isSuppressed("backup-daily", saturday3am));
    }

    @Test
    void removeNonExistentRuleReturnsFalse() {
        assertFalse(manager.removeRule("nonexistent"));
    }

    @Test
    void clearRulesRemovesAll() {
        manager.addRule(maintenanceRule());
        manager.clearRules();
        assertTrue(manager.getRules().isEmpty());
    }

    @Test
    void getRulesReturnsUnmodifiableView() {
        manager.addRule(maintenanceRule());
        assertThrows(UnsupportedOperationException.class, () -> manager.getRules().clear());
    }

    @Test
    void addNullRuleThrows() {
        assertThrows(IllegalArgumentException.class, () -> manager.addRule(null));
    }
}
