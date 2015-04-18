package study.hellogridview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import cn.sharesdk.framework.ShareSDK;

import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity /*extends Activity  */ extends SlidingFragmentActivity implements OnPageChangeListener /* implements OnTouchListener */{

	ImageButton m_deviceBtn;
	ImageButton m_stateBtn;
	
	/** 
     * ViewPager 
     */  
    private ViewPager viewPager;  
      
    /** 
     * 装点点的ImageView数组 
     */  
    private ImageView[] tips;  
      
    /** 
     * 装ImageView数组 
     */  
    private ImageView[] mImageViews;  
      
    /** 
     * 图片资源id 
     */  
    private int[] imgIdArray ;  
    
    public SlidingMenu sm;
    
    Handler handler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		//setContentView(R.layout.index);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
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
		
		
		//device image to connect wifi
		m_deviceBtn = (ImageButton) findViewById(R.id.imageButton1);
		m_deviceBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(MainActivity.this, SmartLinkActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		
		m_stateBtn = (ImageButton) findViewById(R.id.imageButton2);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(MainActivity.this, CurStateActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		
//		// 图片 滑动切换
//		ViewGroup group = (ViewGroup)findViewById(R.id.viewGroup);  
//        viewPager = (ViewPager) findViewById(R.id.viewPager);  
//          
//        //载入图片资源ID  
//        imgIdArray = new int[]{R.drawable.item01, R.drawable.item02, R.drawable.item03, 
//        		R.drawable.item04, R.drawable.item05, R.drawable.item06};  
//          
//          
//        //将点点加入到ViewGroup中  
//        tips = new ImageView[imgIdArray.length]; 
//        Log.v("MainActivity", "tips.length = " + tips.length);
//        for(int i=0; i<tips.length; i++){  
//            ImageView imageView = new ImageView(this);  
//            imageView.setLayoutParams(new LayoutParams(20,20));  
//            tips[i] = imageView;  
//            if(i == 0){  
//                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);  
//            }else{  
//                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);  
//            }  
//              
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,    
//                    LayoutParams.WRAP_CONTENT));  
//            layoutParams.leftMargin = 5;  
//            layoutParams.rightMargin = 5;  
//            group.addView(imageView, layoutParams);  
//        }  
//          
//          
//        //将图片装载到数组中  
//        mImageViews = new ImageView[imgIdArray.length];  
//        for(int i=0; i<mImageViews.length; i++){  
//            ImageView imageView = new ImageView(this);  
//            mImageViews[i] = imageView;  
//            imageView.setBackgroundResource(imgIdArray[i]);
//            imageView.setOnClickListener(new OnClickListener() {  
//                @Override  
//                public void onClick(View v) {  
//                    Intent intent = new Intent(MainActivity.this,DishActivity.class);
//                    if (cur == 1) cur = 6;
//                    if (cur == 0) cur = 7;
//                    if (cur == 2) cur = 9;
//                    if (cur == 3) cur = 8;
//                    if (cur == 4) cur = 10;
//                    if (cur == 5) cur = 11;
//    	        	intent.putExtra("dish_index", String.valueOf(cur)); 
//    	        	startActivity(intent);
//                }
//            });
//        }  
//          
//        //设置Adapter  
//        viewPager.setAdapter(new MyAdapter());  
//        //设置监听，主要是设置点点的背景  
//        viewPager.setOnPageChangeListener(this); 
//        //设置ViewPager的默认项, 设置为长度的100倍，这样子开始就能往左滑动  
//        Log.v("MainActivity", "mImageViews.length = " +  mImageViews.length);
//        viewPager.setCurrentItem(0);  
        
        // 热门菜谱
		LinkedHashMap<Integer, Dish> dishes = Dish.getAllDish();
        Log.v("MainActivity", "length = " + dishes.size());
        
        ArrayList<HashMap<String,Object>> al=new ArrayList<HashMap<String,Object>>();
        for (Iterator<Integer> it = dishes.keySet().iterator();it.hasNext();)
        {
             int key = it.next();
             Dish d = dishes.get(key);
             HashMap<String, Object> map = new HashMap<String, Object>(); 
             
             if (d.isAppBuiltIn()) {
             	map.put("icon", d.img); //添加图像资源的ID 
             	map.put("name", d.name_chinese);//按序号做ItemText 
                al.add(map);
             }
        }
        
        SimpleAdapter sa= new SimpleAdapter(MainActivity.this,al,R.layout.image_text,new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
        GridView gridView2 = (GridView)findViewById(R.id.gridview2);
        gridView2.setAdapter(sa);
       
        //单击GridView元素的响应  
	    gridView2.setOnItemClickListener(new OnItemClickListener() {  
	  
	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
	            //弹出单击的GridView元素的位置  
	            //Toast.makeText(MainActivity.this,mThumbIds[position], Toast.LENGTH_SHORT).show(); 
	        	Log.v("OnItemClickListener", "position = " + position + "id = " + id);
	        	Intent intent = new Intent(MainActivity.this, DishActivity.class);
	        	intent.putExtra("dish_id", Dish.getDishByIndex(position).dishid); 
	        	startActivity(intent);
	        }  
	     });
	    
	}// onCreate
	
	@Override  
    protected void onResume() {  
        super.onResume();  
        TCPClient.getInstance(this).set_mainact(this); 
        Tool.getInstance().saveDevices(this);
    }
	
	public int cur = 0;
	
	public class MyAdapter extends PagerAdapter{  
		  
		
        @Override  
        public int getCount() {  
            return /*Integer.MAX_VALUE*/6;  
        }  
  
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  
  
        @Override  
        public void destroyItem(View container, int position, Object object) {  
            ((ViewPager)container).removeView(mImageViews[position % mImageViews.length]);  
        	Log.v("MainActivity", "destroyItem");
        }  
  
        /** 
         * 载入图片进去，用当前的position 除以 图片数组长度取余数是关键 
         */  
        @Override  
        public Object instantiateItem(View container, int position) {  
        	try {
    			((ViewPager)container).addView(mImageViews[position % mImageViews.length], 0);  
    			Log.v("MainActivity", "position = " +  position);
    			
            }catch(Exception e){  
                //handler something  
            	Log.v("MainActivity", "Exception" + position % mImageViews.length);
            	e.printStackTrace();
            }  
            return mImageViews[position % mImageViews.length];  
        }          
    }  

	@Override  
    public void onPageScrollStateChanged(int arg0) {  
          
    }  
  
    @Override  
    public void onPageScrolled(int arg0, float arg1, int arg2) {  
          
    }  
  
    @Override  
    public void onPageSelected(int arg0) {  
        setImageBackground(arg0 % mImageViews.length);  
        Log.v("MainActivity", "onPageSelected arg0=" + arg0);
        cur = arg0;
    }  
      
    /** 
     * 设置选中的tip的背景 
     * @param selectItems 
     */  
    private void setImageBackground(int selectItems){  
        for(int i=0; i<tips.length; i++){  
            if(i == selectItems){  
                tips[i].setBackgroundResource(R.drawable.page_indicator_focused);  
            }else{  
                tips[i].setBackgroundResource(R.drawable.page_indicator_unfocused);  
            }  
        }  
    }  
    
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        Log.v("MainActivity", "onDestroy");  
        System.exit(0);
    }
 
}
