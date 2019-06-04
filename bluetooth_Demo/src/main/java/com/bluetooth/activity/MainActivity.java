package com.bluetooth.activity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {

	private BlueToothController controller = new BlueToothController();
	private List<BluetoothDevice> dList = new ArrayList<>();

	private DeviceAdapter adapter = null;
	private ListView lv = null;
	private ProgressBar pb;
	private Button btn_OFF_ON;
	private Button btnFind;
	private Button btnBinding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		boolean isBool = controller.isSupportBlueTooth();
		if (!isBool)
			finish();
		btn_OFF_ON = (Button) findViewById(R.id.btn_on_off);
		btn_OFF_ON.setOnClickListener(this);
		findViewById(R.id.btn_visible).setOnClickListener(this);
		btnFind = (Button) findViewById(R.id.btn_find);
		btnFind.setOnClickListener(this);
		btnBinding = (Button) findViewById(R.id.btn_binding);
		btnBinding.setOnClickListener(this);
		pb = (ProgressBar) findViewById(R.id.progressBar1);

		lv = (ListView) findViewById(R.id.lv_list);
		adapter = new DeviceAdapter(this, dList);
		lv.setAdapter(adapter);

		receiver();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		boolean isSwitch = controller.getSwitchBlueTooth();
		setSwitchBlueTooth(isSwitch);
	}

	private void setSwitchBlueTooth(boolean isSwitch) {
		// TODO Auto-generated method stub
		if (isSwitch) {
			btn_OFF_ON.setBackgroundResource(R.drawable.bg_settings_drag_on);
		} else {
			btn_OFF_ON.setBackgroundResource(R.drawable.bg_settings_drag_off_new);
		}
	}

	private void receiver() {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		// 开始查找
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		// 结束查找
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		// 查找设备
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		// 设备扫描模式改变
		filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		// 绑定状态
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(receiver, filter);
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			if (state == BluetoothAdapter.STATE_OFF) {
				setSwitchBlueTooth(false);
			} else if (state == BluetoothAdapter.STATE_ON) {
				setSwitchBlueTooth(true);
			} else {
				String action = intent.getAction();
				if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {// 本地蓝牙适配器已经开始查找远程设备。
					showProgressBar(true);
					// 初始化数据列表
					dList.clear();
					adapter.refresh(dList);
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {// 蓝牙适配器查找远程设备已经完成。
					showProgressBar(false);
				} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {// 发现远程设备。
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//
					// 找到一个，添加一个
					dList.add(device);
					adapter.refresh(dList);
				} else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {// 蓝牙扫描状态。
					int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
					if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
						showProgressBar(true);
					} else {
						showProgressBar(false);
					}
				} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {// 一个远程设备状态的变化。
					BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					if (remoteDevice == null) {
						return;
					}
					int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
					if (status == BluetoothDevice.BOND_BONDED) {
						Toast.makeText(MainActivity.this,"完成配对:"+ remoteDevice.getName(),Toast.LENGTH_LONG).show();
//						toast("完成配对:" + remoteDevice.getName());
					} else if (status == BluetoothDevice.BOND_BONDING) {
//						toast("正在配对..." + remoteDevice.getName());
						Toast.makeText(MainActivity.this,"正在配对..."+ remoteDevice.getName(),Toast.LENGTH_LONG).show();
					} else if (status == BluetoothDevice.BOND_NONE) {
//						toast("取消配对:" + remoteDevice.getName());
						Toast.makeText(MainActivity.this,"取消配对:"+ remoteDevice.getName(),Toast.LENGTH_LONG).show();
						dList.clear();
						dList = controller.getBlueToothDeviceList();
						adapter.refresh(dList);
					}
				}
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_on_off:
			boolean isSwitch = controller.getSwitchBlueTooth();
			if (!isSwitch) {
				controller.trueOnBlueTooth(this);
			} else {
				controller.trueOffBlueTooth();
				setSwitchBlueTooth(false);
				dList.clear();
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.btn_visible:// 蓝牙可见
			controller.isVisibility(this);
			break;
		case R.id.btn_find:// 查找设备
			adapter.refresh(dList);
			controller.findDevice();
			itemListener(true);
			break;
		case R.id.btn_binding:// 获取绑定设备
			dList.clear();
			dList = controller.getBlueToothDeviceList();
			adapter.refresh(dList);
			itemListener(false);
			break;
		default:
			break;
		}
	}

	private void itemListener(final boolean bool) {
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					BluetoothDevice btDevice = dList.get(position);
					if (bool) {
						createBond(btDevice.getClass(), btDevice);
					} else {
						removeBond(btDevice.getClass(), btDevice);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// 与设备解除配对
	public boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	// 与设备配对
	public boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	private void showProgressBar(boolean bool) {
		if (bool) {
			pb.setVisibility(View.VISIBLE);
		} else {
			pb.setVisibility(View.GONE);
		}
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
	}

}
