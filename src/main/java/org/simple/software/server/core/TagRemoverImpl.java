package org.simple.software.server.core;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagRemoverImpl implements TagRemover {

    private static final String HTML_TAG = "(<)(.*?)(>)";

    private static final String TITLE_ATTR = "(title=\")(.*?)(\")";
    private static final int TITLE_ATTR_VALUE_POS_IN_REGEXP_GROUP = 2;

    @Override
    public String removeTags(String document) {

        Pattern tagPattern = Pattern.compile(HTML_TAG);
        Matcher tagMatcher = tagPattern.matcher(document);

        String match;
        String replacement;
        int lastMatchIndex = 0;

        // start with empty string
        StringBuilder output = new StringBuilder();

        // for each match:
        // 1. add substring before match to output
        // 2. convert match into desired form and add to output
        // 3. repeat for all matches
        while (tagMatcher.find()) {
            match = tagMatcher.group();
            replacement = extractTitle(match).orElse("");

            output.append(document, lastMatchIndex, tagMatcher.start())
                    .append(replacement);

            lastMatchIndex = tagMatcher.end();
        }

        // add remaining part of document to output
        if (lastMatchIndex < document.length()) {
            output.append(document, lastMatchIndex, document.length());
        }

        return output.toString();

    }

    private Optional<String> extractTitle(String str) {
        Pattern titlePattern = Pattern.compile(TITLE_ATTR);
        Matcher titleMatcher = titlePattern.matcher(str);

        if (titleMatcher.find()) {
            return Optional.of(titleMatcher.group(TITLE_ATTR_VALUE_POS_IN_REGEXP_GROUP) + " ");
        }

        return Optional.empty();
    }
}
