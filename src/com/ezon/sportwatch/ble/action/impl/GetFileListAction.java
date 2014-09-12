package com.ezon.sportwatch.ble.action.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ezon.sportwatch.ble.action.BaseAction;
import com.ezon.sportwatch.ble.action.ActionConstant;
import com.ezon.sportwatch.ble.action.entity.FileNameHolder;
import com.ezon.sportwatch.ble.util.BleUtils;
import com.ezon.sportwatch.ble.util.LogPrinter;

public class GetFileListAction extends BaseAction<List<FileNameHolder>> {

	private List<FileNameHolder> fileHolderList = new ArrayList<FileNameHolder>();

	public GetFileListAction() {
		setAction(ActionConstant.READ_ACTION_GET_FILE_LIST);
	}

	@Override
	public void onPrepareBodyData(byte[] data) {
		data[0] = 'C';
		data[1] = (byte) 0x01;
	}

	@Override
	public void onParserResultData(byte[] data) {
		byte filePackage[] = new byte[4];
		System.arraycopy(data, 0, filePackage, 0, 4);
		if (!"FILE".equals(new String(filePackage))) {
			// TODO :需要重试
			return;
		}
		int packageIndex = BleUtils.byteToHexInt(data[4]);
		for (int i = 1; i < 4; i++) {
			FileNameHolder holder = new FileNameHolder();
			byte fileName[] = new byte[5];
			System.arraycopy(data, i * 5, fileName, 0, 5);
			String date = BleUtils.byteToHexInt(fileName[0]) + "-" + BleUtils.byteToHexInt(fileName[1]) + "-" + BleUtils.byteToHexInt(fileName[2]);
			SimpleDateFormat format = BleUtils.getFormatter();
			try {
				holder.setFileDate(format.format(format.parse(date)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			holder.setFilePackageIndex(packageIndex);
			holder.setFileNameCode(fileName);
			LogPrinter.println("fileName :" + new String(fileName));
			if (!"0000000000".equals(BleUtils.byteArrayToHexString(fileName))) {
				fileHolderList.add(holder);
				holder.diyplay();
			}
		}
		// TODO:也可以在这里验证 包是否已经全部读完
	}

	@Override
	public void callbackToSecondTimeout() {
		// TODO: 这里来验证 是否读取完成
		callbackResultSuccess(fileHolderList);
	}

	@Override
	public boolean isMultileResult() {
		return true;
	}

	@Override
	public void readyWrite() {
		fileHolderList.clear();
	}
}
