package org.simple.software.server.core;

import org.simple.software.infrastructure.JobRepo;

import java.util.HashSet;
import java.util.Set;

public class JobRepoImpl implements JobRepo {

    private Set<WoCoJob> jobs = new HashSet<>();

    @Override
    public WoCoJob save(WoCoJob job) {
        jobs.add(job);
        return job;
    }

    @Override
    public boolean allJobsProcessed() {
        return jobs.stream().allMatch(WoCoJob::isCompleted);
    }


}
