package com.hyatu.study.service;

import com.hyatu.study.grpc.GRpcDemo;
import com.hyatu.study.grpc.GrpcDemoGrpc;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.stub.StreamObserver;

public class GrpcDemoService extends GrpcDemoGrpc.GrpcDemoImplBase {

    private static final Logger logger = Logger.getLogger(GrpcDemoService.class.getName());

    private Random random = new Random();

    @Override
    public void helloWorld(GRpcDemo.Request request, StreamObserver<GRpcDemo.Response> responseObserver) {
        responseObserver.onNext(GRpcDemo.Response.newBuilder().setRes(request.getReq() + "-from grpc server-hello grpc!-" + new Random().nextInt() + "").build());
        responseObserver.onCompleted();
    }

    @Override
    public void download(GRpcDemo.Request request, StreamObserver<GRpcDemo.Response> responseObserver) {
        for (int i = 0; i < 10; i++) {
            responseObserver.onNext(GRpcDemo.Response.newBuilder().setRes("download progress " + (i + 1) * 10 + "%").build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GRpcDemo.Request> upload(StreamObserver<GRpcDemo.Response> responseObserver) {
        return new StreamObserver<GRpcDemo.Request>() {
            @Override
            public void onNext(GRpcDemo.Request value) {
                logger.info("from server received " + value.getReq());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Encountered error in upload", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(GRpcDemo.Response.newBuilder().setRes("upload finished!").build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GRpcDemo.Request> chat(StreamObserver<GRpcDemo.Response> responseObserver) {
        return new StreamObserver<GRpcDemo.Request>() {
            @Override
            public void onNext(GRpcDemo.Request value) {
                responseObserver.onNext(GRpcDemo.Response.newBuilder().setRes("from grpc server " +random.nextInt(100)+"-"+value.getReq()).build());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Encountered error in upload", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
