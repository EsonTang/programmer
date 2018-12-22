package com.prize.left.page.model;

import org.xutils.x;
import org.xutils.common.Callback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.prize.left.page.request.FoodsRequest;
import com.prize.left.page.response.FoodsResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.view.holder.FoodsViewHolder;
/***
 * 美食或团购业务类
 * @author fanjunchen
 *
 */
public class FoodsModel extends BaseModel<FoodsResponse> {

	private FoodsRequest reqParam;
	
	private FoodsResponse response;
	
	private boolean isTest = true;
	/**测试时的资源ID*/
	private String resIdentity = null;
	
	private FoodsViewHolder viewHolder = null;
	
	public FoodsModel(Context ctx) {
		mCtx = ctx;
	}
	
	public void setResIdentity(String res) {
		resIdentity = res;
	}

	private void doTest() {
		int resId = CommonUtils.getResourceId(mCtx, "string", resIdentity);
		if (resId != -1) {
			newHttpCallback();
			httpCallback.onSuccess(mCtx.getString(resId));
		}
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (FoodsViewHolder) holder;
	}
	
	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		if (isTest) {
			doTest();
			return;
		}
		
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			cancelObj = x.http().get(reqParam, httpCallback);
		}
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (isTest) {
			doTest();
			return;
		}
		if (null == cancelObj || cancelObj.isCancelled()) {
			cancelObj = null;
			newHttpCallback();
			cancelObj = x.http().post(reqParam, httpCallback);
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(FoodsResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null || null == resp.data)
				return;
			viewHolder.setDatas(resp.data.items);
			response.data = resp.data;
			response.data.items = resp.data.items;
		}
	}
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					FoodsResponse r = CommonUtils.getObject(result, FoodsResponse.class);
					if (response == null)
						response = r;
					if (!isPause)
						onResponse(r);
					else
						isNeedFresh = true;
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					// TODO Auto-generated method stub
					if (response == null)
						response = new FoodsResponse();
					response.code = 2;
					response.data = null;
					response.msg = ex.getMessage();
					onResponse(response);
				}

				@Override
				public void onCancelled(CancelledException cex) {// 加载被取消
					// TODO Auto-generated method stub
				}

				@Override
				public void onFinished() { // 加载结束
					// TODO Auto-generated method stub
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
