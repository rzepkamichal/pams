package org.simple.software.infrastructure;

import org.simple.software.protocol.Request;

import java.util.Optional;

public interface RequestRepo {

    Optional<Request> getByClientId(int clientId);
    Request save(Request req);
    void removeByClientId(int clientId);
    long count();
}
