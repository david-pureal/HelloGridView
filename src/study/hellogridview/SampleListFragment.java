package study.hellogridview;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SampleListFragment extends ListFragment {

	public SlidingMenu sm;
	public Handler handler;
	public void set_sm(SlidingMenu sm) {
		this.sm = sm;
	}
	public void set_handler(Handler h) {
		this.handler = h;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		SampleAdapter adapter = new SampleAdapter(getActivity());
	
		adapter.add(new SampleItem("时蔬", R.drawable.vegetable_96));
		adapter.add(new SampleItem("家常菜", R.drawable.home_dish_96));
		adapter.add(new SampleItem("下饭菜", R.drawable.spicy_dish_96));
		adapter.add(new SampleItem("川菜", R.drawable.chuan_dish_96));
		adapter.add(new SampleItem("海鲜", R.drawable.seafood_96));
		
		
		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView parent, View v,int position, long id)   
	{        
		//super.onListItemClick(parent, v, position, id);
	    Log.v("SampleListFragment", "Click position = " + position);
	}

	private class SampleItem {
		public String tag;
		public int iconRes;
		public SampleItem(String tag, int iconRes) {
			this.tag = tag; 
			this.iconRes = iconRes;
		}
	}

    
	public int pos;
	public class SampleAdapter extends ArrayAdapter<SampleItem> {

		public SampleAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			pos = position;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			icon.setFocusable(false);  
			icon.setFocusableInTouchMode(false);
			
			TextView title = (TextView) convertView.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);
			title.setTextColor(Color.BLACK);
			title.setFocusable(false);  
			title.setFocusableInTouchMode(false);
			
			
			Log.v("SampleListFragment", "title = " + title.getText());
			
			convertView.setOnClickListener(new OnClickListener() {  
				@Override  
	            public void onClick(View v) {  
	            	sm.toggle();
	            	Message msg = new Message();
	            	msg.what = 0x567;
	            	msg.obj = pos;
	            	//TODO 返回选择的分类
	            	handler.sendMessage(msg);
	                //finish();//关闭当前Activity  
	            }  
			});

			return convertView;
		}

	}
}
