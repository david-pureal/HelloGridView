package study.hellogridview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import study.hellogridview.R;

public class Dish implements Cloneable {
	public static ArrayList<Integer> alldish_web = new ArrayList<Integer>();
	// �û��Ա�Ĳ������ϴ�ǰ����ʱid���ϴ����ɷ�������·���id
	public static final int USER_MAKE_DISH_START_ID = 60000; 
	public static int current_makedish_dishid = USER_MAKE_DISH_START_ID;
	public String name_chinese = "";
	public String name_english = "name_english";
	public int dishid = 100;
	public short zhuliao_time = 150;
	public short fuliao_time = 80;
	public byte zhuliao_temp = (byte) 180;
	public byte fuliao_temp = (byte) 160;
	public byte zhuliao_jiaoban_speed = 8;
	public byte fuliao_jiaoban_speed = 7;
	public byte water = 0;//0�������ˮ�� 1�����������ʱˮ��2������븨��ʱˮ
	public int water_weight = 0; // ��ˮ�� �� ��λ����
	public byte oil = 30; //������
	public byte qiangguoliao = 1;//������ 0��ʾ�ޣ� 1��ʾ��
	
	public Integer img = R.drawable.tudousi;   // APP�Դ��Ĳ���
	public Bitmap img_bmp = null; // �Ա���ף�������APP��չʾ
	public String img_path; //�Ա���ף����ڴ洢
	
	public Integer img_tiny = R.drawable.tudousi;   //��������ʾ��СͼƬ����СΪ106*76
	public String img_tiny_path;
	public int sound = 0;
	
	//public boolean isBuiltIn = true;
	public int type = Constants.DISH_APP_BUILTIN | Constants.DISH_DEVICE_BUILTIN | Constants.DISH_FAVORITE;
	
	public String text = "1�����ͣ�20��\n2�������ϣ���˿5�ˡ���Ƭ5��\n3�����ϣ�����˿230��,�ཷ˿20�ˣ��콷˿20��\n4�����ϣ�����2�ˡ���2��";
	public String qiangguoliao_content = "��˿5�ˡ���Ƭ5��";
	protected String zhuliao_content = "����˿: 230��\n�ཷ˿: 20��";
	public String fuliao_content;
	public ArrayList<Integer> materials = new ArrayList<Integer>();
	
	public String author_id;
	public String author_name;
	public String device_id;
	public String intro = "";
	public boolean is_cancel_upload = false;
	
	// �����������
	public LinkedHashMap<String, String> qiangguoliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> tiaoliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> zhuliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> fuliao_content_map = new LinkedHashMap<String, String>();
	
	class Material {
		String description;
		BitmapDrawable img_drawable;
		String path;
		int img_resid; // ���ò���ʹ��
		public Material(int resid, String desc) {
			img_resid = resid;
			description = desc;
		}
		public Material(){}
	}
	//����ͼ����⣺����������ôԤ���������г�ʲô��״
	public ArrayList<Material> prepare_material_detail = new ArrayList<Material>(); 
	
	public static LinkedHashMap<Integer, Dish> alldish_map;
	public static LinkedHashMap<Integer, Dish> canceled_map;
	
	public Dish(Integer img, String name) {
		this.img = img;
		this.name_chinese = name;
		
		fuliao_content = "";
		author_id = "";
		author_name = "";
		device_id = "";
	}
	
	//�û��Ա����
	public static int addDish(Dish d) {
		if (alldish_map == null) getAllDish();
		//d.img_tiny = R.raw.zibian_tiny;
		d.img_tiny = R.raw.tudousi_tiny;
		//d.img_tiny = R.raw.temp_tiny;
		d.name_english = HanziToPinyin.getPinYin(d.name_chinese);
		if (d.name_english.length() > 13) d.name_english = d.name_english.substring(0, 13);//Ӣ����������󳤶�
		
		DeviceState ds = DeviceState.getInstance();
		short max = 0;
		for (int i = 0; i < ds.builtin_dishids.length ; ++i) {
			if (ds.builtin_dishids[i] > max) max = ds.builtin_dishids[i];
		}
		if (max > current_makedish_dishid) current_makedish_dishid = max;
		d.dishid = ++current_makedish_dishid;
		
		// create directory
		Tool.getInstance().make_directory(d.getDishDirName());
		
		// ����Ĭ��ͼƬ
		// �˴�Ϊ���·��
		d.img_path = Constants.DISH_IMG_FILENAME;
		Bitmap btm = d.img_bmp;
		String path = d.getDishDirName() + d.img_path;
		Tool.getInstance().savaBitmap(btm, path);
		d.img_tiny_path = Tool.getInstance().makeTinyImage(d);// �˴�Ϊ���·��
		
		d.type = Constants.DISH_MADE_BY_USER;
		
		d.device_id = Account.device_id;
		
		alldish_map.put(d.dishid, d);
		return d.dishid;
	}
	
	public String getDishDirName() {
		return Tool.getInstance().getModulePath() + "/dish" + dishid + "/";
	}
	public String getDishDirRelativeName() {
		return "/dish" + dishid + "/";
	}
	public static String getDishNameById(int id) {
		Log.v("Dish", "getDishNameById dishid = " + id);
		if (alldish_map.get(id) == null) return "";
		return alldish_map.get(id).name_chinese + id;
	}
	public static Dish getDishById(int id) {
		if (!alldish_map.containsKey(id)) Log.v("Dish", "dishid = " + id + " not exist");
		return alldish_map.get(id);
	}
	public static Dish getDishByIndex(int index) {
		int key = (Integer) alldish_map.keySet().toArray()[index];
		return alldish_map.get(key);
	}
	public static void putDish(Dish d) {
		alldish_map.put(d.dishid, d);
	}
	public static void removeDish(Dish d) {
		alldish_map.remove(d.dishid);
	}
	
	public void saveDishParam() {
		
		if (this.isMine()) {
			this.author_id = Account.userid;
			this.author_name = Account.username;
		}
		
		if (fuliao_content_map.isEmpty()) {fuliao_time = 0;fuliao_temp=0;fuliao_jiaoban_speed=0;}
		
		// ����д���ļ�
		JSONObject dishj = new JSONObject();
		try {
			dishj.put("dishid", dishid);
			dishj.put("name_chinese", name_chinese);
			dishj.put("name_english", name_english);
			
			dishj.put("zhuliao_time", zhuliao_time & 0xffff);
			dishj.put("fuliao_time", fuliao_time & 0xffff);
			dishj.put("zhuliao_temp", zhuliao_temp & 0xff);
			dishj.put("fuliao_temp", fuliao_temp & 0xff);
			dishj.put("zhuliao_jiaoban_speed", zhuliao_jiaoban_speed & 0xff);
			dishj.put("fuliao_jiaoban_speed", fuliao_jiaoban_speed & 0xff);
			
			dishj.put("water", water & 0xff);
			dishj.put("water_weight", water_weight);
			dishj.put("oil", oil & 0xff);
			dishj.put("qiangguoliao", qiangguoliao & 0xff);
			dishj.put("sound", sound);
			
			dishj.put("type", type);
			
			dishj.put("img_path", img_path);
			dishj.put("img_tiny_path", img_tiny_path);
			
			dishj.put("author_id", author_id);
			dishj.put("author_name", author_name);
			
			dishj.put("device_id", device_id);
			
			dishj.put("intro", intro);
			

			// ���ϣ����Ϻͱ���ͼ��д���ļ�
			JSONArray zhuliao_array = new JSONArray();
			for (Iterator<String> it = zhuliao_content_map.keySet().iterator();it.hasNext();)
			{
			    String key = it.next();
			    JSONObject element = new JSONObject();
			    element.put(key, zhuliao_content_map.get(key));
			    zhuliao_array.put(element);
			}
			dishj.put("zhuliao_content", zhuliao_array);
			
			JSONArray fuliao_array = new JSONArray();
			for (Iterator<String> it = fuliao_content_map.keySet().iterator();it.hasNext();)
			{
			    String key = it.next();
			    JSONObject element = new JSONObject();
			    element.put(key, fuliao_content_map.get(key));
			    fuliao_array.put(element);
			}
			dishj.put("fuliao_content", fuliao_array);
			
			JSONArray qiangguoliao_array = new JSONArray();
			for (Iterator<String> it = qiangguoliao_content_map.keySet().iterator();it.hasNext();)
			{
			    String key = it.next();
			    JSONObject element = new JSONObject();
			    element.put(key, qiangguoliao_content_map.get(key));
			    qiangguoliao_array.put(element);
			}
			dishj.put("qiangguoliao_content", qiangguoliao_array);
			
			JSONArray tiaoliao_array = new JSONArray();
			for (Iterator<String> it = tiaoliao_content_map.keySet().iterator();it.hasNext();)
			{
			    String key = it.next();
			    JSONObject element = new JSONObject();
			    element.put(key, tiaoliao_content_map.get(key));
			    tiaoliao_array.put(element);
			}
			dishj.put("tiaoliao_content", tiaoliao_array);
			
			JSONArray material_array = new JSONArray();
			for (int i = 0; i < prepare_material_detail.size(); ++i)
			{
			    JSONObject element = new JSONObject();
			    Material m = prepare_material_detail.get(i);
			    element.put("description", m.description);
			    element.put("path", m.path);
			    material_array.put(element);
			}
			dishj.put("material_detail", material_array);
			
			String param_path = getDishDirName() + "/" + Constants.DISH_PARAM_FILENAME;
			Tool.getInstance().writeFile(dishj.toString().getBytes(), param_path);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static LinkedHashMap<Integer, Dish> getAllDish() {
		if (alldish_map == null) {
			alldish_map = new LinkedHashMap<Integer, Dish>();
			canceled_map = new LinkedHashMap<Integer, Dish>();
			
			{
				Dish dish = new Dish(R.drawable.tudousi, "������˿");
				dish.img_tiny = R.raw.tudousi_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = 0;
				dish.zhuliao_time = 230;
				dish.fuliao_time = 0;
				dish.zhuliao_jiaoban_speed = 5;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 0;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.text = "1�����ͣ�30��\n2�������ϣ���˿5�ˡ���Ƭ5��\n3�����ϣ�����˿230��,�ཷ˿20��,�콷˿20��\n4�����ϣ�����2�ˡ���2��"; 
				dish.name_english = "tomato chip";
				//dish0.sound = R.raw.tudousi_voice;
				dish.dishid = 1;
				dish.intro = "��ˬ�ɿڣ��̵ģ���ģ����Ķ��ܺóԡ�����Ӫ����ȫ��������������ŷ�����У��ڶ�������ĳƺš�";
				
				dish.zhuliao_content_map.put("����˿", "230��");
				dish.zhuliao_content_map.put("�ཷ˿", "20��");
				dish.zhuliao_content_map.put("�콷˿", "20��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.tudousi_1, "����˿���ཷ���콷�г�˿"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			Dish dish1 = new Dish(R.drawable.chaoqingcai, "�����");
			dish1.img_tiny = R.raw.chaoqingcai_tiny;
			dish1.zhuliao_temp = (byte) 180;
			dish1.fuliao_temp = 0;
			dish1.zhuliao_time = 180;
			dish1.fuliao_time = 0;
			dish1.zhuliao_jiaoban_speed = 5;
			dish1.fuliao_jiaoban_speed = 5;
			dish1.water = 0;
			dish1.oil = 30;
			dish1.qiangguoliao = 1;
			dish1.text = "1�����ͣ�20��\n2�������ϣ���˿5�ˡ���Ƭ5��\n3�����ϣ������350��\n4�����ϣ���2��";
			dish1.name_english = "Fried peanuts";
			//dish1.sound = R.raw.tudousi_voice;
			dish1.dishid = 2;
			dish1.intro = "Ʒ�����ۣ���ζ�ɿڣ�����ά���ء�Ҷ���ء�΢��Ԫ���Լ��ܴٽ������䶯����ά�ء�";
			
			dish1.zhuliao_content_map.put("�����", "350��");
			dish1.prepare_material_detail.add(dish1.new Material(R.drawable.qiangguoliao, "������"));
			dish1.prepare_material_detail.add(dish1.new Material(R.drawable.chaocaixin_1, "���"));
			dish1.qiangguoliao_content_map.put("��˿", "5��");
			dish1.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish1.tiaoliao_content_map.put("��", "2��");
			alldish_map.put(dish1.dishid, dish1);
			
			Dish dish2 = new Dish(R.drawable.fanqiechaodan, "���ѳ���");
			dish2.img_tiny = R.raw.fanqiechaodan_tiny;//87%
			dish2.zhuliao_temp = (byte) 170;
			dish2.fuliao_temp = (byte) 170;
			dish2.zhuliao_time = 60;
			dish2.fuliao_time = 180;
			dish2.zhuliao_jiaoban_speed = 8;
			dish2.fuliao_jiaoban_speed = 5;
			dish2.water = 0;
			dish2.oil = 30;
			dish2.qiangguoliao = 0;
			dish2.text = "1�����ͣ�45��\n2�����ϣ�3��������0.5���δ���\n3�����ϣ����ѿ�230��\n4�����ϣ���2��"; 
			dish2.name_english = "Tomato Eggs";
			//dish2.sound = R.raw.tudousi_voice;
			dish2.dishid = 3;
			dish2.intro = "��ζ���ˣ�Ӫ���ḻ��ϸ���д���һ��㵯�ԣ������뼦����������Э������������ˬ�������н�����˥�ϵ����á�";

			
			dish2.zhuliao_content_map.put("����", "3��");
			dish2.fuliao_content_map.put("���ѿ�", "230��");
			dish2.prepare_material_detail.add(dish2.new Material(R.drawable.fanqiejidan_1, "3���������δ���"));
			dish2.prepare_material_detail.add(dish2.new Material(R.drawable.fanqiejidan_2, "�����п�"));
			dish2.tiaoliao_content_map.put("��", "2��");
			alldish_map.put(dish2.dishid, dish2);
			
			Dish dish3 = new Dish(R.drawable.maladoufu, "��������");
			dish3.img_tiny = R.raw.maladoufu_tiny;
			dish3.zhuliao_temp = (byte) 165;
			dish3.fuliao_temp = 0;
			dish3.zhuliao_time = 240;
			dish3.fuliao_time = 0;
			dish3.zhuliao_jiaoban_speed = 3;
			dish3.fuliao_jiaoban_speed = 0;
			dish3.water = 1;
			dish3.water_weight = 10;
			dish3.oil = 30;
			dish3.qiangguoliao = 1;
			dish3.text = "1�����ͣ�25��\n2�������ϣ���˿5�ˡ���Ƭ5�ˣ��ɺ콷��3�ˣ��齷��2��\n3�����ϣ��۶�����350��\n4��ˮ�͵��ϣ�ˮ30�ˡ���2�ˡ�����2�ˡ��ϳ�2�ˡ�����10��"; 
			dish3.name_english = "Mapo Tofu";
			//dish3.sound = R.raw.tudousi_voice;
			dish3.dishid = 4;
			dish3.intro = "�顢�����㡢�ۡ��ʡ��������ö���ʵ�ݣ��óԲ��󡣶�����Ӫ������֬�������������ɳ�Ϊ�����ʳƷ֮һ��";

			dish3.zhuliao_content_map.put("�۶�����", "350��");
			dish3.prepare_material_detail.add(dish3.new Material(R.drawable.maladoufu_2, "��˿����Ƭ���ɺ콷�Σ��齷��"));
			dish3.prepare_material_detail.add(dish3.new Material(R.drawable.maladoufu_1, "�۶�����"));
			dish3.qiangguoliao_content_map.put("��˿", "5��");
			dish3.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish3.qiangguoliao_content_map.put("�ɺ콷��", "3��");
			dish3.qiangguoliao_content_map.put("�齷��", "2��");
			dish3.tiaoliao_content_map.put("����", "2��");
			dish3.tiaoliao_content_map.put("��", "2��");
			dish3.tiaoliao_content_map.put("�ϳ�", "2��");
			dish3.tiaoliao_content_map.put("����", "10��");
			alldish_map.put(dish3.dishid, dish3);
			
			Dish dish4 = new Dish(R.drawable.congbaoyangrou, "�б�����");
			dish4.img_tiny = R.raw.congbaoyangrou_tiny;
			dish4.zhuliao_temp = (byte) 180;
			dish4.fuliao_temp = (byte) 180;
			dish4.zhuliao_time = 60;
			dish4.fuliao_time = 200;
			dish4.zhuliao_jiaoban_speed = 8;
			dish4.fuliao_jiaoban_speed = 5;
			dish4.water = 2;
			dish4.water_weight = 10;
			dish4.oil = 30;
			dish4.qiangguoliao = 1;
			dish4.text = "1�����ͣ�35��\n2�������ϣ���Ƭ5�ˡ���Ƭ5��\n3�����ϣ�������Ƭ130��\n4�����ϣ���ж�150�ˡ����ܲ�˿30�ˡ�ľ��Ƭ20��\n5��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2�ˡ�����10��."; 
			dish4.name_english = "Scallion Mutton";
			dish4.dishid = 5;
			dish4.intro = "����Ũ�����������ۣ�Ӫ����ֵ�ߣ���׳����������������Ĺ�Ч��";
			
			dish4.zhuliao_content_map.put("������Ƭ", "130��");
			dish4.fuliao_content_map.put("��ж�", "150��");
			dish4.fuliao_content_map.put("���ܲ�˿", "30��");
			dish4.fuliao_content_map.put("ľ��Ƭ", "20��");
			dish4.prepare_material_detail.add(dish4.new Material(R.drawable.qiangguoliao, "��Ƭ����Ƭ"));
			dish4.prepare_material_detail.add(dish4.new Material(R.drawable.congbaoyangrou_1, "������Ƭ"));
			dish4.prepare_material_detail.add(dish4.new Material(R.drawable.congbaoyangrou_2, "��жΡ����ܲ�˿��ľ��Ƭ"));
			dish4.qiangguoliao_content_map.put("��˿", "5��");
			dish4.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish4.tiaoliao_content_map.put("����", "2��");
			dish4.tiaoliao_content_map.put("��", "2��");
			dish4.tiaoliao_content_map.put("����", "10��");
			alldish_map.put(dish4.dishid, dish4);
			
			Dish dish5 = new Dish(R.drawable.hongshaoyukuai, "�������");
			dish5.img_tiny = R.raw.hongshaoyukuai_tiny;
			dish5.zhuliao_temp = (byte) 160;
			dish5.fuliao_temp = (byte) 160;
			dish5.zhuliao_time = 60;
			dish5.fuliao_time = 300;
			dish5.zhuliao_jiaoban_speed = 5;
			dish5.fuliao_jiaoban_speed = 2;
			dish5.water = 1;
			dish5.water_weight = 10;
			dish5.oil = 30;
			dish5.qiangguoliao = 1;
			dish5.text = "1�����ͣ�50��\n2�������ϣ���Ƭ10�ˡ���Ƭ10��\n3�����ϣ����500��\n4�����ϣ��콷50�ˡ��ཷ50��\n5��ˮ��50��\n6�����ϣ���3�ˡ�����2�ˡ�����20��"; 
			dish5.name_english = "Fish block";
			dish5.dishid = 6;
			dish5.intro = "Ӫ���ḻ������ζ��������������Ч֮���ȡ�";
			
			dish5.zhuliao_content_map.put("���", "500��");
			dish5.fuliao_content_map.put("�콷", "50��");
			dish5.fuliao_content_map.put("�ཷ", "50��");
			dish5.prepare_material_detail.add(dish5.new Material(R.drawable.qiangguoliao, "������"));
			dish5.prepare_material_detail.add(dish5.new Material(R.drawable.hongshaoyukuai_1, "����ϴ������"));
			dish5.prepare_material_detail.add(dish5.new Material(R.drawable.hongshaoyukuai_2, "�콷���ཷ��Ƭ"));
			dish5.qiangguoliao_content_map.put("��˿", "10��");
			dish5.qiangguoliao_content_map.put("��Ƭ", "10��");
			dish5.tiaoliao_content_map.put("����", "2��");
			dish5.tiaoliao_content_map.put("��", "2��");
			dish5.tiaoliao_content_map.put("����", "20��");
			alldish_map.put(dish5.dishid, dish5);
			
			Dish dish6 = new Dish(R.drawable.chaoshuiguo, "��ˮ��");
			dish6.img_tiny = R.raw.chaoshuiguo_tiny;
			dish6.zhuliao_temp = (byte) 170;
			dish6.fuliao_temp = 0;
			dish6.zhuliao_time = 180;
			dish6.fuliao_time = 0;
			dish6.zhuliao_jiaoban_speed = 5;
			dish6.fuliao_jiaoban_speed = 0;
			dish6.water = 0;
			dish6.water_weight = 0;
			dish6.oil = 30;
			dish6.qiangguoliao = 0;
			dish6.text = "1�����ͣ�10��\n2�����ϣ����ҿ�60�ˡ���ݮ��60�ˡ�\n      ƻ����60�ˡ���������60�ˡ�"; 
			dish6.name_english = "Fried fruit";
			dish6.dishid = 7;
			dish6.qiangguoliao = 0;
			dish6.intro = "����ˬ�ڣ�ȥʪ��θ��Ůʿ����֮��Ʒ���ʺ�θ��ʪ����ʿʳ�á�";
			
			dish6.zhuliao_content_map.put("ƻ����", "60��");
			dish6.zhuliao_content_map.put("��������", "60��");
			dish6.prepare_material_detail.add(dish6.new Material(R.drawable.chaoshuiguo_1, "ˮ���г�С��"));
			alldish_map.put(dish6.dishid, dish6);
			
			Dish dish7 = new Dish(R.drawable.zhahuasheng, "ը����");
			dish7.img_tiny = R.raw.zhahuasheng_tiny;
			dish7.zhuliao_temp = (byte) 180;
			dish7.fuliao_temp = 0;
			dish7.zhuliao_time = 600;
			dish7.fuliao_time = 0;
			dish7.zhuliao_jiaoban_speed = 3;
			dish7.fuliao_jiaoban_speed = 0;
			dish7.water = 0;
			dish7.water_weight = 0;
			dish7.oil = 30;
			dish7.qiangguoliao = 0;
			dish7.text = "1�����ͣ�45��\n2�����ϣ�������250��\n3�����ϣ���3��";
			dish7.name_english = "Fried peanuts";
			dish7.dishid = 8;
			dish7.materials.add(R.drawable.zhahuasheng_1);
			dish7.intro = "�����ࡢ���ɿɿڣ���ά���ؼ������ʸ�������Ӫ���ɷ֣���������֬�������ߣ���Ҫ̰�ڶ�Ծ��С�";
			
			dish7.zhuliao_content_map.put("������", "250��");
			dish7.prepare_material_detail.add(dish7.new Material(R.drawable.zhahuasheng_1, "�ɻ�����"));
			dish7.tiaoliao_content_map.put("��", "3��");
			alldish_map.put(dish7.dishid, dish7);
			
			Dish dish8 = new Dish(R.drawable.qingchaosuantai, "�峴��̨");
			dish8.img_tiny = R.raw.qingchaosuantai_tiny;
			dish8.zhuliao_temp = (byte) 180;
			dish8.fuliao_temp = 0;
			dish8.zhuliao_time = 230;
			dish8.fuliao_time = 0;
			dish8.zhuliao_jiaoban_speed = 5;
			dish8.fuliao_jiaoban_speed = 0;
			dish8.water = 1;
			dish8.water_weight = 30;
			dish8.oil = 30;
			dish8.qiangguoliao = 1;
			dish8.text = "1�����ͣ�30��\n2�������ϣ���Ƭ5�ˡ���Ƭ5�� \n3�����ϣ���̦��250��\n4��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2�ˡ�����10��"; 
			dish8.name_english = "Garlic sprout";
			dish8.dishid = 9;
			dish8.intro = "��̨�к��зḻ��ά���أã����н�Ѫ֬��Ԥ�����Ĳ��Ͷ���Ӳ�������ã����ɷ�ֹѪ˨���γɡ�ɱ������ǿ������Ԥ�����кͷ�ֹ�˿ڸ�Ⱦ�Ĺ�Ч��";
			
			dish8.zhuliao_content_map.put("��̦��", "250��");
			dish8.prepare_material_detail.add(dish8.new Material(R.drawable.qiangguoliao, "������"));
			dish8.prepare_material_detail.add(dish8.new Material(R.drawable.qingchaosuantai_1, "��̨�гɶ���"));
			dish8.qiangguoliao_content_map.put("��˿", "5��");
			dish8.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish8.tiaoliao_content_map.put("����", "2��");
			dish8.tiaoliao_content_map.put("��", "2��");
			dish8.tiaoliao_content_map.put("����", "10��");
			alldish_map.put(dish8.dishid, dish8);
			
			Dish dish9 = new Dish(R.drawable.suanmiaolarou, "��������");
			dish9.img_tiny = R.raw.suanmiaolarou_tiny;
			dish9.zhuliao_temp = (byte) 170;
			dish9.fuliao_temp = (byte) 170;
			dish9.zhuliao_time = 60;
			dish9.fuliao_time = 150;
			dish9.zhuliao_jiaoban_speed = 6;
			dish9.fuliao_jiaoban_speed = 6;
			dish9.water = 2;
			dish9.water_weight = 30;
			dish9.oil = 30;
			dish9.qiangguoliao = 1;
			dish9.text = "1�����ͣ�35��\n2�������ϣ���Ƭ5�ˡ���Ƭ5��\n3�����ϣ�����Ƭ130��\n4�����ϣ������150�ˡ��콷����Ƭ20�ˡ��ཷ����Ƭ20��\n5��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2�ˡ�����10��"; 
			dish9.name_english = "Garlic Bacon";
			dish9.dishid = 10;
			dish9.intro = "���������Ǻ��ϴ�ͳ���ˣ�������ζ����Ƣ��θ�����⺬�����ϸߣ���Ѫѹ�����ٳԡ�";
			
			dish9.zhuliao_content_map.put("����Ƭ", "130��");
			dish9.fuliao_content_map.put("�����", "150��");
			dish9.fuliao_content_map.put("�콷Ƭ", "20��");
			dish9.fuliao_content_map.put("�ཷƬ", "20��");
			dish9.prepare_material_detail.add(dish9.new Material(R.drawable.qiangguoliao, "������"));
			dish9.prepare_material_detail.add(dish9.new Material(R.drawable.suanmiaolarou_1, "�����г�СƬ"));
			dish9.prepare_material_detail.add(dish9.new Material(R.drawable.suanmiaolarou_2, "���硢�콷���ཷ�г�СƬ"));
			dish9.qiangguoliao_content_map.put("��˿", "5��");
			dish9.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish9.tiaoliao_content_map.put("����", "2��");
			dish9.tiaoliao_content_map.put("��", "2��");
			dish9.tiaoliao_content_map.put("����", "10��");
			alldish_map.put(dish9.dishid, dish9);
			
			Dish dish10 = new Dish(R.drawable.hongshaojichi, "���ռ���");
			dish10.img_tiny = R.raw.hongshaojichi_tiny;
			dish10.zhuliao_temp = (byte) 185;
			dish10.fuliao_temp = (byte) 185;
			dish10.zhuliao_time = 60;
			dish10.fuliao_time = 210;
			dish10.zhuliao_jiaoban_speed = 4;
			dish10.fuliao_jiaoban_speed = 4;
			dish10.water = 2;
			dish10.water_weight = 30;
			dish10.oil = 30;
			dish10.qiangguoliao = 1;
			dish10.text = "1�����ͣ�40��\n2�������ϣ���Ƭ5�ˡ���Ƭ10�� \n3�����ϣ������5����������3������2���ϳ顢10����������10���ӱ��ã�\n4�����ϣ��콷����Ƭ30�ˡ��ཷ����Ƭ30�ˡ��㹽Ƭ40��\n5��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2��"; 
			dish10.name_english = "Chicken wings";
			dish10.dishid = 11;
			dish10.intro = "���㻬�ۣ������������������������衢ǿ����θ�ȹ�Ч���ḻ�Ľ�ԭ���ף����ڱ���Ƥ��������ǿƤ�����Ծ��кô���";
			
			dish10.zhuliao_content_map.put("����", "5��");
			dish10.fuliao_content_map.put("�콷Ƭ", "30��");
			dish10.fuliao_content_map.put("�ཷƬ", "30��");
			dish10.fuliao_content_map.put("�㹽Ƭ", "40��");
			dish10.prepare_material_detail.add(dish10.new Material(R.drawable.qiangguoliao, "������"));
			dish10.prepare_material_detail.add(dish10.new Material(R.drawable.hongshaojichi_1, "�����5����������3�������ϳ顢��������10���ӱ���"));
			dish10.prepare_material_detail.add(dish10.new Material(R.drawable.hongshaojichi_2, "�콷���ཷ�г�����СƬ"));
			dish10.qiangguoliao_content_map.put("��˿", "5��");
			dish10.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish10.tiaoliao_content_map.put("����", "2��");
			dish10.tiaoliao_content_map.put("��", "2��");
			alldish_map.put(dish10.dishid, dish10);
			
			Dish dish11 = new Dish(R.drawable.xiqinxiaren, "����Ϻ��");
			dish11.img_tiny = R.raw.xiqinxiaren_tiny;
			dish11.zhuliao_temp = (byte) 170;
			dish11.fuliao_temp = (byte) 170;
			dish11.zhuliao_time = 60;
			dish11.fuliao_time = 180;
			dish11.zhuliao_jiaoban_speed = 5;
			dish11.fuliao_jiaoban_speed = 5;
			dish11.water = 2;
			dish11.water_weight = 30;
			dish11.oil = 30;
			dish11.qiangguoliao = 1;
			dish11.text = "1�����ͣ�30��\n2�������ϣ���Ƭ5�ˡ���Ƭ5�� \n3�����ϣ�Ϻ��150�ˣ���10���Ͼ�����5���ӣ�\n4�����ϣ����۶�100�ˡ����ܲ�����Ƭ20��\n5��ˮ�͵��ϣ�ˮ10�ˡ���2�ˡ�����2��"; 
			dish11.name_english = "Celery shrimp";
			dish11.dishid = 12;
			dish11.intro = "Ϻ��Ӫ���ḻ�����ۿɿڣ����ۿ��Խ�ѹ���ԡ��峦���㡢�ⶾ���ס�����Ϻ����Ůʿ��������֮���ȡ�";
			
			dish11.zhuliao_content_map.put("Ϻ��", "150��");
			dish11.fuliao_content_map.put("���۶�", "100��");
			dish11.fuliao_content_map.put("���ܲ�Ƭ", "20��");
			dish11.prepare_material_detail.add(dish11.new Material(R.drawable.qiangguoliao, "������"));
			dish11.prepare_material_detail.add(dish11.new Material(R.drawable.xiqinxiaren_1, "Ϻ�����Ͼ�����5����"));
			dish11.prepare_material_detail.add(dish11.new Material(R.drawable.xiqinxiaren_2, "����С�Σ����ܲ��г�����СƬ"));
			dish11.qiangguoliao_content_map.put("��˿", "5��");
			dish11.qiangguoliao_content_map.put("��Ƭ", "5��");
			dish11.tiaoliao_content_map.put("����", "2��");
			dish11.tiaoliao_content_map.put("��", "2��");
			alldish_map.put(dish11.dishid, dish11);
			
//			Dish dish12 = new Dish(R.drawable.chaoqingcai, "�����ò���");
//			dish12.img_tiny = R.raw.tudousi_tiny;
//			dish12.text = "1�����ͣ�30��\n2�������ϣ���Ƭ5�ˡ���Ƭ5�� \n3�����ϣ�Ϻ��150�ˣ���10���Ͼ�����5���ӣ�\n4�����ϣ����۶�100�ˡ����ܲ�����Ƭ20��\n5��ˮ�͵��ϣ�ˮ10�ˡ���2�ˡ�����2��"; 
//			dish12.name_english = "tomato chip";
//			dish12.dishid = 50;
//			alldish_map.put(dish12.dishid, dish12);
			
			{
				Dish dish = new Dish(R.drawable.xiangganroupian, "�����Ƭ");
				dish.img_tiny = R.drawable.xiangganroupian_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 160;
				dish.zhuliao_jiaoban_speed = 8;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 2;
				dish.water_weight = 20;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "xiangganrou";
				dish.dishid = 13;
				dish.intro = "һ����θ�·��ļҳ��òˡ�����и��������ʡ�ά���ء��ơ�����þ��п���Ƶ�Ӫ��Ԫ�أ�Ӫ����ֵ�ߡ�";
				
				dish.zhuliao_content_map.put("����Ƭ", "100��");
				dish.fuliao_content_map.put("�����",  "150��");
				dish.fuliao_content_map.put("���۶�",  "30��");
				dish.fuliao_content_map.put("�콷",   "20��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "����1�����ۺ�5����������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_2, "����������۶Σ��콷����Ƭ"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				dish.tiaoliao_content_map.put("����", "5��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.jiandouchaorou, "��������");
				dish.img_tiny = R.drawable.jiandouchaorou_tiny;
				dish.zhuliao_temp = (byte) 190;
				dish.fuliao_temp = (byte) 190;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 170;
				dish.zhuliao_jiaoban_speed = 8;
				dish.fuliao_jiaoban_speed = 4;
				dish.water = 2;
				dish.water_weight = 20;
				dish.oil = 35;
				dish.qiangguoliao = 1;
				dish.name_english = "jiandourou";
				dish.dishid = 14;
				dish.intro = "������ˬ��ɿڣ���Ƭ���۶�֭��������������һ������ζ��ʳ�Ĵ���Ҳ����ʵΪһ������ļҳ��Ȳˡ�";
				
				dish.zhuliao_content_map.put("����Ƭ", "100��");
				dish.fuliao_content_map.put("������",  "170��");
				dish.fuliao_content_map.put("������",  "30��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "����1�����ۺ�5����������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.jiandouchaorou_2, "��������������"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yangcongniurou, "���ţ��");
				dish.img_tiny = R.drawable.yangcongniurou_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 8;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 2;
				dish.water_weight = 15;
				dish.oil = 35;
				dish.qiangguoliao = 1;
				dish.name_english = "onion beaf";
				dish.dishid = 15;
				dish.intro = "����ˬ�ڡ�Ӫ����������и�������΢��Ԫ�أ��ܴٽ�֬����л������Ѫ֬��������Ӳ�������ư�֢��";
				
				dish.zhuliao_content_map.put("ţ��Ƭ", "130��");
				dish.fuliao_content_map.put("���",  "150��");
				dish.fuliao_content_map.put("�ཷ",  "10��");
				dish.fuliao_content_map.put("�콷",  "10��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yangcongniurou_1, "����1�����ۺ�5������5���Ͼ�����10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yangcongniurou_2, "��У��ཷ���콷"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			
			{
				Dish dish = new Dish(R.drawable.moyushaoya, "ħ����Ѽ");
				dish.img_tiny = R.drawable.moyushaoya_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 210;
				dish.zhuliao_jiaoban_speed = 6;
				dish.fuliao_jiaoban_speed = 4;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 35;
				dish.qiangguoliao = 1;
				dish.name_english = "konjac duck";
				dish.dishid = 16;
				dish.intro = "ħ����Ѽ���Ĵ���ͳ���ˡ�ɫ�������ħ������ϸ�壬Ѽ����֣���ζ���д��ʣ��������㡣";
				
				dish.zhuliao_content_map.put("Ѽ��", "150��");
				dish.fuliao_content_map.put("ħ��",  "100��");
				dish.fuliao_content_map.put("�ཷ",  "20��");
				dish.fuliao_content_map.put("�콷",  "20��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_0, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_2, "���꽴"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_1, "Ѽ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_2, "ħ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_3, "�ཷ���콷"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("������", "8��");
				dish.qiangguoliao_content_map.put("����", "1��");
				dish.qiangguoliao_content_map.put("���꽴", "һ��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.xiqinlachang, "��������");
				dish.img_tiny = R.drawable.xiqinlachang_tiny;
				dish.zhuliao_temp = (byte) 190;
				dish.fuliao_temp = (byte) 190;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 5;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 2;
				dish.water_weight = 10;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "celery sausage";
				dish.dishid = 17;
				dish.intro = "�ǳ���ζ�ļҳ�С�����۲������ڸ���άʳ����������������ܲ������������������۲ˣ�������Ч�İ���Ƥ����˥�ϡ�";
				
				dish.zhuliao_content_map.put("����", "150��");
				dish.fuliao_content_map.put("����",  "100��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiqinlachang_1, "����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiqinlachang_2, "����"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yumihuotui, "���׻���");
				dish.img_tiny = R.drawable.yumihuotui_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 0;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 0;
				dish.zhuliao_jiaoban_speed = 4;
				dish.fuliao_jiaoban_speed = 4;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 30;
				dish.qiangguoliao = 0;
				dish.name_english = "corn ham";
				dish.dishid = 18;
				dish.intro = "��ζ�ļҳ��ˡ������к��д�����Ӫ���������ʣ�����ʳ������Ƥ�������ͶԽ������嵨�̴�ʮ�����档";
				
				dish.zhuliao_content_map.put("������", "100��");
				dish.zhuliao_content_map.put("���ȶ�", "80��");
				dish.zhuliao_content_map.put("�ඹ", "50��");
				dish.zhuliao_content_map.put("���ܲ���", "30��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yumihuotui_1, "�����������ȶ����ඹ�����ܲ���"));
				dish.tiaoliao_content_map.put("��", "1��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.chaoxingbaogu, "���ӱ���");
				dish.img_tiny = R.drawable.chaoxingbaogu_tiny;
				dish.zhuliao_temp = (byte) 190;
				dish.fuliao_temp = (byte) 0;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 0;
				dish.zhuliao_jiaoban_speed = 4;
				dish.fuliao_jiaoban_speed = 4;
				dish.water = 1;
				dish.water_weight = 15;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "Pleurotus";
				dish.dishid = 19;
				dish.intro = "�ӱ������У����⣢֮�ƣ�Ӫ���ḻ�����������ʡ�̼ˮ�����ά���ؼ��ơ�þ�ȿ����ʣ���������������߹��ܣ���������п�������Ѫ֬����θ�Լ����ݵ����á�";
				
				dish.zhuliao_content_map.put("�ӱ���", "300��");
				dish.zhuliao_content_map.put("���ܲ�", "50��");
				dish.zhuliao_content_map.put("�ཷ����Ƭ", "50��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.chaoxingbaogu_1, "�ӱ���"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				dish.tiaoliao_content_map.put("���", "10��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.xiaochaojikuai, "С������");
				dish.img_tiny = R.drawable.xiaochaojikuai_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 6;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 2;
				dish.water_weight = 20;
				dish.oil = 35;
				dish.qiangguoliao = 1;
				dish.name_english = "chicken lump";
				dish.dishid = 20;
				dish.intro = "���˺��зḻ�ĵ����ʡ������ᡢά�����Լ�΢��Ԫ�أ����п�θ����������εĹ�Ч�������ʺ������˼�����������ʿʳ�á�";
				
				dish.zhuliao_content_map.put("����", "150��");
				dish.fuliao_content_map.put("�ཷ", "50��");
				dish.fuliao_content_map.put("�콷", "50��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiaochaojikuai_1, "����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiaochaojikuai_2, "�ཷ���콷"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.liangguachaorou, "���ϳ���");
				dish.img_tiny = R.drawable.liangguachaorou_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 240;
				dish.zhuliao_jiaoban_speed = 8;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 2;
				dish.water_weight = 25;
				dish.oil = 40;
				dish.qiangguoliao = 1;
				dish.name_english = "balsam pear";
				dish.dishid = 21;
				dish.intro = "ζ���������ڸзḻ������ȥ���������ݡ�����ʳ�ÿ�Ͽ����������ȡ�����Ѫѹ��Ѫ֬��Ѫ�ǡ��ٽ��³´�л��";
				
				dish.zhuliao_content_map.put("����Ƭ", "100��");
				dish.fuliao_content_map.put("����", "220��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "����1�����ۺ�5����������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.liangguachaorou_2, "����"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.ziranyangrou, "��Ȼ����");
				dish.img_tiny = R.drawable.ziranyangrou_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 200;
				dish.zhuliao_jiaoban_speed = 8;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 2;
				dish.water_weight = 20;
				dish.oil = 40;
				dish.qiangguoliao = 1;
				dish.name_english = "lamp";
				dish.dishid = 22;
				dish.intro = "�ʵ����ۣ��������㣬Ӫ���ḻ���˲�Ʒ���ʺ����ﶬ����ʳ��";
				
				dish.zhuliao_content_map.put("����Ƭ", "150��");
				dish.fuliao_content_map.put("�ཷ",  "30��");
				dish.fuliao_content_map.put("���",  "100��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.congbaoyangrou_1, "����Ƭ"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.ziranyangrou_2, "�ཷ�����"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				dish.tiaoliao_content_map.put("��Ȼ��", "3��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.qingjiaochaorou, "�ཷ����");
				dish.img_tiny = R.drawable.qingjiaochaorou_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 190;
				dish.zhuliao_jiaoban_speed = 8;
				dish.fuliao_jiaoban_speed = 6;
				dish.water = 2;
				dish.water_weight = 15;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "squid";
				dish.dishid = 23;
				dish.intro = "���ش��䣬����ˬ�ࡣ��ҪƯ���ģͣ��Ǹ���Ҫ��Դ˲ˣ��ཷ���콷����������ά���أÿ����ð��������Ư��Ŷ��";
				
				dish.zhuliao_content_map.put("����Ƭ", "100��");
				dish.fuliao_content_map.put("�ཷƬ",  "120��");
				dish.fuliao_content_map.put("�콷",  "50��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "����1�����ۺ�5����������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qingjiaochaorou_2, "�ཷ˿���콷"));
				dish.qiangguoliao_content_map.put("��˿", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				dish.tiaoliao_content_map.put("����", "5��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.boluochaorou, "���ܳ���");
				dish.img_tiny = R.drawable.boluochaorou_tiny;
				dish.zhuliao_temp = (byte) 185;
				dish.fuliao_temp = (byte) 185;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 4;
				dish.fuliao_jiaoban_speed = 4;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				//dish.name_english = "squid";
				dish.name_english = "pineapple meet";
				dish.dishid = 24;
				dish.intro = "���ܹ�ʵƷ��������Ӫ���ḻ�������Ƚ������������ͷ���ۻ���֢�������ڹ�֭�У�������һ�ָ�θҺ�����ƵĽ��أ����Էֽ⵰�ף������������������Լ��ʣ����Ҷ����彡�����Ų�ͬ�Ĺ�Ч��";
				
				dish.zhuliao_content_map.put("��", "50��");
				dish.fuliao_content_map.put("����",  "150��");
				dish.fuliao_content_map.put("�ƹ�",  "20��");
				dish.fuliao_content_map.put("���ܲ�",  "20��");
				dish.fuliao_content_map.put("��ͷ",  "20��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.boluochaorou_1, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.boluochaorou_2, "��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.boluochaorou_3, "����"));
				dish.qiangguoliao_content_map.put("�ⶡ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.gongbaojiding, "��������");
				dish.img_tiny = R.drawable.gongbaojiding_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 175;
				dish.zhuliao_time = (short) 120;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 5;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "KungPaoChicken";
				dish.dishid = 25;
				dish.intro = "�����е��������е������ǹ�����������ɫ���ǲ͹��������ʼ��ߵ�һ���ˣ��������ڹ���Ĳ͹��У������˵���й��ˣ����ǹ��������ˣ����������˳��в�ʱ������һ����";
				
				dish.zhuliao_content_map.put("������", "150��");
				dish.zhuliao_content_map.put("���ܲ�", "50��");
				dish.fuliao_content_map.put("�ƹ�",  "150��");
				dish.fuliao_content_map.put("��ը������(��)",  "50��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_1, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_2, "���⸬�ж������Ͼơ����顢�Ρ��׺������͡���������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_3, "���ܲ��ж�"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_4, "�ƹ��ж�"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_5, "�����ը������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_6, "���úõĵ���"));
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("��", "15��");
				dish.qiangguoliao_content_map.put("��������", "5��");
				dish.qiangguoliao_content_map.put("����", "1��");
				dish.tiaoliao_content_map.put("��", "2.5��");
				dish.tiaoliao_content_map.put("����", "5��");
				dish.tiaoliao_content_map.put("��", "4��");
				dish.tiaoliao_content_map.put("�Ͼ�", "2.5��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yuxiangrousi, "������˿");
				dish.img_tiny = R.drawable.yuxiangrousi_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 175;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 5;
				dish.fuliao_jiaoban_speed = 5;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "fish-flavored pork";
				dish.dishid = 26;
				dish.intro = "������˿��һ���������ˡ����㣬���Ĵ�������Ҫ��ͳζ��֮һ����Ϊ��Ʒ��������ζ����ζ�ǵ�ζƷ���ƶ��ɡ��˷�Դ�����Ĵ���������ɫ�������ζ�����������ѹ㷺���ڴ�ζ������С�";
				
				dish.zhuliao_content_map.put("����", "200��");
				dish.fuliao_content_map.put("����˿", "150��");
				dish.fuliao_content_map.put("���ܲ�˿",  "150��");
				dish.fuliao_content_map.put("ľ��",  "50��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_1, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_2, "���꽴һ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_3, "�������Ͼơ����顢�Ρ��׺������͡���������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_4, "���񡢺��ܲ���ľ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_5, "���úõĵ���"));
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("���꽴", "15��");
				dish.tiaoliao_content_map.put("��", "2.5��");
				dish.tiaoliao_content_map.put("����", "5��");
				dish.tiaoliao_content_map.put("��", "4��");
				dish.tiaoliao_content_map.put("�Ͼ�", "2.5��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.nanguachaorou, "�Ϲϳ���");
				dish.img_tiny = R.drawable.nanguachaorou_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 180;
				dish.zhuliao_jiaoban_speed = 6;
				dish.fuliao_jiaoban_speed = 6;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "pumpkin pork";
				dish.dishid = 27;
				dish.intro = "ѡ�������е����Ϲϼӱ�����Ƭ��⿶��ɣ����ش��䣬С�Ϲ��ۡ��ۣ������۲��������ǳ��ʺ����˺�С���ԡ���˵�Ϲ��кܸߵ�ʳ�����ã��ɴٽ��ȵ��ط��ڣ�����Ѫ��ˮƽ������Ԥ�����򲡣������н������˲��˶�ԡ���";
				
				dish.zhuliao_content_map.put("����", "50��");
				dish.zhuliao_content_map.put("���ܲ�˿",  "30��");
				dish.fuliao_content_map.put("�Ϲ�", "120��");
				dish.fuliao_content_map.put("�ཷ", "30��");
				dish.fuliao_content_map.put("ľ��",  "30��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "�������Ͼơ����顢�Ρ��׺������͡���������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.nanguachaorou_3, "�Ϲϡ��ཷ��ľ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.nanguachaorou_4, "���ܲ�˿"));
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yuxiangqiezi, "��������");
				dish.img_tiny = R.drawable.yuxiangqiezi_tiny;
				dish.zhuliao_temp = (byte) 180;
				dish.fuliao_temp = (byte) 180;
				dish.zhuliao_time = (short) 60;
				dish.fuliao_time = (short) 210;
				dish.zhuliao_jiaoban_speed = 6;
				dish.fuliao_jiaoban_speed = 6;
				dish.water = 0;
				dish.water_weight = 0;
				dish.oil = 30;
				dish.qiangguoliao = 1;
				dish.name_english = "pumpkin pork";
				dish.dishid = 28;
				dish.intro = "���������Ǻ�����ɫ���ˣ����й��˴��ϵ�ģ�����Ϊ���ӣ����Զ��ָ��ϼӹ����ƶ��ɡ��ж��ֲ�ͬ�Ʒ�����ζ��������Ӫ���ḻ��";
				
				dish.zhuliao_content_map.put("����", "50��");
				dish.fuliao_content_map.put("����", "350��");
				dish.fuliao_content_map.put("���ܲ�˿",  "30��");
				dish.fuliao_content_map.put("ľ��",  "30��");
				dish.fuliao_content_map.put("�ཷ",  "30��");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "������"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_2, "���꽴һ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "�������Ͼơ����顢�Ρ��׺������͡���������10����"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangqiezi_3, "������������30���ӣ�Ȼ��ȥˮ���ཷ�����ܲ���ľ��"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangqiezi_4, "����"));
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("��Ƭ", "5��");
				dish.qiangguoliao_content_map.put("���꽴", "15��");
				dish.tiaoliao_content_map.put("��", "10��");
				dish.tiaoliao_content_map.put("����", "6��");
				dish.tiaoliao_content_map.put("��", "12��");
				dish.tiaoliao_content_map.put("�Ͼ�", "3��");
				dish.tiaoliao_content_map.put("���", "3��");
				dish.tiaoliao_content_map.put("����", "2��");
				dish.tiaoliao_content_map.put("��", "2��");
				dish.tiaoliao_content_map.put("ˮ", "18��");
				alldish_map.put(dish.dishid, dish);
			}
			
		}
		
		return alldish_map;
	}
	
	public Object clone() { 
		Dish d = null;
		try {
			d = (Dish) super.clone();
			d.dishid = this.dishid;
			d.fuliao_jiaoban_speed = this.fuliao_jiaoban_speed;
			d.fuliao_temp = this.fuliao_temp;
			d.fuliao_time = this.fuliao_time;
			d.zhuliao_jiaoban_speed = this.zhuliao_jiaoban_speed;
			d.zhuliao_temp = this.zhuliao_temp;
			d.zhuliao_time = this.zhuliao_time;
			
			d.img = this.img;
			d.img_tiny = this.img_tiny;
			d.name_chinese = this.name_chinese;
			d.name_english = this.name_english;
			
			d.oil = this.oil;
			d.qiangguoliao = this.qiangguoliao;
			d.sound = this.sound;
			d.text = this.text;
			d.water = this.water;
			
			d.type = this.type;
			d.author_id = this.author_id;
			d.author_name = this.author_name;
			d.device_id = this.device_id;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}
	
	public boolean isMine() {
		// �ֻ��Ǹ��ǳ�˽�˵��豸����������������ͬһ����app�����⣬����һ̨�豸��ʱ����Ϊ��ͬһ���ˣ����׿����໥����
		if (/*this.author_id.isEmpty() && */this.device_id.equals(Account.device_id)) {
			Log.v("Dish", "dish uploaded before login, and device_id is equal");
			return true;
		}
		else if (!this.author_id.isEmpty() && Account.is_login && Account.userid.equals(this.author_id)){
			Log.v("Dish", "dish uploaded after login, and currently login as the same user.");
			return true;
		}
		
		return false;
	}

	public boolean isAppBuiltIn() {
		return (type & Constants.DISH_APP_BUILTIN) == 0x01;
	}
	public boolean isVerifying() {
		return (type & Constants.DISH_UPLOAD_VERIFYING) != 0;
	}
	public boolean isVerifyDone() {
		return (type & Constants.DISH_VERIFY_ACCEPT) != 0;
	}
	public boolean hasNotUploaded() {
		return this.dishid > USER_MAKE_DISH_START_ID;
	}

	public static void remove_not_uploaded_dish(Dish dish) {
		// TODO Auto-generated method stub
		String path = dish.getDishDirName();
		Tool.getInstance().deleteDirectory(path);
		
		Dish.getAllDish().remove(dish.dishid);
	}
	

}
