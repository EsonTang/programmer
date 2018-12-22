/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.music.activities;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.prize.app.beans.ClientInfo;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.InstallResultCallBack;
import com.prize.app.util.PackageUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.music.R;
import com.prize.music.admanager.Configs;
import com.prize.music.admanager.bean.AdCommonInfo;
import com.prize.music.admanager.bean.AdDetailsInfo;
import com.prize.music.admanager.bean.AdOutInfo;
import com.prize.music.admanager.presenter.AdJumpManager;
import com.prize.music.admanager.presenter.AdNetManager;
import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.StatService;
import com.prize.music.helpers.utils.PreferencesUtils;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import rx.android.widget.OnTextChangeEvent;

/**
 * 
 **
 * 欢迎界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class WelcomeActivity extends BaseActivity {
	private final String TAG = "huang-WelcomeActivity";
	// private AdDetailsInfo mAdDetailsInfo;
	private FrameLayout mView;
	private TextView mHide,mTitle,mSource;
	private ImageView mSplashIv, mDefView;
	private Cancelable reqHandler;
	private boolean isRun = false;
	private boolean isGetData = false;
	private WebView mWebView;
	private boolean isShowAd = false;// 判断ad是否已经展示
	/** 默认页展示的时间 */
	private static final long DEFAULT_DEALY = 3 * 1000;
	/** 广告展示的时间 */ 
	private static final int AD_SHOW_TIME = 5 * 1000;
	/** 广告展示间隔 */
	// private static final int AD_SHOW_PERIOD = 30 * 60 * 1000;
	/** 广告展示间隔(测试) */
	private static final int AD_SHOW_PERIOD = 5 * 1000;

	private CountDownTimer mTimer;
	private static final String SPLASH_ONCLICK_EVENT = "music_splash_onclick";
	private static final String SPLASH_SHOW_EVENT = "music_splash_show";	
	protected static final int MSG_NETINFO = 1;

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NETINFO:
				AdOutInfo data = (AdOutInfo) msg.obj;
				loadAd(data);
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		initView();
		initNet();
	}

	private void initView() {
		getIp();
		//getUA();
		mView = (FrameLayout) findViewById(R.id.ad_splash_v);
		mDefView = (ImageView) findViewById(R.id.loading_img);
		mHide = (TextView) findViewById(R.id.ad_splash_clock);
		mTitle = (TextView) findViewById(R.id.ad_splash_title);
		mSource = (TextView) findViewById(R.id.ad_splash_source);
		mSplashIv = (ImageView) findViewById(R.id.ad_splash_iv);
	
	}

	private void initNet() {
		isShowAd = false;
		//mHandler.postDelayed(mTimerOutTask, DEFAULT_DEALY);
		mDefTimer.start();
		getNetAd();
		
		
	}
	

	CountDownTimer mDefTimer = new CountDownTimer(DEFAULT_DEALY, 1000) {

		@Override
		public void onTick(long millisUntilFinished) {
			int num = (int) ((millisUntilFinished / 1000) - 1);
//			if (mHide != null)
//				mHide.setText("跳过 "+num + "s");
			JLog.i(TAG, "onDefTick:--" + num + "---");				
		}

		@Override
		public void onFinish() {
			if (!isShowAd) {
				hide(true);
			}
		}
	};

	Runnable mTimerOutTask = new Runnable() {

		@Override
		public void run() {
			if (!isShowAd) {
				hide(true);
			}
		}
	};

	/**
	 * 跳转到音乐主页
	 * 
	 * @param isToMain
	 */
	private void hide(Boolean isToMain) {
		if (isToMain) {		
			putIp();
			Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
			startActivity(intent);			
			finish();
		}
	}
	
	private void putIp() {		
		ClientInfo info = ClientInfo.getInstance();
		String ip = info.ip;
		if (ip!=null) 				
		PreferencesUtils.putString(getApplicationContext(), Configs.SPLASH_IP_SP_KEY, ip);
	}
	
	private void getIp() {
		ClientInfo info = ClientInfo.getInstance();
		if (info.ip==null) {
			String ip = PreferencesUtils.getString(getApplicationContext(), Configs.SPLASH_IP_SP_KEY);		
			if (ip!=null && info.ip==null) 
				info.ip=ip;
		}		
	}

	// private void fadeToOnkeyActivity(ArrayList<AppsItemBean> itemBeans) {
	// JLog.i(TAG, "fadeToMainActivity");
	// Intent intent = new Intent(this, MainActivity.class);
	// intent.putExtra("datas", (Serializable) itemBeans);
	// startActivity(intent);
	// finish();
	// }
	// private void requestSDKIMG() {
	// if(ClientInfo.networkType==ClientInfo.NONET)
	// return ;
	// BannerTask task = new BannerTask(xiamiSDK,
	// RequestMethods.MOBILE_SDK_IMAGE, bannerHandler);
	// HashMap<String, Object> params = new HashMap<String, Object>();
	// params.put("show_h5", false);
	// task.execute(params);
	// }
	// private Handler bannerHandler = new Handler() {
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case RequestResCode.REQUEST_OK:
	// Gson gson = requestManager.getGson();
	// JsonElement element = (JsonElement) msg.obj;
	// BannerResponse = gson.fromJson(element,
	// BannerResponse.class);
	// initDailySongs();
	// break;
	// case RequestResCode.REQUEST_FAILE:
	// break;
	// case RequestResCode.REQUEST_EXCEPTION:
	// break;
	// }
	// };
	// };
	//
	// /**
	// * 请求推荐歌单
	// *
	// * @return void
	// * @see
	// */
	// void initDailySongs() {
	// BannerTask task = new BannerTask(xiamiSDK,
	// RequestMethods.RECOMMEND_DAILY_SONGS, dailyHandler);
	// HashMap<String, Object> params = new HashMap<String, Object>();
	// params.put("limit", 30);
	// task.execute(params);
	// }
	// private Handler dailyHandler = new Handler() {
	//
	// public void handleMessage(Message msg) {
	//
	// switch (msg.what) {
	// case RequestResCode.REQUEST_OK:
	// Gson gson = requestManager.getGson();
	// JsonElement element = (JsonElement) msg.obj;
	// RecomendHotSongsResponse = gson.fromJson(element,
	// RecomendHotSongsResponse.class);
	//
	// break;
	// case RequestResCode.REQUEST_FAILE:
	// break;
	// case RequestResCode.REQUEST_EXCEPTION:
	// break;
	// }
	// };
	// };
	@Override
	public void onBackPressed() {
		return;
		// super.onBackPressed();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		if (reqHandler != null)
			reqHandler.cancel();
		reqHandler = null;
		if (mTimer != null)
			mTimer.cancel();
		mTimer = null;
		finish();
		super.onDestroy();
	}

	/*** 请求广告 */
	public void getNetAd() {
		if (isRun)
			return;
		isRun = true;
		isGetData = false;
		long newTime = System.currentTimeMillis();
		long time = PreferencesUtils.getLong(this, Configs.SP_TIME_KEY);
		JLog.i(TAG, "ad_period_time:" + (newTime - time) / 1000);
		if (Math.abs(newTime - time) <= AD_SHOW_PERIOD)
			return;
		RequestParams params = new RequestParams(Configs.NET_URL); 
		params.setMaxRetryCount(0);//请求失败不重复请求
		String ua = getUA();
		params.addHeader("User-Agent",ua);
		reqHandler = XExtends.http().get(params, new CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				isRun = false;
				try {
					Log.i(TAG, " result: " + result.toString());
					JSONObject obj = new JSONObject(result);
					if (obj.getInt("code") == 0 && obj.getString("data") != null) {
						AdCommonInfo packageNamesInfo = new Gson().fromJson(result, AdCommonInfo.class);
						if (packageNamesInfo != null) {
							AdOutInfo data = packageNamesInfo.data;
							isGetData = true;
							Message msg = Message.obtain();
							msg.what = MSG_NETINFO;
							msg.obj = data;
							mHandler.sendMessage(msg);

						}
					}
				} catch (JSONException e) {
					JLog.i(TAG, "e.printStackTrace():" + e.toString());
					e.printStackTrace();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				isRun = false;
				JLog.i(TAG, "onError.e:" + ex.toString());
				ex.printStackTrace();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				isRun = false;
			}

			@Override
			public void onFinished() {
				isRun = false;
			}
		});

	}

	/** 加载成功时候展示广告 2017.5.25 */
	public void show(String source) {
		isShowAd = true;
		try {
			mDefTimer.cancel();			
		} catch (Exception e) {			
		}
		// 展示
		// if (mView != null && mView.getVisibility() == View.GONE) {
		// mView.setVisibility(View.VISIBLE);
		// mDefView.setVisibility(View.GONE);
		// }
		if (mHide.getVisibility() == View.GONE)
			mHide.setVisibility(View.VISIBLE);
		if (mTitle.getVisibility() == View.GONE)
			mTitle.setVisibility(View.VISIBLE);
		if (mSource.getVisibility() == View.GONE)
			mSource.setVisibility(View.VISIBLE);
		
		if (source!=null) 
		mSource.setText(source);
		
		mTimer = new CountDownTimer(AD_SHOW_TIME, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				int num = (int) ((millisUntilFinished / 1000) - 1);
				if (mHide != null)
					mHide.setText("跳过 "+num + "s");
				JLog.i(TAG, "onTick:--" + num + "---" + millisUntilFinished);
				if (num <= 0) {
					hide(true);
				}
			}

			@Override
			public void onFinish() {
				// hide(true);
			}
		};
		mTimer.start();

	}
	public static boolean isOwnSource=false;
	public void loadAd(AdOutInfo data) {
		if (data == null || data.adBanner == null || data.adDetails == null)
			return;
		final String bannerUrl = data.adBanner;
		final String id = data.id;
		final AdDetailsInfo adDetails = data.adDetails;
		final String adSource = data.adSource;
		JLog.i(TAG, "start loadAd!");	
		if (adSource!=null && adSource.contains("prize")) 
			isOwnSource=true;
		else 
			isOwnSource=false;
		
		JLog.i(TAG, adDetails.toString());
		// Target<GlideDrawable> into =
		// Glide.with(context).load(bannerUrl).into(mSlotIv);
		try {

			// 监听图片加载是否失败
			Glide.with(this).load(bannerUrl)
					// .preload()
					.placeholder(R.drawable.welcome_logo)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.into(new GlideDrawableImageViewTarget(mSplashIv) {
						@Override
						public void onResourceReady(GlideDrawable arg0, GlideAnimation<? super GlideDrawable> arg1) {
							super.onResourceReady(arg0, arg1);
							try {

								JLog.i(TAG, "SlotView-show:onResourceReady!");
								// 记录广告展示的时间
								PreferencesUtils.putLong(getApplicationContext(), Configs.SP_TIME_KEY,
										System.currentTimeMillis());
								// 展示统计
								
								sendCallBack(SPLASH_SHOW_EVENT, id,adDetails.impr_url);
								
								new Handler().post(new Runnable() {
									@Override
									public void run() {
										show(adSource);
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onLoadFailed(Exception e, Drawable errorDrawable) {
							super.onLoadFailed(e, errorDrawable);
							JLog.i(TAG, "SlotView-show:onLoadFailed!");
							// 统计
							reLoadView(bannerUrl, id, adDetails,adSource);
							// hide();

						}
					});
			// mSlotIv.setOnTouchListener(this);
			mSplashIv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {						
				 onClickEvent(adDetails, id);
				}
			});
			
			mSplashIv.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						 downX = event.getRawX();
						 downY = event.getRawY();
						JLog.i(TAG, "downX:"+downX+" downY"+downY);			 
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						 upX = event.getRawX();
						 upY = event.getRawY();
						 JLog.i(TAG, "upX:"+upX+" upY"+upY);
						 //onClickEvent(adDetails, id);
						break;
					}
					return false;
				}
			});
			mHide.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {					
					// 去除重复的广告、展示成功后一段时间后再展示
					hide(true);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void reLoadView(String url, final String id,final AdDetailsInfo adDetails,final String source) {
		Glide.with(this)
			 .load(url)
			 .into(new SimpleTarget<GlideDrawable>() {
			@Override
			public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
				try {
					mSplashIv.setImageDrawable(resource);
					// 统计
					sendCallBack(SPLASH_SHOW_EVENT, id,adDetails.impr_url);
					// 记录广告展示的时间
					PreferencesUtils.putLong(getApplicationContext(), Configs.SP_TIME_KEY, System.currentTimeMillis());
					show(source);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onLoadFailed(Exception e, Drawable errorDrawable) {
				super.onLoadFailed(e, errorDrawable);
				// hide(true);

			}
		});
	}

	private float downX;
	private float downY;
	private float upX;
	private float upY;
	
	private ArrayList<String> setLoc(ArrayList<String> urls,float dx,float dy,float ux,float uy) {	
		ArrayList<String> dealedUrls = new ArrayList<>();
		try {
			
			for (String url : urls) {		
				url=url.replace("IT_CLK_PNT_DOWN_X",String.valueOf(dx))
					   .replace("IT_CLK_PNT_DOWN_Y", String.valueOf(dy))
					   .replace("IT_CLK_PNT_UP_X", String.valueOf(ux))
					   .replace("IT_CLK_PNT_UP_Y", String.valueOf(uy));
				dealedUrls.add(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dealedUrls;
	}
	
	private void onClickEvent(final AdDetailsInfo adDetails, String id) {
		isShowAd = true;
//		try {
//			mDefTimer.cancel();			
//		} catch (Exception e) {			
//		}
		
		if (adDetails == null || adDetails.detailType == -1 || adDetails.jumpUrl == null) {
			hide(true);
			return;
		}
		// 统计
		ArrayList<String> click_urls = adDetails.click_url;	
		//保留一位小数
		float dx=(float)(Math.round(downX*10))/10;	
		float dy=(Math.round(downY*10))/10;
		float ux=(Math.round(upX*10))/10;
		float uy=(Math.round(upY*10))/10;
		
		ArrayList<String> clickUrls = setLoc(click_urls, dx, dy, ux, uy);

		JLog.i(TAG,"dx:"+dx+" dy:"+dy+" ux:"+ux+" uy:"+uy+" clickUrls.size: "+clickUrls.size());	
		sendCallBack(SPLASH_ONCLICK_EVENT, id,clickUrls);
	
		int detailTypeKey = adDetails.detailType;
		String jumpUrl = adDetails.jumpUrl;
		String apkName = adDetails.apkName;
		String netPckgName = adDetails.packageName;
		String appId = adDetails.appId;
		AppsItemBean appInfo = adDetails.appInfo;
		String isDeep = adDetails.apkMd5;
		final String name=apkName;	
		JLog.i(TAG, "detailTypeKey:" + detailTypeKey + " apkName:" + apkName + " netPckgName:" + netPckgName
				+ " jumpUrl:" + jumpUrl + " appId:" + appId);
		switch (detailTypeKey) {
		case 1:
			// 跳转URL
			if (mTimer != null) // BUG:34187
				mTimer.cancel();
			if (isDeep!=null) {
				try {		
					Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
					startActivity(intent);	
					
					Intent i = new Intent();
					i.setData(Uri.parse(isDeep));
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);									
					getApplicationContext().startActivity(i );
					JLog.i(TAG,"Open App !");
				} catch (Exception e) {
					JLog.i(TAG, "deeplink-OpenApp Failed :" +e.toString() );
					if (jumpUrl != null)
						AdJumpManager.onJumpAdWebActivity(WelcomeActivity.this, jumpUrl);	
				}
	
			}else {
				if (jumpUrl != null)
				AdJumpManager.onJumpAdWebActivity(WelcomeActivity.this, jumpUrl);			  
			}
			finish();
			break;
		case 2:
			// 跳转下载    存在的问题，子线程下载，回调。会有一定的内存泄漏
			try {
				if (mTimer != null) // BUG:34187
					mTimer.cancel();
				//统计
				sendCallBack(null, null, adDetails.inst_downstart_url);	
				Boolean isApkExist = AdNetManager.checkApkIsExist(this, netPckgName, apkName);
				JLog.i(TAG, "downloadApk-isApkExist:" + isApkExist);
				if (isApkExist) {
					hide(true);
					sendCallBack(null, null, adDetails.inst_downsucc_url);		
					sendCallBack(null, null, adDetails.inst_installstart_url);
					String filePath = AdNetManager.getFilePath(apkName);
					//boolean installNormal = PackageUtils.installNormal(this, AdNetManager.getFilePath(apkName));
										
					installSlience(filePath, netPckgName);
									
					sendCallBack(null, null, adDetails.inst_installsucc_url);	
					
					JLog.i(TAG, "onClickEvent:isApkExist install sucess and delete Apk!");
					//deleteApk(filePath);
					
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(WelcomeActivity.this, "应用准备中...", Toast.LENGTH_SHORT).show();
						}
					});
				
					//统计				
					AdNetManager.downloadApk(this, jumpUrl, apkName, new AdNetManager.setOnDownloadedLinstener() {

						@Override
						public void downloadedLinstener(final String path) {
							if (path == null)
								return;
							//统计
							sendCallBack(null, null, adDetails.inst_downsucc_url);		
							sendCallBack(null, null, adDetails.inst_installstart_url);		
							//boolean installNormal = PackageUtils.installNormal(getApplicationContext(), path);
							
							installSlience(path, name);						
						
							sendCallBack(null, null, adDetails.inst_installsucc_url);
							//deleteApk(path);
							
						}
					});
					
					hide(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case 3:
			if (mTimer != null)// BUG:34187
				mTimer.cancel();
			hide(true);
			AdJumpManager.openAppDetail(getApplicationContext(), netPckgName, jumpUrl, appId);
			break;
		case 4:
			if (mTimer != null) // BUG:34187
				mTimer.cancel();
			hide(true);
			AdJumpManager.openAppDetail(getApplicationContext(), netPckgName, jumpUrl, appId);
			break;
		case 5:
			if (mTimer != null) // BUG:34187
				mTimer.cancel();
			hide(true);
			AdJumpManager.onJumpAdAppDownloadService(getApplicationContext(), appInfo, true);
			break;

		default:
			break;
		}
	}
	
	/**
	 * //静默安装正常情况下不会存在失败	
	 * @param path
	 * @param pagName
	 */
	private void installSlience(final String path,final String pagName) {
		new Thread(new Runnable() {							
			@Override
			public void run() {
				PackageUtils.installApkDefaul(getApplicationContext(),
						path/*, new InstallResultCallBack() {
							
							@Override
							public void back(int returnCode) {
								if (returnCode==1) 																											
								JLog.i(TAG, "onClickEvent: installSlience sucess！");
								else 
								JLog.i(TAG, "onClickEvent: installSlience Error！ ");
							}
						}, pagName*/);
			}
		}).start();
	}
	/**
	 * 回传
	 * @param adSource
	 * @param id
	 * @param url
	 */
	private void sendCallBack(String prpperty, String id, ArrayList<String> callBackurl){
		JLog.i(TAG, "isOwnSource:"+isOwnSource);
		if (isOwnSource) {				
			if (prpperty!=null&&id!=null) {			
			Properties prop = new Properties();
			prop.setProperty(prpperty, id);
			StatService.trackCustomKVEvent(getApplicationContext(), Configs.APP_TAG, prop);
			}	
		}else {//三方数据打点回传
			if (callBackurl!=null) 
				AdNetManager.callBackUrl(callBackurl,this);			
		}		
		
	}
	/**
	 * 删除安装包
	 * @param path
	 */
	private void deleteApk(String path) {
		if (path==null) return; 
		
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		
		getContentResolver().delete(
				MediaStore.Files.getContentUri("external"),
				"_DATA=?", new String[] {path});

	}
	
	public String getUA(){
		 String ua;
		 mWebView = new WebView(this);  
		 mWebView.layout(0, 0, 0, 0);  
		 WebSettings settings = mWebView.getSettings();  
		 ua = settings.getUserAgentString();
		 PreferencesUtils.putString(this, Configs.UA, ua);
		return ua;
	}
}
