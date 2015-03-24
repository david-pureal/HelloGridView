package study.hellogridview;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
 
public class HttpUtils {
    private static AsyncHttpClient client =new AsyncHttpClient();    //实例话对象
    
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
}