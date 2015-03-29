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
     * װ����ImageView���� 
     */  
    private ImageView[] tips;  
      
    /** 
     * װImageView���� 
     */  
    private ImageView[] mImageViews;  
      
    /** 
     * ͼƬ��Դid 
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
		
		
		//device image to connect wifi
		m_deviceBtn = (ImageButton) findViewById(R.id.imageButton1);
		m_deviceBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(MainActivity.this, SmartLinkActivity.class));  
                //finish();//�رյ�ǰActivity  
            }  
        });
		
		m_stateBtn = (ImageButton) findViewById(R.id.imageButton2);
		m_stateBtn.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(MainActivity.this, CurStateActivity.class));  
                //finish();//�رյ�ǰActivity  
            }  
        });
		
//		// ͼƬ �����л�
//		ViewGroup group = (ViewGroup)findViewById(R.id.viewGroup);  
//        viewPager = (ViewPager) findViewById(R.id.viewPager);  
//          
//        //����ͼƬ��ԴID  
//        imgIdArray = new int[]{R.drawable.item01, R.drawable.item02, R.drawable.item03, 
//        		R.drawable.item04, R.drawable.item05, R.drawable.item06};  
//          
//          
//        //�������뵽ViewGroup��  
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
//        //��ͼƬװ�ص�������  
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
//        //����Adapter  
//        viewPager.setAdapter(new MyAdapter());  
//        //���ü�������Ҫ�����õ��ı���  
//        viewPager.setOnPageChangeListener(this); 
//        //����ViewPager��Ĭ����, ����Ϊ���ȵ�100���������ӿ�ʼ�������󻬶�  
//        Log.v("MainActivity", "mImageViews.length = " +  mImageViews.length);
//        viewPager.setCurrentItem(0);  
        
        // ���Ų���
		LinkedHashMap<Integer, Dish> dishes = Dish.getAllDish();
        Log.v("MainActivity", "length = " + dishes.size());
        
        ArrayList<HashMap<String,Object>> al=new ArrayList<HashMap<String,Object>>();
        for (Iterator<Integer> it = dishes.keySet().iterator();it.hasNext();)
        {
             int key = it.next();
             Dish d = dishes.get(key);
             HashMap<String, Object> map = new HashMap<String, Object>(); 
             
             if (d.isAppBuiltIn()) {
             	map.put("icon", d.img); //���ͼ����Դ��ID 
             	map.put("name", d.name_chinese);//�������ItemText 
                al.add(map);
             }
        }
        
        SimpleAdapter sa= new SimpleAdapter(MainActivity.this,al,R.layout.image_text,new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
        GridView gridView2 = (GridView)findViewById(R.id.gridview2);
        gridView2.setAdapter(sa);
       
        //����GridViewԪ�ص���Ӧ  
	    gridView2.setOnItemClickListener(new OnItemClickListener() {  
	  
	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
	            //����������GridViewԪ�ص�λ��  
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
         * ����ͼƬ��ȥ���õ�ǰ��position ���� ͼƬ���鳤��ȡ�����ǹؼ� 
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
     * ����ѡ�е�tip�ı��� 
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
    }
 
}
