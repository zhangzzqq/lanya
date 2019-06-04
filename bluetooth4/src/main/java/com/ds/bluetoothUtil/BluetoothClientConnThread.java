package com.ds.bluetoothUtil;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * �����ͻ��������߳�
 * @author liujian
 *
 */
public class BluetoothClientConnThread extends Thread{

	private Handler serviceHandler;		//������ͻ���Service�ش���Ϣ��handler
	private BluetoothDevice serverDevice;	//�������豸
	public BluetoothSocket socket;		//ͨ��Socket
	//����ͨѶ�߳�
	public BluetoothCommunSocket communSocket;
	/**
	 * ���캯��
	 * @param handler
	 * @param serverDevice
	 */
	public BluetoothClientConnThread(Handler handler, BluetoothDevice serverDevice) {
		this.serviceHandler = handler;
		this.serverDevice = serverDevice;
	}
	
	@Override
	public void run() {
		try {
			System.out.println("serverDevice.getName()"+serverDevice.getName()+serverDevice.getAddress());
			socket = serverDevice.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);			
			BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
			socket.connect();
		} catch (Exception ex) {
			try {
				ex.printStackTrace();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//��������ʧ����Ϣ
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			return;
		}
		Log.v("����" , "SOCKET�Ѵ���");
		//�������ӳɹ���Ϣ����Ϣ��obj����Ϊ���ӵ�socket
		Message msg = serviceHandler.obtainMessage();
		msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
		msg.obj = socket;
		msg.sendToTarget();
	}
}
