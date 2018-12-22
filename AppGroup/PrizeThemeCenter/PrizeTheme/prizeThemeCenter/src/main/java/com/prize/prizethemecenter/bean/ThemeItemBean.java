package com.prize.prizethemecenter.bean;

import java.io.Serializable;

/**
 * 单个主题bean
 * @author pengy
 *
 */
public class ThemeItemBean implements Serializable{

	private static final long serialVersionUID = 1L;
	public String id;
	public String name;
	public String size;
	/** 标题 */
	public String title;
	public String ad_pictrue;
	/**价格 */
	public String price;
	public String package_uri;
	public int status;
	public String source;
	/**标签  爱情 浪漫 可爱*/
	public String tag;
	/**分类名称*/
	public String category_name;
	/**壁紙图片URL*/
	public String wallpaper_pic;
	/**壁紙图片类型  1.单屏 2.双屏*/
	public String wallpaper_type;
    /**是否新品 0为假，1为新品*/
	public String is_latest;
	/**是否更新 0为假，1为更新*/
	public String is_update;
	/**是否收藏 0为假，1为更新*/
	public String sort;
	/**服务上对应的MD5值*/
	public String md5_val;

//	/** 展示图片*/
//	public String[] screenshot;
//	public String intro;
//
//	public int download_count;
//	public String updateTime;
//
//	public int hot;
//	public String addtime;
//	/**主题，字体，壁纸各不同*/
//	public int theme_status;
//	public int wallpaper_status;
//	public int font_status;

//	public int source_type;
//	public boolean is_latest;


}
