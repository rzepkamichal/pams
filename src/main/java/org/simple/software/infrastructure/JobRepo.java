package org.simple.software.infrastructure;

import org.simple.software.server.core.WoCoJob;

public interface JobRepo {

    WoCoJob save(WoCoJob job);
    boolean allJobsProcessed();
}
