package org.simple.software.server.stats;

public interface StatsWriter {

    void writeForClient(int clientId);
    void writeTotal();
}
