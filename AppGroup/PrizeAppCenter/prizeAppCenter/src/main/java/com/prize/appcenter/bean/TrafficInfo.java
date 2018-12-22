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
 * 
 * @desc 预留系统安装app流量使用的信息
 * @author huangchangguo
 * @version 版本1.7
 * @Date 2016.5.20
 */
import android.graphics.drawable.Drawable;

public class TrafficInfo {

	public Drawable icon;
	public String label;
	public long recSize;
	public long sendSize;

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String a) {
		this.label = a;
	}

	public long getRecSize() {
		return recSize;
	}

	public void setRecSize(long recSize) {
		this.recSize = recSize;
	}

	public long getSendSize() {
		return sendSize;
	}

	public void setSendSize(long sendSize) {
		this.sendSize = sendSize;
	}

}
