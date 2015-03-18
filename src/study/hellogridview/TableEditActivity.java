package study.hellogridview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class TableEditActivity extends Activity {

	public TextView edit_title;
	public EditText editText;
	public TextView table_edit_addrow;
	private TableLayout tableLayout;
	
	public int dish_index;
	LinkedHashMap<String, String> content_map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_table_edit);
		
		edit_title = (TextView) findViewById(R.id.edit_title);
		Intent intent = getIntent();
		String title = intent.getStringExtra("edit_title");
		edit_title.setText(title);
		dish_index = intent.getIntExtra("dish_index", 0);
		if (title.equals("主料"))
			content_map = Dish.getAllDish()[dish_index].zhuliao_content_map;
		else
			content_map = Dish.getAllDish()[dish_index].fuliao_content_map;
		row_count = content_map.size();
		
		
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
            	save_to_dish();
            }  
        });
		
		tableLayout = (TableLayout) findViewById(R.id.table_edit_table);
		fill_table(content_map);
		
		table_edit_addrow = (TextView) findViewById(R.id.table_edit_addrow);
		table_edit_addrow.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	add_row(new String(""), new String(""));
            }  
        });
	}
	
	protected void save_to_dish() {
		content_map.clear();
		for (int i = 0; i < tableLayout.getChildCount() -1; ++i) {
			TableRow tr = (TableRow) tableLayout.getChildAt(i);
			EditText etkey = (EditText) tr.findViewWithTag(key);//findViewById(100*i);
			EditText etvalue = (EditText) tr.findViewWithTag(value);//findViewById(100*i + 1);
			if (etkey != null && !etkey.getText().toString().isEmpty() /*&& etvalue!= null && !etvalue.getText().toString().isEmpty()*/) {
				content_map.put(etkey.getText().toString(), etvalue.getText().toString());
			}
		}
		Log.v("TableEditActivity", "content_map.size() = " + content_map.size());
    	Intent intent = getIntent();
    	intent.putExtra("edit_content_output", ""); 
    	this.setResult(5, intent);  
    	this.finish();
	}
	
	 public void fill_table(LinkedHashMap<String, String> lmap) {
		 if (!lmap.isEmpty()) {
			 tableLayout.removeViews(0, 2);
		 }
		 for (Iterator<String> it =  lmap.keySet().iterator();it.hasNext();)
		 {
		     String key = it.next();
		     add_row(key, lmap.get(key));
		     Log.v("TableEditActivity", "key = " + key + " value = " + lmap.get(key));
		 }
	 }
	
	protected void add_row(String k, String v) {
		TableRow tableRow = new TableRow(this);  
		TableLayout.LayoutParams lp = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.setMargins(-3, -20, 0, 0);
		tableRow.setLayoutParams(lp);
        EditText textView = new EditText(this);  
        textView.setTag(key);
        textView.setText(k);
        EditText textView2 = new EditText(this);  
        textView2.setTag(value);
        textView2.setText(v);
        
        tableRow.addView(textView);  
        tableRow.addView(textView2);  
        
        tableLayout.addView(tableRow, tableLayout.getChildCount() - 1);
	}
	
	public int row_count;
	public String key = "table_edit_key";
	public String value = "table_edit_value";
}
