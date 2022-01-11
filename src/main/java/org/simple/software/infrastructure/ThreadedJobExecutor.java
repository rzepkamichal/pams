package org.simple.software.infrastructure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedJobExecutor implements JobExecutor {

    private final ExecutorService executorService;

    public ThreadedJobExecutor(int threadNum) {
        executorService = Executors.newFixedThreadPool(threadNum);
    }

    @Override
    public void execute(Job job) {
        executorService.execute(job::execute);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
