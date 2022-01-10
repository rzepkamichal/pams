package org.simple.software.protocol;

public class WoCoRequestFactory implements RequestFactory {

    @Override
    public Request create(int clientId) {
        return new WoCoRequest(clientId, "");
    }
}
