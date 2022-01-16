package org.simple.software.server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexpTagRemoverTest {

    RegexpTagRemover tagRemover = new RegexpTagRemover();

    @Test
    void removes_tags__case1() {
        String html = "<p><b>foo</p></b>";

        String result = tagRemover.removeTags(html);

        assertEquals("foo", result);
    }

    @Test
    void removes_tags__case2() {
        String html = "<p><b>foo</p></b> <td>bar</td>";

        String result = tagRemover.removeTags(html);

        assertEquals("foo bar", result);
    }

    @Test
    void spares_text_attrib__case1() {
        String html = "<a href=\"xyz\" title=\"FooBar\">foobar</a>";

        String result = tagRemover.removeTags(html);

        assertEquals("FooBar foobar", result);
    }

    @Test
    void spares_text_attrib__case2() {
        String html = "<a href=\"xyz\" title=\"FooBar\">foobar</a> <head title=\"FizzBuzz\">buzz</head>";

        String result = tagRemover.removeTags(html);

        assertEquals("FooBar foobar FizzBuzz buzz", result);
    }



}