package org.simple.software.stats;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

    @Test
    void calculates_percentiles() {
        var stats = new TimeAggregateStats();

        for (int i = 0; i < 100; i++) {
            stats.addRecord(100 - i);
        }

        var percentiles = stats.get100Percentiles();
        assertEquals(1.0, percentiles.get(0));
        assertEquals(25.0, percentiles.get(24));
        assertEquals(50.0, percentiles.get(49));
        assertEquals(75.0, percentiles.get(74));
        assertEquals(100.0, percentiles.get(99));
    }
}