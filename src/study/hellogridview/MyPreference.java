package study.hellogridview;

import android.content.Context;

public class MyPreference {
    //偏好文件名
    public static final String SHAREDPREFERENCES_NAME = "my_pref";
    //引导界面KEY
    private static final String KEY_GUIDE_ACTIVITY = "guide_activity";
    // STA模式的ip地址
    private static final String KEY_STA_IP = "sta_ip";
    private static final String KEY_INFO_NAME = "info_name";
    private static final String KEY_INFO_NICKNAME = "info_nickname";
    private static final String KEY_INFO_ADDRESS = "info_address";
    private static final String KEY_INFO_PHONE = "info_phone";
    private static final String KEY_IS_FIRST_LAUNCH = "bbxc_is_first_launch";
    
    /**
     * 判断activity是否引导过
     * 
     * @param context
     * @return  是否已经引导过 true引导过了 false未引导
     */
    public static boolean activityIsGuided(Context context,String className){
        if(context==null || className==null||"".equalsIgnoreCase(className))return false;
        String[] classNames = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                 .getString(KEY_GUIDE_ACTIVITY, "").split("\\|");//取得所有类名 如 com.my.MainActivity
         for (String string : classNames) {
            if(className.equalsIgnoreCase(string)){
                return true;
            }
        }
          return false;
    }
    
    /**设置该activity被引导过了。 将类名已  |a|b|c这种形式保存为value，因为偏好中只能保存键值对
     * @param context
     * @param className
     */
    public static void setIsGuided(Context context,String className){
        if(context==null || className==null||"".equalsIgnoreCase(className))return;
        String classNames = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                 .getString(KEY_GUIDE_ACTIVITY, "");
        StringBuilder sb = new StringBuilder(classNames).append("|").append(className);//添加值
        context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)//保存修改后的值
        .edit()
        .putString(KEY_GUIDE_ACTIVITY, sb.toString())
        .commit();
    }
    
    public static String get_sta_ip(Context context) {
    	if(context != null) {
    		String ip = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                    .getString(KEY_STA_IP, "");
    		return ip;
    	}
    	return "";
    }
    
    public static void set_sta_ip(Context context, String sta_ip) {
    	if(context != null) {
    		context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)//保存修改后的值
            .edit()
            .putString(KEY_STA_IP, sta_ip)
            .commit();
    	}
    }
    
    public static void get_info(Context context) {
    	if(context != null) {
    		Account.info_name = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                    .getString(KEY_INFO_NAME, "");
    		Account.info_nickname = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                    .getString(KEY_INFO_NICKNAME, "");
    		Account.info_address = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                    .getString(KEY_INFO_ADDRESS, "");
    		Account.info_phone = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                    .getString(KEY_INFO_PHONE, "");
    	}
    }
    
    public static void set_info(Context context) {
    	if(context != null) {
    		context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)//保存修改后的值
            .edit()
            .putString(KEY_INFO_NAME, Account.info_name)
            .putString(KEY_INFO_NICKNAME, Account.info_nickname)
            .putString(KEY_INFO_ADDRESS, Account.info_address)
            .putString(KEY_INFO_PHONE, Account.info_phone)
            .commit();
    	}
    }
    
    public static boolean is_first_launch(Context context) {
    	if(context != null) {
    		String res = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                    .getString(KEY_IS_FIRST_LAUNCH, "");
    		return !res.equalsIgnoreCase("false");
    	}
    	return true;
    }
    
    public static void set_first_launch(Context context) {
    	if(context != null) {
    		context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)//保存修改后的值
            .edit()
            .putString(KEY_IS_FIRST_LAUNCH, "false")
            .commit();
    	}
    }
    
}