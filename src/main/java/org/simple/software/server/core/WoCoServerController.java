package org.simple.software.server.core;

import org.simple.software.infrastructure.JobExecutor;
import org.simple.software.infrastructure.ServerController;
import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;
import org.simple.software.stats.ProcessingStats;
import org.simple.software.stats.ProcessingStatsRepo;
import org.simple.software.stats.StatsWriter;
import org.simple.software.stats.TimedRunner;

import java.util.concurrent.CompletableFuture;

public class WoCoServerController implements ServerController {

    public static final boolean PRINT_CLIENT_STATS = false;

    private final JobExecutor jobExecutor;
    private final TagRemover tagRemover;
    private final WordCounter wordCounter;
    private final ResultSerializer serializer;
    private final ProcessingStatsRepo statsRepo;
    private final StatsWriter statsWriter;

    public WoCoServerController(JobExecutor jobExecutor, TagRemover tagRemover, WordCounter wordCounter,
                                ResultSerializer serializer, ProcessingStatsRepo statsRepo, StatsWriter statsWriter) {
        this.jobExecutor = jobExecutor;
        this.tagRemover = tagRemover;
        this.wordCounter = wordCounter;
        this.serializer = serializer;
        this.statsRepo = statsRepo;
        this.statsWriter = statsWriter;
    }

    @Override
    public CompletableFuture<Response> handle(Request request) {
        CompletableFuture<Response> futureResponse = new CompletableFuture<>();

        WoCoJob job = createJob(request);
        job.setOnComplete(result -> futureResponse.complete(resultToResponse(result)));

        jobExecutor.execute(job);

        return futureResponse;
    }

    @Override
    public void onDisconnect(int clientId) {
        if (PRINT_CLIENT_STATS) {
            statsWriter.writeForClient(clientId);
        }
    }

    @Override
    public void onIdle() {
        statsWriter.writeTotal();
    }

    private WoCoJob createJob(Request request) {
        WoCoJob job = new WoCoJob(request, tagRemover, wordCounter);

        ProcessingStats clientStats = statsRepo.getStatsByClient(request.getClientId());
        job.setTagRemovalTimeLogListener(clientStats::logDocCleaningTime);
        job.setWordCountTimeLogListener(clientStats::logWordCountTime);

        return job;
    }


    private Response resultToResponse(WoCoResult result) {
        ProcessingStats clientStats = statsRepo.getStatsByClient(result.getClientId());
        String responseData = TimedRunner.run(() -> serializer.serialize(result), clientStats::logSerializationTime);

        return Response.of(responseData);
    }
}
