package com.prize.left.page.model;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.prize.left.page.activity.WebViewActivity;
import com.prize.left.page.bean.CardBean;
import com.prize.left.page.bean.InvnoNewsItem;
import com.prize.left.page.bean.table.CardType;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.request.InvnoNewsRequest;
import com.prize.left.page.response.InvnoNewsResponse;
import com.prize.left.page.safe.XXTEAUtil;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;
import com.prize.left.page.util.Verification;
import com.prize.left.page.view.holder.InvnoNewsViewHolder;
import com.tencent.stat.StatService;
/***
 * 英威诺业务类
 * @author fanjunchen
 *
 */
public class InvnoNewsModel extends BaseModel<InvnoNewsResponse> {
	
	private InvnoNewsRequest reqParam;

	private InvnoNewsResponse response;
	
	private InvnoNewsViewHolder viewHolder = null;
	/**页码*/
	private int pageIndex = 0;
	/**页大小*/
	private final int pageSize = 3;
	/**信息数据回调接口对象*/
//	private DownloadCallback<FlowNews> bdCallback = null;
	
	/**广告数据回调接口对象*/
	/*private DownloadCallback<FlowAd> adCallback = null;*/
	/**是否正在请求*/
	private boolean isRunning = false;
	
//	private boolean isAdRunOver = false;
//	
//	private boolean isFlowRunOver = false;
	
//	private PiflowInfoManager infoManager;
	
//	private final String MORE_URL = "http://h5chn.hotoday.cn/?app=8214d701-b92-h5";//"http://h5chn.hotoday.cn/";
	
	private final String APPID = "20";
	
	private final String ADIDS = "115";//,116
	
//	private FlowAd mTmpAd = null;
	
	private String channelKey = null;
	
//	private AdApiMgr adMgr = null;
	/**是否需要开启广告*/
	private boolean isNeedOpenAd = com.prize.left.page.AppConfig.IS_OPEN_INVNO_AD;
	
	public InvnoNewsModel(Context ctx) {
		mCtx = ctx;
		reqParam=new InvnoNewsRequest();
	}
	
	private CardBean cardBean = null;
	public void setCardBean(CardBean b) {
		cardBean = b;
	}
	
//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Override
	public void doGet() {
		if (null == cancelObj || cancelObj.isCancelled()) {
			reqParam =  new InvnoNewsRequest();
			cancelObj = null;
		viewHolder.start();
		try {
			WhereBuilder wb = WhereBuilder.b("dataCode", "=",cardBean.cardType.dataCode).and("subCode","=",cardBean.cardType.subCode);
				CardType newsCard = LauncherApplication.getDbManager()
						.selector(CardType.class).where(wb).findFirst();
				if (newsCard != null) {
					reqParam.uitype = newsCard.uitype;
					reqParam.dataCode = newsCard.dataCode;
					reqParam.addBodyParameter("uitype", reqParam.uitype);
					reqParam.addBodyParameter("dataCode", reqParam.dataCode);
					reqParam.setConnectTimeout(10*1000);
					// 消息头验证和参数校验
					if (LeftModel.bodyOk) {
						  if(reqParam.getBodyParams()!=null){
							    String sign = Verification.getInstance().getSign(
										reqParam.getBodyParams());
								reqParam.sign = sign;
						    }
					}

					newHttpCallback();
					PreferencesUtils.addHeaderParam(reqParam, mCtx);
					cancelObj = x.http().get(reqParam, httpCallback);
				} else {
					isRunning = false;
					viewHolder.end();
				}
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		pageIndex =0;
		doGet();
	}
	/***
	 * 获取下一页数据
	 */
	public void getNextPage() {
			if (isRunning)
				return;
			isRunning = true;

			doGet();
			//统计刷新
			StatService.trackCustomEvent(mCtx, "CardNews", "");

	}
	
	private Properties mProp = new Properties();
	/***
	 * 跳转到某个团购详情
	 * @param data
	 */
	public void jumpDetail(InvnoNewsItem data, int pos) {
		if (data.getSurl().startsWith("http")){
			Intent it = new Intent(mCtx, WebViewActivity.class);
			it.putExtra(WebViewActivity.P_URL, data.getSurl());
			mCtx.startActivity(it);
			it = null;
		}
		//统计位置
		mProp.clear();
		mProp.setProperty("position", String.valueOf(pos+1));
		StatService.trackCustomKVEvent(mCtx, "CardNews", mProp);
	}
	/***
	 * 更多资讯
	 * @param channelId
	 */
	public void moreNews(String channelId) {
		if (cardBean.cardType.moreType==1) {
			Intent it = new Intent(mCtx, WebViewActivity.class);
			it.putExtra(WebViewActivity.P_URL, cardBean.cardType.moreUrl);
			mCtx.startActivity(it);
			it = null;
		}
		//统计更多
		mProp.clear();
		mProp.setProperty("isMore", "点击了更多");
		StatService.trackCustomKVEvent(mCtx, "CardNews", mProp);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		if (null != cancelObj)
			cancelObj.cancel();
	}

	@Override
	public void onResponse(InvnoNewsResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (viewHolder == null)
				return;
			
			if (response == resp && (resp.data == null ||
					resp.data.list.size()<1)) {
				//viewHolder.itemView.setVisibility(View.GONE);
				if (mICdNotify != null)
					mICdNotify.notifyUpdate(mCdType, false);
				return;
			}
			
			response.data = resp.data;
			visible();
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
		viewHolder = (InvnoNewsViewHolder) holder;
		
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
						InvnoNewsResponse tmp = CommonUtils.getObject(result, InvnoNewsResponse.class);
						List<InvnoNewsItem> newsdatas=tmp.data.list;
						InvnoNewsResponse newsinfos=new InvnoNewsResponse();
						if (response == null){
							response = newsinfos;
						}
						int start=pageSize*pageIndex;
						if (response!=null) {
							if(newsdatas.size()>pageSize){
								newsinfos.data.list=newsdatas.subList(start, start+pageSize);
							}else{
								newsinfos.data.list=newsdatas;
							}
							onResponse(newsinfos);

							pageIndex ++;
							
							if(pageIndex>=newsdatas.size()/pageSize) {
								pageIndex=0;
							}
					}
						isRunning = false;
						cancelObj=null;
						viewHolder.end();					
					}
				}
				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					cancelObj=null;
					isRunning = false;
				    viewHolder.end();
					
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
