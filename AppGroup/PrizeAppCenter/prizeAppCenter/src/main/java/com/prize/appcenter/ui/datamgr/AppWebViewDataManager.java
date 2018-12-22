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

import android.os.Message;

import com.prize.app.net.NetSourceListener;
import com.prize.appcenter.bean.AppBrefData;

/**
 * 类描述：webView嵌入网面时需要访问的APP
 * @author fanjunchen
 */
public class AppWebViewDataManager extends AbstractDataManager {

	private AppWebViewNetSource appWebViewNetSource;

	public static final int GET_SUCCESS = 0;
	public static final int GET_FAILURE = 6;

	public AppWebViewDataManager(DataManagerCallBack callback) {
		super(callback);
	}

	/**
	 * 应用详情信息监听器
	 */
	private DataManagerListener<AppBrefData> fetchSingleAppListener = new DataManagerListener<AppBrefData>() {
		@Override
		protected Message onSuccess(int what, AppBrefData data) {
			return super.onSuccess(GET_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			if (what == NetSourceListener.WHAT_NETERR) {
				return super.onFailed(what);
			}
			return super.onFailed(GET_FAILURE);
		}
	};


	/**
	 * 方法描述：执行网络请求，获取详情页面参数
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void getNetData(String appId, String userId,String requestType) {
		if (appWebViewNetSource == null) {
			appWebViewNetSource = new AppWebViewNetSource();
			appWebViewNetSource.setListener(fetchSingleAppListener);
		}
		appWebViewNetSource.setData(appId, userId);
		appWebViewNetSource.doRequest(requestType);
	}

	public void setNullListener() {
		if (appWebViewNetSource != null) {
			appWebViewNetSource.setListener(null);
			fetchSingleAppListener = null;
		}
	}

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
	}
}
