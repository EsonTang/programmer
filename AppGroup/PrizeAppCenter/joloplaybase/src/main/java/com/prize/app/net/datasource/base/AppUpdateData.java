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

import com.prize.app.net.AbstractNetData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：应用更新实体类
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppUpdateData extends AbstractNetData implements Serializable {
	/**
	 * serialVersionUID:TODO（用一句话描述这个变量表示什么）
	 *
	 * @since 1.0.0
	 */
	
	private static final long serialVersionUID = -8016981216304294204L;
	// private static final long serialVersionUID = 1L;
	public List<AppsItemBean> apps = new ArrayList<AppsItemBean>();
	public AppUpdateData() {
	}

//	private AppUpdateData(Parcel in) {
//		readFromParcel(in);
//
//	}

	// public List<AppsItemBean> getApps() {
	// return apps;
	// }
	//
	// public void setApps(ArrayList<AppsItemBean> apps) {
	// this.apps = apps;
	// }

//	@Override
//	public int describeContents() {
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel arg0, int flags) {
////		arg0.writeInt(code);
////		arg0.writeString(msg);
//		arg0.writeList(apps);
//
//	}
//
//	public void readFromParcel(Parcel _reply) {
////		code = _reply.readInt();
////		msg = _reply.readString();
//		apps = _reply.readArrayList(AppsItemBean.class.getClassLoader());
//
//	}
//
//	public static final Parcelable.Creator<AppUpdateData> CREATOR = new Parcelable.Creator<AppUpdateData>() {
//		public AppUpdateData createFromParcel(Parcel in) {
//			return new AppUpdateData(in);
//
//		}
//
//		public AppUpdateData[] newArray(int size) {
//			return new AppUpdateData[size];
//		}
//	};

}
