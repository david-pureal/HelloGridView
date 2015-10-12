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

	// App 版本号
    private static final String APP_VERSION = "1.0";

    // 第三方App注册的特定的UA标识（没有的话页面不会显示微信支付按钮）
    private static final String APP_YOUZAN_UA = "kdtunion_babaxiaochao";

    // 用来跟有赞SDK交互的类
    private YouzanJsBridge youzanJsBridge;

    // 分享相关
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

        /** 初始化微信支付 **/
        // wxApi = WXAPIFactory.createWXAPI(this, null);
        // wxApi.registerApp(ThirdPartConst.WeiXin.AppId);

        youzanJsBridge = new YouzanJsBridge(this);
        webView.setWebViewClient(youzanWebViewClient);
        webView.addJavascriptInterface(youzanJsBridge, YouzanJsBridge.JS_INTERFACE);

        /** 设置 WebSettings **/
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // 处理网页后退时的('weview.goBack()') net:ERR_CACHE_MISS 问题
        if (Build.VERSION.SDK_INT >= 19) {
            //settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        	settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        // 更改对应的 UserAgent 为在有赞注册的 UA（默认的UA加上有赞注册的UA标识)
        String youzanUA = webView.getSettings().getUserAgentString() + " " + APP_YOUZAN_UA + " " + APP_VERSION;
        settings.setUserAgentString(youzanUA);

        webView.loadUrl("http://wap.koudaitong.com/v2/home/3wdjrqbd");

//        /** 一般登录都是由JS发起，实现 onCheckUserInfo 方法即可，这里只是用来显示登录需要的操作 **/
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //如果没有登录，需要请求自己的接口获取登录信息
//                String userId = "XXXX";              // 当前平台的用户ID
//                String userName = "XXX";             // 用户名
//                String nickName = "XXX";             // 昵称
//                String telephone = "123123123";      // 手机号
//                int gender = 1;                      // 性别，1为男，2为女
//                String avatar = "http://........";   // 头像链接地址
//                parseDataToJs(userId, nickName, userName, gender, telephone, avatar);
//            }
//        });
//
//        /** 新版本替换原生支付为微信wap支付 **/
//        wxpayButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               // 新版本替换原生支付为微信wap支付
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
     * 页面调起用户信息请求，如果当前 App 已经登录，直接把用户信息传给页面即可，
     * 如果还没登录，则跳转到登录页面
     */

    @Override
    public void onCheckUserInfo() {
        // 获取登录信息之后调用 parseDataToJs() 方法，JS来操作保存登录态
        String userId = "XXXX";              // 当前平台的用户ID
        String userName = "XXX";             // 用户名
        String nickName = "XXX";             // 昵称
        String telephone = "123123123";      // 手机号
        int gender = 1;                      // 性别，1为男，2为女
        String avatar = "http://........";   // 头像链接地址
        parseDataToJs(userId, nickName, userName, gender, telephone, avatar);
    }

    /**
     * 注入登录态JS，这里需要起子线程来注入JS，否则报错
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
     * 页面调源生，通知 Native 环境网页的 JsBridge 已经准备好，可以发起分享等功能
     */
    @Override
    public void onWebReady() {
        // 页面内容已经加载完，可以显示分享按钮
        isReadyForShare = true;
        //loginButton.setEnabled(true);
        // Demo 中是在 ActionBar 中显示分享按钮，这里你也可以设置一个普通的 Button 控件来触发分享操作
        invalidateOptionsMenu();
    }

    /**
     * 页面把需要分享的网页信息传给 Native
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
            // 这个方法必须写，否则无法进行 JS 交互
            YouzanJsHelper.setWebReady(view);
        }

        /**
         * 关键步骤，支持wap微信支付
         * @param view
         * @param url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // 这个方法用来处理 wap 微信支付
            if (YouzanJsHelper.handlerWapWeixinPay(YouZanStoreActivity.this, url)) {
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
	
}
