package com.ds.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ds.bluetoothUtil.BluetoothClientService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.ClientListListener;


public class ClientActivity1 extends Activity {
	private Button SearchBtn;

	private static final String tag = "����";
	private static final int  REQUEST_DISCOVERABLE_BLUETOOTH = 3;
	private BluetoothAdapter bluetooth=BluetoothAdapter.getDefaultAdapter();
	private ArrayList<BluetoothDevice> unbondDevices=new ArrayList<BluetoothDevice>(); // ���ڴ��δ��������豸    
    private ArrayList<BluetoothDevice> bondDevices=new ArrayList<BluetoothDevice>();  // ���ڴ������������豸       
    private ListView unbondDevicesListView ;    
    private ListView bondDevicesListView; 
	ProgressDialog progressDialog = null;  
	private ClientListListener clientListListener; 

	
	@Override
	protected void onStart() {	
		//������̨service
	   Log.v("����" , "������̨Servic");
	   Intent startService = new Intent(ClientActivity1.this, BluetoothClientService.class);
	   startService(startService);
	   
	// ע��Receiver����ȡ�����豸��صĽ�� ��actionָ��Ϊ��ACTION_FOUND
       IntentFilter intent = new IntentFilter();   
       intent.addAction(BluetoothDevice.ACTION_FOUND);// ��BroadcastReceiver��ȡ���������
       intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
       intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);	           
       intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);    
       intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); 
       intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
       //ע��㲥������
       registerReceiver(searchDevices, intent);		
	   super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		unbondDevicesListView = (ListView) this.findViewById(R.id.unbondDevices);    
	    bondDevicesListView = (ListView) this.findViewById(R.id.bondDevices);
		SearchBtn = (Button)findViewById(R.id.searchDevices);
		SearchBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//��ʼ����
				unbondDevices.clear();
				bondDevices.clear();			
	           setTitle("����������ַ��" + bluetooth.getAddress());
	           //ɨ�������豸 ����Ҫ12�룬����Ҳ�ǳ��󣨵�صȣ� ���첽ɨ����˼����һ���þͻ�ɨ��
	           bluetooth.startDiscovery();
			}
		});
		operbluetooth();
	//	clientListListener=new ClientListListener (this, bluetooth, unbondDevices, bondDevices, unbondDevicesListView, bondDevicesListView);
	//	clientListListener.addBondDevicesToListView();
		
		//�������Ե�Զ�������豸�ļ���  
        Set<BluetoothDevice> devices = bluetooth.getBondedDevices(); 
        if(devices.size()>0){ 
            for(Iterator<BluetoothDevice> it = devices.iterator();it.hasNext();){ 
               BluetoothDevice device = (BluetoothDevice)it.next();
               Intent intent = new Intent();                 
               Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
               selectDeviceIntent.putExtra(BluetoothTools.DEVICE, device);
			   this.sendBroadcast(selectDeviceIntent);	
               intent.setClassName(this,"com.ds.bluetooth.ClientActivity2");    
               intent.putExtra("deviceAddress", device.getAddress());    
               this.startActivity(intent);
               break;
            } 
        }else{
        	Toast.makeText(this, "����ʧ��,û������Ե�Զ���豸", Toast.LENGTH_SHORT).show();  
        }
	}
	
	//������
    public void operbluetooth() {
           //�ж��Ƿ���Bluetooth�豸
           if (bluetooth == null) {
              Toast.makeText(this, "û�м�⵽�����豸", Toast.LENGTH_LONG).show();
              finish();
              return;
           }
           Log.v(tag , "��⵽�����豸!");
           //�жϵ�ǰ�豸�е������豸�Ƿ��Ѿ��򿪣�����isEnabled()����ѯ��ǰ�����豸��״̬���������Ϊfalse�����ʾ�����豸û�п�����
           boolean originalBluetooth = (bluetooth != null && bluetooth.isEnabled());
           if(originalBluetooth){
              Log.v(tag , "�����豸�Ѿ�����!");
              setTitle("�����豸�Ѿ�����!");
              return;
           }else if (originalBluetooth == false) {
              //��Bluetooth�豸 �������ʾЧ��
              //bluetooth.enable();
              //Ҳ��������,�������ʾЧ��
              Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
              startActivity(intent);
           }      
           /*ȷ������������*/
           Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
           //���ÿɼ�״̬�ĳ���ʱ��Ϊ500�룬���������300��
           discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 500);
           startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BLUETOOTH);       
    }
	
	//�㲥������
	private BroadcastReceiver searchDevices = new BroadcastReceiver() {		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();		
			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				 progressDialog = ProgressDialog.show(context, "���Ե�...",    
	    	              "���������豸��...", true);  
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				System.out.println("�豸�������");    
	    	    progressDialog.dismiss(); 

	    	    clientListListener.addUnbondDevicesToListView();    
	    	    clientListListener.addBondDevicesToListView(); 			
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				//��ȡ���豸����
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);              
				String str= device.getName() + "|" + device.getAddress();
				System.out.println(str);   
				
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
            	Log.v("����" , "rssi:"+rssi);
				
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {              	
                	//�ź�ǿ�ȡ�
                	
                	
            	  if (!bondDevices.contains(device)) {    
            		  bondDevices.add(device);    
                  }    
            	} else {    
            		if (!unbondDevices.contains(device)) {    
            			unbondDevices.add(device);    
                    }   
            	}				
			}
		}
	};

	@Override
	protected void onDestroy() {
		unregisterReceiver(searchDevices);
		super.onStop();
	}
}
