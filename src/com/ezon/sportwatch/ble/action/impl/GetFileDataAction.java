package com.ezon.sportwatch.ble.action.impl;

import com.ezon.sportwatch.ble.action.BaseAction;
import com.ezon.sportwatch.ble.action.ActionConstant;
import com.ezon.sportwatch.ble.action.entity.FileNameHolder;
import com.ezon.sportwatch.ble.util.BleUtils;
import com.ezon.sportwatch.ble.util.LogPrinter;

public class GetFileDataAction extends BaseAction<Integer> {

	private FileNameHolder mHolder;
	private int packageSum = 0;

	public GetFileDataAction() {
		setAction(ActionConstant.READ_ACTION_GET_FILE_DATA);
	}

	@Override
	public void onPrepareBodyData(byte[] data) {
		data[0] = 'C';
		data[1] = (byte) 0x02;
		// fileName + Timezone
		System.arraycopy(mHolder.getFileNameCode(), 0, data, 2, 5);
	}

	@Override
	public void onParserResultData(byte[] data) {
		byte pag[] = new byte[1];
		pag[0] = data[0];
		if ("P".equals(new String(pag))) {
			packageSum++;
			LogPrinter.println("packageSum :" + packageSum);
			// TODO :
			return;
		}
		final String _ErrorStr = "FILENAMEERROR";
		if (_ErrorStr.equals(BleUtils.byteArrayToString(data, _ErrorStr.length()))) {
			// TODO :数据 错误
			callbackResultFail();
		}
		// TODO:也可以在这里验证 包是否已经全部读完
	}

	@Override
	public void callbackToSecondTimeout() {
		// TODO: 这里来验证 是否读取完成
		callbackResultSuccess(packageSum);
	}

	@Override
	public boolean isMultileResult() {
		return true;
	}

	@Override
	public void readyWrite() {
		packageSum = 0;
	}

	public void setFileNameHolder(FileNameHolder holder) {
		mHolder = holder;
	}
}
