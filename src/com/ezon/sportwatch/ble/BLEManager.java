package com.ezon.sportwatch.ble;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;

import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;
import com.ezon.sportwatch.ble.callback.OnBlueToothOpenResultListener;
import com.ezon.sportwatch.ble.callback.OnBluetoothDeviceSearchListener;
import com.ezon.sportwatch.ble.util.BleUtils;
import com.ezon.sportwatch.ble.util.LogPrinter;

public class BLEManager {
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic;

	// connect state
	private int mConnectionState = STATE_DISCONNECTED;
	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECT_REAYD = 2;
	private static final int STATE_CONNECTED = 3;
	private static final int STATE_SERVICE_DISCOVERD = 4;
	private static final int STATE_CONNECT_FAIL = -1;
	private static final int STATE_CONNECT_FAIL_MAYBY_BT_CLOSED = -2;

	/********************** singleton ***********************/
	private static BLEManager mInstance;

	private BLEManager() {
		initMsgThread();
	}

	public static BLEManager getInstance() {
		if (mInstance == null) {
			mInstance = new BLEManager();
		}
		return mInstance;
	}

	private void connectionStateOperation(int state, BluetoothGatt gatt) {
		mConnectionState = state;
		switch (mConnectionState) {
		case STATE_CONNECT_REAYD:
			cancelPrvConnect();
			break;
		case STATE_CONNECTING:
			break;
		case STATE_CONNECTED:
			mBluetoothGatt.discoverServices();
			break;
		case STATE_SERVICE_DISCOVERD:
			LogPrinter.println("onServicesDiscovered gatt :" + gatt);
			getConnectWriteableChannel(gatt);
			mConnectHandler.removeMessages(0);
			break;
		case STATE_CONNECT_FAIL:
			// TODO :失败 可能 ： 连接设备已经断开
			LogPrinter.println("onServicesDiscovered fail: ");
			break;
		case STATE_DISCONNECTED:
			clearInitParams(gatt);
			LogPrinter.println("Disconnected from GATT server." + gatt.getDevice().getAddress());
			break;
		case STATE_CONNECT_FAIL_MAYBY_BT_CLOSED:
			//TODO :蓝牙连接已经断开
			break;
		default:
			break;
		}
	}

	private boolean isChannelEnable() {
		return mConnectionState == STATE_SERVICE_DISCOVERD;
	}

	private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				connectionStateOperation(STATE_CONNECTED, gatt);
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				connectionStateOperation(STATE_DISCONNECTED, gatt);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO : 调用discoverServices 10s内 没有返回 可能需要重新连接
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// service discovered success is real connect success
				connectionStateOperation(STATE_SERVICE_DISCOVERD, gatt);
			} else {
				connectionStateOperation(STATE_CONNECT_FAIL, gatt);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogPrinter.println("onCharacteristicRead status :" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				LogPrinter.println("-------------------------------------");
				LogPrinter.println("uuid :" + characteristic.getUuid().toString());
				final byte[] data = characteristic.getValue();
				if (data != null && data.length > 0) {
					final StringBuilder stringBuilder = new StringBuilder(data.length);
					for (byte byteChar : data)
						stringBuilder.append(String.format("%02X ", byteChar));
					LogPrinter.println("onCharacteristicRead : " + new String(data) + "\n" + stringBuilder.toString());
				}
			}
		}

		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			LogPrinter.println("onCharacteristicWrite status :" + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				LogPrinter.println("*************************************");
				LogPrinter.println("uuid :" + characteristic.getUuid().toString());
				final byte[] data = characteristic.getValue();
				if (data != null && data.length > 0) {
					LogPrinter.println("onCharacteristicWrite : " + BleUtils.byteArrayToHexString(data));
				}
			}
		}

		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			LogPrinter.println("onCharacteristicChanged*********************************");
			resolveReadData(gatt, characteristic);
		}

	};

	private void cancelPrvConnect() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	/**
	 * 获取 可写通道
	 * 
	 * @param gatt
	 */
	private void getConnectWriteableChannel(BluetoothGatt gatt) {
		List<BluetoothGattService> services = gatt.getServices();
		for (BluetoothGattService gattService : services) {
			if (isEzonChannel(gattService.getUuid().toString())) {
				for (BluetoothGattCharacteristic item : gattService.getCharacteristics()) {
					if (isEzonChannel(item.getUuid().toString())) {
						// 找到可写渠道
						serviceDiscover(gatt, item);
						return;
					}
				}
			}
		}
		// 没有找到可写渠道 按失败处理
		connectionStateOperation(STATE_CONNECT_FAIL, mBluetoothGatt);
	}

	/**
	 * 发现通道
	 * 
	 * @param gatt
	 * @param characteristic
	 */
	private void serviceDiscover(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		resetNotifycation();
		mBluetoothGattCharacteristic = characteristic;
		LogPrinter.println(gatt.getDevice().getAddress() + " is connectd");
	}

	private void resetNotifycation() {
		isSetNotifycation = false;
	}

	private boolean isSetNotifycation = false;
	// 回调
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	private void setNotifycation(BluetoothGattCharacteristic charac) {
		if (isSetNotifycation)
			return;
		isSetNotifycation = true;
		mBluetoothGatt.setCharacteristicNotification(charac, true);
		BluetoothGattDescriptor descriptor = charac.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	private void clearInitParams(final BluetoothGatt gatt) {
		isSetNotifycation = false;
		LogPrinter.println(gatt.getDevice().getAddress() + " is disconnectd");
	}

	private static List<String> attributes = new ArrayList<String>();
	static {
		// Sample Services.
		attributes.add("0000ffb0");
		attributes.add("0000ffb2");
	}

	/**
	 * EZON读写通道
	 * 
	 * @param uuid
	 * @return
	 */
	private static boolean isEzonChannel(String uuid) {
		for (String prefix : attributes) {
			if (uuid.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public void connect(BluetoothDevice device) {
		connectionStateOperation(STATE_CONNECT_REAYD, null);
		LogPrinter.println("new connect");
		connectionStateOperation(STATE_CONNECTING, null);
		mBluetoothGatt = device.connectGatt(mApplication, false, mBluetoothGattCallback);
		LogPrinter.println("mBluetoothGatt :" + mBluetoothGatt);
		if (mBluetoothGatt == null) {
			// TODO : 如果返回空 可能蓝牙已关闭 统一处理 mConnectionState；
			connectionStateOperation(STATE_CONNECT_FAIL_MAYBY_BT_CLOSED, null);
		} else {
			mConnectHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// 超过10S 按失败处理
					connectionStateOperation(STATE_CONNECT_FAIL, null);
				}
			}, 10000);
		}
	}

	protected BluetoothAdapter getAdapter() {
		BluetoothManager bluetoothManager = (BluetoothManager) mApplication.getSystemService(Context.BLUETOOTH_SERVICE);
		return bluetoothManager.getAdapter();
	}

	/******************************* 对外方法 **********************************/

	public boolean isEnableBle() {
		return BleUtils.isSupportBle(mApplication);
	}

	private static Vector<WatchMsg> msgVec = new Vector<WatchMsg>();

	protected synchronized <T> boolean writeToWatch(byte[] data, int action, OnBleRequestCallback<T> t) {
		WatchMsg msg = new WatchMsg();
		msg.data = data;
		msg.action = action;
		msg.calback = t;
		boolean isAdd = msgVec.add(msg);
		if (isAdd && !isOperationMsg) {
			notifyWriteNext();
		}
		return isAdd;
	}

	// write action
	public static final int READ_ACTION_MATCHCODE = 1;
	public static final int READ_ACTION_CLEAR_MATCH_CODE = 2;
	public static final int READ_ACTION_GETTYPECODE = 3;
	public static final int READ_ACTION_GET_FILE_DATA = 4;
	public static final int READ_ACTION_GET_FILE_LIST = 5;
	public static final int READ_ACTION_CHECK_NEW_DATA = 6;

	private void realWriteToWatch(WatchMsg msg) {
		if (mBluetoothGattCharacteristic == null || msg == null || msg.data == null) {
			return;
		}
		mBluetoothGattCharacteristic.setValue(msg.data);
		mBluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic);
		setNotifycation(mBluetoothGattCharacteristic);
		readyToWriteCharacteristic();
		mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic);
	}

	private void readyToWriteCharacteristic() {
		switch (mCurrMsg.action) {
		case READ_ACTION_GET_FILE_LIST:
			fileHolderList.clear();
			break;
		case READ_ACTION_GET_FILE_DATA:
			packageSum = 0;
			break;
		}
	}

	private List<FileNameHolder> fileHolderList = new ArrayList<FileNameHolder>();

	private class FileNameHolder {
		int filePackageIndex;
		byte[] fileNameCode;
		String fileDate;

		void diyplay() {
			LogPrinter.println("fileIndex :" + filePackageIndex + ",fileDate :" + fileDate + ",fileNameCode:" + fileNameCode);
		}
	}

	private int packageSum = 0;
	// 全局 错误指令
	private final String ERROR_GLOBAL_PREFIX = "CQEBLEINFOERROR";

	/**
	 * 解析返回的数据
	 */
	private void resolveReadData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		LogPrinter.println("uuid :" + characteristic.getUuid().toString());
		final byte[] data = characteristic.getValue();
		if (data == null || data.length == 0) {
			return;
		}
		// 全局 错误指令 忽略
		if (BleUtils.byteArrayToHexString(data).startsWith(ERROR_GLOBAL_PREFIX)) {
			return;
		}
		// 以下部分 可以 交给线程处理 提升速度
		removeThreadHandlerMsg();
		LogPrinter.println("onCharacteristicChanged : " + BleUtils.byteArrayToHexString(data));
		LogPrinter.println("onCharacteristicChanged : " + new String(data));
		byte[] b = null;
		switch (mCurrMsg.action) {
		case READ_ACTION_MATCHCODE:
			b = new byte[3];
			b[0] = data[0];
			b[1] = data[2];
			b[2] = data[3];
			if ("COK".equalsIgnoreCase(new String(b))) {
				mCurrMsg.callbackResultSuccess(null);
			} else {
				mCurrMsg.callbackResultFail();
			}
			break;
		case READ_ACTION_CLEAR_MATCH_CODE:
		case READ_ACTION_GETTYPECODE:
			b = new byte[4];
			System.arraycopy(data, 0, b, 0, 4);
			final StringBuffer sb = new StringBuffer();
			sb.append(new String(b));
			sb.append(Integer.toHexString(data[4]));
			sb.append(Integer.toHexString(data[5]));
			sb.append(Integer.toHexString(data[6]));
			sb.append(Integer.toHexString(data[7]));
			if (sb.toString().startsWith("EZON")) {
				mCurrMsg.callbackResultSuccess(sb.toString());
			} else {
				mCurrMsg.callbackResultFail();
			}
			break;
		case READ_ACTION_GET_FILE_DATA:
			byte pag[] = new byte[1];
			pag[0] = data[0];
			if ("P".equals(new String(pag))) {
				packageSum++;
				LogPrinter.println("packageSum :" + packageSum);
				// TODO :
				return;
			}
			final String _ErrorStr = "FILENAMEERROR";
			byte error[] = new byte[_ErrorStr.length()];
			System.arraycopy(data, 0, error, 0, _ErrorStr.length());
			if (_ErrorStr.equals(new String(error))) {
				// TODO :数据 错误
			}
			break;
		case READ_ACTION_GET_FILE_LIST:
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
					holder.fileDate = format.format(format.parse(date));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				holder.filePackageIndex = packageIndex;
				holder.fileNameCode = fileName;
				LogPrinter.println("fileName :" + new String(fileName));
				if (!"0000000000".equals(BleUtils.byteArrayToHexString(fileName))) {
					fileHolderList.add(holder);
					holder.diyplay();
				}
			}
			break;
		case READ_ACTION_CHECK_NEW_DATA:
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
				mCurrMsg.callbackResultSuccess(isHaveNewData);
			} else {
				mCurrMsg.callbackResultFail();
			}
			break;
		default:
			break;
		}
	}

	/**************************** BLE搜索 ******************************/
	private BluetoothLESearcher mSearcher;

	public void startSearch(OnBluetoothDeviceSearchListener listener) {
		if (mSearcher == null) {
			mSearcher = new BluetoothLESearcher(this);
		}
		mSearcher.startSearch(listener);
	}

	/**************************** 蓝牙打开器 ******************************/
	private BlueToothOpener mOpener;

	protected void openBluetooth(OnBlueToothOpenResultListener listener) {
		if (mOpener == null) {
			mOpener = new BlueToothOpener();
		}
		mOpener.openBlueTooth(listener);
	}

	protected BlueToothOpener getBlueToothOpener() {
		return mOpener;
	}

	private static Application mApplication;
	// 其他或对外handler
	private static Handler mHandler;
	// 处理连接的handler
	private static Handler mConnectHandler;
	// 处理MSG线程的handler
	private static Handler mThreadHanlder;

	public static void initApplication(Application app) {
		mApplication = app;
		mConnectHandler = new Handler();
		mThreadHanlder = new Handler();
		mHandler = new Handler(app.getMainLooper());
	}

	public static Application getApplication() {
		return mApplication;
	}

	public static Handler getHandler() {
		return mHandler;
	}

	private WriteMsgThread mThread;
	private boolean isThreadLoop = true;

	private void initMsgThread() {
		if (mThread == null || !isThreadLoop) {
			isThreadLoop = true;
			mThread = new WriteMsgThread();
			mThread.start();
		}
	}

	public void destory() {
		if (mThread != null) {
			isThreadLoop = false;
			mThread.notify();
			mThread = null;
		}
	}

	// 当前MSG
	private WatchMsg mCurrMsg;
	// 线程同步锁对象
	private Object syncObj = new Object();
	// 正在处理MSG
	private boolean isOperationMsg = false;
	// 重试次数
	private int retry = 0;

	private class WriteMsgThread extends Thread {

		@Override
		public void run() {
			while (isThreadLoop) {
				if (msgVec.size() > 0) {
					readyWriteMsg();
				}
				try {
					synchronized (syncObj) {
						syncObj.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * 写下一个
	 * 
	 */
	private void notifyWriteNext() {
		isOperationMsg = false;
		if (mThread != null) {
			synchronized (syncObj) {
				syncObj.notify();
			}
		}
	}

	private void readyWriteMsg() {
		if (isOperationMsg)
			return;
		isOperationMsg = true;
		retry = 0;
		mCurrMsg = msgVec.firstElement();
		msgVec.remove(mCurrMsg);
		writeMsg();
	}

	private void writeMsg() {
		// 重试次数达到3次 或 渠道不可用失败
		if (retry >= 3 || !isChannelEnable()) {
			mCurrMsg.callbackResultFail();
			return;
		}
		realWriteToWatch(mCurrMsg);
		// 如果两秒后没有数据返回 （返回会调用removeThreadHandlerMsg()方法），执行重试
		mThreadHanlder.postDelayed(new Runnable() {

			@Override
			public void run() {
				writeMsg();
				retry++;
				LogPrinter.println("retry.,,,,,," + retry);
			}
		}, 2000);
	}

	private void removeThreadHandlerMsg() {
		mThreadHanlder.removeMessages(0);
	}

	@SuppressWarnings("rawtypes")
	private class WatchMsg {
		byte[] data;
		int action;
		OnBleRequestCallback calback;

		private <T> void callbackResultSuccess(final T t) {
			removeThreadHandlerMsg();
			mHandler.post(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					if (mCurrMsg.calback != null) {
						mCurrMsg.calback.onCallback(OnBleRequestCallback.STATUS_SUCEESS, t);
					}
					notifyWriteNext();
				}
			});
		}

		private void callbackResultFail() {
			removeThreadHandlerMsg();
			mHandler.post(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					if (mCurrMsg.calback != null) {
						mCurrMsg.calback.onCallback(OnBleRequestCallback.STATUS_FAIL, null);
					}
					notifyWriteNext();
				}
			});
		}
	}

}
