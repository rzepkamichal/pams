package org.simple.software.infrastructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.function.Function;

public class CSV {

    public static final String SEPARATOR = ",";

    public static <T> void writeToFile(String dirPath, String fileName, String dataHeader,
            Collection<T> data, Function<T, String> lineMapper) {

        File directory = new File(dirPath);

        if (!directory.exists()){
            directory.mkdirs();
        }

        File csvOutputFile = new File(directory.getAbsolutePath() + File.separator + fileName);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println(dataHeader);
            data.stream()
                    .map(lineMapper)
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
