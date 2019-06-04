package com.bluetooth.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

public class BlueToothController {

	private BluetoothAdapter bluetoothAdapter = null;

	public BlueToothController() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public BluetoothAdapter getAdapter() {
		return bluetoothAdapter;
	}

	// 是否支持蓝牙
	public boolean isSupportBlueTooth() {
		if (bluetoothAdapter != null) {
			return true;
		} else {
			return false;
		}
	}

	// 蓝牙是否打开
	public boolean getSwitchBlueTooth() {
		assert(bluetoothAdapter != null);
		return bluetoothAdapter.isEnabled();
	}

	// 打开蓝牙
	public void trueOnBlueTooth(Activity activity) {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivity(intent);
	}

	// 关闭蓝牙
	public void trueOffBlueTooth() {
		bluetoothAdapter.disable();
	}

	// 蓝牙可见
	public void isVisibility(Context context) {
		Intent dIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		dIntent.putExtra(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE, 300);
		context.startActivity(dIntent);
	}

	// 查找设备
	public void findDevice() {
		assert(bluetoothAdapter != null);
		bluetoothAdapter.startDiscovery();
	}

	// 获取绑定设备
	public List<BluetoothDevice> getBlueToothDeviceList() {
		return new ArrayList<>(bluetoothAdapter.getBondedDevices());
	}

}
