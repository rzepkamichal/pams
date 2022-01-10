package org.simple.software.lb;

public interface LoadBalancer {

    BackendService getNext();
}
