package com.ezon.sportwatch.ble;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;

import com.ezon.sportwatch.ble.callback.OnBlueToothOpenResultListener;
import com.ezon.sportwatch.ble.config.WriteableChannelConfig;
import com.ezon.sportwatch.ble.outercallback.OnDeviceConnectListener;
import com.ezon.sportwatch.ble.util.BleUtils;
import com.ezon.sportwatch.ble.util.LogPrinter;

public class BluetoothleConnector {
	private BluetoothDevice mDevice;
	private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
	// �������ӵ�handler
	private static Handler mConnectHandler;
	private OnDeviceConnectListener mConnectListener;
	private OnDeviceConnectListener mGlobalConnectListener;

	protected BluetoothleConnector() {
		mConnectHandler = new Handler();
	}

	// connect state
	private int mConnectionState = STATE_DISCONNECTED;
	// ���ֿ�д���� �ǿ��Զ�д, ������˵ �������ӳɹ�
	private static final int STATE_CONNECTED = 0;
	private static final int STATE_CONNECT_FAIL_MAYBY_BT_CLOSED = -1;
	private static final int STATE_CONNECT_FAIL = -2;
	private static final int STATE_DISCONNECTED = -3;
	private static final int STATE_CONNECT_RETRY_FAIL = -4;
	// �ڲ�ʹ��
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECT_REAYD = 2;
	private static final int STATE_DEVICE_CONNECTED = 3;
	private static final int STATE_SERVICE_DISCOVERD = 4;

	private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				connectionStateOperation(STATE_DEVICE_CONNECTED, gatt);
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				connectionStateOperation(STATE_DISCONNECTED, gatt);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
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
			BLEManager.getInstance().resolveReadData(gatt, characteristic);
		}

	};
	

	private void connectionStateOperation(int state, BluetoothGatt gatt) {
		mConnectionState = state;
		switch (mConnectionState) {
		case STATE_CONNECTED:
			removeConnectHandlerPendingMsg();
			callbackDeviceConnect(OnDeviceConnectListener.DEVICE_CONNECTED);
			break;
		// �������ӿ����Ѿ��Ͽ�
		case STATE_CONNECT_FAIL_MAYBY_BT_CLOSED:
			if (!BLEManager.getInstance().isBluetoothAdapterEnable()) {
				BLEManager.getInstance().openBluetooth(new OnBlueToothOpenResultListener() {

					@Override
					public void onOpenResult(boolean isOpen) {
						if (isOpen) {
							resetRetry();
							reconnect();
						} else {
							connectionStateOperation(STATE_CONNECT_RETRY_FAIL, mBluetoothGatt);
						}
					}
				});
				return;
			}
			LogPrinter.println("STATE_CONNECT_FAIL_MAYBY_BT_CLOSED");
		case STATE_CONNECT_RETRY_FAIL:
			// ����ʧ�� �����ⲿ��˵ ������ʧ��
			callbackDeviceConnect(OnDeviceConnectListener.DEVICE_CONNECT_FAIL);
			break;
		// ʧ�� ���� �� �����豸�Ѿ��Ͽ�
		case STATE_CONNECT_FAIL:
			LogPrinter.println("onServicesDiscovered fail: ");
			reconnect();
			break;
		case STATE_DISCONNECTED:
			LogPrinter.println("Disconnected from GATT server." + gatt.getDevice().getAddress());
			reconnect();
			break;
		case STATE_CONNECT_REAYD:
			cancelPrvConnect();
			break;
		case STATE_CONNECTING:
			break;
		case STATE_DEVICE_CONNECTED:
			mBluetoothGatt.discoverServices();
			break;
		case STATE_SERVICE_DISCOVERD:
			LogPrinter.println("onServicesDiscovered gatt :" + gatt);
			getConnectWriteableChannel(gatt);
			break;
		}
	}

	private void cancelPrvConnect() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	/**
	 * ��ȡ ��дͨ��
	 * 
	 * @param gatt
	 */
	private void getConnectWriteableChannel(BluetoothGatt gatt) {
		List<BluetoothGattService> services = gatt.getServices();
		for (BluetoothGattService gattService : services) {
			if (WriteableChannelConfig.isEzonChannel(gattService.getUuid().toString())) {
				for (BluetoothGattCharacteristic item : gattService.getCharacteristics()) {
					if (WriteableChannelConfig.isEzonChannel(item.getUuid().toString())) {
						// �ҵ���д����
						connectSuccess(gatt, item);
						return;
					}
				}
			}
		}
		// û���ҵ���д���� ��ʧ�ܴ���
		connectionStateOperation(STATE_CONNECT_FAIL, mBluetoothGatt);
	}

	/**
	 * ����ͨ��
	 * 
	 * @param gatt
	 * @param characteristic
	 */
	private void connectSuccess(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		// ���ֿ�д���� �������壬��������˵���� �������ӳɹ�
		setNotifycation(characteristic);
		mBluetoothGattCharacteristic = characteristic;
		connectionStateOperation(STATE_CONNECTED, gatt);
		LogPrinter.println(gatt.getDevice().getAddress() + " is connectd");
	}

	// �ص�
	public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

	private void setNotifycation(BluetoothGattCharacteristic charac) {
		mBluetoothGatt.setCharacteristicNotification(charac, true);
		BluetoothGattDescriptor descriptor = charac.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	private void callbackDeviceConnect(int state) {
		LogPrinter.println("callbackDeviceConnect :" + state);
		if (mConnectListener != null) {
			mConnectListener.onConnect(state, mDevice);
		}
		if (mGlobalConnectListener != null) {
			mGlobalConnectListener.onConnect(state, mDevice);
		}
	}

	private void removeConnectHandlerPendingMsg() {
		mConnectHandler.removeMessages(0);
	}

	private boolean interceptConnectedDevice(BluetoothDevice device) {
		if (mBluetoothGatt != null && mBluetoothGatt.getDevice() == device && isChannelWriteEnable()) {
			LogPrinter.println("device is connected");
			connectionStateOperation(STATE_CONNECTED, mBluetoothGatt);
			return true;
		}
		return false;
	}

	/****************** ���ⷽ�� ************************/

	private int retry = 0;
	private final int RETRY_MAX = 1;

	public boolean reconnect() {
		if (mDevice == null || retry > RETRY_MAX) {
			resetRetry();
			connectionStateOperation(STATE_CONNECT_RETRY_FAIL, mBluetoothGatt);
			return false;
		}
		retry++;
		connectGatt(mDevice);
		return true;
	}

	public boolean reconnect(OnDeviceConnectListener listener) {
		mConnectListener = listener;
		if (mDevice == null || retry > RETRY_MAX) {
			resetRetry();
			connectionStateOperation(STATE_CONNECT_RETRY_FAIL, mBluetoothGatt);
			return false;
		}
		retry++;
		connectGatt(mDevice);
		return true;
	}

	private void resetRetry() {
		retry = 0;
	}

	public void connect(BluetoothDevice device, OnDeviceConnectListener listener) {
		mGlobalConnectListener = listener;
		if (interceptConnectedDevice(device)) {
			return;
		}
		resetRetry();
		LogPrinter.println("new connect");
		connectionStateOperation(STATE_CONNECT_REAYD, null);
		connectionStateOperation(STATE_CONNECTING, null);
		mDevice = device;
		connectGatt(device);
	}

	private void connectGatt(BluetoothDevice device) {
		mBluetoothGatt = device.connectGatt(BLEManager.getApplication(), false, mBluetoothGattCallback);
		LogPrinter.println("mBluetoothGatt :" + mBluetoothGatt);
		if (mBluetoothGatt == null) {
			// TODO : ������ؿ� ���������ѹر� ͳһ���� mConnectionState��
			connectionStateOperation(STATE_CONNECT_FAIL_MAYBY_BT_CLOSED, null);
		} else {
			mConnectHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// ����10S ��ʧ�ܴ���
					LogPrinter.println("connect fail 10s............");
					connectionStateOperation(STATE_CONNECT_FAIL, null);
				}
			}, 10000);
		}
	}

	public BluetoothGatt getBluetoothGatt() {
		return mBluetoothGatt;
	}

	public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
		return mBluetoothGattCharacteristic;
	}

	public BluetoothDevice getBluetoothDevice() {
		return mDevice;
	}

	public boolean isChannelWriteEnable() {
		return mConnectionState == STATE_CONNECTED;
	}
}
