package org.simple.software.loadbalancer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class ConfigReader {

    public static Set<ServerAddress> readServersFromFile(String path) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        return reader.lines()
                .map(ConfigReader::lineToAddress)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<ServerAddress> lineToAddress(String line) {
        if (line.startsWith("#")) {
            return Optional.empty();
        }

        String[] splitted = line.split(":");
        String host = splitted[0];
        int port = Integer.parseInt(splitted[1]);

        return Optional.of(new ServerAddress(host, port));
    }
}
