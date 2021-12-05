package org.simple.software.server.core;

public class WordCounterImpl implements WordCounter {

    /**
     * Performs the word count on a document. It first converts the document to
     * lower case characters and then extracts words by considering "a-z" english characters
     * only (e.g., "alpha-beta" become "alphabeta"). The code breaks the text up into
     * words based on spaces.
     * @param document The document encoded as a string.
     */
    @Override
    public WoCoResult countWords(String document) {
        String ucLine = document.toLowerCase();
        StringBuilder asciiLine = new StringBuilder();

        char lastAdded = ' ';
        for (int i=0; i<document.length(); i++) {
            char cc = ucLine.charAt(i);
            if ((cc>='a' && cc<='z') || (cc==' ' && lastAdded!=' ')) {
                asciiLine.append(cc);
                lastAdded = cc;
            }
        }

        String[] words = asciiLine.toString().split(" ");

        WoCoResult result = new WoCoResult();

        for (String s : words) {
            result.addSingleResult(s);
        }

        return result;
    }
}
