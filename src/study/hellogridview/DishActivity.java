package study.hellogridview;

import java.io.ByteArrayOutputStream;

import android.view.LayoutInflater;
import android.graphics.drawable.BitmapDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class DishActivity extends Activity implements OnTouchListener, OnClickListener,PlatformActionListener,Callback{

	private static final int MSG_TOAST = 1;
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;

	private static final String FILE_NAME = "/share_pic.jpg";
	public static String TEST_IMAGE;
	
	Button startCook;
	Button share_to_wechat;
	ImageView dish_img;
	TextView dish_title;
	TextView dish_detail;
	
	Handler handler;
	TCPClient tcpclient;
	
	ImageButton m_deviceBtn;
	ImageButton m_stateBtn;
	ProgressBar connect_bar;
	
	int dish_id = 0;
	Dish dish;
	protected int resp_cmd108_count;
	
	//��ָ���һ���ʱ����С�ٶ�  
    private static final int XSPEED_MIN = 200;  
    //��ָ���һ���ʱ����С����  
    private static final int XDISTANCE_MIN = 200;  
    //��¼��ָ����ʱ�ĺ����ꡣ  
    private float xDown;  
    //��¼��ָ�ƶ�ʱ�ĺ����ꡣ  
    private float xMove;  
    //���ڼ�����ָ�������ٶȡ�  
    private VelocityTracker mVelocityTracker;  
    
    public ScrollView dish_layout;
	private float yDown;
	private float real_yDown;
	
	public Button dish_replace;
	
	public Integer[] dishids = new Integer[12];
	public final String dish_names[] = {"","","","","","","","","","","",""};
	protected int current_cmd;
	
	public ImageView favorite;
	
	ImageView material_1;
	ImageView material_2;
	ImageView material_3;
    
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_dish);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
		//ShareSDK.initSDK(this);
		
		Intent intent = getIntent();
		dish_id = intent.getIntExtra("dish_id", 1); 
		dish = Dish.getDishById(dish_id);
		dish_img = (ImageView) findViewById(R.id.dish_img); 
		Dish d = Dish.getDishById(dish_id);
		if (d.isAppBuiltIn()) dish_img.setImageResource(d.img);
		else {
			dish_img.setImageBitmap(d.img_bmp);
		}
		
		dish_detail = (TextView) findViewById(R.id.dish_detail); 
		dish_detail.setText(dish.text);
		
		dish_title = (TextView) findViewById(R.id.dish_title); 
		dish_title.setText(dish.name_chinese);
	
        handler = new Handler() {  
  
            @Override  
            public void handleMessage(Message msg) {  
                // �����Ϣ�������߳�  
                if (msg.what == 0x123) {    
                	RespPackage rp = (RespPackage) msg.obj;
                	Log.v("DishActivity", "got resp, cmdtype_head=" + (rp.cmdtype_head&0xff) + ", cmdtype_body=" + (rp.cmdtype_body&0xff));
                	if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 101) {
                		current_cmd = 101;
                	} else if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 104) {
                		current_cmd = 104;
                	} 
                	
                	if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 108) {
                		++ DishActivity.this.resp_cmd108_count;
                	}
                	
                	if (DishActivity.this.resp_cmd108_count == 5) { //ĿǰͼƬ���Ƿֳ�5��֡�����
                		DishActivity.this.resp_cmd108_count = 0;
                		if (current_cmd == 101) {
	                		Log.v("DishActivity", "resp_cmd108_count = " + resp_cmd108_count + " go to CurStateActivity");
	        	        	Intent intent = new Intent(DishActivity.this, CurStateActivity.class);
	        	        	intent.putExtra("dish_id", dish_id); 
	        	        	startActivity(intent);
                		} else if (current_cmd == 104) {
                			Toast.makeText(DishActivity.this, "�滻�������", Toast.LENGTH_SHORT).show();
                			TCPClient.getInstance().do_heartbeat();// ��ȡ�������ò���
                			Log.v("DishActivity", " replace done");
                    	}
                	}
                } // if (msg.what == 0x123) {   
                else if (msg.what == Constants.MSG_ID_FAVORITE_DONE) {
                	Log.v("DishActivity", "got favorite result, isFavorite ="  + Account.isFavorite(dish));
                	favorite.setImageResource(Account.isFavorite(dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72);
                }
                else if (msg.what == Constants.MSG_ID_CONNECT_STATE) {   
                	Log.v("DishActivity", "got event MSG_ID_CONNECT_STATE = " + tcpclient.connect_state);
                	set_connect_state();
                }
            }  
        }; 
        
        m_deviceBtn = (ImageButton) findViewById(R.id.right);
		m_deviceBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(DishActivity.this, SmartLinkActivity.class));  
                //finish();//�رյ�ǰActivity  
            }  
        });
		m_stateBtn = (ImageButton) findViewById(R.id.left);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(DishActivity.this, CurStateActivity.class));  
                //finish();//�رյ�ǰActivity  
            }  
        });
		connect_bar = (ProgressBar) findViewById(R.id.connecting_bar);
        
		startCook = (Button) findViewById(R.id.startcook);  
        startCook.setOnClickListener(new OnClickListener() {  
        	  
            @Override  
            public void onClick(View v) {  
                try {  
                    // ���û����°�ť֮�󣬽��û���������ݷ�װ��Message  
                    // Ȼ���͸����߳�Handler  
                    Message msg = new Message();  
                    msg.what = 0x345;  
                    //msg.obj = input.getText().toString();  
                    //msg.obj = "msg content";
                    Package data = new Package(Package.Send_Dish, dish);
                    msg.obj = data.getBytes();
                    tcpclient.sendMsg(msg); 
                    
                    DishActivity.this.resp_cmd108_count = 0;
                    
                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while(data.get_img_pkg(baos) && baos.size() != 0) {
                    	Log.v("DishActivity", "img baos.size() = " + baos.size());
                    	Message msgtmp = new Message();  
                    	msgtmp.what = 0x345; 
                    	msgtmp.obj = baos;
                    	tcpclient.sendMsg(msgtmp); 
                    	baos = new ByteArrayOutputStream();
                    }
                    
                    
                    // if use_sound then send sound
//                    if (DeviceState.getInstance().use_sound == 0x01) {
//                    	while(data.get_sound_pkg(baos) && baos.size() != 0) {
//                        	Log.v("DishActivity", "sound baos.size() = " + baos.size());
//                        	Message msgtmp = new Message();  
//                        	msgtmp.what = 0x345; 
//                        	msgtmp.obj = baos;
//                        	tcpClient.sendMsg(msgtmp); 
//                        	baos = new ByteArrayOutputStream();
//                        }
//                    } 
                } catch (Exception e) { 
                	e.printStackTrace();
                	Log.v("DishActivity", "prepare package data exception");
                }  
            }  
        }); 
        
        for (int i = 0; i < dishids.length; ++i) {
			dishids[i] = i;
		}
        
        dish_replace = (Button) findViewById(R.id.dish_replace);
        dish_replace.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	View popupView;
            	LayoutInflater inflater;
            	View self_content_view;
            	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        		self_content_view = inflater.inflate(R.layout.activity_make_dish, null, false);
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 500, 700, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	for (int i = 0; i < dishids.length; ++i) {
    				dishids[i] = 0xffff & DeviceState.getInstance().builtin_dishids[i];
    				dish_names[i] = Dish.getDishNameById(DeviceState.getInstance().builtin_dishids[i]);
    			}
            	if (DeviceState.getInstance().got_builtin == false) {
            		Toast.makeText(DishActivity.this, "�������ӻ�������ȡ���ò���", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	int id = dish.dishid;
            	if (Arrays.binarySearch(dishids, id) >= 0) {
            		Toast.makeText(DishActivity.this, "id(" + id + ")�Ѿ��ڻ���������", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	//ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivity.this, dishids);	
            	ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(DishActivity.this, dish_names);	
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(0);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	Message msg = new Message();  
	                    msg.what = 0x345;  
	                    Package data = new Package(Package.Update_Favorite, dish);
	                    data.set_replaced_id(dishids[column_1.getCurrentItem()]);
	                    msg.obj = data.getBytes();
	                    TCPClient.getInstance().sendMsg(msg); 
	                    
	                    DishActivity.this.resp_cmd108_count = 0;
	                    
	                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                    while(data.get_img_pkg(baos) && baos.size() != 0) {
	                    	Log.v("BuiltinDishes", "img baos.size() = " + baos.size());
	                    	Message msgtmp = new Message();  
	                    	msgtmp.what = 0x345; 
	                    	msgtmp.obj = baos;
	                    	TCPClient.getInstance().sendMsg(msgtmp); 
	                    	baos = new ByteArrayOutputStream();
	                    }
	                    Log.v("BuiltinDishes", "send replace req " + column_1.getCurrentItem() + " to " + dish.dishid + " done!");
                    	
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        favorite = (ImageView) findViewById(R.id.favorite);
        favorite.setImageResource(Account.isFavorite(dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72);
        favorite.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (!Account.is_login) {
            		Intent intent = new Intent(DishActivity.this, LoginActivity.class);
                	intent.putExtra("header", "��¼������ղ�");
                	startActivityForResult(intent, 9);
            	} else {
            		HttpUtils.favorite(dish, DishActivity.this.handler);
            	}
            }  
        });
        
        dish_layout = (ScrollView) findViewById(R.id.layout_dish);  
        dish_layout.setOnTouchListener(this); 
        
        share_to_wechat = (Button) findViewById(R.id.share_to_wechat);
        share_to_wechat.setOnClickListener(this);
        
        material_1 = (ImageView) findViewById(R.id.material_1); 
        material_2 = (ImageView) findViewById(R.id.material_2); 
        material_3 = (ImageView) findViewById(R.id.material_3); 
        if (dish.materials != null) {
        	if (dish.materials.size() > 0) material_1.setImageResource(dish.materials.get(0));
        	if (dish.materials.size() > 1) material_2.setImageResource(dish.materials.get(1));
        	if (dish.materials.size() > 2) material_3.setImageResource(dish.materials.get(2));
        }
        
        new Thread() {
			public void run() {
				initImagePath();
			}
		}.start();
	} // OnCreate
	
	//��ͼƬ��drawable���Ƶ�sdcard��
	//copy the picture from the drawable to sdcard
	private void initImagePath() {
		try {
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
					&& Environment.getExternalStorageDirectory().exists()) {
				TEST_IMAGE = Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_NAME;
			}
			else {
				TEST_IMAGE = getApplication().getFilesDir().getAbsolutePath() + FILE_NAME;
			}
			File file = new File(TEST_IMAGE);
			//if (!file.exists()) {
				file.createNewFile();
				//Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.pic);
				Bitmap pic = ((BitmapDrawable)dish_img.getDrawable()).getBitmap();
				FileOutputStream fos = new FileOutputStream(file);
				pic.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			//}
		} catch(Throwable t) {
			t.printStackTrace();
			TEST_IMAGE = null;
		}
	}
	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
        Log.v("DishActivity", "requestCode = " + requestCode + "resultCode =" + resultCode);
        switch (requestCode) {  
        case 9:  
        	if (Account.is_login) {
	    		 Log.v("DishActivity", "login return success, do favorite");
	    		 HttpUtils.favorite(dish, DishActivity.this.handler);
	    	}
            break;  
        }
	}
	
	@Override
	public void onClick(View v) {
		 OnekeyShare oks = new OnekeyShare();
		 //�ر�sso��Ȩ
		 oks.disableSSOWhenAuthorize(); 
		 
		 // ����ʱNotification��ͼ�������  2.5.9�Ժ�İ汾�����ô˷���
		 //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
		 // title���⣬ӡ��ʼǡ����䡢��Ϣ��΢�š���������QQ�ռ�ʹ��
		 oks.setTitle(getString(R.string.share));
		 // titleUrl�Ǳ�����������ӣ�������������QQ�ռ�ʹ��
		 oks.setTitleUrl("http://sharesdk.cn");
		 // text�Ƿ����ı�������ƽ̨����Ҫ����ֶ�
		 oks.setText("���Ƿ����ı�");
		 // imagePath��ͼƬ�ı���·����Linked-In�����ƽ̨��֧�ִ˲���
		 oks.setImagePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/share_pic.jpg");//ȷ��SDcard������ڴ���ͼƬ
		 // url����΢�ţ��������Ѻ�����Ȧ����ʹ��
		 oks.setUrl("http://sharesdk.cn");
		 // comment���Ҷ�������������ۣ�������������QQ�ռ�ʹ��
		 oks.setComment("���ǲ��������ı�");
		 // site�Ƿ�������ݵ���վ���ƣ�����QQ�ռ�ʹ��
		 oks.setSite(getString(R.string.app_name));
		 // siteUrl�Ƿ�������ݵ���վ��ַ������QQ�ռ�ʹ��
		 oks.setSiteUrl("http://sharesdk.cn");
		 
		 // ��������GUI
		 oks.show(DishActivity.this);
	}
		
	@Override  
    public boolean onTouch(View v, MotionEvent event) {  
        createVelocityTracker(event);  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
            xDown = event.getRawX();  
            yDown = event.getRawY();
            real_yDown = event.getRawY();
            break;  
        case MotionEvent.ACTION_MOVE:  
            xMove = event.getRawX(); 
            float yMove = event.getRawY();
            //��ľ���  
            int distanceX = (int) (xMove - xDown);  
            int real_distanceY = (int) (yMove - real_yDown);
            //��ȡ˳ʱ�ٶ�  
            int xSpeed = getScrollVelocity();  
            //�������ľ�����������趨����С�����һ�����˲���ٶȴ��������趨���ٶ�ʱ�����ص���һ��activity  
            if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN && Math.abs(distanceX) > Math.abs(real_distanceY)) {  
                finish();  
            }  
            else {
            	int distanceY = (int) (yDown - yMove);
            	dish_layout.smoothScrollBy(0, distanceY);
            	yDown = event.getRawY();
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
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		ShareSDK.stopSDK(this);
	}

	//���ü���http://sharesdk.cn/androidDoc/cn/sharesdk/framework/PlatformActionListener.html
	//���������̣߳�����Toast��Ҫ��handler������Ҫ����ô���Ĵ���
	//Setting listener, http://sharesdk.cn/androidDoc/cn/sharesdk/framework/PlatformActionListener.html
	//The listener is the child-thread that can not handle ui
	@Override
	public void onCancel(Platform platform, int action) {
		//ȡ������,handle the cancel msg
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void onComplete(Platform platform, int action, HashMap<String, Object> arg2) {
		//�ɹ�����,handle the successful msg
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void onError(Platform platform, int action, Throwable t) {
		//��ӡ������Ϣ,print the error msg
		t.printStackTrace();
		//�������,handle the error msg
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = t;
		UIHandler.sendMessage(msg, this);		
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case MSG_TOAST: {
			String text = String.valueOf(msg.obj);
			Toast.makeText(DishActivity.this, text, Toast.LENGTH_SHORT).show();
		}
		break;
		case MSG_ACTION_CCALLBACK: {
			switch (msg.arg1) {
				case 1: { // �ɹ���ʾ, successful notification
					showNotification(2000, getString(R.string.share_completed));
				}
				break;
				case 2: { // ʧ����ʾ, fail notification
					String expName = msg.obj.getClass().getSimpleName();
					if ("WechatClientNotExistException".equals(expName)
							|| "WechatTimelineNotSupportedException".equals(expName)) {
						showNotification(2000, getString(R.string.wechat_client_inavailable));
					}
					else if ("GooglePlusClientNotExistException".equals(expName)) {
						showNotification(2000, getString(R.string.google_plus_client_inavailable));
					}
					else if ("QQClientNotExistException".equals(expName)) {
						showNotification(2000, getString(R.string.qq_client_inavailable));
					}
					else {
						showNotification(2000, getString(R.string.share_failed));
					}
				}
				break;
				case 3: { // ȡ����ʾ, cancel notification
					showNotification(2000, getString(R.string.share_canceled));
				}
				break;
			}
		}
		break;
		case MSG_CANCEL_NOTIFY: {
			NotificationManager nm = (NotificationManager) msg.obj;
			if (nm != null) {
				nm.cancel(msg.arg1);
			}
		}
		break;
	}
		return false;
	}
	
	public void set_connect_state() {
    	if (tcpclient.connect_state == Constants.CONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		//m_deviceBtn.setImageResource(R.drawable.connected_32);
    		m_deviceBtn.setImageResource(R.drawable.correct_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    	}
    	else if (tcpclient.connect_state == Constants.DISCONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		m_deviceBtn.setImageResource(R.drawable.wrong_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    	}
    	else if (tcpclient.connect_state == Constants.CONNECTING) {
    		connect_bar.setVisibility(View.VISIBLE);
    		m_deviceBtn.setVisibility(View.GONE);
    	}
    }
	
	@Override  
    protected void onResume() {  
    	Log.v("DishActivity", "onResume");
        super.onResume(); 
        
		tcpclient = TCPClient.getInstance();
        tcpclient.set_dishact(this);
        set_connect_state();
    }

	// ��״̬����ʾ�������,the notification on the status bar
	private void showNotification(long cancelTime, String text) {
		try {
			Context app = getApplicationContext();
			NotificationManager nm = (NotificationManager) app
					.getSystemService(Context.NOTIFICATION_SERVICE);
			final int id = Integer.MAX_VALUE / 13 + 1;
			nm.cancel(id);

			long when = System.currentTimeMillis();
			Notification notification = new Notification(R.drawable.ic_launcher, text, when);
			PendingIntent pi = PendingIntent.getActivity(app, 0, new Intent(), 0);
			notification.setLatestEventInfo(app, "sharesdk test", text, pi);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(id, notification);

			if (cancelTime > 0) {
				Message msg = new Message();
				msg.what = MSG_CANCEL_NOTIFY;
				msg.obj = nm;
				msg.arg1 = id;
				UIHandler.sendMessageDelayed(msg, cancelTime, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    public Handler getHandler() {
    	return this.handler;
    }
}
