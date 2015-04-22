package study.hellogridview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

@SuppressLint("SdCardPath")
public class AllDish extends SlidingFragmentActivity {

	ImageButton m_memu;
	ImageButton m_search;
	
	SlidingMenu sm;
    
    Handler handler;
    
    GridView gridView = null;
    
    boolean isClose = true;
    
    SampleListFragment right_fragment;
    
    ArrayList<Integer> index_id_list = new ArrayList<Integer>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		//setContentView(R.layout.index);
		setContentView(R.layout.activity_builtin_dishes);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
		// 设置存放侧滑栏的容器的布局文件
		setBehindContentView(R.layout.frame_menu);
		// 将侧滑栏的fragment类填充到侧滑栏的容器的布局文件中
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		//SampleListFragment fragment = new SampleListFragment();
		//SampleListFragment fragment = new SampleListFragment();
		MenuFragment fragment = new MenuFragment();
		transaction.replace(R.id.menu_frame, fragment);
		transaction.commit();
		// 获取到SlidingMenu对象，然后设置一些常见的属性
		sm = getSlidingMenu();
		fragment.set_sm(sm);
		
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		// 设置阴影的宽度
		sm.setShadowWidth(50);
		// 设置阴影的颜色
		sm.setShadowDrawable(R.drawable.shadow);
		// 设置侧滑栏完全展开之后，距离另外一边的距离，单位px，设置的越大，侧滑栏的宽度越小
		sm.setBehindOffset(200); // 700
		// 设置渐变的程度，范围是0-1.0f,设置的越大，则在侧滑栏刚划出的时候，颜色就越暗。1.0f的时候，颜色为全黑
		sm.setFadeDegree(0.5f);
		// 设置触摸模式，可以选择全屏划出，或者是边缘划出，或者是不可划出
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//TOUCHMODE_FULLSCREEN
		
		//设置actionBar能否跟随侧滑栏移动，如果没有，则可以去掉
		setSlidingActionBarEnabled(false);
		
		// 向左滑动的菜单
		sm.setSecondaryMenu(R.layout.frame_menu_right);
		sm.setRightMenuOffset(700);
		
		// 监听slidingmenu关闭
		sm.setOnClosedListener(new OnClosedListener() {
		  
		    public void onClosed() {
		    	isClose = true;
		    }
		});

		FragmentTransaction right_transaction = getSupportFragmentManager().beginTransaction();
		right_fragment = new SampleListFragment();
		right_transaction.replace(R.id.menu_frame_right, right_fragment);
		right_transaction.commit();
		right_fragment.set_sm(sm);
		
		
//		SlidingMenu right_sm = getSlidingMenu();
//		right_fragment.set_sm(right_sm);
//		right_sm.setShadowWidth(50);
//		right_sm.setShadowDrawable(R.drawable.shadow);
//		right_sm.setBehindOffset(700);
//		right_sm.setFadeDegree(0.5f);
//		right_sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//TOUCHMODE_FULLSCREEN
//		setSlidingActionBarEnabled(true);
		
		
		//device image to connect wifi
		m_memu = (ImageButton) findViewById(R.id.imageButton2);
		//m_memu.setImageResource(R.drawable.category);
		m_memu.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	//AllDish.this.sm.showMenu(); 
            	startActivity(new Intent(AllDish.this, CurStateActivity.class));  
            }  
        });
		
		m_search = (ImageButton) findViewById(R.id.imageButton1);
		//m_search.setImageResource(R.drawable.search_icon);
		m_search.setImageResource(R.drawable.category_right);
		m_search.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (AllDish.this.isClose) {
            		AllDish.this.showSecondaryMenu();
            		AllDish.this.isClose = false;
            	}
            	else
            		AllDish.this.showContent();
                //startActivity(new Intent(AllDish.this, CurStateActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		
		// 小标题
		TextView tv = (TextView) findViewById(R.id.replace_builtin_tv); 
		Intent intent = getIntent();
		tv.setText(intent.getStringExtra("title"));
		
		Button replace_button = (Button) findViewById(R.id.replace_builtin);
		replace_button.setVisibility(View.GONE);
		
		// 热门菜谱
		LinkedHashMap<Integer, Dish> dishes = Dish.getAllDish();
        Log.v("BuiltinDishes", "length = " + dishes.size());
        
        index_id_list.clear();
        
        ArrayList<HashMap<String,Object>> al=new ArrayList<HashMap<String,Object>>();
        for (Iterator<Integer> it =  dishes.keySet().iterator();it.hasNext();)
        {
             int key = it.next();
             Dish d = dishes.get(key);
             HashMap<String, Object> map = new HashMap<String, Object>(); 
             
             if (d.isAppBuiltIn()) {
              	 map.put("icon", d.img); //添加图像资源的ID 
             }
             else if (d.isVerifyDone()) {
             	 map.put("icon", d.img_bmp); //添加图像资源的ID
             }
             else { continue;}
              
             map.put("name", d.name_chinese);//按序号做ItemText 
             al.add(map);
             index_id_list.add(d.dishid);
        }
        
        SimpleAdapter sa= new SimpleAdapter(AllDish.this, al, R.layout.image_text, new String[]{"icon","name"}, new int[]{R.id.ItemImage, R.id.ItemText});
        
        gridView = (GridView)findViewById(R.id.gridview);
        sa.setViewBinder(new ViewBinder(){  
            @Override  
            public boolean setViewValue(View view, Object data,  
                    String textRepresentation) {  
                if( (view instanceof ImageView) && (data instanceof Bitmap) ) {  
                    ImageView iv = (ImageView) view;  
                    Bitmap  bm = (Bitmap) data;  
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
	        	Log.v("OnItemClickListener", "position = " + position + "id = " + id);
	        	Dish dish = Dish.getDishById(index_id_list.get(position));
	        	
	        	Intent intent;
	        	if (dish.isAppBuiltIn()) intent = new Intent(AllDish.this, DishActivity.class);
	        	else intent = new Intent(AllDish.this, MakeDishActivity.class);
	        	
	        	intent.putExtra("dish_id", dish.dishid); 
	        	startActivity(intent);		
	        }  
	     });
	    
	    handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 更新gridview中的图片
            	if (msg.what == 0x567) {  
            		Log.v("AllDish", "pos = " + right_fragment.getId()); 
            		int pos = (Integer) msg.obj;
            		pos = new Random().nextInt(5); 
            		LinkedHashMap<Integer, Dish> dishes = Dish.getAllDish();
                    Log.v("AllDish", "length = " + dishes.size());
                    
                    index_id_list.clear();
                    
                    ArrayList<HashMap<String, Object>> al = new ArrayList<HashMap<String,Object>>();
                    for (Iterator<Integer> it = dishes.keySet().iterator();it.hasNext();)
                    {
                         int key = it.next();
                         if (key < pos) continue;
                         Dish d = dishes.get(key);
                         
                         HashMap<String, Object> map = new HashMap<String, Object>(); 
                         if (d.isAppBuiltIn()) {
                          	 map.put("icon", d.img); //添加图像资源的ID 
                         }
                         else if (d.isVerifyDone()) {
                         	 map.put("icon", d.img_bmp); //添加图像资源的ID
                         }
                         else { continue;}
                          
                         map.put("name", d.name_chinese);//按序号做ItemText 
                         al.add(map);
                         index_id_list.add(d.dishid);
                    }
                    
                    SimpleAdapter sa= new SimpleAdapter(AllDish.this,al,R.layout.image_text,new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
                    sa.setViewBinder(new ViewBinder(){  
                        @Override  
                        public boolean setViewValue(View view, Object data,  
                                String textRepresentation) {  
                            if( (view instanceof ImageView) && (data instanceof Bitmap) ) {  
                                ImageView iv = (ImageView) view;  
                                Bitmap  bm = (Bitmap) data;  
                                iv.setImageBitmap(bm); 
                                return true;  
                            }  
                            return false;  
                        }  
                    });
                    gridView.setAdapter(sa);
            	}
            }  
        };
        
        fragment.set_handler(handler);
        right_fragment.set_handler(handler);
	}
}
