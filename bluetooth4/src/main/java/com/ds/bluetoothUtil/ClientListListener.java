package com.ds.bluetoothUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.ds.bluetooth.R;

/**
 * ListViewԪ���¼�������
 * @author 
 *
 */
public class ClientListListener {
	public BluetoothAdapter bluetooth;
	public ArrayList<BluetoothDevice> unbondDevices; // ���ڴ��δ��������豸    
	public ArrayList<BluetoothDevice> bondDevices;  // ���ڴ������������豸       
    public ListView unbondDevicesListView ;    
    public ListView bondDevicesListView; 
    public Activity ClientActivity;
	
	public ClientListListener(Activity ClientActivity,BluetoothAdapter bluetooth,ArrayList<BluetoothDevice> unbondDevices,
			ArrayList<BluetoothDevice> bondDevices,ListView unbondDevicesListView,ListView bondDevicesListView){
		this.ClientActivity = ClientActivity;
		this.bluetooth = bluetooth;
		this.unbondDevices = unbondDevices;
		this.bondDevices = bondDevices;
		this.unbondDevicesListView = unbondDevicesListView;
		this.bondDevicesListView = bondDevicesListView;
	}
	
    /**  
    * ���δ�������豸��ListView  
    */    
	public void addUnbondDevicesToListView() {    
       ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();    
       int count = unbondDevices.size();    
       System.out.println("δ���豸������" + count);    
       for (int i = 0; i < count; i++) {    
           HashMap<String, Object> map = new HashMap<String, Object>();    
           map.put("deviceName", this.unbondDevices.get(i).getName());    
           data.add(map);// ��item������ݼӵ�data��    
       }    
       String[] from = { "deviceName" };    
       int[] to = { R.id.device_name };    
       SimpleAdapter simpleAdapter = new SimpleAdapter(ClientActivity, data,R.layout.unbonddevice_item, from, to);    
       // ��������װ�ص�listView��    
       this.unbondDevicesListView.setAdapter(simpleAdapter);    
       // Ϊÿ��item�󶨼����������豸������    
       this.unbondDevicesListView.setOnItemClickListener(new OnItemClickListener() {    
           @Override    
           public void onItemClick(AdapterView<?> arg0, View arg1,    
                   int arg2, long arg3) {    
               try {
            	   bluetooth.cancelDiscovery();
                   Method createBondMethod = BluetoothDevice.class.getMethod("createBond");    
                   createBondMethod.invoke(unbondDevices.get(arg2));    
                   // ���󶨺õ��豸��ӵ��Ѱ�list����    
                   bondDevices.add(unbondDevices.get(arg2));    
                   // ���󶨺õ��豸��δ��list�������Ƴ�    
                   unbondDevices.remove(arg2);
                   addBondDevicesToListView();    
                   addUnbondDevicesToListView();    
               } catch (Exception e) {    
                   Toast.makeText(ClientActivity, "���ʧ�ܣ�", Toast.LENGTH_SHORT).show();    
               }    
           }    
       });    
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
	   	}else{
	   		for (int i = 0; i < count; i++) {    
	               HashMap<String, Object> map = new HashMap<String, Object>();    
	               map.put("deviceName", this.bondDevices.get(i).getName()+ "|" +this.bondDevices.get(i).getAddress());    
	               data.add(map);// ��item������ݼӵ�data��    
	           }
	   	}           
	       String[] from = { "deviceName" };    
	       int[] to = { R.id.device_name };    
	       SimpleAdapter simpleAdapter = new SimpleAdapter(ClientActivity, data,R.layout.bonddevice_item, from, to);    
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
				   ClientActivity.sendBroadcast(selectDeviceIntent);	
	               intent.setClassName(ClientActivity,"com.ds.bluetooth.ClientActivity2");    
	               intent.putExtra("deviceAddress", device.getAddress());    
	               ClientActivity.startActivity(intent);    
	           }    
	       }); 
	       this.bondDevicesListView.setOnItemLongClickListener(new OnItemLongClickListener() { 
	           @Override    
	           public boolean onItemLongClick(AdapterView<?> arg0, View arg1,    
	                   int arg2, long arg3) {               	
					try {
						Method createBondMethod = BluetoothDevice.class.getMethod("removeBond");
						createBondMethod.invoke(bondDevices.get(arg2)); 
					} catch (Exception e) {
						Toast.makeText(ClientActivity, "ȡ�����ʧ�ܣ�", Toast.LENGTH_SHORT).show();    
					}                                
					if(bondDevices.size()>0){//�����û������ǰֱ��ȡ���������,��ʱbondDevicesΪ���򱨴�
						// ���󶨺õ��豸��ӵ��Ѱ�list����    
						unbondDevices.add(bondDevices.get(arg2));  
		                // ���󶨺õ��豸��δ��list�������Ƴ�    
						bondDevices.remove(arg2);   
					}				
	               addBondDevicesToListView();    
	               addUnbondDevicesToListView(); 
	               return true;
	           }    
	       }); 
	}  


}
