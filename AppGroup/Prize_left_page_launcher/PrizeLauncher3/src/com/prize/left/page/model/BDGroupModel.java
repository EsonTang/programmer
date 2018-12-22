package com.prize.left.page.model;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Properties;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.bean.BDGroupItem;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.model.BDMovieModel.BDMovieHandler;
import com.prize.left.page.request.BDGroupRequest;
import com.prize.left.page.response.BDGroupResponse;
import com.prize.left.page.response.BDMovieResponse;
import com.prize.left.page.safe.XXTEAUtil;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.prize.left.page.view.holder.BDGroupViewHolder;
import com.tencent.stat.StatService;
/***
 * 百度团购业务类
 * @author fanjunchen
 *
 */
public class BDGroupModel extends BaseModel<BDGroupResponse> {
	
	private BDGroupRequest reqParam;

	private BDGroupResponse response;
	
	private BDGroupResponse tmpResp;
	
//	private BDXLifeUtil request;
	
	private BDGroupViewHolder viewHolder = null;
	
	private CardBean cardBean = null;
	/**页码*/
	private static int pageIndex = 1;
	/**页大小*/
	private final static int pageSize = 3;
	/**百度生活服务回调接口对象*/
//	private ILifeCallback bdCallback;
	
	private BDGroupHandler mHandler;
	
	private boolean isRunning = false;
	
	private Properties mProp = new Properties();
	
	public BDGroupModel(Context ctx) {
		mCtx = ctx;
//		request = BDXLifeUtil.getInstance();
//		request.init(ctx);
		reqParam=new BDGroupRequest();
		mHandler = new BDGroupHandler();
		mHandler.setModel(this);
	}
	
	public void setCardBean(CardBean b) {
		cardBean = b;
	}
	
	@Override
	public void doGet() {
		reqParam  = new BDGroupRequest();
		viewHolder.start();
		newHttpCallback();
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
//				request.getGroupList(reqParam,httpCallback);
				}else {					
				isRunning=false;
				viewHolder.end();
				}
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
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
		pageIndex = 1;
		doGet();
	}
	/***
	 * 获取下一页数据
	 */
	public void getNextPage() {
		if (isRunning)
			return;
		isRunning = true;
//		pageIndex ++;
		doGet();

		StatService.trackCustomEvent(mCtx, "CardGroup", "");
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
		StatService.trackCustomKVEvent(mCtx, "CardGroup", mProp);
	}
	/***
	 * 跳转到某个团购详情
	 * @param data
	 */
	public void jumpGroupDetail(BDGroupItem data, int pos) {
		if (data.clickUrl.startsWith("http")){
			Intent it = new Intent(mCtx, WebViewActivity.class);
			it.putExtra(WebViewActivity.P_URL, data.clickUrl);
			mCtx.startActivity(it);
			it = null;
		}
		//统计位置
		mProp.clear();
		mProp.setProperty("position", String.valueOf(pos + 1));
		StatService.trackCustomKVEvent(mCtx, "CardGroup", mProp);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(BDGroupResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null || null == resp.data.list)
				return;
			
			if (response == resp && (resp.data.list == null ||
					resp.data.list.size()<1)) {
				
				//viewHolder.itemView.setVisibility(View.GONE);
				if (mICdNotify != null)
					mICdNotify.notifyUpdate(mCdType, false);
				return;
			}
			visible();
			viewHolder.itemView.setVisibility(View.VISIBLE);
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
		viewHolder = (BDGroupViewHolder) holder;
		
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
						tmpResp = CommonUtils.getObject(result, BDGroupResponse.class);
						if (response == null)
							response = tmpResp;
						isRunning = false;
//						if (!isPause)
							mHandler.sendEmptyMessage(BDGroupHandler.MSG_REFRESH);//onResponse(response);
						
					}
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					// TODO 请求出错
					tmpResp = null;
					isRunning = false;
					mHandler.sendEmptyMessage(BDGroupHandler.MSG_ERROR);
					
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

	static class BDGroupHandler extends Handler {
		
		private WeakReference<BDGroupModel> mModel;
		
		static final int MSG_REFRESH = 1;
		
		static final int MSG_ERROR = MSG_REFRESH + 1;
		
		static final int MSG_UPDATE = MSG_ERROR + 1;
		
		public void setModel(BDGroupModel m) {
			mModel = new WeakReference<BDGroupModel>(m);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_REFRESH:
				BDGroupModel m = mModel.get();
				if (m != null) {
					List<BDGroupItem> grouplist = m.tmpResp.data.list;
					BDGroupResponse groupInfos = new BDGroupResponse();
					if (m.response == null) {
						m.response = groupInfos;
					}
					int start = pageSize * pageIndex;
					if (m.response != null) {
						if (grouplist != null) {
							if(grouplist.size()>pageSize){
								m.tmpResp.data.list=grouplist.subList(start, start+pageSize);
							}
							m.onResponse(m.tmpResp);
							pageIndex++;

							if (pageIndex >= grouplist.size() / pageSize) {
								pageIndex = 0;
							}
							m.viewHolder.end();
						}
					}
				}
					break;
				case MSG_ERROR:
					BDGroupModel mError = mModel.get();
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
		// TODO Auto-generated method stub
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
			mHandler.sendEmptyMessage(BDGroupHandler.MSG_REFRESH);
		}
		isPause = false;
	}
}
