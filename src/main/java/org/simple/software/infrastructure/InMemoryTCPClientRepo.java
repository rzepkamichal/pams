package org.simple.software.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class InMemoryTCPClientRepo implements TCPClientRepo {

    private final Map<Integer, TCPClient> clients = new ConcurrentHashMap<>();

    @Override
    public TCPClient getOrCreate(int clientId, Supplier<TCPClient> clientSupplier) {

        TCPClient client = clients.get(clientId);

        if (client == null) {
            client = clientSupplier.get();
            clients.put(clientId, client);
        }

        return client;
    }
}
