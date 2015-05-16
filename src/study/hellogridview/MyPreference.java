package study.hellogridview;

import android.content.Context;

public class MyPreference {
    //ƫ���ļ���
    public static final String SHAREDPREFERENCES_NAME = "my_pref";
    //��������KEY
    private static final String KEY_GUIDE_ACTIVITY = "guide_activity";
    // STAģʽ��ip��ַ
    private static final String KEY_STA_IP = "sta_ip";
    
    /**
     * �ж�activity�Ƿ�������
     * 
     * @param context
     * @return  �Ƿ��Ѿ������� true�������� falseδ����
     */
    public static boolean activityIsGuided(Context context,String className){
        if(context==null || className==null||"".equalsIgnoreCase(className))return false;
        String[] classNames = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                 .getString(KEY_GUIDE_ACTIVITY, "").split("\\|");//ȡ���������� �� com.my.MainActivity
         for (String string : classNames) {
            if(className.equalsIgnoreCase(string)){
                return true;
            }
        }
          return false;
    }
    
    /**���ø�activity���������ˡ� ��������  |a|b|c������ʽ����Ϊvalue����Ϊƫ����ֻ�ܱ����ֵ��
     * @param context
     * @param className
     */
    public static void setIsGuided(Context context,String className){
        if(context==null || className==null||"".equalsIgnoreCase(className))return;
        String classNames = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)
                 .getString(KEY_GUIDE_ACTIVITY, "");
        StringBuilder sb = new StringBuilder(classNames).append("|").append(className);//���ֵ
        context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)//�����޸ĺ��ֵ
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
    		context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_WORLD_READABLE)//�����޸ĺ��ֵ
            .edit()
            .putString(KEY_STA_IP, sta_ip)
            .commit();
    	}
    }
}