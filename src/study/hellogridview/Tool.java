package study.hellogridview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

public class Tool {
	static Tool tool;
	public static Tool getInstance() { 
		if (tool == null) {  
			tool = new Tool();  
	    }
		return tool;
	}
	
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
        String filename = dish.getDishDirName() + "/" + Constants.DISH_IMG_TINY_FILENAME; 
        FileOutputStream fos;
        try {
        	fos = new FileOutputStream(filename);
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
	
	public void writeFile(String data, String path) {
		FileOutputStream fos;
        try {
        	fos = new FileOutputStream(path);
        	fos.write(data.getBytes());
        	fos.close();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
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
}
