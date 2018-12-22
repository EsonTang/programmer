package com.prize.left.page.model;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.common.Callback.CancelledException;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.bean.BDMovieItem;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.model.BDGroupModel.BDGroupHandler;
import com.prize.left.page.request.BDGroupRequest;
import com.prize.left.page.request.BDMovieRequest;
import com.prize.left.page.response.BDMovieResponse;
import com.prize.left.page.response.InvnoNewsResponse;
import com.prize.left.page.safe.XXTEAUtil;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.prize.left.page.view.holder.BDMovieViewHolder;
import com.tencent.stat.StatService;
/***
 * 百度电影业务类
 * @author fanjunchen
 *
 */
public class BDMovieModel extends BaseModel<BDMovieResponse> {
	
	private BDMovieRequest reqParam;

	private BDMovieResponse response;
	
	private BDMovieResponse tmpResp;
	
//	private BDXLifeUtil request;
	
	private BDMovieViewHolder viewHolder = null;
	
	private CardBean cardBean = null;
	/**页码*/
	private static int pageIndex = 0;
	/**页大小*/
	private final static int pageSize = 3;
	/**百度生活服务回调接口对象*/
//	private ILifeCallback bdCallback;s
	
	private BDMovieHandler mHandler;
	
	private boolean isRunning = false;
	
	private Properties mProp = new Properties();
	
	public BDMovieModel(Context ctx) {
		mCtx = ctx;
//		request = BDXLifeUtil.getInstance();
//		request.init(ctx);
		reqParam=new BDMovieRequest();
		mHandler = new BDMovieHandler();
		mHandler.setModel(this);
	}
	
	public void setCardBean(CardBean b) {
		cardBean = b;
	}
	
	@Override
	public void doGet() {

		reqParam  = new BDMovieRequest();
		viewHolder.start();
//		reqParam.cityId=LauncherApplication.getInstance().getCityId();
		try {
			WhereBuilder wb = WhereBuilder.b("dataCode", "=",cardBean.cardType.dataCode).and("subCode","=",cardBean.cardType.subCode);
			CardType moviesCard = LauncherApplication.getDbManager().selector(CardType.class).where(wb).findFirst();
				if(moviesCard!=null) {
				reqParam.uitype = moviesCard.uitype;
				reqParam.dataCode=moviesCard.dataCode;
				reqParam.addBodyParameter("uitype", reqParam.uitype);
				reqParam.addBodyParameter("dataCode", reqParam.dataCode);
				reqParam.setConnectTimeout(10*1000);
				//消息头验证和参数校验
				if (LeftModel.bodyOk) {
					  if(reqParam.getBodyParams()!=null){
						    String sign = Verification.getInstance().getSign(
									reqParam.getBodyParams());
							reqParam.sign = sign;
					    }
				}
				newHttpCallback();
				PreferencesUtils.addHeaderParam(reqParam, mCtx);
				x.http().get(reqParam, httpCallback);
//				request.getMovie(reqParam, httpCallback);
				}else {					
				isRunning=false;
				viewHolder.end();
				}
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		request.getMovie(LauncherApplication.getInstance().getCityId(), pageIndex, pageSize, bdCallback);

	}

	@Override
	public void doPost() {
		if (isRunning)
			return;
		isRunning = true;
		doGet();
	}
	
	@Override
	public void doRefresh() {
		if (isRunning)
			return;
		isRunning = true;
		pageIndex =0;
		doGet();
	}
	/***
	 * 获取下一页数据
	 */
	public void getNextPage() {
//		if (response != null && response.data.pageInfo.pageCount>= pageIndex) {
			if (isRunning)
				return;
			isRunning = true;
//			pageIndex ++;
			doGet();
//		}
//		else if (response != null && response.data.pageInfo.pageCount< pageIndex) {
//			isRunning = true;
//			pageIndex = 1;
//			doGet();
//		}
		StatService.trackCustomEvent(mCtx, "CardMovies", "");
	}
	
	public void clickMore() {
		if (cardBean.cardType.moreType==1) {
			Intent it = new Intent(mCtx, WebViewActivity.class);
			it.putExtra(WebViewActivity.P_URL, cardBean.cardType.moreUrl);
			mCtx.startActivity(it);
			it = null;
		}

		//统计更多
		mProp.clear();
		mProp.setProperty("isMore", "点击了更多");
		StatService.trackCustomKVEvent(mCtx, "CardMovies", mProp);
	}
	
	public void jumpMovieDetail(BDMovieItem data, int pos) {
		if (data.clickUrl.startsWith("http")){
		Intent it = new Intent(mCtx, WebViewActivity.class);
		it.putExtra(WebViewActivity.P_URL, data.clickUrl);
		mCtx.startActivity(it);
		it = null;
		}

		//统计位置
		mProp.clear();
		mProp.setProperty("position", String.valueOf(pos + 1));
		StatService.trackCustomKVEvent(mCtx, "CardMovies", mProp);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(BDMovieResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null || null == resp.data)
				return;
			
			if (response == resp && (resp.data == null ||
					resp.data.list.size()<1)) {
				
				//viewHolder.itemView.setVisibility(View.GONE);
				if (mICdNotify != null)
					mICdNotify.notifyUpdate(mCdType, false);
				return;
			}
			visible();
			response.data.list = resp.data.list;
			viewHolder.setDatas(resp.data.list);
		}
	}
	
	public void gone() {
		viewHolder.mTitleView.setVisibility(View.GONE);
		RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
		if (p != null)
			p.bottomMargin = 0;
		viewHolder.itemView.setLayoutParams(p);
		viewHolder.itemView.setVisibility(View.GONE);
		//viewHolder.itemView.invalidate();
	}
	
	public void visible() {
		RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
		if (p != null)
			p.bottomMargin = mCtx.getResources().getDimensionPixelSize(R.dimen.search_left_margin);
		//contents.setVisibility(View.VISIBLE);
		viewHolder.itemView.setLayoutParams(p);
		viewHolder.mTitleView.setVisibility(View.VISIBLE);
		viewHolder.itemView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (BDMovieViewHolder) holder;
		
		viewHolder.setModel(this);
	}
	
	@Override
	protected void newHttpCallback() {
		if (null==httpCallback) {
			httpCallback=new Callback.CommonCallback<String>() {
				@Override
				public void onSuccess(String result) {
					// 成功获取到资讯
					if (result!= null) {
						 //TODO 请求完成,返回数据
						tmpResp = CommonUtils.getObject(result, BDMovieResponse.class);
						isRunning = false;
//						if (!isPause)
							mHandler.sendEmptyMessage(BDMovieHandler.MSG_REFRESH);//onResponse(response);
//						else
//							isNeedFresh = true;	
						
					}
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					 //TODO 请求出错
					isRunning = false;
					tmpResp = null;
					mHandler.sendEmptyMessage(BDMovieHandler.MSG_ERROR);
					
				}

				@Override
				public void onCancelled(CancelledException cex) {
					// TODO Auto-generated method stub
					isRunning = false;
					cancelObj=null;
					viewHolder.end();
				}

				@Override
				public void onFinished() {
					// TODO Auto-generated method stub
					isRunning = false;
					cancelObj=null;
					viewHolder.end();
				}
			};
		}
	}

	static class BDMovieHandler extends Handler {
		
		private WeakReference<BDMovieModel> mModel;
		
		static final int MSG_REFRESH = 1;
		
		static final int MSG_ERROR = MSG_REFRESH + 1;
		
		static final int MSG_UPDATE = MSG_ERROR + 1;
		
		public void setModel(BDMovieModel m) {
			mModel = new WeakReference<BDMovieModel>(m);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_REFRESH:
				BDMovieModel m = mModel.get();
				if (m != null) {
					List<BDMovieItem> movieslist = m.tmpResp.data.list;
					BDMovieResponse moviesInfos = new BDMovieResponse();
					if (m.response == null) {
						m.response = moviesInfos;
					}
					int start = pageSize * pageIndex;
					if (m.response != null) {
						if (movieslist != null) {
							if(movieslist.size()>pageSize){
								m.tmpResp.data.list=movieslist.subList(start, start+pageSize);
							}
							m.onResponse(m.tmpResp);
							pageIndex++;

							if (pageIndex >= movieslist.size() / pageSize) {
								pageIndex = 0;
							}
							m.viewHolder.end();
						}
					}
					}
					break;
				case MSG_ERROR:
					BDMovieModel mError = mModel.get();
					if (mError!=null) {						
						mError.viewHolder.end();
					}
					break;
				case MSG_UPDATE:
					m = mModel.get();
					if (m != null) {
						m.viewHolder.end();
					}
					break;
			}
		}
	}

	@Override
	public void doBindImg() {
		if (viewHolder != null) {
			viewHolder.doBindImg();
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
		if (isPause && isNeedFresh) {
			isNeedFresh = false;
			mHandler.sendEmptyMessage(BDMovieHandler.MSG_REFRESH);
		}
		isPause = false;
	}
}
