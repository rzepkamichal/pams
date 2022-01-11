package org.simple.software.loadbalancer;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

public interface BackendService {

    Response serve(Request request);
}
