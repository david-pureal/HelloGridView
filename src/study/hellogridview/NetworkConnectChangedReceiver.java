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
		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// �������wifi�Ĵ���رգ���wifi�������޹�  
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
		
        // �������wifi������״̬���Ƿ�������һ����Ч����·�ɣ����ϱ߹㲥��״̬��WifiManager.WIFI_STATE_DISABLING����WIFI_STATE_DISABLED��ʱ�򣬸�������ӵ�����㲥��  
        // ���ϱ߹㲥�ӵ��㲥��WifiManager.WIFI_STATE_ENABLED״̬��ͬʱҲ��ӵ�����㲥����Ȼ�մ�wifi�϶���û�����ӵ���Ч������  
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {  
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);  
            if (null != parcelableExtra) {  
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;  
                State wifistate = networkInfo.getState();  
                Log.v("NetworkChanged", "wifistate = " + wifistate);  
                if (wifistate == State.DISCONNECTED) {    // wifi�Ͽ�
                	//TCPClient.getInstance().is_stop = true;
                	//TCPClient.getInstance().notify_connect_state(Constants.DISCONNECTED);
                	TCPClient.getInstance().notify_connect_state(Constants.CONNECTING);
                } 
                else if (wifistate == State.CONNECTED){   // wifi����
                	// �����ǰ�Ѿ������ϻ����ˣ��Ͳ�Ҫ�����ˣ�����������ϻ����󣬵������Ͻ�����״̬��������
                	// �������Ϊ�߳̾�������ģ����յ���CONNECTING֮ǰ�������߳��Ѿ���ѯ��wifi���ӳɹ������ӻ���
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
        
        // ��������������ӵ����ã�����wifi���ƶ����ݵĴ򿪺͹رա�.  
        // ����õĻ������������wifi����򿪣��رգ��Լ������Ͽ��õ����Ӷ���ӵ���������log  
        // ����㲥�����׶��Ǳ��ϱ������㲥�ķ�ӦҪ�������ֻ��Ҫ����wifi���Ҿ��û������ϱ�������ϱȽϺ���  
//        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {  
//        	ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
//        	NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
//        	NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        	Log.i("NetworkChanged", "����״̬�ı�:" + wifi.isConnected() + " 3g:" + gprs.isConnected());
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
