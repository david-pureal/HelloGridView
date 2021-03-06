package study.hellogridview;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ParamEditActivity extends Activity {

	public TextView edit_title;
	
	TextView makedish_temp;
	TextView makedish_time;
	TextView makedish_jiaoban;
	
	boolean is_setting_zhuliao = false;
	boolean is_setting_fuliao = false;
	boolean is_setting_qiangguoliao = false;
	
	
	Dish new_dish;
	
	public LayoutInflater inflater;
	public View self_content_view;
	public View popupView;
	
	public Integer[] temps = new Integer[36];
	public final String jiaoban[] = {"1不搅拌", "2最慢速", "3较慢速", "4中慢速", "5中快速", "6较快速", "7最快速", "8连续搅"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_param_edit);
		
		for(int i = 0; i < temps.length; i++) {
			temps[i] = Constants.MIN_TEMP + 2 * i;
		}
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		self_content_view = inflater.inflate(R.layout.activity_param_edit, null, false);
		
		edit_title = (TextView) findViewById(R.id.edit_title);
		Intent intent = getIntent();
		edit_title.setText(intent.getStringExtra("edit_title"));
		if (intent.getStringExtra("edit_title").equals("主料参数")) {
			is_setting_zhuliao = true;
		}
		if (intent.getStringExtra("edit_title").equals("辅料参数")) {
			is_setting_fuliao = true;
		}
		if (intent.getStringExtra("edit_title").equals("炝锅料参数")) {
			is_setting_qiangguoliao = true;
		}
		
		int dish_id = intent.getIntExtra("dish_id", 0);
		Log.v("ParamEditActivity", "dish_id = " + dish_id);
		new_dish = Dish.getDishById(dish_id);
		
		TextView cancel = (TextView) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                //startActivity(new Intent(BuiltinDishes.this, InputDishNameActivity.class));  
                finish();//关闭当前Activity  
            }  
        });
		
		TextView makesure = (TextView) findViewById(R.id.makesure);
		makesure.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	Intent intent = getIntent();
            	ParamEditActivity.this.setResult(5, intent);  
            	ParamEditActivity.this.finish();
            }  
        });
		
        makedish_temp = (TextView) findViewById(R.id.makedish_temp);
        int temperature_tmp = is_setting_zhuliao ? (new_dish.zhuliao_temp & 0xff) : (new_dish.fuliao_temp & 0xff);
        final int temperature = is_setting_qiangguoliao ? (new_dish.qiangguo_temp & 0xff) : temperature_tmp;
        makedish_temp.setText(temperature + "°C");
        makedish_temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,600,800,true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	ArrayWheelAdapter<Integer> adapter = new ArrayWheelAdapter<Integer>(ParamEditActivity.this, temps);
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem((temperature - Constants.MIN_TEMP) / 2);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	if (is_setting_zhuliao)  new_dish.zhuliao_temp = temps[column_1.getCurrentItem()].byteValue();
                    	if (is_setting_fuliao) new_dish.fuliao_temp = temps[column_1.getCurrentItem()].byteValue();
                    	if (is_setting_qiangguoliao) new_dish.qiangguo_temp = temps[column_1.getCurrentItem()].byteValue();
                    	
                    	makedish_temp.setText(temps[column_1.getCurrentItem()] + "°C");
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_time = (TextView) findViewById(R.id.makedish_time);
        short time_tmp = is_setting_zhuliao ? new_dish.zhuliao_time : new_dish.fuliao_time;
        final short time = is_setting_qiangguoliao ? new_dish.qiangguo_time : time_tmp;
        makedish_time.setText(Tool.sec2str(time));
        makedish_time.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_2_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView, 900, 800, true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	if (column_1 == null) Log.v("ParamEditActivity", "hours is null!");
            	column_1.setViewAdapter(new NumericWheelAdapter(ParamEditActivity.this, 0, 59));
            	column_1.setCyclic(true);
            	column_1.setCurrentItem(time / 60);
            	
            	final WheelView column_2 = (WheelView) popupView.findViewById(R.id.column_2);
            	if (column_2 == null) Log.v("ParamEditActivity", "hours is null!");
            	column_2.setViewAdapter(new NumericWheelAdapter(ParamEditActivity.this, 0, 59));
            	column_2.setCyclic(true);
            	column_2.setCurrentItem(time % 60);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	if (is_setting_zhuliao) 
                    		new_dish.zhuliao_time = (short) (column_1.getCurrentItem() * 60 + column_2.getCurrentItem());
                    	if (is_setting_fuliao) {
                    		new_dish.fuliao_time = (short) (column_1.getCurrentItem() * 60 + column_2.getCurrentItem());
                    	}
                    	if (is_setting_qiangguoliao) {
                    		new_dish.qiangguo_time = (short) (column_1.getCurrentItem() * 60 + column_2.getCurrentItem());
                    	}
                    	makedish_time.setText("" + column_1.getCurrentItem() + ":" + column_2.getCurrentItem() + "″");
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
        
        makedish_jiaoban = (TextView) findViewById(R.id.makedish_jiaoban);
        makedish_jiaoban.setText(is_setting_zhuliao ? jiaoban[(new_dish.zhuliao_jiaoban_speed & 0xff) - 1] : jiaoban[(new_dish.fuliao_jiaoban_speed & 0xff) - 1]);
        if (is_setting_qiangguoliao) makedish_jiaoban.setVisibility(View.GONE);
        makedish_jiaoban.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	popupView = inflater.inflate(R.layout.wheel_view_1_column, null, false);
            	final PopupWindow popWindow = new PopupWindow(popupView,600,800,true);
            	
            	final WheelView column_1 = (WheelView) popupView.findViewById(R.id.column_1);
            	ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(ParamEditActivity.this, jiaoban);	
            	column_1.setViewAdapter(adapter);
            	column_1.setCurrentItem(new_dish.zhuliao_jiaoban_speed & 0xff);
            	
            	Button sure = (Button) popupView.findViewById(R.id.makesure);
            	sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	if (is_setting_zhuliao) new_dish.zhuliao_jiaoban_speed = (byte) (column_1.getCurrentItem() + 1);
                    	else new_dish.fuliao_jiaoban_speed = (byte) (column_1.getCurrentItem() + 1);
                    	makedish_jiaoban.setText(jiaoban[column_1.getCurrentItem()]);
                        popWindow.dismiss(); //Close the Pop Window
                    }
                });
            	popWindow.showAtLocation(self_content_view, Gravity.CENTER, 0, 0);
            }  
        });
	}
}

