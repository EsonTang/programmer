package com.prize.left.page.model;

import java.util.Properties;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.android.launcher3.LauncherApplication;
import com.prize.left.page.bean.BDMovieItem;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.PushBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.bean.table.PushTable;
import com.prize.left.page.request.PushRequest;
import com.prize.left.page.response.PushResponse;
import com.prize.left.page.safe.XXTEAUtil;
import com.prize.left.page.ui.PushViewLinearLayout;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DBUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.MySystemProperties;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.tencent.stat.StatService;

/***
 * 推送务类
 * 
 * @author zhouerlong
 * 
 */
public class PushModel extends BaseModel<PushResponse> {

	private PushResponse response;

	private PushResponse tmpResp;

	private PushRequest reqParam;
	
	//test
	private PushViewLinearLayout pushView;
	

	
	private String resIdentity = null;

	private boolean isRunning = false;
	

	
	public void setResIdentity(String res) {
		resIdentity = res;
		if (!MySystemProperties.isKoobee())
			resIdentity += "_coosea";
	}

	private Properties mProp = new Properties();

	public PushModel(Context ctx) {
		mCtx = ctx;
		

		reqParam = new PushRequest();
	}
	
	


	private void doTest() {
		int resId = CommonUtils.getResourceId(mCtx, "string", resIdentity);
		if (resId != -1) {
			newHttpCallback();
			httpCallback.onSuccess(mCtx.getString(resId));
		}
	}
	

	
	private boolean isTest = false;

	@Override
	public void doGet() {
		newHttpCallback();

		// TODO Auto-generated method stub
		if (isTest) {
			doTest();
			return;
		}
		try {
			if (null == response) {
				response = new PushResponse();
				response.data = new PushBean();
			}

			/*if(DBUtils.disablePushView()) {
				pushView.visible();
			}else {
				pushView.gone();
			}*/
			response.data.list = LauncherApplication.getDbManager().selector(PushTable.class).where("status", "=", 1).orderBy("_sort").findAll();
			
			if (response.data.list != null &&
					response.data.list.size() > 1 )
				onResponse(response);
//			else
//				doTest();
//			if(DBUtils.disablePushView()>0)
			doPostNet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
	}	

	/**操作网络*/
	public void doPostNet() {
		// TODO Auto-generated method stub
		if (isRunning)
			return;
		isRunning = true;
		new ParseTask().execute();
	}
	
	

	
	/***
	 * 请求网络导航并解析返回的数据
	 * @author fanjunchen
	 *
	 */
	class ParseTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {
			
			boolean b = false;
			String str =null;
			try {
				reqParam = new PushRequest();
				PreferencesUtils.addHeaderParam(reqParam, mCtx);
				reqParam.accessTime = PreferencesUtils.getString(mCtx, IConstants.KEY_NET_PUSH_ACCESS_TIME);
//				if (MySystemProperties.isKoobee())
//					reqParam.channel = "koobee";
				
				WhereBuilder wb = WhereBuilder.b("uitype", "=",IConstants.RECENT_USE_CARD_UITYPE);
				CardType navisCard = LauncherApplication.getDbManager().selector(CardType.class).where(wb).findFirst();
					if(navisCard!=null) {
					reqParam.uitype = navisCard.uitype;
					reqParam.dataCode=navisCard.dataCode;  
					reqParam.addBodyParameter("uitype", reqParam.uitype);
					reqParam.addBodyParameter("dataCode", reqParam.dataCode);
                    
					//消息头验证和参数校验
					if(LeftModel.bodyOk) {
					   String sign = Verification.getInstance().getSign(
							reqParam.getStringParams());
					   reqParam.sign = sign;
					}
					
					str = x.http().postSync(reqParam, String.class);
					}else {					
					isRunning=false;
					}		
				response = CommonUtils.getObject(str, PushResponse.class);
				if(null != response) {
					if (response.code != 0)
						return b;
//					if(false) 
					PreferencesUtils.putString(mCtx, IConstants.KEY_NET_PUSH_ACCESS_TIME, response.data.accessTime);
					if (response.data != null && response.data.list != null
							&& response.data.list.size() > 0) {
						b = true;
						LauncherApplication.getDbManager().saveOrUpdate(response.data.list);
						
						response.data.list = LauncherApplication.getDbManager().selector(PushTable.class).where("status", "=", 1).orderBy("_sort").findAll();
					}
				}
			} catch (Throwable e) {
				e.getMessage();
				e.printStackTrace();
			}
			return b;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				if(DBUtils.disablePushView()>0) {
					pushView.visible();
				}else {
					pushView.gone();
				}
				onResponse(response);
			}
			isRunning = false;
		}
	}

	@Override
	public void doPost() {
		doGet();
	}

	@Override
	public void doRefresh() {
		doGet();
	}

	/***
	 * 获取下一页数据
	 */
	public void getNextPage() {
		/*
		 * if (response != null && response.has_more == 1) { if (isRunning)
		 * return; isRunning = true; pageIndex++; doGet(); } else if (response
		 * != null && response.has_more == 0) { isRunning = true; pageIndex = 1;
		 * doGet(); }
		 */
//		StatService.trackCustomEvent(mCtx, "CardMovieBaidu", "");
	}

	public void clickMore() {
		/*request.moreMovie(LauncherApplication.getInstance().getCityId(),
				bdCallback);

		mProp.clear();
		mProp.setProperty("isMore", "点击了更多");
		StatService.trackCustomKVEvent(mCtx, "CardMovieBaidu", mProp);*/
	}

	public void jumpMovieDetail(BDMovieItem data, int pos) {
	/*	request.jumpSingleMovie(LauncherApplication.getInstance().getCityId(),
				data.from.provider_id, String.valueOf(data.movie_id),
				bdCallback);
		mProp.clear();
		mProp.setProperty("position", String.valueOf(pos + 1));
		StatService.trackCustomKVEvent(mCtx, "CardMovieBaidu", mProp);*/
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(PushResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (pushView == null || null == resp.data)
				return;
			pushView.setDatas(resp.data.list);
		}

//		visible();
	}

/*	public void gone() {
		viewHolder.mTitleView.setVisibility(View.GONE);
		RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) viewHolder.itemView
				.getLayoutParams();
		if (p != null)
			p.bottomMargin = 0;
		viewHolder.itemView.setLayoutParams(p);
		viewHolder.itemView.setVisibility(View.GONE);
		// viewHolder.itemView.invalidate();
	}*/

	/*public void visible() {
		RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) viewHolder.itemView
				.getLayoutParams();
		if (p != null)
			p.bottomMargin = mCtx.getResources().getDimensionPixelSize(
					R.dimen.search_left_margin);
		// contents.setVisibility(View.VISIBLE);
		viewHolder.itemView.setLayoutParams(p);
		viewHolder.mTitleView.setVisibility(View.VISIBLE);
		viewHolder.itemView.setVisibility(View.VISIBLE);
	}*/

	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
	}
	
	public void setContent(View v) {
		pushView = (PushViewLinearLayout) v;
	}

	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					PushResponse r;
					r = CommonUtils.getObject(result, PushResponse.class);
					if (response == null)
						response = r;
					onResponse(r);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					if (response == null)
						response = new PushResponse();
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
		if (pushView != null) {
			pushView.doBindImg();
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		isPause = true;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
	/*	if (isPause && isNeedFresh) {
			isNeedFresh = false;
			mHandler.sendEmptyMessage(PushHandler.MSG_REFRESH);
		}
		isPause = false;*/
		doRefresh();
	}
}
