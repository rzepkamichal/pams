package org.simple.software.stats;

import org.simple.software.server.ServerStats;

import java.util.List;
import java.util.Locale;

import static org.simple.software.server.ServerStats.RECEIVE_TIME;
import static org.simple.software.server.ServerStats.RESPONSE_SERIALIZATION_TIME;
import static org.simple.software.server.ServerStats.TAG_REMOVAL_TIME;
import static org.simple.software.server.ServerStats.WORD_COUNT_TIME;

public class SystemOutAvgStatsWriter implements StatsWriter {

    private final ProcessingStatsRepo<ServerStats> statsRepo;

    public SystemOutAvgStatsWriter(ProcessingStatsRepo<ServerStats> statsRepo) {
        this.statsRepo = statsRepo;
    }

    @Override
    public void writeForClient(int clientId) {
        System.out.println("--------------------------- Client statistics -----------------------------");
        System.out.println("clientId = " + clientId);
        writeStats(statsRepo.getStatsByClient(clientId));
    }

    @Override
    public void writeTotal() {
        System.out.println("------------------------ Total server statistics --------------------------");
        writeStats(statsRepo.getAcummulativeStats());
    }

    private void writeStats(ProcessingStats<ServerStats> stats) {
        System.out.format(Locale.US, "Avg request receive time [ms]: %.4f\n", getMillis(stats.getAvg(RECEIVE_TIME)));
        System.out.format(Locale.US, "Avg tag removal time [ms]: %.4f\n", getMillis(stats.getAvg(TAG_REMOVAL_TIME)));
        System.out.format(Locale.US, "Avg word count time [ms]: %.4f\n", getMillis(stats.getAvg(WORD_COUNT_TIME)));
        System.out.format(Locale.US, "Avg result serialization time [ms]: %.4f\n", getMillis(stats.getAvg(RESPONSE_SERIALIZATION_TIME)));
        System.out.println();
        System.out.println("Percentiles request receive time [ms]");
        writePercentiles(stats.get100Percentiles(RECEIVE_TIME));
        System.out.println();
        System.out.println("Percentiles tag removal time [ms]");
        writePercentiles(stats.get100Percentiles(TAG_REMOVAL_TIME));
        System.out.println();
        System.out.println("Percentiles word count time [ms]");
        writePercentiles(stats.get100Percentiles(WORD_COUNT_TIME));
        System.out.println();
        System.out.println("Percentiles result serialization time [ms]");
        writePercentiles(stats.get100Percentiles(RESPONSE_SERIALIZATION_TIME));
        System.out.println("--------------------------------------------------------------------------");
        System.out.println();
    }

    private void writePercentiles(List<Double> percentiles) {
        if (percentiles.size() != 100) {
            throw new IllegalStateException("Percentiles expected to be list of size 100");
        }

        for (int i = 0; i < 100; i++) {
            System.out.format(Locale.US, "%d,%.4f\n", i, getMillis(percentiles.get(i)));
        }
    }

    private double getMillis(double duration) {
        return duration * TimedRunner.PRECISION * 1000.0;
    }
}
