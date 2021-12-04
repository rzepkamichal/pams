package org.simple.software.server.protocol;

import org.junit.jupiter.api.Test;
import org.simple.software.server.core.WoCoResult;

import static org.junit.jupiter.api.Assertions.*;

class WoCoResultSerializerTest {

    WoCoResultSerializer serializer = new WoCoResultSerializer();

    @Test
    void serializes_result() {
        WoCoResult result = new WoCoResult();
        result.addSingleResult("asd", 2);
        result.addSingleResult("hijk", 2);
        result.addSingleResult("efg", 1);



        String serialized = serializer.serialize(result);

        String expected = "hijk,2,asd,2,efg,1,\n";
        assertEquals(expected, serialized);

    }

}