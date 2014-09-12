package com.ezon.sportwatch.ble.callback;

import android.bluetooth.BluetoothDevice;

/**
 * Éè±¸ËÑË÷¼àÌý
 * 
 */
public interface OnBluetoothDeviceSearchListener {

	public static final int SEARCHING_READY = 0;
	public static final int SEARCHING_PERFORM = 1;
	public static final int SEARCHING_DONE = 2;

	public static final int SEARCH_ERROR_BLUETOOTH_OPENFAIL = -2;

	void onSearch(int action, BluetoothDevice device);
	
}
