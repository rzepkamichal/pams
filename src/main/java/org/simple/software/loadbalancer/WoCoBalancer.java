package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.InMemoryTCPClientRepo;
import org.simple.software.infrastructure.TCPClientRepo;
import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.ThreadedJobExecutor;
import org.simple.software.stats.InMemoryStatsRepo;
import org.simple.software.stats.ProcessingStatsRepo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WoCoBalancer {

    private static final Logger log = Logger.getLogger(WoCoBalancer.class.getName());

    private final TCPServer server;

    // The backend services use a shared repo of TCPClients.
    // The load balancer maintains a separate TCPClient (socket) for each WoCoClient.
    // It enables forwarding WoCoClient requests on separate channels to the backend servers,
    // rather than using a shared channel. In this way, the WoCoServers can recognise connections from different clients.
    // Otherwise, the WoCoServers would get only a single-channel connection from the LB
    // and would muddle requests from different WoCoClients into one.
    private static final TCPClientRepo tcpClientRepo = new InMemoryTCPClientRepo();

    public WoCoBalancer(String address, int port, int threadNum, Collection<BackendService> services) {
        LBServerController controller = new LBServerController(
                new RoundRobinBalancer(services),
                new ThreadedJobExecutor(threadNum),
                tcpClientRepo);

        LBStatsRepo statsRepo = new LBStatsRepo();
        controller.setStatsRepo(statsRepo);

        server = new TCPServer(address, port, controller);
    }

    public static void main(String[] args) {

        // simpler format of logs
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        if (args.length != 4) {
            System.out.println("Usage: <listenaddress> <listenport> <backend_servers_config_file> <threadcount>");
            System.exit(0);
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);
        String configFile = args[2];
        int threadNum = Integer.parseInt(args[3]);

        List<BackendService> backendServices = createBackendServicesFromConfig(configFile);

        WoCoBalancer balancer = new WoCoBalancer(address, port, threadNum, backendServices);
        balancer.run();
    }

    private static List<BackendService> createBackendServicesFromConfig(String configFilePath) {
        try {

            return ConfigReader.readServersFromFile(configFilePath)
                    .stream()
                    .peek(serv -> log.info("Registered backend service: " + serv.getHost() + ":" + serv.getPort()))
                    .map(serv -> new WoCoService(serv.getHost(), serv.getPort(), tcpClientRepo))
                    .collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
