package study.hellogridview;

import study.hellogridview.Dish.Material;
import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
 
public class HttpUtils {
    private static AsyncHttpClient client = new AsyncHttpClient();    //实例话对象
    
    static
    {
        client.setTimeout(10*000);   //设置链接超时，如果不设置，默认为10s
    }
    
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }
    public static AsyncHttpClient getClient()
    {
        return client;
    }
    
    // upload dish.param, main image and material images
    public static boolean uploadDish(Dish dish, Context activity) {
    	if (dish.isBuiltIn()) return true;
    	
    	String param_path = dish.getDishDirName() + "/" + Constants.DISH_PARAM_FILENAME;
    	uploadFile(param_path, activity);
    	
    	String main_img_path = dish.getDishDirName() + "/" + Constants.DISH_IMG_FILENAME;
    	uploadFile(main_img_path, activity);
    	
    	String main_img_tiny_path = dish.getDishDirName() + "/" + Constants.DISH_IMG_TINY_FILENAME;
    	uploadFile(main_img_tiny_path, activity);
    	
    	for (int i = 0; i < dish.prepare_material_detail.size(); ++i) {
			 Material m = dish.prepare_material_detail.get(i);
			 uploadFile(m.path, activity);
		}
    	return false;
    }
    
    private static boolean uploadFile(String file_path, Context activity) {
    	//TODO
    	return false;
    }
}