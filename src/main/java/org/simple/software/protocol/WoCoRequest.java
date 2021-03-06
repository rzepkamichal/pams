package org.simple.software.protocol;

import org.simple.software.server.WoCoServer;

import java.util.function.Consumer;

public class WoCoRequest implements Request {

    private final int clientId;
    private final StringBuilder buffer = new StringBuilder();
    private boolean requestSeparatorReceived = false;

    private final long creationTime = System.nanoTime();
    private long readyTime = 0L;

    private long receiveTime;

    private Consumer<Long> receiveDurationListener = __ -> {};

    WoCoRequest(int clientId, String initialDataChunk) {
        this.clientId = clientId;
        receiveData(initialDataChunk);
    }

    @Override
    public boolean isDataReady() {
        return requestSeparatorReceived;
    }

    @Override
    public int getClientId() {
        return clientId;
    }

    @Override
    public String getData() {

        if (!requestSeparatorReceived) {
            throw new IllegalStateException("Request separator sign not yet received");
        }

        return buffer.toString();
    }

    /**
     * This function handles data received from a specific client (TCP connection).
     * Internally it will check if the buffer associated with the client has a full
     * document in it (based on the SEPARATOR). If yes, it will process the document and
     * return true, otherwise it will add the data to the buffer
     *
     * @param dataChunk
     * @return A document has been processed or not.
     */
    @Override
    public void receiveData(String dataChunk) {

        if (dataChunk.isEmpty()) {
            return;
        }

        buffer.append(dataChunk);

        if (dataChunk.indexOf(Const.REQUEST_SEPARATOR) == -1) {
            return;
        }

        //we have at least one line
        String bufData = buffer.toString();
        int indexNL = bufData.indexOf(WoCoServer.SEPARATOR);
        String rest = (bufData.length() > indexNL + 1) ? bufData.substring(indexNL + 1) : null;

        if (indexNL == 0) {
            System.out.println("SEP@" + indexNL + " bufdata:\n" + bufData);
        }

        if (rest != null) {
            throw new IllegalStateException("More than one line: \n" + rest);
        }

        buffer.deleteCharAt(indexNL);
        requestSeparatorReceived = true;
        receiveTime = System.nanoTime() - creationTime;
        readyTime = System.nanoTime();
    }

    @Override
    public long getReceiveDuration() {
        return receiveTime;
    }

    @Override
    public long getReceiveTime() {
        return creationTime;
    }

    @Override
    public long getReadyTime() {
        return readyTime;
    }
}
