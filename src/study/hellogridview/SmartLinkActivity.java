package study.hellogridview;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SmartLinkActivity extends Activity implements OnTouchListener {
	TextView ssid;
	Button m_startBtn;
	Button button_http;
	EditText pswd;
	SmartLinkManipulator sm;
	boolean isconncting = false;
	
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
    
	Handler hand = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				m_startBtn.setText("ֹͣ����");
				break;
			case 2:
				m_startBtn.setText("��ʼ����");
				break;
			default:
				break;
			}
		};
	};
	
	ConnectCallBack callback = new ConnectCallBack() {
		
		@Override
		public void onConnectTimeOut() {
			// TODO Auto-generated method stub
			hand.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SmartLinkActivity.this, "���ó�ʱ", Toast.LENGTH_SHORT).show();
					m_startBtn.setText("��ʼ����");
					isconncting = false;
				}
			});
		}
		
		@Override
		public void onConnect(final ModuleInfo mi) {
			// TODO Auto-generated method stub
			hand.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SmartLinkActivity.this, 
							"�����豸  "+mi.getMid()+"mac"+ mi.getMac()+"IP"+mi.getModuleIP(), 
							Toast.LENGTH_SHORT).show();
					TCPClient.getInstance().connect_ip_sta(mi.getModuleIP());
				}
			});
		}

		@Override
		public void onConnectOk() {
			// TODO Auto-generated method stub
			hand.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SmartLinkActivity.this, "�������", Toast.LENGTH_SHORT).show();
					m_startBtn.setText("��ʼ����");
					isconncting = false;
				}
			});
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_smart_link);
		m_startBtn = (Button) findViewById(R.id.startcook);
		ssid = (TextView) findViewById(R.id.ssid);
		ssid.setText(Tool.getInstance().getSSid(this));
		pswd = (EditText) findViewById(R.id.pswd);
		
		button_http = (Button) findViewById(R.id.button_http);
		button_http.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//String url_upload = "http://182.92.231.24:8889/upload";
				
				String urlString = "http://182.92.231.24:8889/download?f=/home/david/python/data/tmp/london.jpg";
				RequestParams params = new RequestParams();
				HttpUtils.get(urlString, params, new FileAsyncHttpResponseHandler(SmartLinkActivity.this) {
				    @Override
				    public void onSuccess(int statusCode, Header[] headers, File file) {
				        // Do something with the file `response`
				    	FileInputStream inStream;
						try {
							inStream = new FileInputStream(file);
					    	byte[] img_data = new byte[inStream.available()];
							inStream.read(img_data);
							
							RequestParams params_tmp = new RequestParams();
							params_tmp.put("myfile", new ByteArrayInputStream(img_data), "london.jpg");
							params_tmp.put("path", "dish1000_xiqinchaorou");
							String url_upload = "http://182.92.231.24:8889/upload";
							HttpUtils.post(url_upload, params_tmp, new AsyncHttpResponseHandler() {
								@Override
								public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
									Log.v("smartlink", "onFailure");
									arg3.printStackTrace();
								}
								@Override
								public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
									// TODO Auto-generated method stub
									String res = new String(arg2);
									Log.v("smartlink", "res = " + res);
								}
					        });
							
							if (inStream != null) inStream.close();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }

					@Override
					public void onFailure(int arg0, Header[] arg1, Throwable arg2, File arg3) {
						Log.v("smartlink", "FileAsyncHttpResponseHandler onFailure arg0 = " + arg0);
						arg2.printStackTrace();
					}
				});
				
				
//				AssetFileDescriptor fd = null;
//				fd = SmartLinkActivity.this.getResources().openRawResourceFd(R.drawable.tudousi_tiny);
//				
//				try {
//					inStream = fd.createInputStream();
//					byte[] img_data = new byte[inStream.available()];
//					inStream.read(img_data);
//					
//					params.put("myfile", new ByteArrayInputStream(img_data), "tudousi.jpg");
//					HttpUtils.post(urlString, params, new AsyncHttpResponseHandler() {
//						@Override
//						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//							Log.v("smartlink", "onFailure");
//							arg3.printStackTrace();
//						}
//						@Override
//						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//							// TODO Auto-generated method stub
//							String res = new String(arg2);
//							Log.v("smartlink", "res = " + res);
//						}
//			        });
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		});
		//��ȡʵ��
//		sm = SmartLinkManipulator.getInstence(MainActivity.this);
		
		
		m_startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!isconncting){
					isconncting = true;
					sm = SmartLinkManipulator.getInstence(SmartLinkActivity.this);
					//�����ڲ��ڷ��� ��ֹͣ����  �ͷ����л���
//					sm.StopConnection();
					
					//�ٴλ�ȡʵ�� ������Ҫ����Ϣ
//					sm = SmartLinkManipulator.getInstence(MainActivity.this);
					
					String ss = Tool.getInstance().getSSid(SmartLinkActivity.this);
					Log.v("smartlink", "ssid=" + ss);
					String ps = pswd.getText().toString().trim();
					hand.sendEmptyMessage(1);
					
					//����Ҫ���õ�ssid ��pswd
					sm.setConnection(ss, ps);
					//��ʼ smartLink
					sm.Startconnection(callback);
				}else{
					sm.StopConnection();
					hand.sendEmptyMessage(2);
					isconncting = false;
				}
			}
		});
		
		LinearLayout dish_sl = (LinearLayout) findViewById(R.id.layout_smart_link);  
		dish_sl.setOnTouchListener(this);
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
}

