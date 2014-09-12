package com.ezon.sportwatch.ble.action.impl;

import com.ezon.sportwatch.ble.action.BaseAction;
import com.ezon.sportwatch.ble.action.ActionConstant;

public class GetDeviceTypeAction extends BaseAction<String> {

	public GetDeviceTypeAction() {
		setAction(ActionConstant.READ_ACTION_CLEAR_MATCH_CODE);
	}

	@Override
	public void onPrepareBodyData(byte[] data) {
		data[0] = 'C';
		data[1] = (byte) 0x00;
	}

	@Override
	public void onParserResultData(byte[] data) {
		byte[] b = new byte[4];
		System.arraycopy(data, 0, b, 0, 4);
		final StringBuffer sb = new StringBuffer();
		sb.append(new String(b));
		sb.append(Integer.toHexString(data[4]));
		sb.append(Integer.toHexString(data[5]));
		sb.append(Integer.toHexString(data[6]));
		sb.append(Integer.toHexString(data[7]));
		if (sb.toString().startsWith("EZON")) {
			callbackResultSuccess(sb.toString());
		} else {
			callbackResultFail();
		}
	}
}
