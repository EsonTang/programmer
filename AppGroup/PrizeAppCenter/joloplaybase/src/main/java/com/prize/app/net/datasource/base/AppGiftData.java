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
 * 类描述：应用评论数据实体类
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppGiftData extends AbstractNetData implements Serializable {
	public int pageCount;
	public ArrayList<AppComment> comments = new ArrayList<AppComment>();
	public int pageIndex;
	public int pageSize;
	public float rating;
	public int pageItemCount;

	public static class AppComment implements Serializable {
		public int id;
		public String nickName;
		public int appId;
		public String versionName;
		public String mobile;
		public int starLevel;
		public String content;
		public String createTime;
		public int status;
		public long accountId;
		public String avatarUrl;

		@Override
		public String toString() {
			return "AppComment [id=" + id + ", nickName=" + nickName
					+ ", appId=" + appId + ", versionName=" + versionName
					+ ", mobile=" + mobile + ", starLevel=" + starLevel
					+ ", content=" + content + ", createTime=" + createTime
					+ ", status=" + status + "]";
		}
	}

	@Override
	public String toString() {
		return "AppCommentData [pageCount=" + pageCount + ", pageIndex="
				+ pageIndex + ", pageSize=" + pageSize + ", pageItemCount="
				+ pageItemCount + "]";
	}

}
