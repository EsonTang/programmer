package com.prize.left.page.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.android.launcher3.LauncherApplication;
import com.google.gson.Gson;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.HotBoxTable;
import com.prize.left.page.bean.table.HotWordTable;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.request.HotWordRequest;
import com.prize.left.page.response.HotWordResponse;
import com.prize.left.page.safe.XXTEAUtil;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.prize.left.page.view.holder.HotWordViewHolder;
/***
 * 百度热词业务类
 * @author fanjunchen
 *
 */
public class HotWordModel extends BaseModel<HotWordResponse> {

	private HotWordRequest reqParam;
	
	private HotWordResponse response;
	
	private HotWordViewHolder viewHolder = null;
	
	private CardBean cardBean = null;
	/**是否是第一次, 若为第一次进来则先从数据库中查询东东出来, 若数据库没有再从网络上找.*/
	private boolean isFirst = true;
	/**是否正在请求数据*/
	private boolean isRunning = false;
	
	private final int NUM = 6;

	private EditText mEdit;
	
	public HotWordModel(Context ctx,EditText e) {
		mCtx = ctx;
//		reqParam = new HotWordRequest();
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
//				if (isFirst) {
//					isFirst = false;
					try {
						List<HotWordTable> ls = LauncherApplication.getDbManager().findAll(HotWordTable.class);
//						List<HotBoxTable> bt = LauncherApplication.getDbManager().findAll(HotBoxTable.class);
//						
//						String[] arr = null;
//						if(bt!=null && bt.size()>0){ 
//							arr = bt.get(0).placeholders.split(",");
//							response.data.box.placeholders = arr;
//						}
						
						HotBoxTable ht = LauncherApplication.getDbManager().findFirst(HotBoxTable.class);
						
						String[] arr = null;
						if(ht!=null){ 
							arr = ht.placeholders.split(",");
							response.data.box.placeholders = arr;
						}
						 
						if (ls != null && ls.size() > 0) {
							if(null == response)
								response = new HotWordResponse();
							
							response.data.list = ls;
							onResponse(response);
						}
					} catch (DbException e) {
						e.printStackTrace();
					}
//				}
				

				reqParam = new HotWordRequest();
//				RequestParams reqParam = new RequestParams();
				viewHolder.start();
				WhereBuilder wb = WhereBuilder.b("uitype", "=",IConstants.BD_HOT_WD_CARD_UITYPE);
				CardType hotCard = LauncherApplication.getDbManager().selector(CardType.class).where(wb).findFirst();
				if(hotCard!=null) {
					reqParam.uitype = hotCard.uitype;
					reqParam.dataCode=hotCard.dataCode;
					reqParam.setConnectTimeout(30*1000);
					reqParam.addBodyParameter("uitype", reqParam.uitype);
					reqParam.addBodyParameter("dataCode", reqParam.dataCode);
					reqParam.setConnectTimeout(10*1000);
					Log.i("0000", "reqParam.getStringParams()="+reqParam.getStringParams()+"---reqParam.getBodyParams()="+reqParam.getBodyParams());
					//消息头验证和参数校验
//					if (LeftModel.bodyOk) {
				    if(reqParam.getBodyParams()!=null){
					    String sign = Verification.getInstance().getSign(
								reqParam.getBodyParams());
						reqParam.sign = sign;
				    }
//					}
					newHttpCallback();
					PreferencesUtils.addHeaderParam(reqParam,mCtx);
					Log.i("0000", "111---reqParam.getBodyParams()="+reqParam.getBodyParams());
					cancelObj = x.http().get(reqParam, httpCallback);
					}else {					
					isRunning=false;
					viewHolder.end();
					}
			} catch (Exception e) {
				// TODO: handle exception 
			}
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
	public void onResponse(HotWordResponse resp) {
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
			response.data.box.placeholders = resp.data.box.placeholders;
//			if(mEdit!=null)
//			mEdit.setHint(response.data.box.placeholders[1]);
			
			LeftModel.setStr(response.data.box.placeholders);
			
			// viewHolder.itemView.setVisibility(View.VISIBLE);
			viewHolder.titleTxt.setText(response.data.box.name);
			viewHolder.setDatas(resp.data.list);
		}
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (HotWordViewHolder) holder;
		
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
//					Gson gson = new Gson();
					try {
//						JSONObject jj = new JSONObject(result);
						
						response =CommonUtils.getObject(result, HotWordResponse.class);
//						jj = jj.getJSONObject("hotwords");
						
						List<HotWordTable> ls = new ArrayList<HotWordTable>(4);
						HotWordResponse r = new HotWordResponse();
						if(null == response) {
							response = r;
						} 
					
						// pengy start
					    String[] hint = response.data.box.placeholders;
					    StringBuilder hit = new StringBuilder();
					    for(int i=0; i<hint.length; i++){
					    	String s = hint[i]+",";
					    	hit = hit.append(s);
					    }
					    HotBoxTable bt = new HotBoxTable();
					    bt.name = response.data.box.name;
					    bt.url = response.data.box.url;
					    bt.placeholders = hit.toString().trim();
					    bt.id = response.data.box.id;
					    // pengy end
					    
					    int dex=	page*NUM;
						for (int i=dex; i<NUM+dex; i++) {
							HotWordTable hot = response.data.list.get(i);
							ls.add(hot);
						}
						page++;
						if(page>=response.data.list.size()/NUM) {
							page=0;
						}
						r.data.list = ls;
						r.data.box = response.data.box;
						QueryModel.BD_HOST = response.data.box.url; 
//						if (!isPause)
							onResponse(r);
//						else
//							isNeedFresh = true;
					/*	SQLiteDatabase db = LauncherApplication.getDbManager().getDatabase();
						db.beginTransaction();
						try {
							LauncherApplication.getDbManager().delete(HotWordTable.class);
							for (HotWordTable h : ls) {
								LauncherApplication.getDbManager().save(h);
							}
							db.setTransactionSuccessful();
						}
						finally {
							db.endTransaction();
						}*/
							WhereBuilder b = WhereBuilder.b("status", "=", 1);  
							LauncherApplication.
							getDbManager().update(bt, new String[]{"id","name","url","placeholders"});
							LauncherApplication.
							getDbManager().update(ls, b, new String[]{"word","sort","url"});
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
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
//						response = new HotWordResponse();
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
