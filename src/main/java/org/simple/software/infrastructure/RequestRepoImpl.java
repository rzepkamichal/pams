package org.simple.software.infrastructure;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.WoCoRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestRepoImpl implements RequestRepo {

    Map<Integer, Request> requests = new HashMap<>();

    @Override
    public Optional<Request> getByClientId(int clientId) {
        return Optional.ofNullable(requests.get(clientId));
    }

    @Override
    public Request save(Request req) {
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
