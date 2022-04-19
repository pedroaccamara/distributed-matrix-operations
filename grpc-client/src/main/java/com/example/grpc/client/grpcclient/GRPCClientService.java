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
		int [][] matrix1 = TempStorage.getMatrix1();
		int [][] matrix2 = TempStorage.getMatrix2();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
		.usePlaintext()
		.build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub
		 = MatrixServiceGrpc.newBlockingStub(channel);
		
		MatrixReply A = addBlocks(stub, matrix1, matrix2);
			
		// all operations needed * time of a function call / deadline
		String resp= A.getC00()+" "+A.getC01()+"<br>"+A.getC10()+" "+A.getC11()+"\n";
		return resp;
    }
    public String mult(){ // For just 2 simple 2*2 matrices
		int [][] matrix1 = TempStorage.getMatrix1();
		int [][] matrix2 = TempStorage.getMatrix2();
		ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",9090)
		.usePlaintext()
		.build();
		MatrixServiceGrpc.MatrixServiceBlockingStub stub
			= MatrixServiceGrpc.newBlockingStub(channel);
		MatrixReply A = multBlocks(stub, matrix1, matrix2);

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
			MatrixReply M = addBlocks(stubs[b%8], blocksM1[b], blocksM2[b]);

			int xOffset = (b % sideBlocks)*2;
			int yOffset = Math.floorDiv(b, sideBlocks)*2;
			respM[yOffset][xOffset] = M.getC00();
			respM[yOffset][xOffset+1] = M.getC01();
			respM[yOffset+1][xOffset] = M.getC10();
			respM[yOffset+1][xOffset+1] = M.getC11();
		}

		return htmlStringify(respM);
    }

    public String biggerMult(){ // For strictly bigger than 2*2 matrices
		int [][] m1 = TempStorage.getMatrix1();
		int [][] m2 = TempStorage.getMatrix2();
		int [][][] blocksM1 = getBlocks(m1);
		int [][][] blocksM2 = getBlocks(m2);
		int noBlocks = blocksM1.length;
		int [][][] resultM = zeroedM(noBlocks);
		int sideBlocks = (int) Math.sqrt(noBlocks);

		MatrixServiceGrpc.MatrixServiceBlockingStub[] stubs = new MatrixServiceGrpc.MatrixServiceBlockingStub[8];
		for (int i = 0; i < 8; i++) { // Getting the 8 stubs for each running server
			ManagedChannel channel = ManagedChannelBuilder.forAddress(TempStorage.getInternalIP(i),9090)
			.usePlaintext()
			.build();
			stubs[i] = MatrixServiceGrpc.newBlockingStub(channel);
		}
		int[][] respM = new int[sideBlocks*2][sideBlocks*2];
		int stubI = 0; // Index to loop through the available stubs
		int serversUsed = 1;
		long footprint = 0;
		long start = 0;
		int deadline = 100; // Hard coded for now but will become something received from the post method
		for (int b = 0; b < noBlocks; b++) {
			int r = Math.floorDiv(b, sideBlocks)*sideBlocks; // Starting index for row involved in calc of block b (increments by 1)
			int c = b%sideBlocks; // Starting index for col elements involved in calc of block b (increments by sideBlocks)

			// If there are 2 blocks per side
			// C00 will be comprised of the addition of sideBlocks (2) multiplications
			// In this case those 2 multiplications will be A00*B00 and A01*B10
			// If the side of the matrices (sideBlocks) consisted of 4 blocks
			// C00 would again consist of the addition of 4 multiplications
			// So in this for loop we will perform sideBlock multiplications and add them all together
			// As for the result block these multiplications will be added and contribute to
			// (C00) since we'll keep the result blocks as well in an array of blocks, can be accessed
			// by the index b of the loop we're already in. C00 would be accessed by resultM[0]
			// and it would be calculated from A00*B00 + A01*B10 in such a way that the formula will be (with some pseudo code)
			// resultM[b] += blocksM1[r+i]*blocksM2[c+i*sideBlocks] for (i : sideBlocks)
			for (int i = 0; i<sideBlocks; i++) {
				if (footprint == 0) start = System.nanoTime();
				MatrixReply multResult = multBlocks(stubs[stubI%serversUsed], blocksM1[r+i], blocksM2[c+i*sideBlocks]);
				if (footprint == 0) {
					footprint = System.nanoTime() - start;
					System.out.println("Got a footprint of " + footprint);
					int serverCalls = noBlocks*2*sideBlocks; // The bigger loop iterates noBlocks times and the smaller loop makes 2 serverCalls, sideBlocks times
					serversUsed = Math.min(8, (int) footprint*serverCalls/deadline);
					System.out.println("Chose a number of " + serversUsed + " servers");
				}
				int [][] resultBlock = new int[][]{{multResult.getC00(), multResult.getC01()}, {multResult.getC10(), multResult.getC11()}};
				MatrixReply updatedBlock = addBlocks(stubs[(stubI+1)%serversUsed], resultM[b], resultBlock);
				resultM[b] = new int[][]{{updatedBlock.getC00(), updatedBlock.getC01()}, {updatedBlock.getC10(), updatedBlock.getC11()}};
				stubI += 2; // Since we used a stub for the multiplication then another for the addition
			}

			// Preparing an already 2 dimensional matrix for converting that to a string response in a O(sideBlocks^2) time complexity
			int xOffset = (b % sideBlocks)*2;
			int yOffset = Math.floorDiv(b, sideBlocks)*2;
			respM[yOffset][xOffset] = resultM[b][0][0];
			respM[yOffset][xOffset+1] = resultM[b][0][1];
			respM[yOffset+1][xOffset] = resultM[b][1][0];
			respM[yOffset+1][xOffset+1] = resultM[b][1][1];
		}

		// Converting the prepared respM matrix to an html compatible string response
		return htmlStringify(respM);
    }

	public MatrixReply addBlocks(MatrixServiceGrpc.MatrixServiceBlockingStub stub, int[][] matrix1, int[][] matrix2) {
		return stub.addBlock(MatrixRequest.newBuilder()
		.setA00(matrix1[0][0])
		.setA01(matrix1[0][1])
		.setA10(matrix1[1][0])
		.setA11(matrix1[1][1])
		.setB00(matrix2[0][0])
		.setB01(matrix2[0][1])
		.setB10(matrix2[1][0])
		.setB11(matrix2[1][1])
		.build());
	}

	public MatrixReply multBlocks(MatrixServiceGrpc.MatrixServiceBlockingStub stub, int[][] matrix1, int[][] matrix2) {
		return stub.multiplyBlock(MatrixRequest.newBuilder()
		.setA00(matrix1[0][0])
		.setA01(matrix1[0][1])
		.setA10(matrix1[1][0])
		.setA11(matrix1[1][1])
		.setB00(matrix2[0][0])
		.setB01(matrix2[0][1])
		.setB10(matrix2[1][0])
		.setB11(matrix2[1][1])
		.build());
	}

	// Getting an array of 2x2 blocks from any 2 dimensional matrix
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

	// Creating a 3 dimensional zero matrix of size s
	public int[][][] zeroedM(int s) {
		int[][][] matrix = new int[s][2][2];

		for (int i = 0; i<s; i++) {
			matrix[i] = new int[][]{{0,0}, {0,0}};
		}
		return matrix;
	}

	// Converting the prepared 2 dimensional matrix to an html compatible string response
	public String htmlStringify(int[][] matrix) {
		String resp = "";
		for (int[] row : matrix) {
			resp += "" + row[0];
			for(int col = 1; col<row.length; col++)
				resp += " " + row[col];
			resp += "<br>";
		}
		return resp;
	}
}
