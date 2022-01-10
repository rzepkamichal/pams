package org.simple.software.infrastructure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.simple.software.protocol.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

class TCPServerIT {

    public static final String address = "localhost";
    public static final int port = 12345;

    TCPServer server;
    TCPClient client = new TCPClient(address, port);

    @AfterEach
    void tearDown() throws IOException {
        server.stop();
    }

    @Test
    void server_runs_and_applies_controller() throws IOException {
        // trivial controller changing a given letter to upper case
        ServerController controller = request -> CompletableFuture.completedFuture(
                Response.of(request.getData().toUpperCase()));

        server = new TCPServer(address, port, controller);
        runServerOnNewThread(server);

        waitForServer(server);
        String response = client.send("a");
        client.close();

        assertEquals("A", response);

    }

    @Test
    void handles_multiple_clients() throws IOException {
        // trivial controller changing a given letter to upper case
        ServerController controller = request -> CompletableFuture.completedFuture(
                Response.of(request.getData().toUpperCase()));

        server = new TCPServer(address, port, controller);
        runServerOnNewThread(server);

        waitForServer(server);

        TCPClient client1 = new TCPClient(address, port);
        TCPClient client2 = new TCPClient(address, port);
        TCPClient client3 = new TCPClient(address, port);

        Map<Integer, String> responses = new ConcurrentHashMap<>();

        sendClientRequestOnNewThread(client1, "a", resp -> responses.put(1, resp));
        sendClientRequestOnNewThread(client2, "b", resp -> responses.put(2, resp));
        sendClientRequestOnNewThread(client3, "abc", resp -> responses.put(3, resp));

        // wait for all clients being served
        //noinspection StatementWithEmptyBody
        while(responses.size() != 2) {}

        client1.close();
        client2.close();
        client3.close();

        assertEquals("A", responses.get(1));
        assertEquals("B", responses.get(2));
        assertEquals("ABC", responses.get(3));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void waitForServer(TCPServer server) {
        while(!server.isReady()) {}
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
}