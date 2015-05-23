package study.hellogridview;

// 全局唯一
public class DeviceState {
	private static DeviceState device;
	
	public static DeviceState getInstance() {
		if (device == null) {
			device = new DeviceState();
		}
		return device;
	}
	
	private DeviceState() {
		builtin_dishids = new short[12];
		for (int i = 0;i < builtin_dishids.length;++i) builtin_dishids[i] = (short) (i + 1);
	}
	public byte working_state = Constants.MACHINE_WORK_STATE_STOP; //机器工作状态： 0，正在做菜；1暂停；2待机
	public short time = 580;
	public byte temp = (byte) 180; // 温度
	public int temp_set = 180; // 设置的温度
	public byte jiaoban_speed = 5;
	public short dishid = 1;
	public byte is_pot_in = 1; //锅是否在位
	public byte is_unlock = 0; //是否解锁，1是0否
	public short zhuliao_time_set = 0;
	public short fuliao_time_set = 0;
	
	public int device_id = 0;  // 机器识别码
	public boolean got_builtin = false;
	
	public byte use_sound = 0;
	public byte use_english = 0;
	
	public String wifi_mode = "AP";
	
	public short[] builtin_dishids;
	
	public boolean is_locked() {
		return is_unlock == 0;
	}
}