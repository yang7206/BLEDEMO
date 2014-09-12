package com.ezon.sportwatch.ble.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageManager;

public class BleUtils {

	/******************* 工具方法 *************************/
	// 字节转String
	public static String byteArrayToHexString(byte[] value) {
		StringBuilder stringBuilder = new StringBuilder("");
		for (int i = 0; i < value.length; i++) {
			int v = value[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String byteArrayToString(byte[] value, int length) {
		int max = Math.min(length, value.length);
		StringBuilder stringBuilder = new StringBuilder("");
		for (int i = 0; i < max; i++) {
			stringBuilder.append((char) value[i]);
		}
		return stringBuilder.toString();
	}

	public static int byteToHexInt(byte b) {
		return Integer.parseInt(Integer.toHexString(b), 16);
	}

	public static boolean isSupportBle(Context ctx) {
		return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
	}

	public static SimpleDateFormat getFormatter() {
		SimpleDateFormat format = new SimpleDateFormat("yy-mm-dd", Locale.CHINA);
		return format;
	}
	

	public static byte[] getByte() {
		byte[] b = new byte[20];
		for (int i = 0; i < b.length; i++) {
			b[i] = 0000;
		}
		return b;
	}

}
