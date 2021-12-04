package org.simple.software.server.stats;

import java.time.Clock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TimedRunner {

    public static <T> TimedResult<T> run(Supplier<T> operation) {
        return run(operation, Clock.systemDefaultZone());
    }

    public static <T> T run(Supplier<T> operation, Consumer<Long> elapsedTimeConsumer) {
        TimedResult<T> result = run(operation, Clock.systemDefaultZone());
        elapsedTimeConsumer.accept(result.getProcessingTime());

        return result.getResult();
    }

    public static <T> TimedResult<T> run(Supplier<T> operation, Clock clock) {
        long startTime = clock.millis();
        T result = operation.get();
        long elapsedTime = clock.millis() - startTime;

        return new TimedResult<>(elapsedTime, result);
    }

    public static <T> T run(Supplier<T> operation, Consumer<Long> elapsedTimeConsumer, Clock clock) {
        TimedResult<T> result = run(operation, clock);
        elapsedTimeConsumer.accept(result.getProcessingTime());

        return result.getResult();
    }


}
