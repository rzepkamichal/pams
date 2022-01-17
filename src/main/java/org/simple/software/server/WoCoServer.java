package org.simple.software.server;

import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.JobExecutor;
import org.simple.software.server.core.ResultSerializer;
import org.simple.software.server.core.ServerStatsRepo;
import org.simple.software.server.core.TagRemover;
import org.simple.software.server.core.RegexpTagRemover;
import org.simple.software.server.core.WoCoServerController;
import org.simple.software.server.core.WordCounter;
import org.simple.software.server.core.WordCounterImpl;
import org.simple.software.infrastructure.ThreadedJobExecutor;
import org.simple.software.server.core.WoCoResultSerializer;
import org.simple.software.server.core.ServerStatsCSVWriter;
import org.simple.software.stats.DefaultIntervalMeasurementService;
import org.simple.software.stats.IntervalMeasurementService;
import org.simple.software.stats.StatsWriter;

import java.io.File;
import java.io.IOException;

public class WoCoServer {

    public static final char SEPARATOR = '$';
    public static final int MEASUREMENT_INTERVAL_MS = 200;

    private final TagRemover tagRemover;
    private final WordCounter wordCounter = new WordCounterImpl();
    private final ResultSerializer serializer = new WoCoResultSerializer();
    private final ServerStatsRepo statsRepo = new ServerStatsRepo();
    private final StatsWriter statsWriter;
    private final JobExecutor jobExecutor;

    private final IntervalMeasurementService measurementService;

    private final TCPServer tcpServer;

    public WoCoServer(String address, int port, int threadNum, boolean removeTags) {
        this.jobExecutor = new ThreadedJobExecutor(threadNum);

        // if tag removal disabled, then use just a default "input repeater"
        this.tagRemover = removeTags ? new RegexpTagRemover() : str -> str;

        String logsDirPath = "." + File.separator + "log-" + address + "-" + port;
        statsWriter = new ServerStatsCSVWriter(logsDirPath, statsRepo);

        measurementService = new DefaultIntervalMeasurementService(statsRepo, MEASUREMENT_INTERVAL_MS);

        WoCoServerController controller = new WoCoServerController(jobExecutor, tagRemover,
                wordCounter, serializer);

        controller.setStatsRepo(statsRepo);
        controller.setMeasurementSvc(measurementService);
        controller.setStatsWriter(statsWriter);

        tcpServer = new TCPServer(address, port, controller);
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
            measurementService.start();
            tcpServer.run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            tcpServer.stop();
            measurementService.stop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isReady() {
        return tcpServer.isReady();
    }


}

