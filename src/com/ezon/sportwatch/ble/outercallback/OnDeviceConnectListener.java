package com.ezon.sportwatch.ble.outercallback;

import android.bluetooth.BluetoothDevice;

public interface OnDeviceConnectListener {

	public static final int DEVICE_CONNECTED = 0;
	// 重试失败 将启动搜索 然后重新连接
	public static final int DEVICE_CONNECT_FAIL = -1;

	void onConnect(int state, BluetoothDevice device);
}
