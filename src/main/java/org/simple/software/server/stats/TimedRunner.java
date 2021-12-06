package org.simple.software.server.stats;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TimedRunner {

    /**
     * Timer precision in seconds
     */
    public static final double PRECISION = 0.000000001;

    public static <T> T run(Supplier<T> operation, Consumer<Long> elapsedTimeConsumer) {
        TimedResult<T> result = run(operation);
        elapsedTimeConsumer.accept(result.getProcessingTime());

        return result.getResult();
    }

    public static <T> TimedResult<T> run(Supplier<T> operation) {
        long startTime = System.nanoTime();
        T result = operation.get();
        long elapsedTime = System.nanoTime() - startTime;

        return new TimedResult<>(elapsedTime, result);
    }

}
