package org.simple.software.infrastructure;

import java.util.function.Supplier;

public interface TCPClientRepo {

    TCPClient getOrCreate(int clientId, Supplier<TCPClient> clientSupplier);
}
