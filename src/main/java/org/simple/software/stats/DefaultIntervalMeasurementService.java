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

    public DefaultIntervalMeasurementService(ResponseTimeSource dataSource, int msInteravlLen) {
        this.msInteravlLen = msInteravlLen;
        this.dataSource = dataSource;

        task = new TimerTask() {
            @Override
            public void run() {
                IntervalMeasurement measurement = measureLatestInterval();
                measurements.add(measurement);
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
    public List<IntervalMeasurement> getMeasurements() {
        return Collections.unmodifiableList(measurements);
    }

    @Override
    public IntervalMeasurement measureLatestInterval() {
        List<Long> intervalMeasurements = new LinkedList<>();

        dataSource.getClientIds().stream()
                .map(this::getLatestIntervalByClient)
                .collect(Collectors.toList())
                .forEach(intervalMeasurements::addAll);

        long timestamp = System.nanoTime();
        long elapsedTime = timestamp - latestMeasurementTimestamp;
        latestMeasurementTimestamp = timestamp;
        long successCount = intervalMeasurements.size();
        double tput = 1.0 * successCount / elapsedTime;
        double avgResponseTime = intervalMeasurements.stream()
                .mapToDouble(Long::doubleValue)
                .average()
                .orElse(0);

        IntervalMeasurement measurement = new IntervalMeasurement(timestamp, successCount, tput, avgResponseTime);

        return measurement;
    }

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
