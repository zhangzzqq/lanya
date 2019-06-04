package com.ds.bluetoothUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.util.Calendar;

import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
	
/**
 * ����ͨѶ�߳�
 * @author liujian
 *
 */
public class BluetoothCommunSocket {
	private Handler serviceHandler;		//��Serviceͨ�ŵ�Handler
	public BluetoothSocket socket;
	public DataInputStream inStream;		//����������
	public DataOutputStream outStream;	//���������
	public volatile boolean isRun = true;	//���б�־λ
	private long downbl;

	/**
	 * ���캯��
	 * @param handler ���ڽ�����Ϣ
	 * @param socket
	 */
	public BluetoothCommunSocket(Handler handler, BluetoothSocket socket) {
		this.serviceHandler = handler;
		this.socket = socket;
		try {
			this.inStream= new DataInputStream(this.socket.getInputStream());
			this.outStream= new DataOutputStream(this.socket.getOutputStream());
		} catch (Exception e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//��������ʧ����Ϣ
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			e.printStackTrace();
		}
	}
	public void close(){
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (outStream != null) {
			try {
				outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (socket != null) {
			try {
				socket.close();
				Log.v("����" , "clientsocket�ѹر�");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * д����
	 * @param obj
	 */
	public void write(Object obj) {
		try {
			TransmitBean transmit_s = (TransmitBean) obj;
			if(transmit_s.getFilename()!=null&&!"".equals(transmit_s.getFilename())){	
				Log.v("����" , "type:"+2);
				String filename=transmit_s.getFilename();
				byte type = 2; //����Ϊ2�������ļ� 
				//��ȡ�ļ�����
				FileInputStream fins=new FileInputStream(transmit_s.getFilepath());   
				long fileDataLen = fins.available(); //�ļ����ܳ���			
				int f_len=filename.getBytes("GBK").length; //�ļ�������
				
				byte[] data=new byte[f_len];
				data=filename.getBytes("GBK");
				long totalLen = 4+1+1+f_len+fileDataLen;//���ݵ��ܳ���
				outStream.writeLong(totalLen); //1.д�����ݵ��ܳ���
				outStream.writeByte(type);//2.д������
				outStream.writeByte(f_len); //3.д���ļ����ĳ���
				outStream.write(data);    //4.д���ļ���������
				outStream.flush();								
				//��ȡ�ļ�������
				try { 	
					byte[] buffer=new byte[1024*64]; 
					downbl=0;
					int size=0;
					long sendlen=0;
					float tspeed=0;
					int i=0;
					long time1=Calendar.getInstance().getTimeInMillis();
					while((size=fins.read(buffer, 0, 1024*64))!=-1)  
					{  						
						outStream.write(buffer, 0, size);
						outStream.flush();
						sendlen+=size;
						Log.v("����" , "fileDataLen:"+fileDataLen);
						i++;
						if(i%5==0){
							long time2=Calendar.getInstance().getTimeInMillis();
							tspeed=sendlen/(time2-time1)*1000/1024;	
						}
					//	Log.v("����" ,"tspeed��"+tspeed);
						downbl = ((sendlen * 100) / fileDataLen);
						TransmitBean up = new TransmitBean();
						up.setUppercent(String.valueOf(downbl));	
						up.setTspeed(String.valueOf(tspeed));
						if(i==1){
							up.setShowflag(true);
						}else{
							up.setShowflag(false);
						}
						Message msg = serviceHandler.obtainMessage();											
						msg.what = BluetoothTools.FILE_SEND_PERCENT;					
						msg.obj = up;
						msg.sendToTarget();		
					}    
					fins.close();    	
					Log.v("����" , "�ļ��������");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}		
			}else{
				Log.v("����" , "type:"+1);
				byte type = 1; //����Ϊ1�������ı���Ϣ
				String msg=transmit_s.getMsg();
				int f_len=msg.getBytes("GBK").length; //��Ϣ����
				long totalLen = 4+1+1+f_len;//���ݵ��ܳ���
				byte[] data=new byte[f_len];
				data=msg.getBytes("GBK");			
				outStream.writeLong(totalLen); //1.д�����ݵ��ܳ���
				outStream.writeByte(type);//2.д������
				outStream.writeByte(f_len); //3.д����Ϣ�ĳ���
				outStream.write(data);    //4.д����Ϣ����
				outStream.flush();
			}
			
			this.read();
			
			byte[] ef = new byte[3];
			inStream.read(ef);//��ȡ��Ϣ
			String eof = new String(ef);
			if("EOF".equals(eof)){
				Log.v("����" ,"����EOF");
			}
		}catch (Exception ex) {
			Log.v("����" , "ͨѶ�ж�Exception:");
			//����ͨѶʧ����Ϣ
			serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
			ex.printStackTrace();
		}
		finally {
			close();
		}
	}
	
	
	public void read() throws IOException{			
		TransmitBean transmit_r = new TransmitBean();
		long totalLen = inStream.readLong();//�ܳ���
		if(totalLen>0){
		byte type = inStream.readByte();//����
		if(type==1){//�ı�����
			try {
				byte len = inStream.readByte();//��Ϣ����
				byte[] ml = new byte[len];
				int size=0;
				int receivelen=0;
				while (receivelen <len){						
					size=inStream.read(ml,0,ml.length);
					receivelen+=size;
				}
				String msg = new String(ml);
				Log.v("����" , "msg:"+msg);
				transmit_r.setMsg(msg);			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}				
		if(type==2){//�ļ�����
			try {
				byte len = inStream.readByte();//�ļ�������
				byte[] fn = new byte[len];
				inStream.read(fn);//��ȡ�ļ���
				String filename = new String(fn,"GBK");
				Log.v("����" , "filename:"+filename);
				transmit_r.setFilename(filename);						
				long datalength = totalLen-1-4-1-fn.length;//�ļ�����		
				String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + transmit_r.getFilename();
				transmit_r.setFilepath(savePath);
				FileOutputStream file=new FileOutputStream(savePath, false);						
				byte[] buffer = new byte[1024*1024];
				int size = -1;
				long receivelen=0;
				int i=0;
				float tspeed=0;
				long time1=Calendar.getInstance().getTimeInMillis();
				while (receivelen <datalength){						
					size=inStream.read(buffer);
					file.write(buffer, 0 ,size);
					receivelen+=size;
					i++;
					if(i%10==0){
						long time2=Calendar.getInstance().getTimeInMillis();
						tspeed=receivelen/(time2-time1)*1000/1024;	
					}													
					downbl = (receivelen * 100) / datalength;						
					TransmitBean up = new TransmitBean();
					up.setUppercent(String.valueOf(downbl));	
					up.setTspeed(String.valueOf(tspeed));
					if(i==1){
						up.setShowflag(true);
					}else{
						up.setShowflag(false);
					}
					Message msg = serviceHandler.obtainMessage();											
					msg.what = BluetoothTools.FILE_RECIVE_PERCENT;					
					msg.obj = up;
					msg.sendToTarget();	
				}
				Log.v("����" , "�������,receivelen:"+receivelen);
				file.flush();
				file.close();				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//���ͳɹ���ȡ���������Ϣ����Ϣ��obj����Ϊ��ȡ���Ķ���
		Message msg = serviceHandler.obtainMessage();
		msg.what = BluetoothTools.MESSAGE_READ_OBJECT;	
		msg.obj = transmit_r;
		msg.sendToTarget();
		}
	}

}
