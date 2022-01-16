package org.simple.software.stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

public class CSVStatsWriter implements StatsWriter {

    private final Logger log = Logger.getLogger(getClass().getName());

    private final ProcessingStatsRepo statsRepo;

    private String logsDirPath = "";

    private static final String FILE_RECEIVE_TIME = "receive-time.csv";
    private static final String FILE_TAG_REMOVAL_TIME = "tag-removal-time.csv";
    private static final String FILE_WORD_COUNT_TIME = "word-count-time.csv";
    private static final String FILE_SERIALIZE_TIME = "serialize-time.csv";

    private static final String ATTR_RECEIVE_TIME = "Receive time";
    private static final String ATTR_TAG_REMOVAL_TIME = "Tag removal time";
    private static final String ATTR_WORD_COUNT_TIME = "Word count time";
    private static final String ATTR_SERIALIZATION_TIME = "Response serialization time";

    public CSVStatsWriter(ProcessingStatsRepo statsRepo) {
        this.statsRepo = statsRepo;
    }

    public CSVStatsWriter(String logsDirPath, ProcessingStatsRepo statsRepo) {
        this.statsRepo = statsRepo;
        this.logsDirPath = logsDirPath;
    }

    @Override
    public void writeForClient(int clientId) {
        System.out.println("for client " + clientId);
    }

    @Override
    public void writeTotal() {
        log.info("Writing stats");
        writeSingleStats(FILE_RECEIVE_TIME, ATTR_RECEIVE_TIME,
                statsRepo.getAcummulativeStats().getReceiveTimeRecords());

        writeSingleStats(FILE_TAG_REMOVAL_TIME, ATTR_TAG_REMOVAL_TIME,
                statsRepo.getAcummulativeStats().getDocCleaningTimeRecords());

        writeSingleStats(FILE_WORD_COUNT_TIME, ATTR_WORD_COUNT_TIME,
                statsRepo.getAcummulativeStats().getWordCountTimeRecords());

        writeSingleStats(FILE_SERIALIZE_TIME, ATTR_SERIALIZATION_TIME,
                statsRepo.getAcummulativeStats().getSerializationTimeRecords());
        log.info("Writing stats: done");
    }

    void writeSingleStats(String fileName, String statName, List<Double> dataRecords) {
        File directory = new File(logsDirPath);

        if (!directory.exists()){
            directory.mkdirs();
        }


        File csvOutputFile = new File(directory.getAbsolutePath() + File.separator + fileName);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println(statName);
            dataRecords.stream()
                    .map(String::valueOf)
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
