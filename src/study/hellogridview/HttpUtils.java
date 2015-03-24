package study.hellogridview;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
 
public class HttpUtils {
    private static AsyncHttpClient client =new AsyncHttpClient();    //ʵ��������
    
    static
    {
        client.setTimeout(10*000);   //�������ӳ�ʱ����������ã�Ĭ��Ϊ10s
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