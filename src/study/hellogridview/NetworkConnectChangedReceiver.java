package study.hellogridview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 这个监听wifi的打开与关闭，与wifi的连接无关  
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);  
            Log.e("NetworkChanged", "wifiState" + wifiState);  
            switch (wifiState) {  
            case WifiManager.WIFI_STATE_DISABLED:  
            	Log.v("NetworkChanged", "wifiState DISABLED " + wifiState);
            	//TCPClient.getInstance().notify_connect_state(Constants.DISCONNECTED);
            	TCPClient.getInstance().notify_connect_state(Constants.CONNECTING);
                break;  
            case WifiManager.WIFI_STATE_ENABLED:
            	Log.v("NetworkChanged", "wifiState ENABLED " + wifiState);
                break;
            case WifiManager.WIFI_STATE_DISABLING: 
            	Log.v("NetworkChanged", "wifiState DISABLING " + wifiState);
                break;  
            }
        }  
		
        // 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。  
        // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开wifi肯定还没有连接到有效的无线  
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {  
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);  
            if (null != parcelableExtra) {  
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;  
                State wifistate = networkInfo.getState();  
                Log.v("NetworkChanged", "wifistate = " + wifistate);  
                if (wifistate == State.DISCONNECTED) {    // wifi断开
                	//TCPClient.getInstance().is_stop = true;
                	//TCPClient.getInstance().notify_connect_state(Constants.DISCONNECTED);
                	TCPClient.getInstance().notify_connect_state(Constants.CONNECTING);
                } 
                else if (wifistate == State.CONNECTED){   // wifi连上
                	// 如果当前已经连接上机器了，就不要推送了，否则产生连上机器后，但是右上角连接状态是连接中
                	// 这个是因为线程竞争引起的，在收到改CONNECTING之前，其他线程已经查询到wifi连接成功并连接机器
                	if (TCPClient.getInstance().connect_state != Constants.CONNECTED) {
                		TCPClient.getInstance().notify_connect_state(Constants.CONNECTING);
                	}
                	
                	if (TCPClient.getInstance().connect_state_reason == Constants.WAIT_WIFI_CONNECTED) 	{
	                	synchronized (TCPClient.getInstance().clientThread) {
	                		// wakeup wait wifi connected thread
	                		TCPClient.getInstance().clientThread.notify();
						}
                	}
                	
                	if (TCPClient.getInstance().connect_state_reason == Constants.RECONNECTING 
                			|| TCPClient.getInstance().connect_state_reason == Constants.RECONNECTING_WAIT) 	
                	{
                		if (Tool.getInstance().getSSid(TCPClient.getInstance().main_activity).startsWith(Constants.AP_NAME_PREFIX)) {
							Log.e("tcpclient", "socket connect use ip_ap");
							TCPClient.getInstance().clientThread.current_ip = Constants.AP_IP;
                		}
                		
                		if (TCPClient.getInstance().connect_state_reason == Constants.RECONNECTING_WAIT) {
		                	synchronized (TCPClient.getInstance()) {
		                		// wakeup reconnect thread
		                		
								TCPClient.getInstance().notify();
							}
                		}
                	}
                }  
            }  
        }
        
        // 这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.  
        // 最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log  
        // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适  
//        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {  
//        	ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
//        	NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
//        	NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        	Log.i("NetworkChanged", "网络状态改变:" + wifi.isConnected() + " 3g:" + gprs.isConnected());
//        	
//        	NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);  
//            if (info != null) {  
//                Log.e("NetworkChanged", "info.getTypeName()" + info.getTypeName());  
//                Log.e("NetworkChanged", "getSubtypeName()" + info.getSubtypeName());  
//                Log.e("NetworkChanged", "getState()" + info.getState());  
//                Log.e("NetworkChanged", "getDetailedState()" + info.getDetailedState().name());  
//                Log.e("NetworkChanged", "getDetailedState()" + info.getExtraInfo());  
//                Log.e("NetworkChanged", "getType()" + info.getType());  
//  
//                if (NetworkInfo.State.CONNECTED == info.getState()) {  
//                } else if (info.getType() == 1) {  
//                    if (NetworkInfo.State.DISCONNECTING == info.getState()) {  
//  
//                    }  
//                }  
//            }  
//        }  

	}

}
