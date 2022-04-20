package com.example.grpc.client.grpcclient;

public class TempStorage {
	
	private static int[][] matrix1;
	private static int[][] matrix2;
	private static boolean initialised = false;
	private static String[] internalIPs = {
		"10.128.0.4",
		"10.128.0.5",
		"10.128.0.6",
		"10.128.0.7",
		"10.128.0.12",
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
	public static String getInternalIP(int i) {
		return internalIPs[i];
	}
}