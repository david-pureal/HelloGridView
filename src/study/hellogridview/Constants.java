package study.hellogridview;

public final class Constants {
	public static final String AP_NAME_PREFIX = "RDIntelligent";
	public static final String AP_IP = "192.168.1.50";
	public static final short AP_STA_PORT = 5000;
	public static final int BBXC_SOCKET_TIMEOUT = 5000;
	public static final String BUILTIN_CNAME = "���ڲ���";
	public static final String SYSTEM_CNAME = "ƽ̨����";
	public static final String [] jiaoban_str = {"1������", "2������", "3������", "4������", "5�п���", "6�Ͽ���", "7�ؿ���", "8������"};
	
	public static final int EARLIEST_ADD_ZHULIAO_TIME = 13; // ��λ�룬���������翪ʼʱ��
	
	// connect related
	public static final int CONNECTING   = 0;
	public static final int CONNECTED    = 1;
	public static final int DISCONNECTED = -1;
	
	public static final int RECONNECTING = 2;
	public static final int WAIT_WIFI_CONNECTED = 3;
	public static final int RECONNECTING_WAIT = 4;
	
	public static final int CONNECT_TIMEOUT = 10000;//  milliseconds
	public static final int CONNECTED_RESP_TIMEOUT = 5000; // ���ӻ����ɹ����������5��û�յ��������ݣ�����Ϊ�Ͽ�
	
	// machine work state
	public static final byte MACHINE_WORK_STATE_COOKING = 0x00; // 0�������ˣ�1��ͣ��2����
	public static final byte MACHINE_WORK_STATE_PAUSE   = 0x01;
	public static final byte MACHINE_WORK_STATE_STOP    = 0x02;
	public static final byte MACHINE_UNLOCK_MACHINE     = 0x03; // ����
	public static final byte MACHINE_LOCK_MACHINE       = 0x04; // ����
	
	// machine cooking stage
	public static final int STATE_HEATING = 0;
	public static final int STATE_ADD_OIL = 1;
	public static final int STATE_ZHULIAO = 2;
	public static final int STATE_ZHULIAO_TISHI_DONE = 3;
	public static final int STATE_FULIAO = 4;
	public static final int STATE_FULIAO_TISHI_DONE = 5;
	public static final int STATE_FINISH = 6;
	
	// UI related
	public static final int UI_WIDTH    = 480;
	public static final int UI_HEIGHT   = 272;
	
	// message id
	public static final int MSG_ID_STA_IP        = 100;
	public static final int MSG_ID_UPLOAD_RESULT = 200;
	public static final int MSG_ID_DOWNLOAD_ICON = 300;
	public static final int MSG_ID_VERIFY_DONE   = 400;
	public static final int MSG_ID_FAVORITE_DONE = 500;
	public static final int MSG_ID_REGISTER_DONE = 600;
	public static final int MSG_ID_CONNECT_STATE = 700;
	public static final int MSG_ID_DEL_In_Server = 800;
	
	// dish related
	public static final int DISH_APP_BUILTIN      = 0x01;   // APP���ò���
	public static final int DISH_MADE_BY_USER     = 0x02;   // �û��Ա���ף���δ�ϴ�
	public static final int DISH_UPLOAD_VERIFYING = 0x10;   // �û��Ա���ף����ϴ�������С�����
	public static final int DISH_UPLOAD_CANCELED  = 0x08;   // �û��Ա���ף����ϴ������ͨ��ǰ���ӷ����ɾ����ȡ���ϴ�
	public static final int DISH_VERIFY_ACCEPT    = 0x20;   // �û��Ա���ף����ϴ������ͨ��
	public static final int DISH_VERIFY_REJECT    = 0x40;   // �û��Ա���ף����ϴ������δͨ��
	public static final int DISH_DEVICE_BUILTIN   = 0x100;  // �������ò���
	public static final int DISH_FAVORITE         = 0x1000; // �û��ղصĲ���
	
	public static final String DISH_PARAM_FILENAME = "dish.param";
	public static final String DISH_IMG_FILENAME = "main.jpg";
	public static final String DISH_IMG_TINY_FILENAME = "main_tiny.jpg";
	public static final String LOCAL_USER_DATA = "user.data"; //δ��¼ʱ���ղصĲ���
	
	// OOM decode image sample
	public static final int DECODE_DISH_IMG_SAMPLE = 1;
	public static final int DECODE_MATERIAL_SAMPLE = 4;
	
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
