package org.simple.software.protocol;

public interface Response {

    String getData();

    static Response of(String data) {
        return () -> data;
    }
}
