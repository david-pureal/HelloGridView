package study.hellogridview;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class TCPClient {
	
	private static TCPClient tcpClient;
	public ClientThread clientThread;
	
	public MainActivity main_activity;
	public DishActivity dish_activity;
	public CurStateActivity curstate_activity;
	public BuiltinDishes buildin_activity;
	public SettingActivity setting_activity;
	public MakeDishActivity makedish_activity;
	
	private DeviceState ds;
	
	private TCPClient() {
		clientThread = new ClientThread();
		new Thread(clientThread).start();
		
		//  ��������
		new Thread() {
			@Override
			public void run() {
				long last_hb = System.currentTimeMillis();
				while (true) {
					if (clientThread.revHandler != null && clientThread.s.isConnected()) {
						long curr = System.currentTimeMillis();
						if (curr - last_hb > 30*1000) {
							Message msg = new Message();  
		                    msg.what = 0x345;  
		                    //byte any = 0x03;
		                    //msg.obj = any;
		                    Package data = new Package(Package.Get_Favorite);
		                    msg.obj = data.getBytes();
		                    TCPClient.getInstance().sendMsg(msg);
		                    last_hb = curr;
		                    Log.v("tcpclient", "clientThread heartbeat");
						}
					}
					try {
						Thread.sleep(30*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}.start();
	}
	
	public static TCPClient getInstance() { 
		if (tcpClient == null) {  
	        tcpClient = new TCPClient();  
	    }
		return tcpClient;
	}
	
	public void connect_ip_sta(String sta_ip) {
		Message msg = new Message();
		msg.what = Constants.MSG_ID_STA_IP;
		msg.obj = sta_ip;
		this.sendMsg(msg);
	}
	
	public void set_mainact(MainActivity main) {
		this.main_activity = main;
	}
	public void set_curstateact(CurStateActivity curstateact) {
		this.curstate_activity = curstateact;
	}
	
	public void set_dishact(DishActivity dishact) {
		this.dish_activity = dishact;
	}
	
	public void set_builtinact(BuiltinDishes bact) {
		this.buildin_activity = bact;
	}
	
	public void set_settingact(SettingActivity sact) {
		this.setting_activity = sact;
	}
	
	public void set_makedishact(MakeDishActivity sact) {
		this.makedish_activity = sact;
	}
	
	public boolean sendMsg(Message msg) {
		if (clientThread != null && clientThread.revHandler != null) {
			return clientThread.revHandler.sendMessage(msg);
		} else {
			Log.w("tcpclient", "clientThread or revHandler is null");
		}
		return false;
	}
	
	public class ClientThread implements Runnable {
		private Socket s;
		// ������UI�̷߳�����Ϣ��Handler����
		Handler handler;
		// �������UI�̵߳�Handler����
		Handler revHandler;
		// ���̴߳���Socket�����õ����������
		BufferedInputStream  br = null;
		OutputStream os = null;
		
		public String ip_ap = Constants.AP_IP;
		//public String ip_ap = "192.168.1.172";
		public String ip_sta;
		public short port = Constants.AP_STA_PORT;

		public ClientThread() {}
		
		public ClientThread(Handler handler) {
			this.handler = handler;
		}
		
		public void set_handler(Handler handler) {
			this.handler = handler;
		}
		
		public void set_ip_sta(String ip_sta) {
			this.ip_sta = ip_sta;
			try {
				if (s != null) {
					s.close();
				}
				
				s = new Socket();
				s.connect(new InetSocketAddress(this.ip_sta, port), Constants.BBXC_SOCKET_TIMEOUT);
				br = new BufferedInputStream(new DataInputStream(s.getInputStream()));
				os = s.getOutputStream();
			} catch (IOException io) {
				io.printStackTrace();
			}
			Log.v("TcpClient" ,"Reconnect to sta_ip(" + ip_sta + " done!");
		}
		
		int notifycount = 0;
		public boolean notify(DishActivity act) {
			if (notifycount > 10 && notifycount%3 != 0) {
				// don't bother too often
				++notifycount;
				return true;
			}
			if (act != null && act.getHandler() != null) {
				act.getHandler().post(new Runnable() {
					String info = "���ӵ��豸ʧ��(��ȷ��wifi�����ӵ�" + Constants.AP_NAME + ip_ap +";" + port + " failed! try reconnect...";
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (curstate_activity != null) Toast.makeText(curstate_activity, info, Toast.LENGTH_SHORT).show();
						else if (buildin_activity != null) Toast.makeText(buildin_activity, info, Toast.LENGTH_SHORT).show();
						else if (setting_activity != null) Toast.makeText(setting_activity, info, Toast.LENGTH_SHORT).show();
					}
				});
				++notifycount;
				return true;
			}
			return false;
		}
		
		public boolean notify_setting(SettingActivity act) {
			if (notifycount > 10 && notifycount%3 != 0) {
				// don't bother too often
				++notifycount;
				return true;
			}
			if (act != null && act.getHandler() != null) {
				act.getHandler().post(new Runnable() {
					String info = "���ӵ��豸ʧ��(��ȷ��wifi�����ӵ�" + Constants.AP_NAME + ip_ap +";" + port + " failed! try reconnect...";
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (curstate_activity != null) Toast.makeText(curstate_activity, info, Toast.LENGTH_SHORT).show();
						else if (buildin_activity != null) Toast.makeText(buildin_activity, info, Toast.LENGTH_SHORT).show();
						else if (setting_activity != null) Toast.makeText(setting_activity, info, Toast.LENGTH_SHORT).show();
					}
				});
				++notifycount;
				return true;
			}
			return false;
		}
		
		public boolean notify_curstate(CurStateActivity act) {
			if (notifycount > 10 && notifycount%3 != 0) {
				// don't bother too often
				++notifycount;
				return true;
			}
			if (act != null && act.getHandler() != null) {
				act.getHandler().post(new Runnable() {
					String info = "���ӵ��豸ʧ��(��ȷ��wifi�����ӵ�" + Constants.AP_NAME + ip_ap +";" + port + " failed! try reconnect...";
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (curstate_activity != null) Toast.makeText(curstate_activity, info, Toast.LENGTH_SHORT).show();
						else if (buildin_activity != null) Toast.makeText(buildin_activity, info, Toast.LENGTH_SHORT).show();
						else if (setting_activity != null) Toast.makeText(setting_activity, info, Toast.LENGTH_SHORT).show();
					}
				});
				++notifycount;
				return true;
			}
			return false;
		}
		
		public boolean notify_buildin(BuiltinDishes act) {
			if (notifycount > 10 && notifycount%3 != 0) {
				// don't bother too often
				++notifycount;
				return true;
			}
			if (act != null && act.getHandler() != null) {
				act.getHandler().post(new Runnable() {
					String info = "���ӵ��豸ʧ��(��ȷ��wifi�����ӵ�" + Constants.AP_NAME + ip_ap +";" + port + " failed! try reconnect...";
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (curstate_activity != null) Toast.makeText(curstate_activity, info, Toast.LENGTH_SHORT).show();
						else if (buildin_activity != null) Toast.makeText(buildin_activity, info, Toast.LENGTH_SHORT).show();
						else if (setting_activity != null) Toast.makeText(setting_activity, info, Toast.LENGTH_SHORT).show();
					}
				});
				++notifycount;
				return true;
			}
			return false;
		}

		@Override
		public void run() {
			s = new Socket();
			try {
				while (true) {
					try {
						if (Tool.getInstance().isWifiConnected(main_activity)) {
							Log.v("tcpclient", "not connected to any wifi, don't try to connect device");
							Thread.sleep(15000);
							continue;
						}
						
						// �����ǰwifi���豸��AP���Ǿ�����ʹ��APģʽ
						if (Tool.getInstance().getSSid(main_activity).equals(Constants.AP_NAME)) {
							s.connect(new InetSocketAddress(ip_ap, port), Constants.BBXC_SOCKET_TIMEOUT);
						}
						else if (!ip_sta.isEmpty()) {
							s.connect(new InetSocketAddress(ip_sta, port), Constants.BBXC_SOCKET_TIMEOUT);
						}
						else {
							Log.v("tcpclient", "��ʹ��Smartlinkģʽ�����豸");
							Thread.sleep(15000);
							continue;
						}
						
						if (s.isConnected()) {
							break;
						} else {
							Thread.sleep(15000);
						}
					} catch (Exception e) {
						Log.e("tcpclient", "socket connect to " + ip_ap +";" + port + " failed! try reconnect...");
						if (notify(dish_activity) || notify_curstate(curstate_activity) || notify_buildin(buildin_activity) || notify_setting(setting_activity));
						Thread.sleep(15000);
					}
				}
				Log.e("tcpclient", "socket connected");
						
				br = new BufferedInputStream(new DataInputStream(s.getInputStream()));
				os = s.getOutputStream();
				
				// ����һ�����߳�����ȡ��������Ӧ������
				new Thread() {
					@Override
					public void run() {
						//String content = null;
						// ���ϵĶ�ȡSocket������������
						ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
						while (read_package(br, bytestream)) {
							// ÿ����ȡ�����Է�����������֮�󣬷��͵���Ϣ֪ͨ����,������ʾ������
							TCPClient.getInstance().OnReceive(bytestream);
							bytestream.reset();
						}
					}
				}.start();
				
				// Ϊ��ǰ�̳߳�ʼ��Looper
				Looper.prepare();
				// ����revHandler����
				revHandler = new Handler() {

					@SuppressLint("HandlerLeak")
					@Override
					public void handleMessage(Message msg) {
						// ���յ�UI�̵߳����û����������
						if (msg.what == 0x345) {
							// ���û����ı������������д������
							try {
								//os.write((msg.obj.toString() + "\r\n").getBytes("gbk"));
								ByteArrayOutputStream bst= (ByteArrayOutputStream)(msg.obj);
								Log.v("tcpclient", "len(sendMsg.obj)=" + bst.size());
								Log.v("tcpclient", "send data =" + getstr(bst));
								bst.write(13);
								bst.write(10);
								byte[] bs = bst.toByteArray();
								os.write(bs);
								os.flush();
								/*if (bs.length > 1000) */Thread.sleep(500);
							} catch (Exception e) {
								e.printStackTrace();
								Log.v("tcpclient", "sendMsg exception");
							}
						}
						else if (msg.what == Constants.MSG_ID_STA_IP) {
						set_ip_sta((String) msg.obj);
						}
					}
				}; 
				// ����Looper
				Looper.loop();
				Log.i("tcpclient", "revHandler created done");

			} catch (SocketTimeoutException e) {
				Message msg = new Message();
				msg.what = 0x123;
				msg.obj = "�������ӳ�ʱ��";
				e.printStackTrace();
			} catch (IOException io) {
				io.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	// read N bytes from socket
	boolean read_n(BufferedInputStream bis, int n, byte[] buffer) {
		int nidx = 0;
        int total = n;
        int nreadlen = 0;
        
        while (nidx < total) {
            try {
				nreadlen = bis.read(buffer, nidx, total - nidx);
				
	            if (nreadlen > 0) {
	                nidx = nidx + nreadlen;
	            } else {
	                Log.v("tcpclient", "BufferedInputStream read.ret=" + nreadlen + ",bis.avaliable=" + bis.available() + "going to wait data...");
	                if (TCPClient.getInstance().clientThread.s.isConnected() == false) {
	                	Log.e("tcpclient", "socket disconnected!");
	                }
	                // ���ظ�����ʾ������û����ô�����ݣ����Եȴ�һ��
	                Thread.sleep(100);
					continue;
	            }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("tcpclient", "BufferedInputStream read error! ");
				return false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("tcpclient", "Thread.sleep exception! ");
				return false;
			}
        }
        return true;
	}
	
	// little endian
	public int gotInt(byte[] bs, int i) {
		byte[] b = new byte[4];
		b[0] = bs[i + 3];
		b[1] = bs[i + 2];
		b[2] = bs[i + 1];
		b[3] = bs[i + 0];
		
		return (b[0] << 24 & 0xff000000) | (b[1] << 16 & 0xff0000) | (b[2] << 8 & 0xff00) | ((b[3] << 0) & 0xff);
	}
	
	// little endian
	public short gotShort(byte[] bs, int i) {		
		return (short) (((bs[i+1] << 8) & 0xff00) | ((bs[i] << 0) & 0xff));
	}
	
	protected void OnReceive(ByteArrayOutputStream bytestream) {
		// TODO Auto-generated method stub
		byte[] bs = bytestream.toByteArray();
		if (bs.length < 15) {
			Log.e("tcpclient", "package length error! " + bs.length);
			return;
		}
		byte cmdtype = (byte)(bs[14]);
		switch(cmdtype) {
		case Package.ACK:
			if (bs.length < 26) {
				Log.e("tcpclient", "ACK package length error! " + bs.length);
				break;
			}

			RespPackage rp_ack = new RespPackage();
			rp_ack.reqid_head = gotInt(bs, 10);
			rp_ack.cmdtype_head = bs[14];
			rp_ack.reqid_body = gotInt(bs, 19);
			rp_ack.device_id = gotInt(bs, 15);
			rp_ack.cmdtype_body = bs[23];
			rp_ack.is_ok = (bs[24] == 0x01);
			Log.d("tcpclient", "got " + (rp_ack.is_ok ? "" : "error") + " ACK for cmd:" + 
					(rp_ack.cmdtype_body & 0xff) + ", reqid=" + rp_ack.reqid_body);
			
			//TODO: tell activity
			Message msg_ack = new Message();
			msg_ack.what = 0x123;
			msg_ack.obj = rp_ack;
//			if (this.setting_activity != null && setting_activity.getHandler() != null) {
//				setting_activity.getHandler().sendMessage(msg_ack);
//			} else {
//				Log.v("tcpclient", "setting_activity.getHandler() is null");
//			}
//			
//			if (this.buildin_activity != null && buildin_activity.getHandler() != null) {
//				buildin_activity.getHandler().sendMessage(msg_ack);
//			} else {
//				Log.v("tcpclient", "buildin_activity.getHandler() is null");
//			}
			
			if (this.dish_activity != null && dish_activity.getHandler() != null) {
				dish_activity.getHandler().sendMessage(msg_ack);
			} 
			else if (this.makedish_activity != null && makedish_activity.getHandler() != null) {
				makedish_activity.getHandler().sendMessage(msg_ack);
			} 
			else {
				Log.v("tcpclient", "dish_activity.getHandler() is null");
			}
			break;
		case Package.Machine_State:
			if (bs.length < 35) {
				Log.e("tcpclient", "Machine_State package length error! " + bs.length);
				break;
			}
			
			ds = DeviceState.getInstance();
			ds.working_state = bs[16];
			
			synchronized (this) {
				ds.time = this.gotShort(bs, 20);
				ds.dishid = this.gotShort(bs, 24);
				int tmp = ds.time;
				//����״̬�� 0���������ˣ�1��ͣ��2����
				Log.e("tcpclient", "Machine_Stat = " + bs[15] + ",time = " + tmp + "bs[20]=" + (bs[22] & 0x00ff) + "bs[21]=" + (bs[23] & 0x00ff) + ", dishid=" + ds.dishid);
				ds.working_state = bs[15];
				ds.temp = bs[22];
				ds.jiaoban_speed = bs[23];
			}
			ds.is_pot_in = bs[26];
			ds.is_screen_lock = bs[27];
			ds.use_sound = bs[32];
			ds.use_english = bs[33];
			
			RespPackage rp_ms = new RespPackage();
			rp_ms.reqid_head = gotInt(bs, 10);
			rp_ms.cmdtype_head = bs[14];
			rp_ms.reqid_body = gotInt(bs, 16);
			rp_ms.device_id = gotInt(bs, 28);
			
			Message msg = new Message();
			msg.what = 0x123;
			msg.obj = rp_ms;
			if (curstate_activity != null && curstate_activity.getHandler() != null) {
				curstate_activity.getHandler().sendMessage(msg);
			} else {
				Log.v("tcpclient", "curstate_activity.getHandler() is null");
			}
			
//			if (this.setting_activity != null && setting_activity.getHandler() != null) {
//				setting_activity.getHandler().sendMessage(msg);
//			} else {
//				Log.v("tcpclient", "setting_activity.getHandler() is null");
//			}
			break;
		case Package.Get_Favorite_Resp:
			RespPackage rp_gf = new RespPackage();
			rp_gf.cmdtype_head = bs[14];
			if (bs.length < 26) {
				Log.e("tcpclient", "Get_Favorite_Resp package length error! " + bs.length);
				rp_gf.is_ok = false;
			} else {
				ds = DeviceState.getInstance();
				if (ds.builtin_dishids.length == 12) {
					int start_pos = 15;
					for (int i = 0; i < ds.builtin_dishids.length; ++i) {
						ds.builtin_dishids[i] = gotShort(bs, start_pos);
						start_pos += 2;
					}
				}
			}

			Message msg2 = new Message();
			msg2.what = 0x123;
			msg2.obj = rp_gf;
			if (this.buildin_activity != null && buildin_activity.getHandler() != null) {
				buildin_activity.getHandler().sendMessage(msg2);
			} else {
				Log.v("tcpclient", "buildin_activity.getHandler() is null");
			}
			break;
		}
	}

	// read a whole package from socket
	boolean read_package(BufferedInputStream bis, ByteArrayOutputStream baos) {
		try {
			// ǰ�����ֽ�Ϊ����package�ĳ���
			byte[] bytes_length = new byte[2];
			if (read_n(bis, 2, bytes_length)) {
				baos.write(bytes_length);
				
				int left_length = (short)(((bytes_length[1] << 8) & 0xff) | (bytes_length[0])) - 2;
				if (left_length <= 12 || left_length > 2000) {
					Log.e("tcpclient", "invalid package length = " + (left_length+2) + ", byte[0]=" + (bytes_length[0] & 0xff)  + ", byte[1]=" + (bytes_length[1] & 0xff));
					return true;// just skip
					// TODO fix bug�� Ӧ��ֻ����һ���ֽڵģ������Ǻ����������ֽ�
				} else {
					Log.v("tcpclient", "package length = " + (left_length + 2));
					byte[] bytes_left = new byte[left_length];
					if (read_n(bis, left_length, bytes_left)) {
						baos.write(bytes_left);
						Log.i("tcpclient", "get package success, data = " + getstr(baos));
						return true;
					}
				}
			}
			Log.e("tcpclient", "BufferedInputStream read error!");
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("tcpclient", "BufferedInputStream read error!");
			return false;
		}
	}
	
	// bytes array print to hex string
	String getstr(ByteArrayOutputStream baos) {
		String str = new String();
		byte[] bs = baos.toByteArray();
		for (int i = 0; i < baos.size(); ++i) {
			str += Integer.toHexString((int)(bs[i]) & 0x000000FF);
			if (i != baos.size() - 1) str += ",";
			
			// (cmdtype)
			if (i == 13) str += "(";
			if (i == 14) {
				str += ")";
			}
		}
		return str;
	}
}

