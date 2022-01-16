package org.simple.software.stats;

public interface ProcessingStatsRepo {

    ProcessingStats getStatsByClient(int clientId);
    ProcessingStats getAcummulativeStats();
}
