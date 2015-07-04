package study.hellogridview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.ShareSDK;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends SlidingFragmentActivity {

	TCPClient tcpclient;
	
	ImageButton m_deviceBtn;
	ImageButton m_stateBtn;
	ProgressBar connect_bar;
	
	
	static MainActivity instance;
      
    public SlidingMenu sm;
    
    Handler handler;
    
    ArrayList<Integer> index_id_list = new ArrayList<Integer>();
    
    GridView gridView2;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		instance = this;
		
		Log.v("MainActivity", "onCreate");
		
		try {
			ShareSDK.initSDK(this);
		}
		catch (Exception e) {
			Log.v("MainActivity", "onCreate ShareSDK init exception, this = " + this);
			e.printStackTrace();
		}
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		//setContentView(R.layout.index);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
		FrameLayout layout_main = (FrameLayout) findViewById(R.id.main_framelayout);
		layout_main.setBackground(new BitmapDrawable(this.getResources(), Tool.get_res_bitmap(R.drawable.bkg)));

		// 设置存放侧滑栏的容器的布局文件
		setBehindContentView(R.layout.frame_menu);
		// 将侧滑栏的fragment类填充到侧滑栏的容器的布局文件中
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		//SampleListFragment fragment = new SampleListFragment();
		MenuFragment fragment = new MenuFragment();
		transaction.replace(R.id.menu_frame, fragment);
		transaction.commit();
		// 获取到SlidingMenu对象，然后设置一些常见的属性
		sm = getSlidingMenu();
		fragment.set_sm(sm);
		
		// 设置阴影的宽度
		sm.setShadowWidth(50);
		// 设置阴影的颜色
		sm.setShadowDrawable(R.drawable.shadow);
		// 设置侧滑栏完全展开之后，距离另外一边的距离，单位px，设置的越大，侧滑栏的宽度越小
		sm.setBehindOffset(200);
		// 设置渐变的程度，范围是0-1.0f,设置的越大，则在侧滑栏刚划出的时候，颜色就越暗。1.0f的时候，颜色为全黑
		sm.setFadeDegree(0.5f);
		// 设置触摸模式，可以选择全屏划出，或者是边缘划出，或者是不可划出
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//TOUCHMODE_MARGIN
		
		//设置actionBar能否跟随侧滑栏移动，如果没有，则可以去掉
		setSlidingActionBarEnabled(false);
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		self_content_view = inflater.inflate(R.layout.activity_main, null, false);
		
		m_deviceBtn = (ImageButton) findViewById(R.id.right);
		m_deviceBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	is_title_button_clicked = true;
            	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }  
        });
		m_stateBtn = (ImageButton) findViewById (R.id.left);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	is_title_button_clicked = true;
                startActivity(new Intent(MainActivity.this, CurStateActivity.class));  
            }  
        });
		connect_bar = (ProgressBar) findViewById(R.id.connecting_bar);
		connect_bar.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	is_title_button_clicked = true;
            	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));  
            }  
        });
		
		handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == Constants.MSG_ID_CONNECT_STATE) {   
                	Log.v("MainActivity", "got event MSG_ID_CONNECT_STATE = " + tcpclient.connect_state);
                	set_connect_state();
                }  
                else if (msg.what == 0x777) {
                	popWindow.dismiss();
                	hasTask = false;
                }
                else if (msg.what == 0x778) {
                	addGuideImage(); //添加引导页
            		MyPreference.setIsGuided(MainActivity.this, MainActivity.this.getClass().getName());
                }
            }  
        };
		
        gridView2 = (GridView)findViewById(R.id.gridview2);
        
        // TODO 讯飞语音合成
        init_tts();
	    
	}// onCreate
	
	public int cur = 0;
	
    public void set_connect_state() {
    	if (tcpclient.connect_state == Constants.CONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		//m_deviceBtn.setImageResource(R.drawable.connected_32);
    		m_deviceBtn.setImageResource(R.drawable.correct_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    	}
    	else if (tcpclient.connect_state == Constants.DISCONNECTED) {
    		connect_bar.setVisibility(View.GONE);
    		//m_deviceBtn.setImageResource(R.drawable.disconnected_32);
    		m_deviceBtn.setImageResource(R.drawable.wrong_32);
    		m_deviceBtn.setVisibility(View.VISIBLE);
    	}
    	else if (tcpclient.connect_state == Constants.CONNECTING) {
    		connect_bar.setVisibility(View.VISIBLE);
    		m_deviceBtn.setVisibility(View.GONE);
    	}
    }
    
    boolean is_element_clicked = false;
	private boolean is_title_button_clicked = false;
	
    @Override  
    protected void onResume() {  
    	Log.v("MainActivity", "onResume");
        super.onResume(); 
        // 如果是点了热门菜谱里的菜，然后返回的，那么不调用toggle
        if (!is_element_clicked && !is_title_button_clicked ) {
        	sm.toggle(false);
        }
        else {
        	is_element_clicked = false;
        	is_title_button_clicked = false;
        }
        
        tcpclient = TCPClient.getInstance(this);
        set_connect_state();
        
        Tool.getInstance().saveDevices(this);
        
        // 热门菜谱
 		LinkedHashMap<Integer, Dish> dishes = Dish.getAllDish();
        Log.v("MainActivity", "length = " + dishes.size());
         
        index_id_list.clear();
         
        ArrayList<HashMap<String,Object>> al=new ArrayList<HashMap<String, Object>>();
        int [] hotids = {30187,30188,30186};
        for (int i = 0; i < hotids.length; ++i)
        {
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	Dish d = Dish.getDishById(hotids[i]);
        	if (d == null) continue;
        	if (d.img_bmp == null) d.img_bmp = Tool.decode_res_bitmap(d.img, MainActivity.this, Constants.DECODE_DISH_IMG_SAMPLE);
            if (true/*d.isAppBuiltIn() || d.isVerifyDone()*/) {
              	 map.put("icon", d.img_bmp); //添加图像资源的ID 
              	 map.put("name", d.name_chinese);//按序号做ItemText 
                 al.add(map);
                 index_id_list.add(d.dishid);
                 if (index_id_list.size() == 12) break;
            }
        }
        for (Iterator<Integer> it = dishes.keySet().iterator();it.hasNext();)
        {
             int key = it.next();
             Dish d = dishes.get(key);
             HashMap<String, Object> map = new HashMap<String, Object>(); 
             
             if (d.img_bmp == null) d.img_bmp = Tool.decode_res_bitmap(d.img, MainActivity.this, Constants.DECODE_DISH_IMG_SAMPLE);
             if (d.isAppBuiltIn() /*|| d.isVerifyDone()*/) {
               	 map.put("icon", d.img_bmp); //添加图像资源的ID 
               	 map.put("name", d.name_chinese);//按序号做ItemText 
                 al.add(map);
                 index_id_list.add(d.dishid);
                 if (index_id_list.size() == 16) break;
             }
         }
         
         SimpleAdapter sa= new SimpleAdapter(MainActivity.this, al, R.layout.image_text,new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
         sa.setViewBinder(new ViewBinder(){  
             @Override  
             public boolean setViewValue(View view, Object data,  
                     String textRepresentation) {  
                 if( (view instanceof ImageView) && (data instanceof Bitmap ) ) {  
                     ImageView iv = (ImageView) view;  
                     Bitmap  bm = (Bitmap ) data;  
                     iv.setImageBitmap(bm);
                     return true;  
                 }  
                 return false;  
             }  
         });
         gridView2.setAdapter(sa);
         //单击GridView元素的响应  
 	     gridView2.setOnItemClickListener(new OnItemClickListener() {  
 	        @Override  
 	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
 	            //弹出单击的GridView元素的位置  
 	            //Toast.makeText(MainActivity.this, mThumbIds[position], Toast.LENGTH_SHORT).show(); 
 	        	Log.v("OnItemClickListener", "position = " + position + "id = " + id);
 	        	Dish dish = Dish.getDishById(index_id_list.get(position));
 	        	
 	        	Intent intent = new Intent(MainActivity.this, MakeDishActivityJ.class);
 	        	intent.putExtra("editable", false);
 	        	
 	        	intent.putExtra("dish_id", dish.dishid); 
 	        	startActivity(intent);	
 	        	is_element_clicked = true;
 	        }  
 	     });
 	     
 	    boolean is_guided = MyPreference.activityIsGuided(this, this.getClass().getName());
    	Log.v("MainActivity", "is_guided= " + is_guided);
    	if (!is_guided) {
    		Timer tExit = new Timer();
    		TimerTask task = new TimerTask() {
                @Override
                public void run() {
                	handler.sendEmptyMessage(0x778);
                	//addGuideImage(); //添加引导页
            		//MyPreference.setIsGuided(MainActivity.this, MainActivity.this.getClass().getName());
                }
            };
            tExit.schedule(task, 500);
    	}
 	     
    }
    
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        Log.v("MainActivity", "onDestroy");  
    }
    
    private static Boolean isExit = false;
    private static Boolean hasTask = false;
    Timer tExit = new Timer();
    TimerTask task;
    
    public LayoutInflater inflater;
	public View self_content_view;
	public View popupView;
	PopupWindow popWindow;

    // 长按返回键会直接退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.v("MainActivity", "onKeyDown");

    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(isExit == false ) {
                isExit = true;
                
                // 弹出浮层
                //Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                popupView = inflater.inflate(R.layout.makesure_exit, null, false);
            	popWindow = new PopupWindow(popupView, 700, 260, true);
            	
            	TextView makesure_exit_tv = (TextView) popupView.findViewById(R.id.makesure_exit_tv);
            	makesure_exit_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	System.exit(0);
                    }
                });
            	
            	//popWindow.setAnimationStyle(R.anim.in_from_left);
            	int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：480px）  
            	int screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，
            	//popWindow.showAtLocation(self_content_view, Gravity.NO_GRAVITY, (int)(screenWidth*0.33), (int)(screenHeight * 0.83));
            	popWindow.showAtLocation(self_content_view, Gravity.NO_GRAVITY, (int)(screenWidth*0.2), (int)(screenHeight * 0.72));
//            	Animation translateAnimation = AnimationUtils.loadAnimation(popupView.getContext(), R.anim.fade);
//            	popupView.startAnimation(translateAnimation);
            	
            	
                if(!hasTask) {
                	if (task != null) task.cancel();
                	task = new TimerTask() {
                        @Override
                        public void run() {
                            isExit = false;
                            hasTask = true;
                            //popWindow.dismiss();
                            //popupView.setVisibility(View.GONE);
                            handler.sendEmptyMessage(0x777);
                        }
                    };
                    tExit.schedule(task, 2000);
                }
            } else {
                //finish();
                System.exit(0);
            }
            return true;
        }
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    public Handler getHandler() {
    	return handler;
    }
    
    int guideResourceId = 1;
    
    public void addGuideImage() {
    	popupView = inflater.inflate(R.layout.guide, null, true);
        final PopupWindow popWindow = new PopupWindow(popupView, 1080, 1725, true);
        popupView.findViewById(R.id.main_guide).setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	popWindow.dismiss();
	        }
	    });
        popWindow.showAtLocation(self_content_view, Gravity.LEFT | Gravity.BOTTOM, 0, 0);
        Log.v("MainActivity", "guideImage add");
    }
    
    /**子类在onCreate中调用，设置引导图片的资源id
     *并在布局xml的根元素上设置android:id="@id/my_content_view"
     * @param resId
     */
    protected void setGuideResId(int resId){
        this.guideResourceId=resId;
    }
    
    public static SpeechSynthesizer mTts;

	// 默认发音人
	private String voicer="xiaoyan";
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// 缓冲进度
	private int mPercentForBuffering = 0;
	// 播放进度
	private int mPercentForPlaying = 0;
 
    public void init_tts() {
    	SpeechUtility.createUtility(this, SpeechConstant.APPID +"=55654fe9");
        //1.创建 SpeechSynthesizer 对象, 第二个参数:本地合成时传 InitListener
        //2.合成参数设置,详见《科大讯飞MSC API手册(Android)》SpeechSynthesizer 类
        //设置发音人(更多在线发音人,用户可参见 附录12.2
    	// 初始化合成对象
    	mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener); 
       
    }
    
    /**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d("main", "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		Log.d("main", "初始化失败,错误码 code = " + code);
        	} else {
        		Log.d("main", "初始化成功 code = " + code);
        		setParam();
        		//mTts.startSpeaking("科大讯飞,让世界聆听我们的声音", mTtsListener);
        		//mTts.startSpeaking("请加黄瓜、油炸花生米、再加水，调料。料已加完，可以休息了", mTtsListener);
        		
				// 初始化成功，之后可以调用startSpeaking方法
        		// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
        		// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}		
		}
	};
	
	public static void speak(String data) {
		mTts.startSpeaking(data, null);
	}
	
	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
		}

		@Override
		public void onSpeakPaused() {
		}

		@Override
		public void onSpeakResumed() {
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
			mPercentForBuffering = percent;
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
			mPercentForPlaying = percent;
		}

		@Override
		public void onCompleted(SpeechError error) {
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};
    
    private void setParam(){
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicer);
		}else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// 设置本地合成发音人 voicer为空，默认通过语音+界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME,"");
		}
		//设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, "50");
		//设置合成音调
		mTts.setParameter(SpeechConstant.PITCH, "50");
		//设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		//设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
		
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		
		// 设置合成音频保存路径，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		mTts.setParameter(SpeechConstant.PARAMS,"tts_audio_path="+Environment.getExternalStorageDirectory()+"/test.pcm");
	}
}
