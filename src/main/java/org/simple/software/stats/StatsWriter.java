package org.simple.software.stats;

public interface StatsWriter {

    void writeForClient(int clientId);
    void writeTotal();

    StatsWriter EMPTY = new StatsWriter() {
        @Override
        public void writeForClient(int clientId) {

        }

        @Override
        public void writeTotal() {

        }
    };


}
