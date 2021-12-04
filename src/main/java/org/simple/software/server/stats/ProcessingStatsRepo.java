package org.simple.software.server.stats;

public interface ProcessingStatsRepo {

    ProcessingStats getStatsByClient(int clientId);
    ProcessingStats getAverageStats();
}
