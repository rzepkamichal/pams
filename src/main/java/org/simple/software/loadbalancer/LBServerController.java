package org.simple.software.loadbalancer;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.infrastructure.TCPClient;
import org.simple.software.infrastructure.TCPClientRepo;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;
import org.simple.software.infrastructure.JobExecutor;
import org.simple.software.stats.InMemoryStatsRepo;
import org.simple.software.stats.ProcessingStatsRepo;

import java.util.concurrent.CompletableFuture;

class LBServerController implements ServerController {

    private final LoadBalancer loadBalancer;
    private final JobExecutor jobExecutor;
    private final TCPClientRepo tcpClientRepo;

    private ProcessingStatsRepo<LBStats> statsRepo = new InMemoryStatsRepo<>();

    public LBServerController(LoadBalancer loadBalancer, JobExecutor jobExecutor, TCPClientRepo tcpClientRepo) {
        this.loadBalancer = loadBalancer;
        this.jobExecutor = jobExecutor;
        this.tcpClientRepo = tcpClientRepo;
    }

    @Override
    public CompletableFuture<Response> handle(Request request) {
        CompletableFuture<Response> futureResponse = new CompletableFuture<>();
        BackendService service = loadBalancer.getNext();

        logTimeSpentLoadBalancing(request);

        jobExecutor.execute(() -> {
            Response response = service.serve(request);
            futureResponse.complete(response);
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
    }

    private void logTimeSpentLoadBalancing(Request request) {
        long total = System.nanoTime() - request.getReadyTime() + request.getReceiveDuration();
        statsRepo.getStatsByClient(request.getClientId()).logTime(LBStats.TIME_SPENT_IN_LB, total);
    }

    public void setStatsRepo(ProcessingStatsRepo<LBStats> statsRepo) {
        this.statsRepo = statsRepo;
    }
}
