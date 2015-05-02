package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import cn.sharesdk.onekeyshare.OnekeyShare;
import study.hellogridview.Dish.Material;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MakeDishActivity extends Activity {
	
	ImageView makedish_img;
	TextView makedish_name;
	TextView makedish_brief;
	Button makedish_startcook;
	Button makedish_save;
	
	TextView add_zhuliao;
	//TextView zhuliao_content;
	
	TextView add_fuliao;
	TextView fuliao_content;
	
	TextView table_material_image_addrow;
	
	static File tempFile;
	Dish new_dish;
	
	TableLayout table_zhuliao_param;
	TableLayout table_fuliao_param;
	
	CheckBox zhuliao_water_cb;
	CheckBox fuliao_water_cb;
	
	TextView makedish_zhuliao_temp;
	TextView makedish_zhuliao_time;
	TextView makedish_zhuliao_jiaoban;
	
	TextView makedish_fuliao_temp;
	TextView makedish_fuliao_time;
	TextView makedish_fuliao_jiaoban;
	
	TextView makedish_oil_tv;
	TextView makedish_qiangguoliao;
	TextView water_fuliao;
	TextView water_fuliao_tv;
	
	public ImageView favorite;
	
	Button makedish_replace;
	Button makedish_upload;
	Button makedish_verify;
	ImageView makedish_share;
	ImageView makedish_shareto;
	
	ProgressBar progressBar1;
	
	//public int new_dish_id;
	boolean editable = true;
	
	private TableLayout tableLayout_zhuliao;
	private TableLayout tableLayout_fuliao;
	private TableLayout table_material;
	
	public LayoutInflater inflater;
	public View self_content_view;
	public Integer[] waters = new Integer[20];
	public Integer[] temps = new Integer[41];
	public Integer[] oils = new Integer[10];
	public Integer[] dishids = new Integer[12];
	public final String dish_names[] = {"","","","","","","","","","","",""};
	public final String jiaoban[] = {"0不搅拌", "1最慢速", "2较慢速", "3中慢速", "4中速", "5中快速", "6较快速", "7最快速", "8连续搅拌"};
	protected int current_cmd;
	private TextView makedish_delete;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("MakeDishActivity", "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_make_dish);
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		self_content_view = inflater.inflate(R.layout.activity_make_dish, null, false);
		for(int i = 0; i < waters.length; i++) {
			waters[i] = 5 * (i + 1);
		}
		for(int i = 0; i < temps.length; i++) {
			temps[i] = 120 + 2 * i;
		}
		for(int i = 0; i < oils.length; i++) {
			oils[i] = 10 * (i + 1);
		}
		for (int i = 0; i < dishids.length; ++i) {
			dishids[i] = i;
		}
		
		Intent intent = getIntent();
		int dish_id = intent.getIntExtra("dish_id", 0);
		Log.v("MakeDishActivity", "dish_id = " + dish_id);
		new_dish = Dish.getDishById(dish_id);
		//new_dish_id = dish_id;
		
		tcpClient = TCPClient.getInstance();
        tcpClient.set_makedishact(this);
		
		makedish_img = (ImageView) findViewById(R.id.makedish_img); 
		//makedish_img.setImageDrawable(new_dish.img_drawable);
		makedish_img.setImageBitmap(new_dish.img_bmp);
		makedish_img.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                MakeDishActivity.this.chooseDishImage(); 
            }  
        });
		
		makedish_name = (TextView) findViewById(R.id.makedish_name);
		makedish_name.setText(new_dish.name_chinese);
		makedish_name.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivity.this, EditActivity.class);
            	intent.putExtra("edit_title", "菜谱名称");
            	intent.putExtra("edit_content_input", new_dish.name_chinese);
            	startActivityForResult(intent, 7);
            }  
        });
		
		makedish_brief = (TextView) findViewById(R.id.makedish_brief); 
		//makedish_brief.setText("填写简介");
		
		makedish_startcook = (Button) findViewById(R.id.makedish_startcook);
		makedish_startcook.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	
                try {  
                    Message msg = new Message();  
                    msg.what = 0x345;  
                    Package data = new Package(Package.Send_Dish, MakeDishActivity.this.new_dish);
                    msg.obj = data.getBytes();
                    tcpClient.sendMsg(msg); 
                    
                    MakeDishActivity.this.resp_cmd108_count = 0;
                    
                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while(data.get_img_pkg(baos) && baos.size() != 0) {
                    	Log.v("MakeDishActivity", "img baos.size() = " + baos.size());
                    	Message msgtmp = new Message();  
                    	msgtmp.what = 0x345; 
                    	msgtmp.obj = baos;
                    	tcpClient.sendMsg(msgtmp); 
                    	baos = new ByteArrayOutputStream();
                    }
                } catch (Exception e) { 
                	e.printStackTrace();
                	Log.v("MakeDishActivity", "prepare package data exception");
                }  
            }  
        }); 
		
		zhuliao_water_cb = (CheckBox) findViewById(R.id.makedish_zhuliao_water);
		zhuliao_water_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            	Log.v("MakeDishActivity", "checkbox = " + arg1 + " arg0 = " + arg0);
            	if (new_dish.water != 1 && arg1) { // 需要加水，显示加水量
            		popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
                	final PopupWindow popWindow = new PopupWindow(popupView, 600, 800, true);
                	
                	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
                	ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivity.this, waters);
                	column_1.setViewAdapter(adapter);
                	column_1.setCurrentItem(waters.length / 2);
                	
                	Button sure = (Button) popupView.findViewById(R.id.makesure);
                	sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                        	zhuliao_water_cb.setTextColor(Color.BLACK);
                        	new_dish.water = 1;
                        	new_dish.water_weight = waters[column_1.getCurrentItem()];
                        	zhuliao_water_cb.setText("  加水 " + new_dish.water_weight + " 克");
                            popWindow.dismiss(); //Close the Pop Window
                        }
                    });
                	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            	}
            	else if (new_dish.water == 1 && !arg1){
            		zhuliao_water_cb.setText("  加水 ");
            		zhuliao_water_cb.setTextColor(Color.rgb(85, 85, 85));
            		new_dish.water = 0;
            	}
            	fuliao_water_cb.setVisibility(arg1 || new_dish.fuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
		
		fuliao_water_cb = (CheckBox) findViewById(R.id.makedish_fuliao_water);
		fuliao_water_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            	Log.v("MakeDishActivity", "checkbox = " + arg1);
            	if (new_dish.water != 2 && arg1) { // 需要加水，显示加水量
            		popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
                	final PopupWindow popWindow = new PopupWindow(popupView, 600, 800, true);
                	
                	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
                	ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivity.this, waters);
                	column_1.setViewAdapter(adapter);
                	column_1.setCurrentItem(waters.length / 2);
                	
                	Button sure = (Button) popupView.findViewById(R.id.makesure);
                	sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	fuliao_water_cb.setTextColor(Color.BLACK);
                        	new_dish.water = 2;
                        	new_dish.water_weight = waters[column_1.getCurrentItem()];
                        	fuliao_water_cb.setText("  加水 " + new_dish.water_weight + " 克");
                            popWindow.dismiss(); //Close the Pop Window
                        }
                    });
                	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            	}
            	else if (new_dish.water == 2 && !arg1){
            		fuliao_water_cb.setText("  加水 ");
            		fuliao_water_cb.setTextColor(Color.rgb(85, 85, 85));
            		new_dish.water = 0;
            	}
            	zhuliao_water_cb.setVisibility(arg1 || new_dish.zhuliao_content_map.isEmpty()? View.GONE : View.VISIBLE);
            }
        });
		
		// 消息处理
        handler = new Handler() {  
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == 0x123) {    
                	RespPackage rp = (RespPackage) msg.obj;
                	Log.v("MakeDishActivity", "got resp, cmdtype_head=" + (rp.cmdtype_head&0xff) + ", cmdtype_body=" + (rp.cmdtype_body&0xff));
                	if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 101) {
                		current_cmd = 101;
                	} else if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 104) {
                		current_cmd = 104;
                	} 
                	
                	if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 108) {
                		++ MakeDishActivity.this.resp_cmd108_count;
                		progressBar1.incrementProgressBy(1);
                	}
                	
                	Log.v("MakeDishActivity", "current_cmd=" + current_cmd + ", resp_cmd108_count=" + resp_cmd108_count);
                	if (MakeDishActivity.this.resp_cmd108_count == 5) { //目前图片都是分成5个帧传输的
                		MakeDishActivity.this.resp_cmd108_count = 0;
            			progressBar1.setVisibility(View.GONE);
            			progressBar1.setProgress(0);
                		if (current_cmd == 101) {
	                		Log.v("MakeDishActivity", "start cook dish(" + new_dish.dishid + ") done, go to CurStateActivity");
	        	        	Intent intent = new Intent(MakeDishActivity.this, CurStateActivity.class);
	        	        	intent.putExtra("dish_id", new_dish.dishid); 
	        	        	startActivity(intent);
                		} else if (current_cmd == 104) {
                			Toast.makeText(MakeDishActivity.this, "替换菜谱完成", Toast.LENGTH_SHORT).show();
                			TCPClient.getInstance().do_heartbeat();// 获取最新内置菜谱
                			Log.v("MakeDishActivity", " replace done");
                    	}
                	}
                }// if (msg.what == 0x123)
                else if (msg.what == Constants.MSG_ID_UPLOAD_RESULT) {
                	String state = (String)msg.obj;
                	if (state.equals("success")) {
                		MakeDishActivity.this.makedish_upload.setEnabled(false);
                		makedish_delete.setVisibility(View.GONE);
                		Toast.makeText(MakeDishActivity.this, "上传完成", Toast.LENGTH_SHORT).show();
                	}
                	else if (state.equals("fail")) {
                		Toast.makeText(MakeDishActivity.this, "上传失败，请重试", Toast.LENGTH_SHORT).show();
                	}
                	else {
                		// TODO progress bar 
                	}
                }
                else if (msg.what == Constants.MSG_ID_VERIFY_DONE) {
                	makedish_verify.setText("审核已通过");
                	makedish_verify.setEnabled(false);
                }
                else if (msg.what == Constants.MSG_ID_FAVORITE_DONE) {
                	Log.v("MakeDishActivity", "got favorite result, isFavorite ="  + Account.isFavorite(new_dish));
                	favorite.setImageResource(Account.isFavorite(new_dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72);
                }
            }  
        }; 
        
        makedish_zhuliao_temp = (TextView) findViewById(R.id.makedish_zhuliao_temp);
        makedish_zhuliao_temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,600,800,true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivity.this, temps);
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(((new_dish.zhuliao_temp & 0xff) - 120) / 2);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	new_dish.zhuliao_temp = temps[column_1.getCurrentItem()].byteValue();
                    	makedish_zhuliao_temp.setText(temps[column_1.getCurrentItem()] + "°C");
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_fuliao_temp = (TextView) findViewById(R.id.makedish_fuliao_temp);
        makedish_fuliao_temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,600,800,true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivity.this, temps);
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(((new_dish.fuliao_temp & 0xff) - 120) / 2);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	new_dish.fuliao_temp = temps[column_1.getCurrentItem()].byteValue();
                    	makedish_fuliao_temp.setText(temps[column_1.getCurrentItem()] + "°C");
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_zhuliao_time = (TextView) findViewById(R.id.makedish_zhuliao_time);
        makedish_zhuliao_time.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_2_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 900, 800, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	if (column_1 == null) Log.v("MakeDishActivity", "hours is null!");
            	column_1.setViewAdapter(new NumericWheelAdapter(MakeDishActivity.this, 0, 59));
            	column_1.setCyclic(true);
            	column_1.setCurrentItem(new_dish.zhuliao_time / 60);
            	
            	final WheelView column_2 = (WheelView) popupView.findViewById(R.id.column_2);
            	if (column_2 == null) Log.v("MakeDishActivity", "hours is null!");
            	column_2.setViewAdapter(new NumericWheelAdapter(MakeDishActivity.this, 0, 59));
            	column_2.setCyclic(true);
            	column_2.setCurrentItem(new_dish.zhuliao_time % 60);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	new_dish.zhuliao_time = (short) (column_1.getCurrentItem() * 60 + column_2.getCurrentItem());
                    	makedish_zhuliao_time.setText("" + column_1.getCurrentItem() + ":" + column_2.getCurrentItem());
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_fuliao_time = (TextView) findViewById(R.id.makedish_fuliao_time);
        makedish_fuliao_time.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_2_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 800, 700, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	if (column_1 == null) Log.v("MakeDishActivity", "hours is null!");
            	column_1.setViewAdapter(new NumericWheelAdapter(MakeDishActivity.this, 0, 59));
            	column_1.setCyclic(true);
            	column_1.setCurrentItem(new_dish.fuliao_time / 60);
            	
            	final WheelView column_2 = (WheelView) popupView.findViewById(R.id.column_2);
            	if (column_2 == null) Log.v("MakeDishActivity", "hours is null!");
            	column_2.setViewAdapter(new NumericWheelAdapter(MakeDishActivity.this, 0, 59));
            	column_2.setCyclic(true);
            	column_2.setCurrentItem(new_dish.fuliao_time % 60);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	new_dish.fuliao_time = (short) (column_1.getCurrentItem() * 60 + column_2.getCurrentItem());
                    	makedish_fuliao_time.setText("" + column_1.getCurrentItem() + ":" + column_2.getCurrentItem());
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_zhuliao_jiaoban = (TextView) findViewById(R.id.makedish_zhuliao_jiaoban);
        makedish_zhuliao_jiaoban.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,600,800,true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(MakeDishActivity.this, jiaoban);	
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(new_dish.zhuliao_jiaoban_speed & 0xff);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	makedish_zhuliao_jiaoban.setText(column_1.getCurrentItem() + "");
                    	new_dish.zhuliao_jiaoban_speed = (byte) Math.max(1, column_1.getCurrentItem());
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_fuliao_jiaoban = (TextView) findViewById(R.id.makedish_fuliao_jiaoban);
        makedish_fuliao_jiaoban.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,600,800,true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(MakeDishActivity.this, jiaoban);	
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(new_dish.fuliao_jiaoban_speed & 0xff);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	makedish_fuliao_jiaoban.setText(column_1.getCurrentItem() + "");
                    	new_dish.fuliao_jiaoban_speed = (byte) Math.max(1, column_1.getCurrentItem());
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_oil_tv = (TextView) findViewById (R.id.makedish_oil);
        makedish_oil_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 600, 800, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	column_1.setViewAdapter(new ArrayWheelAdapter<Integer>(MakeDishActivity.this, oils));
            	column_1.setCyclic(true);
            	column_1.setCurrentItem((new_dish.oil & 0xff) / 10 - 1);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	new_dish.oil = oils[column_1.getCurrentItem()].byteValue();
                    	makedish_oil_tv.setText("" + oils[column_1.getCurrentItem()]);
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_qiangguoliao = (TextView) findViewById(R.id.makedish_qiangguoliao);
        makedish_qiangguoliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivity.this, EditActivity.class);
            	intent.putExtra("edit_title", "炝锅料");
            	intent.putExtra("edit_content_input", new_dish.qiangguoliao_content);
            	startActivityForResult(intent, 2);
            }  
        });
        
        add_zhuliao = (TextView) findViewById(R.id.makedish_add_zhuliao);
//        zhuliao_content = (TextView) findViewById(R.id.makedish_zhuliao_content);
//        zhuliao_content.setText(new_dish.zhuliao_content);
//        if (new_dish.zhuliao_content.isEmpty()) zhuliao_content.setText("无");
        add_zhuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivity.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "主料");
            	//intent.putExtra("edit_content_input", new_dish.zhuliao_content);
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 3);
            }  
        });
        
        add_fuliao = (TextView) findViewById(R.id.makedish_add_fuliao);
//        fuliao_content = (TextView) findViewById(R.id.makedish_fuliao_content);
//        fuliao_content.setText(new_dish.fuliao_content);
//        if (new_dish.fuliao_content.isEmpty()) fuliao_content.setText("无");
        add_fuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivity.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "辅料");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 4);
            }  
        });
        
		// 主料表格
		tableLayout_zhuliao = (TableLayout) findViewById(R.id.table_zhuliao);
		tableLayout_zhuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivity.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "主料");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 3);
            }  
        });
		fill_table(tableLayout_zhuliao, new_dish.zhuliao_content_map);
		Log.v("MakeDishActivity", "zhuliao_content_map.size() = " + new_dish.zhuliao_content_map.size());
		Log.v("MakeDishActivity", "tableLayout_zhuliao.getChildCount() = " + tableLayout_zhuliao.getChildCount());
		
		table_zhuliao_param = (TableLayout) findViewById(R.id.table_zhuliao_param);
		table_fuliao_param = (TableLayout) findViewById(R.id.table_fuliao_param);
		
		// 辅料表格
		tableLayout_fuliao = (TableLayout) findViewById(R.id.table_fuliao);
		tableLayout_fuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivity.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "辅料");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 4);
            }  
        });
		fill_table(tableLayout_fuliao, new_dish.fuliao_content_map);
		
		table_material_image_addrow = (TextView) findViewById(R.id.table_material_image_addrow);
		table_material_image_addrow.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivity", "v.getId = " + v.getId());
            	Intent intent = new Intent(MakeDishActivity.this, ImageEditActivity.class);
            	intent.putExtra("edit_title", "备料图文");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 6);
            }  
        });
		
		table_material = (TableLayout) findViewById(R.id.table_material);
		fill_material_table(table_material, new_dish.prepare_material_detail);
		
		// 使用new_dish已有数据初始化
		this.init_param();
		
		makedish_replace = (Button) findViewById(R.id.makedish_replace);
		makedish_replace.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!new_dish.isVerifying() && !new_dish.isVerifyDone()) {
            		Log.v("MakeDishActivity", "can't replace before upload");
            		Toast.makeText(MakeDishActivity.this, "未上传的菜谱不能用来替换内置菜谱", Toast.LENGTH_SHORT).show();
            	}
            	
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 600, 800, true);
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	
            	if (DeviceState.getInstance().got_builtin == false) {
            		Toast.makeText(MakeDishActivity.this, "请先连接机器，获取内置菜谱", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	for (int i = 0; i < dishids.length; ++i) {
    				dishids[i] = 0xffff & DeviceState.getInstance().builtin_dishids[i];
    				dish_names[i] = Dish.getDishNameById(DeviceState.getInstance().builtin_dishids[i]);
    			}
            	
            	int id = new_dish.dishid;
            	if (Arrays.binarySearch(dishids, id) >= 0) {
            		Toast.makeText(MakeDishActivity.this, "id(" + id + ")已经在机器中内置", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	//ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivity.this, dishids);	
            	ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(MakeDishActivity.this, dish_names);	
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(0);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	Message msg = new Message();  
	                    msg.what = 0x345;  
	                    Package data = new Package(Package.Update_Favorite, new_dish);
	                    data.set_replaced_id(dishids[column_1.getCurrentItem()]);
	                    msg.obj = data.getBytes();
	                    TCPClient.getInstance().sendMsg(msg); 
	                    
	                    MakeDishActivity.this.resp_cmd108_count = 0;
	                    progressBar1.setVisibility(View.VISIBLE);
	                    
	                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                    while(data.get_img_pkg(baos) && baos.size() != 0) {
	                    	Log.v("BuiltinDishes", "img baos.size() = " + baos.size());
	                    	Message msgtmp = new Message();  
	                    	msgtmp.what = 0x345; 
	                    	msgtmp.obj = baos;
	                    	TCPClient.getInstance().sendMsg(msgtmp); 
	                    	baos = new ByteArrayOutputStream();
	                    }
	                    Log.v("BuiltinDishes", "send replace req " + column_1.getCurrentItem() + " to " + new_dish.dishid + " done!");
                    	
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
		
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar1.setVisibility(View.GONE);
		
		makedish_upload = (Button) findViewById(R.id.makedish_upload);
		makedish_upload.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivity", "makedish_upload");
            	if (new_dish.zhuliao_content_map.isEmpty()) {
            		Toast.makeText(MakeDishActivity.this, "不能没有主料哦~", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	new_dish.saveDishParam();
            	
            	// 先登录后上传
            	if (!Account.is_login) {
            		Intent intent = new Intent(MakeDishActivity.this, LoginActivity.class);
                	intent.putExtra("header", "登录后才能上传哦～");
                	startActivityForResult(intent, 8);
            	} else {
            		HttpUtils.uploadDish(new_dish, MakeDishActivity.this);
            	}
            }  
        });
		if (new_dish.isVerifying() || new_dish.isVerifyDone()) {
			// 已经上传过的暂不能再更新
			makedish_upload.setEnabled(false);
		}
		
		makedish_verify = (Button) findViewById(R.id.makedish_verify);
		makedish_verify.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivity", "make verify done");
            	HttpUtils.VerifyDish(new_dish, true, MakeDishActivity.this);
            }  
        });
		
		if (intent.getStringExtra("title") == null || !intent.getStringExtra("title").equals("菜谱审核")) {
			makedish_verify.setVisibility(View.GONE);
		}
		
		makedish_share = (ImageView) findViewById(R.id.makedish_share);
		makedish_share.setVisibility(View.GONE);
		makedish_shareto = (ImageView) findViewById(R.id.makedish_shareto);
		makedish_shareto.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivity", "make dish share");
            	 OnekeyShare oks = new OnekeyShare();
        		 //关闭sso授权
        		 oks.disableSSOWhenAuthorize(); 
        		 
        		// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        		 //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        		 // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        		 oks.setTitle(getString(R.string.share));
        		 // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        		 oks.setTitleUrl("http://sharesdk.cn");
        		 // text是分享文本，所有平台都需要这个字段
        		 oks.setText("我是分享文本");
        		 // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        		 oks.setImagePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/share_pic.jpg");//确保SDcard下面存在此张图片
        		 // url仅在微信（包括好友和朋友圈）中使用
        		 oks.setUrl("http://sharesdk.cn");
        		 // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        		 oks.setComment("我是测试评论文本");
        		 // site是分享此内容的网站名称，仅在QQ空间使用
        		 oks.setSite(getString(R.string.app_name));
        		 // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        		 oks.setSiteUrl("http://sharesdk.cn");
        		 
        		// 启动分享GUI
        		 oks.show(MakeDishActivity.this);
            }  
        });
		
		favorite = (ImageView) findViewById(R.id.favorite);
        favorite.setImageResource(Account.isFavorite(new_dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72);
        favorite.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (!new_dish.isVerifying() && !new_dish.isVerifyDone()) {
            		Log.v("MakeDishActivity", "only can favorite after upload. dishid = " + new_dish.dishid);
            		Toast.makeText(MakeDishActivity.this, "未上传的菜谱不能添加收藏，请在自编菜谱中", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	if (!Account.is_login) {
            		Intent intent = new Intent(MakeDishActivity.this, LoginActivity.class);
                	intent.putExtra("header", "登录后才能收藏");
                	startActivityForResult(intent, 9);
            	} else {
            		HttpUtils.favorite(new_dish, MakeDishActivity.this.handler);
            	}
            }  
        });
        
        makedish_delete = (TextView) findViewById(R.id.makedish_delete);
        makedish_delete.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	Dish.remove_not_uploaded_dish(new_dish);
            	finish();
            }  
        });
        
        if (!new_dish.isVerifying() && !new_dish.isVerifyDone()) {
    		Log.v("MakeDishActivity", "before-upload dish can't be favorited or used-as-replacement, so disable it");
    		favorite.setVisibility(View.GONE);
    		this.makedish_replace.setVisibility(View.GONE);
    		
    	}
        else {
        	// 只能删除未上传的
    		makedish_delete.setVisibility(View.GONE);
    	}
        
		new Thread() {
			public void run() {
				initImagePath();
			}
		}.start();
		
	} // onCreate
	
	public View popupView;
	
	protected int resp_cmd108_count = 0; // 图片数据响应计数
	TCPClient tcpClient;
	public Handler handler;
	
	public void init_param() {
		table_zhuliao_param.setVisibility(new_dish.zhuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
		table_fuliao_param.setVisibility(new_dish.fuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
		zhuliao_water_cb.setVisibility(new_dish.water == 2 || new_dish.zhuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
		fuliao_water_cb.setVisibility(new_dish.water == 1 || new_dish.fuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
		
		try {
			TextView et;
			et = (TextView) findViewById(R.id.makedish_oil);
			et.setText("" + new_dish.oil);
			
			if (new_dish.water == 1) {
				zhuliao_water_cb.setText("  加水 " + new_dish.water_weight + " 克");
				zhuliao_water_cb.setTextColor(Color.BLACK);
				zhuliao_water_cb.setChecked(true);
			} else if (new_dish.water == 2) {
				fuliao_water_cb.setText("  加水 " + new_dish.water_weight + " 克");
				fuliao_water_cb.setTextColor(Color.BLACK);
				fuliao_water_cb.setChecked(true);
			}
			
			et = (TextView) findViewById(R.id.makedish_qiangguoliao);
			et.setText(new_dish.qiangguoliao_content);
			
			TextView tv;
			tv = (TextView) findViewById(R.id.makedish_zhuliao_temp);
			tv.setText("" + (new_dish.zhuliao_temp & 0xff) + "°C");
			tv = (TextView) findViewById(R.id.makedish_zhuliao_time);
			tv.setText("" + new_dish.zhuliao_time / 60 + ":" + new_dish.zhuliao_time % 60);
			tv = (TextView) findViewById(R.id.makedish_zhuliao_jiaoban);
			tv.setText("" + new_dish.zhuliao_jiaoban_speed);
			
			tv = (TextView) findViewById(R.id.makedish_fuliao_temp);
			tv.setText("" + (new_dish.fuliao_temp & 0xff) + "°C");
			tv = (TextView) findViewById(R.id.makedish_fuliao_time);
			tv.setText("" + new_dish.fuliao_time / 60 + ":" + new_dish.zhuliao_time % 60);
			tv = (TextView) findViewById(R.id.makedish_fuliao_jiaoban);
			tv.setText("" + new_dish.fuliao_jiaoban_speed);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	@SuppressLint("SdCardPath")
	public void chooseDishImage(/*View view*/) {  
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);  
        innerIntent.putExtra("crop", "true");// 才能出剪辑的小方框，不然没有剪辑功能，只能选取图片  
        innerIntent.putExtra("aspectX", 200);  // 出现放大和缩小  
        innerIntent.putExtra("aspectY", 136);    
		innerIntent.putExtra("outputX", 424);//输出图片大小    
		innerIntent.putExtra("outputY", 288);  
        innerIntent.setType("image/*");      // 查看类型 详细的类型在 com.google.android.mms.ContentType   
	          
        String main_img_path = new_dish.getDishDirName() + "/" + Constants.DISH_IMG_FILENAME;
        tempFile = new File(main_img_path);  
        innerIntent.putExtra("output", Uri.fromFile(tempFile));  // 写入目标文件     
        innerIntent.putExtra("outputFormat", "JPEG"); //输入文件格式    
        
        Intent wrapperIntent = Intent.createChooser(innerIntent, "先择图片"); //开始 并设置标题  
        startActivityForResult(wrapperIntent, 1); // 设返回 码为 1  onActivityResult 中的 requestCode 对应  
    }
	 
	 //调用成功反回方法  
	 @Override  
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         super.onActivityResult(requestCode, resultCode, data);  
         Log.v("MakeDishActivity", "requestCode = " + requestCode + "resultCode =" + resultCode);
         switch (requestCode) {  
         case 1:  
        	 //makedish_img.setImageDrawable();
        	 if (resultCode == -1) {
        		 //new_dish.img_drawable = (BitmapDrawable) Drawable.createFromPath(tempFile.getAbsolutePath());
        		 BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true; 
        		 new_dish.img_bmp = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        		 new_dish.img_tiny_path = Tool.getInstance().makeTinyImage(new_dish);
        	 }
             break;  
         case 2:
        	 if (data != null) {
	        	 String qiangguoliao  = data.getStringExtra("edit_content_output");
	        	 new_dish.qiangguoliao_content = qiangguoliao;
	        	 makedish_qiangguoliao.setText(qiangguoliao);
        	 }
             break; 
         case 3:
        	 if (data != null) {
	        	 fill_table(tableLayout_zhuliao, new_dish.zhuliao_content_map);
	        	 table_zhuliao_param.setVisibility(new_dish.zhuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
	        	 zhuliao_water_cb.setVisibility(new_dish.water == 2 || new_dish.zhuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
        	 }
             break; 
	     case 4:
	    	 if (data != null) {
	        	 fill_table(tableLayout_fuliao, new_dish.fuliao_content_map);
	        	 table_fuliao_param.setVisibility(new_dish.fuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
	        	 fuliao_water_cb.setVisibility(new_dish.water == 1 || new_dish.fuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
	    	 }
	         break;
	     case 6:
	    	 if (data != null) {
	    		 fill_material_table(table_material, new_dish.prepare_material_detail);
	    	 }
	         break;
	     case 7: //菜谱名称
	    	 if (data != null) {
	    		 String  name = data.getStringExtra("edit_content_output");
	        	 new_dish.name_chinese = name;
	        	 new_dish.name_english = HanziToPinyin.getPinYin(name);
	        	 if (new_dish.name_english.length() > 13) new_dish.name_english = new_dish.name_english.substring(0, 13);//英文名字有最大长度
	        	 makedish_name.setText(name);
	    	 }
	         break;
	     case 8: // 登录返回
	    	 if (Account.is_login) {
	    		 Log.v("MakeDishActivity", "login return success, do upload");
	    		 new_dish.saveDishParam(); // 用户登录后要保存创建者信息
	    		 HttpUtils.uploadDish(new_dish, MakeDishActivity.this);
	    	 }
	    	 break;
	     case 9:  
	        	if (Account.is_login) {
		    		 Log.v("DishActivity", "login return success, do favorite");
		    		 HttpUtils.favorite(new_dish, MakeDishActivity.this.handler);
		    	}
	            break;  
	     }
         //makedish_img.setImageDrawable(new_dish.img_drawable);
         makedish_img.setImageBitmap(new_dish.img_bmp);
     }
	 
	 public Handler getHandler() {
		 return handler;
	 }
	 
	 public void fill_table(TableLayout tableLayout, LinkedHashMap<String, String> lmap) {
		 if (tableLayout.getChildCount() >= 2) {
			 tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
		 }
		 //第一个元素是添加图标
		 tableLayout.getChildAt(0).setVisibility(lmap.isEmpty() ? View.VISIBLE : View.GONE);
		 for (Iterator<String> it = lmap.keySet().iterator();it.hasNext();)
		 {
		     String key = it.next();
		     add_row(tableLayout, key, lmap.get(key));
		     Log.v("MakeDishActivity", "key = " + key + " value = " + lmap.get(key));
		 }
	 }
	 
	 public void fill_material_table(TableLayout tableLayout, ArrayList<Material> list) {
		 if (tableLayout.getChildCount() >= 2) {
			 tableLayout.removeViews(0, tableLayout.getChildCount() - 1);
		 }
		 Log.v("MakeDishActivity", "tableLayout.getchildcount() = " + tableLayout.getChildCount()); 
		 
		 for (int i = 0; i < list.size(); ++i) {
			 Material m = list.get(i);
			 if (m.path != null && !m.path.isEmpty() && m.img_drawable == null) {
				 Log.v("MakeDishActivity", "init m.img_drawable");
				 BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true; 
				 Bitmap bmp = BitmapFactory.decodeFile(new_dish.getDishDirName() + m.path, options);
        		 DisplayMetrics dm = this.getResources().getDisplayMetrics();
        		 bmp.setDensity(dm.densityDpi);
        		 m.img_drawable = new BitmapDrawable(this.getResources(), bmp);
			 }
			 add_material_row(tableLayout, m.description, m.img_drawable, i);
			 Log.v("MakeDishActivity", "add_material : " + m.description + ", " + m.img_drawable);
		 }
	 }
	 
	 protected void add_row(TableLayout tableLayout, String key, String value) {
		 TableRow tableRow = new TableRow(this);  
		 TableLayout.LayoutParams lp = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		 lp.setMargins(0, 0, 0, 2);
		 tableRow.setPadding(20, 10, 10, 0);
		 tableRow.setLayoutParams(lp);
		 tableRow.setBackgroundColor(Color.rgb(210, 255, 255));
		 TextView textView = new TextView(tableLayout.getContext());  
		 
         textView.setText(key);
         textView.setTextSize(20);
         textView.setTextColor(Color.rgb(85, 85, 85));
         //LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
         //textView.setLayoutParams(lp2);
         //textView.setmar
         
         TextView textView2 = new TextView(tableLayout.getContext());
         textView2.setText(value);
         textView2.setTextSize(20);
         textView2.setTextColor(Color.rgb(85, 85, 85));
         //textView2.setLayoutParams(lp2);
        
         tableRow.addView(textView);  
         tableRow.addView(textView2); 
         tableLayout.addView(tableRow, tableLayout.getChildCount());
         //tableLayout.addView(textView2, tableLayout.getChildCount() - 1);
	 }
	 
	 protected void add_material_row(TableLayout tableLayout, String key, Object value, int index) {
		 TextView textView = new TextView(tableLayout.getContext());  
		 TableLayout.LayoutParams tlp = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		 tlp.setMargins(5, 50, 150, 0);
		 textView.setLayoutParams(tlp);
		 Drawable image = (Drawable) value;
		 textView.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
		 //textView.setCompoundDrawablePadding(-100);
		 textView.setId(index);
         textView.setText(index + 1 + "");
         textView.setTextSize(30);
         textView.setTextColor(Color.rgb(0, 0, 0));
         textView.setOnClickListener(new OnClickListener() {  
             @Override  
             public void onClick(View v) {  
             	Log.v("MakeDishActivity", "v.getId = " + v.getId());
             	Intent intent = new Intent(MakeDishActivity.this, ImageEditActivity.class);
             	intent.putExtra("edit_title", "备料图文");
             	intent.putExtra("dish_id", new_dish.dishid);
             	intent.putExtra("material_index", v.getId());
             	startActivityForResult(intent, 6);
             }  
         });

         TextView textView2 = new TextView(tableLayout.getContext());
         TableLayout.LayoutParams tlp2 = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		 tlp2.setMargins(150, 5, 0, 0);
		 textView2.setLayoutParams(tlp2);
         textView2.setText(key);
         textView2.setTextSize(20);
         textView2.setTextColor(Color.rgb(85, 85, 85));
         //textView2.setLayoutParams(lp2);
         
         tableLayout.addView(textView, tableLayout.getChildCount() - 1);
         tableLayout.addView(textView2, tableLayout.getChildCount() - 1);
	 }
	 
	 @Override  
	 protected void onDestroy() {  
	     super.onDestroy();  
	     Log.v("MakeDishActivity", "onDestroy saveDishParam to file"); 
	     new_dish.saveDishParam();
	 } 
	 
	 private static final String FILE_NAME = "/share_pic.jpg";
	 public static String TEST_IMAGE;
		
	 private void initImagePath() {
		try {
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
					&& Environment.getExternalStorageDirectory().exists()) {
				TEST_IMAGE = Environment.getExternalStorageDirectory().getAbsolutePath() + FILE_NAME;
			}
			else {
				TEST_IMAGE = getApplication().getFilesDir().getAbsolutePath() + FILE_NAME;
			}
			File file = new File(TEST_IMAGE);
			//if (!file.exists()) {
				file.createNewFile();
				//Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.pic);
				Bitmap pic = new_dish.img_bmp;
				FileOutputStream fos = new FileOutputStream(file);
				pic.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			//}
		} catch(Throwable t) {
			t.printStackTrace();
			TEST_IMAGE = null;
		}
	}
}
