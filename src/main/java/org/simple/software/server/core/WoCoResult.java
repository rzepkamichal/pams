package org.simple.software.server.core;

import java.util.HashMap;

public class WoCoResult {

    private HashMap<String, Integer> results = new HashMap<>();

    void addSingleResult(String word, int count) {
        results.put(word, count);
    }

}
