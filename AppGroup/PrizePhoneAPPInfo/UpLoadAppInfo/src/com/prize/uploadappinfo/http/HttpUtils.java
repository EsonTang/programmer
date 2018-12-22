package com.prize.uploadappinfo.http;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import android.text.TextUtils;
import android.widget.Toast;

import com.prize.uploadappinfo.BaseApplication;
import com.prize.uploadappinfo.bean.AppRecordInfo;
import com.prize.uploadappinfo.bean.ClientInfo;
import com.prize.uploadappinfo.constants.Constant;
import com.prize.uploadappinfo.database.AppStateTable;
import com.prize.uploadappinfo.database.PrizeDatabaseHelper;
import com.prize.uploadappinfo.database.dao.AppStateDAO;
import com.prize.uploadappinfo.utils.CommonUtils;
import com.prize.uploadappinfo.utils.JLog;
import com.prize.uploadappinfo.utils.PreferencesUtils;
import com.prize.uploadappinfo.utils.Verification;

/**
 *
 * 类名称：HttpUtils
 * 
 * 创建人：longbaoxiu
 * 
 * 修改时间：2016年6月12日 下午5:23:46
 * 
 * @version 1.0.0
 *
 */
public class HttpUtils {
	private static String TAG = "HttpUtils";

	/**
	 * 上传手机所有三方app，及安装卸载信息（按照规则）,请求准备，先获取校验码
	 */
	public static void prepareUploadAppInfo() {
		if (ClientInfo.networkType == ClientInfo.NONET)
			return;
		ArrayList<AppRecordInfo> apps = AppStateDAO.getInstance().getApps();
		if (apps == null || apps.size() <= 0) {
			return;

		}
		getPidFromServer(new RequestPIDCallBack() {

			@Override
			public void requestOk(String pid) {
				uploadAppInfo(pid);
			}
		});

	}

	/**
	 * 此处才是真正上传app信息
	 * 
	 * @param pid
	 */
	private static void uploadAppInfo(String pid) {
		if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
			getUuidFromServer(pid);
			return;
		}
		final RequestParams reqParams = new RequestParams(Constant.APPINFOS_URL);
		reqParams.addBodyParameter("pid", pid);
		reqParams.addBodyParameter("datas", CommonUtils.getRequestParam());
		String sign = Verification.getInstance().getSign(
				reqParams.getBodyParams());
		reqParams.addBodyParameter("sign", sign);
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "uploadAppInfo-result=" + result);
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 00000) {
						PrizeDatabaseHelper
								.deleteAllData(AppStateTable.TABLE_NAME);
					} else {
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				JLog.e(TAG,
						"==========onErrorOne(Throwable ex, boolean isOnCallback)===========");
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {

			}
		});
	}

	/**
	 * 从服务器获取pid
	 * 
	 * @param back
	 *            RequestCallBack
	 */
	public static void getPidFromServer(final RequestPIDCallBack back) {
		RequestParams reqParams = new RequestParams(Constant.PID_URL);
		reqParams.addBodyParameter("KOOBEE", "dido");
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {
			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "getPidFromServer-result=" + result);
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 00000) {
						JSONObject o2 = (JSONObject) obj.opt("data");
						String pid = o2.optString("pid");
						if (back != null) {
							back.requestOk(pid);
						}
					} else {
					}
				} catch (JSONException e) {
				}
			}

		});
	}

	/**
	 * 从服务器获取手机唯一标识，并保存到设置（这里暂时使用SharedPreferences保存）
	 * 
	 * @param pid
	 *            这个从服务器获取HttpUtils.getPidFromServer（）；
	 */
	public static void getUuidFromServer(String pid) {
		RequestParams reqParams = new RequestParams(Constant.UUID_URL);
		reqParams.addBodyParameter("pid", pid);
		String sign = Verification.getInstance().getSign(
				reqParams.getBodyParams());
		reqParams.addBodyParameter("sign", sign);
		XExtends.http().post(reqParams, new CommonCallback<String>() {

			@Override
			public void onCancelled(CancelledException arg0) {

			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onSuccess(String result) {
				JLog.i(TAG, "getUuidFromServer-result=" + result);
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					int code = obj.optInt("code");
					if (code == 00000) {// 得到pid
						JSONObject o2 = (JSONObject) obj.opt("data");
						String uuid = o2.optString("uuid");
						PreferencesUtils.saveKEY_TID(uuid);
						Toast.makeText(BaseApplication.curContext, "保存UUId成功", Toast.LENGTH_SHORT).show();
					} else {
					}
				} catch (JSONException e) {
				}
			}

		});
	}

	/**
	 * 请求pid成功后回调
	 * 
	 * @author prize
	 *
	 */
	public interface RequestPIDCallBack {
		/****
		 * 请求pid成功后回调
		 * 
		 * @param pid
		 *            服务器获取的校验码
		 */
		void requestOk(String pid);

	};
}