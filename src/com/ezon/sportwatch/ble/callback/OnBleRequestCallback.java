package com.ezon.sportwatch.ble.callback;

/**
 * BLE请求结果返回callback
 * 
 * @author yxy
 * 
 * @param <T>
 */
public interface OnBleRequestCallback<T> {

	public static final int STATUS_SUCEESS = 0;
	public static final int STATUS_FAIL = -1;
	// 蓝牙连接异常
	public static final int STATUS_BLE_CONNECT_ERROR = -2;

	void onCallback(int status, T t);
}
