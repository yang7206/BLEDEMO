package com.ezon.sportwatch.ble;

import java.util.List;

import com.ezon.sportwatch.ble.action.entity.FileNameHolder;
import com.ezon.sportwatch.ble.action.impl.CheckNewDataAction;
import com.ezon.sportwatch.ble.action.impl.ClearMatchCodeAction;
import com.ezon.sportwatch.ble.action.impl.GetDeviceTypeAction;
import com.ezon.sportwatch.ble.action.impl.GetFileDataAction;
import com.ezon.sportwatch.ble.action.impl.GetFileListAction;
import com.ezon.sportwatch.ble.action.impl.GetMissingPackageDataAction;
import com.ezon.sportwatch.ble.action.impl.SendMatchCodeAction;
import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;

/**
 * ��������
 * 
 * @author yxy
 * 
 */
public class BluetoothLERequest {

	/***************************** ���󷽷� ****************************/

	/**
	 * ���������
	 * 
	 * @param callback
	 *            ���state ����STATUS_SUCEESS �򷵻������
	 * 
	 * @return
	 */
	public static boolean sendMatchCode(final OnBleRequestCallback<String> callback) {
		final SendMatchCodeAction action = new SendMatchCodeAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * ����Ƿ���������
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean checkNewData(OnBleRequestCallback<Boolean> callback) {
		CheckNewDataAction action = new CheckNewDataAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * ��ȡ�ļ��б�
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean getFileList(OnBleRequestCallback<List<FileNameHolder>> callback) {
		GetFileListAction action = new GetFileListAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * ��ȡ�ļ�����
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean getFileData(FileNameHolder holder, OnBleRequestCallback<Integer> callback) {
		GetFileDataAction action = new GetFileDataAction();
		action.setFileNameHolder(holder);
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * ��ȡ�豸������
	 */
	public static boolean getDeviceTypeCode(OnBleRequestCallback<String> callback) {
		GetDeviceTypeAction action = new GetDeviceTypeAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * ��������
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean clearMatchCode(OnBleRequestCallback<String> callback) {
		ClearMatchCodeAction action = new ClearMatchCodeAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	public static boolean getMissingPackageData(int start, int end, OnBleRequestCallback<Integer> callback) {
		GetMissingPackageDataAction action = new GetMissingPackageDataAction();
		action.setOnBleRequestCallback(callback);
		action.setMissPackageRange(start, end);
		return BLEManager.getInstance().writeToWatch(action);
	}

}
