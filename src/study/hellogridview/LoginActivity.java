package study.hellogridview;

import java.util.HashMap;

import study.hellogridview.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.FakeActivity;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

public class LoginActivity extends Activity implements PlatformActionListener, OnTouchListener{
	
	TextView wechat_login;
	ImageView login_usericon;
	TextView login_username;
	TextView login_header;

	EditText info_name;
	EditText info_nickname;
	EditText info_address;
	EditText info_phone;
	TextView login_sms;
	Button info_makesure;
	TextView info_status;
	
	boolean start_by_upload = false; //是否是在上传是自动触发的，如果是，在登录成功后要自动结束并返回
	boolean start_by_favorite = false;
	
	//static Handler handler;
	public Handler handler;
	
	private static String APPKEY = "7935d9c84c08";
	private static String APPSECRET = "7ba954bf0bc2d9cf63b6af111937ebad";

	
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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.activity_login);
		
		Log.v("LoginActivity", "onCreate(), tid=" + Thread.currentThread().getId());
		
		// 初始化ui
		wechat_login = (TextView) findViewById(R.id.tvWeixin);
		// onclick is conflict with onTouch, cause slide-return not work.
//		wechat_login.setOnClickListener(new OnClickListener() {  
//	        @Override  
//	        public void onClick(View v) {  
//	        	Log.v("LoginActivity", "before wechat login");
//	        	if (!Account.is_login) {
//		        	// 微信登陆
//		        	Platform wechat= ShareSDK.getPlatform(LoginActivity.this, Wechat.NAME);
//		        	wechat.SSOSetting(false);  //设置false表示使用SSO授权方式
//		        	if(wechat.isValid()) { // 卸载重新安装后失效
//		        		setCurrentUser(wechat);
//						return;
//		        	}
//		        	wechat.setPlatformActionListener(LoginActivity.this);
//		        	wechat.authorize();
//		        	
//		        	wechat_login.setText("登录中。。。");
//	        	}
//	        }
//		});
		
		login_usericon = (ImageView) findViewById(R.id.login_usericon);
		login_username = (TextView) findViewById(R.id.login_username);
		if (Account.is_login) {
			if (Account.user_icon_img != null) login_usericon.setImageBitmap(Account.user_icon_img);
    		login_username.setText(Account.username);
    		wechat_login.setText("微信已登录");
		}
		
		handler = new Handler() {  
	        @Override  
	        public void handleMessage(Message msg) {  
	            // 如果消息来自子线程  
	            if (msg.what == Constants.MSG_ID_DOWNLOAD_ICON) {  
	            	Log.v("LoginActivity", "got msg MSG_ID_DOWNLOAD_ICON");
	        		login_usericon.setImageBitmap(Account.user_icon_img);
	        		login_username.setText(Account.username);
	        		wechat_login.setText("微信已登录");
	            } 
	            else if (msg.what == Constants.MSG_ID_REGISTER_DONE) {
	        		if (start_by_upload) {
	        			finish();
	        		}
	            }
	        }  
	    };
	    
	    RelativeLayout login_layout = (RelativeLayout) findViewById(R.id.layout_login);  
	    login_layout.setOnTouchListener(this);
	    wechat_login.setOnTouchListener(this);
	    
	    login_header = (TextView) findViewById(R.id.login_header);
	    login_header.setVisibility(View.GONE);
	    
	    Intent intent = getIntent();
	    if (intent.getStringExtra("header") != null) {
	    	login_header.setVisibility(View.VISIBLE);
	    	login_header.setText(intent.getStringExtra("header"));
	    	start_by_upload = true;
	    }
	    
	 
		info_name = (EditText) findViewById(R.id.info_name);
		info_nickname = (EditText) findViewById(R.id.info_nickname);
		info_address = (EditText) findViewById(R.id.info_address);
		info_phone = (EditText) findViewById(R.id.info_phone);
		info_status = (TextView) findViewById(R.id.info_status);
		 
		
		info_makesure = (Button) findViewById(R.id.info_makesure);
		info_makesure.setOnClickListener(new OnClickListener() {  
            @Override
            public void onClick(View v) {
            	if (info_makesure.getText().toString().equals("修改")) {
            		info_name.setText(Account.info_name);
            		info_nickname.setText(Account.info_nickname);
            		info_address.setText(Account.info_address);
            		info_phone.setText(Account.info_phone);
            		info_makesure.setText("确认");
            		
            		boolean editable = !info_makesure.getText().toString().equals("修改");
            		info_name.setEnabled(true);
            		info_nickname.setEnabled(editable);
            		info_address.setEnabled(editable);
            		info_phone.setEnabled(editable);
            		info_name.setFocusable(true);
            		info_name.setFocusableInTouchMode(true);
            		info_nickname.setFocusable(editable);
            		info_nickname.setFocusableInTouchMode(true);
            		info_address.setFocusable(editable);
            		info_address.setFocusableInTouchMode(true);
            		info_phone.setFocusable(editable);
            		info_phone.setFocusableInTouchMode(true);
            	}
            	else {
	            	String name = info_name.getText().toString();
	            	if (name.isEmpty()) {info_status.setText("姓名不能为空");return;}
	            	String nickname = info_nickname.getText().toString();
	            	if (nickname.isEmpty()) {info_status.setText("昵称不能为空");return;}
	            	String address = info_address.getText().toString();
	            	if (address.isEmpty()) {info_status.setText("地址不能为空");return;}
	            	String phone = info_phone.getText().toString();
	            	if (phone.isEmpty() || phone.length() != 11) {info_status.setText("请输入11位手机号");return;}
	            	
	            	Account.set_info(name, nickname, address, phone);
	            	init_param();
            	}
            }  
        });
		
		// 短信验证
		this.initSDK();
		login_sms = (TextView) findViewById(R.id.login_sms);
		login_sms.setOnClickListener(new OnClickListener() {  
	         @Override  
	         public void onClick(View v) {  
	         	Log.v("LoginActivity", "login_sms onclick");
	         	if (Account.is_login) {
	         		Toast.makeText(LoginActivity.this, "已经登录了", Toast.LENGTH_SHORT).show();
	         		return;
	         	}
	         	RegisterPage registerPage = new RegisterPage();
	 			registerPage.setRegisterCallback(new EventHandler() {
	 				public void afterEvent(int event, int result, Object data) {
	 					// 解析注册结果
	 					if (result == SMSSDK.RESULT_COMPLETE) {
	 						@SuppressWarnings("unchecked")
	 						HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
	 						String country = (String) phoneMap.get("country");
	 						String phone = (String) phoneMap.get("phone");
	 						Log.v("LoginActivity", "phone=" + phone + ", country=" + country);
	 						Account.is_login = true;
	 						Account.phone = phone;
	 						Account.register(LoginActivity.this);
	 						
	 						// 提交用户信息
	 						//registerUser(country, phone);
	 					}
	 				}
	 			});
	 			
	 			is_login_sms = true;
	 			registerPage.show(LoginActivity.this); 
	         }  
	    });
		
		init_param();
	} // OnCreate
	
	@Override
	public void onCancel(Platform arg0, int arg1) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getName(), "onCancel");
		wechat_login.setText("用微信登录");
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		Log.v("LoginActivity", "onComplete, tid=" + Thread.currentThread().getId());
		setCurrentUser(arg0);
		//userId=oTyObs9trPUn836t-Lu6Rgeq0MAY, userName=陈辰, 
		//userIcon=http://wx.qlogo.cn/mmopen/icqD4B2F1hHnVRNALbQQtTPYsRbIBqsiabxZJTxHfJDgNzodJ1SdzjjV214vICgSUe5sHOGP33NbPicVE5PVNyCAAGibZO1ptIoZ/0

	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getName(), "onError");
		wechat_login.setText("用微信登录");
	}
	
	private void setCurrentUser(Platform plat) {
		Account.is_login = true;
		Account.userid = plat.getDb().getUserId();
		Account.username = plat.getDb().getUserName();
		Account.setUserIcon(plat.getDb().getUserIcon(), this);
		Log.v("LoginActivity", "userId=" + Account.userid + ", userName=" + Account.username + ", userIcon=" + Account.user_icon_link);
	}

	boolean is_touch_down_on_wechat_login = false;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            xDown = event.getRawX();  
            if (v.getId() == R.id.tvWeixin) {
            	is_touch_down_on_wechat_login = true;
            }
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
            if (v.getId() == R.id.tvWeixin && is_touch_down_on_wechat_login) {
            	do_wechat_authorize();
            }
            break;  
        default:  
            break;  
        }  
        return true;  
	}
	
	private void do_wechat_authorize() {
		Log.v("LoginActivity", "before wechat login");
    	if (!Account.is_login) {
        	// 微信登陆
        	Platform wechat= ShareSDK.getPlatform(LoginActivity.this, Wechat.NAME);
        	wechat.SSOSetting(false);  //设置false表示使用SSO授权方式
        	if(wechat.isValid()) { // 卸载重新安装后失效
        		setCurrentUser(wechat);
				return;
        	}
        	wechat.setPlatformActionListener(LoginActivity.this);
        	wechat.authorize();
        	
        	wechat_login.setText("登录中...");
    	}
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

    public void init_param() {
    	if (Account.info_name.isEmpty()) {
    		info_status.setText("您未注册，可能会影响使用");
    	}
    	else {
    		info_name.setText(Account.info_name.substring(0, 1) + "**");
    		info_nickname.setText(Account.info_nickname);
    		String address = Account.info_address;
    		if (Account.info_address.length() > 10) address = Account.info_address.substring(0, 10) + "**";
    		info_address.setText(address);
    		info_phone.setText(Account.info_phone.substring(0, 7) + "****");
    		info_status.setText("您已按上面信息注册，谢谢！");
    		info_makesure.setText("修改");
    		
    		boolean editable = !info_makesure.getText().toString().equals("修改");
    		info_name.setFocusable(editable);
    		info_nickname.setFocusable(editable);
    		info_address.setFocusable(editable);
    		info_phone.setFocusable(editable);
    		info_name.setEnabled(editable);
    		info_nickname.setEnabled(editable);
    		info_address.setEnabled(editable);
    		info_phone.setEnabled(editable);
    	}
    }
    
    boolean is_login_sms = false;
    
    @Override
	protected void onResume() {
		super.onResume();
		Log.v("LoginActivity", "LoginActivity onResume");
		if (is_login_sms && !Account.phone.isEmpty()) {
			is_login_sms = false;
			login_username.setText(Account.phone);
			
		}
		
	}
    
	private void initSDK() {
		// 初始化短信SDK
		SMSSDK.initSDK(this, APPKEY, APPSECRET);
		EventHandler eventHandler = new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
				Log.v("LoginActivity", "afterEvent");
			}
		};
		// 注册回调监听接口
		SMSSDK.registerEventHandler(eventHandler);
	}

}
