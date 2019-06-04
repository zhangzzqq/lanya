package com.ds.bluetoothUtil;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * �����������߳�
 * @author liujian
 *
 */
public class BluetoothServerConnThread extends Thread {
	
	private Handler serviceHandler;		//����ͬServiceͨ�ŵ�Handler
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;		//����ͨ�ŵ�Socket
	private BluetoothServerSocket serverSocket;
	private boolean isInterrupted=false;
	//����ͨѶ�߳�
	private  BluetoothCommunThreads communThread;	
	
	/**
	 * ���캯��
	 * @param handler
	 */
	public BluetoothServerConnThread(Handler handler) {
		this.serviceHandler = handler;
		adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public void close(){
        isInterrupted = true;
     		if (communThread != null) {
     			communThread.close();
     			communThread = null;
     		}
        if (serverSocket != null) {
			try {
				serverSocket.close();
				Log.v("����" , "serverSocket�ѹر�");
			} catch (IOException e) {
				Log.e("����", "serverSocket�ر� failed", e);
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
				Log.v("����" , "socket�ѹر�");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        super.interrupt();
	}
	
	@Override
	public void run() {		
		try {
			serverSocket = adapter.listenUsingRfcommWithServiceRecord("Server", BluetoothTools.PRIVATE_UUID);
			while(!isInterrupted){				
				socket = serverSocket.accept();	
				if (socket != null) {				
					//����ͨѶ�߳�
					communThread = new BluetoothCommunThreads(serviceHandler, socket);			
					communThread.start();				
//					//�������ӳɹ���Ϣ����Ϣ��obj�ֶ�Ϊ���ӵ�socket
//					Message msg = serviceHandler.obtainMessage();
//					msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
//					msg.obj = socket;
//					msg.sendToTarget();
				} else {
					//��������ʧ����Ϣ
				//	serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
					break;
				}
			}
		} catch (Exception e) {
			//��������ʧ����Ϣ
		//	serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			e.printStackTrace();
			return;
		} 
		finally {
			Log.v("����" , "BluetoothServerConnThread�˳�");
			try {
				serverSocket.close();
			} catch (Exception e) {
				Log.v("����" , "serverSocket.close()  failed");
				e.printStackTrace();
			}
		}
	}
	
}
