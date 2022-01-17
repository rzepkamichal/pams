package org.simple.software.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simple.software.server.ServerStats;

import static org.junit.jupiter.api.Assertions.*;
import static org.simple.software.server.ServerStats.RECEIVE_TIME;
import static org.simple.software.server.ServerStats.WORD_COUNT_TIME;

class InMemoryLBStatsRepoTest {

    InMemoryStatsRepo<ServerStats> repo;

    @BeforeEach
    void setup() {
        repo = new InMemoryStatsRepo<>();
    }

    @Test
    void calculates_aggregate_stats() {
        repo.getStatsByClient(1).logTime(WORD_COUNT_TIME,10);
        repo.getStatsByClient(1).logTime(WORD_COUNT_TIME,20);
        repo.getStatsByClient(2).logTime(WORD_COUNT_TIME,30);
        repo.getStatsByClient(3).logTime(WORD_COUNT_TIME,40);

        repo.getStatsByClient(1).logTime(RECEIVE_TIME,20);
        repo.getStatsByClient(2).logTime(RECEIVE_TIME,10);
        repo.getStatsByClient(2).logTime(RECEIVE_TIME,10);
        repo.getStatsByClient(2).logTime(RECEIVE_TIME,10);
        repo.getStatsByClient(3).logTime(RECEIVE_TIME,70);

        double expectedAvgWordCountTime = 100.0 / 4.0;
        double expectedAvgDocReceiveTime = 120.0 / 5.0;

        double avgWordCountTime = repo.getAcummulativeStats().getAvg(WORD_COUNT_TIME);
        double avgDocReceiveTime = repo.getAcummulativeStats().getAvg(RECEIVE_TIME);

        assertEquals(expectedAvgWordCountTime, avgWordCountTime);
        assertEquals(expectedAvgDocReceiveTime, avgDocReceiveTime);
    }
}