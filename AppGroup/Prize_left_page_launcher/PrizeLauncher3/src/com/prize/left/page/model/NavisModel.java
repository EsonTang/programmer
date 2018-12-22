package com.prize.left.page.model;

import java.util.List;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.android.launcher3.LauncherApplication;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.NetNaviBean;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.NetNaviTable;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.request.InvnoNewsRequest;
import com.prize.left.page.request.NavisRequest;
import com.prize.left.page.response.NavisResponse;
import com.prize.left.page.safe.XXTEAUtil;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.MySystemProperties;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.prize.left.page.view.holder.NavisViewHolder;
/***
 * 网址导航业务类
 * @author fanjunchen
 *
 */
public class NavisModel extends BaseModel<NavisResponse> {

	private NavisRequest reqParam;
	
	private NavisResponse response;
	
	private boolean isTest = false;
	
	private String resIdentity = null;
	
	private NavisViewHolder viewHolder = null;
	
	private boolean isRunning = false;
	
	public NavisModel(Context ctx) {
		mCtx = ctx;
		
		reqParam = new NavisRequest();
	}
	
	@Override
	public void setViewHolder(RecyclerView.ViewHolder holder) {
		viewHolder = (NavisViewHolder) holder;
	}
	
	public void setResIdentity(String res) {
		resIdentity = res;
		if (!MySystemProperties.isKoobee())
			resIdentity += "_coosea";
	}

	private void doTest() {
		int resId = CommonUtils.getResourceId(mCtx, "string", resIdentity);
		if (resId != -1) {
			newHttpCallback();
			httpCallback.onSuccess(mCtx.getString(resId));
		}
	}
	@Override
	public void doGet() {
		// TODO Auto-generated method stub
		doPost();
	}

	@Override
	public void doPost() {
		// TODO Auto-generated method stub
		if (isTest) {
			doTest();
			return;
		}
		try {
			if (null == response) {
				response = new NavisResponse();
				response.data = new NetNaviBean();
			}
			response.data.list = LauncherApplication.getDbManager().selector(NetNaviTable.class).where("status", "=", 1).orderBy("_sort").findAll();
			
			if (response.data.list != null &&
					response.data.list.size() > 1 )
				onResponse(response);
//			else
//				doTest();
			
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
		new ParseNetNaviTask().execute();
	}

	@Override
	public void doRefresh() {
		// TODO Auto-generated method stub
		doPost();
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
			if (viewHolder == null || null == resp.data)
				return;
			viewHolder.setDatas(resp.data.list);
		}
	}
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					NavisResponse r;
					r = CommonUtils.getObject(result, NavisResponse.class);
					if (response == null)
						response = r;
//					if(false) 
					PreferencesUtils.putString(mCtx, IConstants.KEY_NET_NAVI_ACCESS_TIME, response.data.accessTime);
					onResponse(r);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					if (response == null)
						response = new NavisResponse();
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
		// TODO Auto-generated method stub
	}
	
	/***
	 * 请求网络导航并解析返回的数据
	 * @author fanjunchen
	 *
	 */
	class ParseNetNaviTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {
			
			boolean b = false;
			String str=null;
			try {

				reqParam =  new NavisRequest();
				reqParam.accessTime = PreferencesUtils.getString(mCtx, IConstants.KEY_NET_NAVI_ACCESS_TIME);
//				if (MySystemProperties.isKoobee()){
//					reqParam.channel = "koobee";
//					
					WhereBuilder wb = WhereBuilder.b("uitype", "=",cardBean.cardType.uitype);
					CardType navisCard = LauncherApplication.getDbManager().selector(CardType.class).where(wb).findFirst();
						if(navisCard!=null) {
						reqParam.uitype = navisCard.uitype;
						reqParam.dataCode=navisCard.dataCode;
						reqParam.addBodyParameter("uitype", reqParam.uitype);
						reqParam.addBodyParameter("dataCode", reqParam.dataCode);
						
						//消息头验证和参数校验
						if(LeftModel.bodyOk) {
							  if(reqParam.getBodyParams()!=null){
								    String sign = Verification.getInstance().getSign(
											reqParam.getBodyParams());
									reqParam.sign = sign;
							    }
						}
						PreferencesUtils.addHeaderParam(reqParam, mCtx);
						str= x.http().postSync(reqParam, String.class);
						}else {					
						isRunning=false;
						}				
				response = CommonUtils.getObject(str, NavisResponse.class);
				if(null != response) {
					if (response.code != 0)
						return b;

//					if(false) 
					PreferencesUtils.putString(mCtx, IConstants.KEY_SYNC_ACCESS_TIME, response.data.accessTime);
					if (response.data != null && response.data.list != null
							&& response.data.list.size() > 0) {
						b = true;
						LauncherApplication.getDbManager().saveOrUpdate(response.data.list);
						
						response.data.list = LauncherApplication.getDbManager().selector(NetNaviTable.class).where("status", "=", 1).orderBy("_sort").findAll();
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
	
	private CardBean cardBean=null;
	public void setCardBean(CardBean p) {
		// TODO Auto-generated method stub
		cardBean=p;
	}
}
