package org.simple.software.infrastructure;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

import java.util.concurrent.CompletableFuture;

public interface ServerController {

    CompletableFuture<Response> handle(Request request);

    /**
     * Callback triggered when a client has disconnected.
     * @param clientId the client's id
     */
    default void onDisconnect(int clientId) {};

    /**
     * Callback triggered when no client has been served for a configured amount of time.
     */
    default void onIdle() {};
}
