package com.ezon.sportwatch.ble;

import android.content.Intent;

import com.ezon.sportwatch.ble.callback.OnActivityResultCallback;
import com.ezon.sportwatch.ble.callback.OnBlueToothOpenResultListener;

/**
 * ��������
 * 
 * @author yxy
 * 
 */
public class BlueToothOpener implements OnActivityResultCallback {
	private OnBlueToothOpenResultListener mListener;

	public static final int REQUEST_ENABLE_BT = 999;

	protected BlueToothOpener() {
	}

	public void openBlueTooth(OnBlueToothOpenResultListener l) {
		this.mListener = l;
		Intent intent = new Intent(BLEManager.getApplication(), AsistActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BLEManager.getApplication().startActivity(intent);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (BLEManager.getInstance().isBluetoothAdapterEnable()) {
				mListener.onOpenResult(true);
			} else {
				mListener.onOpenResult(false);
			}
		}
	}
}
