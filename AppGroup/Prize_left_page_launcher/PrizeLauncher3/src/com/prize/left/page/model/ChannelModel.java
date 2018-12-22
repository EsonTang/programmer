package com.prize.left.page.model;

import java.util.ArrayList;
import java.util.List;

import org.xutils.common.Callback;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.android.launcher3.R;
import com.prize.left.page.adapter.ChannelAdapter;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.SubCardType;
import com.prize.left.page.request.NavisRequest;
import com.prize.left.page.response.NavisResponse;
import com.prize.left.page.ui.StatusTextView;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DBUtils;
/***
 * 头条新闻业务类
 * @author fanjunchen
 *
 */
public class ChannelModel extends BaseModel<NavisResponse> {

	private NavisRequest reqParam;
	
	private NavisResponse response;
	
	private int cardType = 0;
	
	private ChannelAdapter mAdapter;
	
	private boolean isRunning = false;
	
	private ISelCardChange cardChange;
	
	private boolean isCardChange = false;
	
	private CardType mBean = null;
	
	public ChannelModel(Context ctx) {
		mCtx = ctx;
	}
	
	public void setCardType(int type) {
		cardType = type;
	}
	
	public void setCardBean(CardType type) {
		mBean = type;
	}
	
	public void setAdapter(ChannelAdapter adapter) {
		mAdapter = adapter;
	}
	
	public void setCardChange(ISelCardChange c) {
		cardChange = c;
	}
	/***
	 * 点击事件
	 */
	private OnItemClickListener mItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int pos, long id) {
			// TODO Auto-generated method stub
			SubCardType s = mAdapter.getItem(pos);
			if (null == s || (s.newsType == 1 && s.code == 0)) // 导航不可操作
				return;
			
			StatusTextView txt = (StatusTextView) view.findViewById(R.id.txt_name);
			if (DBUtils.updateChannel(s, txt.getSelStatus()) != 0)
				return;
			
			isCardChange = true;
			
			txt.setSel(!txt.getSelStatus());
		}
	};
	
	public OnItemClickListener getItemClick() {
		return mItemClick;
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
	}
	
	@Override
	public void doGet() {
		// 打开一个异步任务 来取数据
		if (!isRunning) {
			isRunning = true;
			new GetChannelTask().execute();
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
		doGet();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(NavisResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
		}
	}
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					response = CommonUtils.getObject(result, NavisResponse.class);
					onResponse(response);
					cancelObj = null;
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					// TODO Auto-generated method stub
					response = new NavisResponse();
					response.code = 2;
					response.msg = ex.getMessage();
					onResponse(response);
					cancelObj = null;
				}

				@Override
				public void onCancelled(CancelledException cex) {// 加载被取消
					// TODO Auto-generated method stub
					cancelObj = null;
				}

				@Override
				public void onFinished() { // 加载结束
					// TODO Auto-generated method stub
					cancelObj = null;
				}
			};
		}
	}

	/***
	 * 获取某个新闻卡片类型的频道数据
	 * @author fanjunchen
	 *
	 */
	class GetChannelTask extends AsyncTask<Void, Void, Void> {

		private List<SubCardType> data = null;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected Void doInBackground(Void... params) {
			mAdapter.setSelCodes(DBUtils.findAllSelCardCode(cardType));
			data = DBUtils.findAllSubCode(cardType);
			if (data == null && mBean != null) {
				data = new ArrayList<SubCardType>();
				data.add(toSubBean(mBean));
			}
			return null;
		}
		
		private SubCardType toSubBean(CardType m) {
			SubCardType r = new SubCardType();
			r.code = m.subCode;
			r.newsType = m.code;
			r.name = m.name;
			return r;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			isRunning = false;
			mAdapter.setData(data);
			super.onPostExecute(result);
		}
	}
	
	public void doFinish() {
		if (isCardChange && cardChange != null) {
			isCardChange = false;
			cardChange.onSelCardChange();
		}
	}

	@Override
	public void doBindImg() {
		// TODO Auto-generated method stub
		
	}
}
