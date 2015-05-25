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
	public byte oil = 30; //加油量
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
	public String intro = "";
	public boolean is_cancel_upload = false;
	
	// 材料是有序的
	public LinkedHashMap<String, String> qiangguoliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> tiaoliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> zhuliao_content_map = new LinkedHashMap<String, String>();
	public LinkedHashMap<String, String> fuliao_content_map = new LinkedHashMap<String, String>();
	
	class Material {
		String description;
		BitmapDrawable img_drawable;
		String path;
		int img_resid; // 内置菜谱使用
		public Material(int resid, String desc) {
			img_resid = resid;
			description = desc;
		}
		public Material(){}
	}
	//备料图文详解：包括肉类怎么预处理、菜类切成什么形状
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
		
		if (fuliao_content_map.isEmpty()) {fuliao_time = 0;fuliao_temp=0;fuliao_jiaoban_speed=0;}
		
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
			dishj.put("sound", sound);
			
			dishj.put("type", type);
			
			dishj.put("img_path", img_path);
			dishj.put("img_tiny_path", img_tiny_path);
			
			dishj.put("author_id", author_id);
			dishj.put("author_name", author_name);
			
			dishj.put("device_id", device_id);
			
			dishj.put("intro", intro);
			

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
				dish.intro = "清爽可口，咸的，酸的，辣的都很好吃。土豆营养齐全，易于消化，在欧美享有＂第二面包＂的称号。";
				
				dish.zhuliao_content_map.put("土豆丝", "230克");
				dish.zhuliao_content_map.put("青椒丝", "20克");
				dish.zhuliao_content_map.put("红椒丝", "20克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.tudousi_1, "土豆丝、青椒、红椒切成丝"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
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
			dish1.intro = "品质柔嫩，风味可口，富含维生素、叶绿素、微量元素以及能促进肠道蠕动的纤维素。";
			
			dish1.zhuliao_content_map.put("青菜条", "350克");
			dish1.prepare_material_detail.add(dish1.new Material(R.drawable.qiangguoliao, "炝锅料"));
			dish1.prepare_material_detail.add(dish1.new Material(R.drawable.chaocaixin_1, "青菜"));
			dish1.qiangguoliao_content_map.put("姜丝", "5克");
			dish1.qiangguoliao_content_map.put("蒜片", "5克");
			dish1.tiaoliao_content_map.put("盐", "2克");
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
			dish2.intro = "口味宜人，营养丰富，细软中带有一点点弹性，番茄与鸡蛋的软嫩相协调，既酸甜又爽滑，具有健美抗衰老的作用。";

			
			dish2.zhuliao_content_map.put("鸡蛋", "3个");
			dish2.fuliao_content_map.put("番茄块", "230克");
			dish2.prepare_material_detail.add(dish2.new Material(R.drawable.fanqiejidan_1, "3个鸡蛋加盐打匀"));
			dish2.prepare_material_detail.add(dish2.new Material(R.drawable.fanqiejidan_2, "番茄切块"));
			dish2.tiaoliao_content_map.put("盐", "2克");
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
			dish3.intro = "麻、辣、香、嫩、鲜、滑，经济而且实惠，好吃不贵。豆腐高营养、低脂肪、低热量。可称为最健康的食品之一。";

			dish3.zhuliao_content_map.put("嫩豆腐块", "350克");
			dish3.prepare_material_detail.add(dish3.new Material(R.drawable.maladoufu_2, "姜丝、蒜片，干红椒段，麻椒粒"));
			dish3.prepare_material_detail.add(dish3.new Material(R.drawable.maladoufu_1, "嫩豆腐块"));
			dish3.qiangguoliao_content_map.put("姜丝", "5克");
			dish3.qiangguoliao_content_map.put("蒜片", "5克");
			dish3.qiangguoliao_content_map.put("干红椒段", "3克");
			dish3.qiangguoliao_content_map.put("麻椒粒", "2克");
			dish3.tiaoliao_content_map.put("鸡精", "2克");
			dish3.tiaoliao_content_map.put("盐", "2克");
			dish3.tiaoliao_content_map.put("老抽", "2克");
			dish3.tiaoliao_content_map.put("生抽", "10克");
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
			dish4.intro = "葱香浓郁，羊肉鲜嫩，营养价值高，有壮阳健肾、补虚养身的功效。";
			
			dish4.zhuliao_content_map.put("薄羊肉片", "130克");
			dish4.fuliao_content_map.put("大葱段", "150克");
			dish4.fuliao_content_map.put("红萝卜丝", "30克");
			dish4.fuliao_content_map.put("木耳片", "20克");
			dish4.prepare_material_detail.add(dish4.new Material(R.drawable.qiangguoliao, "姜片、蒜片"));
			dish4.prepare_material_detail.add(dish4.new Material(R.drawable.congbaoyangrou_1, "薄羊肉片"));
			dish4.prepare_material_detail.add(dish4.new Material(R.drawable.congbaoyangrou_2, "大葱段、红萝卜丝、木耳片"));
			dish4.qiangguoliao_content_map.put("姜丝", "5克");
			dish4.qiangguoliao_content_map.put("蒜片", "5克");
			dish4.tiaoliao_content_map.put("鸡精", "2克");
			dish4.tiaoliao_content_map.put("盐", "2克");
			dish4.tiaoliao_content_map.put("生抽", "10克");
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
			dish5.intro = "营养丰富，鲜香味美，健康保健功效之佳肴。";
			
			dish5.zhuliao_content_map.put("鱼块", "500克");
			dish5.fuliao_content_map.put("红椒", "50克");
			dish5.fuliao_content_map.put("青椒", "50克");
			dish5.prepare_material_detail.add(dish5.new Material(R.drawable.qiangguoliao, "炝锅料"));
			dish5.prepare_material_detail.add(dish5.new Material(R.drawable.hongshaoyukuai_1, "鱼清洗后腌制"));
			dish5.prepare_material_detail.add(dish5.new Material(R.drawable.hongshaoyukuai_2, "红椒、青椒切片"));
			dish5.qiangguoliao_content_map.put("姜丝", "10克");
			dish5.qiangguoliao_content_map.put("蒜片", "10克");
			dish5.tiaoliao_content_map.put("鸡精", "2克");
			dish5.tiaoliao_content_map.put("盐", "2克");
			dish5.tiaoliao_content_map.put("生抽", "20克");
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
			dish6.intro = "清鲜爽口，去湿保胃，女士减肥之佳品，适合胃寒湿重人士食用。";
			
			dish6.zhuliao_content_map.put("苹果块", "60克");
			dish6.zhuliao_content_map.put("火龙果块", "60克");
			dish6.prepare_material_detail.add(dish6.new Material(R.drawable.chaoshuiguo_1, "水果切成小块"));
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
			dish7.intro = "香香脆脆、质松可口，含维生素及矿物质钙磷铁等营养成分，但热量与脂肪含量高，不要贪口多吃就行。";
			
			dish7.zhuliao_content_map.put("花生米", "250克");
			dish7.prepare_material_detail.add(dish7.new Material(R.drawable.zhahuasheng_1, "干花生米"));
			dish7.tiaoliao_content_map.put("盐", "3克");
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
			dish8.intro = "蒜台中含有丰富的维生素Ｃ，具有降血脂及预防冠心病和动脉硬化的作用，并可防止血栓的形成。杀菌能力强，可起到预防流感和防止伤口感染的功效。";
			
			dish8.zhuliao_content_map.put("蒜苔段", "250克");
			dish8.prepare_material_detail.add(dish8.new Material(R.drawable.qiangguoliao, "炝锅料"));
			dish8.prepare_material_detail.add(dish8.new Material(R.drawable.qingchaosuantai_1, "蒜台切成短条"));
			dish8.qiangguoliao_content_map.put("姜丝", "5克");
			dish8.qiangguoliao_content_map.put("蒜片", "5克");
			dish8.tiaoliao_content_map.put("鸡精", "2克");
			dish8.tiaoliao_content_map.put("盐", "2克");
			dish8.tiaoliao_content_map.put("生抽", "10克");
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
			dish9.intro = "蒜苗腊肉是湖南传统名菜，醇香美味，健脾开胃，腊肉含钠量较高，高血压患者少吃。";
			
			dish9.zhuliao_content_map.put("腊肉片", "130克");
			dish9.fuliao_content_map.put("蒜苗段", "150克");
			dish9.fuliao_content_map.put("红椒片", "20克");
			dish9.fuliao_content_map.put("青椒片", "20克");
			dish9.prepare_material_detail.add(dish9.new Material(R.drawable.qiangguoliao, "炝锅料"));
			dish9.prepare_material_detail.add(dish9.new Material(R.drawable.suanmiaolarou_1, "腊肉切成小片"));
			dish9.prepare_material_detail.add(dish9.new Material(R.drawable.suanmiaolarou_2, "蒜苗、红椒、青椒切成小片"));
			dish9.qiangguoliao_content_map.put("姜丝", "5克");
			dish9.qiangguoliao_content_map.put("蒜片", "5克");
			dish9.tiaoliao_content_map.put("鸡精", "2克");
			dish9.tiaoliao_content_map.put("盐", "2克");
			dish9.tiaoliao_content_map.put("生抽", "10克");
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
			dish10.intro = "鲜香滑嫩，鸡翅有温中益气、补精添髓、强腰健胃等功效，丰富的胶原蛋白，对于保持皮肤光泽、增强皮肤弹性均有好处。";
			
			dish10.zhuliao_content_map.put("鸡翅", "5个");
			dish10.fuliao_content_map.put("红椒片", "30克");
			dish10.fuliao_content_map.put("青椒片", "30克");
			dish10.fuliao_content_map.put("香菇片", "40克");
			dish10.prepare_material_detail.add(dish10.new Material(R.drawable.qiangguoliao, "炝锅料"));
			dish10.prepare_material_detail.add(dish10.new Material(R.drawable.hongshaojichi_1, "鸡翅根5个，表面切3刀，用老抽、生抽腌制10分钟备用"));
			dish10.prepare_material_detail.add(dish10.new Material(R.drawable.hongshaojichi_2, "红椒、青椒切成菱形小片"));
			dish10.qiangguoliao_content_map.put("姜丝", "5克");
			dish10.qiangguoliao_content_map.put("蒜片", "5克");
			dish10.tiaoliao_content_map.put("鸡精", "2克");
			dish10.tiaoliao_content_map.put("盐", "2克");
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
			dish11.intro = "虾仁营养丰富，鲜嫩可口，西芹可以降压健脑、清肠利便、解毒消肿。西芹虾仁是女士美容瘦脸之佳肴。";
			
			dish11.zhuliao_content_map.put("虾仁", "150克");
			dish11.fuliao_content_map.put("西芹段", "100克");
			dish11.fuliao_content_map.put("红萝卜片", "20克");
			dish11.prepare_material_detail.add(dish11.new Material(R.drawable.qiangguoliao, "炝锅料"));
			dish11.prepare_material_detail.add(dish11.new Material(R.drawable.xiqinxiaren_1, "虾仁用料酒腌制5分钟"));
			dish11.prepare_material_detail.add(dish11.new Material(R.drawable.xiqinxiaren_2, "西芹小段，红萝卜切成菱形小片"));
			dish11.qiangguoliao_content_map.put("姜丝", "5克");
			dish11.qiangguoliao_content_map.put("蒜片", "5克");
			dish11.tiaoliao_content_map.put("鸡精", "2克");
			dish11.tiaoliao_content_map.put("盐", "2克");
			alldish_map.put(dish11.dishid, dish11);
			
//			Dish dish12 = new Dish(R.drawable.chaoqingcai, "新内置菜谱");
//			dish12.img_tiny = R.raw.tudousi_tiny;
//			dish12.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：虾仁150克（加10克料酒腌制5分钟）\n4、辅料：西芹段100克、红萝卜菱形片20克\n5、水和调料：水10克、盐2克、鸡精2克"; 
//			dish12.name_english = "tomato chip";
//			dish12.dishid = 50;
//			alldish_map.put(dish12.dishid, dish12);
			
			{
				Dish dish = new Dish(R.drawable.xiangganroupian, "香干肉片");
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
				dish.intro = "一道开胃下饭的家常好菜。香干中富含蛋白质、维生素、钙、铁、镁、锌、钠等营养元素，营养价值高。";
				
				dish.zhuliao_content_map.put("瘦肉片", "100克");
				dish.fuliao_content_map.put("香干条",  "150克");
				dish.fuliao_content_map.put("香芹段",  "30克");
				dish.fuliao_content_map.put("红椒",   "20克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "加入1克生粉和5克生抽腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_2, "香干条，香芹段，红椒菱形片"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				dish.tiaoliao_content_map.put("生抽", "5克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.jiandouchaorou, "剪豆炒肉");
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
				dish.intro = "荷兰豆爽脆可口，肉片软嫩多汁，吃起来还带有一点点的甜味，食材搭配也合理，实为一道经典的家常热菜。";
				
				dish.zhuliao_content_map.put("瘦肉片", "100克");
				dish.fuliao_content_map.put("荷兰豆",  "170克");
				dish.fuliao_content_map.put("玉米粒",  "30克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "加入1克生粉和5克生抽腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.jiandouchaorou_2, "荷兰豆，玉米粒"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yangcongniurou, "洋葱牛肉");
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
				dish.intro = "鲜香爽口、营养健康。洋葱富含多种微量元素，能促进脂肪代谢，降低血脂、抗动脉硬化、抑制癌症。";
				
				dish.zhuliao_content_map.put("牛肉片", "130克");
				dish.fuliao_content_map.put("洋葱",  "150克");
				dish.fuliao_content_map.put("青椒",  "10克");
				dish.fuliao_content_map.put("红椒",  "10克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yangcongniurou_1, "加入1克生粉和5克生抽5克料酒腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yangcongniurou_2, "洋葱，青椒，红椒"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			
			{
				Dish dish = new Dish(R.drawable.moyushaoya, "魔芋烧鸭");
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
				dish.intro = "魔芋烧鸭是四川传统名菜。色泽红亮，魔芋酥软细腻，鸭肉肥酥，滋味咸中带鲜，辣而有香。";
				
				dish.zhuliao_content_map.put("鸭块", "150克");
				dish.fuliao_content_map.put("魔芋",  "100克");
				dish.fuliao_content_map.put("青椒",  "20克");
				dish.fuliao_content_map.put("红椒",  "20克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_0, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_2, "豆瓣酱"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_1, "鸭块"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_2, "魔芋"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.moyushaoya_3, "青椒、红椒"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.qiangguoliao_content_map.put("干辣椒", "8个");
				dish.qiangguoliao_content_map.put("花椒", "1克");
				dish.qiangguoliao_content_map.put("豆瓣酱", "一勺");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.xiqinlachang, "西芹腊肠");
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
				dish.intro = "非常美味的家常小炒。芹菜是属于高纤维食物，经肠内消化作用能产生抗氧化剂，常吃芹菜，可以有效的帮助皮肤抗衰老。";
				
				dish.zhuliao_content_map.put("腊肠", "150克");
				dish.fuliao_content_map.put("西芹",  "100克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiqinlachang_1, "腊肠"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiqinlachang_2, "西芹"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yumihuotui, "玉米火腿");
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
				dish.intro = "美味的家常菜。玉米中含有大量的营养保健物质，经常食用玉米皮和玉米油对降低人体胆固醇十分有益。";
				
				dish.zhuliao_content_map.put("玉米粒", "100克");
				dish.zhuliao_content_map.put("火腿丁", "80克");
				dish.zhuliao_content_map.put("青豆", "50克");
				dish.zhuliao_content_map.put("红萝卜丁", "30克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yumihuotui_1, "玉米粒，火腿丁，青豆，红萝卜丁"));
				dish.tiaoliao_content_map.put("盐", "1克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.chaoxingbaogu, "炒杏鲍菇");
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
				dish.intro = "杏鲍菇素有＂素肉＂之称，营养丰富，富含蛋白质、碳水化合物、维生素及钙、镁等矿物质，可以提高人体免疫功能，对人体具有抗癌、降血脂、润肠胃以及美容等作用。";
				
				dish.zhuliao_content_map.put("杏鲍菇", "300克");
				dish.zhuliao_content_map.put("红萝卜", "50克");
				dish.zhuliao_content_map.put("青椒菱形片", "50克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.chaoxingbaogu_1, "杏鲍菇"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				dish.tiaoliao_content_map.put("蚝油", "10克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.xiaochaojikuai, "小炒鸡块");
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
				dish.intro = "本菜含有丰富的蛋白质、氨基酸、维生素以及微量元素，具有开胃、益气、润肺的功效，尤其适合老年人及身体虚弱人士食用。";
				
				dish.zhuliao_content_map.put("鸡块", "150克");
				dish.fuliao_content_map.put("青椒", "50克");
				dish.fuliao_content_map.put("红椒", "50克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiaochaojikuai_1, "鸡块"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiaochaojikuai_2, "青椒，红椒"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.liangguachaorou, "凉瓜炒肉");
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
				dish.intro = "味道鲜美、口感丰富、清凉去火、养颜美容。经常食用苦瓜可以消炎退热、降低血压、血脂、血糖、促进新陈代谢。";
				
				dish.zhuliao_content_map.put("瘦肉片", "100克");
				dish.fuliao_content_map.put("凉瓜", "220克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "加入1个生粉和5克生抽腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.liangguachaorou_2, "凉瓜"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.ziranyangrou, "孜然羊肉");
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
				dish.intro = "质地软嫩，鲜辣咸香，营养丰富。此菜品较适合在秋冬季节食用";
				
				dish.zhuliao_content_map.put("羊肉片", "150克");
				dish.fuliao_content_map.put("青椒",  "30克");
				dish.fuliao_content_map.put("洋葱",  "100克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.congbaoyangrou_1, "羊肉片"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.ziranyangrou_2, "青椒，洋葱"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				dish.tiaoliao_content_map.put("孜然粉", "3克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.qingjiaochaorou, "青椒炒肉");
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
				dish.intro = "荤素搭配，清新爽脆。想要漂亮的ＭＭ们更是要多吃此菜，青椒、红椒中所包含的维生素Ｃ可以让爱美的你更漂亮哦。";
				
				dish.zhuliao_content_map.put("瘦肉片", "100克");
				dish.fuliao_content_map.put("青椒片",  "120克");
				dish.fuliao_content_map.put("红椒",  "50克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "加入1克生粉和5克生抽腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qingjiaochaorou_2, "青椒丝，红椒"));
				dish.qiangguoliao_content_map.put("姜丝", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				dish.tiaoliao_content_map.put("生抽", "5克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.boluochaorou, "菠萝炒肉");
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
				dish.intro = "菠萝果实品质优良，营养丰富，有清热解暑、消化不良、头昏眼花等症。而且在果汁中，还含有一种跟胃液相类似的酵素，可以分解蛋白，帮助消化。不仅可以减肥，而且对身体健康有着不同的功效。";
				
				dish.zhuliao_content_map.put("肉", "50克");
				dish.fuliao_content_map.put("菠萝",  "150克");
				dish.fuliao_content_map.put("黄瓜",  "20克");
				dish.fuliao_content_map.put("胡萝卜",  "20克");
				dish.fuliao_content_map.put("芋头",  "20克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.boluochaorou_1, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.boluochaorou_2, "肉"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.boluochaorou_3, "菠萝"));
				dish.qiangguoliao_content_map.put("蒜丁", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.gongbaojiding, "宫保鸡丁");
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
				dish.intro = "辣中有点甜，甜中有点辣，是宫保鸡丁的特色，是餐馆中上桌率极高的一道菜，尤其是在国外的餐馆中，外国人说起中国菜，就是宫保鸡丁了，这更是外国人吃中餐时，最常点的一道菜";
				
				dish.zhuliao_content_map.put("鸡脯肉", "150克");
				dish.zhuliao_content_map.put("胡萝卜", "50克");
				dish.fuliao_content_map.put("黄瓜",  "150克");
				dish.fuliao_content_map.put("油炸花生米(熟)",  "50克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_1, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_2, "鸡肉脯切丁，用料酒、生抽、盐、白胡椒、油、生粉腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_3, "红萝卜切丁"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_4, "黄瓜切丁"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_5, "熟的油炸花生米"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.gongbaojiding_6, "配置好的调料"));
				dish.qiangguoliao_content_map.put("姜片", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.qiangguoliao_content_map.put("葱", "15克");
				dish.qiangguoliao_content_map.put("干辣椒段", "5克");
				dish.qiangguoliao_content_map.put("花椒", "1克");
				dish.tiaoliao_content_map.put("醋", "2.5克");
				dish.tiaoliao_content_map.put("酱油", "5克");
				dish.tiaoliao_content_map.put("糖", "4克");
				dish.tiaoliao_content_map.put("料酒", "2.5克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yuxiangrousi, "鱼香肉丝");
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
				dish.intro = "鱼香肉丝是一道常见川菜。鱼香，是四川菜肴主要传统味型之一。因为菜品具有鱼香味，其味是调味品调制而成。此法源出于四川民间独具特色的烹鱼调味方法，而今已广泛用于川味的熟菜中。";
				
				dish.zhuliao_content_map.put("猪肉", "200克");
				dish.fuliao_content_map.put("青笋丝", "150克");
				dish.fuliao_content_map.put("胡萝卜丝",  "150克");
				dish.fuliao_content_map.put("木耳",  "50克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_1, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_2, "豆瓣酱一勺"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_3, "猪肉用料酒、生抽、盐、白胡椒、油、生粉腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_4, "青笋、胡萝卜、木耳"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_5, "配置好的调料"));
				dish.qiangguoliao_content_map.put("姜片", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.qiangguoliao_content_map.put("豆瓣酱", "15克");
				dish.tiaoliao_content_map.put("醋", "2.5克");
				dish.tiaoliao_content_map.put("酱油", "5克");
				dish.tiaoliao_content_map.put("糖", "4克");
				dish.tiaoliao_content_map.put("料酒", "2.5克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.nanguachaorou, "南瓜炒肉");
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
				dish.intro = "选择新上市的嫩南瓜加爆炒肉片烹饪而成，荤素搭配，小南瓜嫩、粉，肉鲜嫩不夹牙，非常适合老人和小孩吃。据说南瓜有很高的食疗作用，可促进胰岛素分泌，降低血糖水平，可以预防糖尿病，不过有脚气的人不宜多吃……";
				
				dish.zhuliao_content_map.put("猪肉", "50克");
				dish.zhuliao_content_map.put("胡萝卜丝",  "30克");
				dish.fuliao_content_map.put("南瓜", "120克");
				dish.fuliao_content_map.put("青椒", "30克");
				dish.fuliao_content_map.put("木耳",  "30克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "猪肉用料酒、生抽、盐、白胡椒、油、生粉腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.nanguachaorou_3, "南瓜、青椒、木耳"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.nanguachaorou_4, "胡萝卜丝"));
				dish.qiangguoliao_content_map.put("姜片", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				alldish_map.put(dish.dishid, dish);
			}
			
			{
				Dish dish = new Dish(R.drawable.yuxiangqiezi, "鱼香茄子");
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
				dish.intro = "鱼香茄子是汉族特色名菜，属中国八大菜系的，主料为茄子，配以多种辅料加工烧制而成。有多种不同制法，其味道鲜美，营养丰富。";
				
				dish.zhuliao_content_map.put("猪肉", "50克");
				dish.fuliao_content_map.put("茄子", "350克");
				dish.fuliao_content_map.put("胡萝卜丝",  "30克");
				dish.fuliao_content_map.put("木耳",  "30克");
				dish.fuliao_content_map.put("青椒",  "30克");
				dish.prepare_material_detail.add(dish.new Material(R.drawable.qiangguoliao, "炝锅料"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangrousi_2, "豆瓣酱一勺"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.xiangganroupian_1, "猪肉用料酒、生抽、盐、白胡椒、油、生粉腌制10分钟"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangqiezi_3, "茄子用盐腌制30分钟，然后去水、青椒、胡萝卜、木耳"));
				dish.prepare_material_detail.add(dish.new Material(R.drawable.yuxiangqiezi_4, "调料"));
				dish.qiangguoliao_content_map.put("姜片", "5克");
				dish.qiangguoliao_content_map.put("蒜片", "5克");
				dish.qiangguoliao_content_map.put("豆瓣酱", "15克");
				dish.tiaoliao_content_map.put("醋", "10克");
				dish.tiaoliao_content_map.put("生抽", "6克");
				dish.tiaoliao_content_map.put("糖", "12克");
				dish.tiaoliao_content_map.put("料酒", "3克");
				dish.tiaoliao_content_map.put("淀粉", "3克");
				dish.tiaoliao_content_map.put("鸡精", "2克");
				dish.tiaoliao_content_map.put("盐", "2克");
				dish.tiaoliao_content_map.put("水", "18克");
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
