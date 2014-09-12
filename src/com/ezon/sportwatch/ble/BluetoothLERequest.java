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
 * 蓝牙请求
 * 
 * @author yxy
 * 
 */
public class BluetoothLERequest {

	/***************************** 请求方法 ****************************/

	/**
	 * 发送配对码
	 * 
	 * @param callback
	 *            如果state 返回STATUS_SUCEESS 则返回配对码
	 * 
	 * @return
	 */
	public static boolean sendMatchCode(final OnBleRequestCallback<String> callback) {
		final SendMatchCodeAction action = new SendMatchCodeAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * 检查是否有新数据
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
	 * 获取文件列表
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
	 * 获取文件数据
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
	 * 获取设备类型码
	 */
	public static boolean getDeviceTypeCode(OnBleRequestCallback<String> callback) {
		GetDeviceTypeAction action = new GetDeviceTypeAction();
		action.setOnBleRequestCallback(callback);
		return BLEManager.getInstance().writeToWatch(action);
	}

	/**
	 * 清楚配对码
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
