package study.hellogridview;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

public interface MyActivity{
	
	
	public Handler getHandler();


	
//	public void OnResp(RespPackage rp);
	
//	static class MyHandler extends Handler {  
//        WeakReference<PopupActivity> mActivity;  
//
//        MyHandler(PopupActivity activity) {  
//                mActivity = new WeakReference<PopupActivity>(activity);  
//        }  
//
//        @Override  
//        public void handleMessage(Message msg) {  
//                PopupActivity theActivity = mActivity.get();  
//                switch (msg.what) {  
//                case 0:  
//                        theActivity.popPlay.setChecked(true);  
//                        break;  
//                }  
//        }  
//	}
}
