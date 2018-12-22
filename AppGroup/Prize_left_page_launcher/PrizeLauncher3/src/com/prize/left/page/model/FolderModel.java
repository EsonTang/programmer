package com.prize.left.page.model;

import java.util.List;
import java.util.Properties;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;

import android.content.Context;
import android.os.AsyncTask;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.LauncherModel;
import com.lqsoft.lqtheme.LqThemeParser;
import com.prize.left.page.bean.FolderBean;
import com.prize.left.page.bean.table.FolderTable;
import com.prize.left.page.request.FolderRequest;
import com.prize.left.page.response.FolderResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.MySystemProperties;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;

/***
 * 推送务类
 * 
 * @author zhouerlong
 * 
 */
public class FolderModel extends BaseModel<FolderResponse> {

	private FolderResponse response;

	private FolderRequest request;

	private boolean isRunning = false;

	private Properties mProp = new Properties();

	private static FolderModel mInstance;

	/** 刷新的时间间隔 2小时 */
	private final long BETWEEN_TIME = 1000 * 60 * 60 * 2;

	public FolderModel(Context ctx) {
		mCtx = ctx;

		request = new FolderRequest();
	}

	public static FolderModel getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new FolderModel(c);
		}
		return mInstance;
	}

	@Override
	public void doGet() {
		if (!LqThemeParser.isDefaultLoacalTHeme()) {
			return;
		}
		newHttpCallback();
		try {
			if (null == response) {
				response = new FolderResponse();
				response.data = new FolderBean();
			}

			/*
			 * response.data.list = LauncherApplication.getDbManager()
			 * .selector(DeskTable.class) .findAll();
			 * 
			 * 
			 * if (response.data.list != null && response.data.list.size() > 1)
			 * { onResponse(response); }
			 */
			ParserLocalAsync p = new ParserLocalAsync();
			p.execute();
			doPostNet();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void dogetLoc() {

		ParserLocalAsync p = new ParserLocalAsync();
		p.execute();
	}

	class ParserLocalAsync extends AsyncTask<Void, Void, List<FolderTable>> {

		@Override
		protected List<FolderTable> doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			try {
				return LauncherApplication.getDbManager()
						.selector(FolderTable.class).where("status", "=", 1)
						.findAll();
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<FolderTable> result) {

			super.onPostExecute(result);
			try {
				if (null == response) {
					response = new FolderResponse();
					response.data = new FolderBean();
				}
				response.data.apps = result;

				if (response.data.apps != null && response.data.apps.size() > 0) {
					onResponse(response);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	public void addOrUpdate(List<FolderTable> entry) {
		try {
			LauncherApplication.getDbManager().delete(FolderTable.class);
			LauncherApplication.getDbManager().save(entry);

		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** 操作网络 */
	public void doPostNet() {
		if (isRunning)
			return;
		isRunning = true;
		new ParseTask().execute();
	}

	/***
	 * 请求网络导航并解析返回的数据
	 * 
	 * @author fanjunchen
	 * 
	 */
	class ParseTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {

			boolean b = false;

			try {
				// request.accessTime = PreferencesUtils.getString(mCtx,
				// IConstants.KEY_NET_ICON_ACCESS_TIME); //这个是制定服务器  如果有应用更新那么 时间会改变 通过这个时间的状态 可以
				// 可以判定 返回的数据是否需要重新返回  如果 时间相同也就是没有修改  return null  否则 返回所有数据
				
				request = new FolderRequest();
				String pkgs =LauncherModel.loadAllAppPkgs(mCtx);
				request.packageNames = pkgs;
				request.addBodyParameter("packageNames", pkgs);

			    if(request.getBodyParams()!=null){
				    String sign = Verification.getInstance().getSign(
				    		request.getBodyParams());
				    request.sign = sign;
			    }
				PreferencesUtils.addHeaderParam(request,mCtx);
				String str = x.http().postSync(request, String.class);

				response = CommonUtils.getObject(str, FolderResponse.class);

				PreferencesUtils.putLong(mCtx,
						IConstants.KEY_REFRESH_FOLDER_TIME,
						System.currentTimeMillis());
				if (null != response) {
					if (response.code != 0)
						return b;
					PreferencesUtils.putString(mCtx,
							IConstants.KEY_NET_ICON_ACCESS_TIME,
							response.data.accessTime);
					if (response.data != null && response.data.apps != null
							&& response.data.apps.size() > 0) {
						b = true;

						addOrUpdate(response.data.apps);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return b;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				onResponse(response);
			}
			isRunning = false;
		}
	}

	@Override
	public void doPost() {
		doGet();
	}

	public void onPageEndMoving() {

		long ll = System.currentTimeMillis()
				- PreferencesUtils.getLong(mCtx,
						IConstants.KEY_REFRESH_FOLDER_TIME);
		if (ll > BETWEEN_TIME) {
			doGet();
		}
	}

	@Override
	public void doRefresh() {
		doGet();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(FolderResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {

		}
	}

	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					FolderResponse r;
					r = CommonUtils.getObject(result, FolderResponse.class);
					if (response == null)
						response = r;
					onResponse(r);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					if (response == null)
						response = new FolderResponse();
					response.code = 2;
					response.msg = ex.getMessage();
					onResponse(response);
				}

				@Override
				public void onCancelled(CancelledException cex) {// 加载被取消
				}

				@Override
				public void onFinished() { // 加载结束
				}
			};
		}
	}

	@Override
	public void doBindImg() {
	}

	@Override
	public void onPause() {
	}

	@Override
	public void onResume() {
	}

	public static void onDestroy(Object object) {
		mInstance = (FolderModel) object;

	}
}
