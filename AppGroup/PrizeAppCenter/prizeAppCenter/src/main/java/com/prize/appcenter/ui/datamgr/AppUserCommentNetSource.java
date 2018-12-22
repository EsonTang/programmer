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

package com.prize.appcenter.ui.datamgr;

import com.google.gson.Gson;
import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.AppCommentData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：用户提交评价
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppUserCommentNetSource
		extends
		AppAbstractNetSource<AppCommentData, Map<String, String>, GetGameListResp> {

	private String appId;
	private String versionName;
	private float starLevel;
	private String content;
	private String mobile;
	private int userId;
	private String nickName;
	private String avatarUrl;

	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("appId", appId + "");
		map.put("versionName", versionName);
		map.put("starLevel", starLevel + "");
		map.put("content", content);
		map.put("mobile", mobile);
		map.put("userId", userId + "");
		map.put("nickName", nickName);
		map.put("avatarUrl", avatarUrl);
		return map;
	}

	public void setData(String appId, String versionName, float starLevel,
			String content, String mobile, int userId, String nickName,
			String avatarUrl) {
		this.appId = appId;
		this.versionName = versionName;
		this.starLevel = starLevel;
		this.content = content;
		this.mobile = mobile;
		this.userId = userId;
		this.nickName = nickName;
		this.avatarUrl = avatarUrl;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AppCommentData parseStrResp(String resp) {

		AppCommentData appCommentData = new Gson().fromJson(resp,
				AppCommentData.class);
		// TODO Auto-generated method stub
		return appCommentData;
	}

	@Override
	public String getUrl() {

		// TODO Auto-generated method stub
		return Constants.GIS_URL + "/comment/post";
	}

}
