package com.ezon.sportwatch.ble;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.ezon.sportwatch.ble.callback.OnBlueToothOpenResultListener;
import com.ezon.sportwatch.ble.callback.OnBluetoothDeviceSearchListener;

/**
 * BLE搜索器
 * 
 * @author yxy
 * 
 */
public class BluetoothLESearcher {
	private BluetoothAdapter mBluetoothAdapter;
	private OnBluetoothDeviceSearchListener mOnDeviceSearchListener;
	private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 10000;

	private boolean mScanning = false;

	private BLEManager mBLEManager;

	protected BluetoothLESearcher(BLEManager manager) {
		this.mBLEManager = manager;
	}

	protected void startSearch(OnBluetoothDeviceSearchListener listener) {
		if (mScanning) {
			// 如果连续两次扫描 listener可能相同
			stopScan(false);
		}
		this.mOnDeviceSearchListener = listener;
		this.mDeviceList.clear();
		initBluetoothAdapter();
	}

	private void initBluetoothAdapter() {
		mBluetoothAdapter = mBLEManager.getAdapter();
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			BLEManager.getInstance().openBluetooth(new OnBlueToothOpenResultListener() {

				@Override
				public void onOpenResult(boolean isOpen) {
					if (isOpen) {
						scanLeDevice();
					} else {
						callbackSearch(OnBluetoothDeviceSearchListener.SEARCH_ERROR_BLUETOOTH_OPENFAIL, null);
					}
				}
			});
			return;
		}
		scanLeDevice();
	}

	private void scanLeDevice() {
		BLEManager.getHandler().postDelayed(new Runnable() {
			@Override
			public void run() {
				stopScan(true);
			}
		}, SCAN_PERIOD);
		startScan();
	}

	private void startScan() {
		mScanning = true;
		callbackSearch(OnBluetoothDeviceSearchListener.SEARCHING_READY, null);
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	private void stopScan(boolean callback) {
		mScanning = false;
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		if (callback) {
			callbackSearch(OnBluetoothDeviceSearchListener.SEARCHING_DONE, null);
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (device != null && !mDeviceList.contains(device)) {
				mDeviceList.add(device);
				callbackSearch(OnBluetoothDeviceSearchListener.SEARCHING_PERFORM, device);
			}
		}
	};

	private void callbackSearch(final int action, final BluetoothDevice device) {
		BLEManager.getHandler().post(new Runnable() {

			@Override
			public void run() {
				if (mOnDeviceSearchListener != null) {
					mOnDeviceSearchListener.onSearch(action, device);
				}
			}
		});
	}
}
