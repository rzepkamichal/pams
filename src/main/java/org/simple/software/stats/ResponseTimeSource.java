package org.simple.software.stats;

import java.util.List;
import java.util.Set;

public interface ResponseTimeSource {

    List<Long> getResponseTimeRecordsByClient(int clientId);

    Set<Integer> getClientIds();
}
