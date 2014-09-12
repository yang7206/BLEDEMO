package com.ezon.sportwatch.ble.action;

import com.ezon.sportwatch.ble.BluetoothDataParser;
import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;

/**
 * 数据action动作
 * 
 * @author yxy
 * @param <T>
 * 
 */
public interface IDataAction<T> {
	/**
	 * actiont动作
	 * 
	 * @return
	 */
	int action();

	/**
	 * 准备请求包体数据
	 * 
	 * @param data
	 */
	byte[] onBodyData();

	/**
	 * 解析包体
	 * 
	 * @param data
	 */
	void onParserResultData(byte[] data);

	/**
	 * 是否是连续结果返回
	 * 
	 * @return
	 */
	boolean isMultileResult();

	/**
	 * 两秒超时 已经没有数据再返回了
	 * 
	 */
	void callbackToSecondTimeout();

	void setOnBleRequestCallback(OnBleRequestCallback<T> t);

	void setBluetoothDataParser(BluetoothDataParser parser);

	void callbackResultFail();
	
	void readyWrite();
}
