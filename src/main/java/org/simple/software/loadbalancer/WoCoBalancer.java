package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.TCPServer;
import org.simple.software.infrastructure.ThreadedJobExecutor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WoCoBalancer {

    private static final Logger log = Logger.getLogger(WoCoBalancer.class.getName());

    private final TCPServer server;

    public WoCoBalancer(String address, int port, int threadNum, Collection<BackendService> services) {
        ServerController controller = new LBServerController(
                new RoundRobinBalancer(services),
                new ThreadedJobExecutor(threadNum)
        );

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
                    .map(serv -> new WoCoService(serv.getHost(), serv.getPort()))
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
