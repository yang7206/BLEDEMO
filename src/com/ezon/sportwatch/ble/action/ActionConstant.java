package com.ezon.sportwatch.ble.action;

public class ActionConstant {
	// write action
	//配对码
	public static final int READ_ACTION_MATCHCODE = 1;
	//清空配对码
	public static final int READ_ACTION_CLEAR_MATCH_CODE = 2;
	//获取类型码
	public static final int READ_ACTION_GETTYPECODE = 3;
	//获取文件数据
	public static final int READ_ACTION_GET_FILE_DATA = 4;
	//获取文件列表
	public static final int READ_ACTION_GET_FILE_LIST = 5;
	//检查是否有新数据
	public static final int READ_ACTION_CHECK_NEW_DATA = 6;
	
	
	//获取漏包
	public static final int READ_ACTION_GET_MISS_PACKAGE_DATA = 7;
	//获取文件的签到点
	public static final int READ_ACTION_GET_CHECKIN_POINTER = 8;
}
