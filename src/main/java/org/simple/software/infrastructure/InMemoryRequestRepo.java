package org.simple.software.infrastructure;

import org.simple.software.protocol.Request;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRequestRepo implements RequestRepo {

    Map<Integer, Request> requests = new ConcurrentHashMap<>();

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
