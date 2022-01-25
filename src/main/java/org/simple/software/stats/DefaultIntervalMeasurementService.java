package org.simple.software.stats;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DefaultIntervalMeasurementService implements IntervalMeasurementService {

    private final int msInteravlLen;
    private final ResponseTimeSource dataSource;

    private final Map<Integer, IntervalMeasurementMemo> latestMeasurementHistory = new ConcurrentHashMap<>();
    private final List<IntervalMeasurement> measurements = Collections.synchronizedList(new LinkedList<>());

    private final TimerTask task;
    private volatile Timer timer;

    private volatile long startTime = 0L;
    private volatile long latestMeasurementTimestamp = startTime;
    private volatile long latestNonZeroMeasurementTimestamp = 0L;

    public DefaultIntervalMeasurementService(ResponseTimeSource dataSource, int msInteravlLen) {
        this.msInteravlLen = msInteravlLen;
        this.dataSource = dataSource;

        task = new TimerTask() {
            @Override
            public void run() {
                IntervalMeasurement measurement = measureLatestInterval();
                measurements.add(measurement);

                if (measurement.getTput() > 0 || measurement.getAvgResponseTime() > 0) {
                    latestNonZeroMeasurementTimestamp = System.nanoTime();
                }
            }
        };
    }

    @Override
    public synchronized void start() {
        if (timer != null) {
            timer.cancel();
            measurements.clear();
            latestMeasurementHistory.clear();
        }

        timer = new Timer();
        startTime = System.nanoTime();
        timer.schedule(task, 0, msInteravlLen);
    }

    @Override
    public synchronized void stop() {
        if (timer != null) {
            timer.cancel();
            latestMeasurementHistory.clear();
        }
    }

    @Override
    public long getStartTimestamp() {
        return startTime;
    }

    @Override
    public long getLastMeasurementTimestamp() {
        // cutoff trailing zeros from the calculation
        // and take the timestamp at which the first zero occurred, after which there were zeros only
        return latestNonZeroMeasurementTimestamp + (long) (msInteravlLen * 1000L * TimedRunner.PRECISION);
    }

    @Override
    public List<IntervalMeasurement> getMeasurements() {
        return Collections.unmodifiableList(measurements);
    }


    @Override
    public IntervalMeasurement measureLatestInterval() {

        // get measurement records from the latest interval for each client and merge them into one list
        List<Long> intervalMeasurements = new LinkedList<>();
        dataSource.getClientIds().stream()
                .map(this::getLatestIntervalByClient)
                .collect(Collectors.toList())
                .forEach(intervalMeasurements::addAll);

        // calculate throughput for the latest interval
        long timestamp = System.nanoTime();
        long elapsedTime = timestamp - latestMeasurementTimestamp;
        latestMeasurementTimestamp = timestamp;
        long successCount = intervalMeasurements.size();
        double tput = 1.0 * successCount / elapsedTime;

        // calculate avg response time for the latest interval
        double avgResponseTime = intervalMeasurements.stream()
                .mapToDouble(Long::doubleValue)
                .average()
                .orElse(0);

        IntervalMeasurement measurement = new IntervalMeasurement(timestamp, successCount, tput, avgResponseTime);

        return measurement;
    }

    /**
     * Gets the measurement records from the data source,
     * which have been added since the last measurement
     *
     * @param clientId the client whose records will be looked up
     * @return a list of new measurement records since last measurement
     */
    private List<Long> getLatestIntervalByClient(int clientId) {
        IntervalMeasurementMemo prevMeasurement = Optional.ofNullable(latestMeasurementHistory.get(clientId))
                .orElseGet(() -> new IntervalMeasurementMemo(0));

        List<Long> allClientResponseTimeRecords = getAllClientResponseTimeRecords(clientId);
        int currentSize = allClientResponseTimeRecords.size();

        if (currentSize <= prevMeasurement.listSize()) {
            return Collections.emptyList();
        }

        IntervalMeasurementMemo currentMeasurement = new IntervalMeasurementMemo(currentSize);
        latestMeasurementHistory.put(clientId, currentMeasurement);

        return allClientResponseTimeRecords.subList(prevMeasurement.listSize(), allClientResponseTimeRecords.size());

    }

    private List<Long> getAllClientResponseTimeRecords(int clientId) {
        return dataSource.getResponseTimeRecordsByClient(clientId);
    }
}
