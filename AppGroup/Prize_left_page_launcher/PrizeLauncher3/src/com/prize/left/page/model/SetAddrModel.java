package com.prize.left.page.model;

import org.xutils.x;
import org.xutils.common.Callback;

import android.content.Context;

import com.android.launcher3.LauncherApplication;
import com.prize.left.page.adapter.AddrAdapter;
import com.prize.left.page.bean.AddrBean;
import com.prize.left.page.bean.table.NormalAddrTable;
import com.prize.left.page.bean.table.PersonTable;
import com.prize.left.page.request.BaiduPlaceRequest;
import com.prize.left.page.response.BaiduPlaceResponse;
import com.prize.left.page.util.CommonUtils;
import com.prize.left.page.util.DBUtils;
/***
 * 设置常用位置业务类
 * @author fanjunchen
 *
 */
public class SetAddrModel extends BaseModel<BaiduPlaceResponse> {

	private BaiduPlaceRequest reqParam;
	
	private BaiduPlaceResponse response;
	
	private boolean isTest = false;
	/**测试时的资源ID*/
	private String resIdentity = null;
	/**1 刷新住址, 2 刷新公司地址*/
	private int freshType = 0;
	
	private AddrAdapter homeAdapter, companyAdapter;
	
	private String locStr = null;
	
	private AddrBean homeAddr, companyAddr;
	/**当前位置*/
	private AddrBean curAddr;
	/**保存好的地址(家里和公司)**/
	private NormalAddrTable addrBean;
	
	public SetAddrModel(Context ctx) {
		mCtx = ctx;
		
		LauncherApplication app = (LauncherApplication)LauncherApplication.getInstance();
		locStr = CommonUtils.getLocQueryStr(app.getLoc());
		
		reqParam = new BaiduPlaceRequest();
		if (null == locStr)
			locStr = CommonUtils.getLocQueryStr(app.getLoc());
		reqParam.location = locStr;
		curAddr = CommonUtils.locToAddrBean(app.getLoc());
		if (curAddr != null)
			reqParam.region = curAddr.cityid;
		String userId = "";
		PersonTable p = LauncherApplication.getInstance().getLoginPerson();
		if (p != null) {
			userId = p.userId;
		}
		addrBean = DBUtils.getAddr(userId);
	}
	/**
	 * 可以不设置, 会自动新建一个
	 * @param req
	 */
	public void setRequest(BaiduPlaceRequest req) {
		reqParam = req;
	}
	
	public void setAddr(AddrBean ab) {
		if (freshType == 1) {
			homeAddr = ab;
		}
		else
			companyAddr = ab;
	}
	
	public NormalAddrTable getAddr() {
		return addrBean;
	}
	
	public void setQuery(String q) {
		if (reqParam != null)
			reqParam.setQ(q);
	}
	
	public void setHomeAdapter(AddrAdapter req) {
		homeAdapter = req;
		homeAdapter.setModel(this);
		homeAdapter.setCurAddr(curAddr);
	}
	
	public void setCompanyAdapter(AddrAdapter req) {
		companyAdapter = req;
		companyAdapter.setModel(this);
		companyAdapter.setCurAddr(curAddr);
	}
	
	/***
	 * 设置刷新类型 <br>1 刷新住址, 2 刷新公司地址
	 */
	public void setFreshType(int t) {
		freshType = t;
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
	/***
	 * 同步请求
	 * @return
	 */
	public BaiduPlaceResponse doGetSync() {
		try {
			/*if (MySystemProperties.isKoobee())*/
				reqParam.addHeader("KOOBEE", "dido");
			/*else 
				reqParam.addHeader("KOOBEE", "coosea");*/
			String result = x.http().getSync(reqParam, String.class);
			
			response = CommonUtils.getObject(result, BaiduPlaceResponse.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return response;
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
	public void onResponse(BaiduPlaceResponse resp) {
		// TODO Auto-generated method stub
		if (callback != null)
			callback.onResponse(resp);
		else {
			// 可以处理刷新UI的事情
			if (resp.status == 0) {
				switch (freshType) {
					case 1:
						homeAdapter.setDatas(resp.result);
						break;
					case 2:
						companyAdapter.setDatas(resp.result);
						break;
				}
			}
		}
	}
	
	@Override
	protected void newHttpCallback() {
		if (null == httpCallback) {
			httpCallback = new Callback.CommonCallback<String>() {

				@Override
				public void onSuccess(String result) {
					response = CommonUtils.getObject(result, BaiduPlaceResponse.class);
					onResponse(response);
				}

				@Override
				public void onError(Throwable ex, boolean isOnCallback) {// 加载出错
					response = new BaiduPlaceResponse();
					response.status = 2;
					response.message = ex.getMessage();
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

	/***
	 * 保存地址到数据库
	 * @return
	 */
	public boolean saveAddr(String home,String company) {
		if (addrBean == null)
			addrBean = new NormalAddrTable();
		if (companyAddr != null && null!=companyAddr.location) {
			addrBean.companyAddr = companyAddr.name;
			addrBean.companyLan = String.valueOf(companyAddr.location.lat);
			addrBean.companyLon = String.valueOf(companyAddr.location.lng);
		}else {
			addrBean.companyAddr = company;
		}
		
		if (homeAddr != null) {
			addrBean.homeAddr = homeAddr.name;
			try{
				addrBean.homeLan = String.valueOf(homeAddr.location.lat);
				addrBean.homeLon = String.valueOf(homeAddr.location.lng);
			}catch(Exception e){
				
			}
		}else {

			addrBean.homeAddr = home;
		}
		
		return DBUtils.updateAddr(addrBean);
	}
	@Override
	public void doBindImg() {
		
	}
}
