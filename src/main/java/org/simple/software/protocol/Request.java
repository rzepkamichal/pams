package org.simple.software.protocol;

public interface Request {

    int getClientId();
    String getData();
    void receiveData(String dataChunk);
    boolean isDataReady();

    long getReceiveDuration();

    long getReceiveTime();
    long getReadyTime();
}
