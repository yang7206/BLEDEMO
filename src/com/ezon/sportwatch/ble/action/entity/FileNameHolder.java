package com.ezon.sportwatch.ble.action.entity;

import com.ezon.sportwatch.ble.util.LogPrinter;

public class FileNameHolder {
	private int filePackageIndex;
	private byte[] fileNameCode;
	private String fileDate;

	public void diyplay() {
		LogPrinter.println("fileIndex :" + filePackageIndex + ",fileDate :" + fileDate + ",fileNameCode:" + fileNameCode);
	}

	public int getFilePackageIndex() {
		return filePackageIndex;
	}

	public void setFilePackageIndex(int filePackageIndex) {
		this.filePackageIndex = filePackageIndex;
	}

	public byte[] getFileNameCode() {
		return fileNameCode;
	}

	public void setFileNameCode(byte[] fileNameCode) {
		this.fileNameCode = fileNameCode;
	}

	public String getFileDate() {
		return fileDate;
	}

	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}

}
