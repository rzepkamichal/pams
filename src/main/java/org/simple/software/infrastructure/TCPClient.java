package org.simple.software.infrastructure;

import org.simple.software.protocol.Const;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPClient {

    private final String serverAddress;
    private final int serverPort;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private Socket sHandle;
    private BufferedReader sInput;
    private BufferedWriter sOutput;

    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public String send(String data) throws IOException {

        if (!initialized.get()) {
            initialize();
        }

        // remove all characters from the data, which belong to the protocol's semantics
        data = data.replaceAll(Const.REQUEST_SEPARATOR_REGEXP, "");

        sOutput.write(data);
        sOutput.write(Const.REQUEST_SEPARATOR);
        sOutput.flush();
        String response = sInput.readLine();

        return response;
    }

    private void initialize() {
        try {
            this.sHandle = new Socket(serverAddress, serverPort);
            this.sInput = new BufferedReader(new InputStreamReader(sHandle.getInputStream()));
            this.sOutput = new BufferedWriter(new OutputStreamWriter(sHandle.getOutputStream()));
            initialized.set(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        if (!initialized.get()) {
            return;
        }

        try {
            sHandle.close();
            initialized.set(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
