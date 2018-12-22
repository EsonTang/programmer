
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
import com.prize.app.net.datasource.base.AppsKeyInstallingPageListData;
import com.prize.app.net.datasource.base.MoreListData;

/**
 * 类描述：app列表数据请求管理类
 * @author huanglingjun
 * @version 版本
 */
public class AppListDataManager extends AbstractDataManager {
//	private AppCollectionListNetSource appCollectionListNetSource;
//	private AppInstalledNetSource appInstalledNetSource;
	
	private AppKeyInstallingPagesNetSource mAppKeyInstallingnetSource;

//	private CoverNetSource mCoverNetSource;
//	private AppUploadInstalledNetSource uploadInstalledNetSource;
//	private AppUpdateNetSource appUpdateNetSource;
//	private AppInstallCancelNetSource appInstallCancelNetSource;
	private AppMoreListNetSource appMoreListNetSource;
//	private AppUucNetSource appUucNetSource;
	
//	public static final int COLLECTION_SUCCESS = 0;
//	public static final int COLLECTION_FAILURE = 1;
//	public static final int INSTALLED_SUCCESS = 2;
//	public static final int INSTALLED_FAILURE = 3;
//	public static final int USER_INSTALLED_SUCCESS = 4;
//	public static final int USER_INSTALLED_FAILURE = 5;
	public static final int UPDATE_SUCCESS = 6;
	public static final int UPDATE_FAILURE = 7;
//	public static final int INSTALL_DELETE_SUCCESS = 8;
	public static final int INSTALL_DELETE_FAILURE = 9;
	public static final int KEY_INSALL_SUCCESS = INSTALL_DELETE_FAILURE+1;
	public static final int KEY_INSALL_FAILURE = KEY_INSALL_SUCCESS+1;
	public static final int MORE_SUCCESS = KEY_INSALL_FAILURE+1;
	public static final int MORE_FAILURE = MORE_SUCCESS+1;
//	public static final int UUC_SUCCESS = MORE_FAILURE+1;
//	public static final int UUC_FAILURE = UUC_SUCCESS+1;
	

//	public static final int COVER_SUCCESS = KEY_INSALL_FAILURE+1;
//	public static final int COVER_FAILURE = COVER_SUCCESS+1;
	
	public AppListDataManager(DataManagerCallBack callback) {
		super(callback);
	}
	


	
	/**
	 * 首页卡片内容扩展更多页面信息
	 */
	private DataManagerListener<MoreListData> moreListener = new DataManagerListener<MoreListData>() {
		@Override
		protected Message onSuccess(int what, MoreListData data) {
			
			return super.onSuccess(MORE_SUCCESS,data);
		}

		@Override
		protected Message onFailed(int what) {
			if (what == NetSourceListener.WHAT_NETERR) {
				return super.onFailed(what);
			}
			return super.onFailed(MORE_FAILURE);
		}
	};
//
//	/**
//	 * 首页卡片内容扩展更多页面信息
//	 */
//	private DataManagerListener<MoreListData> uucListener = new DataManagerListener<MoreListData>() {
//		@Override
//		protected Message onSuccess(int what, MoreListData data) {
//
//			return super.onSuccess(UUC_SUCCESS,data);
//		}
//
//		@Override
//		protected Message onFailed(int what) {
//			if (what == NetSourceListener.WHAT_NETERR) {
//				return super.onFailed(what);
//			}
//			return super.onFailed(UUC_FAILURE);
//		}
//	};
	
	
	/**
	 * 一键安装 信息监听器
	 */
	private DataManagerListener<AppsKeyInstallingPageListData> mKeyInstalledListener = new DataManagerListener<AppsKeyInstallingPageListData>() {
		@Override
		protected Message onSuccess(int what, AppsKeyInstallingPageListData data) {

			return super.onSuccess(KEY_INSALL_SUCCESS,data);
		}

		@Override
		protected Message onFailed(int what) {
			if (what == NetSourceListener.WHAT_NETERR) {
				return super.onFailed(what);
			}
			return super.onFailed(KEY_INSALL_FAILURE);
		}
	};
	
	
//	/**
//	 * 广告页 信息监听器
//	 */
//	private DataManagerListener<CoverData> mCoDataManagerListener = new DataManagerListener<CoverData>() {
//		@Override
//		protected Message onSuccess(int what, CoverData data) {
//
//			return super.onSuccess(COVER_SUCCESS,data);
//		}
//
//		@Override
//		protected Message onFailed(int what) {
//			if (what == NetSourceListener.WHAT_NETERR) {
////				return super.onFailed(what);
//			}
//			return super.onFailed(COVER_FAILURE);
//		}
//	};
	
	
	
	
//	/**
//	 * 安装记录列表 信息监听器
//	 */
//	private DataManagerListener<AppsInstalledListData> installedCancelListener = new DataManagerListener<AppsInstalledListData>() {
//		@Override
//		protected Message onSuccess(int what, AppsInstalledListData data) {
//
//			return super.onSuccess(INSTALL_DELETE_SUCCESS,data);
//		}
//
//		@Override
//		protected Message onFailed(int what) {
//			if (what == NetSourceListener.WHAT_NETERR) {
//				return super.onFailed(what);
//			}
//			return super.onFailed(INSTALL_DELETE_FAILURE);
//		}
//	};
	
//	/**
//	 * 应用更新列表 信息监听器
//	 */
//	private DataManagerListener<AppUpdateData> updateListener = new DataManagerListener<AppUpdateData>() {
//		@Override
//		protected Message onSuccess(int what, AppUpdateData data) {
//
//			return super.onSuccess(UPDATE_SUCCESS,data);
//		}
//
//		@Override
//		protected Message onFailed(int what) {
//			if (what == NetSourceListener.WHAT_NETERR) {
//				return super.onFailed(what);
//			}
//			return super.onFailed(UPDATE_FAILURE);
//		}
//	};
//
//	/**
//	 * 方法描述：获得收藏列表数据请求
//	 */
//	public void doCollectionPost(String userId, int pageIndex, int pageSize,String requestType){
//		if (appCollectionListNetSource == null ) {
//			appCollectionListNetSource = new AppCollectionListNetSource();
//			appCollectionListNetSource.setListener(collectionListListener);
//		}
//		appCollectionListNetSource.setData(userId, pageIndex, pageSize);
//		appCollectionListNetSource.doRequest(requestType);
//	}
	
//	/**
//	 * 方法描述：获得安装记录列表数据请求
//	 */
//	public void doInstalledPost(String userId, int pageIndex, int pageSize,String requestType){
//		if (appInstalledNetSource == null) {
//			appInstalledNetSource = new AppInstalledNetSource();
//			appInstalledNetSource.setListener(installedListListener);
//		}
//		appInstalledNetSource.setData(userId, pageIndex, pageSize);
//		appInstalledNetSource.doRequest(requestType);
//	}
//
//	/**
//	 * 方法描述：获得UUC竞价列表数据
//	 */
//	public void doUucPost(String requestType){
//		if (appUucNetSource == null) {
//			appUucNetSource = new AppUucNetSource();
//			appUucNetSource.setListener(uucListener);
//		}
//		appUucNetSource.doRequest(requestType);
//	}
//
//	/**
//	 * 方法描述：上传安装记录数据请求
//	 */
//	public void doUserInstalledPost(String userId,String appId,String requestType){
//		if (uploadInstalledNetSource == null) {
//			uploadInstalledNetSource = new AppUploadInstalledNetSource();
//			uploadInstalledNetSource.setListener(uploadInstalledListener);
//		}
//		uploadInstalledNetSource.setData(userId, appId);
//		uploadInstalledNetSource.doRequest(requestType);
//	}
	
	/**
	 * 方法描述：一键安装记录数据请求
	 */
	public void doKeyInstallingNetSource(String requestType,String type) {
		if (mAppKeyInstallingnetSource  == null) {
			mAppKeyInstallingnetSource = new AppKeyInstallingPagesNetSource();
			mAppKeyInstallingnetSource.setListener(mKeyInstalledListener);
			mAppKeyInstallingnetSource.setType(type);
		}
		mAppKeyInstallingnetSource.setAppType("koobee");
		mAppKeyInstallingnetSource.doRequest(requestType);
	}
	
	
//	/**
//	 * 方法描述：广告页记录数据请求
//	 */
//	public void doGetCoverNetSource(String requestType) {
//		if (mCoverNetSource  == null) {
//			mCoverNetSource = new CoverNetSource();
//			mCoverNetSource.setListener(mCoDataManagerListener);
//		}
//		mCoverNetSource.doRequest( requestType);
//	}
	
//	/**
//	 * 方法描述：请求应用更新数据
//	 */
//	public void doUpdatePost(String packages,String requestType){
//		if (appUpdateNetSource == null) {
//			appUpdateNetSource = new AppUpdateNetSource();
//			appUpdateNetSource.setListener(updateListener);
//		}
//		appUpdateNetSource.setData(packages);
//		appUpdateNetSource.doRequest(requestType);
//	}
	
//	/**
//	 * 方法描述：请求删除安装记录
//	 */
//	public void doInstallCancelPost(String userId,String appIds,String requestType){
//		if (appInstallCancelNetSource == null) {
//			appInstallCancelNetSource = new AppInstallCancelNetSource();
//			appInstallCancelNetSource.setListener(installedCancelListener);
//		}
//		appInstallCancelNetSource.setData(userId, appIds);
//		appInstallCancelNetSource.doRequest(requestType);
//	}
//
	/**
	 * 方法描述：请求删除安装记录
	 */
	public void doMoreListPost(String cardId,int pageIndex,int pageSize,String requestType){
		if (appMoreListNetSource == null) {
			appMoreListNetSource = new AppMoreListNetSource();
			appMoreListNetSource.setListener(moreListener);
		}
		appMoreListNetSource.setData(cardId,pageIndex,pageSize);
		appMoreListNetSource.doRequest(requestType);
	}
	
	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
	}
	
	public void setNullListener() {
//		if (appCollectionListNetSource != null) {
//			appCollectionListNetSource.setListener(null);
//			collectionListListener = null;
//		}
//		if (appInstalledNetSource != null) {
//			appInstalledNetSource.setListener(null);
//			installedListListener = null;
//		}
//		if (uploadInstalledNetSource != null) {
//			uploadInstalledNetSource.setListener(null);
//			uploadInstalledListener = null;
//		}
//		if (appUpdateNetSource != null){
//			appUpdateNetSource.setListener(null);
//			updateListener = null;
//		}
//		if (appInstallCancelNetSource != null) {
//			appInstallCancelNetSource.setListener(null);
//		}
		
		if (appMoreListNetSource != null) {
			appMoreListNetSource.setListener(null);
		}
	}
}

