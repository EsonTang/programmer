/*package com.prize.left.page.model;

import org.xutils.x;
import org.xutils.common.Callback.CommonCallback;

import android.content.Context;
import android.os.Bundle;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.R;
import com.baidu.location.BDLocation;
import com.baidu.xlife.ConfigConstant;
import com.baidu.xlife.ILifeCallback;
import com.baidu.xlife.LifeClient;
import com.baidu.xlife.LifeException;
import com.baidu.xlife.LifeRuntimeConfig;
import com.baidu.xlife.LocationInfo;
import com.prize.left.page.request.BDGroupRequest;
import com.prize.left.page.request.BDMovieRequest;
*//***
 * 百度糯米生活服务工具类
 * @author fanjunchen
 *
 *//*
public class BDXLifeUtil {

	private LifeClient mLifeClient;
	
	private LifeRuntimeConfig mLifeRuntimeConfig;
	
	private static BDXLifeUtil instance;
	
	private boolean isInit = false;
	
	private LocationInfo locInfo;
	
	private BDXLifeUtil() {
		mLifeClient = LifeClient.getInstance();
        mLifeRuntimeConfig = new LifeRuntimeConfig();
	}
	
	public static BDXLifeUtil getInstance() {
		if (instance == null) {    
            synchronized (BDXLifeUtil.class) {    
               if (instance == null) {    
            	   instance = new BDXLifeUtil();   
               }    
            }    
        }    
        return instance;
	}
	
	public void init(Context ctx) {
		try {
			
			if (isInit) {
				BDLocation loc = LauncherApplication.getInstance().getLoc();
				if (loc != null) {
					LocationInfo info = new LocationInfo(loc .getLatitude() + "", loc.getLongitude() + "");
					info.setCityId(loc.getCityCode());
					info.setCityName(loc.getCity());
		            mLifeClient.setLocationInfo(info);
				}
				return;
			}
			isInit = true;
			openHtmlProgressBar();
			// openHtmlExitView();
			closeHtmlExitView();
			locInfo = new LocationInfo("", "");
			mLifeClient.init(ctx, null);
			initTimeOut();
			
			BDLocation loc = LauncherApplication.getInstance().getLoc();
			if (loc != null) {
				locInfo.setLat(String.valueOf(loc.getLatitude()));
				locInfo.setLng(String.valueOf(loc.getLongitude()));
				locInfo.setCityId(loc.getCityCode());
				locInfo.setCityName(loc.getCity());
	            mLifeClient.setLocationInfo(locInfo);
			}
		} catch (LifeException e) {
			isInit = false;
			e.printStackTrace();
		}
	}
	
	
	public void initTimeOut() {
		// 设定5秒网络超时
		Bundle param = new Bundle();
		param.putString(ConfigConstant.KEY_ACTION, ConfigConstant.ACTION_SET_PARAMETERS);
		param.putInt(ConfigConstant.KEY_PARAM_NETWORK_TIMEOUT, 5 * 1000);
		try {
			if(mLifeClient!=null)
		    mLifeClient.execute("engine", param, null);
		} catch (LifeException e) {
		    e.printStackTrace();
		}
	}
	
	public void setLocInfo() {
		if (!isInit)
			return;
		BDLocation loc = LauncherApplication.getInstance().getLoc();
		if (loc != null) {
			locInfo.setLat(String.valueOf(loc.getLatitude()));
			locInfo.setLng(String.valueOf(loc.getLongitude()));
			locInfo.setCityId(loc.getCityCode());
			locInfo.setCityName(loc.getCity());
            mLifeClient.setLocationInfo(locInfo);
		}
	}
	
	public void close() {
		if (mLifeClient != null)
			mLifeClient.close();
		instance = null;
		locInfo = null;
		mLifeClient = null;
		isInit = false;
	}

	public void openHtmlProgressBar() {
        Bundle bundle = new Bundle();
        bundle.putInt(LifeRuntimeConfig.KEY_LAYOUT_ID, R.layout.progress_bar);
        bundle.putInt(LifeRuntimeConfig.KEY_VIEW_ID, R.id.progressbar);
        mLifeRuntimeConfig.open(LifeRuntimeConfig.OPTION_HTML_PROGRESS_BAR, bundle);
        mLifeClient.setLifeRuntimeConfig(mLifeRuntimeConfig);
    }

	public void closeHtmlProgressBar() {
        mLifeRuntimeConfig.close(LifeRuntimeConfig.OPTION_HTML_PROGRESS_BAR);
        mLifeClient.setLifeRuntimeConfig(mLifeRuntimeConfig);
    }
    
	public void openHtmlExitView() {
        Bundle param = new Bundle();
        param.putInt(LifeRuntimeConfig.KEY_LAYOUT_ID, R.layout.html_exit_icon_view);
        param.putInt(LifeRuntimeConfig.KEY_VIEW_ID, R.id.iamge);
        mLifeRuntimeConfig.open(LifeRuntimeConfig.OPTION_HTML_QUICK_EXIT_VIEW, param);
        mLifeClient.setLifeRuntimeConfig(mLifeRuntimeConfig);
    }
    
	public void closeHtmlExitView() {
        mLifeRuntimeConfig.close(LifeRuntimeConfig.OPTION_HTML_QUICK_EXIT_VIEW);
        mLifeClient.setLifeRuntimeConfig(mLifeRuntimeConfig);
    }
	*//***
	 * 获取正在上映的电影列表数据
	 * @param cityId
	 * @param page 起始页
	 * @param pageSize 页大小
	 * @param callback 回调接口
	 *//*
	public void getMovie(BDMovieRequest reqParam,
			CommonCallback<String> httpCallback) {
		x.http().get(reqParam, httpCallback);
		
	}
	public void getMovie(String cityId, int page, int pageSize, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, MovieConfigConstant.ACTION_OPERATE_ONSHOW_MOVIE_LIST);
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_GET_DATA);
        bundle.putString(MovieConfigConstant.KEY_PAGE_NO, String.valueOf(page));
        bundle.putString(MovieConfigConstant.KEY_PAGE_SIZE, String.valueOf(pageSize));
        bundle.putInt(ConfigConstant.KEY_PARAM_NETWORK_TIMEOUT, 10 * 1000);
        try {
            mLifeClient.execute("movie", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	*//***
	 * 点击电影卡片更多时处理
	 * @param cityId
	 * @param callback 回调接口
	 *//*
	public void moreMovie(String cityId, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, MovieConfigConstant.ACTION_OPERATE_MOVIE_PAGE);
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_SHOW_VIEW);
        try {
            mLifeClient.execute("movie", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	*//***
	 * 跳转到某个电影详情页
	 * @param cityId
	 * @param providerId 来源
	 * @param movieId 电影ID
	 * @param callback 回调接口
	 *//*
	public void jumpSingleMovie(String cityId, String providerId, String movieId, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, MovieConfigConstant.ACTION_OPERATE_MOVIE_DETAIL);
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_SHOW_VIEW);
        bundle.putString(ConfigConstant.KEY_PROVIDER_ID, providerId);
        bundle.putString(MovieConfigConstant.KEY_MOVIE_ID, movieId);
        try {
        	if(mLifeClient==null) {
        		mLifeClient=LifeClient.getInstance();
        	}
            mLifeClient.execute("movie", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	*//***
	 * 获取团购列表数据
	 * @param cityId
	 * @param page 起始页
	 * @param pageSize 页大小
	 * @param callback 回调接口
	 *//*
	public void getGroupList(BDGroupRequest reqParam,
			CommonCallback<String> httpCallback) {
		// TODO Auto-generated method stub
		x.http().get(reqParam, httpCallback);
	}
	public void getGroupList(String cityId, int page, int pageSize, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, "getDealList");
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_GET_DATA);
        bundle.putString("data_type", "json");
        BDLocation loc = LauncherApplication.getInstance().getLoc();
		if (loc != null) {
			bundle.putString("lat", String.valueOf(loc.getLatitude()));
			bundle.putString("lng", String.valueOf(loc.getLongitude()));
			bundle.putString("radius", String.valueOf(5000));
		}
        BDLocation loc = LauncherApplication.getInstance().getLoc();
		if (loc != null && locInfo != null) {
			locInfo.setLat(String.valueOf(loc.getLatitude()));
			locInfo.setLng(String.valueOf(loc.getLongitude()));
            mLifeClient.setLocationInfo(locInfo);
            bundle.putString("radius", String.valueOf(5000));
		}
		bundle.putString("sort", "0");
        bundle.putString(MovieConfigConstant.KEY_PAGE_NO, String.valueOf(page));
        bundle.putString(MovieConfigConstant.KEY_PAGE_SIZE, String.valueOf(pageSize));
        try {
            mLifeClient.execute("groupon", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	*//***
	 * 点击团购更多时处理
	 * @param cityId
	 * @param callback 回调接口
	 *//*
	public void moreGroup(String cityId, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, "showGrouponHomePage");//"getDealList");
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_SHOW_VIEW);
        bundle.putString("data_type", "url");
        //bundle.putString("sort", "5");//距离近优先
        try {
            mLifeClient.execute("groupon", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	*//***
	 * 点击外卖更多时处理
	 * @param cityId
	 * @param callback 回调接口
	 *//*
	public void moreTakeaway(String cityId, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, "showTakeAwayPage");
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_SHOW_VIEW);
        bundle.putString("data_type", "url");
        bundle.putString("sort", "5");//距离近优先
        try {
            mLifeClient.execute("takeaway", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	*//***
	 * 跳转到某个团购详情页
	 * @param keyword 店名
	 * @param providerId 来源
	 * @param shopId 店铺ID
	 * @param callback 回调接口
	 *//*
	public void jumpSingleGroup(int dealId, String providerId, String shopId, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, "getDealDetail");
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_SHOW_VIEW);
        bundle.putString("data_type", "url");
        //bundle.putString("keyword", keyword);
        bundle.putString("deal_id", String.valueOf(dealId));
        bundle.putString(ConfigConstant.KEY_PROVIDER_ID, providerId);
        bundle.putString("shop_id", shopId);
        try {
        	if(mLifeClient==null) {
        		mLifeClient=LifeClient.getInstance();
        	}
            mLifeClient.execute("groupon", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	*//***
	 * 手机充值首页
	 * @param cityId
	 * @param callback 回调接口
	 *//*
	public void toRechargePage(String cityId, ILifeCallback callback) {
		Bundle bundle = new Bundle();
        bundle.putString(ConfigConstant.KEY_ACTION, "showPrepaidRechargeView");
        bundle.putString(ConfigConstant.KEY_CITY_ID, cityId);
        bundle.putString(ConfigConstant.KEY_OPTION, ConfigConstant.OPTION_SHOW_VIEW);
        //bundle.putString("data_type", "url");
        try {
            mLifeClient.execute("prepaidrecharge", bundle, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
*/