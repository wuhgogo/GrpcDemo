package com.hyatu.study.client;

import com.hyatu.study.grpc.GRpcDemo;
import com.hyatu.study.grpc.GrpcDemoGrpc;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcDemoClient {

    private static final Logger logger = Logger.getLogger(GrpcDemoClient.class.getName());

    private final ManagedChannel channel;
    private final GrpcDemoGrpc.GrpcDemoBlockingStub blockingStub;
    private final GrpcDemoGrpc.GrpcDemoStub asyncStub;

    private Random random = new Random();

    /**
     * Construct client for accessing RouteGuide server at {@code host:port}.
     */
    public GrpcDemoClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    public GrpcDemoClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = GrpcDemoGrpc.newBlockingStub(channel);
        asyncStub = GrpcDemoGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void helloWorld() {
        logger.info(blockingStub.helloWorld(GRpcDemo.Request.newBuilder().setReq("from grpc client").build()).getRes());
    }

    public void download() {
        Iterator<GRpcDemo.Response> response = blockingStub.download(GRpcDemo.Request.newBuilder().setReq("from grpc client").build());
        response.forEachRemaining(res -> logger.info(res.getRes()));
    }

    /**
     * 执行顺序
     * requestObserver.onNext发送到服务端
     * 服务端处理
     * requestObserver.onCompleted（responseObserver只能在此方法调用之后执行）
     * responseObserver 接受返回结果
     * 依次执行 responseObserver onNext onCompleted
     */
    public void upload() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<GRpcDemo.Response> responseObserver = new StreamObserver<GRpcDemo.Response>() {
            @Override
            public void onNext(GRpcDemo.Response value) {
                logger.info(value.getRes());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Encountered error in upload", t);
            }

            @Override
            public void onCompleted() {
                logger.info("from grpc client upload finished");
                countDownLatch.countDown();
            }
        };
        StreamObserver<GRpcDemo.Request> requestObserver = asyncStub.upload(responseObserver);
        for (int i = 0; i < 10; i++) {
            requestObserver.onNext(GRpcDemo.Request.newBuilder().setReq("upload progress " + (i + 1) * 10 + "%").build());
        }
        requestObserver.onCompleted();
        countDownLatch.await();
    }

    public void chat() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<GRpcDemo.Response> responseObserver = new StreamObserver<GRpcDemo.Response>() {
            @Override
            public void onNext(GRpcDemo.Response value) {
                logger.info(value.getRes());
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Encountered error in chat", t);
            }

            @Override
            public void onCompleted() {
                logger.info("from grpc client chat finished");
                countDownLatch.countDown();
            }
        };
        StreamObserver<GRpcDemo.Request> requestObserver = asyncStub.chat(responseObserver);
        for (int i = 0; i < 10; i++) {
            requestObserver.onNext(GRpcDemo.Request.newBuilder().setReq("from grpc client " + random.nextInt(100)).build());
        }
        requestObserver.onCompleted();
        countDownLatch.await();
    }

    public static void main(String[] args) throws InterruptedException {
        GrpcDemoClient client = new GrpcDemoClient("localhost", 8888);
        try {

            //简单
//            client.helloWorld();

            //服务端流式
//            client.download();

            //客服端流式
//            client.upload();

            //双向流式
//            client.chat();

        } finally {
            client.shutdown();
        }
    }

}
