package com.ezon.sportwatch.ble;

import java.util.Vector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Handler;

import com.ezon.sportwatch.ble.action.IDataAction;
import com.ezon.sportwatch.ble.callback.OnBlueToothOpenResultListener;
import com.ezon.sportwatch.ble.outercallback.OnDeviceConnectListener;
import com.ezon.sportwatch.ble.util.BleUtils;
import com.ezon.sportwatch.ble.util.LogPrinter;

public class BluetoothDataParser {
	// ����MSG�̵߳�handler
	private static Handler mWriteThreadHanlder;
	private static Handler mPendingMultileResultHanlder;

	private static Vector<WatchMsg> msgVec = new Vector<WatchMsg>();

	private WriteMsgThread mThread;
	private boolean isThreadLoop = true;

	public BluetoothDataParser() {
		mWriteThreadHanlder = new Handler();
		mPendingMultileResultHanlder = new Handler();
		initMsgThread();
	}

	private void initMsgThread() {
		if (mThread == null || !isThreadLoop) {
			isThreadLoop = true;
			mThread = new WriteMsgThread();
			mThread.start();
		}
	}

	protected synchronized <T> boolean writeDataToWatch(IDataAction<T> action) {
		WatchMsg msg = new WatchMsg();
		msg.action = action;
		action.setBluetoothDataParser(this);
		boolean isAdd = msgVec.add(msg);
		if (isAdd && !isOperationMsg) {
			notifyWriteNext();
		}
		return isAdd;
	}

	public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
		return BLEManager.getInstance().getBluetoothGattCharacteristic();
	}

	public BluetoothGatt getBluetoothGatt() {
		return BLEManager.getInstance().getBluetoothGatt();
	}

	private void realWriteToWatch(WatchMsg msg) {
		if (getBluetoothGattCharacteristic() == null || msg == null || msg.action == null) {
			return;
		}
		getBluetoothGattCharacteristic().setValue(msg.action.onBodyData());
		getBluetoothGatt().readCharacteristic(getBluetoothGattCharacteristic());
		readyToWriteCharacteristic();
		getBluetoothGatt().writeCharacteristic(getBluetoothGattCharacteristic());
	}

	private void readyToWriteCharacteristic() {
		mCurrMsg.action.readyWrite();
	}

	// ȫ�� ����ָ��
	private final String ERROR_GLOBAL_PREFIX = "CQEBLEINFOERROR";

	private boolean interceptErrorInfo(byte[] data) {
		byte[] errorData = new byte[ERROR_GLOBAL_PREFIX.length()];
		System.arraycopy(data, 0, errorData, 0, ERROR_GLOBAL_PREFIX.length());
		// ȫ�� ����ָ�� ����
		if (ERROR_GLOBAL_PREFIX.equals(BleUtils.byteArrayToString(errorData, ERROR_GLOBAL_PREFIX.length()))) {
			LogPrinter.println("intercept error info");
			return true;
		}
		return false;
	}

	private void pendingMultileResultTimeoutRunnable() {
		mPendingMultileResultHanlder.postDelayed(new Runnable() {

			@Override
			public void run() {
				mCurrMsg.action.callbackToSecondTimeout();
			}
		}, 2000);
	}

	private void removeTimeoutRunnable() {
		mPendingMultileResultHanlder.removeMessages(0);
	}

	/**
	 * �������ص�����
	 */
	public void resolveReadData(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		LogPrinter.println("uuid :" + characteristic.getUuid().toString());
		final byte[] data = characteristic.getValue();
		if (data == null || data.length == 0) {
			return;
		}
		if (interceptErrorInfo(data)) {
			return;
		}
		// ���²��� ���� �����̴߳��� �����ٶ�
		removeThreadHandlerMsg();
		LogPrinter.println("onCharacteristicChanged : " + BleUtils.byteArrayToHexString(data));
		LogPrinter.println("onCharacteristicChanged : " + new String(data));
		boolean isMultileResult = mCurrMsg.action.isMultileResult();
		if (isMultileResult) {
			removeTimeoutRunnable();
		}
		mCurrMsg.action.onParserResultData(data);
		if (isMultileResult) {
			pendingMultileResultTimeoutRunnable();
		}
	}

	// ��ǰMSG
	private WatchMsg mCurrMsg;
	// �߳�ͬ��������
	private Object syncObj = new Object();
	// ���ڴ���MSG
	private boolean isOperationMsg = false;
	// ���Դ���
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
	 * д��һ��
	 * 
	 */
	public void notifyWriteNext() {
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
		if (!BLEManager.getInstance().isBluetoothAdapterEnable()) {
			BLEManager.getInstance().openBluetooth(new OnBlueToothOpenResultListener() {

				@Override
				public void onOpenResult(boolean isOpen) {
					if (isOpen) {
						writeMsg();
					} else {
						mCurrMsg.action.callbackResultFail();
					}
				}
			});
			return;
		}
		if (!BLEManager.getInstance().isChannelWriteEnable()) {
			// ��������
			LogPrinter.println("need reconnected ");
			if (!BLEManager.getInstance().reconnect(new OnDeviceConnectListener() {

				@Override
				public void onConnect(int state, BluetoothDevice device) {
					switch (state) {
					case DEVICE_CONNECTED:
						writeMsg();
						break;
					case DEVICE_CONNECT_FAIL:
						mCurrMsg.action.callbackResultFail();
						break;
					}
				}
			})) {
				mCurrMsg.action.callbackResultFail();
			}
			return;
		}
		// ���Դ����ﵽ3�� �� ����������ʧ��
		if (retry >= 3) {
			mCurrMsg.action.callbackResultFail();
			return;
		}
		realWriteToWatch(mCurrMsg);
		// ��������û�����ݷ��� �����ػ����removeThreadHandlerMsg()��������ִ������
		mWriteThreadHanlder.postDelayed(new Runnable() {

			@Override
			public void run() {
				writeMsg();
				retry++;
				LogPrinter.println("retry.,,,,,," + retry);
			}
		}, 2000);
	}

	public void removeThreadHandlerMsg() {
		mWriteThreadHanlder.removeMessages(0);
	}

	private class WatchMsg {
		IDataAction action;
	}

	public void destory() {
		if (mThread != null) {
			isThreadLoop = false;
			mThread.notify();
			mThread = null;
		}
	}
}
