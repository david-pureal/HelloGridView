package study.hellogridview;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;

public class Account {
	public static String userid = "";
	public static String username = "";
	public static String user_icon_link = "";
	public static Bitmap user_icon_img;
	public static boolean is_login = false;
	public static String device_id = ""; // 标志一个手机设备，有可能会重复，在用户没有登录的情况下使用, SplashActivity 负责初始化
	
	public static ArrayList<Integer> favorites = new ArrayList<Integer>(); 
	public static ArrayList<Integer> local_favorites = new ArrayList<Integer>();
	
	// let server side remember this user's info
	// note: do twice because the first http fails sometime, reason unknown!!
	public static void register(final LoginActivity context) {
		HttpUtils.register(userid, username, context);
		HttpUtils.register(userid, username, context);
	}

	public static void setUserIcon(final String userIconUrl, final LoginActivity context) {
		// TODO Auto-generated method stub
		if (user_icon_link != null && user_icon_link.equals(userIconUrl)) {
			context.handler.sendEmptyMessage(Constants.MSG_ID_DOWNLOAD_ICON);
			return;
		}
		
		user_icon_link = userIconUrl;
		final Bitmap old_icon_img = user_icon_img;
		final String filename = userid + "_icon.jpg";
		new Thread() {
			@Override
			public void run() {
				File path = new File(Tool.getInstance().getModulePath() + filename);
				if (!path.exists()) {
					HttpUtils.downloadFile(userIconUrl, filename); // 同步http请求不能在ui线程做
				}

				// if download success, load from file
				user_icon_img = Tool.decode_path_bitmap(filename);
				
				if (user_icon_img == null) {
					Log.e("Account", "get user(" + username + ") icon img failed!");
				}
				
				// let ui thead update img and username
				context.handler.sendEmptyMessage(Constants.MSG_ID_DOWNLOAD_ICON);
				
				if (old_icon_img != null) {
					old_icon_img.recycle();
				}
				
				register(context);
			}
		}.start();
	}
	
	public static boolean isFavorite(Dish dish) {
		return Account.favorites.indexOf(dish.dishid) != -1;
	}
}
