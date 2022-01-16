package org.simple.software.stats;

import java.util.List;
import java.util.Locale;

public class SystemOutAvgStatsWriter implements StatsWriter {

    private final ProcessingStatsRepo statsRepo;

    public SystemOutAvgStatsWriter(ProcessingStatsRepo statsRepo) {
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

    private void writeStats(ProcessingStats stats) {
        System.out.format(Locale.US, "Avg request receive time [ms]: %.4f\n", getMillis(stats.getAvgDocReceiveTime()));
        System.out.format(Locale.US, "Avg tag removal time [ms]: %.4f\n", getMillis(stats.getAvgDocCleaningTime()));
        System.out.format(Locale.US, "Avg word count time [ms]: %.4f\n", getMillis(stats.getAvgWordCountTime()));
        System.out.format(Locale.US, "Avg result serialization time [ms]: %.4f\n", getMillis(stats.getAvgSerializationTime()));
        System.out.println();
        System.out.println("Percentiles request receive time [ms]");
        writePercentiles(stats.getReceiveTimePercentiles());
        System.out.println();
        System.out.println("Percentiles tag removal time [ms]");
        writePercentiles(stats.getDocCleaningTimePercentiles());
        System.out.println();
        System.out.println("Percentiles word count time [ms]");
        writePercentiles(stats.getWordCountTimePercentiles());
        System.out.println();
        System.out.println("Percentiles result serialization time [ms]");
        writePercentiles(stats.getSerializationTimePercentiles());
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
