package study.hellogridview;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.util.Log;

// 机器功能选项，1为语音提示； 2为使用英文
enum Option {	
	SOUND(1),
	USE_ENGLISH(2);
	
	Option(int i){}
}

//命令类型
enum CMD_TYPE {
	Send_Dish(101),         // 手机发送菜谱数据给机器
	Set_Param(102),         // 手机对机器参数进行设置
	Get_Favorite(103),      // 手机请求机器内置菜品
	Update_Favorite(104),   // 手机更新机器的内置菜谱
	Set_Option(105),        // 手机设置机器功能
	Img_Data(108),          // 手机发图片数据给机器
	Voice_Data(109),        // 手机发音频数据给机器
	Machine_State(201),     // 机器推送自身状态到手机
	Get_Favorite_Resp(203), // 机器返回内置菜品
	ACK(127);               // 应答
	
	CMD_TYPE(int i){  }
}

class RespPackage {
	public boolean is_ok = true;
	public int reqid_head = 0;
	public int reqid_body = 0; // 目前是上次请求的请求序号
	
	public int device_id = 0;
	
	public byte cmdtype_head = 0;
	public byte cmdtype_body = 0;
}

//一个交互的完整的数据包
public class Package {
	
	public static final byte Send_Dish = 101;

	public static final byte Set_Param = 102;
	public static final byte Get_Favorite = 103;
	public static final byte Update_Favorite = 104;
	public static final byte Set_Option = 105;
	public static final byte Img_Data = 108;
	public static final byte Voice_Data = 109;
	public static final byte Machine_State = (byte) 201;
	public static final byte Get_Favorite_Resp = (byte) 203;
	public static final byte ACK = 127;
	int dish_name_length = 20;

	public DishActivity activity;
	public CurStateActivity curstate_activity;

	Package(byte cmdtype) {
		activity = TCPClient.getInstance().dish_activity;
		curstate_activity = TCPClient.getInstance().curstate_activity;
		this.reqid = (int) (System.currentTimeMillis() / 1000);
		this.cmdtype = cmdtype;
	}
	
	Package(byte cmdtype, Dish dish) {
		activity = TCPClient.getInstance().dish_activity;
		curstate_activity = TCPClient.getInstance().curstate_activity;
		this.cmdtype = cmdtype;
		this.dish = dish;
		this.reqid = (int) (System.currentTimeMillis() / 1000);
	}
	
	public ByteArrayOutputStream getBytes() {
		// add head
 		byte[] tmp = new byte[4];
		putShort(tmp, this.length);
		
		bytestream.write(version);
		
		putShort(tmp, someid);
		
		bytestream.write(etype);
		
		putInt(tmp, deviceid);
	
		putInt(tmp, reqid);
		
		bytestream.write(cmdtype);
		
		switch (cmdtype) {
		case Send_Dish :	
			// read image into byte array
			prepare_img_sounddata();
			
			try {
				byte[] bytes = dish.name_chinese.getBytes("GB2312");
				if (bytes.length < dish_name_length) { //Chinese name is length is 10
					bytestream.write(bytes);
					byte[] b = new byte[dish_name_length - bytes.length];
					bytestream.write(b);
				}
				else {
					bytestream.write(bytes, 0, dish_name_length);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("Package", "getBytes chinese name as GB2312 error!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("Package", "getBytes chinese name as GB2312 error!");
			}
			
			
			putShort(tmp, dish.dishid);
//			bytestream.write(tmp, 25, 2);
			
			putShort(tmp, dish.zhuliao_time);
//			bytestream.write(tmp, 27, 2);
			
			putShort(tmp, dish.fuliao_time);
//			bytestream.write(tmp, 29, 2);
			
			bytestream.write(dish.zhuliao_temp);
			
			bytestream.write(dish.fuliao_temp);
			
			bytestream.write(dish.zhuliao_jiaoban_speed);
			bytestream.write(dish.fuliao_jiaoban_speed);
			
			bytestream.write(dish.water);
			
			bytestream.write(dish.oil);
			
			bytestream.write(dish.qiangguoliao);
			
			Log.i("Package", "img_data.length = " + img_data.length);
			putInt(tmp, img_data.length);
			
			try {
				byte[] bytes = dish.name_english.getBytes("GB2312");
				if (bytes.length < dish_name_length) { //english name is length is 13
					bytestream.write(bytes);
					byte[] b = new byte[dish_name_length];
					bytestream.write(b);
				}
				else {
					bytestream.write(bytes, 0, dish_name_length);
				}
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("Package", "getBytes english name as GB2312 error!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("Package", "getBytes chinese name as GB2312 error!");
			}
			
			break;
		case Set_Param :
			try {
				//Log.v("Package", "write bytes trace!");
				//putShort(tmp, this.curstate_activity.time);
				putShort(tmp, DeviceState.getInstance().time);
				bytestream.write(this.curstate_activity.temp);
				bytestream.write(this.curstate_activity.jiaoban_speed);
				bytestream.write(this.curstate_activity.control);
				putShort(tmp, (short)this.curstate_activity.dish_index);
				bytestream.write(this.curstate_activity.modify_state);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("Package", "getBytes exception!");
			}
			break;
			
		case Update_Favorite :
			try {
				//Log.v("Package", "write bytes trace!");
				short dish_be_replaced = 8;
				putShort(tmp, dish_be_replaced);
				
				// read image into byte array
				prepare_img_sounddata();
				
				try {
					byte[] bytes = dish.name_chinese.getBytes("GB2312");
					if (bytes.length < dish_name_length) { //Chinese name is length is 10
						bytestream.write(bytes);
						byte[] b = new byte[dish_name_length - bytes.length];
						bytestream.write(b);
					}
					else {
						bytestream.write(bytes, 0, dish_name_length);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("Package", "getBytes chinese name as GB2312 error!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("Package", "getBytes chinese name as GB2312 error!");
				}
				
				
				putShort(tmp, dish.dishid);
//				bytestream.write(tmp, 25, 2);
				
				putShort(tmp, dish.zhuliao_time);
//				bytestream.write(tmp, 27, 2);
				
				putShort(tmp, dish.fuliao_time);
//				bytestream.write(tmp, 29, 2);
				
				bytestream.write(dish.zhuliao_temp);
				
				bytestream.write(dish.fuliao_temp);
				
				bytestream.write(dish.zhuliao_jiaoban_speed);
				bytestream.write(dish.fuliao_jiaoban_speed);
				
				bytestream.write(dish.water);
				
				bytestream.write(dish.oil);
				
				bytestream.write(dish.qiangguoliao);
				
				Log.i("Package", "img_data.length = " + img_data.length);
				putInt(tmp, img_data.length);
				
				try {
					byte[] bytes = dish.name_english.getBytes("GB2312");
					if (bytes.length < dish_name_length) { //english name is length is 13
						bytestream.write(bytes);
						byte[] b = new byte[dish_name_length];
						bytestream.write(b);
					}
					else {
						bytestream.write(bytes, 0, dish_name_length);
					}
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("Package", "getBytes english name as GB2312 error!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.v("Package", "getBytes chinese name as GB2312 error!");
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.v("Package", "getBytes exception!");
			}
			break;
			
		case Package.Set_Option :
			if (TCPClient.getInstance().setting_activity != null) {
				bytestream.write(TCPClient.getInstance().setting_activity.option_id);
				bytestream.write(TCPClient.getInstance().setting_activity.opr);
			}
			
		default:
			break;
		}
		
		this.length = (short) (bytestream.size() + 1);

		byte[] pdata = bytestream.toByteArray();
		pdata[0] = (byte) (this.length >> 0);
		pdata[1] = (byte) (this.length >> 8);
		for (int i = 0; i < pdata.length; ++i) {	
			checksum += pdata[i];
		}
		bytestream.reset();
		try {
			bytestream.write(pdata);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v("Package", "write bytes error!");
		}
		bytestream.write(checksum);
		
		return bytestream;
	}
	
	public int img_frame_index = 0;
	public int sound_frame_index = 0;
	public byte[] img_data;
	public byte[] sound_data;
	public boolean get_img_pkg(ByteArrayOutputStream tmp) {
		tmp.reset();
		int total = img_data.length / 1024;
		for (; img_frame_index <= total;) {
			// fill head
			byte[] head = new byte[14];
			try {
				tmp.write(head);
				tmp.write(108);
				putShort(tmp, dish.dishid);
				tmp.write(total+1);
				tmp.write((byte)(img_frame_index+1));
				Log.v("Package", "get_img_pkg write header ok");
				
				if (img_frame_index != total) {
					tmp.write(img_data, img_frame_index*1024, 1024);
				} else {
					tmp.write(img_data, img_frame_index*1024, img_data.length - total*1024);
				}
				
				put_length_checksum(tmp);
				
				++img_frame_index;
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Package", "get_img_pkg exception");
			}
		}
		return false;
	}
	
	public boolean get_sound_pkg(ByteArrayOutputStream tmp) {
		tmp.reset();
		int total = sound_data.length / 1024;
		for (; sound_frame_index <= total;) {
			// fill head
			byte[] head = new byte[14];
			try {
				tmp.write(head);
				tmp.write(109);
				putShort(tmp, dish.dishid);
				tmp.write(total+1);
				tmp.write((byte)(sound_frame_index+1));
				Log.v("Package", "get_sound_pkg write header ok");
				
				if (sound_frame_index != total) {
					tmp.write(sound_data, sound_frame_index*1024, 1024);
				} else {
					tmp.write(sound_data, sound_frame_index*1024, sound_data.length - total*1024);
				}
				
				put_length_checksum(tmp);
				
				++sound_frame_index;
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Package", "get_sound_pkg exception");
			}
		}
		return false;
	}
	
	public void prepare_img_sounddata() {
		AssetFileDescriptor fd = null;
		AssetFileDescriptor fd_sound = null;
		FileInputStream inStream = null;
		if (this.cmdtype == Package.Send_Dish) {
			if (dish.isBuiltIn) {
				fd = TCPClient.getInstance().dish_activity.getResources().openRawResourceFd(dish.img_tiny);
				Log.i("Package", "dish.img_tiny = " + dish.img_tiny);
				//fd_sound = TCPClient.getInstance().dish_activity.getResources().openRawResourceFd(dish.sound);
				Log.i("Package", "dish.sound = " + dish.sound);
			} else {
				//fd = TCPClient.getInstance().makedish_activity.getResources().openRawResourceFd(dish.img_tiny);
				try {
					if (dish.img_tiny_path == null) {
						fd = TCPClient.getInstance().makedish_activity.getResources().openRawResourceFd(R.raw.tudousi_tiny);
					}
					else {
						inStream = new FileInputStream(dish.img_tiny_path);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					fd = TCPClient.getInstance().makedish_activity.getResources().openRawResourceFd(R.raw.tudousi_tiny);
				}
				//fd_sound = TCPClient.getInstance().makedish_activity.getResources().openRawResourceFd(dish.sound);
			}
		} else if (this.cmdtype == Package.Update_Favorite) {
			fd = TCPClient.getInstance().buildin_activity.getResources().openRawResourceFd(dish.img_tiny);
			//fd_sound = TCPClient.getInstance().buildin_activity.getResources().openRawResourceFd(dish.sound);
		}
		
		if (inStream == null) {
			try {
				inStream = fd.createInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (dish.img_tiny != null) {
			try {
				//FileInputStream inStream = fd.createInputStream();
				img_data = new byte[inStream.available()];
				inStream.read(img_data);
				Log.i("Package", "img size = " + img_data.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Package", "prepare_img_sounddata exception");
			}
		}
		
		if (dish.sound != null) {
			try {
				FileInputStream inStream_sound = fd_sound.createInputStream();
				sound_data = new byte[inStream_sound.available()];
				inStream.read(sound_data);
				Log.i("Package", "sound size = " + sound_data.length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Package", "prepare_img_sounddata exception");
			}
		}
	}
	public ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
	
	public short length = 52;
	public byte version = 7;
	public short someid = 0;
	public byte etype = 0;
	public int deviceid = 257;
	
	public int reqid = 0;
	
	public byte cmdtype = 0;
	
	public Dish dish;
	
	public byte checksum = 0;
	
	public void fillhead(ByteArrayOutputStream bytestream) {
		// add head
		byte[] tmp = new byte[4];
		putShort(tmp, this.length);
//		bytestream.write(tmp, 0, 2);
				
		bytestream.write(version);
				
		putShort(tmp, someid);
//		bytestream.write(tmp, 3, 2);
				
		bytestream.write(etype);
//				
		putInt(tmp, deviceid);
//		bytestream.write(tmp, 6, 4);
//				
		putInt(tmp, reqid);
//		bytestream.write(tmp, 10, 4);
//				
		bytestream.write(cmdtype);
	}
	
	public void putShort(ByteArrayOutputStream bytestream, short s) {  
		byte b[] = new byte[2];
		b[0] = (byte) (s >> 0);
		b[1] = (byte) (s >> 8);
		bytestream.write(b[0]);
		bytestream.write(b[1]);
	}    
	
	public void put_length_checksum(ByteArrayOutputStream baos) {
		short length = (short) (baos.size() + 1);

		byte[] pdata = baos.toByteArray();
		pdata[0] = (byte) (length >> 0);
		pdata[1] = (byte) (length >> 8);
		
		byte checksum = 0;
		for (int i = 0; i < pdata.length; ++i) {	
			checksum += pdata[i];
		}
		baos.reset();
		try {
			baos.write(pdata);
			Log.i("Package", "write image data = " + pdata.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v("Package", "write bytes error!");
		}
		baos.write(checksum);
	}
	
	public void putShort(byte b[], short s) {  
		b[0] = (byte) (s >> 0);
		b[1] = (byte) (s >> 8);
		
		this.bytestream.write(b[0]);
		this.bytestream.write(b[1]);
	} 
	
	public void putInt(byte[] b, int x) {       
		b[0] = (byte) (x >> 0);
		b[1] = (byte) (x >> 8); 
		b[2] = (byte) (x >> 16);
		b[3] = (byte) (x >> 24);
		
		this.bytestream.write(b[0]);
		this.bytestream.write(b[1]);
		this.bytestream.write(b[2]);
		this.bytestream.write(b[3]);
	}
}
