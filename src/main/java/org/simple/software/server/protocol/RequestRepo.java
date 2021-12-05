package org.simple.software.server.protocol;

import java.util.Optional;

public interface RequestRepo {

    Optional<WoCoRequest> getByClientId(int clientId);
    WoCoRequest save(WoCoRequest req);
    void removeByClientId(int clientId);
    long count();
}
