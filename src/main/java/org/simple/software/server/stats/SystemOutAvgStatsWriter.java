package org.simple.software.server.stats;

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
        System.out.println("Avg request receive time [ms]: " + stats.getAvgDocReceiveTime());
        System.out.println("Avg tag removal time [ms]: " + stats.getAvgDocCleaningTime());
        System.out.println("Avg word count time [ms]: " + stats.getAvgWordCountTime());
        System.out.println("Avg result serialization time [ms]: " + stats.getAvgSerializationTime());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println();
    }
}
