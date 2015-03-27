package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;














import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;






//import android.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class BuiltinDishes extends SlidingFragmentActivity implements OnTouchListener {

	GridView gridView = null;	
	Button replace_builtin;
	Button getBuiltin;
	Handler handler;
	protected String all_dish_str;
	
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

    public SlidingMenu sm;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("BuiltinDishes", "oncreate");
		
		TCPClient.getInstance().set_builtinact(this);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		setContentView(R.layout.activity_builtin_dishes);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebtn);
		
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
                	
                		//BuiltinDishes.this.tell(rp.is_ok);
                	}
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
        
        //device image to connect wifi
        ImageButton m_deviceBtn = (ImageButton) findViewById(R.id.imageButton1);
  		m_deviceBtn.setOnClickListener(new OnClickListener() {  
              @Override  
              public void onClick(View v) {  
                  startActivity(new Intent(BuiltinDishes.this,SmartLinkActivity.class));  
                  //finish();//�رյ�ǰActivity  
              }  
          });
  		
  		ImageButton m_stateBtn = (ImageButton) findViewById(R.id.imageButton2);
  		m_stateBtn.setOnClickListener(new OnClickListener() {  
              @Override  
              public void onClick(View v) {  
                  startActivity(new Intent(BuiltinDishes.this,CurStateActivity.class));  
                  //finish();//�رյ�ǰActivity  
              }  
          });
		
		gridView = (GridView)findViewById(R.id.gridview);  
		replace_builtin = (Button)findViewById(R.id.replace_builtin); 
		TextView tv = (TextView) findViewById(R.id.replace_builtin_tv); 
		Intent intent = getIntent();
		tv.setText(intent.getStringExtra("title"));
		
		ImageView make_new_dish = (ImageView) findViewById(R.id.make_new_dish);
		make_new_dish.setImageResource(R.drawable.make_new_dish_128);
		make_new_dish.setVisibility(View.GONE);
		
		if (intent.getStringExtra("title").equalsIgnoreCase("�Ա����")) {
			 Log.v("BuiltinDishes", "�Ա����");
			 replace_builtin.setVisibility(View.GONE);
			 make_new_dish.setVisibility(View.VISIBLE);
			 make_new_dish.setOnClickListener(new OnClickListener() {  
	              @Override  
	              public void onClick(View v) {  
	                  startActivity(new Intent(BuiltinDishes.this, InputDishNameActivity.class));  
	                  //finish();//�رյ�ǰActivity  
	              }  
	          });
		}
		replace_builtin = (Button)findViewById(R.id.replace_builtin);  
		replace_builtin.setOnClickListener(new OnClickListener() {  
      	  
            @Override  
            public void onClick(View v) {  
                try {  
                	//for (int i = 0; i < 12; ++i) {
                	int i = 2;
	                    // ���û����°�ť֮�󣬽��û���������ݷ�װ��Message,Ȼ���͸����߳�Handler  
	                    Message msg = new Message();  
	                    msg.what = 0x345;  
	                    Package data = new Package(Package.Update_Favorite, Dish.getAllDish()[12]);
	                    data.set_replaced_id(2);
	                    msg.obj = data.getBytes();
	                    TCPClient.getInstance().sendMsg(msg); 
	                    
	                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                    while(data.get_img_pkg(baos) && baos.size() != 0) {
	                    	Log.v("BuiltinDishes", "img baos.size() = " + baos.size());
	                    	Message msgtmp = new Message();  
	                    	msgtmp.what = 0x345; 
	                    	msgtmp.obj = baos;
	                    	TCPClient.getInstance().sendMsg(msgtmp); 
	                    	baos = new ByteArrayOutputStream();
	                    }
	                    Log.v("BuiltinDishes", "replace dish " + i + " to " + (12 - i) + " done!");
	                    
	                    //TODO : should wait for replace response
                	//}
                    
                    // send sound
//                    if (/*DeviceState.getInstance().use_sound == 0x01*/ true) {
//                    	while(data.get_sound_pkg(baos) && baos.size() != 0) {
//                        	Log.v("DishActivity", "sound baos.size() = " + baos.size());
//                        	Message msgtmp = new Message();  
//                        	msgtmp.what = 0x345; 
//                        	msgtmp.obj = baos;
//                        	TCPClient.getInstance().sendMsg(msgtmp); 
//                        	baos = new ByteArrayOutputStream();
//                        }
//                    }
                } catch (Exception e) { 
                	e.printStackTrace();
                	Log.v("BuiltinDishes", "prepare package data exception");
                }  
            }  
        }); 
		
	    LinearLayout dish_layout = (LinearLayout) findViewById(R.id.layout_builtin);  
        dish_layout.setOnTouchListener(this);

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
				// TODO Auto-generated method stub
				Toast.makeText(BuiltinDishes.this, BuiltinDishes.this.all_dish_str, Toast.LENGTH_SHORT).show();
			}
		});
		
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

	public Handler getHandler() {
    	return this.handler;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.v("BuiltinDishes", "Buildin onResume");
		Dish [] dishes = Dish.getAllDish();
        Log.v("BuiltinDishes", "length = " + dishes.length);
        
        ArrayList<HashMap<String,Object>> al=new ArrayList<HashMap<String,Object>>();
        for (int i=0;i<dishes.length;i++)
        {
            HashMap<String, Object> map = new HashMap<String, Object>(); 
                
            if (dishes[i].isBuiltIn) {
            	map.put("icon", dishes[i].img); //���ͼ����Դ��ID 
            }
            else {
            	BitmapDrawable bd = dishes[i].img_drawable;
            	map.put("icon", bd.getBitmap()); //���ͼ����Դ��ID 
            }
            map.put("name", dishes[i].name_chinese);//�������ItemText 
            al.add(map); 
        }
        
        SimpleAdapter sa= new SimpleAdapter(BuiltinDishes.this,al,R.layout.image_text,new String[]{"icon","name"},new int[]{R.id.ItemImage,R.id.ItemText});
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
        Log.v("BuiltinDishes", "length2 = " + dishes.length);
        
	    //����GridViewԪ�ص���Ӧ  
	    gridView.setOnItemClickListener(new OnItemClickListener() {  
	  
	        @Override  
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  
	            //����������GridViewԪ�ص�λ��  
	            //Toast.makeText(MainActivity.this,mThumbIds[position], Toast.LENGTH_SHORT).show(); 
	        	Log.v("OnItemClickListener", "position = " + position + "id = " + id);
	        	Intent intent;
	        	Dish dish = Dish.getAllDish()[position];
	        	if (dish.isBuiltIn) intent = new Intent(BuiltinDishes.this, DishActivity.class);
	        	else intent = new Intent(BuiltinDishes.this, MakeDishActivity.class);
	        	intent.putExtra("dish_index", position); 
	        	startActivity(intent);
	        }  
	     });
	}
}
