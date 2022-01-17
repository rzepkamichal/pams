package org.simple.software.server.core;

import org.simple.software.server.ServerStats;
import org.simple.software.stats.InMemoryStatsRepo;
import org.simple.software.stats.ResponseTimeSource;

import java.util.List;
import java.util.Set;

public class ServerStatsRepo extends InMemoryStatsRepo<ServerStats> implements ResponseTimeSource {

    @Override
    public List<Long> getResponseTimeRecordsByClient(int clientId) {
        return getStatsByClient(clientId).getAllRecords(ServerStats.RESPONSE_TIME);
    }

    @Override
    public Set<Integer> getClientIds() {
        return clientStats.keySet();
    }
}
