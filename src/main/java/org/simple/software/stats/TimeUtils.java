package org.simple.software.stats;

public class TimeUtils {

    public static double longNanoTimeToMs(Long time) {
        return time * TimedRunner.PRECISION * 1000;
    }

    public static double doubleNanoTimeToMs(Double time) {
        return time * TimedRunner.PRECISION * 1000;
    }
    public static double doubleNanoTimeToSec(Double time) {
        return time * TimedRunner.PRECISION;
    }

    public static double longNanoTimeToSec(Long time) {
        return time * TimedRunner.PRECISION;
    }

    public static double doubleFreqPerNanoToFreqPerSec(Double frequency) {
        return frequency / TimedRunner.PRECISION;
    }
}
