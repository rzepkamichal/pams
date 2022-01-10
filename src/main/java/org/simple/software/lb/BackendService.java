package org.simple.software.lb;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

public interface BackendService {

    Response serve(Request request);
}
