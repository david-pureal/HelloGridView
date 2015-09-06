package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import study.hellogridview.Dish.Material;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class Tool {
	static Tool tool;
	public static Tool getInstance() { 
		if (tool == null) {  
			tool = new Tool();  
	    }
		return tool;
	}
	
	public DisplayMetrics dm;
	//public String alldish_jsonstr = "";
	public String downloading_dish_allfiles = "";
	
	public static Typeface typeFace;
	
	public static String [] material_index = {"①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩"};

	public static HashMap<Integer, Bitmap> image_res_mgr = new HashMap<Integer, Bitmap>(); //一些可以公用的图片资源，比如提示语，启动时加载
	
	public static ArrayList<Bitmap> guide_image = new ArrayList<Bitmap>(); //首次启动时，操作引导页所需图片
	
	public String makeTinyImage(Dish dish/*BitmapDrawable input, short dishid*/) {
		Bitmap src_bmp = dish.img_bmp;
		
		// 先调整大小
		int width_tiny = 102;
		int height_tiny = 70;
		int width = src_bmp.getWidth();  
        int height = src_bmp.getHeight();  
        int newWidth = width_tiny;  
        int newHeight = height_tiny;  
  
        // calculate the scale  
        float scaleWidth = ((float) newWidth) / width;  
        float scaleHeight = ((float) newHeight) / height;  
  
        // create a matrix for the manipulation  
        Matrix matrix = new Matrix();  
        // resize the Bitmap  
        matrix.postScale(scaleWidth, scaleHeight);  
        // if you want to rotate the Bitmap  
        // matrix.postRotate(45);  
  
        // recreate the new Bitmap  
        Bitmap resizedBitmap = Bitmap.createBitmap(src_bmp, 0, 0, width,  
                height, matrix, true);  
  
        // 然后压缩至小于4.7K
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        int options = 100;  
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
        while ( baos.size() / 1024.0 > 4.7f) {  //循环判断如果压缩后图片是否大于4.7kb,大于继续压缩         
            baos.reset();//重置baos即清空baos  
            options -= 1;//每次都减少3  
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
        } 
//      ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
//      Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        Log.v("Tool", "baos.size() = " + baos.size() + ", options = " + options);
        String filename = Constants.DISH_IMG_TINY_FILENAME; 
        FileOutputStream fos;
        try {
        	fos = new FileOutputStream(dish.getDishDirName() + filename);
        	baos.writeTo(fos);
        	baos.flush();
        	baos.close();
        	fos.close();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
         
        Log.v("Tool", "filename = " + filename);
        return filename; 
	}
	
	public static void compressImage(File imgfile, int size) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        int options = 100;  
        FileInputStream fs;
        Bitmap bmp = null;
		try {
			fs = new FileInputStream(imgfile);
			bmp = BitmapFactory.decodeFileDescriptor(fs.getFD());
			fs.close();
			
			bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
			int old_size = baos.size();
	        while ( baos.size() > size) {           
	            baos.reset();
	            options -= options/10;
	            bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
	            int new_size = baos.size();
		        Log.v("tool", "compressImage old_size = " + old_size + ", new_size = " + new_size);
	        }
	        
	        FileOutputStream fos = new FileOutputStream(imgfile);
        	baos.writeTo(fos);
        	baos.flush();
        	baos.close();
        	fos.close();
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	}
	
	//获取本APP的本地存储目录
	public String getModulePath() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/babaxiaochao/";
		//确保路径存在
        make_directory(path);
		return path;
	}
	
	public void make_directory(String dirname) {
		File temp = new File(dirname);
		if (!temp.exists()) temp.mkdir();
	}
	
	public boolean isPathExist(String path) {
		File temp = new File(path);
		return temp.exists();
	}
	
	public Bitmap zoomImage(Bitmap bgimage, double newWidth,  double newHeight) { 
	    // 获取这个图片的宽和高 
	    float width = bgimage.getWidth(); 
	    float height = bgimage.getHeight(); 
	    // 创建操作图片用的matrix对象 
	    Matrix matrix = new Matrix(); 
	    // 计算宽高缩放率 
	    float scaleWidth = ((float) newWidth) / width; 
	    float scaleHeight = ((float) newHeight) / height; 
	    // 缩放图片动作 
	    matrix.postScale(scaleWidth, scaleHeight); 
	    Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width, 
	                    (int) height, matrix, true); 
	    return bitmap; 
	}
	
	public void savaBitmap(Bitmap btm, String path) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 100;  
		btm.compress(Bitmap.CompressFormat.JPEG, options, baos);
		FileOutputStream fos;
        try {
        	fos = new FileOutputStream(path);
        	baos.writeTo(fos);
        	baos.flush();
        	baos.close();
        	fos.close();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public void writeFile(byte[] data, String path) {
		FileOutputStream fos;
        try {
        	fos = new FileOutputStream(path);
        	fos.write(data);
        	fos.close();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public byte [] readFile(String path) {
		try {
			FileInputStream inStream = new FileInputStream(path);
			byte [] bytes = new byte[inStream.available()];
			inStream.read(bytes);
			inStream.close();
			return bytes;
		} 
		catch (FileNotFoundException fnfe) {
			File newfile = new File(path);
			try {
				newfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	// 有bug， 第一次没有连接wifi，但是后续连上wifi后，仍然返回false
	public boolean isWifiConnected(Context context) { 
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
			if (mWiFiNetworkInfo != null) { 
				return mWiFiNetworkInfo.isConnected();
			} 
		} 
		return false; 
	}
	
	public String getSSid(Context context){
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if(wm != null) {
			WifiInfo wi = wm.getConnectionInfo();
			if (wi != null) {
				ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				Log.v("tool", "Is wifi connected = " + (wifi.getState()==State.CONNECTED));
				if (wifi.getState() == State.CONNECTED) {
					String s = wi.getSSID();
					if(s.length()>2&&s.charAt(0) == '"'&&s.charAt(s.length() -1) == '"'){
						return s.substring(1,s.length()-1);
					}
				}
			} //if (wi != null) {
		}
		return "";
	}
	
	public synchronized Module decodeBroadcast2Module(String response) {
		
		if (response == null) {
			return null;
		}
		
		String[] array = response.split(",");
		if (array==null || (array.length<2 && array.length>3) || 
				!isIP(array[0]) || !isMAC(array[1])) {
			return null;
		}
				
		Module module = new Module();
		module.setIp(array[0]);
		module.setMac(array[1]);
		if (array.length == 3) {
			module.setModuleID(array[2]);
		}
		
		return module;
	} 
	
	public boolean isIP(String str) {
		Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])" +
				"\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
				"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
				"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		return pattern.matcher(str).matches();
	}
	
	public boolean isDishDirName(String str) {
		Pattern pattern = Pattern.compile("dish[\\d]{1,5}");
		return pattern.matcher(str).matches();
	}
	
	public boolean isMAC(String str) {
		
		str = str.trim();
		if (str.length() != 12) {
			return false;
		}
		
		char[] chars = new char[12];
		str.getChars(0, 12, chars, 0);
		for (int i = 0; i < chars.length; i++) {
			if (!((chars[i]>='0' && chars[i]<='9') || (chars[i]>='A' && chars[i]<='F') || (chars[i]>='a' && chars[i]<='f'))) {
				return false;
			}
		}
		return true;
	}
	
	public void saveDevices(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("module_list", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		
		int i = 100;
		editor.putInt(Constants.KEY_PRE_ID + i, i);
		editor.putString(Constants.KEY_PRE_IP + i, "asf");
		editor.putString(Constants.KEY_PRE_MAC + i, "asf");
		editor.commit();
		Log.v("tool", "write done");
		//editor.clear().commit();
	}
	
	public Properties loadConfig(Context context, String file) {  
		Properties properties = new Properties();  
		try {  
			FileInputStream s = new FileInputStream(file);  
			properties.load(s);  
		} catch (Exception e) {  
			e.printStackTrace();  
		}  
		return properties;  
	}  
		  
	public void saveConfig(Context context, String file, Properties properties) {  
		try {  
			FileOutputStream s = new FileOutputStream(file, false);  
			properties.store(s, "");  
		} catch (Exception e){  
			e.printStackTrace();  
		}  
	}
	
	// 读取未登录时，用户收藏的菜谱
	public void loadLocalUserData() {
		String path = this.getModulePath() + Constants.LOCAL_USER_DATA;
		byte [] data = readFile(path);
		if (data != null) {
			String userdata_str = new String(data);
			try {
				JSONObject userdata = new JSONObject(userdata_str);
				if (userdata.has("favorites")) {
					JSONArray jfav = userdata.getJSONArray("favorites");
					for (int i = 0; i < jfav.length(); ++i) {
						int dishid = jfav.getInt(i);
						Account.local_favorites.add(dishid);
					}
					Log.v("Tool", "local favorites " + Account.local_favorites);
				}
			}
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void loadLocalDish() {
		LinkedHashMap<Integer, Dish> alldish_map = Dish.getAllDish();
		File root = new File(this.getModulePath());
		File [] dirs = root.listFiles();
		if (dirs == null) { return; }
		for (int i = 0; i < dirs.length; ++i) { 
			File dish_dir = dirs[i];
			String dir_name = dish_dir.getName();
			if (dish_dir.isDirectory() && isDishDirName(dir_name)) {
				try {
					int dishid = Integer.parseInt(dir_name.substring(4));
					if (alldish_map.containsKey(dishid)) continue;
					
					String param_path = dish_dir.getCanonicalPath() + "/" + Constants.DISH_PARAM_FILENAME;
					byte [] res = readFile(param_path);
					if (res == null) {
						Log.v("Tool", "directory dish" + dishid + " is damaged, skip and remove it");
						File dir = new File(dish_dir.getCanonicalPath());
						this.deleteDirectory(dir);
						continue;
					}
					String data = new String(res);

					Dish d = new Dish(dishid, "");
					if (jsonStringToDish(data, d)) {
						// skip local uploaded dish if it's not in web dish_list
						// don't bother the not uploaded local dish.
						if (!Dish.alldish_web.isEmpty() && Dish.alldish_web.indexOf(dishid) == -1 && dishid < Dish.USER_MAKE_DISH_START_ID) {
							Log.v("Tool", "local dishid=" + dishid + " is not in weblist, so delete it.");
							File dir = new File(dish_dir.getCanonicalPath());
							this.deleteDirectory(dir);
							continue;
						}
						
						BitmapFactory.Options options = new BitmapFactory.Options(); options.inPurgeable = true; 
						d.img_bmp = BitmapFactory.decodeFile(d.getDishDirName() + d.img_path, options);
						
						Dish.putDish(d);
						if (d.dishid > Dish.current_makedish_dishid) Dish.current_makedish_dishid = d.dishid;
					}
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		} // for
	}

	public boolean jsonStringToDish(String dish_str, Dish d) {
		try {
			JSONObject dishj = new JSONObject(dish_str);
			d.dishid = dishj.getInt("dishid");
			d.name_chinese = dishj.getString("name_chinese");
			d.name_english = dishj.getString("name_english");
			
			d.zhuliao_time = (short) dishj.getInt("zhuliao_time");
			if (dishj.has("qiangguo_time")) {
				d.qiangguo_time = (short) dishj.getInt("qiangguo_time");
			}
			if (dishj.has("qiangguo_temp")) {
				d.qiangguo_temp = (byte) dishj.getInt("qiangguo_temp");
			}
			d.zhuliao_time = (short) dishj.getInt("zhuliao_time");
			d.zhuliao_temp = (byte) dishj.getInt("zhuliao_temp");
			d.zhuliao_jiaoban_speed = (byte) dishj.getInt("zhuliao_jiaoban_speed");
			d.fuliao_time = (short) dishj.getInt("fuliao_time");
			d.fuliao_temp = (byte) dishj.getInt("fuliao_temp");
			d.fuliao_jiaoban_speed = (byte) dishj.getInt("fuliao_jiaoban_speed");
			
			d.water = (byte) dishj.getInt("water");
			d.water_weight = dishj.getInt("water_weight");
			d.oil = (byte) dishj.getInt("oil");
			d.qiangguoliao = (byte) dishj.getInt("qiangguoliao");
			if (dishj.has("qiangguoliao_content") && dishj.get("qiangguoliao_content") instanceof String) {
				d.qiangguoliao_content = dishj.getString("qiangguoliao_content");
			}
			if (dishj.has("sound")) d.sound = dishj.getInt("sound");
			
			d.type = dishj.getInt("type");
			
			d.img_path = dishj.getString("img_path");
			d.img_tiny_path = dishj.getString("img_tiny_path");
			
			if (dishj.has("author_id") && dishj.has("author_name")) {
				d.author_id = dishj.getString("author_id");
				d.author_name = dishj.getString("author_name");
			}
			
			if (dishj.has("device_id")) {
				d.device_id = dishj.getString("device_id");
			}
			
			if (dishj.has("intro") && dishj.get("intro") instanceof String) {
				d.intro = dishj.getString("intro");
			}
			
			// 主料，辅料和备料图文
			if (dishj.has("zhuliao_content")) {
				JSONArray array_zhuliao = dishj.getJSONArray("zhuliao_content");
				for (int i = 0; i < array_zhuliao.length(); ++i) {
					JSONObject element = array_zhuliao.getJSONObject(i);
					String key = element.keys().next().toString();  
	                String value = element.getString(key);
					d.zhuliao_content_map.put(key, value);
				}
			}
			if (dishj.has("fuliao_content")) {
				JSONArray array_fuliao = dishj.getJSONArray("fuliao_content");
				for (int i = 0; i < array_fuliao.length(); ++i) {
					JSONObject element = array_fuliao.getJSONObject(i);
					String key = element.keys().next().toString();  
	                String value = element.getString(key);
					d.fuliao_content_map.put(key, value);
				}
			}
			if (dishj.has("qiangguoliao_content") && dishj.get("qiangguoliao_content") instanceof JSONArray) {
				JSONArray array_qiangguoliao = dishj.getJSONArray("qiangguoliao_content");
				for (int i = 0; i < array_qiangguoliao.length(); ++i) {
					JSONObject element = array_qiangguoliao.getJSONObject(i);
					String key = element.keys().next().toString();  
	                String value = element.getString(key);
					d.qiangguoliao_content_map.put(key, value);
				}
			}
			if (dishj.has("tiaoliao_content")) {
				JSONArray array_tiaoliao = dishj.getJSONArray("tiaoliao_content");
				for (int i = 0; i < array_tiaoliao.length(); ++i) {
					JSONObject element = array_tiaoliao.getJSONObject(i);
					String key = element.keys().next().toString();  
	                String value = element.getString(key);
					d.tiaoliao_content_map.put(key, value);
				}
			}
			
			if (dishj.has("material_detail")) {
				JSONArray array_material = dishj.getJSONArray("material_detail");
				for (int i = 0; i < array_material.length(); ++i) {
					JSONObject element = array_material.getJSONObject(i);
					Material m = d.new Material();
					
					if (element.has("description") && element.has("path")) {
						m.description = element.getString("description");
						m.path = element.getString("path");
						d.prepare_material_detail.add(m);
					}
					else {
						Log.e("tool", "dish(" + d.dishid + ") material info error,");
					}
					
					if (element.has("type")) m.type = element.getString("type");
					
					// done in MakeDishActivity::fill_material_table
//					Bitmap bmp = BitmapFactory.decodeFile(m.path);
//	        		bmp.setDensity(dm.densityDpi);
//					m.img_drawable = new BitmapDrawable(bmp);
				}
			}
			
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public void getWebDish() {
		try {
			//LinkedHashMap<Integer, Dish> alldish_map = Dish.getAllDish();
			//JSONArray dishes = new JSONArray(Tool.getInstance().alldish_jsonstr);
			for (int i = 0; i < Dish.alldish_web.size(); ++i) {
				int dishid = Dish.alldish_web.get(i).intValue();
				String dir_name = "dish" + dishid;
				if (this.isDishDirName(dir_name) && !this.isPathExist(this.getModulePath() + dir_name)) {
					//if (alldish_map.containsKey(dishid)) continue;
					// download this dish
					File dir = new File(this.getModulePath() + dir_name);
					if (dir.exists() && dir.listFiles() == null) {
						dir.delete();
					}
					this.make_directory(this.getModulePath() + dir_name);
					HttpUtils.downloadDish(dishid);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void rename(int old_dishid, int new_dishid) {
		String old = this.getModulePath() + "/dish" + old_dishid;
		String newpath = this.getModulePath() + "/dish" + new_dishid;
		File newdir = new File(newpath);
		if (newdir.isDirectory() && newdir.exists()) {
			this.deleteDirectory(newdir);
		}
		
		File olddir = new File(old);
		olddir.renameTo(newdir);
	}
	
	public boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
	public boolean deleteDirectory(String path) {
		File olddir = new File(path);
		return deleteDirectory(olddir);
	}
	
	// 加载提示语图片资源
	public static void preload_common_res(Context context) {
		// 标准界面第一版
//		image_res_mgr.put(R.drawable.zhuliao_tiaoliao, decode_res_bitmap(R.drawable.zhuliao_tiaoliao, context));
//		image_res_mgr.put(R.drawable.add_oil, decode_res_bitmap(R.drawable.add_oil, context));
		
		// 标准界面更新后
		// 8个提示语图片和语音文件
		image_res_mgr.put(R.raw.add_fuliao_tiaoliao_chn, decode_res_bitmap(R.raw.add_fuliao_tiaoliao_chn, context));
		image_res_mgr.put(R.raw.add_fuliao_water_tiaoliao_chn, decode_res_bitmap(R.raw.add_fuliao_water_tiaoliao_chn, context));
		image_res_mgr.put(R.raw.add_oil_chn, decode_res_bitmap(R.raw.add_oil_chn, context));
		image_res_mgr.put(R.raw.add_oil_qgl_chn, decode_res_bitmap(R.raw.add_oil_qgl_chn, context));
		image_res_mgr.put(R.raw.add_zhuliao_chn, decode_res_bitmap(R.raw.add_zhuliao_chn, context));
		image_res_mgr.put(R.raw.add_zhuliao_tiaoliao_chn, decode_res_bitmap(R.raw.add_zhuliao_tiaoliao_chn, context));
		image_res_mgr.put(R.raw.add_zhuliao_water_tiaoliao_chn, decode_res_bitmap(R.raw.add_zhuliao_water_tiaoliao_chn, context));
		image_res_mgr.put(R.raw.add_zhuliao_water_chn, decode_res_bitmap(R.raw.add_zhuliao_water_chn, context));
		image_res_mgr.put(R.raw.qiangguo_ing_chn, decode_res_bitmap(R.raw.qiangguo_ing_chn, context));
		
		// 简洁界面和标准界面
		image_res_mgr.put(R.drawable.simple_bkg, decode_res_bitmap(R.drawable.simple_bkg, context));
		image_res_mgr.put(R.raw.locked, decode_res_bitmap(R.raw.locked, context));
		image_res_mgr.put(R.raw.unlock, decode_res_bitmap(R.raw.unlock, context));
		image_res_mgr.put(R.drawable.standard_bkg, decode_res_bitmap(R.drawable.standard_bkg, context));
		
		// 操作引导图片
		//image_res_mgr.put(R.raw.right_slide, decode_res_bitmap(R.raw.right_slide, context));
		
		// 左滑菜单图标
		image_res_mgr.put(R.drawable.vegetable_96, decode_res_bitmap(R.drawable.vegetable_96, context));
		image_res_mgr.put(R.drawable.home_dish_96, decode_res_bitmap(R.drawable.home_dish_96, context));
		image_res_mgr.put(R.drawable.spicy_dish_96, decode_res_bitmap(R.drawable.spicy_dish_96, context));
		image_res_mgr.put(R.drawable.chuan_dish_96, decode_res_bitmap(R.drawable.chuan_dish_96, context));
		image_res_mgr.put(R.drawable.seafood_96, decode_res_bitmap(R.drawable.seafood_96, context));
		
		// 其他
		image_res_mgr.put(R.drawable.unfavorite_dish_72, decode_res_bitmap(R.drawable.unfavorite_dish_72, context));
		image_res_mgr.put(R.drawable.favorite_dish_72, decode_res_bitmap(R.drawable.favorite_dish_72, context));
		image_res_mgr.put(R.drawable.shareto, decode_res_bitmap(R.drawable.shareto, context));
		
		// 背景图片
		image_res_mgr.put(R.drawable.bkg_darker, decode_res_bitmap(R.drawable.bkg_darker, context, Constants.DECODE_MATERIAL_SAMPLE));
		image_res_mgr.put(R.drawable.bkg, decode_res_bitmap(R.drawable.bkg, context, Constants.DECODE_MATERIAL_SAMPLE));
		
		// 盐, 油
		image_res_mgr.put(R.drawable.salt, Tool.decode_res_bitmap(R.drawable.salt, context, Constants.DECODE_MATERIAL_SAMPLE));
		image_res_mgr.put(R.drawable.material_oil, Tool.decode_res_bitmap(R.drawable.material_oil, context, Constants.DECODE_MATERIAL_SAMPLE));

	}
	
	public static Bitmap get_res_bitmap(int resid) {
		return image_res_mgr.get(resid);
	}
	
	public static Bitmap decode_res_bitmap(int resid, Context context) {
		if (!image_res_mgr.containsKey(resid) || image_res_mgr.get(resid) == null) {
			image_res_mgr.put(resid, BitmapFactory.decodeStream(context.getResources().openRawResource(resid)));
		}
		return image_res_mgr.get(resid);
	}
	
	public static Bitmap decode_res_bitmap(int resid, Context context, int sample) {
		if (!image_res_mgr.containsKey(resid) || image_res_mgr.get(resid) == null) {
			InputStream is = context.getResources().openRawResource(resid);
			BitmapFactory.Options options=new BitmapFactory.Options(); 
		    options.inJustDecodeBounds = false; 
		    options.inPreferredConfig = Bitmap.Config.RGB_565; 
		    options.inPurgeable = true; 
		    options.inInputShareable = true;
		    options.inSampleSize = sample;
		    Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
		    if (bmp == null) Log.v("tool", "bmp is null!");
		    bmp.setDensity(Tool.getInstance().dm.densityDpi/sample);
			image_res_mgr.put(resid, bmp);
		}
		return image_res_mgr.get(resid);
	}
	
	public static Bitmap decode_path_bitmap(String path) {
		String abs_path = Tool.getInstance().getModulePath() + path;
		try {
			return BitmapFactory.decodeFile(abs_path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Bitmap decode_res_bitmap2(int resid, Context context, int sample) {
		InputStream is = context.getResources().openRawResource(resid);
		BitmapFactory.Options options=new BitmapFactory.Options(); 
	    options.inJustDecodeBounds = false; 
	    options.inPreferredConfig = Bitmap.Config.RGB_565; 
	    options.inPurgeable = true; 
	    options.inSampleSize = sample;
	    Bitmap bmp = BitmapFactory.decodeStream(is, null, options);
	    if (bmp == null) Log.v("tool", "bmp is null!");
	    return bmp;
	}
	
	public static void load_guide_img(Context context) {
		int sample = 1;//Constants.DECODE_MATERIAL_SAMPLE;
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager1, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager2, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager3, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager4, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager5, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager6, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager7, context, sample));
		guide_image.add(decode_res_bitmap2(R.drawable.viewpager8, context, sample));
	}
	
	public static void unload_guide_img(Context context) {
		for (int i = 0; i < guide_image.size(); ++i) {
			guide_image.get(i).recycle();
		}
		guide_image.clear();
	}
	
	@SuppressWarnings("resource")
	public static Bitmap decode_path_bitmap(String path, int sample) {
		try {
			BitmapFactory.Options options=new BitmapFactory.Options(); 
		    options.inJustDecodeBounds = false; 
		    options.inPreferredConfig = Bitmap.Config.RGB_565; 
		    options.inPurgeable = true; 
		    options.inInputShareable = true;
		    options.inSampleSize = sample;
		    
		    FileInputStream fs = new FileInputStream(new File(path));
		    Bitmap bmp = BitmapFactory.decodeFileDescriptor(fs.getFD(), null, options);
		    bmp.setDensity(Tool.getInstance().dm.densityDpi/sample);
			return bmp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getDeviceId(Context context) {
		if (Account.device_id == null || Account.device_id.isEmpty()) {
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
			Account.device_id = tm.getDeviceId();
		}
		return Account.device_id;
	}
	
	public static String sec2str(short total_seconds) {
		int minites = total_seconds/60;
		int seconds = total_seconds - minites * 60;
		String separator = seconds < 10 ? ":0" : ":";
		//String minites_prefix =  minites < 10 ? "0" : "";
		return minites + separator + seconds + "″";
	}
	
	public static void temp_save_builtin_disk() {
		int tempid = 61000;
		for (int dishid = 1; dishid <= 12; ) {
			Dish d = Dish.getDishById(dishid);
			d.dishid = tempid;
			d.author_id = "oTyObs-ij5aWjDfGY5Agz2O1FAGI";
			d.author_name = "蒋克亮(智能小炒机)";
			Tool.getInstance().make_directory(d.getDishDirName());
			
			d.img_path = Constants.DISH_IMG_FILENAME;
			String path = d.getDishDirName() + d.img_path;
			d.img_bmp = Tool.decode_res_bitmap2(d.img, MainActivity.instance, 1);
			Tool.getInstance().savaBitmap(d.img_bmp, path);
			File file = new File(path);
			Tool.compressImage(file, Constants.MAX_MAIN_IMAGE_SIZE);
			d.img_tiny_path = Tool.getInstance().makeTinyImage(d);// 此处为相对路径
			ArrayList<Material> list = d.prepare_material_detail;
			for (int i = 0; i < list.size(); ++i) {
				 Material m = list.get(i);
				 m.path = "material_" + i + ".jpg";
				 m.img_bmp = Tool.decode_res_bitmap2(m.img_resid, MainActivity.instance, 1);
				 Tool.getInstance().savaBitmap(m.img_bmp, d.getDishDirName() + m.path);
				 File file2 = new File(d.getDishDirName() + m.path);
				 Tool.compressImage(file2, Constants.MAX_MATERIAL_IMAGE_SIZE);
			}
			d.type = Constants.DISH_MADE_BY_USER;
			d.device_id = Account.device_id;
			
			d.saveDishParam();
			
			++dishid;++tempid;
		}
	}
	
}
