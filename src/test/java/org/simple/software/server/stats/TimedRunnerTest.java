package org.simple.software.server.stats;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimedRunnerTest {

    @Test
    void runs_operation_and_logs_time() {
        Clock clock = mock(Clock.class);
        when(clock.millis())
                .thenReturn(1638653040000L)
                .thenReturn(1638653046000L);

        TimedResult<String> result = TimedRunner.run((() -> "it works!"), clock);

        assertSame("it works!", result.getResult());
        assertEquals(6000L, result.getProcessingTime());
    }

    @Test
    void calls_provided_elapsed_time_consumer() {
        Clock clock = mock(Clock.class);
        when(clock.millis())
                .thenReturn(1638653040000L)
                .thenReturn(1638653046000L);

        // some fake use-case - storing time logs in a list
        List<Long> elapsedTimeLogs = new LinkedList<>();

        String result = TimedRunner.run((() -> "it works!"), elapsedTimeLogs::add, clock);

        assertSame("it works!", result);
        assertEquals(6000, elapsedTimeLogs.get(0));

    }
}