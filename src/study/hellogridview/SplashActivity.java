package study.hellogridview;

import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {
	
	ImageView splash_img;
	TextView splash_text;
	TextView skip;
	
	Handler handler;
	
	boolean need_skip = false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,      
                WindowManager.LayoutParams. FLAG_FULLSCREEN); 
        setContentView(R.layout.splash);
        
        handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == 0x789) {   
                	splash_img.setImageResource(R.drawable.dish_puppy);
                    splash_text.setText("每个人都是厨房里的艺术家");      	
                }  
            }  
        };
        
        Tool.typeFace = Typeface.createFromAsset(getAssets(), "fonts/hanyitaiji.ttf");
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/minijianguli.ttf");
        ((TextView) findViewById(R.id.textView1)).setTypeface(tf);

        splash_img = (ImageView) findViewById(R.id.splash_img); 
        splash_text = (TextView) findViewById(R.id.splash_text);
        splash_text.setTypeface(Tool.typeFace);
        
        skip = (TextView) findViewById(R.id.skip);
        skip.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	need_skip = true;
            	skip.setVisibility(View.GONE);
            }  
        }); 
        
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int result;
                long startTime = System.currentTimeMillis();
                
                splash_text.setTypeface(Tool.typeFace);
                
                Tool.getDeviceId(SplashActivity.this);
                result = getWebDish();
                
                result = loadLocalDish();
                Tool.preload_common_res(SplashActivity.this);
                
                long loadingTime = System.currentTimeMillis() - startTime;
                if (loadingTime < 3000) {
                    try {
                    	Log.v("SplashActivity", "loadingTime = " +  loadingTime);
                    	
                    	long total = 3000 - loadingTime;
                    	long cur = 0;
                    	while(!need_skip && cur < total) {
                    		Thread.sleep(300);
                    		cur += 300;
                    	}
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

//                // 更换图片
//                handler.sendEmptyMessage(0x789);
//                
//                startTime = System.currentTimeMillis();
//                result = loadLocalDish();
//                Tool.preload_common_res(SplashActivity.this);
//                loadingTime = System.currentTimeMillis() - startTime;
//                if (loadingTime < 2000) {
//                    try {
//                    	long total = 2000 - loadingTime;
//                    	long cur = 0;
//                    	while(!need_skip && cur < total) {
//                    		Thread.sleep(300);
//                    		cur += 300;
//                    	}
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
                return result;
            }
 
            @Override
            protected void onPostExecute(Integer result) {
            	Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            };
        }.execute(new Void[]{});
    }

	private int loadLocalDish() {
		Tool.getInstance().dm = this.getResources().getDisplayMetrics();
		Tool.getInstance().loadLocalDish();
		Tool.getInstance().loadLocalUserData();
		
        return 0;
    }
	
	protected int getWebDish() {
		HttpUtils.getAllDish();
		Tool.getInstance().getWebDish();
		return 0;
	}
}
