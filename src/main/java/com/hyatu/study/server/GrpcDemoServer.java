package com.hyatu.study.server;

import com.hyatu.study.service.GrpcDemoService;

import java.io.IOException;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcDemoServer {

    private static final Logger logger = Logger.getLogger(GrpcDemoServer.class.getName());

    private final int port;

    private final Server server;

    public GrpcDemoServer(int port) {
        this.port = port;
        this.server = ServerBuilder.forPort(port).addService(new GrpcDemoService()).build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GrpcDemoServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main method.  This comment makes the linter happy.
     */
    public static void main(String[] args) throws Exception {
        GrpcDemoServer server = new GrpcDemoServer(8888);
        server.start();
        server.blockUntilShutdown();
    }

}
