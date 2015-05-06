package study.hellogridview;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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
            	TCPClient.getInstance().connect_state = Constants.DISCONNECTED;
            	TCPClient.getInstance().notify_connect_state();
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
                State state = networkInfo.getState();  
                boolean isConnected = state == State.CONNECTED;// ��Ȼ����߿��Ը���ȷ��ȷ��״̬  
                Log.v("NetworkChanged", "isConnected = " + isConnected);  
                if (isConnected) {  
                	TCPClient.getInstance().is_stop = false;
                	TCPClient.getInstance().connect_state = Constants.CONNECTING;
                	TCPClient.getInstance().start_connecting_timestamp = System.currentTimeMillis();
                	TCPClient.getInstance().notify_connect_state();
                } else {  
                }  
            }  
        }
        
        // ��������������ӵ����ã�����wifi���ƶ����ݵĴ򿪺͹رա�.  
        // ����õĻ������������wifi����򿪣��رգ��Լ������Ͽ��õ����Ӷ���ӵ���������log  
        // ����㲥�����׶��Ǳ��ϱ������㲥�ķ�ӦҪ�������ֻ��Ҫ����wifi���Ҿ��û������ϱ�������ϱȽϺ���  
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {  
        	ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        	NetworkInfo gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
        	NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        	Log.i("NetworkChanged", "����״̬�ı�:" + wifi.isConnected() + " 3g:" + gprs.isConnected());
        	
        	NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);  
            if (info != null) {  
                Log.e("NetworkChanged", "info.getTypeName()" + info.getTypeName());  
                Log.e("NetworkChanged", "getSubtypeName()" + info.getSubtypeName());  
                Log.e("NetworkChanged", "getState()" + info.getState());  
                Log.e("NetworkChanged", "getDetailedState()" + info.getDetailedState().name());  
                Log.e("NetworkChanged", "getDetailedState()" + info.getExtraInfo());  
                Log.e("NetworkChanged", "getType()" + info.getType());  
  
                if (NetworkInfo.State.CONNECTED == info.getState()) {  
                } else if (info.getType() == 1) {  
                    if (NetworkInfo.State.DISCONNECTING == info.getState()) {  
  
                    }  
                }  
            }  
        }  

	}

}
