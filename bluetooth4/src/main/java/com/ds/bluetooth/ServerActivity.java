package com.ds.bluetooth;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ds.bluetoothUtil.BluetoothCommunSocket;
import com.ds.bluetoothUtil.BluetoothServerConnThread;
import com.ds.bluetoothUtil.BluetoothServerService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;

public class ServerActivity extends Activity {

	private TextView serverStateTextView;
	private EditText msgEditText;
	private EditText sendMsgEditText;
	private Button sendBtn;
	private ProgressDialog mpDialog; 
	private ListView bondDevicesListView;
	private ArrayList<BluetoothDevice> bondDevices=new ArrayList<BluetoothDevice>();  // 用于存放已配对蓝牙设备
	private BluetoothAdapter bluetooth=BluetoothAdapter.getDefaultAdapter();
	

		//服务端连接线程
		private  BluetoothServerConnThread connThread;
		
		//蓝牙通讯
		private BluetoothCommunSocket communSocket;
		
		public BluetoothSocket socket;		//通信Socket
	
	@Override
	protected void onStart() {
		//开启后台service
	//	Intent startService = new Intent(ServerActivity.this, BluetoothServerService.class);
	//	startService(startService);
		
		//注册BoradcasrReceiver
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
//		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
//		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_ERROR);
//		intentFilter.addAction(BluetoothTools.ACTION_FILE_SEND_PERCENT);
//		intentFilter.addAction(BluetoothTools.ACTION_FILE_RECIVE_PERCENT);
//		registerReceiver(broadcastReceiver, intentFilter);
		super.onStart();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
			
		bondDevicesListView = (ListView) this.findViewById(R.id.bondDevices);
		serverStateTextView=(TextView) this.findViewById(R.id.serverStateText);
		msgEditText = (EditText)findViewById(R.id.serverEditText);
		addBondDevicesToListView();
		
		mpDialog=new ProgressDialog(ServerActivity.this);	
		mpDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
		mpDialog.setTitle("提示");  
		mpDialog.setIcon(R.drawable.icon); 
		mpDialog.setIndeterminate(false);  
		mpDialog.setCancelable(true); // 设置是否可以通过点击Back键取消
		mpDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条  
		mpDialog.setMax(100);	
//		mpDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定",  
//		           new DialogInterface.OnClickListener() {  
//		 
//		               @Override  
//		               public void onClick(DialogInterface dialog, int which) {  
//		                   // TODO Auto-generated method stub  
//		 
//		               }  
//		           });  
//		mpDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",  
//		           new DialogInterface.OnClickListener() {  
//		 
//		               @Override  
//		               public void onClick(DialogInterface dialog, int which) {  
//		                   // TODO Auto-generated method stub  
//		 
//		               }  
//		           }); 
		
//		IntentFilter controlFilter = new IntentFilter();
//		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
//		controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);	
//		controlFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
//		registerReceiver(controlReceiver, controlFilter);
		
		bluetooth.enable();	//打开蓝牙
		//开启蓝牙发现功能（300秒）
		Intent discoveryIntent = new Intent(bluetooth.ACTION_REQUEST_DISCOVERABLE);
		discoveryIntent.putExtra(bluetooth.EXTRA_DISCOVERABLE_DURATION, 300);
		discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//提示信息
		startActivity(discoveryIntent);
		//开启连接线程
		connThread=new BluetoothServerConnThread(serviceHandler);	
		connThread.start();
	}
	
/**  
    * 添加已绑定蓝牙设备到ListView  
    */    
	public void addBondDevicesToListView() {  
	   	ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();    
	   	int count = bondDevices.size();    
	   	
	   	if(count==0){//刚打开ACTIVITY 尚未开始搜索
	    	//获得已配对的远程蓝牙设备的集合  
	        Set<BluetoothDevice> devices = bluetooth.getBondedDevices(); 
	        if(devices.size()>0){ 
	            for(Iterator<BluetoothDevice> it = devices.iterator();it.hasNext();){ 
	                BluetoothDevice device = (BluetoothDevice)it.next();
	                HashMap<String, Object> map = new HashMap<String, Object>();
	                map.put("deviceName", device.getName()+ "|" +device.getAddress());    
	                bondDevices.add(device);
	                data.add(map);// 把item项的数据加到data中  
	            } 
	        }
	   	}           
	       String[] from = { "deviceName" };    
	       int[] to = { R.id.device_name };    
	       SimpleAdapter simpleAdapter = new SimpleAdapter(ServerActivity.this, data,R.layout.bonddevice_item, from, to);    
	       // 把适配器装载到listView中    
	       this.bondDevicesListView.setAdapter(simpleAdapter);     
	       this.bondDevicesListView.setOnItemClickListener(new OnItemClickListener() { 
	           @Override    
	           public void onItemClick(AdapterView<?> arg0, View arg1,    
	                   int arg2, long arg3) {    
	               BluetoothDevice device = bondDevices.get(arg2);    
	               Intent intent = new Intent();                   
	               Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
	               selectDeviceIntent.putExtra(BluetoothTools.DEVICE, device);
	               ServerActivity.this.sendBroadcast(selectDeviceIntent);	
	               intent.setClassName(ServerActivity.this,"com.ds.bluetooth.ServerActivity1");    
	               intent.putExtra("deviceAddress", device.getAddress()); 
	         //      unregisterReceiver(broadcastReceiver);	
	               ServerActivity.this.startActivity(intent);    
	           }    
	       });  
	} 

		//接收其他线程消息的Handler
		private Handler serviceHandler = new Handler() {
			@Override
			public void handleMessage( Message msg) {			
				switch (msg.what) {
				case BluetoothTools.MESSAGE_CONNECT_SUCCESS:		
//					//发送连接成功消息
//					Intent connSuccIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
//					sendBroadcast(connSuccIntent);
					break;	
				case BluetoothTools.MESSAGE_CONNECT_ERROR:
					//发送连接错误广播
					mpDialog.dismiss();
					Toast.makeText(ServerActivity.this, "通讯失败", 2000).show(); 	
					break;			
				case BluetoothTools.MESSAGE_READ_OBJECT:
					//读取到数据
					TransmitBean transmit = (TransmitBean)msg.obj;
					String text="";
					if(transmit.getFilename()!=null&&!"".equals(transmit.getFilename())){
						text = "receive file from remote " + new Date().toLocaleString() + " :\r\n" + transmit.getFilename() + "\r\n";
					}else{
						text = "receive message from remote " + new Date().toLocaleString() + " :\r\n" + transmit.getMsg() + "\r\n";
					}
					msgEditText.append(text);
					
					break;
				case BluetoothTools.FILE_RECIVE_PERCENT://文件接收百分比	
					//接收文件传输百分比广播，实现进度条用
					TransmitBean data = (TransmitBean)msg.obj;				  					  
					if(!"0".equals(data.getTspeed())){
						mpDialog.setMessage("文件接收速度:"+data.getTspeed()+"k/s");  
					}			  
					mpDialog.setProgress(Integer.valueOf(data.getUppercent())); 				
					if(data.isShowflag()){
					mpDialog.show();
					}
					if(Integer.valueOf(data.getUppercent())==100){				
						mpDialog.dismiss();
						mpDialog.setProgress(0);  
					}				
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		@Override
		public void onBackPressed() {
		// 这里处理逻辑代码，注意：该方法仅适用于2.0或更新版的sdk
			mpDialog.dismiss();
			mpDialog.setProgress(0);
			Intent intent = new Intent();
			intent.setClassName(ServerActivity.this,"com.ds.bluetooth.MainActivity");    	
            ServerActivity.this.startActivity(intent);   		
			super.onBackPressed();
		}
	
	@Override
	public void onDestroy() {		
		//关闭连接线程
		if (connThread != null) {
			connThread.close();
		}
		//关闭
		if (communSocket != null) {
			communSocket.close();
		}
	//	unregisterReceiver(controlReceiver);
		super.onDestroy();
	}

	
	@Override
	protected void onStop() {	
		super.onStop(); 
	}

}
