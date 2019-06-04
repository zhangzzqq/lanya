package com.ds.file;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.ds.bluetooth.ClientActivity2;
import com.ds.bluetooth.R;


public class SelectFileActivity extends Activity {
	ListView mFileListView;
	FileListAdapter mFileListAdapter;
	AdapterManager mAdapterManager;

	
	private Handler mOtherHandler;
	private Runnable updateFileListRunnable;
	
	private File file;   //��ǰ�����ļ� �� �ļ���
	
	private String sdcardPath;  //sd��·��
	private String path;    //��ǰ�ļ���Ŀ¼
	
	Button mBackBtn;  //���ذ�ť
	Button mEnsureBtn;   //ȷ����ť
	Button mCancelBtn;   //ȡ����ť

	TextView mLastClickView;   //���һ�ε�����ļ� --�ļ���
	TextView mNowClickView;   //���ڵ�����ļ� -- �ļ���
	private boolean isSelected = false;   //�Ƿ�ѡ�����ļ�   (���ļ���)

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_file);
		
		mFileListView = (ListView) findViewById(R.id.fileListView);
		mBackBtn = (Button) findViewById(R.id.selectFileBackBtn);
		mEnsureBtn = (Button) findViewById(R.id.selectFileEnsureBtn);
		mCancelBtn = (Button) findViewById(R.id.selectFileCancelBtn);
		
		//ȡ��sd��Ŀ¼
		sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		path = sdcardPath;
		
		/*
		//ʵ����Adapter�����������õ�Application ���±�NULL,�пյ��ԡ�
        mAdapterManager = new AdapterManager(this);      
        mApplication.setAdapterManager(mAdapterManager);	
		mAdapterManager = BluetoothApplication.getInstance().getAdapterManager();
		*/
		
		mAdapterManager = new AdapterManager(this);

		mFileListView.setAdapter(mAdapterManager.getFileListAdapter());		
		
		//������ʾsd���������ļ����ļ���
		mAdapterManager.updateFileListAdapter(path);
		
		mFileListView.setOnItemClickListener(mFileListOnItemClickListener);
		mBackBtn.setOnClickListener(mBackBtnClickListener);
		mEnsureBtn.setOnClickListener(mEnsureBtnClickListener);
		mCancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SelectFileActivity.this.finish();
			}
		});
		
	}
	
	/**
	 * 
	 */
	private OnItemClickListener mFileListOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			//��ǰ�����ļ� �� �ļ���
			file = (File) mFileListView.getAdapter().getItem(position);
			if(file.isFile()){
				//������ļ��� ��ѡ�� --- �ļ�����ɫ 
				if(null != mLastClickView){
					//��֮ǰѡ�����ļ��� ��ȡ��֮ǰѡ��  -- �ָ���ɫ
					mLastClickView.setTextColor(Color.WHITE);
				}
				//�ı��ļ�����ɫ, ѡ���ļ�
				mNowClickView = (TextView) view.findViewById(R.id.fileNameTV);
				mNowClickView.setTextColor(Color.BLUE);
				isSelected = true;
				//����Ϊ���һ�ε�����ļ�
				mLastClickView = mNowClickView;
			}else {
				//������ļ��У� ����ʾ���ļ����������ļ� �� �ļ���
				path = file.getAbsolutePath();
				updateFileList();
			}							
		}

	};
	
	private OnClickListener mBackBtnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(path.equals(sdcardPath)){
				//��ǰ�ļ���Ŀ¼Ϊ sd���� �����κβ���
				return ;
			}
			//������һ��Ŀ¼
			path = path.substring(0, path.lastIndexOf("/"));
			updateFileList();
		}
	};
	
	private OnClickListener mEnsureBtnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(!isSelected){
				//û��ѡ���ļ�
				Toast.makeText(SelectFileActivity.this, "��ѡ���ļ�!", Toast.LENGTH_LONG).show();
				return ;
			}
			//��ѡ����ļ���ȫ·�� ����
			Intent intent = new Intent();
			intent.putExtra(ClientActivity2.SEND_FILE_NAME, file.getAbsolutePath());
			SelectFileActivity.this.setResult(ClientActivity2.RESULT_CODE, intent);
			SelectFileActivity.this.finish();
		}
	};
	
	/**
	 * ���ݸ�Ŀ¼path��ʾpath�������ļ����ļ���
	 */
	private void updateFileList() {
		if(null != mLastClickView){
			//������һ�ļ��У���ȡ��֮ǰ��ѡ��
			mLastClickView.setTextColor(Color.WHITE);
			mLastClickView = null;
			isSelected = false;
		}
		if(null == updateFileListRunnable){
			updateFileListRunnable = new Runnable() {
							
				@Override
				public void run() {
					Log.v("��ӡ·��" , "SelectFileActivity��"+path);
					mAdapterManager.updateFileListAdapter(path);
				}
			};
		}
		if(null == mOtherHandler){
			HandlerThread handlerThread = new HandlerThread("other_thread");
			handlerThread.start();
			mOtherHandler = new Handler(handlerThread.getLooper());
		}
		mOtherHandler.post(updateFileListRunnable);
	}
}
