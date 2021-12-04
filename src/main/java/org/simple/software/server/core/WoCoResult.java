package org.simple.software.server.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WoCoResult {

    private final Map<String, Integer> results = new HashMap<>();

    public void addSingleResult(String word, int count) {
        results.put(word, count);
    }

    public Map<String, Integer> getResults() {
        return Collections.unmodifiableMap(results);
    }
}
