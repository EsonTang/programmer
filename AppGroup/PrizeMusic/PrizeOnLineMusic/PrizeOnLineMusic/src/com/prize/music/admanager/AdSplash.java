package com.prize.music.admanager;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CancelledException;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.PackageUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.music.R;
import com.prize.music.activities.AudioPlayerActivity;
import com.prize.music.admanager.bean.AdCommonInfo;
import com.prize.music.admanager.bean.AdDetailsInfo;
import com.prize.music.admanager.bean.AdOutInfo;
import com.prize.music.admanager.presenter.AdJumpManager;
import com.prize.music.admanager.presenter.AdNetManager;
import com.prize.music.admanager.presenter.HomeKeyReceiver;
import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.view.PopAdView;
import com.prize.music.admanager.view.SlotAdView;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 启动页广告
 * 
 * @author huangchangguo
 * 
 *         2017.5.22
 *
 */
public class AdSplash {
	protected static final String TAG = "huang-AdSplash";
	private static AdSplash mInstance;
	private Cancelable reqHandler;
	private static boolean isRun = false;
	private static Context mCtx;
	private SlotAdView mAdView;
	private AdDetailsInfo mAdDetailsInfo;

	private TextView mHide;
	private ImageView mSplashIv;

	private static final int MSG_NETINFO = 1;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NETINFO:
				AdOutInfo data = (AdOutInfo) msg.obj;
				JLog.i(TAG, data.toString());
				setView(data);
				break;
			}
		}
	};

	public AdSplash() {
		if (mCtx == null)
			return;
		if (mAdView == null) {
			mAdView = new SlotAdView(mCtx);
		}
		// registerHomeKeyReceiver(mCtx);
		// mAdView.show(" ", null, 2 + "");
		if (!isRun) {
			getNetAd();
		}
	}

	/**
	 * 悬浮窗式的开屏广告，由于无法覆盖顶部状态栏，暂时不用
	 */
	public void setView(AdOutInfo data) {
		if (data == null || data.adDetails == null || data.adBanner == null)
			return;
		String bannerUrl = data.adBanner;
		if (mAdView != null) {
			mAdView.show(bannerUrl, data.adDetails, data.id);
		} else {
			mAdView = new SlotAdView(mCtx);
			mAdView.show(bannerUrl, data.adDetails, data.id);
		}
	}

	public static AdSplash getInstance(Context ctx) {
		if (ctx != null && mCtx == null)
			mCtx = ctx.getApplicationContext();
		if (mInstance == null) {
			synchronized (AdSplash.class) {
				if (mInstance == null) {
					mInstance = new AdSplash();
				}
			}
		}
		return mInstance;
	}

	/*** 请求广告 */
	public void getNetAd() {
		if (isRun)
			return;
		isRun = true;
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
				Log.i(TAG, "onError.e:" + ex.toString());
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

	// private static HomeKeyReceiver mHomeKeyReceiver = null;
	private FrameLayout mView;
	// 注册home键监听
	// private static void registerHomeKeyReceiver(Context context) {
	// Log.i(TAG, "registerHomeKeyReceiver");
	// mHomeKeyReceiver = new HomeKeyReceiver();
	// final IntentFilter homeFilter = new
	// IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	// context.registerReceiver(mHomeKeyReceiver, homeFilter);
	// }
	//
	// private static void unregisterHomeKeyReceiver(Context context) {
	// Log.i(TAG, "unregisterHomeKeyReceiver");
	// if (null != mHomeKeyReceiver) {
	// context.unregisterReceiver(mHomeKeyReceiver);
	// }
	// }

	public void onDestory() {
		if (reqHandler != null)
			reqHandler.cancel();
		// if (mCtx != null)
		// unregisterHomeKeyReceiver(mCtx);
		if (mAdView != null)
			mAdView.hide(false);
	}

}
