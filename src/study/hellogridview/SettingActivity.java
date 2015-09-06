package study.hellogridview;

import com.example.smartlinklib.ModuleInfo;
import com.example.smartlinklib.SmartLinkManipulator;
import com.example.smartlinklib.SmartLinkManipulator.ConnectCallBack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
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
	TextView wifi_tv;
	TextView one2one_tv;
	TextView one2one_step2_tv;
	
	public Switch swt_english;
	public Switch swt_sound;
	
	public byte use_english = 0;
	public byte use_sound = 0;
	public byte option_id = 1; // 1Ϊ������ʾ��2Ϊʹ��Ӣ��
	public byte opr = 0; //�û����˿������ǹرյĲ�����0Ϊ�رգ�1Ϊ����
	
	public Button verify_dish;
	
	ImageButton m_deviceBtn;
	ImageButton m_stateBtn;
	ProgressBar connect_bar;
	ProgressBar progressBar_smartlink;
	
	TCPClient tcpclient;
	LinearLayout wifi_step;
	LinearLayout one2one_step;
	
	//��ָ���һ���ʱ����С�ٶ�  
    private static final int XSPEED_MIN = 200;  
    //��ָ���һ���ʱ����С����  
    private static final int XDISTANCE_MIN = 150;  
    //��¼��ָ����ʱ�ĺ����ꡣ  
    private float xDown;  
    //��¼��ָ�ƶ�ʱ�ĺ����ꡣ  
    private float xMove;  
    //���ڼ�����ָ�������ٶȡ�  
    private VelocityTracker mVelocityTracker; 
    
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				m_startBtn.setText("������...");
				break;
			case 2:
				m_startBtn.setText("��ʼ����");
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
		
		wifi_step = (LinearLayout) findViewById(R.id.wifi_step);
		one2one_step = (LinearLayout) findViewById(R.id.one2one_step);
		//one2one_step.setVisibility(View.GONE);
		
		wifi_tv = (TextView) findViewById(R.id.wifi_tv);
		wifi_tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		wifi_tv.getPaint().setAntiAlias(true);
		wifi_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	return;
            }  
        });
		one2one_tv = (TextView) findViewById(R.id.one2one_tv);
		one2one_tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		one2one_tv.getPaint().setAntiAlias(true);
		one2one_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	one2one_step.setVisibility(View.VISIBLE);
            }  
        });
		
		one2one_step2_tv = (TextView) findViewById(R.id.one2one_step2_tv);
		one2one_step2_tv.setText("3�� ѡ��" + Constants.AP_NAME_PREFIX + "xxxx���Ӽ���");
		
		m_startBtn = (Button) findViewById(R.id.start_connect);
		ssid = (TextView) findViewById(R.id.ssid);
		ssid.setText("�����ӵ�WiFi : " + Tool.getInstance().getSSid(this));
		if (Tool.getInstance().getSSid(this).isEmpty()) {
			ssid.setText("δ���ӵ��κ�WiFi");
		}
			
		pswd = (EditText) findViewById(R.id.pswd);
		
		swt_english = (Switch) findViewById(R.id.switch_english);
		swt_english.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_english = opr;
            	option_id = 2;
            	
            	//�л��ֻ�APP����Ӣ��
            	//SettingActivity.this.do_send_msg();    
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
		// oTyObs9trPUn836t-Lu6Rgeq0MAY userName=�³�
		// oTyObs-ij5aWjDfGY5Agz2O1FAGI userName=���������������ܳ��˻���
		boolean is_operator = Account.userid.equals("oTyObs9trPUn836t-Lu6Rgeq0MAY") || Account.userid.equals("oTyObs-ij5aWjDfGY5Agz2O1FAGI");
		verify_dish.setVisibility(Account.is_login && is_operator ? View.VISIBLE : View.GONE);
		verify_dish.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	Intent intent = new Intent(SettingActivity.this, BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("�������")); 
                startActivity(intent);  
                //finish();//�رյ�ǰActivity  
            }  
        });
		
		m_startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isconncting){
					isconncting = true;
					sm = SmartLinkManipulator.getInstence(SettingActivity.this);
					
					String ss = Tool.getInstance().getSSid(SettingActivity.this);
					Log.v("smartlink", "ssid=" + ss);
					String ps = pswd.getText().toString().trim();
					handler.sendEmptyMessage(1);
					
					//����Ҫ���õ�ssid ��pswd
					sm.setConnection(ss, ps);
					//��ʼ smartLink
					sm.Startconnection(callback);
					progressBar_smartlink.setVisibility(View.VISIBLE);
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
            }  
        });
		m_stateBtn = (ImageButton) findViewById(R.id.left);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(SettingActivity.this, CurStateActivity.class));  
            }  
        });
		connect_bar = (ProgressBar) findViewById(R.id.connecting_bar);
		connect_bar.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));  
            }  
        });
//		TextView title_name = (TextView) findViewById (R.id.title_name);
//		title_name.setTypeface(Tool.typeFace);
		
		progressBar_smartlink = (ProgressBar) findViewById(R.id.progressBar_smartlink);
		progressBar_smartlink.setVisibility(View.GONE);
		
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
            //��ľ���  
            int distanceX = (int) (xMove - xDown);  
            //��ȡ˳ʱ�ٶ�  
            int xSpeed = getScrollVelocity();  
            //�������ľ�����������趨����С�����һ�����˲���ٶȴ��������趨���ٶ�ʱ�����ص���һ��activity  
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
     * ����VelocityTracker���󣬲�������content����Ļ����¼����뵽VelocityTracker���С� 
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
     * ����VelocityTracker���� 
     */  
    private void recycleVelocityTracker() {  
        mVelocityTracker.recycle();  
        mVelocityTracker = null;  
    }  
      
    /** 
     * ��ȡ��ָ��content���滬�����ٶȡ� 
     *  
     * @return �����ٶȣ���ÿ�����ƶ��˶�������ֵΪ��λ�� 
     */  
    private int getScrollVelocity() {  
        mVelocityTracker.computeCurrentVelocity(1000);  
        int velocity = (int) mVelocityTracker.getXVelocity();  
        return Math.abs(velocity);  
    }  
    
	ConnectCallBack callback = new ConnectCallBack() {
		@Override
		public void onConnectTimeOut() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, "���ó�ʱ", Toast.LENGTH_SHORT).show();
					m_startBtn.setText("��ʼ����");
					isconncting = false;
				}
			});
		}
		
		@Override
		public void onConnect(final ModuleInfo mi) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(SettingActivity.this, 
							"�����豸  "+mi.getMid()+"mac"+ mi.getMac()+"IP"+mi.getModuleIP(),
							Toast.LENGTH_SHORT).show();
					TCPClient.getInstance().connect_ip_sta(mi.getModuleIP());
				}
			});
		}
		@Override
		public void onConnectOk() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(SettingActivity.this, "�������", Toast.LENGTH_SHORT).show();
					m_startBtn.setText("��ʼ����");
					isconncting = false;
				}
			});
		}
	};
	
	public void set_connect_state() {
		
		ssid.setText("�����ӵ�WiFi : " + Tool.getInstance().getSSid(this));
		if (Tool.getInstance().getSSid(this).isEmpty()) {
			ssid.setText("δ���ӵ�WiFi,�����ֻ���·������������");
		}
		
		TextView device_id_tv = (TextView) findViewById(R.id.device_id_tv);
		
    	if (tcpclient.connect_state == Constants.CONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		//m_deviceBtn.setImageResource(R.drawable.connected_32);
    		m_deviceBtn.setImageResource(R.drawable.correct_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    		
    		m_startBtn.setText("���ӳɹ�");
    		progressBar_smartlink.setVisibility(View.GONE);
    		
    		String ssid = Tool.getInstance().getSSid(this);
			if (ssid.startsWith(Constants.AP_NAME_PREFIX)) {
				smartlink_tv.setText("��ͨ��ֱ��ģʽ���������");
			} else {
				smartlink_tv.setText("��ͨ��WiFi( " + ssid + " )���������");
			}
			
			device_id_tv.setVisibility(View.VISIBLE);
			String id_str = Integer.toHexString(DeviceState.getInstance().device_id);
			device_id_tv.setText("�豸��ţ�" + id_str);
    	}
    	else if (tcpclient.connect_state == Constants.DISCONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		m_deviceBtn.setImageResource(R.drawable.wrong_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    		
    		if (!Tool.getInstance().isWifiConnected(this)) {
    			smartlink_tv.setText("��ǰû�����ӵ��κ�WiFi���������ӵ�WiFi");
    		}
    	}
    	else if (tcpclient.connect_state == Constants.CONNECTING) {
    		connect_bar.setVisibility(View.VISIBLE);
    		m_deviceBtn.setVisibility(View.GONE);
    		
    		smartlink_tv.setText("����������...");
    	}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v("SettingActivity", "SettingActivity onResume");
		set_connect_state();
	}
	
}

