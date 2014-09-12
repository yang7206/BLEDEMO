package com.ezon.sportwatch.ble.action;

import com.ezon.sportwatch.ble.BLEManager;
import com.ezon.sportwatch.ble.BluetoothDataParser;
import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;
import com.ezon.sportwatch.ble.util.BleUtils;

public abstract class BaseAction<T> implements IDataAction<T> {
	protected OnBleRequestCallback<T> callback;
	protected BluetoothDataParser mParser;

	private int action;

	protected void setAction(int action) {
		this.action = action;
	}

	@Override
	public int action() {
		return action;
	}

	@Override
	public byte[] onBodyData() {
		byte[] data = BleUtils.getByte();
		onPrepareBodyData(data);
		return data;
	}

	public abstract void onPrepareBodyData(byte[] data);

	public abstract void onParserResultData(byte[] data);

	@Override
	public boolean isMultileResult() {
		// TODO :多结果需要时重写
		return false;
	}

	@Override
	public void callbackToSecondTimeout() {
		// TODO :多结果需要时重写
	}

	@Override
	public void readyWrite() {
		// TODO :子类需要时重写
	}

	@Override
	public void setOnBleRequestCallback(OnBleRequestCallback<T> t) {
		this.callback = t;
	}

	@Override
	public void setBluetoothDataParser(BluetoothDataParser parser) {
		this.mParser = parser;
	}

	protected void callbackResultSuccess(final T t) {
		mParser.removeThreadHandlerMsg();
		BLEManager.getHandler().post(new Runnable() {

			@Override
			public void run() {
				if (callback != null) {
					callback.onCallback(OnBleRequestCallback.STATUS_SUCEESS, t);
				}
				mParser.notifyWriteNext();
			}
		});
	}

	public void callbackResultFail() {
		mParser.removeThreadHandlerMsg();
		BLEManager.getHandler().post(new Runnable() {

			@Override
			public void run() {
				if (callback != null) {
					callback.onCallback(OnBleRequestCallback.STATUS_FAIL, null);
				}
				mParser.notifyWriteNext();
			}
		});
	}

}
