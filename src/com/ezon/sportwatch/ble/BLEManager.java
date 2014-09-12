package com.ezon.sportwatch.ble;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

import com.ezon.sportwatch.ble.action.IDataAction;
import com.ezon.sportwatch.ble.callback.OnBlueToothOpenResultListener;
import com.ezon.sportwatch.ble.callback.OnBluetoothDeviceSearchListener;
import com.ezon.sportwatch.ble.outercallback.OnDeviceConnectListener;
import com.ezon.sportwatch.ble.util.BleUtils;

public class BLEManager {

	private BluetoothleConnector mConnector;
	private BluetoothLESearcher mSearcher;
	private BlueToothOpener mOpener;
	private BluetoothDataParser mDataParser;

	/********************** singleton ***********************/
	private static BLEManager mInstance;

	private BLEManager() {
		if (mConnector == null) {
			mConnector = new BluetoothleConnector();
		}
		if (mSearcher == null) {
			mSearcher = new BluetoothLESearcher();
		}
		if (mOpener == null) {
			mOpener = new BlueToothOpener();
		}
		if (mDataParser == null) {
			mDataParser = new BluetoothDataParser();
		}
	}

	public static BLEManager getInstance() {
		if (mInstance == null) {
			mInstance = new BLEManager();
		}
		return mInstance;
	}

	protected BlueToothOpener getBlueToothOpener() {
		return mOpener;
	}

	private static Application mApplication;
	// 其他或对外handler
	private static Handler mHandler;

	public static void initApplication(Application app) {
		mApplication = app;
		mHandler = new Handler(app.getMainLooper());
	}

	public static Application getApplication() {
		return mApplication;
	}

	public static Handler getHandler() {
		return mHandler;
	}

	public BluetoothGatt getBluetoothGatt() {
		return mConnector.getBluetoothGatt();
	}

	public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
		return mConnector.getBluetoothGattCharacteristic();
	}

	public void destory() {
		mDataParser.destory();
	}

	/******************************* 对外方法 **********************************/

	public boolean isEnableBle() {
		return BleUtils.isSupportBle(mApplication);
	}

	/**************************** BLE搜索 ******************************/

	public void startSearch(OnBluetoothDeviceSearchListener listener) {
		mSearcher.startSearch(listener);
	}

	/**************************** 蓝牙打开器 ******************************/

	protected void openBluetooth(OnBlueToothOpenResultListener listener) {
		mOpener.openBlueTooth(listener);
	}

	public void connect(BluetoothDevice device, final OnDeviceConnectListener listener) {
		mConnector.connect(device, new OnDeviceConnectListener() {

			@Override
			public void onConnect(final int state, BluetoothDevice device) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						switch (state) {
						case DEVICE_CONNECTED:
							if (listener != null) {
								listener.onConnect(OnDeviceConnectListener.DEVICE_CONNECTED, mConnector.getBluetoothDevice());
							}
							break;
						case DEVICE_CONNECT_FAIL:
							if (listener != null) {
								listener.onConnect(OnDeviceConnectListener.DEVICE_CONNECT_FAIL, mConnector.getBluetoothDevice());
							}
							break;
						}
					}
				});
			}
		});
	}

	public boolean reconnect(OnDeviceConnectListener listener) {
		return mConnector.reconnect(listener);
	}

	protected synchronized void resolveReadData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		mDataParser.resolveReadData(gatt, characteristic);
	}

	protected synchronized <T> boolean writeToWatch(IDataAction<T> action) {
		return mDataParser.writeDataToWatch(action);
	}

	protected BluetoothAdapter getAdapter() {
		BluetoothManager bluetoothManager = (BluetoothManager) mApplication.getSystemService(Context.BLUETOOTH_SERVICE);
		return bluetoothManager.getAdapter();
	}

	public boolean isBluetoothAdapterEnable() {
		BluetoothAdapter adpater = getAdapter();
		return adpater != null && adpater.isEnabled();
	}

	public boolean isChannelWriteEnable() {
		return mConnector.isChannelWriteEnable();
	}

}
