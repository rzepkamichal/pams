package org.simple.software.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server processing time statistics.
 */
public class ProcessingStats<KEY_TYPE> {

    private final Map<KEY_TYPE, TimeAggregateStats> stats = new ConcurrentHashMap<>();

    public ProcessingStats() {
    }

    private ProcessingStats(Map<KEY_TYPE, TimeAggregateStats> other) {
        stats.putAll(other);
    }

    public void logTime(KEY_TYPE logName, long elapsedTime) {
        TimeAggregateStats targetStats = findOrCreateStats(logName);
        targetStats.addRecord(elapsedTime);
    }

    private TimeAggregateStats findOrCreateStats(KEY_TYPE logName) {
        TimeAggregateStats targetStats = stats.get(logName);

        if (targetStats == null) {
            targetStats = new TimeAggregateStats();
            stats.put(logName, targetStats);
        }

        return targetStats;
    }

    public ProcessingStats<KEY_TYPE> join(ProcessingStats<KEY_TYPE> other) {
        final Map<KEY_TYPE, TimeAggregateStats> joinedStats = new HashMap<>();

        other.stats.keySet()
                .forEach(otherKey ->
                        joinedStats.put(
                                otherKey,
                                findOrCreateStats(otherKey).join(other.stats.get(otherKey))
                        )
                );

        return new ProcessingStats<>(joinedStats);
    }


    public double getAvg(KEY_TYPE logName) {
        return findOrCreateStats(logName).getAvg();
    }

    public List<Double> get100Percentiles(KEY_TYPE logName) {
        return findOrCreateStats(logName).get100Percentiles();
    }

    public List<Long> getAllRecords(KEY_TYPE logName) {
        return findOrCreateStats(logName).getAllRecords();
    }
}

