package study.hellogridview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity implements OnTouchListener {

	public Switch swt_english;
	public Switch swt_sound;
	
	public byte use_english = 0;
	public byte use_sound = 0;
	public byte option_id = 1; // 1为语音提示；2为使用英文
	public byte opr = 0; //用户做了开启还是关闭的操作，0为关闭，1为开启
	
	public Button verify_dish;
	
	Handler handler;
	
	//手指向右滑动时的最小速度  
    private static final int XSPEED_MIN = 200;  
    //手指向右滑动时的最小距离  
    private static final int XDISTANCE_MIN = 150;  
    //记录手指按下时的横坐标。  
    private float xDown;  
    //记录手指移动时的横坐标。  
    private float xMove;  
      
    //用于计算手指滑动的速度。  
    private VelocityTracker mVelocityTracker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		TCPClient.getInstance().set_settingact(this);
		
		handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == 0x123) {   
                	
                	RespPackage rp = (RespPackage) msg.obj;
                	Log.v("SettingActivity", "SettingActivity got resp, cmdtype_head=" + (rp.cmdtype_head&0xff) + ", cmdtype_body=" + (rp.cmdtype_body&0xff));
                	if (rp.cmdtype_head == Package.ACK && rp.cmdtype_body == Package.Set_Option && !rp.is_ok) {
                		SettingActivity.this.tell("Set_Option fail!");
                	}
                	else if (rp.cmdtype_head == Package.Machine_State) {
                		SettingActivity.this.OnResp(rp);
                	}
                	Log.v("SettingActivity", "SettingActivity got event");
                }  
            }  
        };
		
		swt_english = (Switch) findViewById(R.id.switch_english);
		swt_english.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_english = opr;
            	option_id = 2;
            	
            	SettingActivity.this.do_send_msg();    
            }  
        }); 
		
		swt_sound = (Switch) findViewById(R.id.switch_sound);
		swt_sound.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_sound = opr;
            	option_id = 1;
            	
            	SettingActivity.this.do_send_msg();
            }  
        }); 
		
		verify_dish = (Button) findViewById(R.id.verify_dish);
		verify_dish.setVisibility(Account.is_login ? View.VISIBLE : View.GONE);
		verify_dish.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	Intent intent = new Intent(SettingActivity.this, BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("菜谱审核")); 
                startActivity(intent);  
                //finish();//关闭当前Activity  
            }  
        });
		
		LinearLayout setting_layout = (LinearLayout) findViewById(R.id.layout_setting);  
		setting_layout.setOnTouchListener(this);
	}

	public boolean do_send_msg() {
		Message msg = new Message();  
        msg.what = 0x345;  
        
        Package data = new Package(Package.Set_Option);
        msg.obj = data.getBytes();
        TCPClient.getInstance().sendMsg(msg); 
        
        return true;
	}
	
	public void OnResp(RespPackage rp) {
		DeviceState ds = DeviceState.getInstance();
		swt_sound.setChecked(ds.use_sound == 0x01);
		swt_english.setChecked(ds.use_english == 0x01);
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	public void tell(final String info) {
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, info, Toast.LENGTH_SHORT).show();
				}
			});
		}
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
            //活动的距离  
            int distanceX = (int) (xMove - xDown);  
            //获取顺时速度  
            int xSpeed = getScrollVelocity();  
            //当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity  
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
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。 
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
     * 回收VelocityTracker对象。 
     */  
    private void recycleVelocityTracker() {  
        mVelocityTracker.recycle();  
        mVelocityTracker = null;  
    }  
      
    /** 
     * 获取手指在content界面滑动的速度。 
     *  
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。 
     */  
    private int getScrollVelocity() {  
        mVelocityTracker.computeCurrentVelocity(1000);  
        int velocity = (int) mVelocityTracker.getXVelocity();  
        return Math.abs(velocity);  
    } 
}
