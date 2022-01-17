package org.simple.software.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStatsRepo<KEY_TYPE> implements ProcessingStatsRepo<KEY_TYPE> {

    protected final Map<Integer, ProcessingStats<KEY_TYPE>> clientStats = new ConcurrentHashMap<>();

    @Override
    public ProcessingStats<KEY_TYPE> getStatsByClient(int clientId) {
        if (clientStats.containsKey(clientId)) {
            return clientStats.get(clientId);
        }

        ProcessingStats<KEY_TYPE> newClientStats = new ProcessingStats<>();
        clientStats.put(clientId, newClientStats);
        return newClientStats;
    }

    @Override
    public ProcessingStats<KEY_TYPE> getAcummulativeStats() {
        return clientStats.values()
                .stream()
                .reduce(new ProcessingStats<>(), ProcessingStats::join);
    }

}
