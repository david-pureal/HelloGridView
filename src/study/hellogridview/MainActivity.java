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

		// ���ô�Ų໬���������Ĳ����ļ�
		setBehindContentView(R.layout.frame_menu);
		// ���໬����fragment����䵽�໬���������Ĳ����ļ���
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		//SampleListFragment fragment = new SampleListFragment();
		MenuFragment fragment = new MenuFragment();
		transaction.replace(R.id.menu_frame, fragment);
		transaction.commit();
		// ��ȡ��SlidingMenu����Ȼ������һЩ����������
		sm = getSlidingMenu();
		fragment.set_sm(sm);
		
		// ������Ӱ�Ŀ��
		sm.setShadowWidth(50);
		// ������Ӱ����ɫ
		sm.setShadowDrawable(R.drawable.shadow);
		// ���ò໬����ȫչ��֮�󣬾�������һ�ߵľ��룬��λpx�����õ�Խ�󣬲໬���Ŀ��ԽС
		sm.setBehindOffset(200);
		// ���ý���ĳ̶ȣ���Χ��0-1.0f,���õ�Խ�����ڲ໬���ջ�����ʱ����ɫ��Խ����1.0f��ʱ����ɫΪȫ��
		sm.setFadeDegree(0.5f);
		// ���ô���ģʽ������ѡ��ȫ�������������Ǳ�Ե�����������ǲ��ɻ���
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//TOUCHMODE_MARGIN
		
		//����actionBar�ܷ����໬���ƶ������û�У������ȥ��
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
                // �����Ϣ�������߳�  
                if (msg.what == Constants.MSG_ID_CONNECT_STATE) {   
                	Log.v("MainActivity", "got event MSG_ID_CONNECT_STATE = " + tcpclient.connect_state);
                	set_connect_state();
                }  
                else if (msg.what == 0x777) {
                	popWindow.dismiss();
                	hasTask = false;
                }
                else if (msg.what == 0x778) {
                	addGuideImage(); //�������ҳ
            		MyPreference.setIsGuided(MainActivity.this, MainActivity.this.getClass().getName());
                }
            }  
        };
		
        gridView2 = (GridView)findViewById(R.id.gridview2);
        
        // TODO Ѷ�������ϳ�
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
        // ����ǵ������Ų�����Ĳˣ�Ȼ�󷵻صģ���ô������toggle
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
        
        // ���Ų���
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
              	 map.put("icon", d.img_bmp); //���ͼ����Դ��ID 
              	 map.put("name", d.name_chinese);//�������ItemText 
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
               	 map.put("icon", d.img_bmp); //���ͼ����Դ��ID 
               	 map.put("name", d.name_chinese);//�������ItemText 
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
         //����GridViewԪ�ص���Ӧ  
 	     gridView2.setOnItemClickListener(new OnItemClickListener() {  
 	        @Override  
 	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
 	            //����������GridViewԪ�ص�λ��  
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
                	//addGuideImage(); //�������ҳ
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

    // �������ؼ���ֱ���˳�
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.v("MainActivity", "onKeyDown");

    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(isExit == false ) {
                isExit = true;
                
                // ��������
                //Toast.makeText(this, "�ٰ�һ���˳�����", Toast.LENGTH_SHORT).show();
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
            	int screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // ��Ļ�����أ��磺480px��  
            	int screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // ��Ļ�ߣ����أ�
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
    
    /**������onCreate�е��ã���������ͼƬ����Դid
     *���ڲ���xml�ĸ�Ԫ��������android:id="@id/my_content_view"
     * @param resId
     */
    protected void setGuideResId(int resId){
        this.guideResourceId=resId;
    }
    
    public static SpeechSynthesizer mTts;

	// Ĭ�Ϸ�����
	private String voicer="xiaoyan";
	// ��������
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// �������
	private int mPercentForBuffering = 0;
	// ���Ž���
	private int mPercentForPlaying = 0;
 
    public void init_tts() {
    	SpeechUtility.createUtility(this, SpeechConstant.APPID +"=55654fe9");
        //1.���� SpeechSynthesizer ����, �ڶ�������:���غϳ�ʱ�� InitListener
        //2.�ϳɲ�������,������ƴ�Ѷ��MSC API�ֲ�(Android)��SpeechSynthesizer ��
        //���÷�����(�������߷�����,�û��ɲμ� ��¼12.2
    	// ��ʼ���ϳɶ���
    	mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener); 
       
    }
    
    /**
	 * ��ʼ��������
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d("main", "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		Log.d("main", "��ʼ��ʧ��,������ code = " + code);
        	} else {
        		Log.d("main", "��ʼ���ɹ� code = " + code);
        		setParam();
        		//mTts.startSpeaking("�ƴ�Ѷ��,�������������ǵ�����", mTtsListener);
        		//mTts.startSpeaking("��ӻƹϡ���ը�����ס��ټ�ˮ�����ϡ����Ѽ��꣬������Ϣ��", mTtsListener);
        		
				// ��ʼ���ɹ���֮����Ե���startSpeaking����
        		// ע���еĿ�������onCreate�����д�����ϳɶ���֮�����Ͼ͵���startSpeaking���кϳɣ�
        		// ��ȷ�������ǽ�onCreate�е�startSpeaking������������
			}		
		}
	};
	
	public static void speak(String data) {
		mTts.startSpeaking(data, null);
	}
	
	/**
	 * �ϳɻص�������
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
			// �ϳɽ���
			mPercentForBuffering = percent;
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// ���Ž���
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
		// ��ղ���
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// ���ݺϳ�����������Ӧ����
		if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// �������ߺϳɷ�����
			mTts.setParameter(SpeechConstant.VOICE_NAME,voicer);
		}else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// ���ñ��غϳɷ����� voicerΪ�գ�Ĭ��ͨ������+����ָ�������ˡ�
			mTts.setParameter(SpeechConstant.VOICE_NAME,"");
		}
		//���úϳ�����
		mTts.setParameter(SpeechConstant.SPEED, "50");
		//���úϳ�����
		mTts.setParameter(SpeechConstant.PITCH, "50");
		//���úϳ�����
		mTts.setParameter(SpeechConstant.VOLUME, "50");
		//���ò�������Ƶ������
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
		
		// ���ò��źϳ���Ƶ������ֲ��ţ�Ĭ��Ϊtrue
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		
		// ���úϳ���Ƶ����·��������·��Ϊsd����ע��WRITE_EXTERNAL_STORAGEȨ��
		mTts.setParameter(SpeechConstant.PARAMS,"tts_audio_path="+Environment.getExternalStorageDirectory()+"/test.pcm");
	}
}
