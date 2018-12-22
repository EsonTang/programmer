package com.prize.left.page.model;

import java.util.ArrayList;
import java.util.List;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.EditText;

import com.android.launcher3.LauncherApplication;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.HotTipsTable;
import com.prize.left.page.request.HotTipsRequest;
import com.prize.left.page.response.HotTipsResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.prize.left.page.view.holder.HotTipsViewHolder;
/***
 * @author pengy
 *  热门搜索
 */
public class HotTipsModel extends BaseModel<HotTipsResponse> {

	private HotTipsRequest reqParam;
	
	private HotTipsResponse response;
	
	private HotTipsViewHolder viewHolder = null;
	
	private CardBean cardBean = null;
	
	/**是否正在请求数据*/
	private boolean isRunning = false;
	
	private final int NUM = 6;

	private EditText mEdit;
	
	public HotTipsModel(Context ctx,EditText e) {
		mCtx = ctx;
		mEdit = e;
	}
	
	public void setCardBean(CardBean b) {
		cardBean = b;
	}
	
	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		if (null == cancelObj || cancelObj.isCancelled()) {   
			cancelObj = null;
			try {
					try {
						List<HotTipsTable> ls = LauncherApplication.getDbManager().findAll(HotTipsTable.class);
						
						if (ls != null && ls.size() > 0) {
							if(null == response)
								response = new HotTipsResponse();
							
							response.data.list = ls;
							onResponse(response);
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
				reqParam = new HotTipsRequest();
				viewHolder.start();
				WhereBuilder wb = WhereBuilder.b("uitype", "=",IConstants.BD_HOT_TIPS_CARD_UITYPE);
				CardType hotCard = LauncherApplication.getDbManager().selector(CardType.class).where(wb).findFirst();
				if(hotCard!=null) {
					reqParam.uitype = hotCard.uitype;
					reqParam.dataCode=hotCard.dataCode;
					reqParam.addBodyParameter("uitype", reqParam.uitype);
					reqParam.addBodyParameter("dataCode", reqParam.dataCode);
					reqParam.setConnectTimeout(10*1000);
					Log.i("0000", "reqParam.getStringParams()="+reqParam.getStringParams()+"---reqParam.getBodyParams()="+reqParam.getBodyParams());
					//消息头验证和参数校验
				    if(reqParam.getBodyParams()!=null){
					    String sign = Verification.getInstance().getSign(
								reqParam.getBodyParams());
						reqParam.sign = sign;
				    }
					newHttpCallback();
					PreferencesUtils.addHeaderParam(reqParam,mCtx);
					
					Log.i("0000", "111---reqParam.getBodyParams()="+reqParam.getBodyParams());
					cancelObj = x.http().get(reqParam, httpCallback);
					}else {					
					isRunning=false;
					viewHolder.end();
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (isRunning)
			return;
		isRunning = true;
		doGet();
	}
	
	@Override
	public void doRefresh() {
		doPost();
	}
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(HotTipsResponse resp) {
		// TODO Auto-generated method stub  
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null)
				return;
			if (response == resp && (resp.data.list == null ||
					resp.data.list.size()<1)) {
				//viewHolder.itemView.setVisibility(View.GONE);
				if (mICdNotify != null)
					mICdNotify.notifyUpdate(mCdType, false);
				
				return;  
			}
			
			response.data.list = resp.data.list;
//			response.data.box.placeholders = resp.data.box.placeholders;
			
//			LeftModel.setStr(response.data.box.placeholders);
			
			viewHolder.titleTxt.setText(cardBean.cardType.name);
			viewHolder.setDatas(resp.data.list);
		}
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (HotTipsViewHolder) holder;
		viewHolder.setModel(this);
	}
	
	int page=0;
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					try {
						response = CommonUtils.getObject(result,
								HotTipsResponse.class);

						List<HotTipsTable> ls = new ArrayList<HotTipsTable>(4);
						HotTipsResponse r = new HotTipsResponse();
						if (null == response) {
							response = r;
						}

						int dex = page * NUM;
						for (int i = dex; i < NUM + dex; i++) {
							HotTipsTable hot = response.data.list.get(i);
							ls.add(hot);
						}
						page++;
						if (page >= response.data.list.size() / NUM) {
							page = 0;
						}
						r.data.list = ls;
//						r.data.box = response.data.box;
						// QueryModel.BD_HOST = response.data.box.url;
						onResponse(r);
						WhereBuilder b = WhereBuilder.b("status", "=", 1);
						LauncherApplication.getDbManager().update(ls, b,
								new String[] { "word", "sort", "url" });

					} catch (Exception e) {
						e.printStackTrace();
					}
					cancelObj = null;
					isRunning = false;
					viewHolder.end();
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					// TODO Auto-generated method stub
					isRunning = false;
//					if (response == null)
//						response = new HotTipsResponse();
//					response.code = 2;
//					response.data.list = null;
//					response.msg = ex.getMessage();
//					onResponse(response);
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
