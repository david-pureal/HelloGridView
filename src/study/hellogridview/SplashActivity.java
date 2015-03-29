package study.hellogridview;

import java.util.LinkedHashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {
	
	ImageView splash_img;
	TextView splash_text;
	
	Handler handler;
	
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

        splash_img = (ImageView) findViewById(R.id.splash_img); 
        splash_text = (TextView) findViewById(R.id.splash_text);
        
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int result;
                long startTime = System.currentTimeMillis();
                result = loadLocalDish();
                long loadingTime = System.currentTimeMillis() - startTime;
                if (loadingTime < 2000) {
                    try {
                        Thread.sleep(2000 - loadingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 更换图片
                handler.sendEmptyMessage(0x789);
                
                result = getWebDish();
                startTime = System.currentTimeMillis();
                loadingTime = System.currentTimeMillis() - startTime;
                if (loadingTime < 2000) {
                    try {
                        Thread.sleep(2000 - loadingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
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
		HttpUtils.getAllDish();
        return 0;
    }
	
	protected int getWebDish() {
		// TODO Auto-generated method stub
		Tool.getInstance().getWebDish();
		return 0;
	}
}
