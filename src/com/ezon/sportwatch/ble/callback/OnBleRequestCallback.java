package com.ezon.sportwatch.ble.callback;

/**
 * BLE����������callback
 * 
 * @author yxy
 * 
 * @param <T>
 */
public interface OnBleRequestCallback<T> {

	public static final int STATUS_SUCEESS = 0;
	public static final int STATUS_FAIL = -1;
	// ���������쳣
	public static final int STATUS_BLE_CONNECT_ERROR = -2;

	void onCallback(int status, T t);
}
