package org.simple.software.server.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestRepoImpl implements RequestRepo {

    Map<Integer, WoCoRequest> requests = new HashMap<>();

    @Override
    public Optional<WoCoRequest> getByClientId(int clientId) {
        return Optional.of(requests.get(clientId));
    }

    @Override
    public WoCoRequest save(WoCoRequest req) {
        requests.put(req.getClientId(), req);
        return req;
    }
}
