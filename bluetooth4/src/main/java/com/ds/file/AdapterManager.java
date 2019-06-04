package com.ds.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.ds.bluetooth.R;

/**
 * ������������
 * @author 210001001427
 *
 */
public class AdapterManager {
	private Context mContext;
	private FileListAdapter mFileListAdapter;    //�ļ��б�adapter
	private List<BluetoothDevice> mDeviceList;   //�豸����
	private List<File> mFileList;    //�ļ�����
	private Handler mainHandler;   //���߳�Handler
	
	public AdapterManager(Context context){
		this.mContext = context;
	}
	

	
	/**
	 * ȡ���ļ��б�adapter
	 * @return
	 */
	public FileListAdapter getFileListAdapter(){
		System.out.print("11111111111");
		Log.v("����" , "getFileListAdapter");
		if(null == mFileListAdapter){
			mFileList = new ArrayList<File>();
			mFileListAdapter = new FileListAdapter(mContext, mFileList, R.layout.file_list_item);
		}	
		System.out.print("222222222222222");
		return mFileListAdapter;
	}
	

	/**
	 * ����豸�б�
	 */
	public void clearDevice(){
		if(null != mDeviceList){
			mDeviceList.clear();
		}
	}
	
	/**
	 * ����豸
	 * @param bluetoothDevice
	 */
	public void addDevice(BluetoothDevice bluetoothDevice){
		mDeviceList.add(bluetoothDevice);
	}
	
	/**
	 * �����豸��Ϣ
	 * @param listId
	 * @param bluetoothDevice
	 */
	public void changeDevice(int listId, BluetoothDevice bluetoothDevice){
		mDeviceList.remove(listId);
		mDeviceList.add(listId, bluetoothDevice);
	}
	
	/**
	 * �����ļ��б�
	 * @param path
	 */
	public void updateFileListAdapter(String path){
		mFileList.clear();
		Log.v("��ӡ·��" , "AdapterManager��"+path);

		mFileList.addAll(FileUtil.getFileList(path));
		if(null == mainHandler){
			mainHandler = new Handler(mContext.getMainLooper());
		}
		mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mFileListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	/**
	 * ȡ���豸�б�
	 * @return
	 */
	public List<BluetoothDevice> getDeviceList() {
		return mDeviceList;
	}
}
