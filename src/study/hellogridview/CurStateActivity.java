package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class CurStateActivity extends Activity implements OnSeekBarChangeListener {
	public short time = 260;
	public byte temp = (byte) 180;
	public byte jiaoban_speed = 1;
	public int dish_id = 1;
	public byte modify_state = (byte) 0xE0; //时间、温度、搅拌、控制。。。是否被用户改动过的标识位，最高字节为时间，以此类推
	public byte control = 3;// 0表示开始炒菜，1表示暂停，2表示取消，3表示解锁
	
	public final String net_mode = "AP";// wifi模块的工作模式：AP或者STA
	public final int MAX_TIME = 3599; // in seconds
	
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
	
	Handler handler;
	
	TCPClient tcpclient;
	
	DeviceState ds = DeviceState.getInstance();
	
	public static int t = 50;

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
			jiaoban_str.add("8连续搅拌");
		}
		
		handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == 0x123) {  
                    // 将读取的内容追加显示在文本框中  
                    //show.append("\n" + msg.obj.toString());  
                	
                	Log.v("CurStateActivity", "got Machine state event time=" + ds.time + "temp=" + (ds.temp & 0x00ff) + "jiaoban=" + ds.jiaoban_speed);
                	synchronized (this) {
	                	CurStateActivity.this.time = ds.time;
	                	//CurStateActivity.this.temp = ds.temp;
	                	CurStateActivity.this.jiaoban_speed = ds.jiaoban_speed;
	                	CurStateActivity.this.dish_id = ds.dishid & 0xffff;
                	}
                	
                	CurStateActivity.this.update_seekbar();
                	CurStateActivity.this.update_radio();
                	
                	int MaxDataSize = 800;
                	if(ds.working_state != 0x02) {//待机中
                		if (ds.working_state == 0x01) {
                			CurStateActivity.this.jiaoban_speed = 1;
                		}
                		else if (data.size() < MaxDataSize){ 
                			data.add(Math.min(200, (int)(ds.temp & 0x00ff)));
                			//t += 5; data.add(Math.min(190, t));
                			
                		}
                	} else {
                		CurStateActivity.this.jiaoban_speed = 1;
                		state = STATE_HEATING;
                		t = 50;
                		data.clear();
                	}
                	draw_temp_baselin();
                	
                } else if (msg.what == 0x234) {
                	//main.invalidate();
                	draw_temp_baselin();
                	//draw_temp_curve();
                } else if (msg.what == 0x345) {
                	//main.invalidate();
                	
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
	        			CurStateActivity.this.jiaoban_speed = (byte) Math.min(8, CurStateActivity.this.jiaoban_speed);
	        			CurStateActivity.this.jiaoban_speed = (byte) Math.max(1, CurStateActivity.this.jiaoban_speed);
	        		} else if (selectedRBtn.getId() == R.id.time) {
	        			Log.v("CurStateActivity", "ds.time =" + ds.time);
	        			ds.time = (short) (ds.time + i*10);
	        			ds.time = (short) Math.min(MAX_TIME, ds.time);
	        			ds.time = (short) Math.max(0, ds.time);
	        			Log.v("CurStateActivity", "CurStateActivity set time = " + ds.time);
	        		}
	            	CurStateActivity.this.updateall();
	            	CurStateActivity.this.update_seekbar();
	            	CurStateActivity.this.modify_state = (byte)0xE0;
	            	CurStateActivity.this.onStopTrackingTouch(CurStateActivity.this.bar);
	            	
	            	selectedRBtn.setTextColor(Color.rgb(0, 0, 0));
	                break;
	        	}
	            return true;  
	        }  
	    } 
		
		add = (ImageView) findViewById(R.id.add);
		add.setOnTouchListener(new PicOnTouchListener(1));
		
		minus = (ImageView) findViewById(R.id.minus);
		minus.setOnTouchListener(new PicOnTouchListener(-1));
		
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
				CurStateActivity.this.update_radio();
			}
		});
		
		//android:src="@drawable/state_bg"
		main = (ImageView) findViewById(R.id.main);
		main.setBackgroundResource(R.drawable.state_bg);
		
		start_pause = (ImageView) findViewById(R.id.start_pause);
		//start_pause.setOnTouchListener(new PicOnTouchListener(2));
		start_pause.setOnClickListener(new OnClickListener() {  
      	  
            @Override  
            public void onClick(View v) {  
            	if (ds.working_state == 0x00) CurStateActivity.this.control = 0x01;
            	else if (ds.working_state == 0x01) CurStateActivity.this.control = 0x00;
            	else if (ds.working_state == 0x02) CurStateActivity.this.control = 0x00;//return;
            	modify_state = (byte) 0x10;
            	
            	Message msg = new Message();  
                msg.what = 0x345;  
                Package data = new Package(Package.Set_Param);
                msg.obj = data.getBytes();
                tcpclient.sendMsg(msg); 
            }  
        }); 
		
        draw_temp_baselin();       

	} // oncreate

	public final static int STATE_HEATING = 0;
	public final int STATE_ADD_OIL = 1;
	public final int STATE_ZHULIAO = 2;
	public final int STATE_FULIAO = 3;
	
	public static int state = STATE_HEATING;
	private static boolean zhuliao_voice_done = false;
	
	MediaPlayer player;
	MediaPlayer player1;
	private int zhuliao_i;
	
	Bitmap last_bmp = null;
	
	// 画设定的温度线，用绿色线
	private void draw_temp_baselin() {
		paint.setStyle(Paint.Style.STROKE); 
        paint.setAntiAlias(true); //去锯齿 
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        
        int width = dip2px(465/*403*/);
        int height = dip2px(272/*240*/);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888); // 每次都创建会导致OutOfMemoryError
        Canvas canvas = new Canvas(bitmap);
        //Log.v("CurStateActivity", "canvas.width = " + canvas.getWidth() + "canvas.height = " + canvas.getHeight());
        //canvas.drawText("原先的画图区域--红色部分", 50, 100, paint) ;
        //canvas.drawColor(Color.RED);
        //canvas.drawLine(0, 0, width, height ,paint); 
        final int x = (int) (169.0/480 * width);
        final int x2 = (int) (286.0/480 * width);
        final int x3 = (int) (428.0/480 * width);
        
        int zhuliao_time = Dish.getDishById(dish_id).zhuliao_time;
        float x_per_seconds_zhuliao = ((float)(x2 - x))/ zhuliao_time;
        if (Dish.getDishById(dish_id).fuliao_time == 0) {
        	x_per_seconds_zhuliao = (x3 - x)/ zhuliao_time;
        }
        
        int ymax = (int) (252.0/272 * height);
        int ymin = (int) (144.0/272 * height);
        float y_per_temp = (float) ((float)(ymax - ymin) / 200.0);
        
        int zhuliao_temp = Dish.getDishById(dish_id).zhuliao_temp & 0x00ff;
        int yend = (int) (ymin + y_per_temp*(200 - zhuliao_temp));
        canvas.drawLine(x, ymax, x, yend ,paint);
        canvas.drawLine(x, yend, x2, yend, paint);
        
        int fuliao_temp = Dish.getDishById(dish_id).fuliao_temp & 0x00ff;;
        int yend2 = (int) (ymin + y_per_temp*(200 - fuliao_temp));
        canvas.drawLine(x2, yend, x2, yend2, paint);
        canvas.drawLine(x2, yend2, x3, yend2, paint);
        
        //temperature
        final int temp_x = (int) (205.0/480 * width);
        final int temp_y = (int) (105.0/272 * height);
        paint.setColor(Color.GREEN);
        paint.setTextSize(60);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("" + (ds.temp & 0xff), temp_x, temp_y, paint) ;
        
        // time
        final int time_x = (int) (367.0/480 * width);
        final int time_y = (int) (105.0/272 * height);
        int minites = ds.time/60;
		int seconds = ds.time - minites * 60;
		String separator = ":";
		if (seconds < 10) separator += "0";
        canvas.drawText(minites + separator + seconds, time_x, time_y, paint) ;
        
        // jiaoban
        final int jiaoban_x = (int) (20.0/480 * width);
        final int jiaoban_y = (int) (162.0/272 * height);
        paint.setTextSize(40);
        canvas.drawText(this.jiaoban_str.get(ds.jiaoban_speed -1), jiaoban_x, jiaoban_y, paint) ;
        
        // dish name
        final int name_x = (int) (130.0/480 * width);
        final int name_y = (int) (60.0/272 * height);
        paint.setTextSize(100);
        paint.setColor(Color.rgb(166, 246, 9));
        canvas.drawText(Dish.getDishById(dish_id).name_chinese, name_x, name_y, paint) ;

	    // dish tiny image
	    Rect img_rect = new Rect();
	    img_rect.left = (int) (368.0/480 * width);
	    img_rect.right = (int) (469.0/480 * width);
	    img_rect.top = (int) (8.0/272 * height);
	    img_rect.bottom = (int) (77.0/272 * height);
	    
        Bitmap bmp;
        if (Dish.getDishById(dish_id).isAppBuiltIn()) {
        	BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true; 
        	bmp = BitmapFactory.decodeResource(this.getResources(), Dish.getDishById(dish_id).img, options);
        }
        else {
        	bmp = Dish.getDishById(dish_id).img_bmp;
        }
        canvas.drawBitmap(bmp, null, img_rect, null);
        
        // add oil
        int cur_temp = ds.temp & 0xff;
        if (data.size() > 0) cur_temp = data.get(data.size()-1);
        else cur_temp = 0;
        int zhu_temp = this.temp & 0x00ff;
        
        //Log.v("CurStateActivity", "cur_temp =" + cur_temp); 
        if (state == STATE_HEATING && cur_temp > 90) {
        	zhuliao_voice_done = false;
        	state = this.STATE_ADD_OIL;
        } else if (state == STATE_ADD_OIL && cur_temp > zhu_temp - 10) {
        	zhuliao_i = data.size();
        	zhuliao_voice_done = false;
        	state = this.STATE_ZHULIAO;
        } else if (state == STATE_ZHULIAO && data.size() > zhuliao_i + 30) {
        	//zhuliao_voice_done = false;
        	state = this.STATE_FULIAO;
        }
        
        if (state == STATE_ADD_OIL && ds.working_state != 0x02 
        		&& (!zhuliao_voice_done || cur_temp < 110 && data.size() % 2 == 0))
        {
        	if (!zhuliao_voice_done) {
        		zhuliao_voice_done = true;
	        	player1 = MediaPlayer.create(this, R.raw.add_oil_voice);
	        	//player1.stop();
	            player1.start();
        	}
        	
	        img_rect = new Rect();
	        img_rect.left = (int) (168.0/480 * width);
	        img_rect.right = (int) (353.0/480 * width);
	        img_rect.top = (int) (198.0/272 * height);
	        img_rect.bottom = (int) (262.0/272 * height);
        
	        Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.add_oil);
	        canvas.drawBitmap(bmp2, null, img_rect, null);
        }
        
        // add zhuliao_tiaoliao
        if (state == STATE_ZHULIAO && ds.working_state != 0x02 
        		&& (!zhuliao_voice_done || cur_temp < zhu_temp + 10 && data.size() % 2 == 0))
        {
        	if (!zhuliao_voice_done) {
        		zhuliao_voice_done = true;
        		player = MediaPlayer.create(CurStateActivity.this, R.raw.zhuliao_tiaoliao_voice);
        		//player.stop();
				player.start();
        	}
        	
        	
	        img_rect = new Rect();
	        img_rect.left = (int) (168.0/480 * width);
	        img_rect.right = (int) (400.0/480 * width);
	        img_rect.top = (int) (154.0/272 * height);
	        img_rect.bottom = (int) (240.0/272 * height);
        
	        Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.zhuliao_tiaoliao);
	        canvas.drawBitmap(bmp2, null, img_rect, null);
        }

        // temp curve
        if (ds.working_state != 0x02) {
	        paint.setColor(Color.RED);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeWidth(15);
			
			zhuliao_index = data.size();
			for(int i=0; i< data.size(); i++){ 
				if (data.get(i) > (this.temp & 0x00ff)) {
					zhuliao_index = i;
					break;
				}
			}
	        for(int i=0; i< data.size(); i++){ 
	        	
	        	int tempx = x;
	        	int tempy = (int) (ymin + y_per_temp*(200 - data.get(i)));
	        	//int tempy = ymax;
	        	if (i < zhuliao_index) {
	        		tempx = x;
	        	} else {
	        		//Log.v("CurStateActivity", "zhuliao_index = " + zhuliao_index + ", i = " + i);
	        		tempx = x + (int)((i - zhuliao_index) * x_per_seconds_zhuliao/2);
	        	}
	        	
	        	//Log.v("CurStateActivity", "tempx = " + tempx + ", tempy = " + tempy);
	        	if (tempx < x3 && tempy > ymin) {
	        		canvas.drawPoint(tempx, tempy, paint);
	        	}
	        } 
	        if (data.size() > 0) {
				Log.v("CurStateActivity", "data[" + (data.size() - 1) + "]=" + data.get(data.size()-1));
				//draw_index = data.size() - 1;
	        }
        }
        
        // jiaoban
        if (true) {
	        int left = (int) (15.0/480 * width);
	        int right = (int) (93.0/480 * width);
	        int top = (int) (197.0/272 * height);
	        int bottom = (int) (255.0/272 * height);
	        Rect rect = new Rect (left, top, right, bottom) ;  
	      
		    //当前的画图区域为Rect裁剪的区域，而不是我们之前赋值的bitmap  
		    canvas.clipRect(rect); 
		    paint.setStyle(Paint.Style.FILL);
		    //canvas.drawColor(Color.YELLOW);
		    
		    int radius = (int) (5.0 * width/480);
		    int radius3 = (int) (49.0 * width/480);
		    int cx = (int) (54.0/480 * width);
		    int cy = (int) (203.0/272 * height);
		    
		    int cx2 = (int) (31.0/480 * width);
		    int cy2 = (int) (231.0/272 * height);
		    
		    int cx3 = (int) (45.0/480 * width);
		    int cy3 = (int) (237.0/272 * height);
		    
		    int cx4 = (int) (54.0/480 * width);
		    int cy4 = (int) (238.0/272 * height);
		    
		    int cx5 = (int) (63.0/480 * width);
		    int cy5 = (int) (237.0/272 * height);
		    
		    int cx6 = (int) (77.0/480 * width);
		    int cy6 = (int) (231.0/272 * height);
		    paint.setColor(Color.RED);
		    canvas.drawCircle(cx, cy, radius, paint);
		    paint.setColor(Color.WHITE);
		    paint.setStyle(Paint.Style.STROKE);
		    paint.setStrokeWidth(10);
		    canvas.drawCircle(cx, cy, radius3, paint);
		    paint.setColor(Color.RED);
		    paint.setStyle(Paint.Style.FILL);
		    paint.setStrokeWidth(15);
		    
		    int radius2 = (int) (9.0 * width/480);
		    paint.setStrokeWidth(8);
		    
		    int bx = cx2;
		    int by = cy2;
		    ++count;
		    if (this.jiaoban_speed > 1 && ds.working_state == 0x00) {
			    if (goright) {
				    switch (degree) {
				    case 0 :
				    	bx = cx2;by = cy2;break;
				    case 1 :
				    	bx = cx3;by = cy3;break;
				    case 2 :
				    	bx = cx4;by = cy4;break;
				    case 3 :
				    	bx = cx5;by = cy5;break;
				    case 4 :
				    	bx = cx6;by = cy6; goright = false; break;
				    }
				    ++degree;
				    if (degree == 5 && waiting == false) {
				    	stopcount = count;
				    	waiting = true;
				    }
				    if (waiting) {
				    	if (count - stopcount < (8 - this.jiaoban_speed)) {
				    		goright = true;
				    		degree = 4;
				    	} else {
				    		waiting = false;
				    		degree = 5;
				    	}
			    	} 
			    } else {
			    	switch (degree) {
				    case 1 :
				    	bx = cx2;by = cy2;goright = true;break;
				    case 2 :
				    	bx = cx3;by = cy3;break;
				    case 3 :
				    	bx = cx4;by = cy4;break;
				    case 4 :
				    	bx = cx5;by = cy5;break;
				    case 5 :
				    	bx = cx6;by = cy6; goright = false; break;
				    }
				    --degree;
				    if (degree == 0 && waiting == false) {
				    	stopcount = count;
				    	waiting = true;
				    }
				    if (waiting) {
				    	if (count - stopcount < (8 - this.jiaoban_speed)) {
				    		goright = false;
				    		degree = 1;
				    	} else {
				    		waiting = false;
				    		degree = 0;
				    	}
			    	} 
			    }
		    }
		    canvas.drawCircle(bx, by, radius2, paint);
		   
		    canvas.drawLine(cx, cy, bx, by, paint);
        }

        
	    
        main.setImageBitmap(bitmap);
        //if (last_bmp != null) last_bmp.recycle();
        last_bmp = bitmap;
	}
	boolean waiting = false;
	int count = 0;
	int stopcount = 0;
	int zhuliao_index = 0;
	//int draw_index = 0;
	
	boolean goright = true;
	int degree = 0;
	
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
			pos = (int) ((float)(this.jiaoban_speed) / 7 * 100);
		} else if (selectedRBtn.getId() == R.id.time) {
			pos = (int) ((float)(ds.time) / 3599 * 100);
			Log.v("CurStateActivity", "now time=" + pos);
		}
		
		bar.setProgress(pos);
		bar.onSizeChanged(bar.getWidth(), bar.getHeight(), 0, 0);
	}

	public void update_radio() {
		if (!is_changing_seekbar) {
//			RadioButton btn = (RadioButton)this.findViewById(R.id.temp);
//			btn.setText(" 温度 \n " + (ds.temp & 0xff));
//			
//			btn = (RadioButton)this.findViewById(R.id.time);
//			Log.v("CurStateActivity", "now time=" + this.time);
//			int minites = ds.time/60;
//			int seconds = ds.time - minites * 60;
//			btn.setText(" 时间\n " + minites + ":" + seconds);
//			
//			btn = (RadioButton)this.findViewById(R.id.jiaoban);
//			btn.setText(" 搅拌  " + ds.jiaoban_speed);
	        
		}
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
        
        this.handler.sendEmptyMessage(0x234);
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
			this.jiaoban_speed = (byte) ((((float)(progress)/100) * 7) + 1);
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
        Log.v("CurStateActivity", "onDestroy");  
    }
}
