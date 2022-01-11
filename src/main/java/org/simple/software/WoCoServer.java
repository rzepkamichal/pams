package org.simple.software;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.WoCoRequestFactory;
import org.simple.software.infrastructure.JobExecutor;
import org.simple.software.infrastructure.JobRepo;
import org.simple.software.server.core.JobRepoImpl;
import org.simple.software.server.core.ResultSerializer;
import org.simple.software.server.core.TagRemover;
import org.simple.software.server.core.TagRemoverImpl;
import org.simple.software.server.core.WoCoJob;
import org.simple.software.server.core.WoCoResult;
import org.simple.software.server.core.WordCounter;
import org.simple.software.server.core.WordCounterImpl;
import org.simple.software.infrastructure.ThreadedJobExecutor;
import org.simple.software.infrastructure.RequestRepo;
import org.simple.software.infrastructure.RequestRepoImpl;
import org.simple.software.protocol.WoCoRequest;
import org.simple.software.server.core.WoCoResultSerializer;
import org.simple.software.server.stats.ProcessingStats;
import org.simple.software.server.stats.ProcessingStatsRepo;
import org.simple.software.server.stats.StatsRepoImpl;
import org.simple.software.server.stats.StatsWriter;
import org.simple.software.server.stats.SystemOutAvgStatsWriter;
import org.simple.software.server.stats.TimedRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WoCoServer {

    public static final char SEPARATOR = '$';
    public static final boolean PRINT_CLIENT_STATS = false;

    private final RequestRepo pendingRequestRepo = new RequestRepoImpl();
    private final TagRemover tagRemover;
    private final WordCounter wordCounter = new WordCounterImpl();
    private final ResultSerializer serializer = new WoCoResultSerializer();
    private final ProcessingStatsRepo statsRepo = new StatsRepoImpl();
    private final StatsWriter statsWriter = new SystemOutAvgStatsWriter(statsRepo);
    private final JobExecutor jobExecutor;
    private final JobRepo jobRepo = new JobRepoImpl();

    private final Set<Integer> servicedClients = new HashSet<>();

    public WoCoServer(int threadNum, boolean removeTags) {
        this.jobExecutor = new ThreadedJobExecutor(threadNum);

        // if tag removal disabled, then use just a default "input repeater"
        this.tagRemover = removeTags ? new TagRemoverImpl() : str -> str;
    }


    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.out.println("Usage: <listenaddress> <listenport> <cleaning> <threadcount>");
            System.exit(0);
        }

        String lAddr = args[0];
        int lPort = Integer.parseInt(args[1]);
        boolean cMode = Boolean.parseBoolean(args[2]);
        int threadCount = Integer.parseInt(args[3]);

        WoCoServer server = new WoCoServer(threadCount, cMode);
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

                    System.out.println("Connection Accepted: " + client.getLocalAddress());
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
                            pendingRequestRepo.save(request.fromRemainingData());
//                            WoCoJob job = createJob(request, client);
//                            jobRepo.save(job);
//                            jobExecutor.execute(job);
                        }

                    } else {
                        key.cancel();
                        pendingRequestRepo.removeByClientId(clientId);
                        servicedClients.remove(clientId);

                        if (PRINT_CLIENT_STATS) {
                            writeStatsForClient(clientId);
                        }

                        if (noClientBeingServed()) {
                            scheduleTotalStatsPrintout();
                        }
                    }
                }
                iterator.remove();
            }
        }
    }

    private int getClientId(SocketChannel client) {
        return client.hashCode();
    }

    private Consumer<WoCoResult> onResultCalculated(SocketChannel client) {
        return result -> sendResultToClient(client, result);
    }

    private void sendResultToClient(SocketChannel client, WoCoResult result) {
        ProcessingStats clientStats = statsRepo.getStatsByClient(getClientId(client));
        String response = TimedRunner.run(() -> serializer.serialize(result), clientStats::logSerializationTime);

        ByteBuffer ba = ByteBuffer.wrap(response.getBytes());
        try {
            client.write(ba);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Request createAndSaveRequest(int clientId) {
        Request request = (new WoCoRequestFactory()).create(clientId);
        return pendingRequestRepo.save(request);
    }

    private WoCoJob createJob(WoCoRequest request, SocketChannel client) {
        WoCoJob job = new WoCoJob(request, tagRemover, wordCounter);
        job.setOnComplete(onResultCalculated(client));

        ProcessingStats clientStats = statsRepo.getStatsByClient(request.getClientId());
        job.setTagRemovalTimeLogListener(clientStats::logDocCleaningTime);
        job.setWordCountTimeLogListener(clientStats::logWordCountTime);

        return job;
    }

    private void writeStatsForClient(int clientId) {
        statsWriter.writeForClient(clientId);
    }

    private void printTotalStatsIfDone() {
        if (noClientBeingServed() && jobRepo.allJobsProcessed()) {
            statsWriter.writeTotal();
        } else {
            // retry until all jabs have been processed
            scheduleTotalStatsPrintout();
        }
    }

    private void scheduleTotalStatsPrintout() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::printTotalStatsIfDone, 200, TimeUnit.MILLISECONDS);
    }

    private boolean noClientBeingServed() {
        return servicedClients.isEmpty();
    }
}

