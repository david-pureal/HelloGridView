package study.hellogridview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {

	public Switch swt_english;
	public Switch swt_sound;
	
	public byte use_english = 0;
	public byte use_sound = 0;
	public byte option_id = 1; // 1为语音提示；2为使用英文
	public byte opr = 0; //用户做了开启还是关闭的操作，0为关闭，1为开启
	
	Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		TCPClient.getInstance().set_settingact(this);
		
		handler = new Handler() {    
            @Override  
            public void handleMessage(Message msg) {  
                // 如果消息来自子线程  
                if (msg.what == 0x123) {   
                	
                	RespPackage rp = (RespPackage) msg.obj;
                	Log.v("SettingActivity", "SettingActivity got resp, cmdtype_head=" + (rp.cmdtype_head&0xff) + ", cmdtype_body=" + (rp.cmdtype_body&0xff));
                	if (rp.cmdtype_head == Package.ACK && rp.cmdtype_body == Package.Set_Option && !rp.is_ok) {
                		SettingActivity.this.tell("Set_Option fail!");
                	}
                	else if (rp.cmdtype_head == Package.Machine_State) {
                		SettingActivity.this.OnResp(rp);
                	}
                	Log.v("SettingActivity", "SettingActivity got event");
                }  
            }  
        };
		
		swt_english = (Switch) findViewById(R.id.switch_english);
		swt_english.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_english = opr;
            	option_id = 2;
            	
            	SettingActivity.this.do_send_msg();    
            }  
        }); 
		
		swt_sound = (Switch) findViewById(R.id.switch_sound);
		swt_sound.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
            
            @Override  
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            	opr = (byte) (isChecked ? 1 : 0);
            	use_sound = opr;
            	option_id = 1;
            	
            	SettingActivity.this.do_send_msg();
            }  
        }); 
	}

	public boolean do_send_msg() {
		Message msg = new Message();  
        msg.what = 0x345;  
        
        Package data = new Package(Package.Set_Option);
        msg.obj = data.getBytes();
        TCPClient.getInstance().sendMsg(msg); 
        
        return true;
	}
	
	public void OnResp(RespPackage rp) {
		DeviceState ds = DeviceState.getInstance();
		swt_sound.setChecked(ds.use_sound == 0x01);
		swt_english.setChecked(ds.use_english == 0x01);
	}
	
	public Handler getHandler() {
		return handler;
	}
	
	public void tell(final String info) {
		// TODO Auto-generated method stub
		if (handler != null) {
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(SettingActivity.this, info, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
