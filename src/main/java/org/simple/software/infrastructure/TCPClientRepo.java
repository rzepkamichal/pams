package org.simple.software.infrastructure;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface TCPClientRepo {

    Collection<TCPClient> getAll();
    void removeAll();
    void removeByClientId(int clientId);
    TCPClient getOrCreate(int clientId, Supplier<TCPClient> clientSupplier);
    Optional<TCPClient> get(int clientId);
}
