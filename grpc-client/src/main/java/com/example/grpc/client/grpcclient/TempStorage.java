package com.example.grpc.client.grpcclient;

public class TempStorage {
	
	public static int[][] matrix1;
	public static int[][] matrix2;
	private static long deadline = 10000*1000000;
	private static boolean initialised = false;
	private static String[] internalIPs = {
		"10.128.0.4",
		"10.128.0.5",
		"10.128.0.6",
		"10.128.0.7",
		"10.128.0.8",
		"10.128.0.9",
		"10.128.0.10",
		"10.128.0.11"
	};

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
	public static boolean getInitialised() {
		return initialised;
	}
	public static void setInitialised(boolean b) {
		initialised = b;
		return;
	}
	public static long getDeadline() {
		return deadline;
	}
	public static String getInternalIP(int i) {
		return internalIPs[i];
	}
}