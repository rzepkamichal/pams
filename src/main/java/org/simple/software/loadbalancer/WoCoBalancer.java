package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.ThreadedJobExecutor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class WoCoBalancer {

    private final TCPServer server;

    public static void main(String[] args) {

        int threadNum = 1;
        BackendService service1 = new WoCoService("localhost", 12346);
        BackendService service2 = new WoCoService("localhost", 12347);
        List<BackendService> backendServices = List.of(service1, service2);

        WoCoBalancer balancer = new WoCoBalancer("localhost", 12345, threadNum, backendServices);
        balancer.run();
    }

    WoCoBalancer(String address, int port, int threadNum, Collection<BackendService> services) {
        ServerController controller = new LBServerController(
                new RoundRobinBalancer(services),
                new ThreadedJobExecutor(threadNum)
        );

        server = new TCPServer(address, port, controller);
    }

    public void run() {
        try {
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isReady() {
        return server.isReady();
    }

    public void stop() {
        try {
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
