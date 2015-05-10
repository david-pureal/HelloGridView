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
	// 用户自编的菜谱在上传前的临时id，上传后由服务端重新分配id
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
	public byte water = 0;//0代表不需加水， 1代表加入主料时水，2代表加入辅料时水
	public int water_weight = 0; // 加水量 ， 单位：克
	public byte oil = 10; //加油量
	public byte qiangguoliao = 1;//炝锅料 0表示无， 1表示有
	
	public Integer img = R.drawable.tudousi;   // APP自带的菜谱
	public Bitmap img_bmp = null; // 自编菜谱，用来在APP上展示
	public String img_path; //自编菜谱，用于存储
	
	public Integer img_tiny = R.drawable.tudousi;   //机器上显示的小图片，大小为106*76
	public String img_tiny_path;
	public int sound = 0;
	
	//public boolean isBuiltIn = true;
	public int type = Constants.DISH_APP_BUILTIN | Constants.DISH_DEVICE_BUILTIN | Constants.DISH_FAVORITE;
	
	public String text = "1、底油：20克\n2、炝锅料：姜丝5克、蒜片5克\n3、主料：土豆丝230克,青椒丝20克，红椒丝20克\n4、调料：鸡精2克、盐2克";
	public String qiangguoliao_content = "姜丝5克、蒜片5克";
	protected String zhuliao_content = "土豆丝: 230克\n青椒丝: 20克";
	public String fuliao_content;
	public ArrayList<Integer> materials = new ArrayList<Integer>();
	
	public String author_id;
	public String author_name;
	public String device_id;
	
	// 材料是有序的
	public LinkedHashMap<String, String> zhuliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> fuliao_content_map = new LinkedHashMap<String, String>();
	
	class Material {
		String description;
		BitmapDrawable img_drawable;
		String path;
	}
	//备料图文详解：包括肉类怎么预处理、菜类切成什么形状
	public ArrayList<Material> prepare_material_detail = new ArrayList<Material>(); 
	
	public static LinkedHashMap<Integer, Dish> alldish_map;
	
	public Dish(Integer img, String name) {
		this.img = img;
		this.name_chinese = name;
		
		fuliao_content = "";
		author_id = "";
		author_name = "";
		device_id = "";
	}
	
	//用户自编菜谱
	public static int addDish(Dish d) {
		if (alldish_map == null) getAllDish();
		//d.img_tiny = R.raw.zibian_tiny;
		d.img_tiny = R.raw.tudousi_tiny;
		//d.img_tiny = R.raw.temp_tiny;
		d.name_english = HanziToPinyin.getPinYin(d.name_chinese);
		if (d.name_english.length() > 13) d.name_english = d.name_english.substring(0, 13);//英文名字有最大长度
		
		DeviceState ds = DeviceState.getInstance();
		short max = 0;
		for (int i = 0; i < ds.builtin_dishids.length ; ++i) {
			if (ds.builtin_dishids[i] > max) max = ds.builtin_dishids[i];
		}
		if (max > current_makedish_dishid) current_makedish_dishid = max;
		d.dishid = ++current_makedish_dishid;
		
		// create directory
		Tool.getInstance().make_directory(d.getDishDirName());
		
		// 菜谱默认图片
		// 此处为相对路径
		d.img_path = Constants.DISH_IMG_FILENAME;
		Bitmap btm = d.img_bmp;
		String path = d.getDishDirName() + d.img_path;
		Tool.getInstance().savaBitmap(btm, path);
		d.img_tiny_path = Tool.getInstance().makeTinyImage(d);// 此处为相对路径
		
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
		
		// 用于写入文件
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
			dishj.put("qiangguoliao_content", qiangguoliao_content);
			dishj.put("sound", sound);
			
			dishj.put("type", type);
			
			dishj.put("img_path", img_path);
			dishj.put("img_tiny_path", img_tiny_path);
			
			dishj.put("author_id", author_id);
			dishj.put("author_name", author_name);
			
			dishj.put("device_id", device_id);
			

			// 主料，辅料和备料图文写入文件
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
			Tool.getInstance().writeFile(dishj.toString().getBytes(), param_path);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static LinkedHashMap<Integer, Dish> getAllDish() {
		if (alldish_map == null) {
			alldish_map = new LinkedHashMap<Integer, Dish>();
			
			{
				Dish dish = new Dish(R.drawable.tudousi, "炒土豆丝");
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
				dish.text = "1、底油：30克\n2、炝锅料：姜丝5克、蒜片5克\n3、主料：土豆丝230克,青椒丝20克,红椒丝20克\n4、调料：鸡精2克、盐2克"; 
				dish.name_english = "tomato chip";
				//dish0.sound = R.raw.tudousi_voice;
				dish.dishid = 1;
				dish.materials.add(R.drawable.tudousi_1);
				dish.materials.add(R.drawable.qiangguoliao);
				alldish_map.put(dish.dishid, dish);
			}
			
			Dish dish1 = new Dish(R.drawable.chaoqingcai, "炒青菜");
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
			dish1.text = "1、底油：20克\n2、炝锅料：姜丝5克、蒜片5克\n3、主料：青菜条350克\n4、调料：盐2克";
			dish1.name_english = "Fried peanuts";
			//dish1.sound = R.raw.tudousi_voice;
			dish1.dishid = 2;
			dish1.materials.add(R.drawable.chaocaixin_1);
			dish1.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish1.dishid, dish1);
			
			Dish dish2 = new Dish(R.drawable.fanqiechaodan, "番茄炒蛋");
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
			dish2.text = "1、底油：45克\n2、主料：3个鸡蛋加0.5克盐打匀\n3、辅料：番茄块230克\n4、调料：盐2克"; 
			dish2.name_english = "Tomato Eggs";
			//dish2.sound = R.raw.tudousi_voice;
			dish2.dishid = 3;
			dish2.materials.add(R.drawable.fanqiejidan_1);
			dish2.materials.add(R.drawable.fanqiejidan_2);
			alldish_map.put(dish2.dishid, dish2);
			
			Dish dish3 = new Dish(R.drawable.maladoufu, "麻辣豆腐");
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
			dish3.text = "1、底油：25克\n2、炝锅料：姜丝5克、蒜片5克，干红椒段3克，麻椒粒2克\n3、主料：嫩豆腐块350克\n4、水和调料：水30克、盐2克、鸡精2克、老抽2克、生抽10克"; 
			dish3.name_english = "Mapo Tofu";
			//dish3.sound = R.raw.tudousi_voice;
			dish3.dishid = 4;
			dish3.materials.add(R.drawable.maladoufu_1);
			dish3.materials.add(R.drawable.maladoufu_2);
			alldish_map.put(dish3.dishid, dish3);
			
			Dish dish4 = new Dish(R.drawable.congbaoyangrou, "葱爆羊肉");
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
			dish4.text = "1、底油：35克\n2、炝锅料：姜片5克、蒜片5克\n3、主料：薄羊肉片130克\n4、辅料：大葱段150克、红萝卜丝30克、木耳片20克\n5、水和调料：水25克、盐2克、鸡精2克、生抽10克."; 
			dish4.name_english = "Scallion Mutton";
			dish4.dishid = 5;
			dish4.materials.add(R.drawable.congbaoyangrou_1);
			dish4.materials.add(R.drawable.congbaoyangrou_2);
			dish4.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish4.dishid, dish4);
			
			Dish dish5 = new Dish(R.drawable.hongshaoyukuai, "红烧鱼块");
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
			dish5.text = "1、底油：50克\n2、炝锅料：姜片10克、蒜片10克\n3、主料：鱼块500克\n4、辅料：红椒50克、青椒50克\n5、水：50克\n6、调料：盐3克、鸡精2克、生抽20克"; 
			dish5.name_english = "Fish block";
			dish5.dishid = 6;
			dish5.materials.add(R.drawable.hongshaoyukuai_1);
			dish5.materials.add(R.drawable.hongshaoyukuai_2);
			dish5.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish5.dishid, dish5);
			
			Dish dish6 = new Dish(R.drawable.chaoshuiguo, "炒水果");
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
			dish6.text = "1、底油：10克\n2、主料：杨桃块60克、草莓块60克、\n      苹果块60克、火龙果块60克。"; 
			dish6.name_english = "Fried fruit";
			dish6.dishid = 7;
			dish6.qiangguoliao = 0;
			dish6.materials.add(R.drawable.chaoshuiguo_1);
			alldish_map.put(dish6.dishid, dish6);
			
			Dish dish7 = new Dish(R.drawable.zhahuasheng, "炸花生");
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
			dish7.text = "1、底油：45克\n2、主料：花生米250克\n3、调料：盐3克";
			dish7.name_english = "Fried peanuts";
			dish7.dishid = 8;
			dish7.materials.add(R.drawable.zhahuasheng_1);
			alldish_map.put(dish7.dishid, dish7);
			
			Dish dish8 = new Dish(R.drawable.qingchaosuantai, "清炒蒜台");
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
			dish8.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：蒜苔段250克\n4、水和调料：水25克、盐2克、鸡精2克、生抽10克"; 
			dish8.name_english = "Garlic sprout";
			dish8.dishid = 9;
			dish8.materials.add(R.drawable.qingchaosuantai_1);
			dish8.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish8.dishid, dish8);
			
			Dish dish9 = new Dish(R.drawable.suanmiaolarou, "蒜苗腊肉");
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
			dish9.text = "1、底油：35克\n2、炝锅料：姜片5克、蒜片5克\n3、主料：腊肉片130克\n4、辅料：蒜苗段150克、红椒菱形片20克、青椒菱形片20克\n5、水和调料：水25克、盐2克、鸡精2克、生抽10克"; 
			dish9.name_english = "Garlic Bacon";
			dish9.dishid = 10;
			dish9.materials.add(R.drawable.suanmiaolarou_1);
			dish9.materials.add(R.drawable.suanmiaolarou_2);
			dish9.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish9.dishid, dish9);
			
			Dish dish10 = new Dish(R.drawable.hongshaojichi, "红烧鸡翅");
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
			dish10.text = "1、底油：40克\n2、炝锅料：姜片5克、蒜片10克 \n3、主料：鸡翅根5个（表面切3刀，加2克老抽、10克生抽腌制10分钟备用）\n4、辅料：红椒菱形片30克、青椒菱形片30克、香菇片40克\n5、水和调料：水25克、盐2克、鸡精2克"; 
			dish10.name_english = "Chicken wings";
			dish10.dishid = 11;
			dish10.materials.add(R.drawable.hongshaojichi_1);
			dish10.materials.add(R.drawable.hongshaojichi_2);
			dish10.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish10.dishid, dish10);
			
			Dish dish11 = new Dish(R.drawable.xiqinxiaren, "西芹虾仁");
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
			dish11.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：虾仁150克（加10克料酒腌制5分钟）\n4、辅料：西芹段100克、红萝卜菱形片20克\n5、水和调料：水10克、盐2克、鸡精2克"; 
			dish11.name_english = "Celery shrimp";
			dish11.dishid = 12;
			dish11.materials.add(R.drawable.xiqinxiaren_1);
			dish11.materials.add(R.drawable.xiqinxiaren_2);
			dish11.materials.add(R.drawable.qiangguoliao);
			alldish_map.put(dish11.dishid, dish11);
			
			Dish dish12 = new Dish(R.drawable.chaoqingcai, "新内菜谱");
			dish12.img_tiny = R.raw.tudousi_tiny;
			dish12.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：虾仁150克（加10克料酒腌制5分钟）\n4、辅料：西芹段100克、红萝卜菱形片20克\n5、水和调料：水10克、盐2克、鸡精2克"; 
			dish12.name_english = "tomato chip";
			dish12.dishid = 13;
			alldish_map.put(dish12.dishid, dish12);
		
			try {
				Dish tmp = (Dish) dish5.clone();tmp.dishid = 14;tmp.name_chinese = "菜十四";
				alldish_map.put(tmp.dishid, tmp);
				
				tmp = (Dish) dish6.clone();tmp.dishid = 15;tmp.name_chinese = "菜十五";
				alldish_map.put(tmp.dishid, tmp);
				
				tmp = (Dish) dish1.clone();tmp.dishid = 16;tmp.name_chinese = "菜十六";
				alldish_map.put(tmp.dishid, tmp);
			} catch (Exception e) {
				e.printStackTrace();
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
		// 手机是个非常私人的设备，不考虑两个人用同一个的app的问题，当在一台设备上时就认为是同一个人，菜谱可以相互看到
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

	public static void remove_not_uploaded_dish(Dish dish) {
		// TODO Auto-generated method stub
		String path = dish.getDishDirName();
		Tool.getInstance().deleteDirectory(path);
		
		Dish.getAllDish().remove(dish.dishid);
	}
	

}
