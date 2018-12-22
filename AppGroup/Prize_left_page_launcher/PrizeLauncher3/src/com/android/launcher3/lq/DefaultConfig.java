package com.android.launcher3.lq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.android.launcher3.Launcher;

import android.content.ComponentName;
import android.util.Xml;

public class DefaultConfig {

	public static String app_icon_size = "app_icon_size"; // 图标大小
	public static String custom_default_theme_path = "custom_default_theme_path";// 默认主题路径
	public static String default_home_page = "default_home_page";// 默认首页
	public static String default_workspace_pagecount_max = "default_workspace_pagecount_max";// 默认workspace最大页数
	public static String default_workspace_pagecount_min = "default_workspace_pagecount_min";// 默认最小页数
	public static String default_workspace_pagecounts = "default_workspace_pagecounts";// 默认页数
	public static String default_effect_id = "default_effect_id";// 默认特效
	public static String default_theme_id = "default_theme_id";// 默认特效
	public static String disable_move_wallpaper = "disable_move_wallpaper";// 默认是否滑动壁纸
	public static String disable_x_effect = "disable_x_effect";// 去除此特效
	public static String fast_scroll_page_velocity = "fast_scroll_page_velocity";// 滑动速度优化
	public static String hotseat_height = "hotseat_height";// 底盘dock栏高度
	public static String hotseat_hide_title = "hotseat_hide_title";// 是否隐藏dock栏图标标题
	public static String icon_title_font = "icon_title_font";// 默认图标标题大小
	public static String default_bulit_in_theme_name = "default_bulit_in_theme_name";// 默认图标标题大小
	public static String default_bulit_in_path = "default_bulit_in_path";
	public static String start_activity_duration = "start_activity_duration";
	public static String unlock_duration = "unlock_duration";
	public static String isUnlock = "isUnlock";
	public static String default_workspace_name_koobee = "default_workspace_name_koobee";
	public static String overlay_icon_koosai = "overlay_icon_koosai";
	public static String overlay_icon_koobee = "overlay_icon_koobee";
	public static String default_workspace_name_koosai = "default_workspace_name_koosai";
	public static String isSupportClone = "isSupportClone";
	public static String isSupportUpgrade = "isSupportUpgrade";
	public static String text_color = "text_color";
	public static String easy_launcher = "easy_launcher";
	public static String start_model = "start_model";
	public static String desk_tick = "desk_tick";
	public static String left_screen = "left_screen";
	public static String install_pos_page = "install_pos_page";
	public static String default_page_count = "default_page_count";
	public static String channel = "channel";
	public static String open_folder_duration = "open_folder_duration";
	public static String enter_edit_duration = "enter_edit_duration";
	public static String uninstall_duration = "uninstall_duration";
	public static String drag_duration = "drag_duration";
	public static String drop_duration = "drop_duration";
	public static String revert_duration = "drop_duration";
	public static String show_edit_list_duration = "show_edit_list_duration";
	public static String default_xml = "default_xml";
	
	
	public static String filter_apps="filter_apps";
	public static String test="test";
	public static String filter_widgets="filter_widgets";

	public static String is_koobee = "is_koobee";
	public static String long_click_enter_edit = "long_click_enter_edit";

	public static String test_config = "/storage/emulated/0/config/default_config/";//默认设置配置文件
	public static String default_config = "/system/media/config/default_config/";//默认设置配置文件
	
	
	
	public static String config_theme_wallpaper_path="";//默认主题和壁纸的配置路径
	public static String default_workspace_path="";//默认桌面布局配置路径
	public static String default_overlay_icon_path="";//默认桌面布局配置路径
	
	public static String default_config_theme_wallpaper_path="/system/media/config/";

	public static StringBuffer shortsPkg= new StringBuffer();
	public static HashMap themeNameList= new HashMap<String, String>();
	
	public static List<String> sOverIconpkgs = new ArrayList<>();
	public static List<String> sOverIconclss = new ArrayList<>();
	public static List<String> sOverIcons = new ArrayList<>();
	public static LinkedHashMap<String, String> findCustomConfig(String path) {
		String configPath = path;
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		LinkedHashMap<String, String> configs = new LinkedHashMap<>();
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("general_config")) {
						for (int i = 0; i < xpp.getAttributeCount(); i++) {
							String key = xpp.getAttributeName(i);
							String value = xpp.getAttributeValue(i);
							configs.put(key, value);
						}
					}else if (xpp.getName().equals("shortcut_pakcage")) {
//						shortsPkg.append(xpp.getAttributeValue(0));
//						shortsPkg.pu
					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return configs;
	}
	
	
	
	
	public static LinkedHashMap<String, String> findOverIcons(String path) {
		String configPath = path;
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		LinkedHashMap<String, String> configs = new LinkedHashMap<>();
		List<String> arrays = null;
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("string-array")) {
						if(xpp.getAttributeValue(0).equals("overlay_icon_package")) {
							arrays = sOverIconpkgs;
						}else if(xpp.getAttributeValue(0).equals("overlay_icon_class")) {
							arrays = sOverIconclss;
						}else if(xpp.getAttributeValue(0).equals("overlay_icon_image")) {
							arrays = sOverIcons;
						}
						
					}else if (xpp.getName().equals("item")) {
						xpp.next();
						if(Launcher.over_icon_news) {
						
						String item = xpp.getText();
						String[] items = item.split(";");
						sOverIconpkgs.add(items[0]);
						sOverIconclss.add(items[1]);
						sOverIcons.add(items[2]);
						}else {
							arrays.add(xpp.getText());
						}
						
						
						
					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return configs;
	}
	
	
	public static LinkedHashMap<ComponentName,String > findDefaultXml(String path) {
		String configPath = path;
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		LinkedHashMap<ComponentName,String> configs = new LinkedHashMap<>();
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:

					if(isContainFormatName(xpp)) {
					if (xpp.getName().equals("favorite")) {
							for (int i = 0; i < xpp.getAttributeCount(); i++) {
								String key=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "name");
								String className=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "className");
								String pageName=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "packageName");
								configs.put(new ComponentName(pageName, className),key);
							}
						}
					}
					if(xpp.getName().equals("folder")) {

//						for (int i = 0; i < xpp.getAttributeCount(); i++) {
							String key=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "name");
							String x=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "x");
							String y=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "y");
							String spanX="1";
							String spanY="1";
							String screen=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "screen");
							String first = spanX+screen+spanY+x+y;
							configs.put(new ComponentName(first, first),key);
//						}
					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return configs;
	}
	
	
	public static boolean findDefault(String path,ComponentName com) {
		String configPath = path;
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return false;
		}
		InputStream is = null;
		LinkedHashMap<ComponentName,String> configs = new LinkedHashMap<>();
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("favorite")) {
								String cls=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "className").toLowerCase();
								String pageName=xpp.getAttributeValue(xpp.getAttributeNamespace(0), "packageName").toLowerCase();
								ComponentName cp = new ComponentName(pageName,cls);
								if(cp.equals(com)) {
									return true;
								}
					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return false;
	}
	
	private static boolean isContainFormatName(XmlPullParser xpp) {
		for (int i = 0; i < xpp.getAttributeCount(); i++) {
			String key = xpp.getAttributeName(i);
			if(key.equals("name")) {
				return true;
			}
		}
		return false;
	}
	
	
	
	public static  String	findCurrentId(String path,String themePath) {
		String configPath = path + "config.xml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		String name = null;
		String id = null;
		String folderIconLayer=null;
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("item-info")) {
					}  if (xpp.getName().equals("id")) {
						eventType = xpp.next();
						id = xpp.getText();
						folderIconLayer="9";
						
					}else if (xpp.getName().equals("folder_layer")) {
						eventType = xpp.next();
						folderIconLayer = xpp.getText();
					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("item-info")) {
						if(id !=null&& folderIconLayer !=null)
						if(themePath.contains(id)) {
							return folderIconLayer;
						}
					}
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static HashMap<String, String> getNameFromXml(String path) {
		String configPath = path + "config.xml";
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			return null;
		}
		InputStream is = null;
		String name = null;
		String id = null;
		String folderIconLayer = null;
		try {
			is = new FileInputStream(configFile);
			XmlPullParser xpp = Xml.newPullParser();
			xpp.setInput(is, "UTF-8");
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				// 判断当前事件是否为文档开始事件
				case XmlPullParser.START_DOCUMENT:
					break;
				// 判断当前事件是否为标签元素开始事件
				case XmlPullParser.START_TAG:
					if (xpp.getName().equals("item-info")) {
						name = null;
						id = null;
					}

					else if (xpp.getName().equals("name")) {
						eventType = xpp.next();
						name = xpp.getText();
					} else if (xpp.getName().equals("id")) {
						eventType = xpp.next();
						id = xpp.getText();

					}
					break;
				// 判断当前事件是否为标签元素结束事件
				case XmlPullParser.END_TAG:
					if (xpp.getName().equals("item-info")) {
						if (id != null && name != null)
							themeNameList.put(id, name);
					}
					break;
				}
				// 进入下一个元素并触发相应事件
				eventType = xpp.next();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return themeNameList;
	}
}
