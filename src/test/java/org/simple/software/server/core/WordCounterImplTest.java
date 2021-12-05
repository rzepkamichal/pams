package org.simple.software.server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordCounterImplTest {

    public static final String DOCUMENT = "asd qwe asd hijk HIJK";

    WordCounter wordCounter = new WordCounterImpl();

    @Test
    void counts_words() {
        WoCoResult result = wordCounter.countWords(DOCUMENT);

        assertSame(2, result.getCountFor("asd"));
        assertSame(1, result.getCountFor("qwe"));
        assertSame(2, result.getCountFor("hijk"));
        assertSame(0, result.getCountFor("abc"));
    }
}