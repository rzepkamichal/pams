package org.simple.software.infrastructure;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.RequestFactory;
import org.simple.software.protocol.Response;
import org.simple.software.protocol.WoCoRequestFactory;
import org.simple.software.server.stats.ProcessingStatsRepo;
import org.simple.software.server.stats.StatsRepoImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class TCPServer {

    private final boolean DEBUG = false;
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final String address;
    private final int port;
    private final ServerController controller;

    private Selector selector;
    private ServerSocketChannel serverSocket;

    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean keepRunning = new AtomicBoolean(false);

    private RequestRepo pendingRequestRepo = new InMemoryRequestRepo();
    private RequestFactory requestFactory = new WoCoRequestFactory();
    private ProcessingStatsRepo statsRepo = new StatsRepoImpl();

    private final Set<Integer> servicedClients = new HashSet<>();

    public TCPServer(String address, int port, ServerController controller) {
        this.address = address;
        this.port = port;
        this.controller = controller;
    }

    public void run() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        InetSocketAddress myAddr = new InetSocketAddress(address, port);
        serverSocket.bind(myAddr);
        serverSocket.configureBlocking(false);

        int ops = serverSocket.validOps();
        serverSocket.register(selector, ops, null);

        ByteBuffer bb = ByteBuffer.allocate(1024 * 1024);

        keepRunning.set(true);
        started.set(true);

        log.info("Started. Ready to accept connections.");

        // Infinite loop..
        // Keep server running
        while (keepRunning.get()) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                    log.info("Connection Accepted: " + client.getLocalAddress());

                    servicedClients.add(getClientId(client));

                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    int clientId = getClientId(client);

                    bb.rewind();
                    int readCnt = client.read(bb);

                    if (readCnt > 0) {
                        Request request = pendingRequestRepo.getByClientId(clientId)
                                .orElseGet(() -> createAndSaveRequest(clientId));
                        String dataChunk = new String(bb.array(), 0, readCnt);
                        request.receiveData(dataChunk);

                        if (request.isDataReady()) {

                            if (DEBUG) {
                                log.info(port + " serving \"" + request.getData() + "\" for " + clientId);
                            }

                            pendingRequestRepo.removeByClientId(clientId);
                            statsRepo.getStatsByClient(clientId).logDocReceiveTime(request.getReceiveTime());
                            controller.handle(request)
                                    .thenAccept(response -> sendResponse(client, response));
                        }

                    } else {
                        key.cancel();
                        pendingRequestRepo.removeByClientId(clientId);
                        servicedClients.remove(clientId);
                        controller.onDisconnect(clientId);

                        if (noClientBeingServed()) {
                            scheduleIdleTimeout();
                        }
                    }
                }
                iterator.remove();
            }
        }


    }

    private boolean noClientBeingServed() {
        return servicedClients.isEmpty();
    }

    private void scheduleIdleTimeout() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::onIdleTimeout, 200, TimeUnit.MILLISECONDS);
    }

    private void onIdleTimeout() {
        if (noClientBeingServed()) {
            controller.onIdle();
        } else {
            // retry until all jabs have been processed
            scheduleIdleTimeout();
        }
    }

    public void stop() throws IOException {
        log.info("Stop requested");
        keepRunning.set(false);

        log.info("Stopped");
        selector.close();
        serverSocket.close();
    }

    private int getClientId(SocketChannel client) {
        return client.hashCode();
    }

    private Request createAndSaveRequest(int clientId) {
        Request request = requestFactory.create(clientId);
        return pendingRequestRepo.save(request);
    }

    private void sendResponse(SocketChannel client, Response response) {
        //ProcessingStats clientStats = statsRepo.getStatsByClient(getClientId(client));
        //String response = TimedRunner.run(() -> serializer.serialize(result), clientStats::logSerializationTime);

        String rawResponse = response.getData() + "\n";
        ByteBuffer ba = ByteBuffer.wrap(rawResponse.getBytes());
        try {
            if (DEBUG) {
                log.info(port + " sending response \"" + response.getData() + "\" to client " + getClientId(client));
            }
            client.write(ba);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPendingRequestRepo(RequestRepo pendingRequestRepo) {
        this.pendingRequestRepo = pendingRequestRepo;
    }

    public void setRequestFactory(RequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    public void setStatsRepo(ProcessingStatsRepo statsRepo) {
        this.statsRepo = statsRepo;
    }

    public boolean isReady() {
        return started.get() && keepRunning.get();
    }
}
