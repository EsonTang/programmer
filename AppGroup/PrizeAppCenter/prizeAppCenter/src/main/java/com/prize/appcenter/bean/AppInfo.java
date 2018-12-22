/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.appcenter.bean;

/** 
 * @desc 系统安装app的信息
 * @author huangchangguo
 * @version 版本1.7
 * @Date 2016.5.20
 *
 */
import android.graphics.drawable.Drawable;

public class AppInfo {

	public String mPackageName;
	public Drawable mIcon;
	public String mLabel;
//	public boolean isSdcardApp; // 是否安装在SD卡，是否是外部程序
	public long mSize;
//	public boolean isSystemApp; // 是否是系统程序
	public int id = 0;
	public int versionCode = 0; // 是否是系统程序
	public boolean isUninstalling = false; // 不在卸载状态
}
