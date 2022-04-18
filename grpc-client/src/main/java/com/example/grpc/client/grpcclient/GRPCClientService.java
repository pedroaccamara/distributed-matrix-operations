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
    public String add(){ // For just 2 simple 2*2 matrices
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
		String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
		return resp;
    }
    public String mult(){ // For just 2 simple 2*2 matrices
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
		String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
		return resp;
    }
    public String biggerAdd(){ // For strictly bigger than 2*2 matrices
		int [][] m1 = TempStorage.getMatrix1();
		int [][] m2 = TempStorage.getMatrix2();
		int [][][] blocksM1 = getBlocks(m1);
		int [][][] blocksM2 = getBlocks(m2);
		int sideBlocks = (int) Math.sqrt(blocksM1.length);

		MatrixServiceGrpc.MatrixServiceBlockingStub[] stubs = new MatrixServiceGrpc.MatrixServiceBlockingStub[8];
		for (int i = 0; i < 8; i++) { // Getting the 8 stubs for each running server
			ManagedChannel channel = ManagedChannelBuilder.forAddress(TempStorage.getInternalIP(i),9090)
			.usePlaintext()
			.build();
			stubs[i] = MatrixServiceGrpc.newBlockingStub(channel);
		}
		int[][] respM = new int[sideBlocks*2][sideBlocks*2];
		for (int b = 0; b < blocksM1.length; b++) {
			MatrixReply M=stubs[b%8].addBlock(MatrixRequest.newBuilder()
			.setA00(blocksM1[b][0][0])
			.setA01(blocksM1[b][0][1])
			.setA10(blocksM1[b][1][0])
			.setA11(blocksM1[b][1][1])
			.setB00(blocksM2[b][0][0])
			.setB01(blocksM2[b][0][1])
			.setB10(blocksM2[b][1][0])
			.setB11(blocksM2[b][1][1])
			.build());

			int xOffset = (b % sideBlocks)*2;
			int yOffset = Math.floorDiv(b, sideBlocks)*2;
			respM[yOffset][xOffset] = M.getC00();
			respM[yOffset][xOffset+1] = M.getC01();
			respM[yOffset+1][xOffset] = M.getC10();
			respM[yOffset+1][xOffset+1] = M.getC11();
		}
		String resp = "";
		for (int[] row : respM) {
			resp += "" + row[0];
			for(int col = 1; col<row.length; col++)
				resp += " " + row[col];
			resp += "<br>";
		}
		return resp;
    }
    public String biggerMult(){ // For strictly bigger than 2*2 matrices
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
		String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
		return resp;
    }

	public int[][][] getBlocks(int[][] matrix) {
		int noBlocks = matrix.length*matrix.length / 4;
		int sideBlocks = matrix.length / 2;
		int[][][] blocks = new int[noBlocks][2][2];

		for (int b = 0; b<noBlocks; b++) {
			int xOffset = (b%sideBlocks)*2;
			int yOffset = Math.floorDiv(b,sideBlocks)*2;
			blocks[b][0][0] = matrix[yOffset][xOffset];
			blocks[b][0][1] = matrix[yOffset][xOffset+1];
			blocks[b][1][0] = matrix[yOffset+1][xOffset];
			blocks[b][1][1] = matrix[yOffset+1][xOffset+1];
		}

		return blocks;
	}
}
