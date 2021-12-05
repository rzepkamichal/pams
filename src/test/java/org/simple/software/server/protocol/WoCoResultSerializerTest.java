package org.simple.software.server.protocol;

import org.junit.jupiter.api.Test;
import org.simple.software.server.core.WoCoResult;

import static org.junit.jupiter.api.Assertions.*;

class WoCoResultSerializerTest {

    WoCoResultSerializer serializer = new WoCoResultSerializer();

    @Test
    void serializes_result() {
        WoCoResult result = new WoCoResult();
        result.addSingleResult("asd");
        result.addSingleResult("asd");
        result.addSingleResult("hijk");
        result.addSingleResult("hijk");
        result.addSingleResult("efg");



        String serialized = serializer.serialize(result);

        String expected = "hijk,2,asd,2,efg,1,\n";
        assertEquals(expected, serialized);

    }

}