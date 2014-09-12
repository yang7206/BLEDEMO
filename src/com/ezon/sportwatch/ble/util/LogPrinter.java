package com.ezon.sportwatch.ble.util;

public class LogPrinter {

	private static boolean DEBUG = true;

	public static void println(String text) {
		if (DEBUG) {
			System.out.println(text);
		}
	}
}
