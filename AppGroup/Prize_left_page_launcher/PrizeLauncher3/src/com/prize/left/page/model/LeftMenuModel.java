package com.prize.left.page.model;

import org.xutils.x;
import org.xutils.common.Callback;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.launcher3.LauncherModel;
import com.prize.left.page.bean.AppInfoBean;
import com.prize.left.page.request.FolderRequest;
import com.prize.left.page.request.UpgradeRequest;
import com.prize.left.page.response.FolderResponse;
import com.prize.left.page.response.UpgradeResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DownloadModel;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
/***
 * 左边菜单对话框业务类
 * @author fanjunchen
 *
 */
public class LeftMenuModel extends BaseModel<UpgradeResponse> {

	private UpgradeRequest reqParam;
	
	private UpgradeResponse response;
	/**是否正在请求数据**/
	private boolean isRunning = false;
	/** 刷新的时间间隔 2小时 */
	private final long BETWEEN_TIME = 1000 * 60 * 60 * 2;
	
	public LeftMenuModel(Context ctx) {
		mCtx = ctx;
		reqParam = new UpgradeRequest();
	}
	/**
	 * 可以不设置, 会自动新建一个
	 * @param req
	 */
	public void setRequest(UpgradeRequest req) {
		reqParam = req;
	}
	/***
	 * 获取返回结果
	 * @return
	 */
	public AppInfoBean getAppInfoBean() {
		
		if (response != null && response.data != null 
				&& response.data.app != null ) 
			return response.data.app;
		
		return null;
	}
	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		/*if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			cancelObj = x.http().get(reqParam, httpCallback);
			
		}*/
	}
	/***
	 * 发送检测更新请求
	 */
	public void doCheckUpdate() {
		
		/*int resId = CommonUtils.getResourceId(mCtx, "string", "upgrade_resp");
		if (resId != -1) {
			newHttpCallback();
			httpCallback.onSuccess(mCtx.getString(resId));
			return;
		}*/
		
		if (!isRunning && (null == cancelObj || cancelObj.isCancelled())) {
			isRunning = true;
			newHttpCallback();
//			ClientInfo	clientInfo = ClientInfo.getInstance(mCtx);
//			PersonTable p = LauncherApplication.getInstance().getLoginPerson();
//			if(p!=null) {
//				clientInfo.setUserId(p.userId);
//			}
////			clientInfo.city = LauncherApplication.getInstance().getCityId();
//			clientInfo.setClientStartTime(System.currentTimeMillis());
//			clientInfo.setNetStatus(ClientInfo.networkType);
//			//
//			String headParams = CommonUtils.toGson(clientInfo);
//			headParams = XXTEAUtil.getParamsEncypt(headParams);
//			if (!TextUtils.isEmpty(headParams)) {
//				reqParam.addHeader("params", headParams);
//			}
			PreferencesUtils.addHeaderParam(reqParam, mCtx);
			reqParam.versionCode = mCtx.getApplicationInfo().versionCode;
			reqParam.packages = mCtx.getApplicationInfo().packageName + "#" + reqParam.versionCode;
			cancelObj = x.http().post(reqParam, httpCallback);
		}
	}
	/***
	 * 同步请求
	 * @return
	 */
	public UpgradeResponse doGetSync() {
		try {
			reqParam.addHeader("KOOBEE", "dido");
			String result = x.http().getSync(reqParam, String.class);
			response = CommonUtils.getObject(result, UpgradeResponse.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return response;
	}
	
	
	public void doCheckUpSync() {
		new Task().execute();
	}
	
	public void onPageEndMoving() {

		long ll = System.currentTimeMillis()
				- PreferencesUtils.getLong(mCtx,
						IConstants.KEY_REFRESH_UP_TIME);
		if (ll > BETWEEN_TIME) {
			doCheckUpSync();
		}
	}
	/***
	 * 请求网络导航并解析返回的数据
	 * 
	 * @author fanjunchen
	 * 
	 */
	class Task extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {
			

				boolean result = false;
			if (!isRunning && (null == cancelObj || cancelObj.isCancelled())) {
				isRunning = true;
				try {
					PreferencesUtils.addHeaderParam(reqParam, mCtx);
					reqParam.versionCode = mCtx.getApplicationInfo().versionCode;
					reqParam.packages = mCtx.getApplicationInfo().packageName
							+ "#" + reqParam.versionCode;
					String str = x.http().getSync(reqParam, String.class);
					PreferencesUtils.putLong(mCtx,
							IConstants.KEY_REFRESH_UP_TIME,
							System.currentTimeMillis());
					response = CommonUtils
							.getObject(str, UpgradeResponse.class);

					if (response != null) {
						if (response.code != 0) {
							result = false;
						} else {
							result = true;
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			
			if (result) {
				irs.onResponse(response);
			}
			isRunning = false;
			return result;
		
		}

		@Override
		protected void onPostExecute(Boolean result) {/*
			if (result) {
				onResponse(response);
				irs.onResponse(response);
			}
			isRunning = false;
		*/}
	}
	
	
	
	
	
	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			cancelObj = x.http().post(reqParam, httpCallback);
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(UpgradeResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
		}
	}
	
	
	private IResponse<UpgradeResponse> irs = new IResponse<UpgradeResponse>(){
		@Override
		public void onResponse(UpgradeResponse resp) {
			DownloadModel.getInstance(mCtx).onResponse(resp);
		}
	};
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					cancelObj = null;
					response = CommonUtils.getObject(result, UpgradeResponse.class);
					onResponse(response);
					isRunning = false;
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					response = new UpgradeResponse();
					response.code = 2;
					response.msg = ex.getMessage();
					onResponse(response);
					cancelObj = null;
					isRunning = false;
				}

				@Override
				public void onCancelled(CancelledException cex) {// 加载被取消
					isRunning = false;
				}

				@Override
				public void onFinished() { // 加载结束
					cancelObj = null;
					isRunning = false;
				}
			};
		}
	}
	
	@Override
	public void doBindImg() {
		// TODO Auto-generated method stub
	}
}
