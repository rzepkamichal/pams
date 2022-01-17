package org.simple.software.loadbalancer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.simple.software.stats.IntervalMeasurement;
import org.simple.software.stats.DefaultIntervalMeasurementService;

import static org.junit.jupiter.api.Assertions.*;

class DefaultIntervalMeasurementServiceTest {

    LBStatsRepo repo;

    DefaultIntervalMeasurementService service;


    @BeforeEach
    void setUp() {
        repo = new LBStatsRepo();
        service = new DefaultIntervalMeasurementService(repo, 500);
    }

    @Test
    void calculates_latest_measurement() {
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 30);
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 10);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 20);

        IntervalMeasurement latestMeasurement = service.measureLatestInterval();

        assertEquals(3, latestMeasurement.getSuccessCount());
        assertEquals(60 / 3.0, latestMeasurement.getAvgResponseTime());

        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 25);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 15);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 22);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 28);
        repo.getStatsByClient(3).logTime(LBStats.SYSTEM_RESPONSE_TIME, 40);

        latestMeasurement = service.measureLatestInterval();

        assertEquals(5, latestMeasurement.getSuccessCount());
        assertEquals(130 / 5.0, latestMeasurement.getAvgResponseTime());

        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 30);
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 10);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 20);

        latestMeasurement = service.measureLatestInterval();

        assertEquals(3, latestMeasurement.getSuccessCount());
        assertEquals(60 / 3.0, latestMeasurement.getAvgResponseTime());
    }

    @Test
    void returns_zero__when_no_new_measurements() {
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 30);
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 10);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 20);

        IntervalMeasurement latestMeasurement = service.measureLatestInterval();

        assertEquals(3, latestMeasurement.getSuccessCount());
        assertEquals(60 / 3.0, latestMeasurement.getAvgResponseTime());

        latestMeasurement = service.measureLatestInterval();

        assertEquals(0, latestMeasurement.getSuccessCount());
        assertEquals(0, latestMeasurement.getAvgResponseTime());
    }

    @Test
    void calculates_latest_measurement__when_only_one_client_got_new_log() {
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 30);
        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 10);
        repo.getStatsByClient(2).logTime(LBStats.SYSTEM_RESPONSE_TIME, 20);

        IntervalMeasurement latestMeasurement = service.measureLatestInterval();

        assertEquals(3, latestMeasurement.getSuccessCount());
        assertEquals(60 / 3.0, latestMeasurement.getAvgResponseTime());

        repo.getStatsByClient(1).logTime(LBStats.SYSTEM_RESPONSE_TIME, 30);

        latestMeasurement = service.measureLatestInterval();

        assertEquals(1, latestMeasurement.getSuccessCount());
        assertEquals(30, latestMeasurement.getAvgResponseTime());
    }
}