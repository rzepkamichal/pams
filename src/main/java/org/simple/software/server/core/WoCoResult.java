package org.simple.software.server.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WoCoResult {

    private final Map<String, Integer> results = new HashMap<>();

    public void addSingleResult(String word) {
        if (results.containsKey(word)) {
            results.put(word, results.get(word) + 1);
        } else {
            results.put(word, 1);
        }
    }

    public Map<String, Integer> getResults() {
        return Collections.unmodifiableMap(results);
    }

    public int getCountFor(String word) {
        return results.getOrDefault(word, 0);
    }
}
