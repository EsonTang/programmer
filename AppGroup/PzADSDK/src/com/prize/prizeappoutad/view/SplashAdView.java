package com.prize.prizeappoutad.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.pm.PackageParser.Activity;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
import com.prize.prizeappoutad.R;

/**
 * 开屏广告 -View版本（不可用）
 * 
 * @author huangchangguo 2016.11.22
 * 
 */
public class SplashAdView {

	private static final String TAG = "huang-SlotAdView";
	private View mView;
	private Context context;
	private Activity activity;
	private WindowManager mWM;

	private WindowManager.LayoutParams mParams;

	public boolean canJumpImmediately = false;
	private RelativeLayout adsBgDef;
	private Timer timer = new Timer();
	private boolean isClicked = false;
	private TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			// 自关闭
			if (!isClicked) {
				hide();
			}

		}
	};

	public SplashAdView(Context context) {
		this.context = context;
		mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		// defined that sets up the layout params appropriately.
		mParams = new WindowManager.LayoutParams();
		// 指定宽高
		mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.setTitle("SlotAdView");
		mParams.gravity = Gravity.CENTER;
		// 指定格式，是否有焦点，是否可以触摸
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		mParams.format = PixelFormat.TRANSLUCENT;
		mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		timer.schedule(timerTask, (long) (6 * 1000));

	}

	// public void show() {
	// if (mView != null)
	// return;
	// mView = View.inflate(context, R.layout.splash, null);
	// RelativeLayout adsParent = (RelativeLayout) mView
	// .findViewById(R.id.adsRl);
	// adsBgDef = (RelativeLayout) mView.findViewById(R.id.splash_bg);
	//
	// // the observer of AD
	// SplashAdListener listener = new SplashAdListener() {
	// @Override
	// public void onAdDismissed() {
	// JLog.i("huang-RSplashActivity", "onAdDismissed");
	// hide(); // 跳转至您的应用主界面
	// }
	//
	// @Override
	// public void onAdFailed(String arg0) {
	// Log.i("huang-RSplashActivity", "onAdFailed");
	// MTAUtils.splashAdFailed(context);
	// hide();
	// }
	//
	// @Override
	// public void onAdPresent() {
	// JLog.i("huang-RSplashActivity", "onAdPresent");
	// MTAUtils.splashAdPresent(context);
	// adsBgDef.setVisibility(View.GONE);
	// // Toast.makeText(RSplashActivity.this, " versionCode:1 ",
	// // Toast.LENGTH_LONG).show();
	// }
	//
	// @Override
	// public void onAdClick() {
	// JLog.i("huang-RSplashActivity", "onAdClick");
	// // 设置开屏可接受点击时，该回调可用
	// isClicked = true;
	// // MTA统计
	// MTAUtils.splashAdDownload(context);
	// }
	// };
	//
	// String adPlaceId = "2975007"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
	// if (adsParent != null) {
	// new SplashAd(context, adsParent, listener, adPlaceId, true);
	// }
	// mWM.addView(mView, mParams);
	// }

	InterstitialAd interAd;

	public void show() {
		if (mView != null)
			return;
		mView = View.inflate(context, R.layout.splash, null);
		// RelativeLayout adsParent = (RelativeLayout) mView
		// .findViewById(R.id.adsRl);
		adsBgDef = (RelativeLayout) mView.findViewById(R.id.splash_bg);

		String adPlaceId = "2403633"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
		interAd = new InterstitialAd(context, adPlaceId);
		interAd.setListener(new InterstitialAdListener() {

			@Override
			public void onAdClick(InterstitialAd arg0) {
				Log.i("InterstitialAd", "onAdClick");
			}

			@Override
			public void onAdDismissed() {
				Log.i("InterstitialAd", "onAdDismissed");
				interAd.loadAd();
			}

			@Override
			public void onAdFailed(String arg0) {
				Log.i("InterstitialAd", "onAdFailed");
			}

			@Override
			public void onAdPresent() {
				Log.i("InterstitialAd", "onAdPresent");
			}

			@Override
			public void onAdReady() {
				Log.i("InterstitialAd", "onAdReady");
				//interAd.showAd();
			}

		});
		interAd.loadAd();

		mWM.addView(mView, mParams);
	}

	public void hide() {
		if (mView != null) {
			if (mView.getParent() != null) {
				mWM.removeView(mView);
			}
			mView = null;
		}
	}

}
