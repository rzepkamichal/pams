package org.simple.software.server.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WoCoRequestTest {

    public static final char SEPARATOR = Config.REQUEST_SEPARATOR;

    @Test
    void receives_data() {
        String data = "abcdefg";

        WoCoRequest req = new WoCoRequest(1, data + SEPARATOR);

        assertEquals(data,req.getData());
        assertTrue(req.isDataReady());
    }

    @Test
    void receives_data_in_chunks() {
        String data1 = "abcdefg";
        String data2 = "hijklmn";

        WoCoRequest req = new WoCoRequest(1, data1);
        req.receiveData(data2 + SEPARATOR);

        assertEquals(data1 + data2, req.getData());
        assertTrue(req.isDataReady());
    }

    @Test
    void not_ready__when_separator_not_yet_received() {
        String data1 = "abcdefg";
        String data2 = "hijklmn";

        WoCoRequest req = new WoCoRequest(1, data1);
        req.receiveData(data2);

        assertFalse(req.isDataReady());
    }

    @Test
    void throws_error__when_getting_data_but_separator_not_yet_received() {
        String data1 = "abcdefg";
        String data2 = "hijklmn";

        WoCoRequest req = new WoCoRequest(1, data1);
        req.receiveData(data2);

        assertThrows(IllegalStateException.class, req::getData);
    }

}