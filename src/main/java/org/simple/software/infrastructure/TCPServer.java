package org.simple.software.infrastructure;

public class TCPServer {

    private final String address;
    private final int port;
    private final ServerController controller;

    public TCPServer(String address, int port, ServerController controller) {
        this.address = address;
        this.port = port;
        this.controller = controller;
    }
}
