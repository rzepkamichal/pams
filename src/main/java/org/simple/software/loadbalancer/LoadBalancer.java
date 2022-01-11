package org.simple.software.loadbalancer;

public interface LoadBalancer {

    BackendService getNext();
}
