package com.ezon.sportwatch.ble.callback;

/**
 * 蓝牙打开结果监听
 * 
 * @author yxy
 * 
 */
public interface OnBlueToothOpenResultListener {
	/**
	 * true为打开成功,false为失败
	 * 
	 * @param isOpen
	 */
	void onOpenResult(boolean isOpen);
}