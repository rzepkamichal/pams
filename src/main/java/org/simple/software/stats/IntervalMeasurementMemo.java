package org.simple.software.stats;

class IntervalMeasurementMemo {

    private final int prevLastIndex;

    public IntervalMeasurementMemo(int prevLastIndex) {
        this.prevLastIndex = prevLastIndex;
    }

    public int listSize() {
        return prevLastIndex;
    }
}
