package org.simple.software.loadbalancer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;
import java.util.stream.Collectors;

class ConfigReader {

    public static Set<ServerAddress> readServersFromFile(String path) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        return reader.lines()
                .map(ConfigReader::lineToAddress)
                .collect(Collectors.toSet());
    }

    private static ServerAddress lineToAddress(String line) {
        String[] splitted = line.split(":");
        String host = splitted[0];
        int port = Integer.parseInt(splitted[1]);

        return new ServerAddress(host, port);
    }
}
