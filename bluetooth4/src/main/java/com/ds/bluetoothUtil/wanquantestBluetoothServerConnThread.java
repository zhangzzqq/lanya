package com.ds.bluetoothUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * �����������߳�
 * @author liujian
 *
 */
public class wanquantestBluetoothServerConnThread {
	
	private Handler serviceHandler;		//����ͬServiceͨ�ŵ�Handler
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;		//����ͨ�ŵ�Socket
//	private BluetoothServerSocket serverSocket;
	private boolean isInterrupted=false;
	//����ͨѶ�߳�
	private  BluetoothCommunThreads communThread;	
	
	
	private AcceptThread mAcceptThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	// ��ǰ����״̬�ĳ���
		public static final int STATE_NONE = 0; // Ĭ�ϵ�,ʲô��û��
		public static final int STATE_LISTEN = 1; // �������ӵļ���
		public static final int STATE_CONNECTING = 2;// ����һ����������
		public static final int STATE_CONNECTED = 3; // ���ӵ�һ��Զ���豸
	
	/**
	 * ���캯��
	 * @param handler
	 */
	public wanquantestBluetoothServerConnThread(Handler handler) {
		this.serviceHandler = handler;
		adapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public void close(){
		mState=STATE_NONE;

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;

		}
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
	}

	public synchronized void start() {


		// �����߳�������BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		mState=STATE_LISTEN;
	}
	
	
	/**
	 * ����߳����м������������,����һ���������˿ͻ���,������,ֱ��һ�����ӱ�����(����,ֱ����ȡ��)��
	 */
	private class AcceptThread extends Thread {
		// ���ط�����Socket
		private final BluetoothServerSocket serverSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			// ����һ���µļ���������Socket
			try {
				tmp = adapter.listenUsingRfcommWithServiceRecord("Server", BluetoothTools.PRIVATE_UUID);
			} catch (IOException e) {
			}
			
			serverSocket=tmp;
		}

		public void run() {

			setName("AcceptThread");
			BluetoothSocket socket = null;

			// ����ǲ����ӵ���������Socket

			while (mState != STATE_CONNECTED) {
				try {
					// һ����������,ֻ�᷵�سɹ����ӻ�һ������
					socket = serverSocket.accept();
				} catch (IOException e) {
					break;
				}

				// ������ӱ�����
				if (socket != null) {

					connected(socket);
				}
			}
		}
		public void cancel() {
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}

	}
		
	
	public synchronized void connected(BluetoothSocket socket) {
		// ��������һ������ȡ���κ��߳�
				if (mConnectedThread != null) {
					mConnectedThread.cancel();
					mConnectedThread = null;
				}
				// ȡ�������߳�,��Ϊֻ�������ӵ�һ���豸
				if (mAcceptThread != null) {
					mAcceptThread.cancel();
					mAcceptThread = null;
				}
				// �����߳����������Ӻ�ִ�д���
				mConnectedThread = new ConnectedThread(socket);
				mConnectedThread.start();
		mState=STATE_CONNECTED;
	}
	
	
	/**
	 * ����߳�����������һ��Զ�̵��豸�� ���������д���ʹ����Ĵ��䡣
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// BluetoothSocket����������
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			int bytes, i;
			while (true) {
				try {
					byte[] buffer = new byte[256];
					// ��InputStream���Ķ�  
					bytes = mmInStream.read(buffer);
					if (bytes > 0) {
			    		
					} else {

						if (mState != STATE_NONE) {
							// ������������������ģʽ
							wanquantestBluetoothServerConnThread.this.start();
						}
						break;
					}
				} catch (IOException e) {
					if (mState != STATE_NONE) {

						wanquantestBluetoothServerConnThread.this.start();
					}
					break;
				}
			}
		}

  
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
		
	}
	
}
