package com.ezon.sportwatch.ble.action.impl;

import com.ezon.sportwatch.ble.action.BaseAction;
import com.ezon.sportwatch.ble.action.ActionConstant;

public class CheckNewDataAction extends BaseAction<Boolean> {

	public CheckNewDataAction() {
		setAction(ActionConstant.READ_ACTION_CHECK_NEW_DATA);
	}

	@Override
	public void onPrepareBodyData(byte[] data) {
		data[0] = 'C';
		data[1] = (byte) 0x04;
	}

	@Override
	public void onParserResultData(byte[] data) {
		byte[] firstByte = new byte[1];
		byte[] secondByte = new byte[1];
		firstByte[0] = data[0];
		secondByte[0] = data[2];
		if ("C4".equals(new String(firstByte) + Integer.toHexString(data[1]))) {
			boolean isHaveNewData = false;
			if ("N".equals(new String(secondByte))) {
				isHaveNewData = false;
			} else if ("Y".equals(new String(secondByte))) {
				isHaveNewData = true;
			}
			callbackResultSuccess(isHaveNewData);
		} else {
			callbackResultFail();
		}
	}
}
