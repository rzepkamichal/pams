package org.simple.software.infrastructure;

import org.simple.software.protocol.WoCoRequest;

import java.util.Optional;

public interface RequestRepo {

    Optional<WoCoRequest> getByClientId(int clientId);
    WoCoRequest save(WoCoRequest req);
    void removeByClientId(int clientId);
    long count();
}
