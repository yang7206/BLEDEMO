package com.ezon.sportwatch.ble.action.impl;

import com.ezon.sportwatch.ble.action.BaseAction;
import com.ezon.sportwatch.ble.action.ActionConstant;

public class SendMatchCodeAction extends BaseAction<String> {
	private static byte[] codes = new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
			(byte) 0x08, (byte) 0x09 };
	private String matchCode;

	public SendMatchCodeAction() {
		setAction(ActionConstant.READ_ACTION_MATCHCODE);
	}

	@Override
	public void onPrepareBodyData(byte[] data) {
		data[0] = 'C';
		data[1] = (byte) 0x00;
		final byte[] code = new byte[4];
		int temp = 0;
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			temp = codes[(int) (Math.random() * 10)];
			sb.append(temp);
			code[i] = (byte) temp;
		}
		System.arraycopy(code, 0, data, 2, 4);
		matchCode = sb.toString();
	}

	@Override
	public void onParserResultData(byte[] data) {
		byte[] b = new byte[3];
		b[0] = data[0];
		b[1] = data[2];
		b[2] = data[3];
		if ("COK".equalsIgnoreCase(new String(b))) {
			callbackResultSuccess(matchCode);
		} else {
			callbackResultFail();
		}
	}
}
