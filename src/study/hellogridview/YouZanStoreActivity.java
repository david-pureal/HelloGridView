package study.hellogridview;

import com.youzan.sdk.YouzanJsBridge;
import com.youzan.sdk.YouzanJsHandler;
import com.youzan.sdk.YouzanJsHelper;
import com.youzan.sdk.YouzanShareData;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class YouZanStoreActivity extends Activity implements YouzanJsHandler {

	// App �汾��
    private static final String APP_VERSION = "1.0";

    // ������Appע����ض���UA��ʶ��û�еĻ�ҳ�治����ʾ΢��֧����ť��
    private static final String APP_YOUZAN_UA = "kdtunion_babaxiaochao";

    // ����������SDK��������
    private YouzanJsBridge youzanJsBridge;

    // �������
    private boolean isReadyForShare = false;

    private WebView webView;
    private final YouzanWebViewClient youzanWebViewClient = new YouzanWebViewClient();
//    private Button loginButton;
//    private Button wxpayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_youzan_webview);
        webView = (WebView) findViewById(R.id.webview);

        /** ��ʼ��΢��֧�� **/
        // wxApi = WXAPIFactory.createWXAPI(this, null);
        // wxApi.registerApp(ThirdPartConst.WeiXin.AppId);

        youzanJsBridge = new YouzanJsBridge(this);
        webView.setWebViewClient(youzanWebViewClient);
        webView.addJavascriptInterface(youzanJsBridge, YouzanJsBridge.JS_INTERFACE);

        /** ���� WebSettings **/
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // ������ҳ����ʱ��('weview.goBack()') net:ERR_CACHE_MISS ����
        if (Build.VERSION.SDK_INT >= 19) {
            //settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        	settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        // ���Ķ�Ӧ�� UserAgent Ϊ������ע��� UA��Ĭ�ϵ�UA��������ע���UA��ʶ)
        String youzanUA = webView.getSettings().getUserAgentString() + " " + APP_YOUZAN_UA + " " + APP_VERSION;
        settings.setUserAgentString(youzanUA);

        webView.loadUrl("http://wap.koudaitong.com/v2/home/3wdjrqbd");

//        /** һ���¼������JS����ʵ�� onCheckUserInfo �������ɣ�����ֻ��������ʾ��¼��Ҫ�Ĳ��� **/
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //���û�е�¼����Ҫ�����Լ��Ľӿڻ�ȡ��¼��Ϣ
//                String userId = "XXXX";              // ��ǰƽ̨���û�ID
//                String userName = "XXX";             // �û���
//                String nickName = "XXX";             // �ǳ�
//                String telephone = "123123123";      // �ֻ���
//                int gender = 1;                      // �Ա�1Ϊ�У�2ΪŮ
//                String avatar = "http://........";   // ͷ�����ӵ�ַ
//                parseDataToJs(userId, nickName, userName, gender, telephone, avatar);
//            }
//        });
//
//        /** �°汾�滻ԭ��֧��Ϊ΢��wap֧�� **/
//        wxpayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               // �°汾�滻ԭ��֧��Ϊ΢��wap֧��
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        if (isReadyForShare) {
//            menuInflater.inflate(R.menu.menu_main, menu);
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_share) {
//            YouzanJsHelper.sharePage(webView);
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ҳ������û���Ϣ���������ǰ App �Ѿ���¼��ֱ�Ӱ��û���Ϣ����ҳ�漴�ɣ�
     * �����û��¼������ת����¼ҳ��
     */

    @Override
    public void onCheckUserInfo() {
        // ��ȡ��¼��Ϣ֮����� parseDataToJs() ������JS�����������¼̬
        String userId = "XXXX";              // ��ǰƽ̨���û�ID
        String userName = "XXX";             // �û���
        String nickName = "XXX";             // �ǳ�
        String telephone = "123123123";      // �ֻ���
        int gender = 1;                      // �Ա�1Ϊ�У�2ΪŮ
        String avatar = "http://........";   // ͷ�����ӵ�ַ
        parseDataToJs(userId, nickName, userName, gender, telephone, avatar);
    }

    /**
     * ע���¼̬JS��������Ҫ�����߳���ע��JS�����򱨴�
     */
    private void parseDataToJs(final String userId, final String nickName, final String userName,
                               final int gender, final String telephone, final String avatar) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                YouzanJsHelper.passUserInfoToJs(webView, userId, nickName, userName, gender,
                        telephone, avatar);
            }
        });
    }

    /**
     * ҳ���Դ����֪ͨ Native ������ҳ�� JsBridge �Ѿ�׼���ã����Է������ȹ���
     */
    @Override
    public void onWebReady() {
        // ҳ�������Ѿ������꣬������ʾ����ť
        isReadyForShare = true;
        //loginButton.setEnabled(true);
        // Demo ������ ActionBar ����ʾ����ť��������Ҳ��������һ����ͨ�� Button �ؼ��������������
        invalidateOptionsMenu();
    }

    /**
     * ҳ�����Ҫ�������ҳ��Ϣ���� Native
     * @param youzanShareData
     */
    @Override
    public void onGetShareData(YouzanShareData youzanShareData) {
        String title = youzanShareData.getTitle();
        String desc = youzanShareData.getDesc();
        String link = youzanShareData.getLink();
        String imageUrl = youzanShareData.getImgUrl();
    }

    private class YouzanWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // �����������д�������޷����� JS ����
            YouzanJsHelper.setWebReady(view);
        }

        /**
         * �ؼ����裬֧��wap΢��֧��
         * @param view
         * @param url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // ��������������� wap ΢��֧��
            if (YouzanJsHelper.handlerWapWeixinPay(YouZanStoreActivity.this, url)) {
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
	
}
