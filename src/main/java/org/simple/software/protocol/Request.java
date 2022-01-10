package org.simple.software.protocol;

public interface Request {

    int getClientId();
    String getData();
    void receiveData(String dataChunk);
    boolean isDataReady();
    Request fromRemainingData();
}
