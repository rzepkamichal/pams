package org.simple.software.stats;

public interface StatsWriter {

    void writeForClient(int clientId);
    void writeTotal();
}
