package study.hellogridview;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TCPClient {
	
	private static TCPClient tcpClient;
	public ClientThread clientThread;
	
	private UdpBroadcast udpBroadcast;
	private List<Module> mModules;
	
	public MainActivity main_activity;
	public DishActivity dish_activity;
	public CurStateActivity curstate_activity;
	public BuiltinDishes buildin_activity;
	public SettingActivity setting_activity;
	public MakeDishActivityJ makedish_activity;
	public AllDish alldish_activity;
	
	private DeviceState ds;
	
	public int connect_state = Constants.CONNECTING;
	public int connect_state_reason = 0;
	public long start_connecting_timestamp = System.currentTimeMillis();
	
	public boolean is_stop = false;
	
	boolean check_resp_thread_is_pause = true;
	
	private TCPClient() {
		init();
		Log.v("tcpclient", "in TCPClient()");
	} // TCPClient()
	
	private TCPClient(Activity main_activity) {
		this.main_activity = (MainActivity) main_activity;
		init();
	}
	
	private void init() {
		udpBroadcast = new UdpBroadcast() {
			@Override
			public void onReceived(List<DatagramPacket> packets) {
				mModules = decodePackets(packets);
				//只取第一个module，暂不考虑有两个module的情况
				//如果没有连接wifi，那么modules肯定为空
				for (int i = 0; i < mModules.size(); ++i) {
					Module m = mModules.get(i);
					if (!m.getIp().equals(Constants.AP_IP) && 
							!Tool.getInstance().getSSid(main_activity).startsWith(Constants.AP_NAME_PREFIX)){
						Log.v("tcpclient", "find device in sta mode, ip = " + m.getIp());
						if (connect_state != Constants.CONNECTED) {
							connect_ip_sta(m.getIp());
						}
					}
				}
			}
		};
		
		//  发送心跳, keep-alive
		new Thread() {
			@Override
			public void run() {
				long last_hb = System.currentTimeMillis();
				Log.v("tcpclient", "heartbeat Thread starting");
				while (true) {
					try {
						if (clientThread != null && clientThread.revHandler != null && clientThread.s.isConnected()) {
							long curr = System.currentTimeMillis();
							if (curr - last_hb >= Constants.HEARTBEAT_INTEVAL) {
								do_heartbeat();
			                    last_hb = curr;
			                    Log.v("tcpclient", "send heartbeat");
			                    Thread.sleep(Constants.HEARTBEAT_INTEVAL/2);
							}
						} else {
							Log.v("TcpClient", "socket is not connected yet, try heartbeat later");
							Thread.sleep(Constants.HEARTBEAT_INTEVAL/2);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		//  在connected的情况下检查机器是不是没有了响应
		new Thread() {
			@Override
			public void run() {
				Log.v("tcpclient", "check machine resp Thread starting");
				while (true) {
					try {
						if (!check_resp_thread_is_pause) {
							long curr = System.currentTimeMillis();
							if (curr - last_resp_timestamp >= Constants.CONNECTED_RESP_TIMEOUT) {
								notify_connect_state(Constants.DISCONNECTED);
								Log.v("tcpclient", "disconnected because check machine resp fail.");
								
								clientThread.recv_data.stop_recv = true;
								Thread.sleep(3000);
								
								// TODO reconnect
								String curr_ssid = Tool.getInstance().getSSid(main_activity);
								// AP 模式下，关闭机器并立即，此时并没有检测到wifi变化，但socket已经断开，需要重新连接到 AP的wifi
								Log.v("tcpclient", "check machine resp cur_ip=" + clientThread.current_ip + "," + curr_ssid + ", con_ssid=" + connected_ssid);
								if (clientThread.current_ip.equals(Constants.AP_IP) && curr_ssid.equals(connected_ssid)) {
									
									WifiManager mWifiManager = (WifiManager) main_activity.getSystemService(Context.WIFI_SERVICE); 
							        // 取得WifiInfo对象  
							        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo(); 
							        int id = mWifiInfo.getNetworkId();
							        mWifiManager.disconnect();
							        mWifiManager.enableNetwork(id, true);
							        Log.v("tcpclient", "check machine resp reconnect to current AP wifi");
							        
							        clientThread.reconnect();
								}
								else {
									clientThread.reconnect();
								}
							}
							else {
								Log.v("TcpClient", "check machine resp ok, recheck after 3 seconds");
								Thread.sleep(3*1000);
							}
						} else {
							Log.v("TcpClient", "socket is not connected yet, try check machine resp after 5 seconds");
							Thread.sleep(5*1000);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		clientThread = new ClientThread();
		new Thread(clientThread).start();
	}

	public static TCPClient getInstance() { 
		if (tcpClient == null) {  
	        tcpClient = new TCPClient();  
	    }
		return tcpClient;
	}
	
	public static TCPClient getInstance(Activity main_activity) { 
		if (tcpClient == null) {  
	        tcpClient = new TCPClient(main_activity);  
	    }
		return tcpClient;
	}
	
	public void connect_ip_sta(String sta_ip) {
		MyPreference.set_sta_ip(main_activity, sta_ip);
		Log.v("tcpclient", "set_sta_ip = " + sta_ip + " done.");
		
		if (connect_state_reason == Constants.RECONNECTING || connect_state_reason == Constants.RECONNECTING) {
			clientThread.current_ip = sta_ip;
		}

		if (clientThread != null && clientThread.revHandler != null) {
			Message msg = new Message();
			msg.what = Constants.MSG_ID_STA_IP;
			msg.obj = sta_ip;
			this.sendMsg(msg);
		}
		else {
			clientThread.ip_sta = sta_ip;
		}
	}
	
	// 是否通过AP模式连接机器
	public boolean is_conn_ap() {
		return clientThread!=null && clientThread.current_ip.equals(Constants.AP_IP);
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
	
	public void set_makedishact(MakeDishActivityJ sact) {
		this.makedish_activity = sact;
	}
	
	public void set_alldishact(AllDish all) {
		this.alldish_activity = all;
	}
	
	public boolean sendMsg(Message msg) {
		if (clientThread != null && clientThread.revHandler != null) {
			return clientThread.revHandler.sendMessage(msg);
		} else {
			Log.w("tcpclient", "clientThread or revHandler is null");
		}
		return false;
	}
	
	// do heartbeat after socket is connected
	public void do_heartbeat() {
		Message msg = new Message();  
        msg.what = 0x345;  
        Package data = new Package(Package.Get_Favorite);
        msg.obj = data.getBytes();
        TCPClient.getInstance().sendMsg(msg);
        Log.v("tcpclient", "do get favorite from device");
	}
	
	String connected_ssid = "";
	
	public void notify_connect_state(int con_state) {
		synchronized (this) {
			if (connect_state == con_state) return;
			
			connect_state = con_state;
			if (connect_state == Constants.CONNECTING) {
				TCPClient.getInstance().start_connecting_timestamp = System.currentTimeMillis();
			}
			else if (connect_state == Constants.CONNECTED) {
				connected_ssid = Tool.getInstance().getSSid(main_activity);
				last_resp_timestamp = System.currentTimeMillis();
				check_resp_thread_is_pause = false;
			}
			else if (connect_state == Constants.DISCONNECTED) {
				check_resp_thread_is_pause = true;
			}
			
			if (main_activity != null && main_activity.getHandler() != null) main_activity.getHandler().sendEmptyMessage(Constants.MSG_ID_CONNECT_STATE);
			if (buildin_activity != null && buildin_activity.getHandler() != null) {
				buildin_activity.getHandler().sendEmptyMessage(Constants.MSG_ID_CONNECT_STATE);
			}
			if (dish_activity != null && dish_activity.getHandler() != null) {
				dish_activity.getHandler().sendEmptyMessage(Constants.MSG_ID_CONNECT_STATE);
			}
			if (setting_activity != null && setting_activity.getHandler() != null) {
				setting_activity.getHandler().sendEmptyMessage(Constants.MSG_ID_CONNECT_STATE);
			}
			if (alldish_activity != null && alldish_activity.getHandler() != null) {
				alldish_activity.getHandler().sendEmptyMessage(Constants.MSG_ID_CONNECT_STATE);
			}
		
		}
	}
	
	/**
	 * decode pagkets to mudoles
	 * @param packets
	 * @return
	 */
	private List<Module> decodePackets(List<DatagramPacket> packets) {
		
		int i = 1;
		Module module;
		List<String> list = new ArrayList<String>();
		List<Module> modules = new ArrayList<Module>();
		
		DECODE_PACKETS:
		for (DatagramPacket packet : packets) {
			
			String data = new String(packet.getData(), 0, packet.getLength());
			Log.d("tcpclient", i + ": " + data);
			for (String item : list) {
				if (item.equals(data)) {
					continue DECODE_PACKETS;
				}
			}
			
			list.add(data);
			if ((module = Tool.getInstance().decodeBroadcast2Module(data)) != null) {
				module.setId(i);
				modules.add(module);
				i++;
			}
		}
		return modules;
	}
	
	public class ClientThread implements Runnable {
		private Socket s;
		// 定义向UI线程发送消息的Handler对象
		Handler handler;
		// 定义接收UI线程的Handler对象
		Handler revHandler;
		// 该线程处理Socket所对用的输入输出流
		BufferedInputStream  br = null;
		OutputStream os = null;
		
		public String ip_ap = Constants.AP_IP;
		public String ip_sta = "";
		public String current_ip = "";
		public short port = Constants.AP_STA_PORT;

		public ClientThread() {
			ip_sta = MyPreference.get_sta_ip(main_activity);
			Log.v("tcpclient", "get_sta_ip = " + ip_sta + " done.");
		}
		
		public ClientThread(Handler handler) {
			this.handler = handler;
		}
		
		public void set_handler(Handler handler) {
			this.handler = handler;
		}
		
		public void set_ip_sta(String ip_sta) {
			this.ip_sta = ip_sta;
			current_ip = ip_sta;
//			if (connect_state != Constants.CONNECTED) {
//				Log.v("tcpclient", "ClientThread.set_ip_sta = " + ip_sta);
//				current_ip = ip_sta;
//			}
//			else if (s.isConnected() && !this.current_ip.equals(ip_sta)) {
//				notify_connect_state(Constants.CONNECTING);
//				current_ip = ip_sta;
//				reconnect();
//			}
		}

		@Override
		public void run() {
			s = new Socket();
			udpBroadcast.open();
			try {
				while (true) {
					try {
						if (!Tool.getInstance().isWifiConnected(main_activity)) {
							Log.v("tcpclient", "not connected to any wifi, don't try to connect device");
							
							if (connect_state == Constants.CONNECTING) {
								notify_connect_state(Constants.DISCONNECTED);
							}
							
							synchronized (ClientThread.this) {
								Log.v("tcpclient", "before wait wifi connected.");
								connect_state_reason = Constants.WAIT_WIFI_CONNECTED;
								wait();
								Log.v("tcpclient", "after wait wifi connected.");
							}
						}
						
						// 如果当前wifi是设备的AP，那就优先使用AP模式
						if (Tool.getInstance().getSSid(main_activity).startsWith(Constants.AP_NAME_PREFIX)) {
							Log.e("tcpclient", "socket connect use ip_ap");
							current_ip = ip_ap;
							s.connect(new InetSocketAddress(ip_ap, port), Constants.BBXC_SOCKET_TIMEOUT);
						}
						else if (!ip_sta.isEmpty()) {
							Log.e("tcpclient", "socket trying connect to ip_sta = " + ip_sta);
							current_ip = ip_sta;
							s.connect(new InetSocketAddress(ip_sta, port), Constants.BBXC_SOCKET_TIMEOUT);
							if (!s.isConnected()) {
								Log.e("tcpclient", "socket connect to sta_ip fail");
								ip_sta = "";
								current_ip = "";
							}
						}
						else {
							udpBroadcast.send(Constants.CMD_SCAN_MODULES);
							Log.v("tcpclient", "尝试查找附近的设备，或使用Smartlink模式连接设备");
							
							long current = System.currentTimeMillis();
							if (connect_state == Constants.CONNECTING && current - start_connecting_timestamp > Constants.CONNECT_TIMEOUT) {
								//connect_state = Constants.DISCONNECTED;
								notify_connect_state(Constants.DISCONNECTED);
							}
							
							Thread.sleep(5000);
							continue;
						}
						
						if (s.isConnected()) {
							//connect_state = Constants.CONNECTED;
							notify_connect_state(Constants.CONNECTED);
							
							break;
						} else {
							Log.e("tcpclient", "socket not connected");
							
							long current = System.currentTimeMillis();
							if (connect_state == Constants.CONNECTING && current - start_connecting_timestamp > Constants.CONNECT_TIMEOUT) {
								notify_connect_state(Constants.DISCONNECTED);
							}
							Thread.sleep(10000);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.v("tcpclient", "exception name = " + e.getClass().toString()); //java.net.SocketException, fcntl failed EBADF (Bad file number)
						Log.e("tcpclient", "socket connect to " + current_ip +":" + port + " failed! try reconnect...");
						if (current_ip.equals(ip_sta)) {
							Log.e("tcpclient", "socket connect to sta_ip fail");
							ip_sta = "";
							current_ip = "";
						}
						
						long current = System.currentTimeMillis();
						if (connect_state == Constants.CONNECTING && current - start_connecting_timestamp > Constants.CONNECT_TIMEOUT) {
							//connect_state = Constants.DISCONNECTED;
							notify_connect_state(Constants.DISCONNECTED);
						}
						
						s.close();
						Thread.sleep(10000);
						s = new Socket();
					}
				}
				Log.e("tcpclient", "socket connected");
						
				br = new BufferedInputStream(new DataInputStream(s.getInputStream()));
				os = s.getOutputStream();
				
				// 启动一条子线程来读取服务器相应的数据
				recv_data = new ReceiveThread();
				recv_data.recv_thread.start();
				
				// 为当前线程初始化Looper
				Looper.prepare();
				// 创建revHandler对象
				revHandler = new Handler() {
					@SuppressLint("HandlerLeak")
					@Override
					public void handleMessage(Message msg) {
						this.obtainMessage();
						// 接收到UI线程的中用户输入的数据
						if (msg.what == 0x345) {
							// 将用户在文本框输入的内容写入网络
							try {
								ByteArrayOutputStream bst= (ByteArrayOutputStream)(msg.obj);
								Log.v("tcpclient", "len(sendMsg.obj)=" + bst.size());
								Log.v("tcpclient", "send data =" + getstr(bst));
								bst.write(13);
								bst.write(10);
								byte[] bs = bst.toByteArray();
								os.write(bs);
								os.flush();
								if (bs.length > 100)Thread.sleep(500);
								else Thread.sleep(200);
							} catch (SocketException e) {
								Log.v("tcpclient", "sendMsg SocketException, try to reconnect");
								e.printStackTrace();
								notify_connect_state(Constants.CONNECTING);
								//reconnect();
							}
							catch (Exception e) {
								e.printStackTrace();
								notify_connect_state(Constants.CONNECTING);
								Log.v("tcpclient", "sendMsg exception");
							}
						}
						else if (msg.what == Constants.MSG_ID_STA_IP) {
							set_ip_sta((String) msg.obj);
						}
					}
				};
				
				do_heartbeat(); //get favorite dishes in device
				// 启动Looper
				Looper.loop();

			} catch (SocketTimeoutException e) {
				Message msg = new Message();
				msg.what = 0x123;
				msg.obj = "网络连接超时！";
				e.printStackTrace();
			} catch (IOException io) {
				io.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		public ReceiveThread recv_data;
		
		class ReceiveThread implements Runnable {
			public boolean stop_recv = false;
			public Thread recv_thread;
			public ReceiveThread() {
				recv_thread = new Thread(this);
			}
			@Override
			public void run() {
				while (!stop_recv) {
					// 不断的读取Socket输入流的内容
					ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
					while (read_package(br, bytestream)) {
						TCPClient.getInstance().OnReceive(bytestream);
						bytestream.reset();
					}
					
					// read error
					Log.v("tcpclient", "read package error! stop_recv = " + stop_recv);
					notify_connect_state(Constants.CONNECTING);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Log.v("tcpclient", "ReceiveThread exit");
			}
		} //ReceiveThread
		
		public void reconnect() {
			connect_state_reason = Constants.RECONNECTING;
			while (true) {
				Log.v("tcpclient", "try to reconnect to ip:" + current_ip + ", is_stop = " + is_stop);
				
				try {
					if (is_stop) {
						Thread.sleep(10*1000);
						continue;
					}
					
					recv_data.stop_recv = true;
					Thread.sleep(1000);
					if (s != null) {
						s.close();
					}
					
					if (s.isConnected()) {
						s.close();
						Thread.sleep(1000);
					}
					
					s = new Socket();
					s.connect(new InetSocketAddress(current_ip, port), Constants.BBXC_SOCKET_TIMEOUT);
					
					if (s.isConnected()) {
						br = new BufferedInputStream(new DataInputStream(s.getInputStream()));
						os = s.getOutputStream();
						recv_data = new ReceiveThread();
						recv_data.recv_thread.start();
						
						notify_connect_state(Constants.CONNECTED);
						do_heartbeat(); // get builtin dishes
						connect_state_reason = 0;
						Log.v("tcpclient", "successfully reconnect to ip:" + current_ip);
						break;
					}
					else {
						Log.v("tcpclient", "failed reconnect to ip:" + current_ip);
						long current = System.currentTimeMillis();
						if (connect_state == Constants.CONNECTING && current - start_connecting_timestamp > Constants.CONNECT_TIMEOUT) {
							notify_connect_state(connect_state = Constants.DISCONNECTED);
						}
						
						synchronized (TCPClient.getInstance()) {
							TCPClient.getInstance().wait(5*1000);
							Log.v("tcpclient", "after wait for wifi connected.");
						}
						
						//Thread.sleep(5*1000);
					}
				} catch (Exception io) {
					io.printStackTrace();
					Log.v("tcpclient", "exception failed reconnect to ip:" + current_ip);
					long current = System.currentTimeMillis();
					if (connect_state == Constants.CONNECTING && current - start_connecting_timestamp > Constants.CONNECT_TIMEOUT) {
						//connect_state = Constants.DISCONNECTED;
						notify_connect_state(Constants.DISCONNECTED);
					}
					
					try {
						synchronized (TCPClient.getInstance()) {
							connect_state_reason = Constants.RECONNECTING_WAIT;
							TCPClient.getInstance().wait(5*1000);
							Log.v("tcpclient", "after wait for wifi connected.");
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} // try
			}// while (!is_stop)
		}
	} // clientThead
	
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
	                if (TCPClient.getInstance().connect_state != Constants.CONNECTED) {
	                	Log.e("tcpclient", "socket disconnected!");
	                	break;
	                }
	                // 返回负数表示，现在没有那么多数据，所以等待一会
	                Thread.sleep(100);
					continue;
	            }
            } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("tcpclient", "read_n BufferedInputStream read error! ");
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
	
	long last_resp_timestamp = 0;
	
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
			
			last_resp_timestamp = System.currentTimeMillis();
			
			ds = DeviceState.getInstance();
			ds.working_state = bs[16];
			
			synchronized (this) {
				ds.time = this.gotShort(bs, 20);
				ds.dishid = this.gotShort(bs, 24);
				int tmp = ds.time;
				//机器状态： 0，正在做菜；1暂停；2待机
				Log.e("tcpclient", "Machine_Stat = " + bs[15] + ",time = " + tmp + "bs[20]=" + (bs[22] & 0x00ff) + "bs[21]=" + (bs[23] & 0x00ff) + ", dishid=" + (ds.dishid&0xffff));
				ds.working_state = bs[15];
				ds.temp = bs[22];
				ds.jiaoban_speed = bs[23];
			}
			ds.is_pot_in = bs[26];
			ds.is_unlock = bs[27];
			ds.device_id = this.gotInt(bs, 28);
			ds.use_sound = bs[32];
			ds.use_english = bs[33];
			ds.temp_set = bs[34] & 0xff;
			ds.zhuliao_time_set = this.gotShort(bs, 35);
			ds.fuliao_time_set = this.gotShort(bs, 37);
			//Log.v("tcpclient", "zhuliao_time_set = " + ds.zhuliao_time_set + ", fuliao_time_set=" + ds.fuliao_time_set);
			
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
			Log.v("tcpclient", "Get_Favorite_Resp package");
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
					
					short[] cook_times = new short[12];
					String cook_times_str = "";
					for (int i = 0; i < cook_times.length; ++i) {
						cook_times[i] = gotShort(bs, start_pos);
						start_pos += 2;
						cook_times_str += cook_times[i] + ",";
					}
					Log.v("tcpclient", "cook_times_str = " + cook_times_str);
				}
			}
			
			String builtin_dishid = "builtin_dishid : ";
			for (int i = 0; i < ds.builtin_dishids.length; ++i) {
				builtin_dishid += ds.builtin_dishids[i] + ", ";
			}
			Arrays.sort(ds.builtin_dishids);
			ds.got_builtin = true;
			Log.v("tcpclient", "builtin_dishid : " + builtin_dishid);
			
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
			// 前两个字节为整个package的长度
			byte[] bytes_length = new byte[2];
			if (read_n(bis, 2, bytes_length)) {
				baos.write(bytes_length);
				
				int left_length = (short)(((bytes_length[1] << 8) & 0xff) | (bytes_length[0])) - 2;
				if (left_length <= 12 || left_length > 2000) {
					Log.e("tcpclient", "invalid package length = " + (left_length+2) + ", byte[0]=" + (bytes_length[0] & 0xff)  + ", byte[1]=" + (bytes_length[1] & 0xff));
					return false;// just skip
					// TODO fix bug： 应该只忽略一个字节的，现在是忽略了两个字节
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
			Log.e("tcpclient", "read_package BufferedInputStream read error!");
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("tcpclient", "read_package exception!");
			
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

