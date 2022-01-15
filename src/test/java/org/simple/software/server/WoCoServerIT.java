package org.simple.software.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.simple.software.infrastructure.TCPClient;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WoCoServerIT {
    public static final String ADDRESS = "localhost";
    public static final int PORT = 12347;

    WoCoServer server;

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void counts_words__serving_single_client() throws IOException {
        server = new WoCoServer(ADDRESS, PORT, 1, true);
        new Thread(server::run).start();
        waitForServer(server);

        TCPClient client = new TCPClient(ADDRESS, PORT);

        String response = client.send("aaa bb cc cc bb aa c");
        client.close();

        // FIXME actually, the order of words shouldn't matter but I was to lazy to implement such check
        assertEquals("aaa,1,bb,2,cc,2,aa,1,c,1,", response);
    }

    @Test
    void counts_words__serving_multiple_clients() {
        server = new WoCoServer(ADDRESS, PORT, 1, true);
        new Thread(server::run).start();
        waitForServer(server);

        TCPClient client1 = new TCPClient(ADDRESS, PORT);
        TCPClient client2 = new TCPClient(ADDRESS, PORT);
        TCPClient client3 = new TCPClient(ADDRESS, PORT);

        Map<Integer, String> responses = new ConcurrentHashMap<>();

        sendClientRequestOnNewThread(client1, "aa bb bb", resp -> responses.put(1, resp));
        sendClientRequestOnNewThread(client2, "cc c cc cc dd", resp -> responses.put(2, resp));
        sendClientRequestOnNewThread(client3, "eee eee kk eee kk", resp -> responses.put(3, resp));

        // wait for all clients  to be served
        while (responses.size() != 3) {
        }

        // FIXME actually, the order of words shouldn't matter but I was to lazy to implement such check
        assertEquals("aa,1,bb,2,", responses.get(1));
        assertEquals("cc,3,dd,1,c,1,", responses.get(2));
        assertEquals("kk,2,eee,3,", responses.get(3));
    }

    @Test
    void counts_words__serving_multiple_clients_on_multiple_threads() {
        server = new WoCoServer(ADDRESS, PORT, 3, true);
        new Thread(server::run).start();
        waitForServer(server);

        TCPClient client1 = new TCPClient(ADDRESS, PORT);
        TCPClient client2 = new TCPClient(ADDRESS, PORT);
        TCPClient client3 = new TCPClient(ADDRESS, PORT);

        Map<Integer, String> responses = new ConcurrentHashMap<>();

        sendClientRequestOnNewThread(client1, "aa bb bb", resp -> responses.put(1, resp));
        sendClientRequestOnNewThread(client2, "cc c cc cc dd", resp -> responses.put(2, resp));
        sendClientRequestOnNewThread(client3, "eee eee kk eee kk", resp -> responses.put(3, resp));

        // wait for all clients  to be served
        while (responses.size() != 3) {
        }

        // FIXME actually, the order of words shouldn't matter but I was to lazy to implement such check
        assertEquals("aa,1,bb,2,", responses.get(1));
        assertEquals("cc,3,dd,1,c,1,", responses.get(2));
        assertEquals("kk,2,eee,3,", responses.get(3));
    }


    private void waitForServer(WoCoServer server) {
        while(!server.isReady()) {}
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
