package study.hellogridview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import study.hellogridview.R;
public class Dish implements Cloneable {
	// �û��Ա�Ĳ������ϴ�ǰ����ʱid���ϴ����ɷ�������·���id
	public static final int USER_MAKE_DISH_START_ID = 60000; 
	public static int current_makedish_dishid = USER_MAKE_DISH_START_ID;
	public String name_chinese = "";
	public String name_english = "name_english";
	public short dishid = 100;
	public short zhuliao_time = 150;
	public short fuliao_time = 80;
	public byte zhuliao_temp = (byte) 180;
	public byte fuliao_temp = (byte) 160;
	public byte zhuliao_jiaoban_speed = 8;
	public byte fuliao_jiaoban_speed = 7;
	public byte water = 0;//0�������ˮ�� 1�����������ʱˮ��2������븨��ʱˮ
	public int water_weight = 0; // ��ˮ�� �� ��λ����
	public byte oil = 10; //������
	public byte qiangguoliao = 1;//������ 0��ʾ�ޣ� 1��ʾ��
	
	public Integer img = R.drawable.tudousi;   // APP�Դ��Ĳ���
	public BitmapDrawable img_drawable = null; // �Ա���ף�������APP��չʾ
	public String img_path; //�Ա���ף����ڴ洢
	
	public Integer img_tiny = R.drawable.tudousi;   //��������ʾ��СͼƬ����СΪ106*76
	public String img_tiny_path;
	public Integer sound;
	
	public boolean isBuiltIn = true;
	public int type = Constants.DISH_MADE_BY_SYSTEM & Constants.DISH_DEVICE_BUILTIN & Constants.DISH_FAVORITE;
	
	
	public String text = "1�����ͣ�20��\n2�������ϣ���˿5�ˡ���Ƭ5��\n3�����ϣ�����˿230��,�ཷ˿20�ˣ�\n      �콷˿20��\n4�����ϣ�����2�ˡ���2��";
	public String qiangguoliao_content = "��˿5�ˡ���Ƭ5��";
	protected String zhuliao_content = "����˿: 230��\n�ཷ˿: 20��";
	public String fuliao_content = "";
	
	// �����������
	public LinkedHashMap<String, String> zhuliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> fuliao_content_map = new LinkedHashMap<String, String>();
	
	class Material {
		String description;
		BitmapDrawable img_drawable;
		String path;
	}
	//����ͼ����⣺����������ôԤ���������г�ʲô��״
	public ArrayList<Material> prepare_material_detail = new ArrayList<Material>(); 
	
	private static Dish[] dishes = null;
	//TODO ArrayList��ΪLinkedHashMap<short, Dish>
	public static ArrayList<Dish>  dish_list = null;
	public static TreeMap<Integer, Dish> alldish_map;
	
	public Dish(Integer img, String name) {
		this.img = img;
		this.name_chinese = name;
	}
	
	//�û��Ա����
	public static int addDish(Dish d) {
		if (dish_list == null) getAllDish();
		//d.img_tiny = R.raw.zibian_tiny;
		d.img_tiny = R.raw.tudousi_tiny;
		//d.img_tiny = R.raw.temp_tiny;
		d.name_english = HanziToPinyin.getPinYin(d.name_chinese);
		if (d.name_english.length() > 20) d.name_english.substring(0, 13);//Ӣ����������󳤶�
		DeviceState ds = DeviceState.getInstance();
		short max = 0;
		for (int i = 0; i < ds.builtin_dishids.length ; ++i) {
			if (ds.builtin_dishids[i] > max) max = ds.builtin_dishids[i];
		}
		if (max > current_makedish_dishid) current_makedish_dishid = max + 1;
		d.dishid = (short) ++current_makedish_dishid;
		
		// create directory
		Tool.getInstance().make_directory(d.getDishDirName());
		
		// Ĭ�ϵĲ���ͼƬ
		d.img_path = d.getDishDirName() + "/" + Constants.DISH_IMG_FILENAME;
		Bitmap btm = d.img_drawable.getBitmap();
		Tool.getInstance().savaBitmap(btm, d.img_path);
		
		d.type = Constants.DISH_MADE_BY_USER;
		
		dish_list.add(d);
		return dish_list.size() - 1;
	}
	
	public String getDishDirName() {
		return Tool.getInstance().getModulePath() + "/dish" + (dishid & 0xffff) + "_" + name_english;
	}
	public static String getDishNameById(short id) {
		for (int i = 0; i < dish_list.size() ;++i) {
			if (dish_list.get(i).dishid == id) {
				return dish_list.get(i).name_chinese;
			}
		}
		return ""+id;
	}
	
	public void saveDishParam() {
		// ����д���ļ�
		JSONObject dishj = new JSONObject();
		try {
			dishj.put("dishid", dishid & 0xffff);
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
			dishj.put("qiangguoliao_content", qiangguoliao_content);
			dishj.put("sound", sound);
			
			dishj.put("type", type);
			
			dishj.put("img_path", img_path);
			dishj.put("img_tiny_path", img_tiny_path);

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
			Tool.getInstance().writeFile(dishj.toString(), param_path);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Dish[] getAllDish() {
		if (alldish_map == null) {
			alldish_map = new TreeMap<Integer, Dish>();
		}
//		if (dish_list == null) {
//			dish_list = new ArrayList<Dish>();
//		} else {
//			dishes = dish_list.toArray(new Dish[dish_list.size()]);
//			return dishes;
//		}
		
		Dish dish0 = new Dish(R.drawable.tudousi, "������˿");
		dish0.img_tiny = R.raw.tudousi_tiny;
		dish0.zhuliao_temp = (byte) 180;
		dish0.fuliao_temp = (byte) 180;
		dish0.zhuliao_time = 230;
		dish0.fuliao_time = 0;
		dish0.zhuliao_jiaoban_speed = 5;
		dish0.fuliao_jiaoban_speed = 5;
		dish0.text = "1�����ͣ�30��\n2�������ϣ���˿5�ˡ���Ƭ5��\n3�����ϣ�����˿230��,�ཷ˿20�ˣ�\n      �콷˿20��\n4�����ϣ�����2�ˡ���2��"; 
		dish0.name_english = "tomato chip";
		//dish0.sound = R.raw.tudousi_voice;
		dish0.dishid = 1;
		dish_list.add(dish0);
		
		Dish dish1 = new Dish(R.drawable.chaoqingcai, "�����");
		dish1.img_tiny = R.raw.chaoqingcai_tiny;
		dish1.text = "1�����ͣ�20��\n2�������ϣ���˿5�ˡ���Ƭ5��\n3�����ϣ������350��\n4�����ϣ���2��";
		dish1.name_english = "Fried peanuts";
		//dish1.sound = R.raw.tudousi_voice;
		dish1.dishid = 2;
		dish_list.add(dish1);
		
		Dish dish2 = new Dish(R.drawable.fanqiechaodan, "���ѳ���");
		dish2.img_tiny = R.raw.fanqiechaodan_tiny;//87%
		dish2.text = "1�����ͣ�45��\n2�����ϣ�3��������0.5���δ���\n3�����ϣ����ѿ�230��\n4�����ϣ���2��"; 
		dish2.name_english = "Tomato Eggs";
		//dish2.sound = R.raw.tudousi_voice;
		dish2.dishid = 3;
		dish_list.add(dish2);
		
		Dish dish3 = new Dish(R.drawable.maladoufu, "��������");
		dish3.img_tiny = R.raw.maladoufu_tiny;
		dish3.text = "1�����ͣ�25��\n2�������ϣ���˿5�ˡ���Ƭ5�ˣ��ɺ콷��3�ˣ��齷��2��\n3�����ϣ��۶�����350��\n4��ˮ�͵��ϣ�ˮ30�ˡ���2�ˡ�����2�ˡ��ϳ�2�ˡ�����10��"; 
		dish3.name_english = "Mapo Tofu";
		//dish3.sound = R.raw.tudousi_voice;
		dish3.dishid = 4;
		dish_list.add(dish3);
		
		Dish dish4 = new Dish(R.drawable.congbaoyangrou, "�б�����");
		dish4.img_tiny = R.raw.congbaoyangrou_tiny;
		dish4.text = "1�����ͣ�35��\n2�������ϣ���Ƭ5�ˡ���Ƭ5��\n3�����ϣ�������Ƭ130��\n4�����ϣ���ж�150�ˡ����ܲ�˿30�ˡ�ľ��Ƭ20��\n5��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2�ˡ�����10��."; 
		dish4.name_english = "Scallion Mutton";
		dish4.dishid = 5;
		dish_list.add(dish4);
		
		Dish dish5 = new Dish(R.drawable.hongshaoyukuai, "�������");
		dish5.img_tiny = R.raw.hongshaoyukuai_tiny;
		dish5.text = "1�����ͣ�50��\n2�������ϣ���Ƭ10�ˡ���Ƭ10��\n3�����ϣ����500��\n4�����ϣ��콷50�ˡ��ཷ50��\n5��ˮ��50��\n6�����ϣ���3�ˡ�����2�ˡ�����20��"; 
		dish5.name_english = "Fish block";
		dish5.dishid = 6;
		dish_list.add(dish5);
		
		Dish dish6 = new Dish(R.drawable.chaoshuiguo, "��ˮ��");
		dish6.img_tiny = R.raw.chaoshuiguo_tiny;
		dish6.zhuliao_temp = (byte) 170;
		dish6.fuliao_temp = (byte) 170;
		dish6.text = "1�����ͣ�10��\n2�����ϣ����ҿ�60�ˡ���ݮ��60�ˡ�\n      ƻ����60�ˡ���������60�ˡ�"; 
		dish6.name_english = "Fried fruit";
		dish6.dishid = 7;
		dish_list.add(dish6);
		
		Dish dish7 = new Dish(R.drawable.zhahuasheng, "ը����");
		dish7.img_tiny = R.raw.zhahuasheng_tiny;
		dish7.text = "1�����ͣ�45��\n2�����ϣ�������250��\n3�����ϣ���3��";
		dish7.name_english = "Fried peanuts";
		dish7.dishid = 8;
		dish_list.add(dish7);
		
		Dish dish8 = new Dish(R.drawable.qingchaosuantai, "�峴��̨");
		dish8.img_tiny = R.raw.qingchaosuantai_tiny;
		dish8.text = "1�����ͣ�30��\n2�������ϣ���Ƭ5�ˡ���Ƭ5�� \n3�����ϣ���̦��250��\n4��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2�ˡ�����10��"; 
		dish8.name_english = "Garlic sprout";
		dish8.dishid = 9;
		dish_list.add(dish8);
		
		Dish dish9 = new Dish(R.drawable.suanmiaolarou, "��������");
		dish9.img_tiny = R.raw.suanmiaolarou_tiny;
		dish9.text = "1�����ͣ�35��\n2�������ϣ���Ƭ5�ˡ���Ƭ5��\n3�����ϣ�����Ƭ130��\n4�����ϣ������150�ˡ��콷����Ƭ20�ˡ��ཷ����Ƭ20��\n5��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2�ˡ�����10��"; 
		dish9.name_english = "Garlic Bacon";
		dish9.dishid = 10;
		dish_list.add(dish9);
		
		Dish dish10 = new Dish(R.drawable.hongshaojichi, "���ռ���");
		dish10.img_tiny = R.raw.hongshaojichi_tiny;
		dish10.text = "1�����ͣ�40��\n2�������ϣ���Ƭ5�ˡ���Ƭ10�� \n3�����ϣ������5����������3������2���ϳ顢10����������10���ӱ��ã�\n4�����ϣ��콷����Ƭ30�ˡ��ཷ����Ƭ30�ˡ��㹽Ƭ40��\n5��ˮ�͵��ϣ�ˮ25�ˡ���2�ˡ�����2��"; 
		dish10.name_english = "Chicken wings";
		dish10.dishid = 11;
		dish_list.add(dish10);
		
		Dish dish11 = new Dish(R.drawable.xiqinxiaren, "��ʮ��");
		dish11.img_tiny = R.raw.xiqinxiaren_tiny;
		dish11.text = "1�����ͣ�30��\n2�������ϣ���Ƭ5�ˡ���Ƭ5�� \n3�����ϣ�Ϻ��150�ˣ���10���Ͼ�����5���ӣ�\n4�����ϣ����۶�100�ˡ����ܲ�����Ƭ20��\n5��ˮ�͵��ϣ�ˮ10�ˡ���2�ˡ�����2��"; 
		dish11.name_english = "Celery shrimp";
		dish11.dishid = 1200;
		dish_list.add(dish11);
		
		Dish dish12 = new Dish(R.drawable.chaoqingcai, "���ڲ���");
		dish12.img_tiny = R.raw.tudousi_tiny;
		dish12.text = "1�����ͣ�30��\n2�������ϣ���Ƭ5�ˡ���Ƭ5�� \n3�����ϣ�Ϻ��150�ˣ���10���Ͼ�����5���ӣ�\n4�����ϣ����۶�100�ˡ����ܲ�����Ƭ20��\n5��ˮ�͵��ϣ�ˮ10�ˡ���2�ˡ�����2��"; 
		dish12.name_english = "tomato chip";
		dish12.dishid = 1300;
		dish_list.add(dish12);
		
		try {
			Dish tmp = (Dish) dish5.clone();tmp.dishid = 14;
			dish_list.add(tmp);
			
			tmp = (Dish) dish6.clone();tmp.dishid = 15;
			dish_list.add(tmp);
			
			tmp = (Dish) dish0.clone();tmp.dishid = 16;
			dish_list.add(tmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		dishes = dish_list.toArray(new Dish[dish_list.size()]);
		return dishes;
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
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return d;
	}

	public boolean isBuiltIn() {
		return (type & Constants.DISH_MADE_BY_USER) == 0x00;
	} 
	
}
