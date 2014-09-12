package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ezon.sportwatch.ble.BLEManager;
import com.ezon.sportwatch.ble.BluetoothLERequest;
import com.ezon.sportwatch.ble.action.entity.FileNameHolder;
import com.ezon.sportwatch.ble.callback.OnBleRequestCallback;
import com.ezon.sportwatch.ble.callback.OnBluetoothDeviceSearchListener;
import com.ezon.sportwatch.ble.outercallback.OnDeviceConnectListener;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener {
	private Handler mHandler;

	private DeviceAdapter mLeDeviceListAdapter;
	private TextView currdevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.checkbleavailable).setOnClickListener(this);
		findViewById(R.id.scan).setOnClickListener(this);
		findViewById(R.id.sendaskcode).setOnClickListener(this);
		findViewById(R.id.gettypecode).setOnClickListener(this);
		findViewById(R.id.clearaskcode).setOnClickListener(this);
		findViewById(R.id.checknewdata).setOnClickListener(this);
		findViewById(R.id.checkfilelist).setOnClickListener(this);
		findViewById(R.id.getfiledata).setOnClickListener(this);
		findViewById(R.id.getmissingdata).setOnClickListener(this);

		currdevice = (TextView) findViewById(R.id.currdevice);

		mHandler = new Handler(getMainLooper());
		ListView list = (ListView) findViewById(R.id.devicelist);
		mLeDeviceListAdapter = new DeviceAdapter();
		list.setAdapter(mLeDeviceListAdapter);
		list.setOnItemClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkbleavailable:
			checkBleAvailable();
			break;
		case R.id.scan:
			BLEManager.getInstance().startSearch(new OnBluetoothDeviceSearchListener() {

				@Override
				public void onSearch(int action, BluetoothDevice device) {
					switch (action) {
					case SEARCHING_READY:
						mLeDeviceListAdapter.clearDevice();
						break;
					case SEARCHING_PERFORM:
						mLeDeviceListAdapter.addDevice(device);
						break;
					case SEARCHING_DONE:
						break;
					case SEARCH_ERROR_BLUETOOTH_OPENFAIL:
						Toast.makeText(getBaseContext(), "蓝牙打开失败", Toast.LENGTH_SHORT).show();
						break;
					}
				}
			});
			break;
		case R.id.sendaskcode:
			BluetoothLERequest.sendMatchCode(new OnBleRequestCallback<String>() {

				@Override
				public void onCallback(int status, String t) {
					if (status == STATUS_SUCEESS) {
						Toast.makeText(getApplicationContext(), "code :" + t, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "sendMatchCode fail :" + status, Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		case R.id.gettypecode:
			BluetoothLERequest.getDeviceTypeCode(new OnBleRequestCallback<String>() {

				@Override
				public void onCallback(int status, String t) {
					if (status == STATUS_SUCEESS) {
						Toast.makeText(getApplicationContext(), "getDeviceTypeCode :" + t, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "getDeviceTypeCode fail :" + status, Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		case R.id.clearaskcode:
			BluetoothLERequest.clearMatchCode(new OnBleRequestCallback<String>() {

				@Override
				public void onCallback(int status, String t) {
					if (status == STATUS_SUCEESS) {
						Toast.makeText(getApplicationContext(), "getDeviceTypeCode :" + t, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "getDeviceTypeCode fail :" + status, Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		case R.id.checknewdata:
			BluetoothLERequest.checkNewData(new OnBleRequestCallback<Boolean>() {

				@Override
				public void onCallback(int status, Boolean t) {
					if (status == STATUS_SUCEESS) {
						Toast.makeText(getApplicationContext(), "checkNewData :" + t.booleanValue(), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "getDeviceTypeCode fail :" + status, Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		case R.id.checkfilelist:
			BluetoothLERequest.getFileList(new OnBleRequestCallback<List<FileNameHolder>>() {

				@Override
				public void onCallback(int status, List<FileNameHolder> t) {
					if (status == STATUS_SUCEESS) {
						fileHolderList = t;
						Toast.makeText(getBaseContext(), "size :" + t.size(), Toast.LENGTH_SHORT).show();
						for (FileNameHolder holder : t) {
							holder.diyplay();
						}
					} else {
						Toast.makeText(getBaseContext(), "getFileList fail", Toast.LENGTH_SHORT).show();
					}
				}
			});
			break;
		case R.id.getfiledata:
			if (fileHolderList == null) {
				Toast.makeText(getBaseContext(), "call checkfilelist", Toast.LENGTH_SHORT).show();
				return;
			}
			for (int i = 0; i < fileHolderList.size(); i++) {
				final int _temp = i;
				BluetoothLERequest.getFileData(fileHolderList.get(i), new OnBleRequestCallback<Integer>() {

					@Override
					public void onCallback(int status, Integer t) {
						if (status == STATUS_SUCEESS) {
							Toast.makeText(getBaseContext(), "getFileData file _temp:" + _temp + ",no: " + t.intValue(), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getBaseContext(), "getFileData fail file _temp :" + _temp, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
			break;
		case R.id.getmissingdata:
			BluetoothLERequest.getMissingPackageData(10, 15, new OnBleRequestCallback<Integer>() {

				@Override
				public void onCallback(int status, Integer t) {

				}
			});
			break;
		default:
			break;
		}

	}

	private List<FileNameHolder> fileHolderList;

	private void checkBleAvailable() {
		if (BLEManager.getInstance().isEnableBle()) {
			Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "ble_supported", Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		BluetoothDevice device = (BluetoothDevice) mLeDeviceListAdapter.getItem(position);
		BLEManager.getInstance().connect(device, new OnDeviceConnectListener() {

			@Override
			public void onConnect(int state, BluetoothDevice device) {
				String address = device == null ? "" : device.getAddress();
				switch (state) {
				case DEVICE_CONNECTED:
					currdevice.setText("连接成功 :" + address);
					Toast.makeText(getBaseContext(), "连接成功", Toast.LENGTH_SHORT).show();
					break;
				case DEVICE_CONNECT_FAIL:
					currdevice.setText("连接失败 :" + address);
					Toast.makeText(getBaseContext(), "连接失败", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});
	}

	/******************* 适配器 *************************/

	private class DeviceAdapter extends BaseAdapter {

		private List<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BluetoothDevice device = list.get(position);
			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_device, null);
				holder.nameTv = (TextView) convertView.findViewById(R.id.name);
				holder.addressTv = (TextView) convertView.findViewById(R.id.address);
				holder.typeTv = (TextView) convertView.findViewById(R.id.type);
				holder.stateTv = (TextView) convertView.findViewById(R.id.state);
				holder.uuidslayout = (LinearLayout) convertView.findViewById(R.id.uuidslayout);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			System.out.println("getView : " + position);
			bindData(device, holder);
			return convertView;
		}

		private void bindData(BluetoothDevice device, ViewHolder holder) {

			holder.nameTv.setText("Name : " + device.getName());
			holder.addressTv.setText("Address : " + device.getAddress());
			String typeName = "";
			switch (device.getType()) {
			case BluetoothDevice.DEVICE_TYPE_CLASSIC:
				typeName = "CLASSIC";
				break;
			case BluetoothDevice.DEVICE_TYPE_DUAL:
				typeName = "DUAL";
				break;
			case BluetoothDevice.DEVICE_TYPE_LE:
				typeName = "LE";
				break;
			case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
				typeName = "UNKNOWN";
				break;
			}
			holder.typeTv.setText("Type : " + typeName);
			String bondState = "";
			switch (device.getBondState()) {
			case BluetoothDevice.BOND_NONE:
				bondState = "未绑定";
				break;
			case BluetoothDevice.BOND_BONDING:
				bondState = "未绑定中";
				break;
			case BluetoothDevice.BOND_BONDED:
				bondState = "已绑定";
				break;
			}
			holder.stateTv.setText("State : " + bondState);
			holder.uuidslayout.removeAllViews();

			ParcelUuid uuid[] = device.getUuids();
			if (uuid != null && uuid.length > 0) {
				for (int i = 0; i < uuid.length; i++) {
					ParcelUuid parcelUuid = uuid[i];
					TextView tv = new TextView(getBaseContext());
					tv.setText("Uuid" + i + " : " + parcelUuid.getUuid());
					tv.setTextColor(Color.BLACK);
					holder.uuidslayout.addView(tv);
				}
			}
		}

		public void addDevice(BluetoothDevice device) {
			list.add(device);
			notifyDataSetChanged();
		}

		public void clearDevice() {
			list.clear();
		}
	}

	private class ViewHolder {
		TextView nameTv;
		TextView addressTv;
		TextView typeTv;
		TextView stateTv;
		LinearLayout uuidslayout;
	}

}
