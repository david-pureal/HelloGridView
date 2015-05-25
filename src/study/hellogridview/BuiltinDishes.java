package study.hellogridview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BuiltinDishes extends SlidingFragmentActivity {

	GridView gridView = null;	
	Button getBuiltin;
	Handler handler;
	protected String all_dish_str;

    public SlidingMenu sm;
    
    TextView tv;
	
	TCPClient tcpclient;
	
	ImageButton m_deviceBtn;
	ImageButton m_stateBtn;
	ProgressBar connect_bar;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("BuiltinDishes", "oncreate");
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		setContentView(R.layout.activity_builtin_dishes);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
		LinearLayout layout_builtin = (LinearLayout) findViewById(R.id.layout_builtin);
		layout_builtin.setBackground(new BitmapDrawable(this.getResources(), Tool.get_res_bitmap(R.drawable.bkg)));
		
		handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // �����Ϣ�������߳�  
                if (msg.what == 0x123) {   
                	Log.v("BuiltinDishes", "BuiltinDishes got event");
                	RespPackage rp = (RespPackage) msg.obj;
	                if (rp.cmdtype_head == Package.Get_Favorite_Resp) {
	                	if (rp.is_ok) {
	                		Log.v("BuiltinDishes", "Get_Favorite OK!");
	                	} else {
	                		Log.v("BuiltinDishes", "Get_Favorite failed!");
	                	}
                	
                	}
                } 
                else if (msg.what == Constants.MSG_ID_CONNECT_STATE) {   
                	Log.v("MainActivity", "got event MSG_ID_CONNECT_STATE = " + tcpclient.connect_state);
                	set_connect_state();
                } 
            }  
        };
        
        // ���ô�Ų໬���������Ĳ����ļ�
		setBehindContentView(R.layout.frame_menu);
		// ���໬����fragment����䵽�໬���������Ĳ����ļ���
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
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
        
		m_deviceBtn = (ImageButton) findViewById(R.id.right);
		m_deviceBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));  
                //finish();//�رյ�ǰActivity  
            }  
        });
		m_stateBtn = (ImageButton) findViewById(R.id.left);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(BuiltinDishes.this, CurStateActivity.class));  
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
		
		gridView = (GridView)findViewById(R.id.gridview);  
		tv = (TextView) findViewById(R.id.replace_builtin_tv); 
		Intent intent = getIntent();
		tv.setText(intent.getStringExtra("title"));
		
		if (intent.getStringExtra("title").equalsIgnoreCase("�Ա����")) {
			 Log.v("BuiltinDishes", "�Ա����");
			 //make_new_dish.setVisibility(View.GONE);
			 TextView make_new_dish2 = (TextView) findViewById(R.id.make_new_dish2);
			 make_new_dish2.setVisibility(View.VISIBLE);
			 make_new_dish2.setOnClickListener(new OnClickListener() {  
	              @Override  
	              public void onClick(View v) {  
	                  startActivity(new Intent(BuiltinDishes.this, InputDishNameActivity.class));  
	                  //finish();//�رյ�ǰActivity  
	              }  
	        });
		}
		else if (intent.getStringExtra("title").equalsIgnoreCase("�������")) {
			 Log.v("BuiltinDishes", "�������");
			 //make_new_dish.setVisibility(View.GONE);
		}
		else if (intent.getStringExtra("title").equals("�û�����")) {
			tv.setText("�����û�����Ĳ���");
			TextView note  = (TextView) findViewById(R.id.verify_note);
			note.setVisibility(View.VISIBLE);
		}
		
	} // oncreate
	
	protected void tell(boolean is_ok) {
		this.all_dish_str = "�������ò�����ţ� \n";
		DeviceState ds = DeviceState.getInstance();
		for (int i = 0; i < ds.builtin_dishids.length; ++i) {
			this.all_dish_str += String.valueOf(ds.builtin_dishids[i]);
			this.all_dish_str += ",";
		}
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BuiltinDishes.this, BuiltinDishes.this.all_dish_str, Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	public Handler getHandler() {
    	return this.handler;
    }
	
	ArrayList<Integer> index_id_list = new ArrayList<Integer>();
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v("BuiltinDishes", "Buildin onResume");
		
		tcpclient = TCPClient.getInstance();
		tcpclient.set_builtinact(this);
        set_connect_state();
        
        DeviceState ds = DeviceState.getInstance();
        String title = tv.getText().toString();
        index_id_list.clear();
        
		LinkedHashMap<Integer, Dish> dishes = Dish.getAllDish();
        Log.v("BuiltinDishes", "alldish length = " + dishes.size());
        
        ArrayList<HashMap<String, Object>> al=new ArrayList<HashMap<String, Object>>();
        for (Iterator<Integer> it = dishes.keySet().iterator();it.hasNext();)
        {
             int key = it.next();
             Dish d = dishes.get(key);
             HashMap<String, Object> map = new HashMap<String, Object>(); 
             if (title.equals(Constants.BUILTIN_CNAME)) {
            	 int pos = Arrays.binarySearch(ds.builtin_dishids, (short)key);
            	 if (pos < 0) {
            		 continue;
            	 }
             }
             else if (title.equals("�������") && !d.isVerifying()) {
            	 continue;
             }
             else if (title.equals("�Ա����")) {
            	 if (d.isAppBuiltIn()) continue;
            	 
            	 Log.v("BuiltinDishes", "d.dishid=" + d.dishid + ", d.author_id=" + d.author_id + ", d.author_name=" + d.author_name);
            	 if (!d.isMine()) {
            		 Log.v("BuiltinDishes", "dish not my dish.");
            		 continue;
            	 }
             }
             else if (title.equals("�ղز���")) {
            	 if (!Account.isFavorite(d)) continue;
             }
             else if (title.equals("�����û�����Ĳ���")) {
            	 if (d.isAppBuiltIn()) continue;
            	 if (d.hasNotUploaded()) continue;
             }
             else if (title.equals(Constants.SYSTEM_CNAME)) {
            	 if (!d.isAppBuiltIn()) continue;
             }
             
             if (d.img_bmp == null) d.img_bmp = Tool.decode_res_bitmap(d.img, this, Constants.DECODE_DISH_IMG_SAMPLE);
             map.put("icon", d.img_bmp); //���ͼ����Դ��ID 
             map.put("name", d.name_chinese);//�������ItemText 
             if (title.equals("�����û�����Ĳ���")) {
            	 String prefix = d.isVerifyDone() ? "�� " : "X ";
            	 map.put("name", prefix + d.name_chinese);
            	 String author_name = "(�ҵ�)";
            	 if (!d.author_name.isEmpty()) author_name = "(" + d.author_name.substring(0, 1) + "**)";
            	 map.put("info", author_name);
             }
             
             index_id_list.add(d.dishid);
             
             al.add(map);
        }
        
        int layout_resid = R.layout.image_text;
        SimpleAdapter sa= new SimpleAdapter(BuiltinDishes.this, al, layout_resid, new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
        if (title.equals("�����û�����Ĳ���")) { 
        	layout_resid = R.layout.image_text_text;
        	sa=new SimpleAdapter(BuiltinDishes.this,al,layout_resid,new String[]{"icon","name","info"},new int[]{R.id.ItemImage,R.id.ItemText,R.id.ItemInfo});
        }
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
        gridView.setAdapter(sa);
        
	    //����GridViewԪ�ص���Ӧ  
	    gridView.setOnItemClickListener(new OnItemClickListener() {  
	  
	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
	            //����������GridViewԪ�ص�λ��  
	            //Toast.makeText(MainActivity.this,mThumbIds[position], Toast.LENGTH_SHORT).show(); 
	        	Log.v("OnItemClickListener", "position = " + position + "id = " + id);
	        	
	        	Dish dish = Dish.getDishById(index_id_list.get(position));
	        	
	        	Intent intent;
	        	if (dish.isAppBuiltIn()) intent = new Intent(BuiltinDishes.this, MakeDishActivityJ.class);
	        	else intent = new Intent(BuiltinDishes.this, MakeDishActivityJ.class);
	        	
	        	intent.putExtra("dish_id", dish.dishid); 
	        	intent.putExtra("title", tv.getText().toString()); 
	        	
	        	intent.putExtra("editable", tv.getText().toString().equals("�Ա����") && !dish.isVerifyDone());
	        	startActivity(intent);
	        }  
	     });
	}
	
//	@Override  
//    public boolean onKeyDown(int keyCode, KeyEvent event) {  
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			back_key_clicked = true;
//		}
//        return super.onKeyDown(keyCode, event);  
//    }
	
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
	protected void onPause() {
		Log.v("BuiltinDishes", "Buildin onPause");
		super.onPause();
//		if (back_key_clicked) {
//			this.finish();
//			back_key_clicked = false;
//		}
	}
	
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        Log.v("BuiltinDishes", "onDestroy");
	}
}
