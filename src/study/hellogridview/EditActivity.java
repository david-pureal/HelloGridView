package study.hellogridview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends Activity {
	public TextView edit_title;
	public EditText editText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit);
		
		
		edit_title = (TextView) findViewById(R.id.edit_title);
		Intent intent = getIntent();
		edit_title.setText(intent.getStringExtra("edit_title"));
		
		editText = (EditText)findViewById(R.id.edit_content);  
		editText.setText(intent.getStringExtra("edit_content_input"));
		
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
            	String content = EditActivity.this.editText.getText().toString();
            	if (EditActivity.this.edit_title.getText().toString().equals("菜谱名称")) {
            		if (content.isEmpty()) {
            			Toast.makeText(EditActivity.this, "菜谱名称不能为空", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		if (content.length() > 4) {
            			Toast.makeText(EditActivity.this, "菜谱名称不能超过四个字", Toast.LENGTH_SHORT).show();
            			return;
            		}
            	}
            	
            	Intent intent = getIntent();
            	intent.putExtra("edit_content_output", content); 
            	EditActivity.this.setResult(5, intent);  
            	EditActivity.this.finish();
            }  
        });
	}
}
