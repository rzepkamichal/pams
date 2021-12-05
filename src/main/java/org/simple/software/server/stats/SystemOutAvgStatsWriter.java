package org.simple.software.server.stats;

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
        writeStats(statsRepo.getAverageStats());
    }

    private void writeStats(ProcessingStats stats) {
        System.out.format(Locale.US, "Avg request receive time [ms]: %.4f\n", stats.getAvgDocReceiveTime());
        System.out.format(Locale.US, "Avg tag removal time [ms]: %.4f\n", stats.getAvgDocCleaningTime());
        System.out.format(Locale.US, "Avg word count time [ms]: %.4f\n", stats.getAvgWordCountTime());
        System.out.format(Locale.US, "Avg result serialization time [ms]: %.4f\n", stats.getAvgSerializationTime());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println();
    }
}
