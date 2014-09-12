package com.ezon.sportwatch.ble.action.impl;

import com.ezon.sportwatch.ble.action.ActionConstant;
import com.ezon.sportwatch.ble.action.BaseAction;

public class GetMissingPackageDataAction extends BaseAction<Integer> {

	private int packageSum = 0;

	private int startMissPackageIndex = 0;
	private int endMissPackageIndex = 0;

	public GetMissingPackageDataAction() {
		setAction(ActionConstant.READ_ACTION_GET_MISS_PACKAGE_DATA);
	}

	@Override
	public void onPrepareBodyData(byte[] data) {
		data[0] = 'C';
		data[1] = (byte) 0x03;
		for (int i = startMissPackageIndex; i <= endMissPackageIndex; i++) {
			data[i - startMissPackageIndex + 2] = (byte) i;
		}
	}

	@Override
	public void onParserResultData(byte[] data) {
		//
		// byte pag[] = new byte[1];
		// pag[0] = data[0];
		// if ("P".equals(new String(pag))) {
		// packageSum++;
		// LogPrinter.println("packageSum :" + packageSum);
		// // TODO :
		// return;
		// }
		// final String _ErrorStr = "FILENAMEERROR";
		// if (_ErrorStr.equals(BleUtils.byteArrayToString(data,
		// _ErrorStr.length()))) {
		// // TODO :���� ����
		// callbackResultFail();
		// }
		// // TODO:Ҳ������������֤ ���Ƿ��Ѿ�ȫ������
	}

	@Override
	public void callbackToSecondTimeout() {
		// TODO: ��������֤ �Ƿ��ȡ���
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

	public void setMissPackageRange(int start, int end) {
		startMissPackageIndex = start;
		endMissPackageIndex = end;
	}
}
