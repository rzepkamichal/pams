package org.simple.software.server.protocol;

import java.util.Optional;

public interface RequestRepo {

    Optional<WoCoRequest> getByClientId(int clientId);
    void save(WoCoRequest req);
}
