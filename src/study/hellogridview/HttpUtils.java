package study.hellogridview;

import java.io.ByteArrayInputStream;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import study.hellogridview.Dish.Material;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
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
        sync_client.setTimeout(30*1000);
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
			    	
			    	String main_img_tiny_path = dish.getDishDirName() + Constants.DISH_IMG_TINY_FILENAME;
			    	
			    	//HttpUtils.upload_semphore.acquire();
			    	Log.v("HttpUtil", "uploadFile 1 uuid =" + upload_uuid);
			    	String main_img_path = dish.getDishDirName() + Constants.DISH_IMG_FILENAME;
			    	uploadFile(main_img_path, dish.dishid, upload_uuid, "uploading");
			    	// for test
			    	//uploadFile(main_img_tiny_path, dish.dishid, upload_uuid, "uploading");
			    	//uploadFile(param_path, dish.dishid, upload_uuid, "uploading");
			    	//uploadFile(param_path, dish.dishid, upload_uuid, "uploading");
			    	
					Log.v("HttpUtil", "uploadFile 2 uuid =" + upload_uuid);
			    	
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
			params_tmp.put("tag", tag); // 
			params_tmp.put("stage", stage);
			
			params_tmp.put("device_id", Account.device_id);
			params_tmp.put("author_id", Account.is_login ? Account.userid : "");
			params_tmp.put("author_name", Account.is_login ? Account.username : "");
			
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
						if (jsonres.getString("stage").equals("ignore")) {
							return;
						}
						else if (jsonres.getString("stage").equals("end")) {
							Tool.getInstance().rename(dishid, jsonres.getInt("new_dishid"));
							Dish dish = Dish.getDishById(dishid); 
							Dish.removeDish(dish);
							dish.dishid = jsonres.getInt("new_dishid");
							dish.type = Constants.DISH_UPLOAD_VERIFYING;
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
				Log.v("HttpUtils", "getAllDish res=" + alldish_jsonstr);
				JSONArray dishes;
				try {
					dishes = new JSONArray(alldish_jsonstr);
					for (int i = 0; i < dishes.length(); ++i) {
						int dishid = Integer.parseInt(dishes.getString(i));
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
				String url = "http://182.92.231.24:8889/download";
				downloadFile(url, path_param);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void downloadFile(final String url, final String filename) {
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
	
	public static void register(final String id, final String name, final String phone, final String address, final LoginActivity context) {
		RequestParams params = new RequestParams();
		params.put("userId", id);
		params.put("userName", name);
		params.put("phone", phone);
		params.put("address", address);
		
		String url = "http://182.92.231.24:8889/register";
		
    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				arg3.printStackTrace();
				Log.v("HttpUtil", "register fail");
			}
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String res = new String(arg2);
				Log.v("HttpUtil", "register done. userId=" + id + ", userName=" + name + ", res=" + res);
				JSONArray favoritesj;
				try {
					if (Account.favorites.isEmpty()) {
						favoritesj = new JSONArray(res);
						for (int i = 0; i < favoritesj.length(); ++i) {
							int dishid = favoritesj.getInt(i);  
							if (Dish.getDishById(dishid) == null) continue;
							Account.favorites.add(dishid);
							
							// 添加到本地收藏
							if (Account.local_favorites.indexOf(dishid) < 0) {
								Account.do_local_favorite(Dish.getDishById(dishid));
							}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				context.handler.sendEmptyMessage(Constants.MSG_ID_REGISTER_DONE);
			}
    	});
	}
	
	public static void VerifyDish(final Dish dish, final boolean is_accept, final MakeDishActivityJ context) {
    	if (dish.isAppBuiltIn()) return;
    	
    	new Thread() {
			@Override
			public void run() {
				RequestParams params = new RequestParams();
				params.put("is_accept", is_accept ? "true" : "false");
				params.put("dishid", "" + dish.dishid);
				
				String url = "http://182.92.231.24:8889/verify";
				
				int retry_times = 0;
				while (Dish.getDishById(dish.dishid).isVerifying() && retry_times++ < 2) {
					Log.v("HttpUtil", "retry_times = " + retry_times);
			    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							arg3.printStackTrace();
							Log.v("HttpUtil", "verify onFailure");
						}
						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							String res = new String(arg2);
							Log.v("HttpUtil", "verify done. res=" + res + ", dishid=" + dish.dishid);
							Dish d = Dish.getDishById(dish.dishid);
							//d.type = (d.type & ~Constants.DISH_UPLOAD_VERIFYING) | (is_accept ? Constants.DISH_VERIFY_ACCEPT : Constants.DISH_VERIFY_REJECT);
							d.type = is_accept ? Constants.DISH_VERIFY_ACCEPT : Constants.DISH_VERIFY_REJECT;
							d.saveDishParam();
						}
			    	});
				} //while
				
				context.handler.sendEmptyMessage(Constants.MSG_ID_VERIFY_DONE);
			}
		}.start();
    }
	
	public static void favorite(final Dish dish, final Handler handler) {
    	new Thread() {
			@Override
			public void run() {
				RequestParams params = new RequestParams();
				params.put("dishId", "" + dish.dishid);
				params.put("userId", Account.userid);
				
				final boolean is_favorite_before = Account.isFavorite(dish);
				final int index = Account.favorites.indexOf(dish.dishid);
				
				String url = "http://182.92.231.24:8889/favorite";
				
				int retry_times = 0;
				while (retry_times++ < 2) {
					Log.v("HttpUtil", "retry_times = " + retry_times);
			    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							arg3.printStackTrace();
							Log.v("HttpUtil", "favorite onFailure");
						}
						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							String res = new String(arg2);
							Log.v("HttpUtil", "favorite done. res=" + res + ", dishid=" + dish.dishid);
							if (res.equals("ok")) {
								if (is_favorite_before) {
									if (Account.favorites.indexOf(dish.dishid) >= 0)
										Account.favorites.remove(index);
									if (Account.local_favorites.indexOf(dish.dishid) >= 0) 
										Account.do_local_favorite(dish);
								}
								else {
									if (Account.favorites.indexOf(dish.dishid) < 0)
										Account.favorites.add(dish.dishid);
									if (Account.local_favorites.indexOf(dish.dishid) < 0) 
										Account.do_local_favorite(dish);
								}
							}
						}
			    	});
			    	
			    	boolean is_favorite_after = Account.isFavorite(dish);
			    	if (is_favorite_after != is_favorite_before) break;
				} //while
				
				handler.sendEmptyMessage(Constants.MSG_ID_FAVORITE_DONE);
			}
		}.start();
    }
	
	public static void deleteDishInServer(final Dish dish, final Handler handler) {
    	new Thread() {
			@Override
			public void run() {
				RequestParams params = new RequestParams();
				params.put("dishId", "" + dish.dishid);
				params.put("userId", Account.userid);
				
				String url = "http://182.92.231.24:8889/delete";
				
				int retry_times = 0;
				final int old_dishid = dish.dishid;
				while (retry_times++ < 2) {
					Log.v("HttpUtil", "retry_times = " + retry_times);
			    	HttpUtils.getSync(url, params, new AsyncHttpResponseHandler() {
						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							arg3.printStackTrace();
							Log.v("HttpUtil", "deleteDishInServer onFailure");
						}
						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							String res = new String(arg2);
							Log.v("HttpUtil", "deleteDishInServer done. res=" + res + ", dishid=" + dish.dishid);
							if (res.equals("ok")) {
								int new_dishid = ++ Dish.current_makedish_dishid;
								Tool.getInstance().rename(dish.dishid, new_dishid);
								Dish newdish = Dish.getDishById(dish.dishid);
								Dish.removeDish(dish);
								newdish.dishid = new_dishid;
								newdish.type = Constants.DISH_MADE_BY_USER | Constants.DISH_UPLOAD_CANCELED;
								newdish.saveDishParam();
								Dish.putDish(newdish);
								
								Log.v("HttpUtil", "deleteDishInServer rename done. fromid=" + old_dishid + ",toid=" + new_dishid);
							}
						}
			    	});
			    	
			    	if (!Dish.alldish_map.containsKey(dish.dishid)) break;
				} //while
				
				handler.sendEmptyMessage(Constants.MSG_ID_DEL_In_Server);
			}
		}.start();
    }
}