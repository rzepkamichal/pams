package org.simple.software.stats;

import java.util.Collections;
import java.util.List;

public interface ResponseTimeMeasurementService {

    void start();
    void stop();
    List<ResponseTimeMeasurement> getMeasurements();
    ResponseTimeMeasurement measureLatestInterval();
    long getStartTimestamp();

    ResponseTimeMeasurementService EMPTY = new ResponseTimeMeasurementService() {
        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public List<ResponseTimeMeasurement> getMeasurements() {
            return Collections.emptyList();
        }

        @Override
        public ResponseTimeMeasurement measureLatestInterval() {
            return ResponseTimeMeasurement.empty();
        }

        @Override
        public long getStartTimestamp() {
            return 0;
        }
    };
}
