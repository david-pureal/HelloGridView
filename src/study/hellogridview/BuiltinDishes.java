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
                // 如果消息来自子线程  
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
        
        // 设置存放侧滑栏的容器的布局文件
		setBehindContentView(R.layout.frame_menu);
		// 将侧滑栏的fragment类填充到侧滑栏的容器的布局文件中
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
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
		
		if (intent.getStringExtra("title").equalsIgnoreCase("自编菜谱")) {
			 Log.v("BuiltinDishes", "自编菜谱");
			 //make_new_dish.setVisibility(View.GONE);
			 TextView make_new_dish2 = (TextView) findViewById(R.id.make_new_dish2);
			 make_new_dish2.setVisibility(View.VISIBLE);
			 make_new_dish2.setOnClickListener(new OnClickListener() {  
	              @Override  
	              public void onClick(View v) {  
	                  startActivity(new Intent(BuiltinDishes.this, InputDishNameActivity.class));  
	                  //finish();//关闭当前Activity  
	              }  
	        });
		}
		else if (intent.getStringExtra("title").equalsIgnoreCase("菜谱审核")) {
			 Log.v("BuiltinDishes", "菜谱审核");
			 //make_new_dish.setVisibility(View.GONE);
		}
		else if (intent.getStringExtra("title").equals("用户菜谱")) {
			tv.setText("所有用户分享的菜谱");
			TextView note  = (TextView) findViewById(R.id.verify_note);
			note.setVisibility(View.VISIBLE);
		}
		
	} // oncreate
	
	protected void tell(boolean is_ok) {
		this.all_dish_str = "所有内置菜谱序号： \n";
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
             else if (title.equals("菜谱审核") && !d.isVerifying()) {
            	 continue;
             }
             else if (title.equals("自编菜谱")) {
            	 if (d.isAppBuiltIn()) continue;
            	 
            	 Log.v("BuiltinDishes", "d.dishid=" + d.dishid + ", d.author_id=" + d.author_id + ", d.author_name=" + d.author_name);
            	 if (!d.isMine()) {
            		 Log.v("BuiltinDishes", "dish not my dish.");
            		 continue;
            	 }
             }
             else if (title.equals("收藏菜谱")) {
            	 if (!Account.isFavorite(d)) continue;
             }
             else if (title.equals("所有用户分享的菜谱")) {
            	 if (d.isAppBuiltIn()) continue;
            	 if (d.hasNotUploaded()) continue;
             }
             else if (title.equals(Constants.SYSTEM_CNAME)) {
            	 if (!d.isAppBuiltIn()) continue;
             }
             
             if (d.img_bmp == null) d.img_bmp = Tool.decode_res_bitmap(d.img, this, Constants.DECODE_DISH_IMG_SAMPLE);
             map.put("icon", d.img_bmp); //添加图像资源的ID 
             map.put("name", d.name_chinese);//按序号做ItemText 
             if (title.equals("所有用户分享的菜谱")) {
            	 String prefix = d.isVerifyDone() ? "√ " : "X ";
            	 map.put("name", prefix + d.name_chinese);
            	 String author_name = "(我的)";
            	 if (!d.author_name.isEmpty()) author_name = "(" + d.author_name.substring(0, 1) + "**)";
            	 map.put("info", author_name);
             }
             
             index_id_list.add(d.dishid);
             
             al.add(map);
        }
        
        int layout_resid = R.layout.image_text;
        SimpleAdapter sa= new SimpleAdapter(BuiltinDishes.this, al, layout_resid, new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
        if (title.equals("所有用户分享的菜谱")) { 
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
        
	    //单击GridView元素的响应  
	    gridView.setOnItemClickListener(new OnItemClickListener() {  
	  
	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
	            //弹出单击的GridView元素的位置  
	            //Toast.makeText(MainActivity.this,mThumbIds[position], Toast.LENGTH_SHORT).show(); 
	        	Log.v("OnItemClickListener", "position = " + position + "id = " + id);
	        	
	        	Dish dish = Dish.getDishById(index_id_list.get(position));
	        	
	        	Intent intent;
	        	if (dish.isAppBuiltIn()) intent = new Intent(BuiltinDishes.this, MakeDishActivityJ.class);
	        	else intent = new Intent(BuiltinDishes.this, MakeDishActivityJ.class);
	        	
	        	intent.putExtra("dish_id", dish.dishid); 
	        	intent.putExtra("title", tv.getText().toString()); 
	        	
	        	intent.putExtra("editable", tv.getText().toString().equals("自编菜谱") && !dish.isVerifyDone());
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
