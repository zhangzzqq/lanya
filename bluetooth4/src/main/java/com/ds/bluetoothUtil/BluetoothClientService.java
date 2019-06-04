package com.ds.bluetoothUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * ����ģ��ͻ���������Service
 * @author liujian
 *
 */
public class BluetoothClientService extends Service {
	
	//��������Զ���豸����
	private List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();	
	//����������
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	//����ͨѶ
	private BluetoothCommunSocket communSocket;
	
	public BluetoothSocket socket;		//ͨ��Socket
	
	//����������߳�
	private  BluetoothServerConnThread connThread;
	//������Ϣ�㲥�Ľ�����
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		private BluetoothDevice device;
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();		
			if (BluetoothTools.ACTION_START_DISCOVERY.equals(action)) {
				//��ʼ����
				discoveredDevices.clear();	//��մ���豸�ļ���
				bluetoothAdapter.enable();	//������
				bluetoothAdapter.startDiscovery();	//��ʼ����		
			} else if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
				//ѡ�������ӵķ������豸
				device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);				
			} else if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
				//�ر������߳�
				if (connThread != null) {
					connThread.close();
				}
				//�ر�
				if (communSocket != null) {
					communSocket.close();
				}
				//ֹͣ��̨����
				stopSelf();					
			} else if (BluetoothTools.ACTION_DATA_TO_SERVICE.equals(action)) {
				//�����˽�������
				try {
					socket = device.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
			//		BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					socket.connect();
				} catch (IOException e) {
					Log.v("����", "���ӷ����ʧ��");
					e.printStackTrace();
				}			
				communSocket = new BluetoothCommunSocket(handler, socket);				
				final TransmitBean transmit = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				if (communSocket != null) {
				class MyRunnable implements Runnable{
					public void run(){
						communSocket.write(transmit);
					}
					}
				Thread t=new Thread(new MyRunnable());
				t.start();
				}
			}
		}
	};
	
	/*
	//���������㲥�Ľ�����
	private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {	
		@Override
		public void onReceive(Context context, Intent intent) {
			//��ȡ�㲥��Action
			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				System.out.print("//��ʼ����");
				//��ʼ����
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				System.out.print("//����Զ�������豸");
				//����Զ�������豸
				//��ȡ�豸
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				discoveredDevices.add(bluetoothDevice);
				//���ͷ����豸�㲥
				Intent deviceListIntent = new Intent(BluetoothTools.ACTION_FOUND_DEVICE);
				deviceListIntent.putExtra(BluetoothTools.DEVICE, bluetoothDevice);
				sendBroadcast(deviceListIntent);				
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				//��������
				System.out.print("//��������");
				Intent foundIntent = new Intent(BluetoothTools.ACTION_DISCOVERY_FINISHED);
				sendBroadcast(foundIntent);
			}
		}
	};
	*/
	
	//���������߳���Ϣ��Handler
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			//������Ϣ
			switch (msg.what) {
			case BluetoothTools.MESSAGE_CONNECT_ERROR://���Ӵ���
				//�������Ӵ���㲥
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);
				break;
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS://���ӳɹ�			
				//����ͨѶ�߳�
			//	communSocket = new BluetoothCommunSocket(handler, (BluetoothSocket)msg.obj);
			//	communSocket.start();				
				//�������ӳɹ��㲥
//				Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
//				sendBroadcast(succIntent);
				break;
			case BluetoothTools.MESSAGE_READ_OBJECT://��ȡ������
				//�������ݹ㲥���������ݶ���
				Intent dataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
				dataIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
				sendBroadcast(dataIntent);
				break;
			case BluetoothTools.FILE_SEND_PERCENT://�ļ����Ͱٷֱ�
				//�����ļ�����ٷֱȹ㲥��ʵ�ֽ�������
				Intent flieIntent = new Intent(BluetoothTools.ACTION_FILE_SEND_PERCENT);
				flieIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
				sendBroadcast(flieIntent);
				break;
			case BluetoothTools.FILE_RECIVE_PERCENT://�ļ����հٷֱ�	
				//�����ļ�����ٷֱȹ㲥��ʵ�ֽ�������
				Intent flieIntent1 = new Intent(BluetoothTools.ACTION_FILE_RECIVE_PERCENT);
				flieIntent1.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
				sendBroadcast(flieIntent1);			
				break;
			}
			super.handleMessage(msg);
		}
	};
	

	@Override
	public void onStart(Intent intent, int startId) {		
		super.onStart(intent, startId);
	}	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Service����ʱ�Ļص�����
	 */
	@Override
	public void onCreate() {
		//discoveryReceiver��IntentFilter
		IntentFilter discoveryFilter = new IntentFilter();
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);	
		//controlReceiver��IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_START_DISCOVERY);
		controlFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);	
		//ע��BroadcastReceiver
	//	registerReceiver(discoveryReceiver, discoveryFilter);
		registerReceiver(controlReceiver, controlFilter);
		//���������߳�
		connThread=new BluetoothServerConnThread(handler);	
		connThread.start();
		super.onCreate();
	}	

	
	/**
	 * Service����ʱ�Ļص�����
	 */
	@Override
	public void onDestroy() {
		//�ر������߳�
		if (connThread != null) {
			connThread.close();
		}
		//�ر�
		if (communSocket != null) {
			communSocket.close();
		}
		//�����
	//	unregisterReceiver(discoveryReceiver);
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}
}
