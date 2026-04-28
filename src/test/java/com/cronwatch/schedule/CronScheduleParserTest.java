package com.cronwatch.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CronScheduleParserTest {

    private CronScheduleParser parser;

    @BeforeEach
    void setUp() {
        parser = new CronScheduleParser();
    }

    @Test
    void testValidExpression() {
        assertTrue(parser.isValid("0 2 * * *"));
        assertTrue(parser.isValid("*/15 * * * *"));
        assertTrue(parser.isValid("30 6 1-15 * 1"));
    }

    @Test
    void testInvalidExpression_wrongFieldCount() {
        assertFalse(parser.isValid("0 2 * *"));
        assertFalse(parser.isValid("0 2 * * * *"));
    }

    @Test
    void testInvalidExpression_outOfRange() {
        assertFalse(parser.isValid("60 2 * * *"));  // minute > 59
        assertFalse(parser.isValid("0 25 * * *")); // hour > 23
    }

    @Test
    void testNullAndBlankExpression() {
        assertFalse(parser.isValid(null));
        assertFalse(parser.isValid(""));
        assertFalse(parser.isValid("   "));
    }

    @Test
    void testDescribe() {
        String desc = parser.describe("0 2 * * *");
        assertTrue(desc.contains("minute 0"));
        assertTrue(desc.contains("hour 2"));
    }

    @Test
    void testDescribeInvalid() {
        assertEquals("Invalid cron expression", parser.describe("bad expression"));
    }

    @Test
    void testParseJobDefinitions_valid() {
        List<String> defs = Arrays.asList(
                "# comment line",
                "backup 0 2 * * * 300",
                "cleanup */5 * * * * 60"
        );
        List<ParsedSchedule> schedules = parser.parseJobDefinitions(defs);
        assertEquals(2, schedules.size());
        assertEquals("backup", schedules.get(0).getJobName());
        assertEquals(300L, schedules.get(0).getExpectedDurationSeconds());
        assertEquals("cleanup", schedules.get(1).getJobName());
    }

    @Test
    void testParseJobDefinitions_malformedSkipped() {
        List<String> defs = Arrays.asList(
                "onlytwofields 0",
                "valid 0 3 * * * 120"
        );
        List<ParsedSchedule> schedules = parser.parseJobDefinitions(defs);
        assertEquals(1, schedules.size());
        assertEquals("valid", schedules.get(0).getJobName());
    }

    @Test
    void testParseJobDefinitions_emptyInput() {
        List<ParsedSchedule> schedules = parser.parseJobDefinitions(List.of());
        assertTrue(schedules.isEmpty());
    }
}
