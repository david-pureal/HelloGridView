package study.hellogridview;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import study.hellogridview.Dish.Material;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageEditActivity extends Activity {
	public TextView edit_title;
	public EditText editText;
	ImageView add_material_img;
	
	Dish dish;
	ArrayList<Material> material_list;
	Material m;
	public int material_index = -1;
	String key;
	TextView material_delete;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_image_edit);
		
		
		edit_title = (TextView) findViewById(R.id.edit_title);
		Intent intent = getIntent();
		edit_title.setText(intent.getStringExtra("edit_title"));
		int dish_id = intent.getIntExtra("dish_id", 1);
		material_index = intent.getIntExtra("material_index", -1);
		if (intent.getStringExtra("edit_title").equals("备料图文")) {
			dish = Dish.getDishById(dish_id);
			material_list = dish.prepare_material_detail;
		}
		
		editText = (EditText)findViewById(R.id.edit_content);  
		editText.setText(intent.getStringExtra("edit_content_input"));
		if (material_index != -1) {
			m = material_list.get(material_index);
			key = m.description;
			editText.setText(key);
		}
		else {
			material_index = material_list.size();
		}
		
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
            	String content = ImageEditActivity.this.editText.getText().toString();
            	if (ImageEditActivity.this.edit_title.getText().toString().equals("菜谱名称")) {
            		if (content.isEmpty()) {
            			Toast.makeText(ImageEditActivity.this, "菜谱名称不能为空", Toast.LENGTH_SHORT).show();
            			return;
            		}
            	}
            	else if (ImageEditActivity.this.edit_title.getText().toString().equals("备料图文")) {
            		if (img_drawable == null) {
            			Toast.makeText(ImageEditActivity.this, "请选择图片", Toast.LENGTH_SHORT).show();
            			return;
            		}
            		
            		String str_content = editText.getText().toString();
            		
            		if (m == null) {
            			m = dish.new Material();
            			material_list.add(m);
            		}
            		m.description = str_content;
            		m.img_drawable = img_drawable;
            		m.path = img_path;
            		
            		Log.v("ImageEditActivity", "save done, material_list.size() = " + material_list.size());
            	}
            	
            	Intent intent = getIntent();
            	ImageEditActivity.this.setResult(1, intent);  
            	ImageEditActivity.this.finish();
            }  
        });
		
		material_delete = (TextView) findViewById(R.id.material_delete);
		if (material_index == material_list.size()) {
			material_delete.setVisibility(View.GONE);
		}
		material_delete.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	material_list.remove(m);
            	Log.v("ImageEditActivity", "material_list.size() = " + material_list.size());
            	Intent intent = getIntent();
            	ImageEditActivity.this.setResult(1, intent);  
            	ImageEditActivity.this.finish();
            }  
        });

		
		add_material_img = (ImageView) findViewById(R.id.add_material_img);
		add_material_img.setImageResource(R.drawable.camera);
		
		if (key != null) {
			add_material_img.setImageDrawable(m.img_drawable);
			img_drawable = m.img_drawable;
		}
		
//		Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.camera_760_548);
//		bm.setDensity(this.getResources().getDisplayMetrics().densityDpi);
//		img_drawable = new BitmapDrawable(this.getResources(), bm);
		
		add_material_img.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	ImageEditActivity.this.chooseDishImage(); 
            }  
        });
	}
	
	File tempFile;
	@SuppressLint("SdCardPath")
	public void chooseDishImage(/*View view*/) {  
        Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);  
        innerIntent.putExtra("crop", "true");// 才能出剪辑的小方框，不然没有剪辑功能，只能选取图片  
        innerIntent.putExtra("aspectX", 760);  // 出现放大和缩小  
        innerIntent.putExtra("aspectY", 548);    
		innerIntent.putExtra("outputX", 760);//输出图片大小    
		innerIntent.putExtra("outputY", 548);  
		innerIntent.putExtra("scale", true);
		innerIntent.putExtra("scaleUpIfNeeded", true);
        innerIntent.setType("image/*");      // 查看类型 详细的类型在 com.google.android.mms.ContentType 
        img_path = "material_" + material_index + ".jpg";
        tempFile = new File(dish.getDishDirName() + img_path);  
        innerIntent.putExtra("output", Uri.fromFile(tempFile));  // 专入目标文件     
        innerIntent.putExtra("outputFormat", "JPEG"); //输入文件格式    
        
        Intent wrapperIntent = Intent.createChooser(innerIntent, "先择图片"); //开始 并设置标题  
        startActivityForResult(wrapperIntent, 1); // 设返回 码为 1  onActivityResult 中的 requestCode 对应 
    }
	 
	 //调用成功反回方法  
	 @Override  
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         super.onActivityResult(requestCode, resultCode, data);  
         Log.v("ImageEditActivity", "requestCode = " + requestCode + "resultCode =" + resultCode);
         switch (requestCode) {  
         case 1:  
        	 //makedish_img.setImageDrawable();
        	 if (resultCode == -1) {
        		 Bitmap bmp = Tool.decode_path_bitmap(tempFile.getAbsolutePath(), Constants.DECODE_MATERIAL_SAMPLE);
        		 img_drawable = new BitmapDrawable(this.getResources(), bmp);
        		 add_material_img.setImageDrawable(img_drawable);
        	 }
             break;  
	     }
     }
	 
	 BitmapDrawable img_drawable;
	 String img_path;
	 TextView edit_content;
}

