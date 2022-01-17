package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.CSV;
import org.simple.software.stats.IntervalMeasurement;
import org.simple.software.stats.ProcessingStatsRepo;
import org.simple.software.stats.IntervalMeasurementService;
import org.simple.software.stats.StatsWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static org.simple.software.stats.TimeUtils.doubleFreqPerNanoToFreqPerSec;
import static org.simple.software.stats.TimeUtils.doubleNanoTimeToMs;
import static org.simple.software.stats.TimeUtils.doubleNanoTimeToSec;
import static org.simple.software.stats.TimeUtils.longNanoTimeToSec;

public class LBStatsCSVWriter implements StatsWriter {

    private final Logger log = Logger.getLogger(getClass().getName());

    private static final String FILE_NAME_AVG_STATS = "total.csv";
    private static final String FILE_NAME_PERCENTILES = "percentiles.csv";
    private static final String FILE_NAME_INTERVALS = "intervals.csv";

    private static final String ATTRIB_TIME = "Time [s]";
    private static final String ATTRIB_TPUT = "Throughput [1/s]";
    private static final String ATTRIB_RESPONSE_TIME = "System Response Time [ms]";
    private static final String ATTRIB_TIME_SPENT_IN_LB = "Load Balancer Service Time [ms]";

    private final ProcessingStatsRepo<LBStats> statsRepo;
    private final IntervalMeasurementService rtMeasurementSvc;

    private final String logsDir;

    public LBStatsCSVWriter(ProcessingStatsRepo<LBStats> statsRepo, IntervalMeasurementService rtMeasurementSvc, String logsDir) {
        this.statsRepo = statsRepo;
        this.rtMeasurementSvc = rtMeasurementSvc;
        this.logsDir = logsDir;
    }

    @Override
    public void writeForClient(int clientId) {

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
        double avgLBTime = statsRepo.getAcummulativeStats().getAvg(LBStats.TIME_SPENT_IN_LB);
        double avgSystemRT = statsRepo.getAcummulativeStats().getAvg(LBStats.SYSTEM_RESPONSE_TIME);
        double totalTime = getTotalTime();
        double totalTput = getTotalTput(totalTime);

        Record record = new Record(avgLBTime, avgSystemRT, totalTime, totalTput);

        String header = ATTRIB_TIME_SPENT_IN_LB + CSV.SEPARATOR + ATTRIB_RESPONSE_TIME + CSV.SEPARATOR
                + ATTRIB_TIME + CSV.SEPARATOR + ATTRIB_TPUT;

        CSV.writeToFile(logsDir, FILE_NAME_AVG_STATS, header, List.of(record), this::recordToCSVLineWithTput);
    }

    private double getTotalTime() {
        final long measurementStartTime = rtMeasurementSvc.getStartTimestamp();
        final long lastMeasurementTime = rtMeasurementSvc.getLastMeasurementTimestamp();
        return lastMeasurementTime - measurementStartTime;
    }

    private double getTotalTput(double totalTime) {
        final int successCount = statsRepo.getAcummulativeStats().getAllRecords(LBStats.SYSTEM_RESPONSE_TIME).size();
        return 1.0 * successCount / totalTime;
    }

    private void writePercentiles() {
        List<Double> lbTimePercentiles = statsRepo.getAcummulativeStats().get100Percentiles(LBStats.TIME_SPENT_IN_LB);
        List<Double> rtPercentiles = statsRepo.getAcummulativeStats().get100Percentiles(LBStats.SYSTEM_RESPONSE_TIME);

        List<Record> records = new LinkedList<>();

        for (int i = 0; i < 100; i++) {
            Record record = new Record(lbTimePercentiles.get(i), rtPercentiles.get(i), -1, -1);
            records.add(record);
        }

        String header = ATTRIB_TIME_SPENT_IN_LB + CSV.SEPARATOR + ATTRIB_RESPONSE_TIME;
        CSV.writeToFile(logsDir, FILE_NAME_PERCENTILES, header, records, this::recordToCSVLineWithoutTput);
    }

    private void writeIntervals() {
        List<IntervalMeasurement> measurements = rtMeasurementSvc.getMeasurements();
        final long measurementStartTime = rtMeasurementSvc.getStartTimestamp();
        String header = ATTRIB_TIME + CSV.SEPARATOR + ATTRIB_TPUT + CSV.SEPARATOR + ATTRIB_RESPONSE_TIME;
        CSV.writeToFile(logsDir, FILE_NAME_INTERVALS, header, measurements,
                measurement -> intervalMeasurementToCSVLine(measurement, measurementStartTime));
    }

    private String intervalMeasurementToCSVLine(IntervalMeasurement measurement, Long measurementStartTime) {
        double time = longNanoTimeToSec(measurement.getTimestamp() - measurementStartTime);
        double tput = doubleFreqPerNanoToFreqPerSec(measurement.getTput());
        double responseTime = doubleNanoTimeToMs(measurement.getAvgResponseTime());

        return String.format(Locale.US, "%.2f%s%.2f%s%.2f", time, CSV.SEPARATOR, tput, CSV.SEPARATOR, responseTime);
    }

    private String recordToCSVLineWithTput(Record record) {
        double avgLBTimeMs = doubleNanoTimeToMs(record.getLoadBalancingTime());
        double avgSystemRTMs = doubleNanoTimeToMs(record.getSystemResponseTime());
        double totalTime = doubleNanoTimeToSec(record.getTotalTime());
        double totalTput = doubleFreqPerNanoToFreqPerSec(record.getTotalTput());

        return String.format(Locale.US, "%.2f%s%.2f%s%.2f%s%.2f",
                avgLBTimeMs, CSV.SEPARATOR,
                avgSystemRTMs, CSV.SEPARATOR,
                totalTime, CSV.SEPARATOR,
                totalTput);
    }

    private String recordToCSVLineWithoutTput(Record record) {
        double avgLBTimeMs = doubleNanoTimeToMs(record.getLoadBalancingTime());
        double avgSystemRTMs = doubleNanoTimeToMs(record.getSystemResponseTime());

        return String.format(Locale.US, "%.2f%s%.2f", avgLBTimeMs, CSV.SEPARATOR, avgSystemRTMs);
    }

    private static class Record {
        private final double loadBalancingTime;
        private final double systemResponseTime;
        private final double totalTime;
        private final double totalTput;

        private Record(double loadBalancingTime, double systemResponseTime, double totalTime, double totalTput) {
            this.loadBalancingTime = loadBalancingTime;
            this.systemResponseTime = systemResponseTime;
            this.totalTime = totalTime;
            this.totalTput = totalTput;
        }

        public double getLoadBalancingTime() {
            return loadBalancingTime;
        }

        public double getSystemResponseTime() {
            return systemResponseTime;
        }

        public double getTotalTime() {
            return totalTime;
        }

        public double getTotalTput() {
            return totalTput;
        }
    }
}
