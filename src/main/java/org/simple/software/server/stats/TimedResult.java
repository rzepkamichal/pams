package org.simple.software.server.stats;

public class TimedResult<T> {

    private final T result;
    private final long processingTime;

    public TimedResult(long processingTime, T result) {
        this.result = result;
        this.processingTime = processingTime;
    }

    public T getResult() {
        return result;
    }

    public long getProcessingTime() {
        return processingTime;
    }
}
