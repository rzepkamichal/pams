package org.simple.software.server.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsRepoImplTest {

    StatsRepoImpl repo;

    @BeforeEach
    void setup() {
        repo = new StatsRepoImpl();
    }

    @Test
    void calculates_aggregate_stats() {
        repo.getStatsByClient(1).logWordCountTime(10);
        repo.getStatsByClient(1).logWordCountTime(20);
        repo.getStatsByClient(2).logWordCountTime(30);
        repo.getStatsByClient(3).logWordCountTime(40);

        repo.getStatsByClient(1).logDocReceiveTime(20);
        repo.getStatsByClient(2).logDocReceiveTime(10);
        repo.getStatsByClient(2).logDocReceiveTime(10);
        repo.getStatsByClient(2).logDocReceiveTime(10);
        repo.getStatsByClient(3).logDocReceiveTime(70);

        double expectedAvgWordCountTime = 100.0 / 4.0;
        double expectedAvgDocReceiveTime = 120.0 / 5.0;

        double avgWordCountTime = repo.getAverageStats().getAvgWordCountTime();
        double avgDocReceiveTime = repo.getAverageStats().getAvgDocReceiveTime();

        assertEquals(expectedAvgWordCountTime, avgWordCountTime);
        assertEquals(expectedAvgDocReceiveTime, avgDocReceiveTime);
    }
}