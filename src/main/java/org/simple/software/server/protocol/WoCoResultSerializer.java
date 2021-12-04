package org.simple.software.server.protocol;

import org.simple.software.server.core.ResultSerializer;
import org.simple.software.server.core.WoCoResult;

import java.util.HashMap;
import java.util.Map;

public class WoCoResultSerializer implements ResultSerializer {

    /**
     * Returns a serialized version of the word count associated with the last
     * processed document for a given client. If not called before processing a new
     * document, the result is overwritten by the new one.
     *
     * @return
     */
    @Override
    public String serialize(WoCoResult result) {

        StringBuilder sb = new StringBuilder();
        Map<String, Integer> results = result.getResults();

        for (String key : results.keySet()) {
            sb.append(key + ",");
            sb.append(results.get(key) + ",");
        }

        sb.append("\n");
        return sb.substring(0);

    }
}
