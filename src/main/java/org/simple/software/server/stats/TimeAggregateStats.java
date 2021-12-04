package org.simple.software.server.stats;

class TimeAggregateStats {

    private long elapsedTime = 0;
    private long count = 0;

    public void addRecord(long elapsedTime) {
        this.elapsedTime += elapsedTime;
        count++;
    }

    public double getAvg() {
        if (count == 0) {
            return 0;
        }

        return elapsedTime * 1.0/ count;
    }

    public TimeAggregateStats join(TimeAggregateStats other) {
        long joinedTime = elapsedTime + other.elapsedTime;
        long joinedCount = count + other.count;

        TimeAggregateStats result = new TimeAggregateStats();
        result.elapsedTime = joinedTime;
        result.count = joinedCount;

        return result;
    }
}
