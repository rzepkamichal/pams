package org.simple.software.lb;

import org.simple.software.protocol.Request;
import org.simple.software.protocol.Response;

import java.util.concurrent.CompletableFuture;

public interface BackendService {

    Response service(Request request);
}
