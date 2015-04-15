package study.hellogridview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
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
	
	public String makeTinyImage(Dish dish/*BitmapDrawable input, short dishid*/) {
		Bitmap src_bmp = dish.img_drawable.getBitmap();
		
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

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
		if(wm != null){
			WifiInfo wi = wm.getConnectionInfo();
			if (wi != null) {
				String s = wi.getSSID();
				if(s.length()>2&&s.charAt(0) == '"'&&s.charAt(s.length() -1) == '"'){
					return s.substring(1,s.length()-1);
				}
			}
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
						d.img_drawable = (BitmapDrawable) Drawable.createFromPath(d.getDishDirName() + d.img_path);
						
						// skip local uploaded dish if it's not in web dish_list
						if (Dish.alldish_web.indexOf(dishid) == -1 && d.isAppBuiltIn()) continue;
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
			d.zhuliao_temp = (byte) dishj.getInt("zhuliao_temp");
			d.zhuliao_jiaoban_speed = (byte) dishj.getInt("zhuliao_jiaoban_speed");
			d.fuliao_time = (short) dishj.getInt("fuliao_time");
			d.fuliao_temp = (byte) dishj.getInt("fuliao_temp");
			d.fuliao_jiaoban_speed = (byte) dishj.getInt("fuliao_jiaoban_speed");
			
			d.water = (byte) dishj.getInt("water");
			d.water_weight = dishj.getInt("water_weight");
			d.oil = (byte) dishj.getInt("oil");
			d.qiangguoliao = (byte) dishj.getInt("qiangguoliao");
			d.qiangguoliao_content = dishj.getString("qiangguoliao_content");
			if (dishj.has("sound")) d.sound = dishj.getInt("sound");
			
			d.type = dishj.getInt("type");
			
			d.img_path = dishj.getString("img_path");
			d.img_tiny_path = dishj.getString("img_tiny_path");
			
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
			
			if (dishj.has("material_detail")) {
				JSONArray array_material = dishj.getJSONArray("material_detail");
				for (int i = 0; i < array_material.length(); ++i) {
					JSONObject element = array_material.getJSONObject(i);
					Material m = d.new Material();
					m.description = element.getString("description");
					m.path = element.getString("path");
					
					// done in MakeDishActivity::fill_material_table
//					Bitmap bmp = BitmapFactory.decodeFile(m.path);
//	        		bmp.setDensity(dm.densityDpi);
//					m.img_drawable = new BitmapDrawable(bmp);
					d.prepare_material_detail.add(m);
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
}
