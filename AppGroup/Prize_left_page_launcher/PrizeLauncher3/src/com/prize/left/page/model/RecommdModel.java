package com.prize.left.page.model;

import java.util.List;
import java.util.Properties;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;

import android.content.Context;
import android.os.AsyncTask;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.PrizeRecommdView;
import com.android.launcher3.RecommdLinearLayout;
import com.lqsoft.lqtheme.LqThemeParser;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.left.page.bean.AppsCardBean;
import com.prize.left.page.bean.RecommdBean;
import com.prize.left.page.request.RecommdRequest;
import com.prize.left.page.response.RecommdResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;

/***
 * 推送务类
 * 
 * @author zhouerlong
 * 
 */
public class RecommdModel extends BaseModel<RecommdResponse> {

	private RecommdResponse response;

	private RecommdRequest request;

	private boolean isRunning = false;

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}



	private Properties mProp = new Properties();

	/** 刷新的时间间隔 2小时 */
	private final long BETWEEN_TIME = 1000 * 60 * 60 * 2;

	public RecommdModel(Context ctx) {
		mCtx = ctx;

		request = new RecommdRequest();
	}
	


	private static RecommdModel mInstance;
	public static RecommdModel getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new RecommdModel(c);
		}
		return mInstance;
	}
	
	String pkgs ;
	
	public void doGet(String parm) {
		pkgs = parm;
		doGet();
	}

	@Override
	public void doGet() {
		newHttpCallback();
		try {
			if (null == response) {
				response = new RecommdResponse();
				response.data = new RecommdBean();
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

	class ParserLocalAsync extends AsyncTask<Void, Void, List<AppsItemBean>> {

		@Override
		protected List<AppsItemBean> doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			try {
				return LauncherApplication.getDbManager()
						.selector(AppsItemBean.class).where("status", "=", 1)
						.findAll();
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<AppsItemBean> result) {

			super.onPostExecute(result);
			try {
				if (null == response) {
					response = new RecommdResponse();
					response.data = new RecommdBean();
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

	public void addOrUpdate(List<AppsCardBean> entry) {
		try {
			LauncherApplication.getDbManager().delete(AppsCardBean.class);
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
	
	public void doPostRefresh() {
		if (isRunning)
			return;
		isRunning = true;

		mPagedIndex++;
		new ParseTask().execute();
		if(callback !=null && callback instanceof PrizeRecommdView) {
			PrizeRecommdView r = (PrizeRecommdView) callback;
			RecommdLinearLayout p = (RecommdLinearLayout) r.getParent();
			p.start();
		}
	
	}
	
	
	

	/***
	 * 请求网络导航并解析返回的数据
	 * 
	 * @author fanjunchen
	 * 
	 */
	
	public int mPagedIndex=1;
	class ParseTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {

			boolean b = true;

			try {
				// request.accessTime = PreferencesUtils.getString(mCtx,
				// IConstants.KEY_NET_ICON_ACCESS_TIME); //这个是制定服务器  如果有应用更新那么 时间会改变 通过这个时间的状态 可以
				// 可以判定 返回的数据是否需要重新返回  如果 时间相同也就是没有修改  return null  否则 返回所有数据
				
				request = new RecommdRequest();
				
				request.packageNames = pkgs;

				if(mPagedIndex>response.data.pageCount) {
					mPagedIndex=1;
				}
				request.pageIndex=mPagedIndex;
				request.addBodyParameter("packageNames", pkgs);

			    if(request.getBodyParams()!=null){
				    String sign = Verification.getInstance().getSign(
				    		request.getBodyParams());
				    request.sign = sign;
			    }
				PreferencesUtils.addHeaderParam(request,mCtx);
				String str = x.http().postSync(request, String.class);

				response = CommonUtils.getObject(str, RecommdResponse.class);

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

//						addOrUpdate(response.data.apps);
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
	public void onResponse(RecommdResponse resp) {
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
					RecommdResponse r;
					r = CommonUtils.getObject(result, RecommdResponse.class);
					if (response == null)
						response = r;
					onResponse(r);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					if (response == null)
						response = new RecommdResponse();
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
}
