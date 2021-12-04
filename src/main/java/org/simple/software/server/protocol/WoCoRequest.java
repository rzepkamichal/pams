package org.simple.software.server.protocol;

import org.simple.software.WoCoServer;
import org.simple.software.server.core.JobDataProvider;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;

public class WoCoRequest implements JobDataProvider {

    private final int clientId;
    private final StringBuilder buffer = new StringBuilder();
    private boolean requestSeparatorReceived = false;
    private final long creationTime = Clock.systemDefaultZone().millis();

    /**
     * Data which was sent after a request separator and so begins a new request
     */
    private String remainingData;

    public WoCoRequest(int clientId, String initialDataChunk) {
        this.clientId = clientId;
        receiveData(initialDataChunk);
    }

    public boolean isDataReady() {
        return requestSeparatorReceived;
    }

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
    public void receiveData(String dataChunk) {

        buffer.append(dataChunk);

        if (dataChunk.indexOf(Config.REQUEST_SEPARATOR) == -1) {
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
            System.out.println("more than one line: \n" + rest);
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }

            remainingData = rest;
        }

        buffer.deleteCharAt(indexNL);
        requestSeparatorReceived = true;
    }

    public Optional<String> getRemainingData() {
        return Optional.ofNullable(remainingData);
    }
}
