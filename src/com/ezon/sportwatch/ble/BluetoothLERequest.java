package com.ezon.sportwatch.ble;

import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;

/**
 * ��������
 * 
 * @author yxy
 * 
 */
public class BluetoothLERequest {

	/***************************** ���󷽷� ****************************/

	private static byte[] codes = new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
			(byte) 0x08, (byte) 0x09 };

	/**
	 * ���������
	 * 
	 * @param callback
	 *            ���state ����STATUS_SUCEESS �򷵻������
	 * 
	 * @return
	 */
	public static boolean sendMatchCode(final OnBleRequestCallback<String> callback) {
		byte[] value = getByte();
		value[0] = 'C';
		value[1] = (byte) 0x00;
		final byte[] code = new byte[4];
		int temp = 0;
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			temp = codes[(int) (Math.random() * 10)];
			sb.append(temp);
			code[i] = (byte) temp;
		}
		System.arraycopy(code, 0, value, 2, 4);

		return BLEManager.getInstance().writeToWatch(value, BLEManager.READ_ACTION_MATCHCODE, new OnBleRequestCallback<String>() {

			@Override
			public void onCallback(int status, String t) {
				if (status == STATUS_SUCEESS) {
					callback.onCallback(status, sb.toString());
					return;
				}
				callback.onCallback(status, t);
			}
		});
	}

	/**
	 * ����Ƿ���������
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean checkNewData(OnBleRequestCallback<Boolean> callback) {
		byte[] value = getByte();
		value[0] = 'C';
		value[1] = (byte) 0x04;
		return BLEManager.getInstance().writeToWatch(value, BLEManager.READ_ACTION_CHECK_NEW_DATA, callback);
	}

	/**
	 * ��ȡ�ļ��б�
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean getFileList() {
		byte[] value = getByte();
		value[0] = 'C';
		value[1] = (byte) 0x01;
		return BLEManager.getInstance().writeToWatch(value, BLEManager.READ_ACTION_GET_FILE_LIST, null);
	}

	/**
	 * ��ȡ�ļ�����
	 * 
	 * @param charac
	 * @return
	 */
	// public static boolean getFileData(FileNameHolder holder) {
	// byte[] value = getByte();
	// value[0] = 'C';
	// value[1] = (byte) 0x02;
	// // fileName + Timezone
	// System.arraycopy(holder.fileNameCode, 0, value, 2, 5);
	// return BLEManager.getInstance().writeToWatch(value,
	// BLEManager.READ_ACTION_GET_FILE_DATA);
	// }

	/**
	 * ��ȡ�豸������
	 */
	public static boolean getDeviceTypeCode(OnBleRequestCallback<String> callback) {
		byte[] value = getByte();
		value[0] = 'C';
		value[1] = (byte) 0x00;
		return BLEManager.getInstance().writeToWatch(value, BLEManager.READ_ACTION_GETTYPECODE, callback);
	}

	/**
	 * ��������
	 * 
	 * @param charac
	 * @return
	 */
	public static boolean clearMatchCode(OnBleRequestCallback<String> callback) {
		return getDeviceTypeCode(callback);
	}

	private static byte[] getByte() {
		byte[] b = new byte[20];
		for (int i = 0; i < b.length; i++) {
			b[i] = 0000;
		}
		return b;
	}

}
