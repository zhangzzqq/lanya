package com.ds.bluetoothUtil;

import java.io.IOException;
import java.io.Serializable;
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

/**
 * ����ģ���������������Service
 * @author liujian
 *
 */
public class BluetoothServerService extends Service {

	//����������
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();	
	//����������߳�
	private  BluetoothServerConnThread connThread;
	
	//����ͨѶ
	private BluetoothCommunSocket communSocket;
	
	public BluetoothSocket socket;		//ͨ��Socket
	//������Ϣ�㲥������
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		private BluetoothDevice device;
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();		
			if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {				
				//�ر������߳�
				if (connThread != null) {
					connThread.close();
					connThread=null;
				}
				//�ر�
				if (communSocket != null) {
					communSocket.close();
				}
				//ֹͣ��̨����
				stopSelf();				
			}else if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
				//ѡ�������ӵķ������豸
				device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
		
				//�����ͻ��������߳�
			//	new BluetoothClientConnThread(serviceHandler, device).start();			
			} else if (BluetoothTools.ACTION_DATA_TO_SERVICE.equals(action)) {
				
				try {
					socket = device.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					socket.connect();
				} catch (IOException e) {
					e.printStackTrace();
				}			
				communSocket = new BluetoothCommunSocket(serviceHandler, socket);
				
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
	
	//���������߳���Ϣ��Handler
	private Handler serviceHandler = new Handler() {
		@Override
		public void handleMessage( Message msg) {			
			switch (msg.what) {
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS:		
//				//�������ӳɹ���Ϣ
//				Intent connSuccIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
//				sendBroadcast(connSuccIntent);
				break;	
			case BluetoothTools.MESSAGE_CONNECT_ERROR:
				//�������Ӵ���㲥
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);			
				break;			
			case BluetoothTools.MESSAGE_READ_OBJECT:
				//��ȡ������
				//�������ݹ㲥���������ݶ���
				TransmitBean transmit = (TransmitBean)msg.obj;	
				Intent dataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
				dataIntent.putExtra(BluetoothTools.DATA, transmit);
				sendBroadcast(dataIntent);
				break;
			case BluetoothTools.FILE_RECIVE_PERCENT://�ļ����հٷֱ�	
				//�����ļ�����ٷֱȹ㲥��ʵ�ֽ�������
				Intent flieIntent = new Intent(BluetoothTools.ACTION_FILE_RECIVE_PERCENT);
				flieIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
				sendBroadcast(flieIntent);			
				break;
			case BluetoothTools.FILE_SEND_PERCENT://�ļ����Ͱٷֱ�	
				//�����ļ�����ٷֱȹ㲥��ʵ�ֽ�������
				Intent flieIntent1 = new Intent(BluetoothTools.ACTION_FILE_SEND_PERCENT);
				flieIntent1.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
				sendBroadcast(flieIntent1);			
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	public void onCreate() {
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);	
		controlFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
		registerReceiver(controlReceiver, controlFilter);	
		
		bluetoothAdapter.enable();	//������
		//�����������ֹ��ܣ�300�룩
		Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//��ʾ��Ϣ
		startActivity(discoveryIntent);
		//���������߳�
		connThread=new BluetoothServerConnThread(serviceHandler);	
		connThread.start();
		super.onCreate();
	}

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
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
