package org.simple.software.server;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.JobExecutor;
import org.simple.software.infrastructure.JobRepo;
import org.simple.software.server.core.JobRepoImpl;
import org.simple.software.server.core.ResultSerializer;
import org.simple.software.server.core.TagRemover;
import org.simple.software.server.core.TagRemoverImpl;
import org.simple.software.server.core.WoCoServerController;
import org.simple.software.server.core.WordCounter;
import org.simple.software.server.core.WordCounterImpl;
import org.simple.software.infrastructure.ThreadedJobExecutor;
import org.simple.software.server.core.WoCoResultSerializer;
import org.simple.software.server.stats.ProcessingStatsRepo;
import org.simple.software.server.stats.StatsRepoImpl;
import org.simple.software.server.stats.StatsWriter;
import org.simple.software.server.stats.SystemOutAvgStatsWriter;

import java.io.IOException;

public class WoCoServer {

    public static final char SEPARATOR = '$';

    private final TagRemover tagRemover;
    private final WordCounter wordCounter = new WordCounterImpl();
    private final ResultSerializer serializer = new WoCoResultSerializer();
    private final ProcessingStatsRepo statsRepo = new StatsRepoImpl();
    private final StatsWriter statsWriter = new SystemOutAvgStatsWriter(statsRepo);
    private final JobExecutor jobExecutor;
    private final JobRepo jobRepo = new JobRepoImpl();

    private final TCPServer tcpServer;

    public WoCoServer(String address, int port, int threadNum, boolean removeTags) {
        this.jobExecutor = new ThreadedJobExecutor(threadNum);

        // if tag removal disabled, then use just a default "input repeater"
        this.tagRemover = removeTags ? new TagRemoverImpl() : str -> str;

        ServerController controller = new WoCoServerController(jobExecutor, tagRemover, wordCounter,
                serializer, statsRepo, statsWriter);

        tcpServer = new TCPServer(address, port, controller);
        tcpServer.setStatsRepo(statsRepo);
    }


    public static void main(String[] args) throws IOException {

        // simpler format of logs
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        if (args.length != 4) {
            System.out.println("Usage: <listenaddress> <listenport> <cleaning> <threadcount>");
            System.exit(0);
        }

        String lAddr = args[0];
        int lPort = Integer.parseInt(args[1]);
        boolean cMode = Boolean.parseBoolean(args[2]);
        int threadCount = Integer.parseInt(args[3]);

        WoCoServer server = new WoCoServer(lAddr, lPort, threadCount, cMode);
        server.run();
    }

    public void run() {
        try {
            tcpServer.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            tcpServer.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isReady() {
        return tcpServer.isReady();
    }


}

