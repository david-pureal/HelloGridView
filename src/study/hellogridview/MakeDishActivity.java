package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
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
	
	Button makedish_replace;
	
	ProgressBar progressBar1;
	
	public int new_dish_id;
	
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
	protected int resp_cmd104_count = 0;
	protected int current_cmd;
	
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
		int dish_index = intent.getIntExtra("dish_index", 0);
		Log.v("MakeDishActivity", "dish_index = " + dish_index);
		new_dish = Dish.getAllDish()[dish_index];
		new_dish_id = dish_index;
		
		tcpClient = TCPClient.getInstance();
        tcpClient.set_makedishact(this);
		
		makedish_img = (ImageView) findViewById(R.id.makedish_img); 
		makedish_img.setImageDrawable(new_dish.img_drawable);
		//dish_img.setImageResource(Dish.getAllDish()[dish_index].img);
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
                    // 当用户按下按钮之后，将用户输入的数据封装成Message  
                    // 然后发送给子线程Handler  
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
                	final PopupWindow popWindow = new PopupWindow(popupView, 500, 700, true);
                	
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
                	final PopupWindow popWindow = new PopupWindow(popupView, 500, 700, true);
                	
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
                	
                	if (MakeDishActivity.this.resp_cmd108_count == 5) { //目前图片都是分成5个帧传输的
                		MakeDishActivity.this.resp_cmd108_count = 0;
            			progressBar1.setVisibility(View.GONE);
            			progressBar1.setProgress(0);
                		if (current_cmd == 101) {
	                		Log.v("MakeDishActivity", "resp_cmd108_count = " + resp_cmd108_count + " go to CurStateActivity");
	        	        	Intent intent = new Intent(MakeDishActivity.this, CurStateActivity.class);
	        	        	intent.putExtra("dish_index", new_dish_id); 
	        	        	startActivity(intent);
                		} else if (current_cmd == 104) {
                			Toast.makeText(MakeDishActivity.this, "替换菜谱完成", Toast.LENGTH_SHORT).show();
                			TCPClient.getInstance().do_heartbeat();// 获取最新内置菜谱
                			Log.v("MakeDishActivity", " replace done");
                    	}
                	}
                }  
            }  
        }; 
        
        makedish_zhuliao_temp = (TextView) findViewById(R.id.makedish_zhuliao_temp);
        makedish_zhuliao_temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,500,700,true);
            	
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
            	final PopupWindow popWindow = new PopupWindow(popupView,500,700,true);
            	
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
            	final PopupWindow popWindow = new PopupWindow(popupView, 800, 700, true);
            	
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
            	final PopupWindow popWindow = new PopupWindow(popupView,500,700,true);
            	
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
            	final PopupWindow popWindow = new PopupWindow(popupView,500,700,true);
            	
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
            	final PopupWindow popWindow = new PopupWindow(popupView, 500, 700, true);
            	
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
            	intent.putExtra("dish_index", new_dish_id);
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
            	intent.putExtra("dish_index", new_dish_id);
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
            	intent.putExtra("dish_index", new_dish_id);
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
            	intent.putExtra("dish_index", new_dish_id);
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
            	intent.putExtra("dish_index", new_dish_id);
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
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 500, 700, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	for (int i = 0; i < dishids.length; ++i) {
    				dishids[i] = 0xffff & DeviceState.getInstance().builtin_dishids[i];
    				dish_names[i] = Dish.getDishNameById(DeviceState.getInstance().builtin_dishids[i]);
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
	}
	
	public View popupView;
	
	protected int resp_cmd108_count = 0;
	TCPClient tcpClient;
	Handler handler;
	
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
	          
		//===============================  
		//	          innerIntent.setType("image/*");   
		//	          innerIntent.putExtra("crop", "true");     
		//	          innerIntent.putExtra("aspectX", 1);//裁剪框比例    
		//	          innerIntent.putExtra("aspectY", 1);    
		//	          innerIntent.putExtra("outputX", 120);//输出图片大小    
		//	          innerIntent.putExtra("outputY", 120);    
		//================================  
        tempFile = new File(Tool.getInstance().getModulePath() + new_dish.dishid + ".jpg"); // 以时间秒为文件名  
        innerIntent.putExtra("output", Uri.fromFile(tempFile));  // 专入目标文件     
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
        		 new_dish.img_drawable = (BitmapDrawable) Drawable.createFromPath(tempFile.getAbsolutePath());
        		 new_dish.img_tiny_path = Tool.getInstance().makeTinyImage(new_dish.img_drawable, new_dish.dishid);
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
	        	 makedish_name.setText(name);
	    	 }
	         break;
	     }
         makedish_img.setImageDrawable(new_dish.img_drawable);
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
	 
	 public void fill_material_table(TableLayout tableLayout, LinkedHashMap<String, Object> lmap) {
		 if (tableLayout.getChildCount() >= 2) {
			 tableLayout.removeViews(0, tableLayout.getChildCount() - 1);
		 }
		 Log.v("MakeDishActivity", "tableLayout.getchildcount() = " + tableLayout.getChildCount()); 
		 int i = 0;
		 for (Iterator<String> it = lmap.keySet().iterator();it.hasNext();++i)
		 {
		     String key = it.next();
		     add_material_row(tableLayout, key, lmap.get(key), i);
		     Log.v("MakeDishActivity", "key = " + key + " value = " + lmap.get(key));
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
             	intent.putExtra("dish_index", new_dish_id);
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
}
