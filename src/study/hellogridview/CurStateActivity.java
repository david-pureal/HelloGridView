package study.hellogridview;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CurStateActivity extends Activity implements OnSeekBarChangeListener {
	public int total_time = 260;
	public byte temp = (byte) 180;
	public byte jiaoban_speed = 1;
	public int dish_id = 1;
	public Dish dish;
	public byte modify_state = (byte) 0xE0; //时间、温度、搅拌、控制。。。是否被用户改动过的标识位，最高字节为时间，以此类推
	public byte control = 3;                // 0表示开始炒菜，1表示暂停，2表示取消，3表示解锁
	
	public final String net_mode = "AP";    // wifi模块的工作模式：AP或者STA
	public final int MAX_TIME = 3599;       // in seconds
	
	public List<String> jiaoban_str = new ArrayList<String>();

	RadioGroup group;
	RadioButton selectedRBtn;
	
	VerticalSeekBar bar;
	//SeekBar bar;
	
	public ImageView main;
	private Paint paint = new Paint();
	private static List<Integer> data;
	public ImageView add;
	public ImageView minus;
	public ImageView start_pause;
	public ImageView stop_cook;
	
	Handler handler;
	
	TCPClient tcpclient;
	
	DeviceState ds = DeviceState.getInstance();
	
	public static int t = 50;
	
	ImageView back;
	
	public TextView switch_ui_tv;
	public boolean is_standard_ui = true;
	
	private long down_timestamp = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,      
                WindowManager.LayoutParams. FLAG_FULLSCREEN); 
		
		setContentView(R.layout.activity_cur_state);
		
		if (tcpclient == null) {
			tcpclient = TCPClient.getInstance();
			tcpclient.set_curstateact(this); 
		}
		
		if (data == null) {
			data = new ArrayList<Integer>();
		}
		
		Intent intent = getIntent();
		if (intent != null) {
			dish_id = intent.getIntExtra("dish_id", 1); 
		} else {
			dish_id = ds.dishid & 0xffff;
		}
		Log.v("CurStateActivity", "onCreate dish_id = " + dish_id + ", ds.dishid= " + ds.dishid);
		
		//data.clear();
		
		if (jiaoban_str.isEmpty()) {
			jiaoban_str.add("1不搅拌");
			jiaoban_str.add("2特慢速");
			jiaoban_str.add("3较慢速");
			jiaoban_str.add("4中慢速");
			jiaoban_str.add("5中快速");
			jiaoban_str.add("6较快速");
			jiaoban_str.add("7特快速");
			jiaoban_str.add("8连续搅");
		}
		
		width = dip2px(Constants.UI_WIDTH);
        height = dip2px(Constants.UI_HEIGHT);
        Log.v("CurStateActivity", "width=" + width + ", height=" + height);
        
        x_start  = (int) (198.0/Constants.UI_WIDTH * width);
        x_middle = (int) (316.0/Constants.UI_WIDTH * width);
        x_end    = (int) (458.0/Constants.UI_WIDTH * width);
        
        y_min = (int) (153.0/Constants.UI_HEIGHT * height);
        y_max = (int) (260.0/Constants.UI_HEIGHT * height);
        
        temperature_x = (int) (180.0/Constants.UI_WIDTH * width);  // 左下角坐标
        temperature_x_2 = (int) (195.0/Constants.UI_WIDTH * width);
    	temperature_y = (int) (118.0/Constants.UI_HEIGHT * height);
    	
    	time_x = (int) (376.0/Constants.UI_WIDTH * width);
    	time_y = (int) (118.0/Constants.UI_HEIGHT * height);
    	zhuliao_time_x = (int) (296.0/Constants.UI_WIDTH * width);
    	zhuliao_time_y = (int) (144.0/Constants.UI_HEIGHT * height);
    	fuliao_time_x = (int) (426.0/Constants.UI_WIDTH * width);
    	fuliao_time_y = (int) (144.0/Constants.UI_HEIGHT * height);
    	
    	jiaoban_x = (int) (21.0/Constants.UI_WIDTH * width);
    	jiaoban_y = (int) (182.0/Constants.UI_HEIGHT * height);
    	
    	img_tiny_rect.left   = (int) (368.0/Constants.UI_WIDTH  * width);
    	img_tiny_rect.right  = (int) (470.0/Constants.UI_WIDTH  * width);
    	img_tiny_rect.top    = (int) (8.0  /Constants.UI_HEIGHT * height);
    	img_tiny_rect.bottom = (int) (78.0 /Constants.UI_HEIGHT * height);
    	
    	lock_rect.left   = (int) (14.0 /Constants.UI_WIDTH  * width);
        lock_rect.right  = (int) (40.0 /Constants.UI_WIDTH  * width);
        lock_rect.top    = (int) (30.0 /Constants.UI_HEIGHT * height);
        lock_rect.bottom = (int) (64.0 /Constants.UI_HEIGHT * height);
    	
    	jianban_angles.add(85);
    	jianban_angles.add(45);
    	jianban_angles.add(0);
    	jianban_angles.add(-45);
    	jianban_angles.add(-85);
    	
    	simple_time_x        = (int) (28.0/Constants.UI_WIDTH  * width);
    	simple_time_x_4      = (int) (5.0/Constants.UI_WIDTH  * width);
    	simple_time_y        = (int) (116.0/Constants.UI_HEIGHT * height);
        simple_temperature_x = (int) (184.0/Constants.UI_WIDTH  * width);
    	simple_temperature_y = (int) (253.0/Constants.UI_HEIGHT * height);
    	simple_temperature_x_2 = (int) (210.0/Constants.UI_WIDTH  * width);
    	simple_jiaoban_x     = (int) (388.0/Constants.UI_WIDTH  * width);
    	simple_jiaoban_y     = (int) (131.0/Constants.UI_HEIGHT * height);
    	
		handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == 0x123) {  
                    // 将读取的内容追加显示在文本框中  
                    //show.append("\n" + msg.obj.toString());  
                	
                	Log.v("CurStateActivity", "got Machine state event time=" + ds.time + "temp=" + (ds.temp & 0x00ff) + "jiaoban=" + ds.jiaoban_speed);
                	synchronized (this) {
	                	//CurStateActivity.this.time = ds.time;
	                	//CurStateActivity.this.temp = ds.temp;
	                	CurStateActivity.this.jiaoban_speed = ds.jiaoban_speed;
	                	dish_id = Math.max(1, ds.dishid & 0xffff);
	                	
	                	if (dish != null && dish_id != dish.dishid) dish = Dish.getDishById(dish_id); 
                	}
                	
                	CurStateActivity.this.update_seekbar();
                	
                	int MaxDataSize = 800;
                	if(ds.working_state != Constants.MACHINE_WORK_STATE_STOP) {//待机中
                		//stop_cook.setVisibility(View.VISIBLE);
                		if (ds.working_state == Constants.MACHINE_WORK_STATE_PAUSE) {
                			//CurStateActivity.this.jiaoban_speed = 1;
                		}
                		else if (data.size() < MaxDataSize){ 
                			data.add(Math.min(200, (int)(ds.temp & 0x00ff)));
                			//t += 5; data.add(Math.min(190, t));
                		}
                	} else {
                		//stop_cook.setVisibility(View.GONE);
                		//CurStateActivity.this.jiaoban_speed = 1;
                		state = STATE_HEATING;
                		t = 50;
                		data.clear();
                	}
                	draw_temp_baselin();
                	
                } else if (msg.what == 0x234) {
                	//main.invalidate();
                	draw_temp_baselin();
                	//draw_temp_curve();
                }
                
            }  
        };
		
		group = (RadioGroup)this.findViewById(R.id.radioGroup1);
		selectedRBtn = (RadioButton)this.findViewById(group.getCheckedRadioButtonId());
		
		bar = (VerticalSeekBar) findViewById(R.id.seekBar1);
		bar.setMax(100);
		bar.setOnSeekBarChangeListener(this);
		bar.mlistener = this;
		
		update_seekbar();
		
		class PicOnTouchListener implements OnTouchListener{  
			int i = 1;
			PicOnTouchListener (int i) {
				this.i = i;
			}
			
	        @Override  
	        public boolean onTouch(View v, MotionEvent event){  
	        	boolean is_do_stop = false;
	        	if (v.equals(add) || v.equals(minus)) { // add or minus
		        	switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	selectedRBtn.setTextColor(Color.rgb(255, 0, 0));
		          	  	break;
		            case MotionEvent.ACTION_UP:
		            	
		            	if (selectedRBtn.getId() == R.id.temp) {
		            		int t = (CurStateActivity.this.temp & 0x00ff)+ i*2;
		            		t = Math.min(200, t);
		            		CurStateActivity.this.temp = (byte) Math.max(0, t);
		        		} else if (selectedRBtn.getId() == R.id.jiaoban) {
		        			CurStateActivity.this.jiaoban_speed = (byte) (CurStateActivity.this.jiaoban_speed + i*1);
		        			CurStateActivity.this.jiaoban_speed = (byte) Math.min(8, CurStateActivity.this.jiaoban_speed & 0xff);
		        			CurStateActivity.this.jiaoban_speed = (byte) Math.max(1, CurStateActivity.this.jiaoban_speed & 0xff);
		        		} else if (selectedRBtn.getId() == R.id.time) {
		        			Log.v("CurStateActivity", "ds.time =" + ds.time);
		        			ds.time = (short) (ds.time + i*10);
		        			ds.time = (short) Math.min(MAX_TIME, ds.time);
		        			ds.time = (short) Math.max(0, ds.time);
		        			Log.v("CurStateActivity", "CurStateActivity set time = " + ds.time);
		        		}
		            	
		            	CurStateActivity.this.modify_state = (byte)0xE0;
		            	CurStateActivity.this.onStopTrackingTouch(CurStateActivity.this.bar);
		            	
		            	selectedRBtn.setTextColor(Color.rgb(0, 0, 0));
		                break;
		        	}
	        	}
	        	else if (v.equals(start_pause)) { // 长按start_pause3秒，停止炒菜
	        		switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	down_timestamp = System.currentTimeMillis();
		          	  	break;
		            case MotionEvent.ACTION_UP:
		            	if (System.currentTimeMillis() - down_timestamp > 3000) {
		            		if (ds.working_state != Constants.MACHINE_WORK_STATE_STOP) control = Constants.MACHINE_WORK_STATE_STOP;
		                	modify_state = (byte) 0x10;
		                	is_do_stop = true;
		            	}
		            	else {
		            		if (ds.working_state == Constants.MACHINE_WORK_STATE_COOKING) control = Constants.MACHINE_WORK_STATE_PAUSE;
		                	else if (ds.working_state == Constants.MACHINE_WORK_STATE_PAUSE) control = Constants.MACHINE_WORK_STATE_COOKING;
		                	else if (ds.working_state == Constants.MACHINE_WORK_STATE_STOP) control = Constants.MACHINE_WORK_STATE_COOKING;
		                	modify_state = (byte) 0x10;
		            	}
		            	
		            	Message msg = new Message();  
	                    msg.what = 0x345;  
	                    Package data = new Package(Package.Set_Param);
	                    msg.obj = data.getBytes();
	                    tcpclient.sendMsg(msg);
	                    
	                    //play stop voice
	                    if (is_do_stop) {
		                    player_stop = MediaPlayer.create(CurStateActivity.this, R.raw.voice_stop);
		                    player_stop.start();
	                    }
		                break;
		        	}
	        	}
	        	else if (v.equals(main)) {
	        		Log.v("CurStateActivity", "main.x = " + event.getX() + ", main.y = " + event.getY());
	        		Log.v("CurStateActivity", "main.x = " + lock_rect.right + ", main.y = " + lock_rect.bottom);
	        		boolean down_on_lock = false;
	        		switch (event.getAction()) {
		            case MotionEvent.ACTION_DOWN:
		            	if (event.getX() < lock_rect.right && event.getY() < lock_rect.bottom) down_on_lock = true;
		            case MotionEvent.ACTION_UP:
		            	if (event.getX() < lock_rect.right && event.getY() < lock_rect.bottom && down_on_lock) {
		            		control = ds.is_locked() ? Constants.MACHINE_UNLOCK_MACHINE : Constants.MACHINE_LOCK_MACHINE;
		                	modify_state = (byte) 0x10;
		                	Log.v("CurStateActivity", "change lock state, current locked = " + ds.is_locked());
		                	
		                	if (control == Constants.MACHINE_UNLOCK_MACHINE) { // 只能解锁，不能上锁，上锁是自动完成
			                	Message msg = new Message();  
			                    msg.what = 0x345;  
			                    Package data = new Package(Package.Set_Param);
			                    msg.obj = data.getBytes();
			                    tcpclient.sendMsg(msg);
		                	}
		            	}
		            	break;
	        		}
	        	}
	            return true;  
	        }  
	    } 
		
		add = (ImageView) findViewById(R.id.add);
		add.setOnTouchListener(new PicOnTouchListener(1));
		
		minus = (ImageView) findViewById(R.id.minus);
		minus.setOnTouchListener(new PicOnTouchListener(-1));
		
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	finish(); 
            }  
        });
		
		//绑定一个匿名监听器
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				int radioButtonId = arg0.getCheckedRadioButtonId();
				//根据ID获取RadioButton的实例
				selectedRBtn = (RadioButton)CurStateActivity.this.findViewById(radioButtonId);
				// update seekbar progress
				CurStateActivity.this.update_seekbar();
			}
		});
		
		//android:src="@drawable/state_bg"
		main = (ImageView) findViewById(R.id.main);
		main.setOnTouchListener(new PicOnTouchListener(0));
		main.setBackgroundResource(R.drawable.standard_bkg);
		
		start_pause = (ImageView) findViewById(R.id.start_pause);
		start_pause.setOnTouchListener(new PicOnTouchListener(0));
//		start_pause.setOnClickListener(new OnClickListener() {  
//            @Override  
//            public void onClick(View v) {  
//            	if (ds.working_state == Constants.MACHINE_WORK_STATE_COOKING) control = Constants.MACHINE_WORK_STATE_PAUSE;
//            	else if (ds.working_state == Constants.MACHINE_WORK_STATE_PAUSE) control = Constants.MACHINE_WORK_STATE_COOKING;
//            	else if (ds.working_state == Constants.MACHINE_WORK_STATE_STOP) control = Constants.MACHINE_WORK_STATE_COOKING;
//            	modify_state = (byte) 0x10;
//            	
//            	Message msg = new Message();  
//                msg.what = 0x345;  
//                Package data = new Package(Package.Set_Param);
//                msg.obj = data.getBytes();
//                tcpclient.sendMsg(msg); 
//            }  
//        }); 
		
		stop_cook = (ImageView) findViewById (R.id.stop_cook);
		stop_cook.setVisibility(View.GONE);
//		stop_cook.setOnClickListener(new OnClickListener() {  
//            @Override  
//            public void onClick(View v) {  
//            	if (ds.working_state != Constants.MACHINE_WORK_STATE_STOP) control = Constants.MACHINE_WORK_STATE_STOP;
//            	modify_state = (byte) 0x10;
//            	
//            	Message msg = new Message();  
//                msg.what = 0x345;  
//                Package data = new Package(Package.Set_Param);
//                msg.obj = data.getBytes();
//                tcpclient.sendMsg(msg); 
//            }  
//        });
		
		switch_ui_tv = (TextView) findViewById (R.id.switch_ui_tv);
		switch_ui_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	is_standard_ui = !is_standard_ui;
            	switch_ui_tv.setText(is_standard_ui ? "简洁界面" : "标准界面");
            	//main.setImageResource(is_standard_ui ? R.drawable.standard_bkg : R.drawable.simple_bkg);
            	ui_changed = true;
            	handler.sendEmptyMessage(0x234);
            }  
        });
		
		dish_id = Math.max(1, dish_id);
		dish = Dish.getDishById(dish_id);
		this.total_time = dish.zhuliao_time + dish.fuliao_time;
        draw_temp_baselin(); 
	} // oncreate
	
	public boolean ui_changed = false;

	public final static int STATE_HEATING = 0;
	public final int STATE_ADD_OIL = 1;
	public final int STATE_ZHULIAO = 2;
	public final int STATE_ZHULIAO_TISHI_DONE = 3;
	public final int STATE_FULIAO = 4;
	public final int STATE_FULIAO_DONE = 5;
	
	public static int state = STATE_HEATING;
	private static boolean zhuliao_voice_done = false;
	
	MediaPlayer player_oil;
	MediaPlayer player_zhuliao;
	MediaPlayer player_fuliao;
	MediaPlayer player_stop;
	private int zhuliao_i;
	private int fuliao_i;
	
	static Bitmap canvas_bmp = null;
	static Bitmap simple_bkg_bmp = null;
	static ByteBuffer background_buffer; // 缓冲，每次绘制后canvas_bmp会被修改， 下次绘制时使用该buffer把canvas_bmp还原到初始状态
	static ByteBuffer simple_background_buffer;
	
	int width     = 0;
	int height    = 0;
	int x_start   = 0; // 横轴起点，预热的x轴
	int x_middle  = 0; // 主料和辅料分界x轴位置
	int x_end     = 0; // 辅料结束的x
	
	int y_min     = 0; // 温度曲线的最高和最低点
	int y_max     = 0;
	
	int temperature_x = 0;
	int temperature_x_2 = 0;
	int temperature_y = 0;
	int time_x = 0;
	int time_y = 0;
	int zhuliao_time_x = 0;
	int zhuliao_time_y = 0;
	int fuliao_time_x  = 0;
	int fuliao_time_y  = 0;
	int jiaoban_x = 0;
	int jiaoban_y = 0;
	
	// 简洁界面的位置
	int simple_time_x = 0;
	int simple_time_x_4 = 0;
	int simple_time_y = 0;
	int simple_temperature_x = 0;
	int simple_temperature_x_2 = 0;
	int simple_temperature_y = 0;
	int simple_jiaoban_x = 0;
	int simple_jiaoban_y = 0;
	
	Rect img_tiny_rect = new Rect();
	Rect lock_rect = new Rect();
	
	ArrayList<Integer> jianban_angles = new ArrayList<Integer>();
	boolean jiaoban_goright = true;
	int jiaoban_current_pos = 0;
	int wait_count = 0;
	
	// 画设定的温度线，用绿色线
	private void draw_temp_baselin() {
		if (dish == null) {
			Log.v("CurStateActivity", "dishid=" + dish_id + " is not exist, skip drawing");
			return;
		}
		
		if (ui_changed) {
			ui_changed = false;
			if (!is_standard_ui && simple_background_buffer == null) {
				//canvas_bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
				//simple_background_buffer = ByteBuffer.allocate(canvas_bmp.getByteCount()); 
			}
		}
        
        if (canvas_bmp == null) {
        	Log.v("CurStateActivity", "canvas_bmp is null, create one");
        	canvas_bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888); // 每次都创建会的话导致OutOfMemoryError
        	background_buffer = ByteBuffer.allocate(canvas_bmp.getByteCount());
        	simple_bkg_bmp = Tool.get_res_bitmap(R.drawable.simple_bkg);
        }
        else {
        	background_buffer.position(0);
        	if (simple_background_buffer !=null ) simple_background_buffer.position(0);
            canvas_bmp.copyPixelsFromBuffer(background_buffer);
        }
        
        Canvas canvas = new Canvas(canvas_bmp);
        paint.setStyle(Paint.Style.STROKE); 
        paint.setAntiAlias(false); //去锯齿 
        paint.setStrokeWidth(3);

        float scale = 1; //(float) (480.0 / width * 0.7);
        if (!is_standard_ui) {
        	// draw simple background
        	Rect rect = new Rect(0, 0, width, height);
        	canvas.drawBitmap(simple_bkg_bmp, null, rect, null);
        	
        	// 温度
        	int temp = ds.temp & 0xff;
            paint.setColor(Color.WHITE); // 字的颜色
            paint.setTextSize(220 * scale);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(12);
            paint.setAntiAlias(true);
            if (temp >= 100) {
            	canvas.drawText("" + temp, simple_temperature_x, simple_temperature_y, paint);
            }
            else {
            	canvas.drawText("" + temp, simple_temperature_x_2, simple_temperature_y, paint);
            }

            // jiaoban
    		paint.setTextSize(170 * scale);
            canvas.drawText("" + ds.jiaoban_speed, simple_jiaoban_x, simple_jiaoban_y, paint);
            
            // time
            
            int minites = ds.time / 60;
    		int seconds = ds.time - minites * 60;
    		String separator = seconds < 10 ? ":0" : ":";
    		//minites += 10;
    		if (minites < 10)
    			canvas.drawText(minites + separator + seconds, simple_time_x, simple_time_y, paint);
    		else {
    			paint.setTextSize(168 * scale);
                paint.setStrokeWidth(6);
    			canvas.drawText(minites + separator + seconds, simple_time_x_4, simple_time_y, paint);
    		}
            
            main.setImageBitmap(canvas_bmp);
        	return;
        }
        
        // 绿色的标准线段
        paint.setColor(Color.GREEN);
        int zhuliao_time = dish.zhuliao_time & 0xffff;
        float x_per_seconds_zhuliao = ((float)(x_middle - x_start)) / zhuliao_time;
        if (dish.fuliao_time == 0) {
        	x_per_seconds_zhuliao = ((float)(x_end - x_start)) / zhuliao_time;
        }
        final float y_per_temp = (float) ((float)(y_max - y_min) / 200.0);
        
        int zhuliao_temp = dish.zhuliao_temp & 0x00ff;
        int yend = (int) (y_min + y_per_temp*(200 - zhuliao_temp));
        canvas.drawLine(x_start, y_max, x_start, yend, paint);
        canvas.drawLine(x_start, yend, x_middle, yend, paint);
        
        int fuliao_temp = dish.fuliao_temp & 0x00ff;
        if (dish.fuliao_time == 0) fuliao_temp = zhuliao_temp;
        int yend2 = (int) (y_min + y_per_temp*(200 - fuliao_temp));
        canvas.drawLine(x_middle, yend, x_middle, yend2, paint);
        canvas.drawLine(x_middle, yend2, x_end, yend2, paint);
        
        //temperature
        int temp = ds.temp & 0xff;
        paint.setColor(Color.rgb(246, 221, 53)); // 字的颜色
        paint.setTextSize(110 * scale);
        paint.setStyle(Paint.Style.FILL);
        if (temp >= 100) {
        	canvas.drawText("" + temp, temperature_x, temperature_y, paint);
        }
        else {
        	canvas.drawText("" + temp, temperature_x_2, temperature_y, paint);
        }
        
        // total time
        int minites = ds.time/60;
		int seconds = ds.time - minites * 60;
		String separator = seconds < 10 ? ":0" : ":";
		String minites_prefix =  minites < 10 ? "0" : "";
        canvas.drawText(minites_prefix + minites + separator + seconds, time_x, time_y, paint);

        // jiaoban
        paint.setTextSize(90 * scale);
        canvas.drawText(this.jiaoban_str.get(Math.max(0, ds.jiaoban_speed-1)), jiaoban_x, jiaoban_y, paint);
        
        // dish name
        final int name_x = (int) (130.0/Constants.UI_WIDTH * width);
        final int name_y = (int) (60.0/Constants.UI_HEIGHT * height);
        paint.setTextSize(100 * scale);
        paint.setColor(Color.rgb(166, 246, 9));
        canvas.drawText(dish.name_chinese, name_x, name_y, paint);
        
        // zhuliao time
        paint.setColor(Color.rgb(115, 115, 115));
        paint.setTextSize(45 * scale);
        minites = dish.zhuliao_time / 60;
        seconds = dish.zhuliao_time - minites * 60;
        separator = seconds < 10 ? ":0" : ":";
        minites_prefix =  minites < 10 ? "0" : "";
        canvas.drawText(minites_prefix + minites + separator + seconds, zhuliao_time_x, zhuliao_time_y, paint);
        
        // fuliao time
        minites = dish.fuliao_time / 60;
        seconds = dish.fuliao_time - minites * 60;
        separator = seconds < 10 ? ":0" : ":";
        minites_prefix =  minites < 10 ? "0" : "";
        canvas.drawText(minites_prefix + minites + separator + seconds, fuliao_time_x, fuliao_time_y, paint);
        
	    // dish tiny image
	    if (dish.isAppBuiltIn() && dish.img_bmp == null) {
        	dish.img_bmp = Tool.decode_res_bitmap(dish.img, this);
        }
        canvas.drawBitmap(dish.img_bmp, null, img_tiny_rect, null);
        
        // 解锁开锁的图标
        Bitmap lock_bmp = ds.is_locked() ? Tool.get_res_bitmap(R.raw.locked) : Tool.get_res_bitmap(R.raw.unlock);
        canvas.drawBitmap(lock_bmp, null, lock_rect, null);
        
        // add oil
        int cur_temp = ds.temp & 0xff;
        if (data.size() > 0) cur_temp = data.get(data.size() - 1);
        else cur_temp = 0;
        int zhu_temp = dish.zhuliao_temp & 0x00ff;
        
        //Log.v("CurStateActivity", "cur_temp =" + cur_temp); 
        if (state == STATE_HEATING && cur_temp > 90) {
        	zhuliao_voice_done = false;
        	state = this.STATE_ADD_OIL;
        } else if (state == STATE_ADD_OIL && cur_temp > zhu_temp - 10) {
        	zhuliao_i = data.size();
        	zhuliao_voice_done = false;
        	state = this.STATE_ZHULIAO;
        } else if (state == STATE_ZHULIAO && data.size() > zhuliao_i + 10) { // 5秒闪烁提示图片
        	state = STATE_ZHULIAO_TISHI_DONE;
        } else if (state == STATE_ZHULIAO_TISHI_DONE && dish.fuliao_time != 0 && data.size() > zhuliao_i + 20) { // 保证主料的语音已经播放完，20为10秒
        	Log.v("CurStateActivity", "ds.time = " + ds.time + "dish.fuliao_time = " + dish.fuliao_time);
        	int time_left = ds.time & 0xffff;
        	if (time_left < (dish.fuliao_time & 0xffff) + 10) {// 在开始辅料时间10秒前
        		fuliao_i = data.size();
        		zhuliao_voice_done = false;
        		state = STATE_FULIAO;
        	}
        } else if (state == STATE_FULIAO && data.size() > fuliao_i + 10) { // 5秒闪烁提示图片
        	state = STATE_FULIAO_DONE;
        }
        
        float img_y_per_temp = (float) ((166.0-159) / 20); // 159.0 ---- 180℃   166.0 ---- 160℃
        // 加油提示语和图片
        if (state == STATE_ADD_OIL && ds.working_state != Constants.MACHINE_WORK_STATE_STOP 
        		&& (!zhuliao_voice_done || cur_temp < 110 && data.size() % 2 == 0))
        {
        	if (!zhuliao_voice_done) {
        		zhuliao_voice_done = true;
        		
        		int voice_resid = R.raw.voice_add_oil_qgl_chn;
        		if (dish.qiangguoliao == 0) voice_resid = R.raw.voice_add_oil_chn;
	        	player_oil = MediaPlayer.create(this, voice_resid);
	        	player_oil.start();
        	}
        	
        	int img_resid = 0;
        	Rect img_rect = new Rect();
        	img_rect.left    = (int) (198.0/Constants.UI_WIDTH * width);
        	img_rect.top    = (int) (200.0/Constants.UI_HEIGHT * height);
	        img_rect.bottom  = (int) (265.0/Constants.UI_HEIGHT * height);
        	if (dish.qiangguoliao == 0)  {
        		img_resid = R.raw.add_oil_chn;
		        img_rect.right   = (int) (320.0/Constants.UI_WIDTH * width);
        	}
	        else {
		        img_rect.right   = (int) (430.0/Constants.UI_WIDTH * width);
        		img_resid = R.raw.add_oil_qgl_chn;
        	}
	        canvas.drawBitmap(Tool.get_res_bitmap(img_resid), null, img_rect, null);
        }
        
        // 主料提示语和图片
        if (state == STATE_ZHULIAO && ds.working_state != Constants.MACHINE_WORK_STATE_STOP
        		&& (!zhuliao_voice_done || cur_temp < zhu_temp + 10 && data.size() % 2 == 0))
        {
        	int voice_resid = 0;
        	int img_resid = 0;
        	Rect img_rect = new Rect();
        	img_rect.left   = (int) (192.0/Constants.UI_WIDTH * width); 
        	img_rect.right  =  0;
        	img_rect.top    = (int) ((166.0 + ((160 - zhuliao_temp) * img_y_per_temp))/Constants.UI_HEIGHT * height); 
	        img_rect.bottom = (int) (245.0/Constants.UI_HEIGHT * height);
	        
    		if ((dish.water == 0 || dish.water == 2) && dish.fuliao_time != 0) {
    			voice_resid = R.raw.voice_add_zhuliao_chn;
    			img_resid = R.raw.add_zhuliao_chn;
    			img_rect.right  = (int) (332.0/Constants.UI_WIDTH * width);
    		}
    		else if ((dish.water == 0 || dish.water == 2) && dish.fuliao_time == 0){
    			voice_resid = R.raw.voice_add_zhuliao_tiaoliao_chn;
    			img_resid = R.raw.add_zhuliao_tiaoliao_chn;
    			img_rect.right  = (int) (446.0/Constants.UI_WIDTH * width);
    		}
    		else if (dish.water == 1 && dish.fuliao_time == 0){
    			voice_resid = R.raw.voice_add_zhuliao_water_tiaoliao_chn;
    			img_resid = R.raw.add_zhuliao_water_tiaoliao_chn;
    			img_rect.right  = (int) (468.0/Constants.UI_WIDTH * width);
    			img_rect.bottom = (int) (250.0/Constants.UI_HEIGHT * height);
    		}
    		else if (dish.water == 1 && dish.fuliao_time != 0){
    			voice_resid = R.raw.voice_add_zhuliao_water_chn;
    			img_resid = R.raw.add_zhuliao_water_chn;
    			// 加主料再加水的图片中的箭头比其他的长
    			img_rect.left   = (int) (195.0/Constants.UI_WIDTH * width);
    			img_rect.top    = (int) (170.0/Constants.UI_HEIGHT * height);
    			img_rect.right  = (int) (416.0/Constants.UI_WIDTH * width);
    			img_rect.bottom = (int) (255.0/Constants.UI_HEIGHT * height);
    		}
    		
        	if (!zhuliao_voice_done) {
        		zhuliao_voice_done = true;
        		player_zhuliao = MediaPlayer.create(CurStateActivity.this, voice_resid);
        		//player.stop();
        		player_zhuliao.start();
        	}
        
	        canvas.drawBitmap(Tool.get_res_bitmap(img_resid), null, img_rect, null);
        }
        
        // 辅料提示语和图片
        if (state == STATE_FULIAO && ds.working_state != Constants.MACHINE_WORK_STATE_STOP
        		&& (!zhuliao_voice_done || data.size() % 2 == 0))
        {
        	int voice_resid = 0;
        	int img_resid = 0;
        	Rect img_rect = new Rect();
        	img_rect.left   = (int) (239.0/Constants.UI_WIDTH * width);
        	img_rect.right  = 0;
        	img_rect.top    = (int) ((166.0 + ((160 - zhuliao_temp) * img_y_per_temp))/Constants.UI_HEIGHT * height);
	        img_rect.bottom = (int) (247.0/Constants.UI_HEIGHT * height);
	        
	        if (dish.water != 2) {
	        	voice_resid = R.raw.voice_add_fuliao_tiaoliao_chn;
    			img_resid = R.raw.add_fuliao_tiaoliao_chn;
    			img_rect.right  = (int) (421.0/Constants.UI_WIDTH * width);
	        }
	        else if (dish.water == 2) {
	        	voice_resid = R.raw.voice_add_fuliao_water_tiaoliao_chn;
    			img_resid = R.raw.add_fuliao_water_tiaoliao_chn;
    			img_rect.left   = (int) (235.0/Constants.UI_WIDTH * width);
    			img_rect.right  = (int) (441.0/Constants.UI_WIDTH * width);
	        }

	        
        	if (!zhuliao_voice_done) {
        		zhuliao_voice_done = true;
        		player_fuliao = MediaPlayer.create(CurStateActivity.this, voice_resid);
        		player_fuliao.start();
        	}
        	
	        canvas.drawBitmap(Tool.get_res_bitmap(img_resid), null, img_rect, null);
        }

        // temp curve
        if (ds.working_state != Constants.MACHINE_WORK_STATE_STOP) {
	        paint.setColor(Color.RED);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeWidth(15);
			
			zhuliao_index = data.size();
			for(int i=0; i< data.size(); i++){ 
				if (data.get(i) > zhuliao_temp) {
					zhuliao_index = i;
					break;
				}
			}
			
	        for(int i=0; i< data.size(); i++){ 
	        	int tempx = x_start;
	        	int tempy = (int) (y_min + y_per_temp*(200 - data.get(i)));
	        	//int tempy = ymax;
	        	if (i < zhuliao_index) {
	        		tempx = x_start;
	        	} else {
	        		//Log.v("CurStateActivity", "zhuliao_index = " + zhuliao_index + ", i = " + i);
	        		tempx = x_start + (int)((i - zhuliao_index) * x_per_seconds_zhuliao/2);
	        	}
	        	
	        	//Log.v("CurStateActivity", "tempx = " + tempx + ", tempy = " + tempy);
	        	if (tempx < x_end && tempy > y_min) {
	        		canvas.drawPoint(tempx, tempy, paint);
	        	}
	        } 
	        if (data.size() > 0) {
				Log.v("CurStateActivity", "data[" + (data.size() - 1) + "]=" + data.get(data.size()-1));
				//draw_index = data.size() - 1;
	        }
        }
        
        // jiaoban stick animation
        if (ds.jiaoban_speed > 1) {
	        Path path = new Path();
	        int left = (int) (31.0/480 * width);
	        int right = (int) (120.0/480 * width);
	        int top = (int) (169.0/272 * height);
	        int bottom = (int) (260.0/272 * height);
	        RectF oval = new RectF (left, top, right, bottom); 
	        path.addArc(oval, 0.0f, 360.0f);
	        path.close();
	        canvas.clipPath(path);
	        
	        paint.setColor(Color.rgb(177, 31, 19));
	        //paint.setAntiAlias(true);
	        float cx = (float) (76.0/480 * width);
	        float cy = (float) (214.0/272 * height);
	        float bx = cx;
	        float by = (float) (261.0/272 * height);
		    
	        float left_up_x = (float) (72.0/480 * width);
	        float left_up_y = cy;
	        float right_up_x = (float) (80.0/480 * width);
	        float right_up_y = left_up_y;
	        float left_bottom_x = (float) (68.0/480 * width);
	        float left_bottom_y = (float) (258.0/272 * height);
	        float right_bottom_x = (float) (84.0/480 * width);
	        float right_bottom_y = left_bottom_y;
	        
	        float arc_up_radius = cx - left_up_x;
	        RectF arc_up = new RectF(left_up_x, cy - arc_up_radius, right_up_x, cy + arc_up_radius);
	        float arc_bottom_height = by - left_bottom_y;
	        RectF arc_bottom = new RectF(left_bottom_x, left_bottom_y - arc_bottom_height, right_bottom_x, left_bottom_y + arc_bottom_height);
	        float green_boll_radius = (float) (6.5/480 * width);
	        
	        canvas.rotate(jianban_angles.get(jiaoban_current_pos), cx, cy);
	        // 画红色的搅拌棒
	        Path stick = new Path();
	        stick.moveTo(left_up_x, left_up_y);
	        stick.arcTo(arc_up, 180, 180);
	        stick.lineTo(right_bottom_x, right_bottom_y);
	        stick.arcTo(arc_bottom, 0, 180);
	        stick.close();
	        canvas.drawPath(stick, paint);
	        // 画绿色的小球
	        paint.setColor(Color.rgb(172, 254, 62));
	        if (jiaoban_goright) {
	        	canvas.drawCircle(right_bottom_x + green_boll_radius, left_bottom_y - green_boll_radius - (float)2.5/480*width, green_boll_radius, paint);
	        } else {
	        	canvas.drawCircle(left_bottom_x - green_boll_radius, left_bottom_y - green_boll_radius - (float)2.5/480*width, green_boll_radius, paint);
	        }
	        
	        Log.v("CurState", "jiaoban_current_pos="+jiaoban_current_pos+",wait_count=" + wait_count+",goright=" + jiaoban_goright);
	        
	        if (ds.working_state == Constants.MACHINE_WORK_STATE_COOKING) {
		        if ((jiaoban_current_pos == 0 || jiaoban_current_pos == jianban_angles.size() - 1)  && wait_count > 0) {
		        }
		        else 
		        {
			        jiaoban_current_pos += jiaoban_goright ? 1 : -1;
			        if (jiaoban_current_pos == jianban_angles.size() - 1) {
			        	jiaoban_goright = false;
			        } else if (jiaoban_current_pos == 0) {
			        	jiaoban_goright = true;
			        }
		        }
		        
		        if (wait_count > 0) { // 控制搅拌速度
		        	-- wait_count;
		        }
		        else if (wait_count == 0 && (jiaoban_current_pos == 0 || jiaoban_current_pos == jianban_angles.size() - 1)) {
		        	wait_count = 8 - ds.jiaoban_speed;
		        }
	        }
        } //if (ds.jiaoban_speed > 1)

        main.setImageBitmap(canvas_bmp);
	}
	
	boolean waiting = false;
	int count = 0;
	int stopcount = 0;
	int zhuliao_index = 0;
	//int draw_index = 0;
	
	public int dip2px(float dipValue){   
        final float scale = this.getResources().getDisplayMetrics().density;   
        return (int)(dipValue * scale + 0.5f);   
	}

	public void update_seekbar() {
		int pos = bar.getProgress();
		if (selectedRBtn.getId() == R.id.temp) {
			pos = (this.temp & 0xff)/2;
			Log.v("CurStateActivity", "now temp=" + pos);
		} else if (selectedRBtn.getId() == R.id.jiaoban) {
			pos = (int) ((float)(this.jiaoban_speed) / 8 * 100);
		} else if (selectedRBtn.getId() == R.id.time) {
			pos = (int) ((float)(ds.time) / 3599 * 100);
			Log.v("CurStateActivity", "now time=" + pos);
		}
		
		bar.setProgress(pos);
		bar.onSizeChanged(bar.getWidth(), bar.getHeight(), 0, 0);
	}

	boolean is_changing_seekbar = false;
	
	 /**
     * 当用户开始滑动进度条时调用该方法
     */
	@Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.v("CurStateActivity", "onStartTrackingTouch  start--->"+"+seekBar="+seekBar.getProgress());
        is_changing_seekbar = true;
        selectedRBtn.setTextColor(Color.rgb(255, 0, 0));
    }
    /**
     * 当用户结束滑动是调用该方法
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.v("TAG", "onStopTrackingTouch  end--->"+"+seekBar="+seekBar.getProgress());
        Message msg = new Message();  
        msg.what = 0x345;  
        Package data = new Package(Package.Set_Param);
        msg.obj = data.getBytes();
        tcpclient.sendMsg(msg); 
        
        selectedRBtn.setTextColor(Color.rgb(0, 0, 0));
        
        is_changing_seekbar = false;
        zhuliao_index = 0;
        
        //this.handler.sendEmptyMessage(0x234);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
		// TODO Auto-generated method stub
		Log.v("CurStateActivity", "onProgressChanged-->seekBar="+seekBar.getId() +"progress="+progress+"fromUser="+arg2);
		//非用户操作，直接返回
		if (!arg2) return;
		
		if (selectedRBtn.getId() == R.id.temp) {
			this.temp = (byte) (progress*2);
		} else if (selectedRBtn.getId() == R.id.jiaoban) {
			this.jiaoban_speed = (byte) ((((float)(progress)/100) * 8) + 1);
		} else if (selectedRBtn.getId() == R.id.time) {
			ds.time = (short) (((float)(progress)/100) * 3599);
		}
		
		this.modify_state = (byte)0xE0;
		this.updateall();
	}
	
	void updateall() {
//		if (selectedRBtn.getId() == R.id.temp) {
//			selectedRBtn.setText(" 温度 \n " + (this.temp & 0xff));
//		} else if (selectedRBtn.getId() == R.id.jiaoban) {			
//			selectedRBtn.setText(" 搅拌  " + jiaoban_speed);
//		} else if (selectedRBtn.getId() == R.id.time) {
//			int minites = this.time/60;
//			int seconds = this.time - minites * 60;
//			selectedRBtn.setText(" 时间\n " + minites + ":" + seconds);
//		}
	}
	
	public Handler getHandler() {
    	return this.handler;
    }
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    if (keyCode == KeyEvent.KEYCODE_BACK) {
//	        moveTaskToBack(true);
//	        return true;
//	    }
//	    return super.onKeyDown(keyCode, event);
//	}
	
	@Override  
    protected void onPause() {  
        super.onPause(); 
        Log.v("CurStateActivity", "onPause");  
    }  
    @Override  
    protected void onStop() {  
        super.onStop();  
        Log.v("CurStateActivity", "onStop");  
    }  
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //if (canvas_bmp != null) canvas_bmp.recycle();
        Log.v("CurStateActivity", "onDestroy");  
    }
}
