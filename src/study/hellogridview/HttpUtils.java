package study.hellogridview;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import study.hellogridview.Dish.Material;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
 
public class HttpUtils {
    private static AsyncHttpClient client = new AsyncHttpClient();    //实例话对象
    private static AsyncHttpClient sync_client = new SyncHttpClient();
    
    private static String upload_uuid = "";

    static
    {
        client.setTimeout(10*000);   //设置链接超时，如果不设置，默认为10s
    }
    
    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }
    public static void getSync(String url, RequestParams params, ResponseHandlerInterface  responseHandler) {
    	sync_client.setConnectTimeout(2000);
    	sync_client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
    	sync_client.post(url, params, responseHandler);
    }
    public static AsyncHttpClient getClient()
    {
        return client;
    }
    
    // upload dish.param, main image and material images
    public static void uploadDish(final Dish dish, final Context context) {
    	if (dish.isAppBuiltIn()) return;
    	new Thread() {
			@Override
			public void run() {
				
				upload_uuid = "";
		    	try {
		    		String param_path = dish.getDishDirName() + Constants.DISH_PARAM_FILENAME;
		    		// 为了绕过第一次请求失败的bug
		    		uploadFile(param_path, dish.dishid, upload_uuid, "ignore");
		    		
		    		Log.v("HttpUtil", "uploadFile 0 threadid=" + Thread.currentThread().getId());
			    	uploadFile(param_path, dish.dishid, upload_uuid, "start");
			    	
			    	//HttpUtils.upload_semphore.acquire();
			    	Log.v("HttpUtil", "uploadFile 1 uuid =" + upload_uuid);
			    	String main_img_path = dish.getDishDirName() + Constants.DISH_IMG_FILENAME;
			    	uploadFile(main_img_path, dish.dishid, upload_uuid, "uploading");
			    	
					Log.v("HttpUtil", "uploadFile 2 uuid =" + upload_uuid);
			    	String main_img_tiny_path = dish.getDishDirName() + Constants.DISH_IMG_TINY_FILENAME;
			    	uploadFile(main_img_tiny_path, dish.dishid, upload_uuid, 
			    			dish.prepare_material_detail.isEmpty() ? "end" : "uploading");
			    	
			    	for (int i = 0; i < dish.prepare_material_detail.size(); ++i) {
						 Material m = dish.prepare_material_detail.get(i);
						 uploadFile(dish.getDishDirName() + m.path, dish.dishid, upload_uuid
								 , i == dish.prepare_material_detail.size() -1 ? "end" : "uploading");
					}
		    	} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
    }
    
    private static boolean uploadFile(final String file_path, final int dishid, final String tag, final String stage) {
    	try {
	    	RequestParams params_tmp = new RequestParams();
	    	byte [] img_data = Tool.getInstance().readFile(file_path);
	    	String filename = file_path.substring(file_path.lastIndexOf("/"));
			params_tmp.put("myfile", new ByteArrayInputStream(img_data), filename);
			params_tmp.put("tag", tag);
			params_tmp.put("stage", stage);
			String url_upload = "http://182.92.231.24:8889/upload";
			HttpUtils.post(url_upload, params_tmp, new AsyncHttpResponseHandler() {
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Log.v("HttpUtil", "uploadFile fail, file_path=" + file_path);
					if (!stage.equals("ignore")) {
						arg3.printStackTrace();
						Message m = new Message();
						m.what = Constants.MSG_ID_UPLOAD_RESULT;
						m.obj = "fail";
						TCPClient.getInstance().makedish_activity.getHandler().sendMessage(m);
					}
				}
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					// TODO Auto-generated method stub
					String res = new String(arg2);
					Log.v("HttpUtil", "uploadFile done, res=" + res + ", threadid=" + Thread.currentThread().getId());
					try {
						Message m = new Message();
						m.what = Constants.MSG_ID_UPLOAD_RESULT;
						JSONObject jsonres = new JSONObject(res);
						if (jsonres.getString("stage").equals("end")) {
							Tool.getInstance().rename(dishid, jsonres.getInt("new_dishid"));
							Dish dish = Dish.getDishById(dishid); 
							Dish.removeDish(dish);
							dish.dishid = jsonres.getInt("new_dishid");
							dish.type = Constants.DISH_UPLOAD_BY_USER;
							dish.saveDishParam();
							Dish.putDish(dish);
							
							m.obj = "success";
						} else {
							m.obj = "uploading";
							upload_uuid = jsonres.getString("uuid");
							Log.v("HttpUtil", "uploadFile got uuid =" + upload_uuid);
						}
						TCPClient.getInstance().makedish_activity.getHandler().sendMessage(m);
						//HttpUtils.upload_semphore.release();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	        });
    	} catch (Exception e1) {
    		// TODO Auto-generated catch block
    		e1.printStackTrace();
    	}
    	//}
		
    	return false;
    }
    
    public static void getAllDish() {
    	String url = "http://182.92.231.24:8889/alldish";
    	RequestParams params = new RequestParams();
    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				arg3.printStackTrace();
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String alldish_jsonstr = new String(arg2);
				JSONArray dishes;
				try {
					dishes = new JSONArray(alldish_jsonstr);
					for (int i = 0; i < dishes.length(); ++i) {
						String dir_name = dishes.getString(i);  
						int dishid = Integer.parseInt(dir_name.substring(4));
						Dish.alldish_web.add(dishid);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.v("HttpUtil", "getAllDish done, res =" + alldish_jsonstr);
			}
    	});
    }
    
    public static void getDishSubFiles(final String dirname) {
    	String url = "http://182.92.231.24:8889/download";
    	RequestParams params = new RequestParams();
    	params.put("filename", dirname);
    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				arg3.printStackTrace();
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				Tool.getInstance().downloading_dish_allfiles = new String(arg2);
				Log.v("HttpUtil", "getDishFiles " + dirname + " done, res =" + Tool.getInstance().downloading_dish_allfiles);
			}
    	});
    }

	public static void downloadDish(int dishid) {
		// get all files
		String dir_name = "/dish" + dishid + "/";
		getDishSubFiles(dir_name);
		
		JSONArray subfiles;
		try {
			subfiles = new JSONArray(Tool.getInstance().downloading_dish_allfiles);
			for (int i = 0; i < subfiles.length(); ++i) {
				String filename = subfiles.getString(i);  
				// download
				String path_param = dir_name + filename;
				downloadFile(path_param, dishid);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void downloadFile(final String filename, int dishid) {
		String url = "http://182.92.231.24:8889/download";
		RequestParams params = new RequestParams();
		params.put("filename", filename);
		
    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				arg3.printStackTrace();
				Log.v("HttpUtil", "downloadFile " + filename + " fail");
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String path = Tool.getInstance().getModulePath() + filename;
				Tool.getInstance().writeFile(arg2, path);
				Log.v("HttpUtil", "downloadFile " + filename + " done");
			}
    	});
		
	}
}