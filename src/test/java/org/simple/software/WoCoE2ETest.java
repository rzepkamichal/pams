package org.simple.software;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.simple.software.infrastructure.InMemoryTCPClientRepo;
import org.simple.software.infrastructure.TCPClient;
import org.simple.software.infrastructure.TCPClientRepo;
import org.simple.software.loadbalancer.BackendService;
import org.simple.software.loadbalancer.WoCoBalancer;
import org.simple.software.loadbalancer.WoCoService;
import org.simple.software.server.WoCoServer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class WoCoE2ETest {

    // beautifies logs
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }

    public static final String ADDRESS = "localhost";

    public static final int PORT_LB = 25000;
    public static final int PORT_S1 = 25001;
    public static final int PORT_S2 = 25002;

    WoCoServer woCoServer1;
    WoCoServer woCoServer2;
    BackendService service1;
    BackendService service2;

    WoCoBalancer loadBalancer;

    TCPClient client1 = new TCPClient(ADDRESS, PORT_LB);
    TCPClient client2 = new TCPClient(ADDRESS, PORT_LB);
    TCPClient client3 = new TCPClient(ADDRESS, PORT_LB);
    TCPClient client4 = new TCPClient(ADDRESS, PORT_LB);

    @AfterEach
    void tearDown() {
        client1.close();
        client2.close();
        client3.close();
        client4.close();
        loadBalancer.stop();
        woCoServer1.stop();
        woCoServer2.stop();
    }

    @Test
    void test_with_sequential_clients() throws IOException {
        startServers();
        startLoadBalancer();

        String resp1 = client1.send("aa");
        String resp2 = client2.send("bb bb");
        String resp3 = client1.send("cc cc cc");
        String resp4 = client2.send("dd dd dd dd");

        // both backend servers should get equal num of requests
        verify(service1, times(2)).serve(any());
        verify(service2, times(2)).serve(any());

        assertEquals("aa,1,", resp1);
        assertEquals("bb,2,", resp2);
        assertEquals("cc,3,", resp3);
        assertEquals("dd,4,", resp4);
    }

    @Test
    void test_with_sequential_clients_and_data_with_multiple_lines() throws IOException {
        startServers();
        startLoadBalancer();

        String resp1 = client1.send("aa $ xyz");
        String resp2 = client2.send("bb bb");
        String resp3 = client1.send("cc $cc cc");
        String resp4 = client2.send("dd dd dd dd");

        // both backend servers should get equal num of requests
        verify(service1, times(2)).serve(any());
        verify(service2, times(2)).serve(any());

        assertEquals("aa,1,xyz,1,", resp1);
        assertEquals("bb,2,", resp2);
        assertEquals("cc,3,", resp3);
        assertEquals("dd,4,", resp4);
    }

    @Test
    void test_with_parallel_clients() {
        startServers();
        startLoadBalancer();

        // the number of requests each client repeats
        int repeatNum = 100;
        Map<Integer, String> responses = new ConcurrentHashMap<>();
        sendClientRequestOnNewThread(client1, "aa bb$ bb", resp -> responses.put(1, resp), repeatNum);
        sendClientRequestOnNewThread(client2, "cc c cc cc dd", resp -> responses.put(2, resp), repeatNum);
        sendClientRequestOnNewThread(client3, "eee eee kk eee kk", resp -> responses.put(3, resp), repeatNum);
        sendClientRequestOnNewThread(client4, "aa aa bb", resp -> responses.put(4, resp), repeatNum);

        // wait for all clients to get responses
        while (responses.size() != 4) {
        }

        // both backend servers should get equal num of requests
        int totalRequestNum = repeatNum * 4;
        verify(service1, times(totalRequestNum / 2)).serve(any());
        verify(service2, times(totalRequestNum / 2)).serve(any());

        // FIXME actually, the order of words shouldn't matter but I was to lazy to implement such check
        assertEquals("aa,1,bb,2,", responses.get(1));
        assertEquals("cc,3,dd,1,c,1,", responses.get(2));
        assertEquals("kk,2,eee,3,", responses.get(3));
        assertEquals("aa,2,bb,1,", responses.get(4));
    }

    private void startServers() {
        woCoServer1 = new WoCoServer(ADDRESS, PORT_S1, 1, true);
        woCoServer2 = new WoCoServer(ADDRESS, PORT_S2, 1, true);

        new Thread(woCoServer1::run).start();
        new Thread(woCoServer2::run).start();

        // wait for servers to start
        while (!(woCoServer1.isReady() && woCoServer2.isReady())) {
        }
    }

    private void startLoadBalancer() {
        TCPClientRepo repo = new InMemoryTCPClientRepo();

        service1 = spy(new WoCoService(ADDRESS, PORT_S1, repo));
        service2 = spy(new WoCoService(ADDRESS, PORT_S2, repo));
        loadBalancer = new WoCoBalancer(ADDRESS, PORT_LB, 2, List.of(service1, service2));

        new Thread(loadBalancer::run).start();

        // wait for load balancer to start
        while (!loadBalancer.isReady()) {
        }
    }

    private void sendClientRequestOnNewThread(TCPClient client, String request, Consumer<String> onResponse, int repeatNum) {
        new Thread(() -> {
            try {
                for (int i = 0; i < repeatNum; i++) {
                    String response = client.send(request);

                    // for test purposes: accept only last response
                    if (i == repeatNum - 1) {
                        onResponse.accept(response);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}

