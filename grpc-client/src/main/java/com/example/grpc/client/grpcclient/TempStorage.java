package com.example.grpc.client.grpcclient;

public class TempStorage {
	
	public static int[][] matrix1;
	public static int[][] matrix2;

	public static int[][] getMatrix1() {
		return matrix1;
	}
	public static int[][] getMatrix2() {
		return matrix2;
	}
	public static void setMatrix1(int[][] m) {
		matrix1 = m;
	}
	public static void setMatrix2(int[][] m) {
		matrix2 = m;
	}
}