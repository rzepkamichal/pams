package org.simple.software.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestRepoImpl implements RequestRepo {

    Map<Integer, WoCoRequest> requests = new HashMap<>();

    @Override
    public Optional<WoCoRequest> getByClientId(int clientId) {
        return Optional.ofNullable(requests.get(clientId));
    }

    @Override
    public WoCoRequest save(WoCoRequest req) {
        requests.put(req.getClientId(), req);
        return req;
    }

    @Override
    public void removeByClientId(int clientId) {
        requests.remove(clientId);
    }

    @Override
    public long count() {
        return requests.size();
    }
}
