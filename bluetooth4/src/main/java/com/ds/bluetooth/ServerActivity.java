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
	private ArrayList<BluetoothDevice> bondDevices=new ArrayList<BluetoothDevice>();  // ���ڴ������������豸
	private BluetoothAdapter bluetooth=BluetoothAdapter.getDefaultAdapter();
	

		//����������߳�
		private  BluetoothServerConnThread connThread;
		
		//����ͨѶ
		private BluetoothCommunSocket communSocket;
		
		public BluetoothSocket socket;		//ͨ��Socket
	
	@Override
	protected void onStart() {
		//������̨service
	//	Intent startService = new Intent(ServerActivity.this, BluetoothServerService.class);
	//	startService(startService);
		
		//ע��BoradcasrReceiver
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
		mpDialog.setTitle("��ʾ");  
		mpDialog.setIcon(R.drawable.icon); 
		mpDialog.setIndeterminate(false);  
		mpDialog.setCancelable(true); // �����Ƿ����ͨ�����Back��ȡ��
		mpDialog.setCanceledOnTouchOutside(false);// �����ڵ��Dialog���Ƿ�ȡ��Dialog������  
		mpDialog.setMax(100);	
//		mpDialog.setButton(DialogInterface.BUTTON_POSITIVE, "ȷ��",  
//		           new DialogInterface.OnClickListener() {  
//		 
//		               @Override  
//		               public void onClick(DialogInterface dialog, int which) {  
//		                   // TODO Auto-generated method stub  
//		 
//		               }  
//		           });  
//		mpDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "ȡ��",  
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
		
		bluetooth.enable();	//������
		//�����������ֹ��ܣ�300�룩
		Intent discoveryIntent = new Intent(bluetooth.ACTION_REQUEST_DISCOVERABLE);
		discoveryIntent.putExtra(bluetooth.EXTRA_DISCOVERABLE_DURATION, 300);
		discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//��ʾ��Ϣ
		startActivity(discoveryIntent);
		//���������߳�
		connThread=new BluetoothServerConnThread(serviceHandler);	
		connThread.start();
	}
	
/**  
    * ����Ѱ������豸��ListView  
    */    
	public void addBondDevicesToListView() {  
	   	ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();    
	   	int count = bondDevices.size();    
	   	
	   	if(count==0){//�մ�ACTIVITY ��δ��ʼ����
	    	//�������Ե�Զ�������豸�ļ���  
	        Set<BluetoothDevice> devices = bluetooth.getBondedDevices(); 
	        if(devices.size()>0){ 
	            for(Iterator<BluetoothDevice> it = devices.iterator();it.hasNext();){ 
	                BluetoothDevice device = (BluetoothDevice)it.next();
	                HashMap<String, Object> map = new HashMap<String, Object>();
	                map.put("deviceName", device.getName()+ "|" +device.getAddress());    
	                bondDevices.add(device);
	                data.add(map);// ��item������ݼӵ�data��  
	            } 
	        }
	   	}           
	       String[] from = { "deviceName" };    
	       int[] to = { R.id.device_name };    
	       SimpleAdapter simpleAdapter = new SimpleAdapter(ServerActivity.this, data,R.layout.bonddevice_item, from, to);    
	       // ��������װ�ص�listView��    
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

		//���������߳���Ϣ��Handler
		private Handler serviceHandler = new Handler() {
			@Override
			public void handleMessage( Message msg) {			
				switch (msg.what) {
				case BluetoothTools.MESSAGE_CONNECT_SUCCESS:		
//					//�������ӳɹ���Ϣ
//					Intent connSuccIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
//					sendBroadcast(connSuccIntent);
					break;	
				case BluetoothTools.MESSAGE_CONNECT_ERROR:
					//�������Ӵ���㲥
					mpDialog.dismiss();
					Toast.makeText(ServerActivity.this, "ͨѶʧ��", 2000).show(); 	
					break;			
				case BluetoothTools.MESSAGE_READ_OBJECT:
					//��ȡ������
					TransmitBean transmit = (TransmitBean)msg.obj;
					String text="";
					if(transmit.getFilename()!=null&&!"".equals(transmit.getFilename())){
						text = "receive file from remote " + new Date().toLocaleString() + " :\r\n" + transmit.getFilename() + "\r\n";
					}else{
						text = "receive message from remote " + new Date().toLocaleString() + " :\r\n" + transmit.getMsg() + "\r\n";
					}
					msgEditText.append(text);
					
					break;
				case BluetoothTools.FILE_RECIVE_PERCENT://�ļ����հٷֱ�	
					//�����ļ�����ٷֱȹ㲥��ʵ�ֽ�������
					TransmitBean data = (TransmitBean)msg.obj;				  					  
					if(!"0".equals(data.getTspeed())){
						mpDialog.setMessage("�ļ������ٶ�:"+data.getTspeed()+"k/s");  
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
		// ���ﴦ���߼����룬ע�⣺�÷�����������2.0����°��sdk
			mpDialog.dismiss();
			mpDialog.setProgress(0);
			Intent intent = new Intent();
			intent.setClassName(ServerActivity.this,"com.ds.bluetooth.MainActivity");    	
            ServerActivity.this.startActivity(intent);   		
			super.onBackPressed();
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
	//	unregisterReceiver(controlReceiver);
		super.onDestroy();
	}

	
	@Override
	protected void onStop() {	
		super.onStop(); 
	}

}
