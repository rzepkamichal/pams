package org.simple.software.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class TimeAggregateStats {

    private final List<Long> timeStats = new LinkedList<>();
    private long sum = 0;

    public TimeAggregateStats() {
    }

    private TimeAggregateStats(List<Long> timeStats) {
        this.timeStats.addAll(timeStats);
        sum = timeStats.stream().reduce(0L, Long::sum);
    }

    public void addRecord(long duration) {
        timeStats.add(duration);
        sum += duration;
    }

    public double getAvg() {
        if (timeStats.isEmpty()) {
            return 0;
        }

        return sum * 1.0 / timeStats.size();
    }

    public TimeAggregateStats join(TimeAggregateStats other) {
        List<Long> joinedStats = new LinkedList<>();
        joinedStats.addAll(timeStats);
        joinedStats.addAll(other.timeStats);

        TimeAggregateStats result = new TimeAggregateStats(joinedStats);

        return result;
    }

    public List<Double> get100Percentiles() {
        // use array list to access values faster
        List<Long> stats = new ArrayList<>(timeStats);
        Collections.sort(stats);

        List<Double> percentiles = new ArrayList<>(100);

        double percentile;
        for (int p = 1; p <= 100; p++) {
            int index = (int) Math.ceil(stats.size() * p / 100.0) - 1;
            percentile = stats.get(index);
            percentiles.add(percentile);
        }

        return percentiles;
    }

    public List<Double> getAllRecords() {
        return timeStats.stream()
                .map(Long::doubleValue)
                .collect(Collectors.toList());
    }


}
