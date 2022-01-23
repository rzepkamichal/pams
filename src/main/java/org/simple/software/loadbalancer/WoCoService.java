package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.StringUtils;
import org.simple.software.infrastructure.TCPClient;
import org.simple.software.infrastructure.TCPClientRepo;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

import java.io.IOException;

/**
 * Backend service which call WoCoServers.
 */
public class WoCoService implements BackendService {

    private final String address;
    private final int port;

    private final TCPClientRepo repo;

    public WoCoService(String address, int port, TCPClientRepo repo) {
         this.address = address;
         this.port = port;
         this.repo = repo;
    }

    // The backend services use a shared repo of TCPClients.
    // The load balancer maintains a separate TCPClient (socket) for each WoCoClient.
    // It enables forwarding WoCoClient requests on separate channels to the backend servers,
    // rather than using a shared channel. In this way, the WoCoServers can recognise connections from different clients.
    // Otherwise, the WoCoServers would get only a single-channel connection from the LB
    // and would muddle requests from different WoCoClients into one.
    @Override
    public Response serve(Request request) {
        try {
            TCPClient tcpClient = repo.getOrCreate(request.getClientId() + port, () -> new TCPClient(address, port));
            String requestData = request.getData();
            Response response = Response.of(tcpClient.send(requestData));
            return response;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
