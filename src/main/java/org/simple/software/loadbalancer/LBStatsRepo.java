package org.simple.software.loadbalancer;

import org.simple.software.stats.InMemoryStatsRepo;
import org.simple.software.stats.ResponseTimeSource;

import java.util.List;
import java.util.Set;

public class LBStatsRepo extends InMemoryStatsRepo<LBStats> implements ResponseTimeSource {

    @Override
    public List<Long> getResponseTimeRecordsByClient(int clientId) {
        return getStatsByClient(clientId).getAllRecords(LBStats.SYSTEM_RESPONSE_TIME);
    }

    @Override
    public Set<Integer> getClientIds() {
        return clientStats.keySet();
    }
}
