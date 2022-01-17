package org.simple.software.stats;

public class ResponseTimeMeasurement {

    private final long timestamp;
    private final long successCount;
    private final double tput;
    private final double avgResponseTime;

    public ResponseTimeMeasurement(long timestamp, long successCount, double tput, double avgResponseTime) {
        this.timestamp = timestamp;
        this.successCount = successCount;
        this.tput = tput;
        this.avgResponseTime = avgResponseTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getTput() {
        return tput;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public static ResponseTimeMeasurement empty() {
        return new ResponseTimeMeasurement(0, 0, 0.0, 0.0);
    }
}
