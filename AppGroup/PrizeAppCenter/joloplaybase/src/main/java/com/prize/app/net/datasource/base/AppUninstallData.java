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

package com.prize.app.net.datasource.base;

import java.io.Serializable;
import java.util.ArrayList;

import com.prize.app.net.AbstractNetData;

/**
 * 类描述：应用卸载实体类
 * 
 * @author huangchangguo
 * @version 版本1.7
 */
public class AppUninstallData extends AbstractNetData implements Serializable {
	private static final long serialVersionUID = 1L;
	public ArrayList<AppsItemBean> apps = new ArrayList<AppsItemBean>();

	public ArrayList<AppsItemBean> getApps() {
		return apps;
	}

	public void setApps(ArrayList<AppsItemBean> apps) {
		this.apps = apps;
	}

	@Override
	public String toString() {
		return "AppUninstallData [apps=" + apps + "]";
	}

}
