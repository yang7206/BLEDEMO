package com.ezon.sportwatch.ble;

import com.ezon.sportwatch.ble.callback.OnActivityResultCallback;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * 间接层
 * 
 * @author yxy
 * 
 */
public class AsistActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setOnActivityResultListener(BLEManager.getInstance().getBlueToothOpener());
		startBluetooth();
	}

	private void startBluetooth() {
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, BlueToothOpener.REQUEST_ENABLE_BT);
	}

	private OnActivityResultCallback mOnActivityResultListener;

	private void setOnActivityResultListener(OnActivityResultCallback listener) {
		mOnActivityResultListener = listener;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == BlueToothOpener.REQUEST_ENABLE_BT) {
			int delay = 100;
			if (resultCode != RESULT_OK) {
				delay = 2000;
			}
			// 部分手机打开成功 但是会返回失败 需要延迟一下返回然后让opener去获取适配器判断是否成功
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (mOnActivityResultListener != null) {
						mOnActivityResultListener.onActivityResult(requestCode, resultCode, data);
					}
					finish();
				}
			}, delay);
		}
	}

}
