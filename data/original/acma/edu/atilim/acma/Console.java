package edu.atilim.acma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class Console {
	private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public static String readLine() {
		try {
			return in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static int readUInt(String ask, int max) {
		int i = -1;
		do {
			System.out.println(ask);
			try { i = Integer.parseInt(readLine()); } catch (NumberFormatException nfe) { }
		} while(i < 0 || i > max);
		return i;
	}
}
