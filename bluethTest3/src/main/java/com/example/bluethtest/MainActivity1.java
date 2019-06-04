package com.example.bluethtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity1 extends Activity implements
		AdapterView.OnItemClickListener {
	// 获取到蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter;
	// 用来保存搜索到的设备信息
	private List<String> bluetoothDevices = new ArrayList<String>();
	// ListView组件
	private ListView lvDevices;
	// ListView的字符串数组适配器
	private ArrayAdapter<String> arrayAdapter;
	// UUID，蓝牙建立链接需要的
	private final UUID MY_UUID = UUID
			.fromString("db764ac8-4b08-7f25-aafe-59d03c27bae3");
	// 为其链接创建一个名称
	private final String NAME = "Bluetooth_Socket";
	// 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
	private BluetoothDevice selectDevice;
	// 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
	private BluetoothSocket clientSocket;
	// 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
	private OutputStream os;
	// 服务端利用线程不断接受客户端信息
	private AcceptThread thread;
	private MyHandler handler;

	private boolean isPause = false;

	private static final int START_SEARCH = 1;
	private static final int SEND_MESSAGE = 2;
	private static final int SENDED_MESSAGE = 3;
	private static final int RECEIVE_MESSAGE = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		handler = new MyHandler();
		// 获取到蓝牙默认的适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// 开始搜索蓝牙设备
		handler.sendEmptyMessage(START_SEARCH);
		// 获取到ListView组件
		lvDevices = (ListView) findViewById(R.id.lvdevices);
		// 为listview设置字符换数组适配器
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				bluetoothDevices);
		// 为listView绑定适配器
		lvDevices.setAdapter(arrayAdapter);
		// 为listView设置item点击事件侦听
		lvDevices.setOnItemClickListener(this);

		// 用Set集合保持已绑定的设备
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		if (devices.size() > 0) {
			for (BluetoothDevice bluetoothDevice : devices) { // 保存到arrayList集合中
				bluetoothDevices.add(bluetoothDevice.getName() + ":"
						+ bluetoothDevice.getAddress() + "\n");
			}

		}
		// 因为蓝牙搜索到设备和完成搜索都是通过广播来告诉其他应用的
		// 这里注册找到设备和完成搜索广播
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
	
	}

	@Override
	protected void onResume() {
		isPause = false;
		super.onResume();
	}

	@Override
	protected void onPause() {
		isPause = false;
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
		}
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
		thread = null;
		handler = null;
	}

	public void onClick_Search(View view) {
		bluetoothDevices.clear();
		/*if (mBluetoothAdapter.isDiscovering()) {
			Toast.makeText(MainActivity1.this, "正在扫描...", Toast.LENGTH_SHORT)
					.show();
			return;
		}*/

		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		if (devices.size() > 0) {
			for (BluetoothDevice bluetoothDevice : devices) { // 保存到arrayList集合中
				bluetoothDevices.add(bluetoothDevice.getName() + ":"
						+ bluetoothDevice.getAddress() + "\n");
			}
		}

		// bluetoothDevices.clear();
		arrayAdapter.notifyDataSetChanged();
		handler.sendEmptyMessage(START_SEARCH);

	}

	// 注册广播接收者
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// 获取到广播的action
			String action = intent.getAction();
			// 判断广播是搜索到设备还是搜索完成
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				// 找到设备后获取其设备

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// 判断这个设备是否是之前已经绑定过了，如果是则不需要添加，在程序初始化的时候已经添加了
				// if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				// 设备没有绑定过，则将其保持到arrayList集合中
				if (!bluetoothDevices.contains(device.getName() + ":"
						+ device.getAddress() + "\n")) {

					bluetoothDevices.add(device.getName() + ":"
							+ device.getAddress() + "\n");

					// 更新字符串数组适配器，将内容显示在listView中
					arrayAdapter.notifyDataSetChanged();
				}
				// }
			} else if (action
					.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				setTitle("已搜索完成");
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						Integer.MIN_VALUE);
				if (state == BluetoothAdapter.STATE_ON) {
					mBluetoothAdapter.startDiscovery();
				}
			}
		}
	};

	// 点击listView中的设备，传送数据
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 获取到这个设备的信息
		String s = arrayAdapter.getItem(position);
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
		// 对其进行分割，获取到这个设备的地址
		String address = s.substring(s.indexOf(":") + 1).trim();
		// 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
		if (mBluetoothAdapter.isDiscovering()) {
			/*
			 * Toast.makeText(MainActivity1.this, "正在扫描...", Toast.LENGTH_SHORT)
			 * .show(); return;
			 */
			mBluetoothAdapter.cancelDiscovery();
		}
		// 如果选择设备为空则代表还没有选择设备
		if (selectDevice == null) {
			// 通过地址获取到该设备
			selectDevice = mBluetoothAdapter.getRemoteDevice(address);
			String string = bluetoothDevices.get(position);
			arrayAdapter.notifyDataSetChanged();
		}

		// 这里需要try catch一下，以防异常抛出
		Message message = new Message();
		message.what = SEND_MESSAGE;
		handler.sendMessage(message);
		// 实例接收客户端传过来的数据线程
		if (thread == null) {
			thread = new AcceptThread();
			// 线程开始
			thread.start();
		}
		

	}

	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			// 通过msg传递过来的信息，吐司一下收到的信息
			switch (msg.what) {
			case SEND_MESSAGE:
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							// 判断客户端接口是否为空
							if (clientSocket == null) {
								// 获取到客户端接口
								clientSocket = selectDevice
										.createRfcommSocketToServiceRecord(MY_UUID);
								// 向服务端发送连接
								clientSocket.connect();
								// 获取到输出流，向外写数据
								os = clientSocket.getOutputStream();

							}
							// 判断是否拿到输出流
							if (os != null) {
								// 需要发送的信息
								String text = "发送的数据";
								// 以utf-8的格式发送出去
								os.write(text.getBytes("UTF-8"));
								Message message = new Message();
								message.what = SENDED_MESSAGE;
								message.arg1 = 1;
								handler.sendMessage(message);
							}
							// 吐司一下，告诉用户发送成功
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// 如果发生异常则告诉用户发送失败
							Message message = new Message();
							message.what = SENDED_MESSAGE;
							message.arg1 = 0;
							handler.sendMessage(message);
						}
					}
				}).start();

				break;
			case RECEIVE_MESSAGE:
				Toast.makeText(MainActivity1.this, (String) msg.obj,
						Toast.LENGTH_SHORT).show();
				break;
			case SENDED_MESSAGE:
				if (msg.arg1 == 1) {
					Toast.makeText(MainActivity1.this, "发送信息成功，请查收",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MainActivity1.this, "发送信息失败",
							Toast.LENGTH_SHORT).show();
					if (clientSocket != null) {
						try {
							clientSocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						clientSocket = null;
					}
					
				}
				break;
			case START_SEARCH:
				setTitle("正在扫描...");
				if (mBluetoothAdapter.isEnabled()) {
					if (mBluetoothAdapter.isDiscovering()) {
						mBluetoothAdapter.cancelDiscovery();
					} else {
						mBluetoothAdapter.startDiscovery();
					}

				} else {
					mBluetoothAdapter.enable();
					// handler.sendEmptyMessageDelayed(START_SEARCH, 500);
				}
				break;
			default:
				break;

			}

		}
	}

	// 服务端接收信息线程
	private class AcceptThread extends Thread {
		private BluetoothServerSocket serverSocket;// 服务端接口
		private BluetoothSocket socket;// 获取到客户端的接口
		private InputStream is;// 获取到输入流
		private OutputStream os;// 获取到输出流

		public AcceptThread() {
			try {
				// 通过UUID监听请求，然后获取到对应的服务端接口
				serverSocket = mBluetoothAdapter
						.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		public void run() {
			try {
				// 接收其客户端的接口
				socket = serverSocket.accept();

				// 获取到输入流
				is = socket.getInputStream();
				// 获取到输出流
				os = socket.getOutputStream();
				// 无线循环来接收数据
				while (true) {
					// 创建一个128字节的缓冲
					byte[] buffer = new byte[128];
					// 每次读取128字节，并保存其读取的角标
					int count = is.read(buffer);
					// 创建Message类，向handler发送数据
					Message msg = new Message();
					msg.what = RECEIVE_MESSAGE;
					// 发送一个String的数据，让他向上转型为obj类型
					msg.obj = new String(buffer, 0, count, "utf-8");
					// 发送数据
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

		}

	}

}
