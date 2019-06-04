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
 * 服务器连接线程
 * @author liujian
 *
 */
public class wanquantestBluetoothServerConnThread {
	
	private Handler serviceHandler;		//用于同Service通信的Handler
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;		//用于通信的Socket
//	private BluetoothServerSocket serverSocket;
	private boolean isInterrupted=false;
	//蓝牙通讯线程
	private  BluetoothCommunThreads communThread;	
	
	
	private AcceptThread mAcceptThread;
	private ConnectedThread mConnectedThread;
	private int mState;
	// 当前连接状态的常量
		public static final int STATE_NONE = 0; // 默认的,什么都没做
		public static final int STATE_LISTEN = 1; // 进入连接的监听
		public static final int STATE_CONNECTING = 2;// 启动一个外向连接
		public static final int STATE_CONNECTED = 3; // 连接到一个远程设备
	
	/**
	 * 构造函数
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


		// 启动线程来监听BluetoothServerSocket
		if (mAcceptThread == null) {
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
		mState=STATE_LISTEN;
	}
	
	
	/**
	 * 这个线程运行监听传入的连接,它像一个服务器端客户机,它运行,直到一个连接被接受(或者,直到被取消)。
	 */
	private class AcceptThread extends Thread {
		// 本地服务器Socket
		private final BluetoothServerSocket serverSocket;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			// 创建一个新的监听服务器Socket
			try {
				tmp = adapter.listenUsingRfcommWithServiceRecord("Server", BluetoothTools.PRIVATE_UUID);
			} catch (IOException e) {
			}
			
			serverSocket=tmp;
		}

		public void run() {

			setName("AcceptThread");
			BluetoothSocket socket = null;

			// 如果是不连接的听服务器Socket

			while (mState != STATE_CONNECTED) {
				try {
					// 一个阻塞调用,只会返回成功连接或一个例外
					socket = serverSocket.accept();
				} catch (IOException e) {
					break;
				}

				// 如果连接被接受
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
		// 正在运行一个连接取消任何线程
				if (mConnectedThread != null) {
					mConnectedThread.cancel();
					mConnectedThread = null;
				}
				// 取消接受线程,因为只是想连接到一个设备
				if (mAcceptThread != null) {
					mAcceptThread.cancel();
					mAcceptThread = null;
				}
				// 启动线程来管理连接和执行传输
				mConnectedThread = new ConnectedThread(socket);
				mConnectedThread.start();
		mState=STATE_CONNECTED;
	}
	
	
	/**
	 * 这个线程运行在连接一个远程的设备上 它处理所有传入和传出的传输。
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// BluetoothSocket输入和输出流
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
					// 从InputStream中阅读  
					bytes = mmInStream.read(buffer);
					if (bytes > 0) {
			    		
					} else {

						if (mState != STATE_NONE) {
							// 启动服务到重启的听力模式
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
