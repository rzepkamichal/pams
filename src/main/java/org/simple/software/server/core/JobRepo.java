package org.simple.software.server.core;

public interface JobRepo {

    WoCoJob save(WoCoJob job);
    boolean allJobsProcessed();
}
