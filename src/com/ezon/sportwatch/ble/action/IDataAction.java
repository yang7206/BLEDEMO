package com.ezon.sportwatch.ble.action;

import com.ezon.sportwatch.ble.BluetoothDataParser;
import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;

/**
 * ����action����
 * 
 * @author yxy
 * @param <T>
 * 
 */
public interface IDataAction<T> {
	/**
	 * actiont����
	 * 
	 * @return
	 */
	int action();

	/**
	 * ׼�������������
	 * 
	 * @param data
	 */
	byte[] onBodyData();

	/**
	 * ��������
	 * 
	 * @param data
	 */
	void onParserResultData(byte[] data);

	/**
	 * �Ƿ��������������
	 * 
	 * @return
	 */
	boolean isMultileResult();

	/**
	 * ���볬ʱ �Ѿ�û�������ٷ�����
	 * 
	 */
	void callbackToSecondTimeout();

	void setOnBleRequestCallback(OnBleRequestCallback<T> t);

	void setBluetoothDataParser(BluetoothDataParser parser);

	void callbackResultFail();
	
	void readyWrite();
}
