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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

public class LoginActivity extends Activity implements PlatformActionListener, OnTouchListener{
	
	TextView wechat_login;
	ImageView login_usericon;
	TextView login_username;
	TextView login_header;
	
	boolean start_by_upload = false; //�Ƿ������ϴ����Զ������ģ�����ǣ��ڵ�¼�ɹ���Ҫ�Զ�����������
	boolean start_by_favorite = false;
	
	//static Handler handler;
	public Handler handler;
	
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
	}
	
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
	
//	public static void SendMsg(Message m) {
//		handler.sendMessage(m);
//	}

}
