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

/**
 * 类描述：app详情页面数据实体类
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppDetailData extends AbstractNetData implements Serializable {
	public DetailApp app;
	public String isCollection;
//	public ArrayList<AppsItemBean> sameCatApps = new ArrayList<AppsItemBean>();
//	public ArrayList<AppsItemBean> sameDevApps = new ArrayList<AppsItemBean>();
	public ArrayList<GiftsItem> gifts = new ArrayList<GiftsItem>();
	private static final long serialVersionUID = 1L;
	public static class GiftsItem implements Serializable {
		/** 用一句话描述这个变量表示什么 */
		private static final long serialVersionUID = -1075301295994529621L;
		public int id;
		public String appId;
		public String title;
		public String content;
		public String startTime;
		public String endTime;
		public String createTime;
		public String usage;

		public int giftType;
		/***
		 * 0 - 初始； 1 - 进行中； 2 - 活动结束； 3 - 激活码领取完成；
		 */
		public int giftStatus;
		public String activationCode;

		@Override
		public String toString() {
			return "GiftsItem [id=" + id + ", appId=" + appId + ", title="
					+ title + ", content=" + content + ", startTime="
					+ startTime + ", endTime=" + endTime + ", createTime="
					+ createTime + ", usage=" + usage + ", giftType="
					+ giftType + ", giftStatus=" + giftStatus + "]";
		}

		/*
		 * "id": 2, "appId": 503, "title": "《时空猎人》下载有礼", "content": "下载即可xxx",
		 * "startTime": "2015-11-26 11:42:02", "endTime": "2015-11-30 11:42:07",
		 * "createTime": "2015-11-26 11:42:32", "usage": "下载并按照游戏即可获取礼包",
		 * "giftType": 0, "giftStatus": 1
		 */

	}


//	public static class Medias implements Serializable{
//		public MediasData data;
//		public String type;
//
//		public static class MediasData implements Serializable {
//			 public int appId;
//			 public int zuimeiaId;
//			 public String articleUrl;
//		     public String authorAvatarUrl;
//			 public String authorUsername;
//		     public String content;
//			 public String coverImage;
//			 public String digest;
//			 public String downloadUrlZuimei;
//			 public String iconImage;
//			 public String packageName;
//			 public String subTitle;
//			 public String title;
//			 public String recommandedDate;
//		}
//	}
}
