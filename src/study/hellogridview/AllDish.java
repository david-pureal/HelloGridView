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
		
		// ���ô�Ų໬���������Ĳ����ļ�
		setBehindContentView(R.layout.frame_menu);
		// ���໬����fragment����䵽�໬���������Ĳ����ļ���
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		//SampleListFragment fragment = new SampleListFragment();
		//SampleListFragment fragment = new SampleListFragment();
		MenuFragment fragment = new MenuFragment();
		transaction.replace(R.id.menu_frame, fragment);
		transaction.commit();
		// ��ȡ��SlidingMenu����Ȼ������һЩ����������
		sm = getSlidingMenu();
		fragment.set_sm(sm);
		
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		// ������Ӱ�Ŀ��
		sm.setShadowWidth(50);
		// ������Ӱ����ɫ
		sm.setShadowDrawable(R.drawable.shadow);
		// ���ò໬����ȫչ��֮�󣬾�������һ�ߵľ��룬��λpx�����õ�Խ�󣬲໬���Ŀ��ԽС
		sm.setBehindOffset(200); // 700
		// ���ý���ĳ̶ȣ���Χ��0-1.0f,���õ�Խ�����ڲ໬���ջ�����ʱ����ɫ��Խ����1.0f��ʱ����ɫΪȫ��
		sm.setFadeDegree(0.5f);
		// ���ô���ģʽ������ѡ��ȫ�������������Ǳ�Ե�����������ǲ��ɻ���
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//TOUCHMODE_FULLSCREEN
		
		//����actionBar�ܷ����໬���ƶ������û�У������ȥ��
		setSlidingActionBarEnabled(false);
		
		// ���󻬶��Ĳ˵�
		sm.setSecondaryMenu(R.layout.frame_menu_right);
		sm.setRightMenuOffset(700);
		
		// ����slidingmenu�ر�
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
                //finish();//�رյ�ǰActivity  
            }  
        });
		
		// С����
		TextView tv = (TextView) findViewById(R.id.replace_builtin_tv); 
		Intent intent = getIntent();
		tv.setText(intent.getStringExtra("title"));
		
		Button replace_button = (Button) findViewById(R.id.replace_builtin);
		replace_button.setVisibility(View.GONE);
		
		// ���Ų���
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
              	 map.put("icon", d.img); //���ͼ����Դ��ID 
             }
             else if (d.isVerifyDone()) {
             	 map.put("icon", d.img_bmp); //���ͼ����Դ��ID
             }
             else { continue;}
              
             map.put("name", d.name_chinese);//�������ItemText 
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
       
        //����GridViewԪ�ص���Ӧ  
	    gridView.setOnItemClickListener(new OnItemClickListener() {  
	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
	            //����������GridViewԪ�ص�λ��  
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
                // ����gridview�е�ͼƬ
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
                          	 map.put("icon", d.img); //���ͼ����Դ��ID 
                         }
                         else if (d.isVerifyDone()) {
                         	 map.put("icon", d.img_bmp); //���ͼ����Դ��ID
                         }
                         else { continue;}
                          
                         map.put("name", d.name_chinese);//�������ItemText 
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
