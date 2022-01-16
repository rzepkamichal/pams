package org.simple.software.stats;

public interface ProcessingStatsRepo<KEY_TYPE> {

    ProcessingStats<KEY_TYPE> getStatsByClient(int clientId);
    ProcessingStats<KEY_TYPE> getAcummulativeStats();
}
