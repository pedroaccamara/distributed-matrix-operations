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
		int [][] m1 = TempStorage.getMatrix1();
		int [][] m2 = TempStorage.getMatrix2();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
		.usePlaintext()
		.build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub
		 = MatrixServiceGrpc.newBlockingStub(channel);
		MatrixReply A=stub.addBlock(MatrixRequest.newBuilder()
			.setA00(m1[0][0])
			.setA01(m1[0][1])
			.setA10(m1[1][0])
			.setA11(m1[1][1])
			.setB00(m2[0][0])
			.setB01(m2[0][1])
			.setB10(m2[1][0])
			.setB11(m2[1][1])
			.build());
			
			// all operations needed * time of a function call / deadline
		System.out.println("Filled A's stub with " + m1[1][1] + " and " + m2[1][1]);
		String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
		return resp;
    }
    public String mult(){
		int [][] m1 = TempStorage.getMatrix1();
		int [][] m2 = TempStorage.getMatrix2();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
		.usePlaintext()
		.build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub
			= MatrixServiceGrpc.newBlockingStub(channel);
		MatrixReply A=stub.multiplyBlock(MatrixRequest.newBuilder()
			.setA00(m1[0][0])
			.setA01(m1[0][1])
			.setA10(m1[1][0])
			.setA11(m1[1][1])
			.setB00(m2[0][0])
			.setB01(m2[0][1])
			.setB10(m2[1][0])
			.setB11(m2[1][1])
			.build());
		String resp= A.getC00()+A.getC01()+A.getC10()+A.getC11()+"";
		return resp;
    }
}
