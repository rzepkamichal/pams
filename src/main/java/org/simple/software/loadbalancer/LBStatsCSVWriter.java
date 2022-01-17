package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.CSV;
import org.simple.software.stats.IntervalMeasurement;
import org.simple.software.stats.ProcessingStatsRepo;
import org.simple.software.stats.IntervalMeasurementService;
import org.simple.software.stats.StatsWriter;
import org.simple.software.stats.TimedRunner;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class LBStatsCSVWriter implements StatsWriter {

    private final Logger log = Logger.getLogger(getClass().getName());

    private static final String FILE_NAME_INTERVAL_MEASUREMENTS = "tput-rt-intervals.csv";

    private static final String FILE_NAME_AVG_STATS = "avg-stats.csv";
    private static final String FILE_NAME_LB_TIME_PERCENTILES = "lb-time-percentiles.csv";
    private static final String FILE_NAME_SYSTEM_RESPONSE_TIME_PERCENTILES = "system-response-time-percentiles.csv";

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

        double avgLBTime = statsRepo.getAcummulativeStats().getAvg(LBStats.TIME_SPENT_IN_LB);
        double avgSystemRT = statsRepo.getAcummulativeStats().getAvg(LBStats.SYSTEM_RESPONSE_TIME);
        AvgRecord record = new AvgRecord(avgLBTime, avgSystemRT);

        String header = ATTRIB_TIME_SPENT_IN_LB + CSV.SEPARATOR + ATTRIB_RESPONSE_TIME;
        CSV.writeToFile(logsDir, FILE_NAME_AVG_STATS, header, List.of(record), this::avgRecordToCSVLine);

        List<Double> lbTimePercentiles = statsRepo.getAcummulativeStats().get100Percentiles(LBStats.TIME_SPENT_IN_LB);
        CSV.writeToFile(logsDir, FILE_NAME_LB_TIME_PERCENTILES, "", lbTimePercentiles,
                value -> doubleToCSVValue(doubleNanoTimeToMs(value)));

        List<Double> rtPercentiles = statsRepo.getAcummulativeStats().get100Percentiles(LBStats.SYSTEM_RESPONSE_TIME);
        CSV.writeToFile(logsDir, FILE_NAME_SYSTEM_RESPONSE_TIME_PERCENTILES, "", rtPercentiles,
                value -> doubleToCSVValue(doubleNanoTimeToMs(value)));

        List<IntervalMeasurement> measurements = rtMeasurementSvc.getMeasurements();
        final long measurementStartTime = rtMeasurementSvc.getStartTimestamp();
        header = ATTRIB_TIME + CSV.SEPARATOR + ATTRIB_TPUT + CSV.SEPARATOR + ATTRIB_RESPONSE_TIME;
        CSV.writeToFile(logsDir, FILE_NAME_INTERVAL_MEASUREMENTS, header, measurements,
                measurement -> intervalMeasurementToCSVLine(measurement, measurementStartTime));
        log.info("Writing stats: done");
    }

    private String doubleToCSVValue(Double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private double longNanoTimeToMs(Long time) {
        return time * TimedRunner.PRECISION * 1000;
    }

    private double doubleNanoTimeToMs(Double time) {
        return time * TimedRunner.PRECISION * 1000;
    }

    private double longNanoTimeToSec(Long time) {
        return time * TimedRunner.PRECISION;
    }

    private double doubleFreqPerNanoToFreqPerSec(Double frequency) {
        return frequency / TimedRunner.PRECISION;
    }


    private String intervalMeasurementToCSVLine(IntervalMeasurement measurement, Long measurementStartTime) {
        double time = longNanoTimeToSec(measurement.getTimestamp() - measurementStartTime);
        double tput = doubleFreqPerNanoToFreqPerSec(measurement.getTput());
        double responseTime = doubleNanoTimeToMs(measurement.getAvgResponseTime());

        return String.format(Locale.US, "%.2f%s%.2f%s%.2f", time, CSV.SEPARATOR, tput, CSV.SEPARATOR, responseTime);
    }

    private String avgRecordToCSVLine(AvgRecord record) {
        double avgLBTimeMs = doubleNanoTimeToMs(record.getAvgLBTime());
        double avgSystemRTMs = doubleNanoTimeToMs(record.getAvgSystemRT());

        return String.format(Locale.US, "%.2f%s%.2f", avgLBTimeMs, CSV.SEPARATOR, avgSystemRTMs);
    }

    private static class AvgRecord {
        private final double avgLBTime;
        private final double avgSystemRT;

        private AvgRecord(double avgLBTime, double avgSystemRT) {
            this.avgLBTime = avgLBTime;
            this.avgSystemRT = avgSystemRT;
        }

        public double getAvgLBTime() {
            return avgLBTime;
        }

        public double getAvgSystemRT() {
            return avgSystemRT;
        }
    }
}
