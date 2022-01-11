package org.simple.software.loadbalancer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundRobinBalancerTest {

    @Mock
    BackendService service1;

    @Mock
    BackendService service2;

    @Mock
    BackendService service3;

    LoadBalancer loadBalancer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        loadBalancer = new RoundRobinBalancer(List.of(service1, service2, service3));
    }

    @Test
    void returns_services_in_cycle() {
        BackendService service = loadBalancer.getNext();
        assertSame(service1, service);

        service = loadBalancer.getNext();
        assertSame(service2, service);

        service = loadBalancer.getNext();
        assertSame(service3, service);

        service = loadBalancer.getNext();
        assertSame(service1, service);

        service = loadBalancer.getNext();
        assertSame(service2, service);

        service = loadBalancer.getNext();
        assertSame(service3, service);

        service = loadBalancer.getNext();
        assertSame(service1, service);

        service = loadBalancer.getNext();
        assertSame(service2, service);

        service = loadBalancer.getNext();
        assertSame(service3, service);

        service = loadBalancer.getNext();
        assertSame(service1, service);

        service = loadBalancer.getNext();
        assertSame(service2, service);

        service = loadBalancer.getNext();
        assertSame(service3, service);
    }
}