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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @desc 系统安装app的信息
 * @author huangchangguo
 * @version 版本1.7
 * @Date 2016.5.20
 *
 */
public class MessageBean implements Parcelable, Serializable {
	private static final long serialVersionUID = -6531879493960458625L;
	public String title;
	public String createTime;
	public String content;
	public String copyContent;
	public String pushTitle;
	public String pushContent;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int flags) {
		arg0.writeString(title);
		arg0.writeString(createTime);
		arg0.writeString(content);
		arg0.writeString(copyContent);
		arg0.writeString(pushTitle);
		arg0.writeString(pushContent);

	}

	public static final Parcelable.Creator<MessageBean> CREATOR = new Parcelable.Creator<MessageBean>() {
		public MessageBean createFromParcel(Parcel in) {
			return new MessageBean(in);
		}

		public MessageBean[] newArray(int size) {
			return new MessageBean[size];
		}
	};

	public MessageBean(Parcel arg0) {
		title = arg0.readString();
		createTime = arg0.readString();
		content = arg0.readString();
		copyContent = arg0.readString();
		pushTitle = arg0.readString();
		pushContent = arg0.readString();
	}
}
