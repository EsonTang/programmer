package com.prize.left.page.model;

import java.util.List;
import java.util.Properties;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.launcher3.LauncherApplication;
import com.lqsoft.lqtheme.LqThemeParser;
import com.prize.left.page.bean.DeskBean;
import com.prize.left.page.bean.table.DeskTable;
import com.prize.left.page.request.DeskRequest;
import com.prize.left.page.response.DeskResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.MySystemProperties;
import com.prize.left.page.util.PreferencesUtils;

/***
 * 推送务类
 * 
 * @author zhouerlong
 * 
 */
public class DeskModel extends BaseModel<DeskResponse> {

	private DeskResponse response;

	private DeskRequest request;

	private boolean isRunning = false;

	private Properties mProp = new Properties();

	private static  DeskModel mInstance;

	/**刷新的时间间隔 2小时*/
	private final long BETWEEN_TIME = 1000 * 60 *60 * 2;

	public DeskModel(Context ctx) {
		mCtx = ctx;

		request = new DeskRequest();
	}
	
	public static DeskModel getInstance(Context c) {
		if(mInstance ==null) {
			mInstance = new DeskModel(c);
		}
		return  mInstance;
	}

	@Override
	public void doGet() {
		if(!LqThemeParser.isDefaultLoacalTHeme()) {
			return;
		}
		newHttpCallback();
		try {
			if (null == response) {
				response = new DeskResponse();
				response.data = new DeskBean();
			}
			
			
			/*response.data.list = LauncherApplication.getDbManager()
					.selector(DeskTable.class)
					.findAll();
			

			if (response.data.list != null && response.data.list.size() > 1) {
				onResponse(response);
			}*/
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
	
	class ParserLocalAsync  extends AsyncTask<Void, Void, List<DeskTable>> {

		@Override
		protected List<DeskTable> doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			try {
				return  LauncherApplication.getDbManager()
						.selector(DeskTable.class).where("status","=",1)
						.findAll();
			} catch (DbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<DeskTable> result) {
			
			super.onPostExecute(result);
			try {
				if (null == response) {
					response = new DeskResponse();
					response.data = new DeskBean();
				}
				response.data.list = result;
				
				if (response.data.list != null && response.data.list.size() > 0) {
					onResponse(response);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		
		
	}
	
	public void addOrUpdate(List<DeskTable> entry) {
		try {
				LauncherApplication.getDbManager().delete(DeskTable.class);
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
//				request.accessTime = PreferencesUtils.getString(mCtx,
//						IConstants.KEY_NET_ICON_ACCESS_TIME);
				if (MySystemProperties.isKoobee())
					request.channel = "koobee";
				String str = x.http().postSync(request, String.class);
				
				response = CommonUtils.getObject(str, DeskResponse.class);

				PreferencesUtils.putLong(mCtx, IConstants.KEY_REFRESH_DESK_TIME, System.currentTimeMillis());
				if (null != response) {
					if (response.code != 0)
						return b;
						PreferencesUtils.putString(mCtx,
								IConstants.KEY_NET_ICON_ACCESS_TIME,
								response.data.accessTime);
					if (response.data != null && response.data.list != null
							&& response.data.list.size() > 0) {
						b = true;

						addOrUpdate(response.data.list);
						
						response.data.list=  LauncherApplication.getDbManager()
								.selector(DeskTable.class).where("status","=",1)
								.findAll();
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

		long ll = System.currentTimeMillis() - PreferencesUtils.getLong(mCtx, IConstants.KEY_REFRESH_DESK_TIME);
		if(ll>BETWEEN_TIME) {
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
	public void onResponse(DeskResponse resp) {
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
					DeskResponse r;
					r = CommonUtils.getObject(result, DeskResponse.class);
					if (response == null)
						response = r;
					onResponse(r);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					if (response == null)
						response = new DeskResponse();
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
		mInstance =(DeskModel) object;
		
	}
}
