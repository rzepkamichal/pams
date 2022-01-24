package org.simple.software.infrastructure;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class InMemoryTCPClientRepo implements TCPClientRepo {

    private final Map<String, TCPClient> clients = new ConcurrentHashMap<>();

    @Override
    public Optional<TCPClient> get(String id) {
        return Optional.ofNullable(clients.get(id));
    }

    @Override
    public TCPClient getOrCreate(String id, Supplier<TCPClient> clientSupplier) {

        TCPClient client = clients.get(id);

        if (client == null) {
            client = clientSupplier.get();
            clients.put(id, client);
        }

        return client;
    }

    @Override
    public Collection<TCPClient> getAll() {
        return clients.values();
    }

    @Override
    public void removeAll() {
        clients.clear();
    }

    @Override
    public void removeById(String id) {
        clients.remove(id);
    }
}
