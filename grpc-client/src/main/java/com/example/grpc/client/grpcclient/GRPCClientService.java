package com.example.grpc.client.grpcclient;

// import com.example.grpc.client.grpcclient.PingRequest;
// import com.example.grpc.client.grpcclient.PongResponse;
import com.example.grpc.server.grpcserver.PingPongServiceGrpc;
import com.example.grpc.server.grpcserver.MatrixRequest;
import com.example.grpc.server.grpcserver.MatrixReply;
import com.example.grpc.server.grpcserver.MatrixServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
@Service
public class GRPCClientService {
    public String add(){
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
		.usePlaintext()
		.build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub
		 = MatrixServiceGrpc.newBlockingStub(channel);
		MatrixReply A=stub.addBlock(MatrixRequest.newBuilder()
			.setA00(1)
			.setA01(2)
			.setA10(5)
			.setA11(6)
			.setB00(1)
			.setB01(2)
			.setB10(5)
			.setB11(6)
			.build());
		String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
		int [][] m1 = TempStorage.getMatrix1();
		resp = m1[0][0]+" "+m1[0][1]+"<br>"+m1[1][0]+" "+m1[1][1]+"\n";
		return resp;
    }
    public String mult(){
    		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
    		.usePlaintext()
    		.build();
    		MatrixServiceGrpc.MatrixServiceBlockingStub stub
    		 = MatrixServiceGrpc.newBlockingStub(channel);
    		MatrixReply A=stub.multiplyBlock(MatrixRequest.newBuilder()
    			.setA00(1)
    			.setA01(2)
    			.setA10(5)
			.setA11(6)
			.setB00(2)
			.setB01(3)
			.setB10(6)
			.setB11(7)
			.build());
		String resp= A.getC00()+A.getC01()+A.getC10()+A.getC11()+"";
		return resp;
    }
}
