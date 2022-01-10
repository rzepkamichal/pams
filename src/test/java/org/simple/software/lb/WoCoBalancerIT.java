package org.simple.software.lb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.TCPClient;
import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.ThreadedJobExecutor;
import org.simple.software.protocol.Response;
import org.simple.software.server.core.JobExecutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class WoCoBalancerIT {

    final String address = "localhost";
    final int port = 12345;

    private WoCoBalancer balancer;

    @AfterEach
    void tearDown() {
        balancer.stop();
    }

    @Test
    void call_two_services_alternately__when_using_round_robin() {

        balancer = setupBalancerUnderTest();
        new Thread(balancer::run).start();

        // wait for server to set up
        //noinspection StatementWithEmptyBody
        while (!balancer.isReady()) {
        }

        Map<Integer, String> responses = new HashMap<>();

        TCPClient client1 = new TCPClient(address, port);
        TCPClient client2 = new TCPClient(address, port);
        TCPClient client3 = new TCPClient(address, port);
        TCPClient client4 = new TCPClient(address, port);
        TCPClient client5 = new TCPClient(address, port);
        TCPClient client6 = new TCPClient(address, port);

        sendClientRequest(client1, "a", resp -> responses.put(1, resp));
        sendClientRequest(client2, "b", resp -> responses.put(2, resp));
        sendClientRequest(client3, "c", resp -> responses.put(3, resp));
        sendClientRequest(client4, "d", resp -> responses.put(4, resp));
        sendClientRequest(client5, "e", resp -> responses.put(5, resp));
        sendClientRequest(client6, "f", resp -> responses.put(6, resp));

        // proof, that the two services have been used alternately
        assertEquals("aa", responses.get(1));
        assertEquals("B", responses.get(2));
        assertEquals("cc", responses.get(3));
        assertEquals("D", responses.get(4));
        assertEquals("ee", responses.get(5));
        assertEquals("F", responses.get(6));
    }

    @Test
    void call_two_services_alternately__when_using_round_robin__and_parallel_clients() {

        balancer = setupBalancerUnderTest();
        new Thread(balancer::run).start();

        // wait for server to set up
        //noinspection StatementWithEmptyBody
        while (!balancer.isReady()) {
        }

        Map<Integer, String> responses = new ConcurrentHashMap<>();

        TCPClient client1 = new TCPClient(address, port);
        TCPClient client2 = new TCPClient(address, port);
        TCPClient client3 = new TCPClient(address, port);
        TCPClient client4 = new TCPClient(address, port);
        TCPClient client5 = new TCPClient(address, port);
        TCPClient client6 = new TCPClient(address, port);

        sendClientRequestOnNewThread(client1, "a", resp -> responses.put(1, resp));
        sendClientRequestOnNewThread(client2, "b", resp -> responses.put(2, resp));
        sendClientRequestOnNewThread(client3, "c", resp -> responses.put(3, resp));
        sendClientRequestOnNewThread(client4, "d", resp -> responses.put(4, resp));
        sendClientRequestOnNewThread(client5, "e", resp -> responses.put(5, resp));
        sendClientRequestOnNewThread(client6, "f", resp -> responses.put(6, resp));

        // wait for all clients beeing served
        //noinspection StatementWithEmptyBody
        while (responses.size() != 6) {
        }

        // we can not check the exact order as clients run in parallel
        // however we can test, whether the amount of double-letters and upper-case letters are equal
        long doubleLetterCount = responses.values().stream()
                .filter(val -> val.length() == 2).count();
        assertSame(3L, doubleLetterCount);

        long upperCaseLetterCount = responses.values().stream()
                .filter(val -> Character.isUpperCase(val.charAt(0))).count();
        assertSame(3L, upperCaseLetterCount);
    }

    @Test
    void call_two_services_alternately__when_using_round_robin__and_parallel_clients__and_real_server_instances() {
        balancer = setupBalancerUnderTestWithRealServers();
        new Thread(balancer::run).start();

        // wait for server to set up
        //noinspection StatementWithEmptyBody
        while (!balancer.isReady()) {
        }

        Map<Integer, String> responses = new ConcurrentHashMap<>();

        TCPClient client1 = new TCPClient(address, port);
        TCPClient client2 = new TCPClient(address, port);
        TCPClient client3 = new TCPClient(address, port);
        TCPClient client4 = new TCPClient(address, port);
        TCPClient client5 = new TCPClient(address, port);
        TCPClient client6 = new TCPClient(address, port);

        sendClientRequestOnNewThread(client1, "a", resp -> responses.put(1, resp));
        sendClientRequestOnNewThread(client2, "b", resp -> responses.put(2, resp));
        sendClientRequestOnNewThread(client3, "c", resp -> responses.put(3, resp));
        sendClientRequestOnNewThread(client4, "d", resp -> responses.put(4, resp));
        sendClientRequestOnNewThread(client5, "e", resp -> responses.put(5, resp));
        sendClientRequestOnNewThread(client6, "f", resp -> responses.put(6, resp));

        // wait for all clients beeing served
        //noinspection StatementWithEmptyBody
        while (responses.size() != 6) {
        }

        // we can not check the exact order as clients run in parallel
        // however we can test, whether the amount of double-letters and upper-case letters are equal
        long doubleLetterCount = responses.values().stream()
                .filter(val -> val.length() == 2).count();
        assertSame(3L, doubleLetterCount);

        long upperCaseLetterCount = responses.values().stream()
                .filter(val -> Character.isUpperCase(val.charAt(0))).count();
        assertSame(3L, upperCaseLetterCount);
    }

    private WoCoBalancer setupBalancerUnderTest() {
        // the first service duplicates the received data
        BackendService duplicatingService = req -> Response.of(req.getData() + req.getData());

        // the second service transforms the received data to upper case
        BackendService upperCaseService = req -> Response.of(req.getData().toUpperCase());

        LoadBalancer lb = new RoundRobinBalancer(List.of(duplicatingService, upperCaseService));
        JobExecutor executor = new ThreadedJobExecutor(1);

        return new WoCoBalancer(address, port, lb, executor);
    }

    private WoCoBalancer setupBalancerUnderTestWithRealServers() {
        // the first service duplicates the received data
        ServerController controller1 = req -> CompletableFuture.completedFuture(
                Response.of(req.getData() + req.getData()));
        TCPServer server1 = new TCPServer("localhost", 26000, controller1);
        runServerOnNewThread(server1);

        // client which calls duplicatingServer
        BackendService duplicatingService =
                new WoCoService(new TCPClient("localhost", 26000));

        // the second service transforms the received data to upper case
        ServerController controller2 = req -> CompletableFuture.completedFuture(
                Response.of(req.getData().toUpperCase()));
        TCPServer server2 = new TCPServer("localhost", 26001, controller2);
        runServerOnNewThread(server2);

        // client which calls upperCase server
        BackendService upperCaseService =
                new WoCoService(new TCPClient("localhost", 26001));

        LoadBalancer lb = new RoundRobinBalancer(List.of(duplicatingService, upperCaseService));
        JobExecutor executor = new ThreadedJobExecutor(1);

        // wait for services to set up
        //noinspection StatementWithEmptyBody
        while (!server1.isReady() && !server2.isReady()) {
        }

        return new WoCoBalancer(address, port, lb, executor);
    }

    private void sendClientRequest(TCPClient client, String request, Consumer<String> onResponse) {
        try {
            String response = client.send(request);
            onResponse.accept(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendClientRequestOnNewThread(TCPClient client, String request, Consumer<String> onResponse) {
        new Thread(() -> {
            try {
                String response = client.send(request);
                onResponse.accept(response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void runServerOnNewThread(TCPServer server) {
        new Thread(() -> {
            try {
                server.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


}