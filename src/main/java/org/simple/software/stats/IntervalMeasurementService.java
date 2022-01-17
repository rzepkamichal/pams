package org.simple.software.stats;

import java.util.Collections;
import java.util.List;

public interface IntervalMeasurementService {

    void start();
    void stop();
    List<IntervalMeasurement> getMeasurements();
    IntervalMeasurement measureLatestInterval();
    long getStartTimestamp();
    long getLastMeasurementTimestamp();

    IntervalMeasurementService EMPTY = new IntervalMeasurementService() {
        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public List<IntervalMeasurement> getMeasurements() {
            return Collections.emptyList();
        }

        @Override
        public IntervalMeasurement measureLatestInterval() {
            return IntervalMeasurement.empty();
        }

        @Override
        public long getStartTimestamp() {
            return 0;
        }

        @Override
        public long getLastMeasurementTimestamp() {
            return 0;
        }
    };
}
