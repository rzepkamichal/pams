package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.TCPClient;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

import java.io.IOException;

public class WoCoService implements BackendService {

    private final TCPClient client;

    public WoCoService(String address, int port) {
        this.client = new TCPClient(address, port);
    }

    @Override
    public Response serve(Request request) {
        try {
            String resp = client.send(request.getData());
            return Response.of(resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
