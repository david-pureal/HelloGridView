package study.hellogridview;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.NetworkInterface;

import org.apache.http.Header;

import com.example.smartlinklib.ModuleInfo;
import com.example.smartlinklib.SmartLinkManipulator;
import com.example.smartlinklib.SmartLinkManipulator.ConnectCallBack;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingActivity extends Activity implements OnTouchListener {
	TextView ssid;
	Button m_startBtn;
	Button button_http;
	EditText pswd;
	SmartLinkManipulator sm;
	boolean isconncting = false;
	
	TextView smartlink_tv;
	
	public Switch swt_english;
	public Switch swt_sound;
	
	public byte use_english = 0;
	public byte use_sound = 0;
	public byte option_id = 1; // 1为语音提示；2为使用英文
	public byte opr = 0; //用户做了开启还是关闭的操作，0为关闭，1为开启
	
	public Button verify_dish;
	
	ImageButton m_deviceBtn;
	ImageButton m_stateBtn;
	ProgressBar connect_bar;
	
	TCPClient tcpclient;
	
	//手指向右滑动时的最小速度  
    private static final int XSPEED_MIN = 200;  
    //手指向右滑动时的最小距离  
    private static final int XDISTANCE_MIN = 150;  
    //记录手指按下时的横坐标。  
    private float xDown;  
    //记录手指移动时的横坐标。  
    private float xMove;  
    //用于计算手指滑动的速度。  
    private VelocityTracker mVelocityTracker; 
    
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				m_startBtn.setText("停止链接");
				break;
			case 2:
				m_startBtn.setText("开始链接");
				break;
			case 0x123:
			 {   
             	RespPackage rp = (RespPackage) msg.obj;
             	Log.v("SettingActivity", "SettingActivity got resp, cmdtype_head=" + (rp.cmdtype_head&0xff) + ", cmdtype_body=" + (rp.cmdtype_body&0xff));
             	if (rp.cmdtype_head == Package.ACK && rp.cmdtype_body == Package.Set_Option && !rp.is_ok) {
             		SettingActivity.this.tell("Set_Option fail!");
             	}
             	else if (rp.cmdtype_head == Package.Machine_State) {
             		SettingActivity.this.OnResp(rp);
             	}
             	Log.v("SettingActivity", "SettingActivity got event");
             }  
			case Constants.MSG_ID_CONNECT_STATE:
			{
				Log.v("SettingActivity", "got event MSG_ID_CONNECT_STATE = " + tcpclient.connect_state);
            	set_connect_state();
			}
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		setContentView(R.layout.activity_setting);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
		tcpclient = TCPClient.getInstance();
		tcpclient.set_settingact(this);
		
		m_startBtn = (Button) findViewById(R.id.startcook);
		ssid = (TextView) findViewById(R.id.ssid);
		ssid.setText(Tool.getInstance().getSSid(this));
		pswd = (EditText) findViewById(R.id.pswd);
		
		swt_english = (Switch) findViewById(R.id.switch_english);
		swt_english.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_english = opr;
            	option_id = 2;
            	
            	SettingActivity.this.do_send_msg();    
            }  
        }); 
		
		swt_sound = (Switch) findViewById(R.id.switch_sound);
		swt_sound.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_sound = opr;
            	option_id = 1;
            	
            	SettingActivity.this.do_send_msg();
            }  
        }); 
		
		verify_dish = (Button) findViewById(R.id.verify_dish);
		// oTyObs9trPUn836t-Lu6Rgeq0MAY userName=陈辰
		verify_dish.setVisibility(Account.is_login && Account.userid.equals("oTyObs9trPUn836t-Lu6Rgeq0MAY") ? View.VISIBLE : View.GONE);
		verify_dish.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	Intent intent = new Intent(SettingActivity.this, BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("菜谱审核")); 
                startActivity(intent);  
                //finish();//关闭当前Activity  
            }  
        });
		
		button_http = (Button) findViewById(R.id.button_http);
		button_http.setVisibility(View.GONE);
		button_http.setText("WiFI设置");
		button_http.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//String url_upload = "http://182.92.231.24:8889/upload";
				
//				WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//				WifiInfo info = wifi.getConnectionInfo();
//				NetworkInterface.getHardwareAddress();
//				Toast.makeText(SmartLinkActivity.this, "MAC=" + info.getMacAddress(), Toast.LENGTH_SHORT).show();
				
	            //Toast.makeText(SmartLinkActivity.this, "deviceId=" + Account.device_id, Toast.LENGTH_SHORT).show();
				
				startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 1);
				finish();
			}
		});
		//获取实例
//		sm = SmartLinkManipulator.getInstence(MainActivity.this);
		
		m_startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isconncting){
					isconncting = true;
					sm = SmartLinkManipulator.getInstence(SettingActivity.this);
					//不管在不在发送 先停止发送  释放所有缓存
//					sm.StopConnection();
					
					//再次获取实例 加载需要的信息
//					sm = SmartLinkManipulator.getInstence(MainActivity.this);
					
					String ss = Tool.getInstance().getSSid(SettingActivity.this);
					Log.v("smartlink", "ssid=" + ss);
					String ps = pswd.getText().toString().trim();
					handler.sendEmptyMessage(1);
					
					//设置要配置的ssid 和pswd
					sm.setConnection(ss, ps);
					//开始 smartLink
					sm.Startconnection(callback);
				}else{
					sm.StopConnection();
					handler.sendEmptyMessage(2);
					isconncting = false;
				}
			}
		});
		
		m_deviceBtn = (ImageButton) findViewById(R.id.right);
		m_deviceBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));  
                //finish();//关闭当前Activity  
            }  
        });
		m_stateBtn = (ImageButton) findViewById(R.id.left);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(SettingActivity.this, CurStateActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		connect_bar = (ProgressBar) findViewById(R.id.connecting_bar);
		
		smartlink_tv = (TextView) findViewById(R.id.smartlink_tv);
		set_connect_state();
		
		LinearLayout dish_sl = (LinearLayout) findViewById(R.id.layout_setting);  
		dish_sl.setOnTouchListener(this);
	}
	
	public boolean do_send_msg() {
		Message msg = new Message();  
        msg.what = 0x345;  
        
        Package data = new Package(Package.Set_Option);
        msg.obj = data.getBytes();
        TCPClient.getInstance().sendMsg(msg); 
        
        return true;
	}
	
	public void OnResp(RespPackage rp) {
		DeviceState ds = DeviceState.getInstance();
		swt_sound.setChecked(ds.use_sound == 0x01);
		swt_english.setChecked(ds.use_english == 0x01);
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	public void tell(final String info) {
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, info, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override  
    public boolean onTouch(View v, MotionEvent event) {  
        createVelocityTracker(event);  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            xDown = event.getRawX();  
            break;  
        case MotionEvent.ACTION_MOVE:  
            xMove = event.getRawX();  
            //活动的距离  
            int distanceX = (int) (xMove - xDown);  
            //获取顺时速度  
            int xSpeed = getScrollVelocity();  
            //当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity  
            if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN) {  
                finish();  
            }  
            break;  
        case MotionEvent.ACTION_UP:  
            recycleVelocityTracker();  
            break;  
        default:  
            break;  
        }  
        return true;  
    }  
      
    /** 
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。 
     *  
     * @param event 
     *         
     */  
    private void createVelocityTracker(MotionEvent event) {  
        if (mVelocityTracker == null) {  
            mVelocityTracker = VelocityTracker.obtain();  
        }  
        mVelocityTracker.addMovement(event);  
    }  
      
    /** 
     * 回收VelocityTracker对象。 
     */  
    private void recycleVelocityTracker() {  
        mVelocityTracker.recycle();  
        mVelocityTracker = null;  
    }  
      
    /** 
     * 获取手指在content界面滑动的速度。 
     *  
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。 
     */  
    private int getScrollVelocity() {  
        mVelocityTracker.computeCurrentVelocity(1000);  
        int velocity = (int) mVelocityTracker.getXVelocity();  
        return Math.abs(velocity);  
    }  
    
	ConnectCallBack callback = new ConnectCallBack() {
		@Override
		public void onConnectTimeOut() {
			// TODO Auto-generated method stub
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, "配置超时", Toast.LENGTH_SHORT).show();
					m_startBtn.setText("开始链接");
					isconncting = false;
				}
			});
		}
		
		@Override
		public void onConnect(final ModuleInfo mi) {
			// TODO Auto-generated method stub
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, 
							"发现设备  "+mi.getMid()+"mac"+ mi.getMac()+"IP"+mi.getModuleIP(), 
							Toast.LENGTH_SHORT).show();
					TCPClient.getInstance().connect_ip_sta(mi.getModuleIP());
				}
			});
		}
		@Override
		public void onConnectOk() {
			// TODO Auto-generated method stub
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, "配置完成", Toast.LENGTH_SHORT).show();
					m_startBtn.setText("开始链接");
					isconncting = false;
				}
			});
		}
	};
	
	public void set_connect_state() {
		
		ssid.setText(Tool.getInstance().getSSid(this));
		
    	if (tcpclient.connect_state == Constants.CONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		//m_deviceBtn.setImageResource(R.drawable.connected_32);
    		m_deviceBtn.setImageResource(R.drawable.correct_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    		
    		String ssid = Tool.getInstance().getSSid(this);
			if (ssid.startsWith(Constants.AP_NAME_PREFIX)) {
				smartlink_tv.setText("已通过直连模式与机器相连");
			} else {
				smartlink_tv.setText("已通过WiFi( " + ssid + " )与机器相连");
			}
    	}
    	else if (tcpclient.connect_state == Constants.DISCONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		m_deviceBtn.setImageResource(R.drawable.wrong_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    		
    		if (!Tool.getInstance().isWifiConnected(this)) {
    			smartlink_tv.setText("当前没有连接到任何WiFi，请先连接到WiFi");
    		}
    	}
    	else if (tcpclient.connect_state == Constants.CONNECTING) {
    		connect_bar.setVisibility(View.VISIBLE);
    		m_deviceBtn.setVisibility(View.GONE);
    		
    		smartlink_tv.setText("尝试连接中...");
    	}
    }
}

