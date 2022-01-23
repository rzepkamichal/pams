package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.StringUtils;
import org.simple.software.infrastructure.TCPClient;
import org.simple.software.infrastructure.TCPClientRepo;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;
import org.simple.software.infrastructure.JobExecutor;
import org.simple.software.stats.InMemoryStatsRepo;
import org.simple.software.stats.ProcessingStatsRepo;
import org.simple.software.stats.IntervalMeasurementService;
import org.simple.software.stats.StatsWriter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

class LBServerController implements ServerController {

    private final LoadBalancer loadBalancer;
    private final JobExecutor jobExecutor;
    private final TCPClientRepo tcpClientRepo;

    private ProcessingStatsRepo<LBStats> statsRepo = new InMemoryStatsRepo<>();
    private IntervalMeasurementService measurementService = IntervalMeasurementService.EMPTY;
    private StatsWriter statsWriter = StatsWriter.EMPTY;

    private boolean firstRequest = true;

    private final Map<Integer, BackendService> serviceMap = new ConcurrentHashMap<>();

    public LBServerController(LoadBalancer loadBalancer, JobExecutor jobExecutor, TCPClientRepo tcpClientRepo) {
        this.loadBalancer = loadBalancer;
        this.jobExecutor = jobExecutor;
        this.tcpClientRepo = tcpClientRepo;
    }

    @Override
    public CompletableFuture<Response> handle(Request request) {

        if (firstRequest) {
            firstRequest = false;
            measurementService.start();
        }

        if (serviceMap.get(request.getClientId()) == null) {
            BackendService service = loadBalancer.getNext();
            serviceMap.put(request.getClientId(), service);
        }

        BackendService service = serviceMap.get(request.getClientId());
        CompletableFuture<Response> futureResponse = new CompletableFuture<>();

        logTimeSpentLoadBalancing(request);

        jobExecutor.execute(() -> {
            Response response = service.serve(request);
            long systemResponseTime = System.nanoTime() - request.getReceiveTime();
            statsRepo.getStatsByClient(request.getClientId()).logTime(LBStats.SYSTEM_RESPONSE_TIME, systemResponseTime);

            if (!StringUtils.isBlank(response.getData())) {
                serviceMap.remove(request.getClientId());
                futureResponse.complete(response);
            }

        });

        return futureResponse;
    }

    @Override
    public void onDisconnect(int clientId) {
        // also close the WoCoServer-connection associated with the disconnected client
        tcpClientRepo.get(clientId)
                .ifPresent(tcpClient -> {
                    tcpClient.close();
                    tcpClientRepo.removeByClientId(clientId);
                });
    }

    @Override
    public void onIdle() {
        // also close all WoCoServer-connections maintained for each client
        tcpClientRepo.getAll().forEach(TCPClient::close);
        tcpClientRepo.removeAll();

        measurementService.stop();
        statsWriter.writeTotal();
    }

    private void logTimeSpentLoadBalancing(Request request) {
        long total = System.nanoTime() - request.getReadyTime() + request.getReceiveDuration();
        statsRepo.getStatsByClient(request.getClientId()).logTime(LBStats.TIME_SPENT_IN_LB, total);
    }

    public void setStatsRepo(ProcessingStatsRepo<LBStats> statsRepo) {
        this.statsRepo = statsRepo;
    }

    public void setMeasurementService(IntervalMeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    public void setStatsWriter(StatsWriter statsWriter) {
        this.statsWriter = statsWriter;
    }
}
