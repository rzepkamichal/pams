package org.simple.software.lb;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RoundRobinBalancer implements LoadBalancer {

    private final Map<Integer, BackendService> services = new HashMap<>();
    private final int numServices;

    private int nextId = 0;

    public RoundRobinBalancer(Collection<BackendService> services) {
        numServices = services.size();
        var iter = services.iterator();
        for (int i = 0; i < numServices; i++) {
            this.services.put(i, iter.next());
        }
    }

    @Override
    public BackendService getNext() {
        BackendService nextService = services.get(nextId);
        nextId = (nextId + 1) % numServices;

        return nextService;
    }
}
