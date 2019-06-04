package com.ds.bluetooth;

import java.util.Date;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.BluetoothClientService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;
import com.ds.file.SelectFileActivity;


public class ClientActivity2 extends Activity {

	public static final int RESULT_CODE = 1000;    //ѡ���ļ�   ������
	public static final String SEND_FILE_NAME = "sendFileName";
	private  TextView serversText;
	private  EditText chatEditText;
	private EditText sendEditText;
	private  Button sendBtn;
	private Button filesendBtn;
	Button mSelectFileBtn;
	TextView mSendFileNameTV;
	private ProgressDialog spDialog; 
	private ProgressDialog rpDialog;
	
	
	@Override
	protected void onStart() {
		
		//������̨service  ��Ϊ֮ǰ�رպ�̨service���˴�����ֻ�ǵ��ú�̨service��onStart����������ȥ�������startService
//		Intent startService = new Intent(ClientActivity2.this, BluetoothClientService.class);
//		startService(startService);
			
		//ע��BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_ERROR);
		intentFilter.addAction(BluetoothTools.ACTION_FILE_SEND_PERCENT);
		intentFilter.addAction(BluetoothTools.ACTION_FILE_RECIVE_PERCENT);
		registerReceiver(broadcastReceiver, intentFilter);
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client2);		
		spDialog=new ProgressDialog(ClientActivity2.this);
		rpDialog=new ProgressDialog(ClientActivity2.this);
	//	serversText = (TextView)findViewById(R.id.clientServersText);
		chatEditText = (EditText)findViewById(R.id.clientChatEditText);
		sendEditText = (EditText)findViewById(R.id.clientSendEditText);	
		mSendFileNameTV = (TextView) findViewById(R.id.sendFileTV);
		mSelectFileBtn = (Button) findViewById(R.id.cancelSearchBtn);
		mSelectFileBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				Intent intent = new Intent(ClientActivity2.this, SelectFileActivity.class);
				startActivityForResult(intent, ClientActivity2.RESULT_CODE);				
			}
		});
		
		sendBtn = (Button)findViewById(R.id.clientSendMsgBtn);
		sendBtn.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				//������Ϣ
				if ("".equals(sendEditText.getText().toString().trim())) {
					Toast.makeText(ClientActivity2.this, "���벻��Ϊ��", Toast.LENGTH_SHORT).show();
				} else {
					//������Ϣ
					TransmitBean data = new TransmitBean();
					data.setMsg(sendEditText.getText().toString());
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
					sendEditText.setText("");
				}
			}
		});
		
		filesendBtn = (Button)findViewById(R.id.fileSendBtn);
		filesendBtn.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				/*�����ļ�  ����Intent�޷����ݺܶ����ݣ������Ƚ��ļ�·���㲥��BluetoothClientService��
				 * *�ɸ�Service��ȡ�ļ���ͨ�����������͸�Զ�������豸
				 */
				if ("".equals(mSendFileNameTV.getText().toString().trim())) {
					Toast.makeText(ClientActivity2.this, "δѡ���ļ�", Toast.LENGTH_SHORT).show();
				} else {
					TransmitBean transmit = new TransmitBean();
					String path=mSendFileNameTV.getText().toString();
					String filename=path.substring(path.lastIndexOf("/")+1,path.length());
					transmit.setFilename(filename);
					transmit.setFilepath(path);
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
					sendDataIntent.putExtra(BluetoothTools.DATA, transmit);
					sendBroadcast(sendDataIntent);
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RESULT_CODE){
			//����Ϊ "ѡ���ļ�"
			try {
				//ȡ��ѡ����ļ���
				String sendFileName = data.getStringExtra(SEND_FILE_NAME);
				mSendFileNameTV.setText(sendFileName);
			} catch (Exception e) {				
			}
		}	
	}
	
	//�㲥������
	public  BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {	
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();		
//			if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {//���ӳɹ�
//				serversText.setText("���ӳɹ�");
//				sendBtn.setEnabled(true);				
//			}
			if (BluetoothTools.ACTION_CONNECT_ERROR.equals(action)) {//����ʧ��
				spDialog.dismiss();
				rpDialog.dismiss();
				Toast.makeText(ClientActivity2.this, "ͨѶʧ��", 2000).show(); 
			//	serversText.setText("ͨѶʧ��");
			//	sendBtn.setEnabled(true);				
			}
			if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {//��������
				TransmitBean transmit = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				String msg = "";					
				if(transmit.getFilename()!=null&&!"".equals(transmit.getFilename())){
					msg = "receive file from remote " + new Date().toLocaleString() + " :\r\n" + transmit.getFilename() + "\r\n";
				}else{
					msg = "receive message from remote " + new Date().toLocaleString() + " :\r\n" + transmit.getMsg() + "\r\n";
				}
				chatEditText.append(msg);
			}
			if (BluetoothTools.ACTION_FILE_SEND_PERCENT.equals(action)) {//�����ļ��ٷֱ�
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);			  
				spDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
				spDialog.setTitle("��ʾ");  
				spDialog.setIcon(R.drawable.icon);  
				if(!"0".equals(data.getTspeed())){
					spDialog.setMessage("�ļ������ٶ�:"+data.getTspeed()+"k/s");  
				}
				spDialog.setMax(100);  
				spDialog.setProgress(Integer.valueOf(data.getUppercent())); 
				spDialog.setIndeterminate(false);  
				spDialog.setCancelable(true);  
//				spDialog.setButton("ȡ��", new DialogInterface.OnClickListener(){  
//				    @Override  
//				    public void onClick(DialogInterface dialog, int which) {  
//				        dialog.cancel();  			          
//				    }      
//				});				
				spDialog.show();
				if(Integer.valueOf(data.getUppercent())==100){				
					spDialog.dismiss();
					spDialog.setProgress(0);  
				}			
			}
			if (BluetoothTools.ACTION_FILE_RECIVE_PERCENT.equals(action)) {//�����ļ��ٷֱ�
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);				  
				rpDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
				rpDialog.setTitle("��ʾ");  
				rpDialog.setIcon(R.drawable.icon);   
				if(!"0".equals(data.getTspeed())){
					rpDialog.setMessage("�ļ������ٶ�:"+data.getTspeed()+"k/s");  
				}
				rpDialog.setMax(100);  
				rpDialog.setProgress(Integer.valueOf(data.getUppercent()));   
				rpDialog.setIndeterminate(false);  
				rpDialog.setCancelable(true);  
			
				rpDialog.show();
				if(Integer.valueOf(data.getUppercent())==100){				
					rpDialog.dismiss();
					rpDialog.setProgress(0);  
				}			
			}
		}
	};

//	@Override
//	public void onBackPressed() {
//	// ���ﴦ���߼����룬ע�⣺�÷�����������2.0����°��sdk
//		//�رպ�̨Service 
//		Intent stopService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
//		sendBroadcast(stopService);
//		unregisterReceiver(broadcastReceiver);	 
//		super.onBackPressed();
//	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);		
		super.onStop();
	}

	@Override
	protected void onStop() {		
		super.onStop();
	}
}
