package com.example.grpc.client.grpcclient;
import io.grpc.stub.StreamObserver;
import com.example.grpc.server.grpcserver.MatrixReply;

public class ReplyStreamObserver implements StreamObserver<MatrixReply> {

    @Override
    public MatrixReply onNext(MatrixReply reply) {
        return reply;
    }

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onCompleted() {}

}