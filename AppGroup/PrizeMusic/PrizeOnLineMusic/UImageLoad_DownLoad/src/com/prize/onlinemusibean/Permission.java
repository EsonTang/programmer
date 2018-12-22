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

package com.prize.onlinemusibean;

import java.io.Serializable;
import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 歌曲权限信息 //是否提供服务: 0:正常, 1:不提供服务, 2:需要VIP
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class Permission implements Parcelable {
	/** 用一句话描述这个变量表示什么 */
	/** 当前用户可听的最高品质： 前端根据该字段判断是否显示“HQ” 元素类型为string */
	public String[] quality;
	/**
	 * 需要购买VIP的品质列表: 前端根据当前试听品质全局开关与该字段做比对， 若全局开关对应的品质在此列表中，则显示“VIP” 元素类型为string
	 */
	public String[] need_vip;
	/** 是否可用： (低品质不可试听 && VIP不免费试听) => SDK端视为下架 */
	public boolean available;

	@Override
	public int describeContents() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int flags) {
		arg0.writeStringArray(quality);
		arg0.writeStringArray(need_vip);
		arg0.writeByte((byte) (available ? 1 : 0));
	}

	public static final Parcelable.Creator<Permission> CREATOR = new Parcelable.Creator<Permission>() {
		public Permission createFromParcel(Parcel arg0) {
			Permission p = new Permission(arg0);
			p.quality = arg0.readStringArray();
			p.need_vip = arg0.readStringArray();
			p.available = arg0.readByte() != 0;
			return p;
		}

		public Permission[] newArray(int size) {
			return new Permission[size];
		}
	};

	public Permission(Parcel in) {
	}

	public Permission() {
	}

	
	@Override
	public String toString() {
		return "Permission [quality=" + Arrays.toString(quality)
				+ ", need_vip=" + Arrays.toString(need_vip) + ", available="
				+ available + "]";
	}

}
