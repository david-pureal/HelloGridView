package study.hellogridview;

public final class Constants {
	public static final String AP_NAME = "RDIntelligent";
	public static final String AP_IP = "192.168.1.50";
	public static final short AP_STA_PORT = 5000;
	public static final int BBXC_SOCKET_TIMEOUT = 5000;
	
	// connect related
	public static final int CONNECTING   = 0;
	public static final int CONNECTED    = 1;
	public static final int DISCONNECTED = -1;
	public static final int CONNECT_TIMEOUT = 30000;//  milliseconds
	
	// message id
	public static final int MSG_ID_STA_IP = 100;
	public static final int MSG_ID_UPLOAD_RESULT = 200;
	public static final int MSG_ID_DOWNLOAD_ICON = 300;
	public static final int MSG_ID_VERIFY_DONE = 400;
	public static final int MSG_ID_FAVORITE_DONE = 500;
	public static final int MSG_ID_REGISTER_DONE = 600;
	public static final int MSG_ID_CONNECT_STATE = 700;
	
	// dish related
	public static final int DISH_APP_BUILTIN      = 0x01;   // APP内置菜谱
	public static final int DISH_MADE_BY_USER     = 0x02;   // 用户自编菜谱，还未上传
	public static final int DISH_UPLOAD_VERIFYING = 0x10;   // 用户自编菜谱，已上传，审核中。。。
	public static final int DISH_VERIFY_ACCEPT    = 0x20;   // 用户自编菜谱，已上传，审核通过
	public static final int DISH_VERIFY_REJECT    = 0x40;   // 用户自编菜谱，已上传，审核未通过
	public static final int DISH_DEVICE_BUILTIN   = 0x100;  // 机器内置菜谱
	public static final int DISH_FAVORITE         = 0x1000; // 用户收藏的菜谱
	
	public static final String DISH_PARAM_FILENAME = "dish.param";
	public static final String DISH_IMG_FILENAME = "main.jpg";
	public static final String DISH_IMG_TINY_FILENAME = "main_tiny.jpg";
	
	// for SmartLink
	public static final String TAG = "HF-A11 | ";
	public static final int UDP_PORT = 48899;
	public static final int REQUEST_CODE_CHOOSE_FILE = 1;
	public static final int RESULT_CODE_CHOOSE_FILE = 1;
	public static final String KEY_CMD_SCAN_MODULES = "cmd_scan_modules";
	public static final String KEY_IP = "ip";
	public static final String KEY_UDP_PORT = "udp_port";
	public static final String KEY_PRE_ID = "id_";
	public static final String KEY_PRE_IP = "ip_";
	public static final String KEY_PRE_MAC = "mac_";
	public static final String KEY_PRE_MODULEID = "moduleid_";
	public static final String KEY_MODULE_COUNT = "module_count";
	public static final String ENTER = "<0x0d>";
	public static final String CMD_SCAN_MODULES = "HF-A11ASSISTHREAD";
	public static final String CMD_ENTER_CMD_MODE = "+ok";
	public static final String CMD_EXIT_CMD_MODE = "AT+Q\r";
	public static final String CMD_RELOAD = "AT+RELD\r";
	public static final String CMD_RESET = "AT+Z\r";
	public static final String CMD_TRANSPARENT_TRANSMISSION = "AT+ENTM\r";
	public static final String CMD_NETWORK_PROTOCOL = "AT+NETP\r";
	public static final String CMD_TEST = "AT+\r";
	public static final String RESPONSE_OK = "+ok";
	public static final String RESPONSE_OK_OPTION = "+ok=";
	public static final String RESPONSE_REBOOT_OK = "+ok=rebooting";
	public static final String FILE_TO_SEND = "FilePath";
	public static final int BATTERY_MIN = 30;
	public static final int WIFI_MIN = 150;
	public static final int TIMER_CHECK_CMD = 50000;
}
