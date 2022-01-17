package org.simple.software.stats;

import java.util.Set;

public interface ProcessingStatsRepo<KEY_TYPE> {

    ProcessingStats<KEY_TYPE> getStatsByClient(int clientId);
    ProcessingStats<KEY_TYPE> getAcummulativeStats();
}
