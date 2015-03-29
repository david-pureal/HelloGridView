package study.hellogridview;

import java.io.ByteArrayInputStream;

import org.apache.http.Header;

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
    	if (dish.isAppBuiltIn()) return true;
    	try {
	    	String param_path = dish.getDishDirName() + "/" + Constants.DISH_PARAM_FILENAME;
	    	uploadFile(param_path, activity, dish.dishid);
	    	Thread.sleep(500);
	    	
	    	String main_img_path = dish.getDishDirName() + "/" + Constants.DISH_IMG_FILENAME;
	    	uploadFile(main_img_path, activity, dish.dishid);
			Thread.sleep(500);
	    	
	    	String main_img_tiny_path = dish.getDishDirName() + "/" + Constants.DISH_IMG_TINY_FILENAME;
	    	uploadFile(main_img_tiny_path, activity, dish.dishid);
	    	
	    	for (int i = 0; i < dish.prepare_material_detail.size(); ++i) {
				 Material m = dish.prepare_material_detail.get(i);
				 uploadFile(m.path, activity, dish.dishid);
				 Thread.sleep(500);
			}
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return true;
    }
    
    private static boolean uploadFile(final String file_path, Context activity, int dishid) {
    	boolean isSuccess = false;
    	int max_retry_times = 3;
    	int retry_count = 0;
    	//while(retry_count < max_retry_times) {
	    	RequestParams params_tmp = new RequestParams();
	    	byte [] img_data = Tool.getInstance().readFile(file_path);
	    	String filename = file_path.substring(file_path.lastIndexOf("/"));
			params_tmp.put("myfile", new ByteArrayInputStream(img_data), filename);
			params_tmp.put("path", "dish" + dishid);
			String url_upload = "http://182.92.231.24:8889/upload";
			HttpUtils.post(url_upload, params_tmp, new AsyncHttpResponseHandler() {
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Log.v("HttpUtil", "uploadFile fail, file_path=" + file_path);
					arg3.printStackTrace();
				}
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					// TODO Auto-generated method stub
					String res = new String(arg2);
					Log.v("HttpUtil", "uploadFile done, file_path=" + file_path);
				}
	        });
    	//}
		
    	return false;
    }
    
    public static void getAllDish() {
    	String url = "http://182.92.231.24:8889/alldish";
    	RequestParams params = new RequestParams();
    	HttpUtils.get(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				arg3.printStackTrace();
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				Tool.getInstance().alldish_jsonstr = new String(arg2);
				Log.v("HttpUtil", "getAllDish done, res =" + Tool.getInstance().alldish_jsonstr);
			}
    	});
    }

	public static void downloadDish(int dishid) {
		// down dish.param
		downloadFile()
		// TODO dish.param 改为相对路径；上传后要修改dishid
	}
}