package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

import cn.sharesdk.onekeyshare.OnekeyShare;
import study.hellogridview.Dish.Material;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MakeDishActivityJ extends Activity implements OnTouchListener {
	
	ImageView makedish_img;
	TextView makedish_name;
	TextView makedish_brief;
	TextView makedish_brief_add;
	Button makedish_startcook;
	Button makedish_save;
	
	TextView add_qiangguoliao_tv;
	TextView add_tiaoliao_tv;
	TextView add_zhuliao;
	//TextView zhuliao_content;
	
	TextView add_fuliao;
	TextView fuliao_content;
	
	TextView table_material_image_addrow;
	TextView makedish_set_zhuliao_param_tv;
	TextView makedish_set_fuliao_param_tv;
	
	static File tempFile;
	Dish new_dish;
	
	TableLayout table_qiangguoliao;
	
	TableLayout table_zhuliao_param;
	TableLayout table_fuliao_param;
	
	TableLayout table_tiaoliao;
	TableRow table_tiaoliao_tr0;
	
	CheckBox zhuliao_water_cb;
	
	TextView makedish_zhuliao_temp;
	TextView makedish_zhuliao_time;
	TextView makedish_zhuliao_jiaoban;
	
	TextView makedish_fuliao_temp;
	TextView makedish_fuliao_time;
	TextView makedish_fuliao_jiaoban;
	
	TextView makedish_oil_tv;
	TextView water_fuliao;
	TextView water_fuliao_tv;
	
	public ImageView favorite;
	public TextView favorite_tv;
	
	Button makedish_replace;
	Button makedish_upload;
	Button makedish_verify;
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
	public final String jiaoban[] = {"1������", "2������", "3������", "4������", "5�п���", "6�Ͽ���", "7�����", "8������"};
	protected int current_cmd;
	private TextView makedish_delete;
	RelativeLayout explain;
	
	boolean is_starting_cook = false;
	int image_package_count = Integer.MAX_VALUE;
	
	int dishid_old_cancel = 0;
	
	//��ָ���һ���ʱ����С�ٶ�  
    private static final int XSPEED_MIN = 150;  
    //��ָ���һ���ʱ����С����  
    private static final int XDISTANCE_MIN = 150;  
    //��¼��ָ����ʱ�ĺ����ꡣ  
    private float xDown;  
    //��¼��ָ�ƶ�ʱ�ĺ����ꡣ  
    private float xMove;  
    //���ڼ�����ָ�������ٶȡ�  
    private VelocityTracker mVelocityTracker;
    
    ScrollView layout_makedish;
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("MakeDishActivityJ", "onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_make_dishj);
		
		layout_makedish = (ScrollView) findViewById(R.id.layout_makedish);
		layout_makedish.setOnTouchListener(this);
		layout_makedish.setBackground(new BitmapDrawable(this.getResources(), Tool.get_res_bitmap(R.drawable.bkg_darker)));
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		self_content_view = inflater.inflate(R.layout.activity_make_dishj, null, false);
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
		editable = intent.getBooleanExtra("editable", true);
		Log.v("MakeDishActivityJ", "dish_id = " + dish_id);
		new_dish = Dish.getDishById(dish_id);
		
		tcpClient = TCPClient.getInstance();
        tcpClient.set_makedishact(this);
		
		makedish_img = (ImageView) findViewById(R.id.makedish_img); 
		if (editable) makedish_img.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
                MakeDishActivityJ.this.chooseDishImage(); 
            }  
        });
		
		makedish_name = (TextView) findViewById(R.id.makedish_name);
		if (editable) makedish_name.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	Intent intent = new Intent(MakeDishActivityJ.this, EditActivity.class);
            	intent.putExtra("edit_title", "��������");
            	intent.putExtra("edit_content_input", new_dish.name_chinese);
            	startActivityForResult(intent, 7);
            }  
        });
		
		makedish_brief = (TextView) findViewById(R.id.makedish_brief); 
		makedish_brief_add = (TextView) findViewById(R.id.makedish_brief_add);
		if (editable) makedish_brief.setOnClickListener(new OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				if (!editable) return;
				Intent intent = new Intent(MakeDishActivityJ.this, EditActivity.class);
				intent.putExtra("edit_title", "���׼��");
            	intent.putExtra("edit_content_input", new_dish.intro);
            	startActivityForResult(intent, 17);
			}  
		});
		if (editable) makedish_brief_add.setOnClickListener(new OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				if (!editable) return;
				Intent intent = new Intent(MakeDishActivityJ.this, EditActivity.class);
				intent.putExtra("edit_title", "���׼��");
            	intent.putExtra("edit_content_input", new_dish.intro);
            	startActivityForResult(intent, 17);
			}  
		});
		
		add_qiangguoliao_tv = (TextView) findViewById(R.id.add_qiangguoliao_tv);
		if (editable) add_qiangguoliao_tv.setOnClickListener(new OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
				intent.putExtra("edit_title", "������");
				intent.putExtra("dish_id", new_dish.dishid);
				startActivityForResult(intent, 2);
			}  
		});
		
		table_qiangguoliao = (TableLayout) findViewById (R.id.table_qiangguoliao);
		if (editable) table_qiangguoliao.setOnClickListener(new OnClickListener() {  
			@Override  
			public void onClick(View v) {
				if (!editable) return;
				Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
				intent.putExtra("edit_title", "������");
				intent.putExtra("dish_id", new_dish.dishid);
				startActivityForResult(intent, 2);
			}  
		});
		
		add_tiaoliao_tv = (TextView) findViewById(R.id.add_tiaoliao_tv);
		if (editable) add_tiaoliao_tv.setOnClickListener(new OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
				intent.putExtra("edit_title", "����");
				intent.putExtra("dish_id", new_dish.dishid);
				startActivityForResult(intent, 12);
			}  
		});
		table_tiaoliao = (TableLayout) findViewById (R.id.table_tiaoliao);
		if (editable) table_tiaoliao.setOnClickListener(new OnClickListener() {  
			@Override  
			public void onClick(View v) {  
				if (!editable) return;
				Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
				intent.putExtra("edit_title", "����");
				intent.putExtra("dish_id", new_dish.dishid);
				startActivityForResult(intent, 12);
			}  
		});
		
		makedish_startcook = (Button) findViewById(R.id.makedish_startcook);
		makedish_startcook.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (TCPClient.getInstance().connect_state != Constants.CONNECTED) {
            		Toast.makeText(MakeDishActivityJ.this, "�������ӻ���", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	if (DeviceState.getInstance().working_state != Constants.MACHINE_WORK_STATE_STOP) {
            		Toast.makeText(MakeDishActivityJ.this, "�Ѿ��ڳ�����", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	
                try {
                    Message msg = new Message();  
                    msg.what = 0x345;  
                    Package data = new Package(Package.Send_Dish, new_dish);
                    msg.obj = data.getBytes();
                    tcpClient.sendMsg(msg); 
                    
                    is_starting_cook = true;
                	image_package_count = data.get_img_pkg_count();
                	progressBar1.setProgress(0);
                	progressBar1.setMax(image_package_count - 1); //��0��ʼ��
                	progressBar1.setVisibility(View.VISIBLE);
                    resp_cmd108_count = 0;
                    Log.e("MakeDishActivityJ", "starting cook, image_package_count = " + image_package_count);
                    
                	ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while(data.get_img_pkg(baos) && baos.size() != 0) {
                    	Log.v("MakeDishActivityJ", "img baos.size() = " + baos.size());
                    	Message msgtmp = new Message();  
                    	msgtmp.what = 0x345; 
                    	msgtmp.obj = baos;
                    	tcpClient.sendMsg(msgtmp); 
                    	
                    	baos = new ByteArrayOutputStream();
                    }
                } catch (Exception e) { 
                	e.printStackTrace();
                	Log.e("MakeDishActivityJ", "startcook prepare package data exception");
                	is_starting_cook = false;
                	image_package_count = Integer.MAX_VALUE;
                	progressBar1.setVisibility(View.GONE);
                	Toast.makeText(MakeDishActivityJ.this, "��ʼ����ʧ��", Toast.LENGTH_SHORT).show();
                }  
            }  
        }); 
		
		zhuliao_water_cb = (CheckBox) findViewById(R.id.makedish_zhuliao_water);
		if (editable) zhuliao_water_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            	Log.v("MakeDishActivityJ", "checkbox = " + arg1 + " arg0 = " + arg0);
            	if (new_dish.water == 0 && arg1) { // ��Ҫ��ˮ����ʾ��ˮ��
            		popupView = inflater.inflate(R.layout.wheel_view_2_column, null, false);
                	final PopupWindow popWindow = new PopupWindow(popupView, 900, 800, true);
                	
                	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
                	final TextView coloum_1_name = (TextView) popupView.findViewById(R.id.column_1_name);
                	coloum_1_name.setText("��ˮ");
                	final String strs[] = {"������", "������"};
                	ArrayWheelAdapter<String> adapter_str = new ArrayWheelAdapter<String>(MakeDishActivityJ.this, strs);	
                	column_1.setViewAdapter(adapter_str);
                	column_1.setCurrentItem(0);
                	
                	final WheelView column_2 = (WheelView) popupView.findViewById(R.id.column_2);
                	final TextView coloum_2_name = (TextView)popupView.findViewById(R.id.column_2_name);
                	coloum_2_name.setText("��");
                	ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivityJ.this, waters);
                	column_2.setViewAdapter(adapter);
                	column_2.setCurrentItem(2); //default 15g
                	
                	Button sure = (Button) popupView.findViewById(R.id.makesure);
                	sure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	zhuliao_water_cb.setTextColor(Color.BLACK);
                        	new_dish.water = (byte) (column_1.getCurrentItem() == 0 ? 1 : 2);
                        	new_dish.water_weight = waters[column_2.getCurrentItem()];
                        	zhuliao_water_cb.setText("   " + strs[column_1.getCurrentItem()] + " ��ˮ " + new_dish.water_weight + " ��");
                            popWindow.dismiss(); //Close the Pop Window
                        }
                    });
                	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            	}
            	else if (new_dish.water != 0 && !arg1){
            		zhuliao_water_cb.setText("   �Ƿ��ˮ ");
            		zhuliao_water_cb.setTextColor(Color.rgb(85, 85, 85));
            		new_dish.water = 0;
            	}
            }
        });
		
		// ��Ϣ����
		// TODO just for handler mark
        handler = new Handler() {  
            @Override  
            public void handleMessage(Message msg) {  
                // �����Ϣ�������߳�  
                if (msg.what == 0x123) {    
                	RespPackage rp = (RespPackage) msg.obj;
                	Log.v("MakeDishActivityJ", "got resp, cmdtype_head=" + (rp.cmdtype_head&0xff) + ", cmdtype_body=" + (rp.cmdtype_body&0xff));
                	if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 101) {
                		current_cmd = 101;
                	} else if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 104) {
                		current_cmd = 104;
                	} 
                	
                	if ((rp.cmdtype_head&0xff) == 127 && (rp.cmdtype_body&0xff) == 108) {
                		++ resp_cmd108_count;
                		progressBar1.setProgress(resp_cmd108_count);
                	}
                	
                	Log.v("MakeDishActivityJ", "current_cmd=" + current_cmd + ", resp_cmd108_count=" + resp_cmd108_count
                			+ ",image_package_count=" + image_package_count);
                	if (resp_cmd108_count == image_package_count) { //ĿǰͼƬ���Ƿֳ�5��֡�����
                		resp_cmd108_count = 0;
                		is_starting_cook = false;
                		image_package_count = Integer.MAX_VALUE;
                		
            			progressBar1.setVisibility(View.GONE);
            			
                		if (current_cmd == 101) {
	                		Log.v("MakeDishActivityJ", "start cook dish(" + new_dish.dishid + ") done, go to CurStateActivity");
	        	        	Intent intent = new Intent(MakeDishActivityJ.this, CurStateActivity.class);
	        	        	intent.putExtra("dish_id", new_dish.dishid); 
	        	        	startActivity(intent);
                		} else if (current_cmd == 104) {
                			Toast.makeText(MakeDishActivityJ.this, "�滻�������", Toast.LENGTH_SHORT).show();
                			TCPClient.getInstance().do_heartbeat();// ��ȡ�������ò���
                			Log.v("MakeDishActivityJ", " replace done");
                    	}
                	}
                }// if (msg.what == 0x123)
                else if (msg.what == Constants.MSG_ID_UPLOAD_RESULT) {
                	String state = (String)msg.obj;
                	if (state.equals("success")) {
                		//MakeDishActivityJ.this.makedish_upload.setEnabled(false);
                		makedish_delete.setVisibility(View.GONE);
                		favorite.setVisibility(View.VISIBLE);
                		favorite_tv.setVisibility(View.GONE);
                		
                		// ��Ϊ���ɱ༭
                		editable = false;
                		init_param();
                		
                		Toast.makeText(MakeDishActivityJ.this, "�ϴ����", Toast.LENGTH_SHORT).show();
                	}
                	else if (state.equals("fail")) {
                		Toast.makeText(MakeDishActivityJ.this, "�ϴ�ʧ�ܣ�������", Toast.LENGTH_SHORT).show();
                	}
                	else {
                		// TODO progress bar 
                	}
                }
                else if (msg.what == Constants.MSG_ID_VERIFY_DONE) {
                	makedish_verify.setText("�����ͨ��");
                	makedish_verify.setEnabled(false);
                }
                else if (msg.what == Constants.MSG_ID_FAVORITE_DONE) {
                	Log.v("MakeDishActivityJ", "got favorite result, isFavorite ="  + Account.isFavorite(new_dish));
                	favorite.setImageResource(Account.isFavorite(new_dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72);
                	favorite_tv.setText(Account.isFavorite(new_dish) ? "���ղ�" : "δ�ղ�");
                	String text = Account.isFavorite(new_dish) ? "���ղ�" : "ȡ���ղ�";
                	Toast.makeText(MakeDishActivityJ.this, text, Toast.LENGTH_SHORT).show();
                }
                else if (msg.what == Constants.MSG_ID_DEL_In_Server) {
                	Log.v("MakeDishActivityJ", "got deleteDishInServer done. ");
                	if (new_dish.hasNotUploaded()/*Dish.alldish_map.containsKey(dishid_old_cancel)*/){
                		Log.v("MakeDishActivityJ", "got deleteDishInServer done. could be deleted.");
                		Toast.makeText(MakeDishActivityJ.this, "�����ϴ��ɹ�", Toast.LENGTH_SHORT).show();
                		
                		editable = true;
                		init_param();
                	}
                	else {
                		Log.v("MakeDishActivityJ", "got deleteDishInServer done. cancel failed.");
                		Toast.makeText(MakeDishActivityJ.this, "�����ϴ�ʧ��", Toast.LENGTH_SHORT).show();
                	}
                }
            }  
        }; 
        
        makedish_oil_tv = (TextView) findViewById (R.id.makedish_oil);
        if (editable) makedish_oil_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 600, 800, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	column_1.setViewAdapter(new ArrayWheelAdapter<Integer>(MakeDishActivityJ.this, oils));
            	column_1.setCyclic(true);
            	column_1.setCurrentItem((new_dish.oil & 0xff) / 10 - 1);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	new_dish.oil = oils[column_1.getCurrentItem()].byteValue();
                    	makedish_oil_tv.setText("" + oils[column_1.getCurrentItem()] + "��");
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        add_zhuliao = (TextView) findViewById(R.id.makedish_add_zhuliao);
        add_zhuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "����");
            	//intent.putExtra("edit_content_input", new_dish.zhuliao_content);
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 3);
            }  
        });
        
        add_fuliao = (TextView) findViewById(R.id.makedish_add_fuliao);
        add_fuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "����");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 4);
            }  
        });
        
		makedish_set_zhuliao_param_tv = (TextView) findViewById(R.id.makedish_set_zhuliao_param_tv);
		makedish_set_zhuliao_param_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(MakeDishActivityJ.this, ParamEditActivity.class);
            	intent.putExtra("edit_title", "���ϲ���");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 10);
            }  
        });
		
		// ���ϱ��
		tableLayout_zhuliao = (TableLayout) findViewById(R.id.table_zhuliao);
		if (editable) tableLayout_zhuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "����");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 3);
            }  
        });
		
		table_zhuliao_param = (TableLayout) findViewById(R.id.table_zhuliao_param);
		if (editable) table_zhuliao_param.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	Intent intent = new Intent(MakeDishActivityJ.this, ParamEditActivity.class);
            	intent.putExtra("edit_title", "���ϲ���");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 10);
            }  
        });
		
		makedish_set_fuliao_param_tv = (TextView) findViewById(R.id.makedish_set_fuliao_param_tv);
		makedish_set_fuliao_param_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	Intent intent = new Intent(MakeDishActivityJ.this, ParamEditActivity.class);
            	intent.putExtra("edit_title", "���ϲ���");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 11);
            }  
        });
		
		// ���ϱ��
		tableLayout_fuliao = (TableLayout) findViewById(R.id.table_fuliao);
		if (editable) tableLayout_fuliao.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	Intent intent = new Intent(MakeDishActivityJ.this, TableEditActivity.class);
            	intent.putExtra("edit_title", "����");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 4);
            }  
        });
		
		table_fuliao_param = (TableLayout) findViewById(R.id.table_fuliao_param);
		if (editable) table_fuliao_param.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!editable) return;
            	Intent intent = new Intent(MakeDishActivityJ.this, ParamEditActivity.class);
            	intent.putExtra("edit_title", "���ϲ���");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 11);
            }  
        });
		
		table_material_image_addrow = (TextView) findViewById(R.id.table_material_image_addrow);
		table_material_image_addrow.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (!editable) return;
            	Log.v("MakeDishActivityJ", "v.getId = " + v.getId());
            	if (new_dish.prepare_material_detail.size() == Tool.material_index.length) {
            		Toast.makeText(MakeDishActivityJ.this, "�������10������ͼ��", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	Intent intent = new Intent(MakeDishActivityJ.this, ImageEditActivity.class);
            	intent.putExtra("edit_title", "����ͼ��");
            	intent.putExtra("dish_id", new_dish.dishid);
            	startActivityForResult(intent, 6);
            }  
        });
		
		table_material = (TableLayout) findViewById(R.id.table_material);
		
		makedish_replace = (Button) findViewById(R.id.makedish_replace);
		makedish_replace.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (editable && !new_dish.isAppBuiltIn() && new_dish.hasNotUploaded()) {
            		Log.v("MakeDishActivityJ", "can't replace before upload");
            		Toast.makeText(MakeDishActivityJ.this, "δ�ϴ��Ĳ��ײ��������滻���ò���", Toast.LENGTH_SHORT).show();
            	}
            	
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 600, 800, true);
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	
            	if (DeviceState.getInstance().got_builtin == false) {
            		Toast.makeText(MakeDishActivityJ.this, "�������ӻ�������ȡ���ò���", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	for (int i = 0; i < dishids.length; ++i) {
    				dishids[i] = 0xffff & DeviceState.getInstance().builtin_dishids[i];
    				dish_names[i] = Dish.getDishNameById(dishids[i]);
    			}
            	
            	int id = new_dish.dishid;
            	if (Arrays.binarySearch(dishids, id) >= 0) {
            		Toast.makeText(MakeDishActivityJ.this, "id(" + id + ")�Ѿ��ڻ���������", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	//ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(MakeDishActivityJ.this, dishids);	
            	ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(MakeDishActivityJ.this, dish_names);	
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
	                    
	                    image_package_count = data.get_img_pkg_count();
	                	progressBar1.setProgress(0);
	                	progressBar1.setMax(image_package_count - 1); //��0��ʼ��
	                    resp_cmd108_count = 0;
	                    progressBar1.setProgress(0);
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
            	popWindow.setOutsideTouchable(true);
            	popWindow.setBackgroundDrawable(new BitmapDrawable());
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
		
		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar1.setVisibility(View.GONE);
		
		makedish_upload = (Button) findViewById(R.id.makedish_upload);
		makedish_upload.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivityJ", "makedish_upload");
            	if (new_dish.zhuliao_content_map.isEmpty()) {
            		Toast.makeText(MakeDishActivityJ.this, "����û������Ŷ~", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	new_dish.saveDishParam();
            	
            	// �ȵ�¼���ϴ�
            	if (!Account.is_login) {
            		Intent intent = new Intent(MakeDishActivityJ.this, LoginActivity.class);
                	intent.putExtra("header", "��¼������ϴ�Ŷ��");
                	startActivityForResult(intent, 8);
            	} else {
            		new_dish.author_id = Account.userid;
            		new_dish.author_name = Account.username;
            		new_dish.saveDishParam();
            		HttpUtils.uploadDish(new_dish, MakeDishActivityJ.this);
            	}
            }  
        });
//		if (new_dish.isVerifying() || new_dish.isVerifyDone()) {
//			// �Ѿ��ϴ������ݲ����ٸ���
//			makedish_upload.setEnabled(false);
//		}
		
		makedish_verify = (Button) findViewById(R.id.makedish_verify);
		makedish_verify.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivityJ", "make verify done");
            	HttpUtils.VerifyDish(new_dish, true, MakeDishActivityJ.this);
            }  
        });
		
		if (intent.getStringExtra("title") == null || !intent.getStringExtra("title").equals("�������")) {
			makedish_verify.setVisibility(View.GONE);
		}
		
		makedish_shareto = (ImageView) findViewById(R.id.makedish_shareto);
		makedish_shareto.setImageBitmap(Tool.get_res_bitmap(R.drawable.shareto));
		makedish_shareto.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Log.v("MakeDishActivityJ", "make dish share");
            	 OnekeyShare oks = new OnekeyShare();
        		 //�ر�sso��Ȩ
        		 oks.disableSSOWhenAuthorize(); 
        		 
        		// ����ʱNotification��ͼ�������  2.5.9�Ժ�İ汾�����ô˷���
        		 //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        		 // title���⣬ӡ��ʼǡ����䡢��Ϣ��΢�š���������QQ�ռ�ʹ��
        		 oks.setTitle(getString(R.string.share));
        		 // titleUrl�Ǳ�����������ӣ�������������QQ�ռ�ʹ��
        		 oks.setTitleUrl("http://sharesdk.cn");
        		 // text�Ƿ����ı�������ƽ̨����Ҫ����ֶ�
        		 oks.setText("���Ƿ����ı�");
        		 // imagePath��ͼƬ�ı���·����Linked-In�����ƽ̨��֧�ִ˲���
        		 oks.setImagePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/share_pic.jpg");//ȷ��SDcard������ڴ���ͼƬ
        		 // url����΢�ţ��������Ѻ�����Ȧ����ʹ��
        		 oks.setUrl("http://sharesdk.cn");
        		 // comment���Ҷ�������������ۣ�������������QQ�ռ�ʹ��
        		 oks.setComment("���ǲ��������ı�");
        		 // site�Ƿ�������ݵ���վ���ƣ�����QQ�ռ�ʹ��
        		 oks.setSite(getString(R.string.app_name));
        		 // siteUrl�Ƿ�������ݵ���վ��ַ������QQ�ռ�ʹ��
        		 oks.setSiteUrl("http://sharesdk.cn");
        		 
        		// ��������GUI
        		 oks.show(MakeDishActivityJ.this);
            }  
        });
		
		favorite = (ImageView) findViewById(R.id.favorite);
		favorite_tv = (TextView) findViewById(R.id.favorite_tv);
        favorite.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (!Account.is_login) {
            		Account.do_local_favorite(new_dish);
            		int favorite_resid = Account.isFavorite(new_dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72;
                    favorite.setImageBitmap(Tool.get_res_bitmap(favorite_resid));
                    favorite_tv.setText(Account.isFavorite(new_dish) ? "���ղ�" : "δ�ղ�");
            		//String text = is_fav ? "���ղ�" : "ȡ���ղ�";
            		//Toast.makeText(MakeDishActivityJ.this, text, Toast.LENGTH_SHORT).show();
            	} else {
            		HttpUtils.favorite(new_dish, MakeDishActivityJ.this.handler);
            	}
            }  
        });
        favorite_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (!Account.is_login) {
            		Account.do_local_favorite(new_dish);
            		int favorite_resid = Account.isFavorite(new_dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72;
                    favorite.setImageBitmap(Tool.get_res_bitmap(favorite_resid));
                    favorite_tv.setText(Account.isFavorite(new_dish) ? "���ղ�" : "δ�ղ�");
            		//String text = is_fav ? "���ղ�" : "ȡ���ղ�";
            		//Toast.makeText(MakeDishActivityJ.this, text, Toast.LENGTH_SHORT).show();
            	} else {
            		HttpUtils.favorite(new_dish, MakeDishActivityJ.this.handler);
            	}
            }  
        });
        
        makedish_delete = (TextView) findViewById(R.id.makedish_delete);
        makedish_delete.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (new_dish.hasNotUploaded()) {
	            	Dish.remove_not_uploaded_dish(new_dish);
	            	finish();
            	}
            	else {
            		if (!Account.is_login) {
                		Intent intent = new Intent(MakeDishActivityJ.this, LoginActivity.class);
                    	intent.putExtra("header", "��¼����ܳ����ϴ�");
                    	startActivityForResult(intent, 13);
                	} else {
                		dishid_old_cancel = new_dish.dishid;
                		HttpUtils.deleteDishInServer(new_dish, MakeDishActivityJ.this.handler);
                	}
            	}
            }  
        });
        
        explain = (RelativeLayout) findViewById(R.id.explain);
        
        this.init_param();
        
		new Thread() {
			public void run() {
				initImagePath();
			}
		}.start();
		
	} // onCreate
	
	public View popupView;
	
	protected int resp_cmd108_count = 0; // ͼƬ������Ӧ����
	TCPClient tcpClient;
	public Handler handler;
	private float yDown;
	private float real_yDown;
	private float yspeed = 0;
	
	public void init_param() {
		try {
			makedish_img.setImageBitmap(new_dish.img_bmp);
			if (!editable) makedish_img.setOnClickListener(null);
			
			makedish_name.setText(new_dish.name_chinese);
			if (!editable) {
				//makedish_name.setOnClickListener(null);
				makedish_name.setCompoundDrawables(null, null, null, null);
			}
			
			if (!editable) add_qiangguoliao_tv.setVisibility(View.GONE);
			//if (!editable) table_qiangguoliao.setOnClickListener(null);
			fill_liao_table(table_qiangguoliao, new_dish.qiangguoliao_content_map);
			
			if (!editable) add_tiaoliao_tv.setVisibility(View.GONE);
			//if (!editable) table_tiaoliao.setOnClickListener(null);
			fill_liao_table(table_tiaoliao, new_dish.tiaoliao_content_map);
			
			//zhuliao_water_cb.setOnCheckedChangeListener(null);
			zhuliao_water_cb.setClickable(editable);
			
			makedish_brief.setText("����" + new_dish.intro + "\n  ");
			makedish_brief.setVisibility(new_dish.intro.isEmpty() ? View.GONE : View.VISIBLE);
			makedish_brief_add.setVisibility(editable && new_dish.intro.isEmpty() ? View.VISIBLE : View.GONE);
//			if (!editable) {
//				makedish_brief.setOnClickListener(null);
//				makedish_brief_add.setOnClickListener(null);
//			}
			
			makedish_oil_tv.setText("" + new_dish.oil + "��");
			//if (!editable) makedish_oil_tv.setOnClickListener(null);
			
			if (!editable) add_zhuliao.setVisibility(View.GONE);
			if (!editable) add_fuliao.setVisibility(View.GONE);
			
			if (!editable) makedish_set_zhuliao_param_tv.setVisibility(View.GONE);
			
			//if (!editable) tableLayout_zhuliao.setOnClickListener(null);
			fill_table(tableLayout_zhuliao, new_dish.zhuliao_content_map);
			Log.v("MakeDishActivityJ", "zhuliao_content_map.size() = " + new_dish.zhuliao_content_map.size());
			Log.v("MakeDishActivityJ", "tableLayout_zhuliao.getChildCount() = " + tableLayout_zhuliao.getChildCount());
			
			//if (!editable) table_zhuliao_param.setOnClickListener(null);
			fill_param_table(table_zhuliao_param);
			makedish_set_fuliao_param_tv.setVisibility(editable ? View.VISIBLE : View.GONE);
			
			//if (!editable) tableLayout_fuliao.setOnClickListener(null);
			fill_table(tableLayout_fuliao, new_dish.fuliao_content_map);
			
			//if (!editable) table_fuliao_param.setOnClickListener(null);
			fill_param_table(table_fuliao_param);
			
			
			table_material_image_addrow.setVisibility(editable ? View.VISIBLE : View.GONE);
				//table_material_image_addrow.setOnClickListener(null);
			
			fill_material_table(table_material, new_dish.prepare_material_detail);
			
			makedish_upload.setVisibility(editable ? View.VISIBLE : View.GONE);
			
			int favorite_resid = Account.isFavorite(new_dish) ? R.drawable.favorite_dish_72 : R.drawable.unfavorite_dish_72;
	        favorite.setImageBitmap(Tool.get_res_bitmap(favorite_resid));
			favorite_tv.setText(Account.isFavorite(new_dish) ? "���ղ�" : "δ�ղ�");
	        if (!new_dish.isAppBuiltIn() && new_dish.hasNotUploaded()) {
	        	favorite.setVisibility(View.GONE);
	        	favorite_tv.setVisibility(View.GONE);
	    	}
	        
	        if (!editable) makedish_delete.setVisibility(View.GONE);
	        if (!new_dish.isAppBuiltIn()) {
	    		Log.v("MakeDishActivityJ", "before-upload dish can't be favorited or used-as-replacement, so disable it");
	    		makedish_replace.setVisibility((!new_dish.isAppBuiltIn() && new_dish.hasNotUploaded()) ? View.GONE : View.VISIBLE);
	    	}
	        if (editable && !new_dish.isAppBuiltIn() && !new_dish.isVerifyDone()){
	        	if (new_dish.hasNotUploaded()) {
		        	// δ�ϴ���ֱ���ڱ���ɾ��
		        	makedish_delete.setText("ɾ��");
		    		makedish_delete.setVisibility(View.VISIBLE);
	        	}
	        	else {
	        		// ���ϴ��ģ��ӷ����ɾ��������ԭΪδ�ϴ����Ա����
	        		makedish_delete.setText("�����ϴ�");
		    		makedish_delete.setVisibility(View.VISIBLE);
	        	}
	    	}
	        
	        if (!new_dish.isAppBuiltIn() && new_dish.isMine() && !new_dish.isVerifyDone() && !new_dish.hasNotUploaded()) {
	        	makedish_delete.setText("�����ϴ�");
        		makedish_delete.setVisibility(View.VISIBLE);
	        }
	        
	        if(editable) explain.setVisibility(View.GONE);
			
			Log.v("MakeDishActivity", "newdish.water = " + new_dish.water);
			if (new_dish.water != 0) {
				zhuliao_water_cb.setText("   " + (new_dish.water==1 ? "������" : "������") + " ��ˮ " + new_dish.water_weight + " ��");
				zhuliao_water_cb.setTextColor(Color.BLACK);
				zhuliao_water_cb.setChecked(true);
			} 
			
			// ����ÿ��ı���
			int i = 2;
			int zhuliao_i = i, fuliao_i = i, tiaoliao_i = i, water_i = i, material_i = i;
			if (!editable) {
				if (new_dish.qiangguoliao == 0) {
					((TextView)findViewById(R.id.makedish_qiangguoliao_title)).setVisibility(View.GONE);
				} else {++i;}
				
				zhuliao_i = i;
				++i; // ���Ͽ϶�����
				fuliao_i = i;
				
				if (new_dish.fuliao_time == 0) {
					((TextView)findViewById(R.id.makedish_fuliao_title)).setVisibility(View.GONE);
				} else {++i;}
				
				tiaoliao_i = i;
				
				if (new_dish.tiaoliao_content_map.isEmpty()) {
					((TextView)findViewById(R.id.makedish_tiaoliao_title)).setVisibility(View.GONE);
				} else {++i;}
				
				water_i = i;
				
				if (new_dish.water == 0) {
					((TextView)findViewById(R.id.makedish_water_title)).setVisibility(View.GONE);
					zhuliao_water_cb.setVisibility(View.GONE);
				} else {++i;}
				
				material_i = i;
				
				if (new_dish.prepare_material_detail.isEmpty()) {
					((TextView)findViewById(R.id.makedish_material_title)).setVisibility(View.GONE);
				}
				
				((TextView)findViewById(R.id.makedish_zhuliao_title)).setText(zhuliao_i + ".����");
				((TextView)findViewById(R.id.makedish_fuliao_title)).setText(fuliao_i + ".����");
				((TextView)findViewById(R.id.makedish_tiaoliao_title)).setText(tiaoliao_i + ".����");
				((TextView)findViewById(R.id.makedish_water_title)).setText(water_i + ".��ˮ");
				((TextView)findViewById(R.id.makedish_material_title)).setText(material_i + ".����ͼ��");
			}
			else {
				((TextView)findViewById(R.id.makedish_zhuliao_title)).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.makedish_fuliao_title)).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.makedish_tiaoliao_title)).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.makedish_water_title)).setVisibility(View.VISIBLE);
				((TextView)findViewById(R.id.makedish_material_title)).setVisibility(View.VISIBLE);
			}
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@SuppressLint("SdCardPath")
	public void chooseDishImage(/*View view*/) {  
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);  
        innerIntent.putExtra("crop", "true");// ���ܳ�������С���򣬲�Ȼû�м������ܣ�ֻ��ѡȡͼƬ  
        innerIntent.putExtra("aspectX", 200);  // ���ַŴ����С  
        innerIntent.putExtra("aspectY", 136);    
		innerIntent.putExtra("outputX", 424);//���ͼƬ��С    
		innerIntent.putExtra("outputY", 288);  
        innerIntent.setType("image/*");      // �鿴���� ��ϸ�������� com.google.android.mms.ContentType   
	          
        String main_img_path = new_dish.getDishDirName() + "/" + Constants.DISH_IMG_FILENAME;
        tempFile = new File(main_img_path);  
        innerIntent.putExtra("output", Uri.fromFile(tempFile));  // д��Ŀ���ļ�     
        innerIntent.putExtra("outputFormat", "JPEG"); //�����ļ���ʽ    
        
        Intent wrapperIntent = Intent.createChooser(innerIntent, "����ͼƬ"); //��ʼ �����ñ���  
        startActivityForResult(wrapperIntent, 1); // �践�� ��Ϊ 1  onActivityResult �е� requestCode ��Ӧ  
    }
	 
	 //���óɹ����ط���  
	 @Override  
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         super.onActivityResult(requestCode, resultCode, data);  
         Log.v("MakeDishActivityJ", "requestCode = " + requestCode + "resultCode =" + resultCode);
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
	        	 if (new_dish.qiangguoliao_content_map.isEmpty()) new_dish.qiangguoliao = 0;
	        	 fill_liao_table(table_qiangguoliao, new_dish.qiangguoliao_content_map);
        	 }
             break; 
         case 3:
        	 if (data != null) {
	        	 fill_table(tableLayout_zhuliao, new_dish.zhuliao_content_map);
	        	 if (new_dish.zhuliao_content_map.isEmpty()) {
	        		 table_zhuliao_param.removeAllViews();
	        		 makedish_set_zhuliao_param_tv.setVisibility(View.VISIBLE);
	        	 }
	        	 //zhuliao_water_cb.setVisibility(new_dish.water == 2 || new_dish.zhuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
        	 }
             break; 
	     case 4:
	    	 if (data != null) {
	        	 fill_table(tableLayout_fuliao, new_dish.fuliao_content_map);
	        	 if (new_dish.fuliao_content_map.isEmpty()) {
	        		 table_fuliao_param.removeAllViews();
	        		 makedish_set_fuliao_param_tv.setVisibility(View.VISIBLE);
	        	 }
	        	 //table_fuliao_param.setVisibility(new_dish.fuliao_content_map.isEmpty() ? View.GONE : View.VISIBLE);
	    	 }
	         break;
	     case 6:
	    	 if (data != null) {
	    		 fill_material_table(table_material, new_dish.prepare_material_detail);
	    	 }
	         break;
	     case 7: //��������
	    	 if (data != null) {
	    		 String  name = data.getStringExtra("edit_content_output");
	        	 new_dish.name_chinese = name;
	        	 new_dish.name_english = HanziToPinyin.getPinYin(name);
	        	 if (new_dish.name_english.length() > 13) new_dish.name_english = new_dish.name_english.substring(0, 13);//Ӣ����������󳤶�
	        	 makedish_name.setText(name);
	    	 }
	         break;
	     case 17:
        	 if (data != null) {
        		 String  name = data.getStringExtra("edit_content_output");
        		 new_dish.intro = name;
        		 init_param();
        	 }
             break;
	     case 8: // ��¼����
	    	 if (Account.is_login) {
	    		 Log.v("MakeDishActivityJ", "login return success, do upload");
	    		 new_dish.author_id = Account.userid;
         		 new_dish.author_name = Account.username;
	    		 new_dish.saveDishParam(); // �û���¼��Ҫ���洴������Ϣ
	    		 HttpUtils.uploadDish(new_dish, MakeDishActivityJ.this);
	    	 }
	    	 break;
	     case 9:  
	        	if (Account.is_login) {
		    		 Log.v("DishActivity", "login return success, do favorite");
		    		 HttpUtils.favorite(new_dish, MakeDishActivityJ.this.handler);
		    	}
	            break;
	     case 10:  
	        	fill_param_table(table_zhuliao_param);
	            break;
	     case 11:  
	        	fill_param_table(table_fuliao_param);
	            break;
	     case 12:
	        	if (data != null) {
		        	fill_liao_table(table_tiaoliao, new_dish.tiaoliao_content_map);
	        	}
	        	break;
	     case 13:  
	        	if (Account.is_login) {
		    		 Log.v("DishActivity", "login return success, do cancel uploaded");
		    		 HttpUtils.deleteDishInServer(new_dish, MakeDishActivityJ.this.handler);
		    	}
	            break;
	     
	     }
         
         //makedish_img.setImageDrawable(new_dish.img_drawable);
         makedish_img.setImageBitmap(new_dish.img_bmp);
     }
	 
	 public Handler getHandler() {
		 return handler;
	 }
	 
	 // �����Ϻ͵���
	 public void fill_liao_table(TableLayout tableLayout, LinkedHashMap<String, String> lmap) {
         Log.v("MakedishActivityj", "fill_liao_table  lmap.size() = " + lmap.size());
		 tableLayout.removeAllViews();
		 //��һ��Ԫ�������ͼ��
		 if (tableLayout.equals(table_qiangguoliao)) add_qiangguoliao_tv.setVisibility(editable && lmap.isEmpty() ? View.VISIBLE : View.GONE);
		 if (tableLayout.equals(table_tiaoliao)) add_tiaoliao_tv.setVisibility(editable && lmap.isEmpty() ? View.VISIBLE : View.GONE);
		 
		 for (Iterator<String> it = lmap.keySet().iterator();it.hasNext();)
		 {
			 String key = it.next();
		     add_row(tableLayout, key, lmap.get(key), false);
		     Log.v("MakeDishActivityJ", "key = " + key + " value = " + lmap.get(key));
		 }
	 }
	 
	 public void fill_table(TableLayout tableLayout, LinkedHashMap<String, String> lmap) {
		 tableLayout.removeAllViews();
		 //��һ��Ԫ�������ͼ��
		 if (tableLayout.equals(tableLayout_zhuliao)) add_zhuliao.setVisibility(editable && lmap.isEmpty() ? View.VISIBLE : View.GONE);
		 if (tableLayout.equals(tableLayout_fuliao)) add_fuliao.setVisibility(editable && lmap.isEmpty() ? View.VISIBLE : View.GONE);
		 int i = 0;
		 for (Iterator<String> it = lmap.keySet().iterator();it.hasNext();++i)
		 {
		     String key = it.next();
		     String value = lmap.get(key);
		     add_row(tableLayout, key, value, i == 0);
		     Log.v("MakeDishActivityJ", "key = " + key + " value = " + lmap.get(key));
		 }
	 }
	 
	 public void fill_param_table(TableLayout tableLayout) {
		 if (!editable) return;
		 tableLayout.removeAllViews();
		 if (tableLayout.equals(table_zhuliao_param)) {
			 makedish_set_zhuliao_param_tv.setVisibility(editable && new_dish.zhuliao_content_map.isEmpty() ? View.VISIBLE : View.GONE);
			 if (!new_dish.zhuliao_content_map.isEmpty()) {
				 add_row(tableLayout, "�¶�", (new_dish.zhuliao_temp & 0xff) + "��C", true);
				 add_row(tableLayout, "ʱ��", Tool.sec2str(new_dish.zhuliao_time), false);
				 add_row(tableLayout, "����", jiaoban[(new_dish.zhuliao_jiaoban_speed & 0xff)-1], false);
			 }
		 }
		 else if (tableLayout.equals(table_fuliao_param)) {
			 makedish_set_fuliao_param_tv.setVisibility(editable && new_dish.fuliao_content_map.isEmpty() ? View.VISIBLE : View.GONE);
			 if (!new_dish.fuliao_content_map.isEmpty()) {
				 add_row(tableLayout, "�¶�", (new_dish.fuliao_temp & 0xff) + "��C", true);
				 add_row(tableLayout, "ʱ��", Tool.sec2str(new_dish.fuliao_time), false);
				 add_row(tableLayout, "����", jiaoban[(new_dish.fuliao_jiaoban_speed & 0xff) - 1], false);
			 }
		 }
		 Log.v("makedish", "fill_param_table, tableLayout.childViewCount = " + tableLayout.getChildCount());
	 }
	 
	 public void fill_material_table(TableLayout tableLayout, ArrayList<Material> list) {
		 if (tableLayout.getChildCount() >= 2) {
			 tableLayout.removeViews(0, tableLayout.getChildCount() - 1);
		 }
		 Log.v("MakeDishActivityJ", "tableLayout.getchildcount() = " + tableLayout.getChildCount()); 
		 
		 for (int i = 0; i < list.size(); ++i) {
			 Material m = list.get(i);
			 Log.v("MakeDishActivityJ", "list = " + list + "m = " + m);
			 if (m.path != null && !m.path.isEmpty() && m.img_bmp == null) {
				 m.img_bmp = Tool.decode_path_bitmap(new_dish.getDishDirName() + m.path, Constants.DECODE_MATERIAL_SAMPLE);
			 }
			 else if (m.img_resid != 0 && m.img_bmp == null) {
				 m.img_bmp = Tool.decode_res_bitmap(m.img_resid, MakeDishActivityJ.this, Constants.DECODE_MATERIAL_SAMPLE);
			 }
			 add_material_row(tableLayout, m, i);
			 Log.v("MakeDishActivityJ", "add_material : " + m.description + ", " + m.img_bmp);
		 }
	 }
	 
	 protected void add_row(TableLayout tableLayout, String key, String value, boolean isfirst) {
		 TableRow tableRow = new TableRow(this);  
		 TableLayout.LayoutParams lp = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		 lp.setMargins(0, 0, 0, 2);
		 tableRow.setPadding(20, 10, 10, 0);
		 tableRow.setLayoutParams(lp);
		 
		 TextView textView0 = new TextView(tableLayout.getContext());
		 textView0.setTextSize(17);
		 textView0.setWidth(70);
		 textView0.setTextColor(Color.TRANSPARENT);
		 String title = "ԭ�ϣ�";
		 if (isfirst && (tableLayout.equals(tableLayout_zhuliao) || tableLayout.equals(tableLayout_fuliao))) {
			 title = "ԭ�ϣ�";
			 textView0.setTextColor(Color.GRAY);
		 }
		 if (isfirst && (tableLayout.equals(table_zhuliao_param) || tableLayout.equals(table_fuliao_param))) {
			 title = "������";
			 textView0.setTextColor(Color.GRAY);
		 }
		 textView0.setText(title);
		 if (!editable) textView0.setTextColor(Color.TRANSPARENT);
		 
		 TextView textView = new TextView(tableLayout.getContext());  
         textView.setText(key);
         textView.setWidth(100);
         textView.setTextSize(17);
         textView.setTextColor(Color.rgb(85, 85, 85));
         
         TextView textView2 = new TextView(tableLayout.getContext());
         textView2.setText(value);
         textView2.setWidth(70);
         textView2.setTextSize(17);
         textView2.setTextColor(Color.rgb(85, 85, 85));
        
         tableRow.addView(textView0);
         tableRow.addView(textView);  
         tableRow.addView(textView2); 
         tableLayout.addView(tableRow, tableLayout.getChildCount());
	 }
	 
	 protected void add_material_row(TableLayout tableLayout, Material m, int index) {
		 TextView textView = new TextView(tableLayout.getContext());  
		 TableLayout.LayoutParams tlp = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		 tlp.setMargins(5, 50, 150, 0);
		 textView.setLayoutParams(tlp);
		 BitmapDrawable image = new BitmapDrawable(this.getResources(), m.img_bmp);
		 textView.setCompoundDrawablesWithIntrinsicBounds(null, null, image, null);
		 //textView.setCompoundDrawablePadding(-100);
		 textView.setId(index);
		 index = Math.min(index, Tool.material_index.length - 1);
         //textView.setText(Tool.material_index[index])
		 textView.setText(m.type);
         textView.setTextSize(12);
         textView.setTextColor(Color.rgb(0, 0, 0));
         if (editable) textView.setOnClickListener(new OnClickListener() {  
             @Override  
             public void onClick(View v) {  
             	Log.v("MakeDishActivityJ", "v.getId = " + v.getId());
             	Intent intent = new Intent(MakeDishActivityJ.this, ImageEditActivity.class);
             	intent.putExtra("edit_title", "����ͼ��");
             	intent.putExtra("dish_id", new_dish.dishid);
             	intent.putExtra("material_index", v.getId());
             	startActivityForResult(intent, 6);
             }  
         });

         TextView textView2 = new TextView(tableLayout.getContext());
         TableLayout.LayoutParams tlp2 = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		 tlp2.setMargins(150, 5, 50, 0);
		 textView2.setLayoutParams(tlp2);
         textView2.setText(m.description);
         textView2.setTextSize(20);
         textView2.setTextColor(Color.rgb(85, 85, 85));
         //textView2.setLayoutParams(lp2);
         
         tableLayout.addView(textView, tableLayout.getChildCount() - 1);
         tableLayout.addView(textView2, tableLayout.getChildCount() - 1);
	 }
	 
	 @Override  
	 protected void onDestroy() {  
	     super.onDestroy();  
	     if (editable) {
	    	 new_dish.saveDishParam();
	    	 Log.v("MakeDishActivityJ", "onDestroy saveDishParam to file"); 
	     }
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
	 
	@Override  
    public boolean onTouch(View v, MotionEvent event) {  
        createVelocityTracker(event);  
        switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
        	xDown = event.getRawX();  
            yDown = event.getRawY();
            real_yDown = event.getRawY();  
            break;  
        case MotionEvent.ACTION_MOVE:  
        	xMove = event.getRawX(); 
            float yMove = event.getRawY();
            //��ľ���  
            int distanceX = (int) (xMove - xDown);  
            int real_distanceY = (int) (yMove - real_yDown);
            //��ȡ˳ʱ�ٶ�  
            int xSpeed = getScrollVelocity();  
            //�������ľ�����������趨����С�����һ�����˲���ٶȴ��������趨���ٶ�ʱ�����ص���һ��activity  
            if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN && Math.abs(distanceX) > Math.abs(real_distanceY)) {  
                finish();  
            }  
            else {
//            	int distanceY = (int) (yDown - yMove);
//            	layout_makedish.smoothScrollBy(0, distanceY);
            	yDown = event.getRawY();
            	layout_makedish.onTouchEvent(event);
            }
            break;  
        case MotionEvent.ACTION_UP: 
        	xSpeed = getScrollVelocity();
        	layout_makedish.fling((int) -yspeed);
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
        yspeed = mVelocityTracker.getYVelocity();
        return Math.abs(velocity);  
    } 
}

