package org.simple.software.infrastructure;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

public interface ServerController {

    CompletableFuture<Response> doService(Request request);

    void onDisconnect(SocketChannel client);
}
