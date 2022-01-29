package org.simple.software.server.core;

import org.simple.software.infrastructure.CSV;
import org.simple.software.server.ServerStats;
import org.simple.software.stats.IntervalMeasurement;
import org.simple.software.stats.IntervalMeasurementService;
import org.simple.software.stats.ProcessingStatsRepo;
import org.simple.software.stats.StatsWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static org.simple.software.server.ServerStats.INTERARRIVAL_TIME;
import static org.simple.software.server.ServerStats.RECEIVE_TIME;
import static org.simple.software.server.ServerStats.RESPONSE_SERIALIZATION_TIME;
import static org.simple.software.server.ServerStats.RESPONSE_TIME;
import static org.simple.software.server.ServerStats.TAG_REMOVAL_TIME;
import static org.simple.software.server.ServerStats.WORD_COUNT_TIME;
import static org.simple.software.stats.TimeUtils.doubleFreqPerNanoToFreqPerSec;
import static org.simple.software.stats.TimeUtils.doubleNanoTimeToMs;
import static org.simple.software.stats.TimeUtils.doubleNanoTimeToSec;
import static org.simple.software.stats.TimeUtils.longNanoTimeToSec;

public class ServerStatsCSVWriter implements StatsWriter {

    private final Logger log = Logger.getLogger(getClass().getName());

    public static final String FILE_AVG_STATS = "total.csv";
    public static final String FILE_PERCENTILES = "percentiles.csv";
    public static final String FILE_INTERVALS = "intervals.csv";

    private static final String ATTR_RECEIVE_TIME = "Receive time [ms]";
    private static final String ATTR_TAG_REMOVAL_TIME = "Tag removal time [ms]";
    private static final String ATTR_WORD_COUNT_TIME = "Word count time [ms]";
    private static final String ATTR_SERIALIZATION_TIME = "Response serialization time [ms]";

    private static final String ATTRIB_TIME = "Time [s]";
    private static final String ATTRIB_TPUT = "Throughput [1/s]";
    private static final String ATTR_RESPONSE_TIME = "Response time [ms]";
    private static final String ATTRIB_INTERARRIVAL_TIME = "Interarrival time [ms]";

    private final ProcessingStatsRepo<ServerStats> statsRepo;
    private final IntervalMeasurementService measurementService;

    private String logsDirPath = "";

    public ServerStatsCSVWriter(ProcessingStatsRepo<ServerStats> statsRepo, IntervalMeasurementService measurementService) {
        this.statsRepo = statsRepo;
        this.measurementService = measurementService;
    }

    public ServerStatsCSVWriter(String logsDirPath, ProcessingStatsRepo<ServerStats> statsRepo, IntervalMeasurementService measurementService) {
        this.statsRepo = statsRepo;
        this.logsDirPath = logsDirPath;
        this.measurementService = measurementService;
    }

    @Override
    public void writeForClient(int clientId) {
        System.out.println("for client " + clientId);
    }

    @Override
    public void writeTotal() {
        log.info("Writing stats");
        writeTotalStats();
        writePercentiles();
        writeIntervals();
        log.info("Writing stats: done");
    }

    private void writeTotalStats() {
        double avgReceiveTime = statsRepo.getAcummulativeStats().getAvg(RECEIVE_TIME);
        double avgTagRemovalTime = statsRepo.getAcummulativeStats().getAvg(TAG_REMOVAL_TIME);
        double avgWordCountTime = statsRepo.getAcummulativeStats().getAvg(WORD_COUNT_TIME);
        double avgSerializationTime = statsRepo.getAcummulativeStats().getAvg(RESPONSE_SERIALIZATION_TIME);
        double avgResponseTime = statsRepo.getAcummulativeStats().getAvg(RESPONSE_TIME);
        double interarrivalTime = statsRepo.getAcummulativeStats().getAvg(INTERARRIVAL_TIME);
        double totalTime = getTotalTime();
        double totalTput = getTotalTput(totalTime);

        Record record = new Record(avgReceiveTime, avgTagRemovalTime, avgWordCountTime,
                avgSerializationTime, avgResponseTime, interarrivalTime, totalTime, totalTput);

        String header = ATTR_RECEIVE_TIME + CSV.SEPARATOR + ATTR_TAG_REMOVAL_TIME + CSV.SEPARATOR
                + ATTR_WORD_COUNT_TIME + CSV.SEPARATOR + ATTR_SERIALIZATION_TIME + CSV.SEPARATOR
                + ATTR_RESPONSE_TIME + CSV.SEPARATOR + ATTRIB_TIME + CSV.SEPARATOR
                + ATTRIB_TPUT + CSV.SEPARATOR + ATTRIB_INTERARRIVAL_TIME;
        CSV.writeToFile(logsDirPath, FILE_AVG_STATS, header, List.of(record), this::processStatRecordToCSVLineWithTput);
    }

    private double getTotalTime() {
        final long measurementStartTime = measurementService.getStartTimestamp();
        final long lastMeasurementTime = measurementService.getLastMeasurementTimestamp();
        return lastMeasurementTime - measurementStartTime;
    }

    private double getTotalTput(double totalTime) {
        final int successCount = statsRepo.getAcummulativeStats().getAllRecords(RESPONSE_TIME).size();
        return 1.0 * successCount / totalTime;
    }

    private void writePercentiles() {
        List<Double> receiveTimePercentiles = statsRepo.getAcummulativeStats().get100Percentiles(RECEIVE_TIME);
        List<Double> tagRemovalPercentiles = statsRepo.getAcummulativeStats().get100Percentiles(TAG_REMOVAL_TIME);
        List<Double> wordCountPercentiles = statsRepo.getAcummulativeStats().get100Percentiles(WORD_COUNT_TIME);
        List<Double> serializationTimePercentiles = statsRepo.getAcummulativeStats().get100Percentiles(RESPONSE_SERIALIZATION_TIME);
        List<Double> responseTimePercentiles = statsRepo.getAcummulativeStats().get100Percentiles(RESPONSE_TIME);

        List<Record> records = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            Record record = new Record(receiveTimePercentiles.get(i), tagRemovalPercentiles.get(i),
                    wordCountPercentiles.get(i), serializationTimePercentiles.get(i),
                    responseTimePercentiles.get(i), -1L, -1L, -1L);
            records.add(record);
        }

        String header = ATTR_RECEIVE_TIME + CSV.SEPARATOR + ATTR_TAG_REMOVAL_TIME + CSV.SEPARATOR
                + ATTR_WORD_COUNT_TIME + CSV.SEPARATOR + ATTR_SERIALIZATION_TIME + CSV.SEPARATOR + ATTR_RESPONSE_TIME;

        CSV.writeToFile(logsDirPath, FILE_PERCENTILES, header, records, this::processStatRecordToCSVLineWithoutTput);
    }

    private void writeIntervals() {
        List<IntervalMeasurement> measurements = measurementService.getMeasurements();
        final long measurementStartTime = measurementService.getStartTimestamp();
        String header = ATTRIB_TIME + CSV.SEPARATOR + ATTRIB_TPUT + CSV.SEPARATOR + ATTR_RESPONSE_TIME;
        CSV.writeToFile(logsDirPath, FILE_INTERVALS, header, measurements,
                measurement -> intervalMeasurementToCSVLine(measurement, measurementStartTime));
    }

    private String processStatRecordToCSVLineWithoutTput(Record record) {
        double receiveTime = doubleNanoTimeToMs(record.getReceiveTime());
        double tagRemovalTime = doubleNanoTimeToMs(record.getTagRemovalTime());
        double wordCountTime = doubleNanoTimeToMs(record.getWordCountTime());
        double serializationTime = doubleNanoTimeToMs(record.getSerializationTime());
        double responseTime = doubleNanoTimeToMs(record.getResponseTime());

        return String.format(Locale.US, "%.2f%s%.2f%s%.2f%s%.2f%s%.2f",
                receiveTime, CSV.SEPARATOR,
                tagRemovalTime, CSV.SEPARATOR,
                wordCountTime, CSV.SEPARATOR,
                serializationTime, CSV.SEPARATOR,
                responseTime
        );
    }

    private String processStatRecordToCSVLineWithTput(Record record) {
        double receiveTime = doubleNanoTimeToMs(record.getReceiveTime());
        double tagRemovalTime = doubleNanoTimeToMs(record.getTagRemovalTime());
        double wordCountTime = doubleNanoTimeToMs(record.getWordCountTime());
        double serializationTime = doubleNanoTimeToMs(record.getSerializationTime());
        double responseTime = doubleNanoTimeToMs(record.getResponseTime());
        double interarrivalTime = doubleNanoTimeToMs(record.getInterarrivalTime());
        double totalTime = doubleNanoTimeToSec(record.getTotalTime());
        double totalTput = doubleFreqPerNanoToFreqPerSec(record.getTotalTput());

        return String.format(Locale.US, "%.2f%s%.2f%s%.2f%s%.2f%s%.2f%s%.2f%s%.2f",
                receiveTime, CSV.SEPARATOR,
                tagRemovalTime, CSV.SEPARATOR,
                wordCountTime, CSV.SEPARATOR,
                serializationTime, CSV.SEPARATOR,
                responseTime, CSV.SEPARATOR,
                totalTime, CSV.SEPARATOR,
                totalTput, CSV.SEPARATOR,
                interarrivalTime
        );
    }

    private String intervalMeasurementToCSVLine(IntervalMeasurement measurement, Long measurementStartTime) {
        double time = longNanoTimeToSec(measurement.getTimestamp() - measurementStartTime);
        double tput = doubleFreqPerNanoToFreqPerSec(measurement.getTput());
        double responseTime = doubleNanoTimeToMs(measurement.getAvgResponseTime());

        return String.format(Locale.US, "%.2f%s%.2f%s%.2f", time, CSV.SEPARATOR, tput, CSV.SEPARATOR, responseTime);
    }

    private static class Record {
        final double receiveTime;
        final double tagRemovalTime;
        final double wordCountTime;
        final double serializationTime;
        final double responseTime;
        final double interarrivalTime;
        final double totalTime;
        final double totalTput;

        public Record(double receiveTime, double tagRemovalTime, double wordCountTime, double serializationTime, double responseTime, double interarrivalTime, double totalTime, double totalTput) {
            this.receiveTime = receiveTime;
            this.tagRemovalTime = tagRemovalTime;
            this.wordCountTime = wordCountTime;
            this.serializationTime = serializationTime;
            this.responseTime = responseTime;
            this.interarrivalTime = interarrivalTime;
            this.totalTime = totalTime;
            this.totalTput = totalTput;
        }

        public double getReceiveTime() {
            return receiveTime;
        }

        public double getTagRemovalTime() {
            return tagRemovalTime;
        }

        public double getWordCountTime() {
            return wordCountTime;
        }

        public double getSerializationTime() {
            return serializationTime;
        }

        public double getResponseTime() {
            return responseTime;
        }

        public double getTotalTime() {
            return totalTime;
        }

        public double getTotalTput() {
            return totalTput;
        }

        public double getInterarrivalTime() {
            return interarrivalTime;
        }
    }

}
