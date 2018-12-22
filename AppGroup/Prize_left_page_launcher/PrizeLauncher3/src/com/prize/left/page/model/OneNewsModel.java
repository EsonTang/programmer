package com.prize.left.page.model;

import org.xutils.x;
import org.xutils.common.Callback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.prize.left.page.bean.CardBean;
import com.prize.left.page.request.OneNewsRequest;
import com.prize.left.page.response.OneNewsResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.view.holder.OneNewsViewHolder;
/***
 * 一点资讯新闻业务类
 * @author fanjunchen
 *
 */
public class OneNewsModel extends BaseModel<OneNewsResponse> {

	private OneNewsRequest reqParam;
	
	private OneNewsResponse response;
	
	private OneNewsViewHolder viewHolder = null;
	
	private CardBean cardBean = null;
	/**更多时的中转URL*/
	public static final String MORE_URL = "http://www.yidianzixun.com/m/channel/keyword/%1$s?s=kusai";
	/**是否正在请求数据*/
	private boolean isRunning = false;
	
	public OneNewsModel(Context ctx) {
		mCtx = ctx;
		reqParam = new OneNewsRequest();
	}
	
	public void setCardBean(CardBean b) {
		cardBean = b;
	}
	
	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			viewHolder.start();
			reqParam.channel_id = String.valueOf(cardBean.cardType.subCode);
			
			cancelObj = x.http().get(reqParam, httpCallback);
		}
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		/*if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			cancelObj = x.http().post(reqParam, httpCallback);
		}*/
		if (isRunning)
			return;
		isRunning = true;
		doGet();
	}
	/***
	 * 获取下一页数据
	 */
	public void getNextPage() {
		if (isRunning)
			return;
		isRunning = true;
		reqParam.nextPage();
		doGet();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(OneNewsResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null || null == resp.result)
				return;
			if (response == resp && (resp.data.items == null ||
					resp.data.items.size()<1)) {
				//viewHolder.itemView.setVisibility(View.GONE);
				if (mICdNotify != null)
					mICdNotify.notifyUpdate(mCdType, false);
				return;
			}
			else
				viewHolder.setDatas(resp.result);
		}
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (OneNewsViewHolder) holder;
		
		viewHolder.setModel(this);
	}
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					OneNewsResponse r = CommonUtils.getObject(result, OneNewsResponse.class);
					if (response == null)
						response = r;
					if (!isPause)
						onResponse(r);
					else
						isNeedFresh = true;
					cancelObj = null;
					isRunning = false;
					viewHolder.end();
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					// TODO Auto-generated method stub
					isRunning = false;
					if (response == null)
						response = new OneNewsResponse();
					response.code = 2;
					response.data = null;
					response.msg = ex.getMessage();
					onResponse(response);
					cancelObj = null;
					viewHolder.end();
				}

				@Override
				public void onCancelled(CancelledException cex) {// 加载被取消
					// TODO Auto-generated method stub
					cancelObj = null;
					isRunning = false;
					viewHolder.end();
				}

				@Override
				public void onFinished() { // 加载结束
					// TODO Auto-generated method stub
					cancelObj = null;
					isRunning = false;
					viewHolder.end();
				}
			};
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
	public void doRefresh() {
		if (isRunning)
			return;
		isRunning = true;
		reqParam.reset();
		doGet();
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
			onResponse(response);
		}
		isPause = false;
	}
}
