package com.prize.music.admanager;

import java.io.File;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.PackageUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.music.MainApplication;
import com.prize.music.R;
import com.prize.music.admanager.bean.AdCommonInfo;
import com.prize.music.admanager.bean.AdDetailsInfo;
import com.prize.music.admanager.bean.AdOutInfo;
import com.prize.music.admanager.presenter.AdJumpManager;
import com.prize.music.admanager.presenter.AdNetManager;
import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.StatService;
import com.prize.music.helpers.utils.PreferencesUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 启动页广告的配置信息
 * 
 * @author huangchangguo
 * 
 *         2017.5.22
 *
 */
public class ActivitySplash {
	protected static final String TAG = "huang-ActivitySplash";
	private Context mCtx;
	private AdDetailsInfo mAdDetailsInfo;
	private FrameLayout mView;
	private TextView mHide;
	private ImageView mSplashIv, mDefView;
	private Cancelable reqHandler;
	private Activity mAct;
	private static boolean isRun = false;
	private static boolean isGetData = false;
	/** 广告展示的时间 */
	private static final int AD_SHOW_TIME = 5000;
	/** 广告展示间隔 */
	// private static final int AD_SHOW_PERIOD = 30 * 60 * 1000;
	/** 广告展示间隔(测试) */
	private static final int AD_SHOW_PERIOD = 3 * 1000;

	public boolean isGetData() {
		return isGetData;

	}

	private CountDownTimer mTimer;
	private DataChangerLinstener linstener;
	private static final String SPLASH_ONCLICK_EVENT = "music_splash_onclick";
	private static final String SPLASH_SHOW_EVENT = "music_splash_show";

	public interface DataChangerLinstener {
		void getData(AdOutInfo data);
	}

	public ActivitySplash(Context ctx) {
		mCtx = ctx;
	}

	/** 加载成功时候展示广告 2017.5.25 */
	public void show() {
		// 展示
		// if (mView != null && mView.getVisibility() == View.GONE) {
		// mView.setVisibility(View.VISIBLE);
		// mDefView.setVisibility(View.GONE);
		// }
		if (mHide.getVisibility() == View.GONE)
			mHide.setVisibility(View.VISIBLE);

		mTimer = new CountDownTimer(AD_SHOW_TIME, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				int num = (int) ((millisUntilFinished / 1000) - 1);
				if (mHide != null)
					mHide.setText(num + "");
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

	public void loadAd(Activity act, AdOutInfo data) {
		mAct=act;
		mView = (FrameLayout) act.findViewById(R.id.ad_splash_v);
		mDefView = (ImageView) act.findViewById(R.id.loading_img);
		mHide = (TextView) act.findViewById(R.id.ad_splash_clock);
		mSplashIv = (ImageView) act.findViewById(R.id.ad_splash_iv);

		if (data == null || data.adBanner == null || data.adDetails == null)
			return;
		final String bannerUrl = data.adBanner;
		final String id = data.id;
		mAdDetailsInfo = data.adDetails;
		JLog.i(TAG, mAdDetailsInfo.toString());
		// Target<GlideDrawable> into =
		// Glide.with(context).load(bannerUrl).into(mSlotIv);

		// 监听图片加载是否失败
		Glide.with(act).load(bannerUrl)
				// .preload()
				// .placeholder(R.drawable.welcome_logo)
				.fitCenter().into(new GlideDrawableImageViewTarget(mSplashIv) {
					@Override
					public void onResourceReady(GlideDrawable arg0, GlideAnimation<? super GlideDrawable> arg1) {
						super.onResourceReady(arg0, arg1);
						JLog.i(TAG, "SlotView-show:onResourceReady!");
						// 记录广告展示的时间
						PreferencesUtils.putLong(mCtx, Configs.SP_TIME_KEY, System.currentTimeMillis());
						// 展示统计
						Properties prop = new Properties();
						prop.setProperty(SPLASH_SHOW_EVENT, id);
						StatService.trackCustomKVEvent(mCtx, Configs.APP_TAG, prop);

						new Handler().post(new Runnable() {
							@Override
							public void run() {
								show();
							}
						});

					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
						super.onLoadFailed(e, errorDrawable);
						JLog.i(TAG, "SlotView-show:onLoadFailed!");
						// 统计
						reLoadView(mSplashIv, bannerUrl, id);
						// hide();

					}
				});
		// mSlotIv.setOnTouchListener(this);
		mSplashIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickEvent(id);
			}
		});
		mHide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 去除重复的广告、展示成功后一段时间后再展示
				hide(true);
			}
		});
	}

	private void reLoadView(final ImageView v, String url, final String id) {
		Glide.with(mCtx).load(url).into(new SimpleTarget<GlideDrawable>() {
			@Override
			public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
				try {
					v.setImageDrawable(resource);
					// 统计
					Properties prop = new Properties();
					prop.setProperty(SPLASH_SHOW_EVENT, id);
					StatService.trackCustomKVEvent(mCtx, Configs.APP_TAG, prop);
					// 记录广告展示的时间
					PreferencesUtils.putLong(mCtx, Configs.SP_TIME_KEY, System.currentTimeMillis());
					show();
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

	/*** 请求广告 */
	public void getNetAd(final DataChangerLinstener dataChangerLinstener) {

		long time = PreferencesUtils.getLong(mCtx, Configs.SP_TIME_KEY);
		long newTime = System.currentTimeMillis();
		JLog.i(TAG, "ad_period_time:" + (newTime - time) / 1000);
		if (Math.abs(newTime - time) <= AD_SHOW_PERIOD)
			return;
		if (isRun)
			return;
		isRun = true;
		isGetData = false;
		RequestParams params = new RequestParams(Configs.NET_URL);

		reqHandler = XExtends.http().post(params, new CommonCallback<String>() {
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
							// Message msg = Message.obtain();
							// msg.what = MSG_NETINFO;
							// msg.obj = data;
							// mHandler.sendMessage(msg);
							dataChangerLinstener.getData(data);
							isGetData = true;
						}
					}
				} catch (JSONException e) {
					JLog.i(TAG, "e.printStackTrace():" + e.toString());
					dataChangerLinstener.getData(null);
					e.printStackTrace();
				}

			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				isRun = false;
				dataChangerLinstener.getData(null);
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

	public void hide(Boolean isToMain) {
		if (isToMain) {
			AdJumpManager.onJumpMainActivity(mCtx);
		}
		if (mAct!=null) {
			mAct.finish();
		}
	}

	public Boolean isShow() {
		if (mView != null && mView.getVisibility() == View.VISIBLE)
			return true;
		return false;
	}

	private void onClickEvent(String id) {
		if (mAdDetailsInfo == null || mAdDetailsInfo.detailType == -1 || mAdDetailsInfo.jumpUrl == null) {
			hide(true);
			return;
		}
		// 统计
		Properties prop = new Properties();
		prop.setProperty(SPLASH_ONCLICK_EVENT, id);
		StatService.trackCustomKVEvent(mCtx, Configs.APP_TAG, prop);

		int detailTypeKey = mAdDetailsInfo.detailType;
		String jumpUrl = mAdDetailsInfo.jumpUrl;
		String apkName = mAdDetailsInfo.apkName;
		String netPckgName = mAdDetailsInfo.packageName;
		String appId = mAdDetailsInfo.appId;
		AppsItemBean appInfo = mAdDetailsInfo.appInfo;
		JLog.i(TAG, "detailTypeKey:" + detailTypeKey + " apkName:" + apkName + " netPckgName:" + netPckgName
				+ " jumpUrl:" + jumpUrl + " appId:" + appId);
		switch (detailTypeKey) {
		case 1:
			// 跳转URL
			mTimer.cancel();
			if (jumpUrl != null)
				AdJumpManager.onJumpAdWebActivity(mCtx, jumpUrl);
			hide(false);
			break;
		case 2:
			// 跳转下载
			try {
				mTimer.cancel();
				Boolean isApkExist = AdNetManager.checkApkIsExist(mCtx, netPckgName, apkName);
				JLog.i(TAG, "downloadApk-isApkExist:" + isApkExist);
				if (isApkExist) {
					PackageUtils.installNormal(mCtx, AdNetManager.getFilePath(apkName));
				} else {
					Toast.makeText(mCtx, "应用准备中...", Toast.LENGTH_SHORT).show();
					AdNetManager.downloadApk(mCtx, jumpUrl, apkName, new AdNetManager.setOnDownloadedLinstener() {

						@Override
						public void downloadedLinstener(String path) {
							if (path == null)
								return;
							PackageUtils.installNormal(mCtx, path);
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			hide(true);
			break;
		case 3:
			mTimer.cancel();
			AdJumpManager.openAppDetail(MainApplication.getContext(), netPckgName, jumpUrl, appId);
			hide(true);
			break;
		case 4:
			mTimer.cancel();
			AdJumpManager.openAppDetail(MainApplication.getContext(), netPckgName, jumpUrl, appId);
			hide(true);
			break;
		case 5:
			mTimer.cancel();
			AdJumpManager.onJumpAdAppDownloadService(MainApplication.getContext(), appInfo, true);
			hide(true);
			break;

		default:
			break;
		}
	}

	public void onDestory() {
		if (reqHandler != null)
			reqHandler.cancel();
		if (mView != null)
			hide(false);
		if (mTimer != null)
			mTimer.cancel();
		mView = null;		
	}
}
