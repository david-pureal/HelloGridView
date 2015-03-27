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
	// 用户自编的菜谱在上传前的临时id，上传后由服务端重新分配id
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
	public byte water = 0;//0代表不需加水， 1代表加入主料时水，2代表加入辅料时水
	public int water_weight = 0; // 加水量 ， 单位：克
	public byte oil = 10; //加油量
	public byte qiangguoliao = 1;//炝锅料 0表示无， 1表示有
	
	public Integer img = R.drawable.tudousi;   // APP自带的菜谱
	public BitmapDrawable img_drawable = null; // 自编菜谱，用来在APP上展示
	public String img_path; //自编菜谱，用于存储
	
	public Integer img_tiny = R.drawable.tudousi;   //机器上显示的小图片，大小为106*76
	public String img_tiny_path;
	public Integer sound;
	
	public boolean isBuiltIn = true;
	public int type = Constants.DISH_MADE_BY_SYSTEM & Constants.DISH_DEVICE_BUILTIN & Constants.DISH_FAVORITE;
	
	
	public String text = "1、底油：20克\n2、炝锅料：姜丝5克、蒜片5克\n3、主料：土豆丝230克,青椒丝20克，\n      红椒丝20克\n4、调料：鸡精2克、盐2克";
	public String qiangguoliao_content = "姜丝5克、蒜片5克";
	protected String zhuliao_content = "土豆丝: 230克\n青椒丝: 20克";
	public String fuliao_content = "";
	
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
	
	private static Dish[] dishes = null;
	//TODO ArrayList改为LinkedHashMap<short, Dish>
	public static ArrayList<Dish>  dish_list = null;
	public static TreeMap<Integer, Dish> alldish_map;
	
	public Dish(Integer img, String name) {
		this.img = img;
		this.name_chinese = name;
	}
	
	//用户自编菜谱
	public static int addDish(Dish d) {
		if (dish_list == null) getAllDish();
		//d.img_tiny = R.raw.zibian_tiny;
		d.img_tiny = R.raw.tudousi_tiny;
		//d.img_tiny = R.raw.temp_tiny;
		d.name_english = HanziToPinyin.getPinYin(d.name_chinese);
		if (d.name_english.length() > 20) d.name_english.substring(0, 13);//英文名字有最大长度
		DeviceState ds = DeviceState.getInstance();
		short max = 0;
		for (int i = 0; i < ds.builtin_dishids.length ; ++i) {
			if (ds.builtin_dishids[i] > max) max = ds.builtin_dishids[i];
		}
		if (max > current_makedish_dishid) current_makedish_dishid = max + 1;
		d.dishid = (short) ++current_makedish_dishid;
		
		// create directory
		Tool.getInstance().make_directory(d.getDishDirName());
		
		// 默认的菜谱图片
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
		// 用于写入文件
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
		
		Dish dish0 = new Dish(R.drawable.tudousi, "炒土豆丝");
		dish0.img_tiny = R.raw.tudousi_tiny;
		dish0.zhuliao_temp = (byte) 180;
		dish0.fuliao_temp = (byte) 180;
		dish0.zhuliao_time = 230;
		dish0.fuliao_time = 0;
		dish0.zhuliao_jiaoban_speed = 5;
		dish0.fuliao_jiaoban_speed = 5;
		dish0.text = "1、底油：30克\n2、炝锅料：姜丝5克、蒜片5克\n3、主料：土豆丝230克,青椒丝20克，\n      红椒丝20克\n4、调料：鸡精2克、盐2克"; 
		dish0.name_english = "tomato chip";
		//dish0.sound = R.raw.tudousi_voice;
		dish0.dishid = 1;
		dish_list.add(dish0);
		
		Dish dish1 = new Dish(R.drawable.chaoqingcai, "炒青菜");
		dish1.img_tiny = R.raw.chaoqingcai_tiny;
		dish1.text = "1、底油：20克\n2、炝锅料：姜丝5克、蒜片5克\n3、主料：青菜条350克\n4、调料：盐2克";
		dish1.name_english = "Fried peanuts";
		//dish1.sound = R.raw.tudousi_voice;
		dish1.dishid = 2;
		dish_list.add(dish1);
		
		Dish dish2 = new Dish(R.drawable.fanqiechaodan, "番茄炒蛋");
		dish2.img_tiny = R.raw.fanqiechaodan_tiny;//87%
		dish2.text = "1、底油：45克\n2、主料：3个鸡蛋加0.5克盐打匀\n3、辅料：番茄块230克\n4、调料：盐2克"; 
		dish2.name_english = "Tomato Eggs";
		//dish2.sound = R.raw.tudousi_voice;
		dish2.dishid = 3;
		dish_list.add(dish2);
		
		Dish dish3 = new Dish(R.drawable.maladoufu, "麻辣豆腐");
		dish3.img_tiny = R.raw.maladoufu_tiny;
		dish3.text = "1、底油：25克\n2、炝锅料：姜丝5克、蒜片5克，干红椒段3克，麻椒粒2克\n3、主料：嫩豆腐块350克\n4、水和调料：水30克、盐2克、鸡精2克、老抽2克、生抽10克"; 
		dish3.name_english = "Mapo Tofu";
		//dish3.sound = R.raw.tudousi_voice;
		dish3.dishid = 4;
		dish_list.add(dish3);
		
		Dish dish4 = new Dish(R.drawable.congbaoyangrou, "葱爆羊肉");
		dish4.img_tiny = R.raw.congbaoyangrou_tiny;
		dish4.text = "1、底油：35克\n2、炝锅料：姜片5克、蒜片5克\n3、主料：薄羊肉片130克\n4、辅料：大葱段150克、红萝卜丝30克、木耳片20克\n5、水和调料：水25克、盐2克、鸡精2克、生抽10克."; 
		dish4.name_english = "Scallion Mutton";
		dish4.dishid = 5;
		dish_list.add(dish4);
		
		Dish dish5 = new Dish(R.drawable.hongshaoyukuai, "红烧鱼块");
		dish5.img_tiny = R.raw.hongshaoyukuai_tiny;
		dish5.text = "1、底油：50克\n2、炝锅料：姜片10克、蒜片10克\n3、主料：鱼块500克\n4、辅料：红椒50克、青椒50克\n5、水：50克\n6、调料：盐3克、鸡精2克、生抽20克"; 
		dish5.name_english = "Fish block";
		dish5.dishid = 6;
		dish_list.add(dish5);
		
		Dish dish6 = new Dish(R.drawable.chaoshuiguo, "炒水果");
		dish6.img_tiny = R.raw.chaoshuiguo_tiny;
		dish6.zhuliao_temp = (byte) 170;
		dish6.fuliao_temp = (byte) 170;
		dish6.text = "1、底油：10克\n2、主料：杨桃块60克、草莓块60克、\n      苹果块60克、火龙果块60克。"; 
		dish6.name_english = "Fried fruit";
		dish6.dishid = 7;
		dish_list.add(dish6);
		
		Dish dish7 = new Dish(R.drawable.zhahuasheng, "炸花生");
		dish7.img_tiny = R.raw.zhahuasheng_tiny;
		dish7.text = "1、底油：45克\n2、主料：花生米250克\n3、调料：盐3克";
		dish7.name_english = "Fried peanuts";
		dish7.dishid = 8;
		dish_list.add(dish7);
		
		Dish dish8 = new Dish(R.drawable.qingchaosuantai, "清炒蒜台");
		dish8.img_tiny = R.raw.qingchaosuantai_tiny;
		dish8.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：蒜苔段250克\n4、水和调料：水25克、盐2克、鸡精2克、生抽10克"; 
		dish8.name_english = "Garlic sprout";
		dish8.dishid = 9;
		dish_list.add(dish8);
		
		Dish dish9 = new Dish(R.drawable.suanmiaolarou, "蒜苗腊肉");
		dish9.img_tiny = R.raw.suanmiaolarou_tiny;
		dish9.text = "1、底油：35克\n2、炝锅料：姜片5克、蒜片5克\n3、主料：腊肉片130克\n4、辅料：蒜苗段150克、红椒菱形片20克、青椒菱形片20克\n5、水和调料：水25克、盐2克、鸡精2克、生抽10克"; 
		dish9.name_english = "Garlic Bacon";
		dish9.dishid = 10;
		dish_list.add(dish9);
		
		Dish dish10 = new Dish(R.drawable.hongshaojichi, "红烧鸡翅");
		dish10.img_tiny = R.raw.hongshaojichi_tiny;
		dish10.text = "1、底油：40克\n2、炝锅料：姜片5克、蒜片10克 \n3、主料：鸡翅根5个（表面切3刀，加2克老抽、10克生抽腌制10分钟备用）\n4、辅料：红椒菱形片30克、青椒菱形片30克、香菇片40克\n5、水和调料：水25克、盐2克、鸡精2克"; 
		dish10.name_english = "Chicken wings";
		dish10.dishid = 11;
		dish_list.add(dish10);
		
		Dish dish11 = new Dish(R.drawable.xiqinxiaren, "菜十二");
		dish11.img_tiny = R.raw.xiqinxiaren_tiny;
		dish11.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：虾仁150克（加10克料酒腌制5分钟）\n4、辅料：西芹段100克、红萝卜菱形片20克\n5、水和调料：水10克、盐2克、鸡精2克"; 
		dish11.name_english = "Celery shrimp";
		dish11.dishid = 1200;
		dish_list.add(dish11);
		
		Dish dish12 = new Dish(R.drawable.chaoqingcai, "新内菜谱");
		dish12.img_tiny = R.raw.tudousi_tiny;
		dish12.text = "1、底油：30克\n2、炝锅料：姜片5克、蒜片5克 \n3、主料：虾仁150克（加10克料酒腌制5分钟）\n4、辅料：西芹段100克、红萝卜菱形片20克\n5、水和调料：水10克、盐2克、鸡精2克"; 
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
