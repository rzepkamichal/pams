package org.simple.software;

import org.simple.software.server.core.ResultSerializer;
import org.simple.software.server.core.TagRemover;
import org.simple.software.server.core.TagRemoverImpl;
import org.simple.software.server.core.WoCoJob;
import org.simple.software.server.core.WoCoResult;
import org.simple.software.server.core.WordCounter;
import org.simple.software.server.core.WordCounterImpl;
import org.simple.software.server.protocol.RequestRepo;
import org.simple.software.server.protocol.RequestRepoImpl;
import org.simple.software.server.protocol.WoCoRequest;
import org.simple.software.server.protocol.WoCoResultSerializer;
import org.simple.software.server.stats.ProcessingStats;
import org.simple.software.server.stats.ProcessingStatsRepo;
import org.simple.software.server.stats.StatsRepoImpl;
import org.simple.software.server.stats.StatsWriter;
import org.simple.software.server.stats.SystemOutAvgStatsWriter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class WoCoServer {

    public static final char SEPARATOR = '$';

    private final RequestRepo pendingRequestRepo = new RequestRepoImpl();
    private final TagRemover tagRemover = new TagRemoverImpl();
    private final WordCounter wordCounter = new WordCounterImpl();
    private final ResultSerializer serializer = new WoCoResultSerializer();
    private final ProcessingStatsRepo statsRepo = new StatsRepoImpl();
    private final StatsWriter statsWriter = new SystemOutAvgStatsWriter(statsRepo);


    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.out.println("Usage: <listenaddress> <listenport> <cleaning> <threadcount>");
            System.exit(0);
        }

        String lAddr = args[0];
        int lPort = Integer.parseInt(args[1]);
        boolean cMode = Boolean.parseBoolean(args[2]);
        int threadCount = Integer.parseInt(args[3]);

        if (cMode == true) {
            //TODO: will have to implement cleaning from HTML tags
            System.out.println("FEATURE NOT IMPLEMENTED");
            System.exit(0);

        }

        if (threadCount > 1) {
            //TODO: will have to implement multithreading
            System.out.println("FEATURE NOT IMPLEMENTED");
            System.exit(0);

        }

        WoCoServer server = new WoCoServer();
        server.run(lAddr, lPort);
    }

    private void run(String addr, int port) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        InetSocketAddress myAddr = new InetSocketAddress(addr, port);
        serverSocket.bind(myAddr);
        serverSocket.configureBlocking(false);

        int ops = serverSocket.validOps();
        serverSocket.register(selector, ops, null);

        ByteBuffer bb = ByteBuffer.allocate(1024 * 1024);

        // Infinite loop..
        // Keep server running
        while (true) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);

                    System.out.println("Connection Accepted: " + client.getLocalAddress() + "\n");

                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    int clientId = client.hashCode();

                    WoCoRequest request = pendingRequestRepo.getByClientId(clientId).orElse(
                            pendingRequestRepo.save(createAndSaveRequest(clientId)));

                    bb.rewind();
                    int readCnt = client.read(bb);

                    if (readCnt > 0) {
                        String dataChunk = new String(bb.array(), 0, readCnt);
                        request.receiveData(dataChunk);

                        if (request.isDataReady()) {
                            pendingRequestRepo.removeByClientId(clientId);
                            WoCoJob job = createJob(request, client);
                            job.execute();
                        }

                    } else {
                        key.cancel();
                    }
                }
                iterator.remove();
            }
        }
    }

    private void sendResultToClient(SocketChannel client, WoCoResult result) {
        String response = serializer.serialize(result);

        ByteBuffer ba = ByteBuffer.wrap(response.getBytes());
        try {
            client.write(ba);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private WoCoRequest createAndSaveRequest(int clientId) {
        WoCoRequest request = new WoCoRequest(clientId, "");
        request.setReceiveDurationListener(statsRepo.getStatsByClient(clientId)::logDocReceiveTime);
        return pendingRequestRepo.save(request);
    }

    private WoCoJob createJob(WoCoRequest request, SocketChannel client) {
        WoCoJob job = new WoCoJob(request, tagRemover, wordCounter);
        job.setOnComplete(result -> sendResultToClient(client, result));

        ProcessingStats clientStats = statsRepo.getStatsByClient(request.getClientId());
        job.setTagRemovalTimeLogListener(clientStats::logDocCleaningTime);
        job.setWordCountTimeLogListener(clientStats::logWordCountTime);

        return job;
    }
}

