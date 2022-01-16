package org.simple.software.infrastructure;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class InMemoryTCPClientRepo implements TCPClientRepo {

    private final Map<Integer, TCPClient> clients = new ConcurrentHashMap<>();

    @Override
    public Optional<TCPClient> get(int clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @Override
    public TCPClient getOrCreate(int clientId, Supplier<TCPClient> clientSupplier) {

        TCPClient client = clients.get(clientId);

        if (client == null) {
            client = clientSupplier.get();
            clients.put(clientId, client);
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
    public void removeByClientId(int clientId) {
        clients.remove(clientId);
    }
}
