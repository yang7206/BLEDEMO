package com.ezon.sportwatch.ble.callback;

/**
 * �����򿪽������
 * 
 * @author yxy
 * 
 */
public interface OnBlueToothOpenResultListener {
	/**
	 * trueΪ�򿪳ɹ�,falseΪʧ��
	 * 
	 * @param isOpen
	 */
	void onOpenResult(boolean isOpen);
}