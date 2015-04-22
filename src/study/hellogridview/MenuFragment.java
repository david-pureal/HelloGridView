package study.hellogridview;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuFragment extends Fragment {
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.slidingmenu, null);
	}
	
	public SlidingMenu sm;
	public Handler handler;
	public void set_sm(SlidingMenu sm) {
		this.sm = sm;
	}
	public void set_handler(Handler h) {
		this.handler = h;
	}
	
	TextView hot_tv;
	TextView builtin_tv;
	TextView favorite_tv;
	TextView all_tv;
	TextView share_tv;
	TextView account_tv;
	TextView makedish_tv;
	TextView login_tv;
	TextView setting_tv;
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		ImageView m_hot = (ImageView) getView().findViewById(R.id.hot_dishes);
		m_hot.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	
            	Intent intent = new Intent(getActivity(), MainActivity.class);
            	intent.putExtra("title", String.valueOf("热门菜谱")); 
            	if (getActivity().getClass() != MainActivity.class)
            		startActivity(intent); 
            	else 
            		sm.toggle(); 
            	 
            }
        });
		hot_tv = (TextView) getView().findViewById(R.id.hot_dishes_tv);
		hot_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	
            	Intent intent = new Intent(getActivity(), MainActivity.class);
            	intent.putExtra("title", String.valueOf("热门菜谱")); 
            	if (getActivity().getClass() != MainActivity.class)
            		startActivity(intent); 
            	else 
            		sm.toggle(); 
            	 
            }
        });
		
		ImageView m_buildin = (ImageView) getView().findViewById(R.id.builtin_dishes);
		m_buildin.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(getActivity(),BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("快捷菜谱")); 
            	//if (getActivity().getClass() != BuiltinDishes.class)
            		startActivity(intent); 
            	//else 
            		//sm.toggle(); 
            	 
            }
        });	
		builtin_tv = (TextView) getView().findViewById(R.id.builtin_dishes_tv);
		builtin_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(getActivity(),BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("快捷菜谱")); 
            	//if (getActivity().getClass() != BuiltinDishes.class)
            		startActivity(intent); 
            	//else 
            	//	sm.toggle(); 
            	 
            }
        });
		
		ImageView m_favorite = (ImageView) getView().findViewById(R.id.favorites);
		m_favorite.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
            	if (!Account.is_login) {
            		Intent intent = new Intent(getActivity(), LoginActivity.class);
                	intent.putExtra("header", "请先登录");
                	startActivityForResult(intent, 10);
            	}
            	else {
	            	Intent intent = new Intent(getActivity(),BuiltinDishes.class);
	            	intent.putExtra("title", String.valueOf("收藏菜谱")); 
	            	startActivity(intent); 
            	}
            }  
        });	
		favorite_tv = (TextView) getView().findViewById(R.id.favorites_tv);
		favorite_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	if (!Account.is_login) {
            		Intent intent = new Intent(getActivity(), LoginActivity.class);
                	intent.putExtra("header", "请先登录");
                	startActivityForResult(intent, 10);
            	}
            	else {
	            	Intent intent = new Intent(getActivity(),BuiltinDishes.class);
	            	intent.putExtra("title", String.valueOf("收藏菜谱")); 
	            	startActivity(intent); 
            	}  
            }  
        });	
		
		ImageView m_alldishes = (ImageView) getView().findViewById(R.id.alldishes);
		m_alldishes.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(getActivity(), AllDish.class);
            	intent.putExtra("title", String.valueOf("全部菜谱")); 
            	if (getActivity().getClass() != AllDish.class)
            		startActivity(intent); 
            	else 
            		sm.toggle(); 
                //finish();//关闭当前Activity  
            }  
        });
		all_tv = (TextView) getView().findViewById(R.id.alldishes_tv);
		all_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
            	Intent intent = new Intent(getActivity(),AllDish.class);
            	intent.putExtra("title", String.valueOf("全部菜谱")); 
            	if (getActivity().getClass() != AllDish.class)
            		startActivity(intent); 
            	else 
            		sm.toggle(); 
                //finish();//关闭当前Activity  
            }  
        });
		
		ImageView temp = null; 
		temp = (ImageView) getView().findViewById(R.id.share);
//		temp.setOnClickListener(new OnClickListener() {  
//            @Override  
//            public void onClick(View v) {  
//                startActivity(new Intent(getActivity(),BuiltinDishes.class));  
//                //finish();//关闭当前Activity  
//            }  
//        });
		temp = (ImageView) getView().findViewById(R.id.account);
//		temp.setOnClickListener(new OnClickListener() {  
//            @Override  
//            public void onClick(View v) {  
//                startActivity(new Intent(getActivity(),BuiltinDishes.class));  
//                //finish();//关闭当前Activity  
//            }  
//        });
		temp = (ImageView) getView().findViewById(R.id.makedish);
		temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	Intent intent = new Intent(getActivity(), BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("自编菜谱")); 
//            	if (getActivity().getClass() != AllDish.class)
//            		startActivity(intent); 
//            	else 
//            		sm.toggle(); 
                startActivity(intent);  
                //finish();//关闭当前Activity  
            }  
        });
		makedish_tv = (TextView) getView().findViewById(R.id.makedish_tv);
		makedish_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) { 
            	Intent intent = new Intent(getActivity(), BuiltinDishes.class);
            	intent.putExtra("title", String.valueOf("自编菜谱")); 
//            	if (getActivity().getClass() != AllDish.class)
//            		startActivity(intent); 
//            	else 
//            		sm.toggle(); 
                startActivity(intent);  
                //finish();//关闭当前Activity  
            }  
        });
		
		temp = (ImageView) getView().findViewById(R.id.login);
		temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(getActivity(), LoginActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		
		TextView login_tv = (TextView) getView().findViewById(R.id.login_tv);
		login_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(getActivity(), LoginActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		
		temp = (ImageView) getView().findViewById(R.id.setting);
		temp.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(getActivity(), SettingActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
		setting_tv = (TextView) getView().findViewById(R.id.setting_tv);
		setting_tv.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                startActivity(new Intent(getActivity(), SettingActivity.class));  
                //finish();//关闭当前Activity  
            }  
        });
//		temp = (ImageView) getView().findViewById(R.id.tips); 
//		temp.setOnClickListener(new OnClickListener() {  
//            @Override  
//            public void onClick(View v) {  
//                startActivity(new Intent(getActivity(),BuiltinDishes.class));  
//                //finish();//关闭当前Activity  
//            }  
//        });
	}
	
	@Override  
    public void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
        Log.v("MenuFragment", "requestCode = " + requestCode + "resultCode =" + resultCode);
        switch (requestCode) {  
        case 10:  
        	if (Account.is_login) {
	    		 Log.v("MenuFragment", "login return success, see all favorites");
	    		 Intent intent = new Intent(getActivity(),BuiltinDishes.class);
	             intent.putExtra("title", String.valueOf("收藏菜谱")); 
	             startActivity(intent);
	    	}
            break;  
        }
	}
}
