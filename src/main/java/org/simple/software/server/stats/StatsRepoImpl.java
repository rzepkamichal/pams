package org.simple.software.server.stats;

import org.simple.software.server.stats.ProcessingStats;
import org.simple.software.server.stats.ProcessingStatsRepo;

import java.util.HashMap;
import java.util.Map;

public class StatsRepoImpl implements ProcessingStatsRepo {

    private final Map<Integer, ProcessingStats> clientStats = new HashMap<>();

    @Override
    public ProcessingStats getStatsByClient(int clientId) {
        if (clientStats.containsKey(clientId)) {
            return clientStats.get(clientId);
        }

        ProcessingStats newClientStats = new ProcessingStats();
        clientStats.put(clientId, newClientStats);
        return newClientStats;
    }

    @Override
    public ProcessingStats getAverageStats() {
        return clientStats.values()
                .stream()
                .reduce(new ProcessingStats(), ProcessingStats::join);
    }
}
