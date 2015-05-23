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
	
	boolean start_by_upload = false; //�Ƿ������ϴ����Զ������ģ�����ǣ��ڵ�¼�ɹ���Ҫ�Զ�����������
	boolean start_by_favorite = false;
	
	//static Handler handler;
	public Handler handler;
	
	private static String APPKEY = "7935d9c84c08";
	private static String APPSECRET = "7ba954bf0bc2d9cf63b6af111937ebad";

	
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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.activity_login);
		
		Log.v("LoginActivity", "onCreate(), tid=" + Thread.currentThread().getId());
		
		// ��ʼ��ui
		wechat_login = (TextView) findViewById(R.id.tvWeixin);
		// onclick is conflict with onTouch, cause slide-return not work.
//		wechat_login.setOnClickListener(new OnClickListener() {  
//	        @Override  
//	        public void onClick(View v) {  
//	        	Log.v("LoginActivity", "before wechat login");
//	        	if (!Account.is_login) {
//		        	// ΢�ŵ�½
//		        	Platform wechat= ShareSDK.getPlatform(LoginActivity.this, Wechat.NAME);
//		        	wechat.SSOSetting(false);  //����false��ʾʹ��SSO��Ȩ��ʽ
//		        	if(wechat.isValid()) { // ж�����°�װ��ʧЧ
//		        		setCurrentUser(wechat);
//						return;
//		        	}
//		        	wechat.setPlatformActionListener(LoginActivity.this);
//		        	wechat.authorize();
//		        	
//		        	wechat_login.setText("��¼�С�����");
//	        	}
//	        }
//		});
		
		login_usericon = (ImageView) findViewById(R.id.login_usericon);
		login_username = (TextView) findViewById(R.id.login_username);
		if (Account.is_login) {
			if (Account.user_icon_img != null) login_usericon.setImageBitmap(Account.user_icon_img);
    		login_username.setText(Account.username);
    		wechat_login.setText("΢���ѵ�¼");
		}
		
		handler = new Handler() {  
	        @Override  
	        public void handleMessage(Message msg) {  
	            // �����Ϣ�������߳�  
	            if (msg.what == Constants.MSG_ID_DOWNLOAD_ICON) {  
	            	Log.v("LoginActivity", "got msg MSG_ID_DOWNLOAD_ICON");
	        		login_usericon.setImageBitmap(Account.user_icon_img);
	        		login_username.setText(Account.username);
	        		wechat_login.setText("΢���ѵ�¼");
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
            	if (info_makesure.getText().toString().equals("�޸�")) {
            		info_name.setText(Account.info_name);
            		info_nickname.setText(Account.info_nickname);
            		info_address.setText(Account.info_address);
            		info_phone.setText(Account.info_phone);
            		info_makesure.setText("ȷ��");
            		
            		boolean editable = !info_makesure.getText().toString().equals("�޸�");
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
	            	if (name.isEmpty()) {info_status.setText("��������Ϊ��");return;}
	            	String nickname = info_nickname.getText().toString();
	            	if (nickname.isEmpty()) {info_status.setText("�ǳƲ���Ϊ��");return;}
	            	String address = info_address.getText().toString();
	            	if (address.isEmpty()) {info_status.setText("��ַ����Ϊ��");return;}
	            	String phone = info_phone.getText().toString();
	            	if (phone.isEmpty() || phone.length() != 11) {info_status.setText("������11λ�ֻ���");return;}
	            	
	            	Account.set_info(name, nickname, address, phone);
	            	init_param();
            	}
            }  
        });
		
		// ������֤
		this.initSDK();
		login_sms = (TextView) findViewById(R.id.login_sms);
		login_sms.setOnClickListener(new OnClickListener() {  
	         @Override  
	         public void onClick(View v) {  
	         	Log.v("LoginActivity", "login_sms onclick");
	         	if (Account.is_login) {
	         		Toast.makeText(LoginActivity.this, "�Ѿ���¼��", Toast.LENGTH_SHORT).show();
	         		return;
	         	}
	         	RegisterPage registerPage = new RegisterPage();
	 			registerPage.setRegisterCallback(new EventHandler() {
	 				public void afterEvent(int event, int result, Object data) {
	 					// ����ע����
	 					if (result == SMSSDK.RESULT_COMPLETE) {
	 						@SuppressWarnings("unchecked")
	 						HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
	 						String country = (String) phoneMap.get("country");
	 						String phone = (String) phoneMap.get("phone");
	 						Log.v("LoginActivity", "phone=" + phone + ", country=" + country);
	 						Account.is_login = true;
	 						Account.phone = phone;
	 						Account.register(LoginActivity.this);
	 						
	 						// �ύ�û���Ϣ
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
		wechat_login.setText("��΢�ŵ�¼");
	}

	@Override
	public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
		Log.v("LoginActivity", "onComplete, tid=" + Thread.currentThread().getId());
		setCurrentUser(arg0);
		//userId=oTyObs9trPUn836t-Lu6Rgeq0MAY, userName=�³�, 
		//userIcon=http://wx.qlogo.cn/mmopen/icqD4B2F1hHnVRNALbQQtTPYsRbIBqsiabxZJTxHfJDgNzodJ1SdzjjV214vICgSUe5sHOGP33NbPicVE5PVNyCAAGibZO1ptIoZ/0

	}

	@Override
	public void onError(Platform arg0, int arg1, Throwable arg2) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getName(), "onError");
		wechat_login.setText("��΢�ŵ�¼");
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
        	// ΢�ŵ�½
        	Platform wechat= ShareSDK.getPlatform(LoginActivity.this, Wechat.NAME);
        	wechat.SSOSetting(false);  //����false��ʾʹ��SSO��Ȩ��ʽ
        	if(wechat.isValid()) { // ж�����°�װ��ʧЧ
        		setCurrentUser(wechat);
				return;
        	}
        	wechat.setPlatformActionListener(LoginActivity.this);
        	wechat.authorize();
        	
        	wechat_login.setText("��¼��...");
    	}
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

    public void init_param() {
    	if (Account.info_name.isEmpty()) {
    		info_status.setText("��δע�ᣬ���ܻ�Ӱ��ʹ��");
    	}
    	else {
    		info_name.setText(Account.info_name.substring(0, 1) + "**");
    		info_nickname.setText(Account.info_nickname);
    		String address = Account.info_address;
    		if (Account.info_address.length() > 10) address = Account.info_address.substring(0, 10) + "**";
    		info_address.setText(address);
    		info_phone.setText(Account.info_phone.substring(0, 7) + "****");
    		info_status.setText("���Ѱ�������Ϣע�ᣬлл��");
    		info_makesure.setText("�޸�");
    		
    		boolean editable = !info_makesure.getText().toString().equals("�޸�");
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
		// ��ʼ������SDK
		SMSSDK.initSDK(this, APPKEY, APPSECRET);
		EventHandler eventHandler = new EventHandler() {
			public void afterEvent(int event, int result, Object data) {
				Log.v("LoginActivity", "afterEvent");
			}
		};
		// ע��ص������ӿ�
		SMSSDK.registerEventHandler(eventHandler);
	}

}
