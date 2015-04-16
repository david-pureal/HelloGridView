package study.hellogridview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class InputDishNameActivity extends Activity {

	public String name;
	public EditText editText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_input_dishname);
		
		editText=(EditText)findViewById(R.id.edit_dishname);  
		name = editText.getText().toString();
		
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
            	InputDishNameActivity.this.name = InputDishNameActivity.this.editText.getText().toString();
            	if (InputDishNameActivity.this.name.isEmpty()) {
            		Toast.makeText(InputDishNameActivity.this, "请输入菜谱名称", Toast.LENGTH_SHORT).show();
            		return;
            	}
            	
            	Dish new_dish = new Dish(0, "");
            	new_dish.name_chinese = InputDishNameActivity.this.name;
            	BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true; 
        		new_dish.img_bmp = BitmapFactory.decodeResource(InputDishNameActivity.this.getResources(), R.drawable.camera, options);
        		
        		int new_dish_id = Dish.addDish(new_dish);
        		Log.v("InputDishNameActivity", "new_dish_id = " + new_dish_id);
        		
        		Intent intent = new Intent(InputDishNameActivity.this, MakeDishActivity.class);
            	intent.putExtra("dish_id", new_dish_id); 
            	startActivity(intent); 
            	finish();
            }  
        });
	}
}
