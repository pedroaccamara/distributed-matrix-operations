package com.example.grpc.client.grpcclient;
import io.grpc.stub.StreamObserver;
import com.example.grpc.server.grpcserver.MatrixReply;

public abstract class ReplyStreamObserver implements StreamObserver<MatrixReply> {


    @Override
    public void onNext(MatrixReply reply) {}

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onCompleted() {}

}