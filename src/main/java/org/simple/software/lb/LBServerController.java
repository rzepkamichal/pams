package org.simple.software.lb;

import org.simple.software.infrastructure.ServerController;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;
import org.simple.software.server.core.JobExecutor;

import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

public class LBServerController implements ServerController {

    private final LoadBalancer loadBalancer;
    private final JobExecutor jobExecutor;

    public LBServerController(LoadBalancer loadBalancer, JobExecutor jobExecutor) {
        this.loadBalancer = loadBalancer;
        this.jobExecutor = jobExecutor;
    }

    @Override
    public CompletableFuture<Response> handle(Request request) {
        CompletableFuture<Response> futureResponse = new CompletableFuture<>();
        BackendService service = loadBalancer.getNext();

        jobExecutor.execute(() -> {
            Response response = service.serve(request);
            futureResponse.complete(response);
        });

        return futureResponse;
    }

    @Override
    public void onDisconnect(SocketChannel client) {
        ServerController.super.onDisconnect(client);
    }
}
