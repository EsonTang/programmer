
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

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.prize.app.constants.Constants;
import com.prize.app.net.AppAbstractNetSource;
import com.prize.app.net.datasource.base.AppCommentData;
import com.prize.app.net.req.BaseResp;
import com.prize.app.net.req.GetGameListResp;

public class AppUserCollectionNetSource extends AppAbstractNetSource<AppCommentData, Map<String, String>, GetGameListResp> {

	private int appId;
	private String userId;
	@Override
	protected Map<String, String> getRequest() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("appId", appId+"");
		map.put("userId", userId);
		return map;
	}

	@Override
	protected Class<? extends BaseResp> getRespClass() {
		
		// TODO Auto-generated method stub
		return null;
	}

	public void setData(int appId,String userId){
		if (appId == 0 && TextUtils.isEmpty(userId)) return;
		this.appId = appId;
		this.userId = userId;
	}
	
	@Override
	protected AppCommentData parseStrResp(String resp) {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl() {
		
		// TODO Auto-generated method stub
		return Constants.GIS_URL+"/collection/post";
	}

}

