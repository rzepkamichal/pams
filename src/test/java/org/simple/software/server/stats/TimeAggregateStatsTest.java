package org.simple.software.server.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeAggregateStatsTest {

    @Test
    void avg_zero__when_no_records() {
        var stats = new TimeAggregateStats();
        assertEquals(0, stats.getAvg());
    }

    @Test
    void calculates_avg() {
        var stats = new TimeAggregateStats();
        stats.addRecord(30);
        stats.addRecord(30);
        stats.addRecord(40);

        double expectedResult = 100.0 / 3.0;
        assertEquals(expectedResult, stats.getAvg());
    }
}