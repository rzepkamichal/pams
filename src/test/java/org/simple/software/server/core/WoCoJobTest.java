package org.simple.software.server.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.simple.software.protocol.Request;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class WoCoJobTest {

    public static final String DOCUMENT = "asd efg hijk asd <p>hijk</p>";
    public static final String WITHOUT_TAGS = "asd efg hijk asd hijk";

    @Mock
    TagRemover tagRemover;

    @Mock
    WordCounter wordCounter;

    @Mock
    Request jobData;

    WoCoResult result;

    @BeforeEach
    void setup() {

        MockitoAnnotations.initMocks(this);

        when(tagRemover.removeTags(DOCUMENT))
                .thenReturn(WITHOUT_TAGS);

        result = new WoCoResult();
        result.addSingleResult("asd");
        result.addSingleResult("hijk");
        result.addSingleResult("asd");
        result.addSingleResult("hijk");
        result.addSingleResult("efg");

        when(wordCounter.countWords(WITHOUT_TAGS))
                .thenReturn(result);
    }

    @Test
    void removes_tags_and_counts_words() {
        List<WoCoResult> results = new LinkedList<>();
        when(jobData.getClientId()).thenReturn(1);
        when(jobData.getData()).thenReturn(DOCUMENT);
        WoCoJob job = new WoCoJob(jobData, tagRemover, wordCounter);
        job.setOnComplete(results::add);

        job.execute();

        // if the proper mocks have been called in the right order,
        // then the expected and delivered result should be same
        assertEquals(result, results.get(0));
    }

    @Test
    void notifies_about_time_log() {
        List<Long> tagRemovalLogs = new LinkedList<>();
        List<Long> wordCountLogs = new LinkedList<>();
        when(jobData.getClientId()).thenReturn(1);
        when(jobData.getData()).thenReturn(DOCUMENT);
        WoCoJob job = new WoCoJob(jobData, tagRemover, wordCounter);
        job.setTagRemovalTimeLogListener(tagRemovalLogs::add);
        job.setWordCountTimeLogListener(wordCountLogs::add);

        job.execute();

        assertSame(1, tagRemovalLogs.size());
        assertSame(1, wordCountLogs.size());
    }


}
