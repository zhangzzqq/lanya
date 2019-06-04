package com.bluetooth.activity;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Rex on 2015/5/27.
 */
public class DeviceAdapter extends BaseAdapter {

	private List<BluetoothDevice> data;
	private LayoutInflater inflater;

	public DeviceAdapter(Context context, List<BluetoothDevice> data) {
		this.data = data;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int i) {
		return data.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View itemView, ViewGroup viewGroup) {
		// 复用View，优化性能
		if (itemView == null) {
			itemView = inflater.inflate(android.R.layout.simple_list_item_2, viewGroup, false);
		}
		TextView name = (TextView) itemView.findViewById(android.R.id.text1);
		TextView address = (TextView) itemView.findViewById(android.R.id.text2);
		// 获取蓝牙设备
		BluetoothDevice device = (BluetoothDevice) getItem(i);
		name.setTextColor(Color.parseColor("#000000"));
		name.setText(device.getName());
		address.setTextColor(Color.parseColor("#000000"));
		address.setText(device.getAddress());
		return itemView;
	}

	public void refresh(List<BluetoothDevice> data) {
		this.data = data;
		notifyDataSetChanged();
	}

}
