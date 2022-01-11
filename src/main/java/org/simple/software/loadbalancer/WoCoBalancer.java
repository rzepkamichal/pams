package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.TCPClient;
import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.ThreadedJobExecutor;
import org.simple.software.infrastructure.JobExecutor;

import java.io.IOException;
import java.util.List;

public class WoCoBalancer {

    private final TCPServer server;
    private final ServerController controller;

    public static void main(String[] args) {

        BackendService service1 = new WoCoService(new TCPClient("localhost", 12346));
        BackendService service2 = new WoCoService(new TCPClient("localhost", 12347));

        LoadBalancer lb = new RoundRobinBalancer(List.of(service1, service2));
        JobExecutor executor = new ThreadedJobExecutor(1);

        WoCoBalancer balancer = new WoCoBalancer("localhost", 12345, lb, executor);
        balancer.run();
    }

    WoCoBalancer(String address, int port, LoadBalancer loadBalancer, JobExecutor jobExecutor) {
        controller = new LBServerController(loadBalancer, jobExecutor);
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
