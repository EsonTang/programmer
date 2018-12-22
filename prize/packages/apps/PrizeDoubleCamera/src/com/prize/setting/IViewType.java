package com.prize.setting;

/**
 * 
 **
 * 类描述：设置界面 统一类别和内容的文字显示
 * @author 作者 wanzhijuan 
 * @version 版本
 */
public interface IViewType {
	/**
	 * 类别：照片、拍照、高级、视频类型
	 */
	int VIEW_TYPE_TITLE = 0;
	/**
	 * 内容，数据为ListPreference
	 */
	int VIEW_TYPE_CONTENT = 1; 
	
	/**
	 * 
	 * 方法描述：返回类别
	 * @param 参数名 说明
	 * @return 返回类型 说明 0 类别：照片、拍照、高级、视频类型   1内容，数据为ListPreference
	 * @see 类名/完整类名/完整类名#方法名
	 */
	int getViewType();
	/**
	 * 
	 * 方法描述：文字描述
	 * @param 参数名 说明
	 * @return 返回类型 说明文字描述
	 * @see 类名/完整类名/完整类名#方法名
	 */
	String getTitle();
}
