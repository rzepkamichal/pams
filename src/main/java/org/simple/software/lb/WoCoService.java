package org.simple.software.lb;

import org.simple.software.infrastructure.TCPClient;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

import java.io.IOException;

public class WoCoService implements BackendService {

    private final TCPClient client;

    public WoCoService(TCPClient client) {
        this.client = client;
    }

    @Override
    public Response serve(Request request) {
        try {
            return Response.of(client.send(request.getData()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
