package com.example.lanyastudy2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author 作者 :zhangqi
 * @version 版本号 :
 * @date 创建时间 :2019/6/4
 * @Description 描述 :
 **/
public class BluePairedActivity extends Activity {
    public static final String TAG ="Chunna==BlueActivity";
    private List<HashMap> blueList;
    private HashMap blueHashMap;
    private ListView glvPaired;
    private BluetoothAdapter adapter;
    private PairedBluetoothDialogAdapter pairedAdapter;

    public static BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_paired);
        initBlueTooth();
        glvPaired = (ListView)findViewById(R.id.lv_blue_paired);
        pairedAdapter = new PairedBluetoothDialogAdapter(this,blueList);
        pairedAdapter.notifyDataSetChanged();

        glvPaired.setAdapter(pairedAdapter);
        glvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice gDevice = (BluetoothDevice)(((HashMap)pairedAdapter.getItem(position)).get("blue_device"));
                Log.d(TAG, "想要连接的远程主机：" + gDevice);
                Log.d(TAG, "想要连接的远程主机：" + gDevice.toString());

                //然后就可以连接或者做操作啦
            }
        });
    }

    private void initBlueTooth() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                adapter.enable();
                //sleep one second ,avoid do not discovery
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            blueList = new ArrayList<HashMap>();
            Log.d(TAG,"获取已经配对devices"+devices.size());
            for (BluetoothDevice bluetoothDevice : devices)
            {
                Log.d(TAG, "已经配对的蓝牙设备：");
                Log.d(TAG, bluetoothDevice.getName());
                Log.d(TAG, bluetoothDevice.getAddress());
                blueHashMap = new HashMap();
                blueHashMap.put("blue_device",bluetoothDevice);
                blueHashMap.put("blue_name",bluetoothDevice.getName());
                blueHashMap.put("blue_address",bluetoothDevice.getAddress());
                blueList.add(blueHashMap);
            }
        }else{
            Toast.makeText(this,"本机没有蓝牙设备",Toast.LENGTH_LONG).show();
        }
    }
}
