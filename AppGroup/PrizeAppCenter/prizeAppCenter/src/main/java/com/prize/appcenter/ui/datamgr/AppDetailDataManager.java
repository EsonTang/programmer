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
import com.prize.app.net.datasource.base.AppCommentData;
import com.prize.app.net.datasource.base.AppDetailData;
import com.prize.app.net.datasource.base.AppsCollectionListData;
import com.prize.app.net.datasource.base.Giftdata;

/**
 * 类描述：app详情页面数据请求管理类
 * 
 * @author huanglingjun
 * @version 版本
 */
public class AppDetailDataManager extends AbstractDataManager {

	private AppDetailNetSource appDetailNetSource;
	private AppCommentNetSource appCommentNetSource;
	private AppUserCommentNetSource appUserCommentNetSource;
	private AppUserCollectionNetSource appUserCollectionNetSource;
	private GiftDataNetSource mGiftDataNetSource;
	private AppCancelCollectionNetSource appCancelCollectionNetSource;

	public static final int DETAIL_SUCCESS = 0;
	public static final int DETAIL_FAILURE = 6;
	public static final int COMMENT_SUCCESS = 1;
	public static final int USER_COMMENT_SUCCESS = 2;
	public static final int USER_COMMENT_FAILURE = 3;
	public static final int USER_COLLECTION_SUCCESS = 4;
	public static final int USER_COLLECTION_FAILURE = 5;
	public static final int CANCEL_COLLECTION_SUCCESS = 7;
	public static final int CANCEL_COLLECTION_FAILURE = 8;
	public static final int GIFT_SUCCESS = CANCEL_COLLECTION_FAILURE + 1;
	public static final int GIFT_FAILURE = GIFT_SUCCESS + 1;

	public AppDetailDataManager(DataManagerCallBack callback) {
		super(callback);
	}

	/**
	 * 应用详情信息监听器
	 */
	private DataManagerListener<AppDetailData> recommandListListener = new DataManagerListener<AppDetailData>() {
		@Override
		protected Message onSuccess(int what, AppDetailData data) {

			return super.onSuccess(DETAIL_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			if (what == NetSourceListener.WHAT_NETERR) {
				return super.onFailed(what);
			}
			return super.onFailed(DETAIL_FAILURE);
		}
	};

	/**
	 * 评价列表信息监听器
	 */
	private DataManagerListener<AppCommentData> commentListListener = new DataManagerListener<AppCommentData>() {
		@Override
		protected Message onSuccess(int what, AppCommentData data) {

			return super.onSuccess(COMMENT_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			if (what == NetSourceListener.WHAT_NETERR) {
				return super.onFailed(what);
			}
			return super.onFailed(NetSourceListener.WHAT_NETERR);
		}
	};

	/**
	 * 用户评价信息监听器
	 */
	private DataManagerListener<AppCommentData> userCommentListener = new DataManagerListener<AppCommentData>() {
		@Override
		protected Message onSuccess(int what, AppCommentData data) {
			return super.onSuccess(USER_COMMENT_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			return super.onFailed(USER_COMMENT_FAILURE);
		}
	};

	/**
	 * 用户收藏信息监听器
	 */
	private DataManagerListener<AppCommentData> userCollectionListener = new DataManagerListener<AppCommentData>() {
		@Override
		protected Message onSuccess(int what, AppCommentData data) {
			return super.onSuccess(USER_COLLECTION_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			return super.onFailed(USER_COLLECTION_FAILURE);
		}
	};

	/**
	 * 用户取消收藏信息监听器
	 */
	private DataManagerListener<AppsCollectionListData> cancelCollectionListener = new DataManagerListener<AppsCollectionListData>() {
		@Override
		protected Message onSuccess(int what, AppsCollectionListData data) {
			return super.onSuccess(CANCEL_COLLECTION_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			return super.onFailed(CANCEL_COLLECTION_FAILURE);
		}
	};

	/**
	 * 用户取消收藏信息监听器
	 */
	private DataManagerListener<Giftdata> mGiftDataManagerListener = new DataManagerListener<Giftdata>() {
		@Override
		protected Message onSuccess(int what, Giftdata data) {
			return super.onSuccess(GIFT_SUCCESS, data);
		}

		@Override
		protected Message onFailed(int what) {
			return super.onFailed(GIFT_FAILURE);
		}
	};

	/**
	 * 方法描述：执行网络请求，获取详情页面参数
	 * 
	 * @return void
	 */
	public void getNetData(String appId, String userId,String requestType) {
		if (appDetailNetSource == null) {
			appDetailNetSource = new AppDetailNetSource();
			appDetailNetSource.setListener(recommandListListener);
		}
		appDetailNetSource.setData(appId, userId);
		appDetailNetSource.doRequest(requestType);
	}

	/**
	 * 方法描述：得到评价列表数据
	 * 
	 * @return void
	 */
	public void getCommentData(String appId, int pageIndex, int pageSize,String requestType) {
		if (appCommentNetSource == null) {
			appCommentNetSource = new AppCommentNetSource();
			appCommentNetSource.setListener(commentListListener);
		}
		appCommentNetSource.setData(appId, pageIndex, pageSize);
		appCommentNetSource.doRequest(requestType);
	}

	/**
	 * 方法描述：提交用户评价
	 * 
	 * @return void
	 */
	public void doPostComment(String appId, String versionName,
			float starLevel, String content, String mobile, int userId,
			String nickName, String avatarUrl,String requestType) {
		if (appUserCommentNetSource == null) {
			appUserCommentNetSource = new AppUserCommentNetSource();
			appUserCommentNetSource.setListener(userCommentListener);
		}
		appUserCommentNetSource.setData(appId, versionName, starLevel, content,
				mobile, userId, nickName, avatarUrl);
		appUserCommentNetSource.doRequest(requestType);
	}

//	/**
//	 * 方法描述：提交用户评价
//	 *
//	 * @return void
//	 */
//	public void doPostCancelCollection(String userId, String appIds,String requestType) {
//		if (appCancelCollectionNetSource == null) {
//			appCancelCollectionNetSource = new AppCancelCollectionNetSource();
//			appCancelCollectionNetSource.setListener(cancelCollectionListener);
//		}
//		appCancelCollectionNetSource.setData(userId, appIds);
//		appCancelCollectionNetSource.doRequest(requestType);
//	}

	/**
	 * 
	 * 方法描述：收藏
	 * 
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void doPostCollection(int appId, String userId,String requestType) {
		if (appUserCollectionNetSource == null) {
			appUserCollectionNetSource = new AppUserCollectionNetSource();
			appUserCollectionNetSource.setListener(userCollectionListener);
		}
		appUserCollectionNetSource.setData(appId, userId);
		appUserCollectionNetSource.doRequest(requestType);
	}

	/**
	 * 方法描述：一键安装记录数据请求
	 * 
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void doGetGiftAtivationCodeNetSource(String userId, String giftId,String requestType) {
		if (mGiftDataNetSource == null) {
			mGiftDataNetSource = new GiftDataNetSource();
			mGiftDataNetSource.setListener(mGiftDataManagerListener);
			mGiftDataNetSource.setData(userId, giftId);
		}
		mGiftDataNetSource.doRequest(requestType);
	}

	public void setNullListener() {
		if (appCommentNetSource != null) {
			appCommentNetSource.setListener(null);
			commentListListener = null;
		}
		if (appDetailNetSource != null) {
			appDetailNetSource.setListener(null);
			recommandListListener = null;
		}
		if (appUserCollectionNetSource != null) {
			appUserCollectionNetSource.setListener(null);
			userCollectionListener = null;
		}
		if (appUserCommentNetSource != null) {
			appUserCommentNetSource.setListener(null);
			userCommentListener = null;
		}
		if (appCancelCollectionNetSource != null) {
			appCancelCollectionNetSource.setListener(null);
			appCancelCollectionNetSource = null;
		}
	}

	@Override
	protected void handleMessage(int what, int arg1, int arg2, Object obj) {
	}
}
