package org.simple.software.server.core;

import org.simple.software.server.stats.ProcessingStatsRepo;
import org.simple.software.server.stats.TimedRunner;

import java.util.function.Consumer;

public class WoCoJob {

    private final int clientId;
    private final String document;

    private final TagRemover tagRemover;
    private final WordCounter wordCounter;

    private Consumer<WoCoResult> onComplete = __ -> {};
    private Consumer<Long> tagRemovalTimeLogListener = __ -> {};
    private Consumer<Long> wordCountTimeLogListener = __ -> {};

    public WoCoJob(int clientId, JobDataProvider dataProvider, TagRemover tagRemover, WordCounter wordCounter) {
        this.clientId = clientId;
        this.document = dataProvider.getData();
        this.tagRemover = tagRemover;
        this.wordCounter = wordCounter;
    }

    public int getClientId() {
        return clientId;
    }

    public void execute() {
        String withoutTags = removeTagsAndLogTime(tagRemover);
        WoCoResult result = countWordsAndLogTime(withoutTags, wordCounter);

        onComplete.accept(result);
    }

    public void setOnComplete(Consumer<WoCoResult> onComplete) {
        this.onComplete = onComplete;
    }

    public void setTagRemovalTimeLogListener(Consumer<Long> tagRemovalTimeLogListener) {
        this.tagRemovalTimeLogListener = tagRemovalTimeLogListener;
    }

    public void setWordCountTimeLogListener(Consumer<Long> wordCountTimeLogListener) {
        this.wordCountTimeLogListener = wordCountTimeLogListener;
    }

    private String removeTagsAndLogTime(TagRemover tagRemover) {
        String withoutTags = TimedRunner.run(() -> tagRemover.removeTags(document), tagRemovalTimeLogListener);
        return withoutTags;
    }

    private WoCoResult countWordsAndLogTime(String withoutTags, WordCounter wordCounter) {
        WoCoResult result = TimedRunner.run(() -> wordCounter.countWords(withoutTags), wordCountTimeLogListener);
        return result;
    }
}
