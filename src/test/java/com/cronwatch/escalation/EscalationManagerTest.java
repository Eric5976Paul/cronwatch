package com.cronwatch.escalation;

import com.cronwatch.alert.AlertNotifier;
import com.cronwatch.alert.AlertRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EscalationManagerTest {

    private List<AlertRecord> sent;
    private AlertNotifier notifier;
    private EscalationManager manager;

    @BeforeEach
    void setUp() {
        sent = new ArrayList<>();
        notifier = record -> sent.add(record);
        manager = new EscalationManager(notifier);
    }

    private EscalationPolicy buildPolicy(String jobName) {
        List<EscalationPolicy.Tier> tiers = List.of(
            new EscalationPolicy.Tier(1, Duration.ZERO, EscalationPolicy.Level.WARNING),
            new EscalationPolicy.Tier(3, Duration.ZERO, EscalationPolicy.Level.CRITICAL),
            new EscalationPolicy.Tier(5, Duration.ZERO, EscalationPolicy.Level.EMERGENCY)
        );
        return new EscalationPolicy(jobName, tiers);
    }

    @Test
    void firstAlertTriggersWarning() {
        manager.registerPolicy(buildPolicy("backup-job"));
        manager.recordAlert("backup-job", "exceeded duration");
        assertEquals(1, sent.size());
        assertTrue(sent.get(0).getMessage().startsWith("WARNING"));
    }

    @Test
    void thirdAlertEscalatesToCritical() {
        manager.registerPolicy(buildPolicy("backup-job"));
        manager.recordAlert("backup-job", "exceeded duration");
        manager.recordAlert("backup-job", "exceeded duration");
        manager.recordAlert("backup-job", "exceeded duration");
        assertEquals(2, sent.size()); // WARNING then CRITICAL
        assertTrue(sent.get(1).getMessage().startsWith("CRITICAL"));
    }

    @Test
    void noAlertSentWhenNoPolicyRegistered() {
        manager.recordAlert("unknown-job", "exceeded duration");
        assertTrue(sent.isEmpty());
    }

    @Test
    void clearStateResetsOccurrenceCount() {
        manager.registerPolicy(buildPolicy("report-job"));
        manager.recordAlert("report-job", "slow");
        manager.recordAlert("report-job", "slow");
        manager.clearState("report-job");
        assertEquals(0, manager.getOccurrenceCount("report-job"));
    }

    @Test
    void constructorRejectsNullNotifier() {
        assertThrows(IllegalArgumentException.class, () -> new EscalationManager(null));
    }

    @Test
    void policyRejectsBlankJobName() {
        assertThrows(IllegalArgumentException.class,
            () -> new EscalationPolicy(" ", List.of(
                new EscalationPolicy.Tier(1, Duration.ZERO, EscalationPolicy.Level.WARNING)
            )));
    }

    @Test
    void tierRejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class,
            () -> new EscalationPolicy.Tier(1, Duration.ofSeconds(-1), EscalationPolicy.Level.WARNING));
    }
}
