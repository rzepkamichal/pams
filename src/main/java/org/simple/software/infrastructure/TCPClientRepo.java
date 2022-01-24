package org.simple.software.infrastructure;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface TCPClientRepo {

    Collection<TCPClient> getAll();
    void removeAll();
    void removeById(String id);
    TCPClient getOrCreate(String id, Supplier<TCPClient> clientSupplier);
    Optional<TCPClient> get(String id);
}
