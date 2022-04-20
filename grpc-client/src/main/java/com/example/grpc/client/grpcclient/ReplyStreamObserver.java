package com.example.grpc.client.grpcclient;
import io.grpc.stub.StreamObserver;
import com.example.grpc.server.grpcserver.MatrixReply;
// import com.example.grpc.client.grpcclient.MatrixReply;

public class ReplyStreamObserver implements StreamObserver<MatrixReply> {

    public ReplyStreamObserver() {
        return;
    }

    @Override
    public void onNext(MatrixReply reply) {
        System.out.println("Received from server " + reply.getC00() + "\n" + reply.getC01() + "\n" + reply.getC10() + "\n" + reply.getC11() + "\n");
    }

    public MatrixReply onNext(MatrixReply reply) {
        return reply;
    }

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onCompleted() {}

}