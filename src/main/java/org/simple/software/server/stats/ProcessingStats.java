package org.simple.software.server.stats;

import java.util.List;

/**
 * Server processing time statistics.
 */
public class ProcessingStats {

    private final TimeAggregateStats docReceiveStats;
    private final TimeAggregateStats docCleaningStats;
    private final TimeAggregateStats wordCountStats;
    private final TimeAggregateStats serializationTimeStats;

    public ProcessingStats() {
        docReceiveStats = new TimeAggregateStats();
        docCleaningStats = new TimeAggregateStats();
        wordCountStats = new TimeAggregateStats();
        serializationTimeStats = new TimeAggregateStats();
    }

    private ProcessingStats(TimeAggregateStats docReceiveStats, TimeAggregateStats docCleaningStats, TimeAggregateStats wordCountStats, TimeAggregateStats serializationTimeStats) {
        this.docReceiveStats = docReceiveStats;
        this.docCleaningStats = docCleaningStats;
        this.wordCountStats = wordCountStats;
        this.serializationTimeStats = serializationTimeStats;
    }

    private void logTime(long elapsedTime, TimeAggregateStats targetStats) {
        targetStats.addRecord(elapsedTime);
    }
    
    public void logDocReceiveTime(long elapsedTime) {
        logTime(elapsedTime, docReceiveStats);
    }
    
    public void logDocCleaningTime(long elapsedTime) {
        logTime(elapsedTime, docCleaningStats);
    }

    public void logWordCountTime(long elapsedTime) {
        logTime(elapsedTime, wordCountStats);
    }

    public void logSerializationTime(long elapsedTime) {
        logTime(elapsedTime, serializationTimeStats);
    }

    public ProcessingStats join(ProcessingStats other) {
        TimeAggregateStats joinedDocReceiveStats = docReceiveStats.join(other.docReceiveStats);
        TimeAggregateStats joinedDocCleaningStats = docCleaningStats.join(other.docCleaningStats);
        TimeAggregateStats joinedWordCountStats = wordCountStats.join(other.wordCountStats);
        TimeAggregateStats joinedSerializationStats = serializationTimeStats.join(other.serializationTimeStats);

        return new ProcessingStats(
                joinedDocReceiveStats,
                joinedDocCleaningStats,
                joinedWordCountStats,
                joinedSerializationStats
        );
    }

    /**
     * @return Time spent on receiving a document from client in nanoseconds.
     */
    public double getAvgDocReceiveTime() {
        return docReceiveStats.getAvg();
    }

    /**
     *
     * @return Time spent on cleaning a document from HTML tags in nanoseconds.
     */
    public double getAvgDocCleaningTime() {
        return docCleaningStats.getAvg();
    }

    /**
     *
     * @return Time spent on counting words in nanoseconds.
     */
    public double getAvgWordCountTime() {
        return wordCountStats.getAvg();
    }

    /**
     *
     * @return Time spent on serializing the response for clients in nanoseconds.
     */
    public double getAvgSerializationTime() {
        return serializationTimeStats.getAvg();
    }

    public List<Double> getReceiveTimePercentiles() {
        return docReceiveStats.getPercentiles();
    }

    public List<Double> getDocCleaningTimePercentiles() {
        return docCleaningStats.getPercentiles();
    }

    public List<Double> getWordCountTimePercentiles() {
        return wordCountStats.getPercentiles();
    }

    public List<Double> getSerializationTimePercentiles() {
        return serializationTimeStats.getPercentiles();
    }
}
