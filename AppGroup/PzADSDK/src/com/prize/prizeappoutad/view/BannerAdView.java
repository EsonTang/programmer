package com.prize.prizeappoutad.view;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.prize.prizeappoutad.R;
import com.prize.prizeappoutad.bean.AdDetailsInfo;
import com.prize.prizeappoutad.bean.AppsItemBean;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.manager.JumpingManager;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.MTAUtils;
import com.prize.prizeappoutad.utils.PreferencesUtils;

/**
 * 插屏广告
 * 
 * @author huangchangguo 2016.11.8
 * 
 */
public class BannerAdView implements OnTouchListener {

	private static final String TAG = "huang-BannerAdView";
	private static final int BANNERVIEW = 1;
	private static final int SLOTVIEW = BANNERVIEW + 1;
	private View mView;
	private Context mContext;
	private WindowManager mWM;
	private AdDetailsInfo mAdDetailsInfo;
	private WindowManager.LayoutParams mParams;

	private TextView mHide;
	private ImageView mBanner;

	public BannerAdView(Context context) {
		this.mContext = context;
		mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		// defined that sets up the layout params appropriately.
		mParams = new WindowManager.LayoutParams();
		// 指定宽高
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;

		mParams.setTitle("BannerAdView");

		// 指定格式，是否有焦点，是否可以触摸
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| /*
				 * WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
				 */WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		mParams.format = PixelFormat.TRANSLUCENT;
		mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
	}

	public void show(String bannerUrl, AdDetailsInfo adDetailsInfo) {
		if (mView != null) {
			// 展示的广告没有关闭
			return;
		}
		if (bannerUrl == null && adDetailsInfo == null) {
			return;
		}
		MTAUtils.onJumpBannerAd(mContext);
		this.mAdDetailsInfo = adDetailsInfo;
		mView = View.inflate(mContext, R.layout.view_banner, null);
		mView.setVisibility(View.GONE);
		mHide = (TextView) mView.findViewById(R.id.banner_tv_hide);
		mBanner = (ImageView) mView.findViewById(R.id.banner_iv);
		Glide.with(mContext).load(bannerUrl)
				.into(new GlideDrawableImageViewTarget(mBanner) {
					@Override
					public void onResourceReady(GlideDrawable arg0,
							GlideAnimation<? super GlideDrawable> arg1) {
						mView.setVisibility(View.VISIBLE);
						PreferencesUtils.clearOutAd(mContext);
						Log.i(TAG, "BannerAdView-show:onResourceReady!");
						super.onResourceReady(arg0, arg1);
					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
						hide();
						Log.i(TAG, "BannerAdView-show:onLoadFailed!");
						super.onLoadFailed(e, errorDrawable);
					}
				});
		mBanner.setOnTouchListener(this);
		mBanner.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JLog.i(TAG, "mBanner-onClick");
				MTAUtils.onClickBannerAd(mContext);
				onClickEvent();
			}
		});
		mHide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hide();
			}
		});
		JLog.i(TAG, "BannerAdView-show:waiting!");

		if (Constants.JLog) {
			int versionCode = 0;
			String versionName = null;
			try {
				PackageInfo pi = mContext.getPackageManager().getPackageInfo(
						mContext.getPackageName(), 0);
				versionCode = pi.versionCode;
				versionName = pi.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();

			}
			Toast.makeText(
					mContext,
					"versionName:" + versionName + " versionCode:"
							+ versionCode, Toast.LENGTH_LONG).show();
		}
		mParams.gravity = Gravity.TOP;
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

	// @Override
	// public void onClick(View v) {
	// int id = v.getId();
	// switch (id) {
	// case R.id.banner_tv_hide:
	// hide();
	// break;
	// case R.id.banner_iv:
	// // 跳转详情
	// // TODO
	// onClickEvent();
	//
	// default:
	// break;
	// }
	// }

	private void onClickEvent() {
		int detailTypeKey = mAdDetailsInfo.detailType;
		Log.i(TAG, "onClick:detailType(1||2):" + detailTypeKey);
		if (detailTypeKey == 1) {
			// 跳转URL
			String jumpUrl = mAdDetailsInfo.jumpUrl;
			if (jumpUrl != null) {
				JumpingManager.onJumpAdWebView(mContext, jumpUrl);
				// JumpingManager.onJumpAdWebView2(mContext, jumpUrl, "详情");
			}

		} else if (detailTypeKey == 2) {
			// 跳转下载
			AppsItemBean appInfo = mAdDetailsInfo.appInfo;
			if (appInfo != null) {
				JumpingManager.onJumpAdAppDownloadService(mContext, appInfo,
						false);
			}
		}
		hide();
	}

	float downX, downY, moveX, moveY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// downX = event.getRawX();
			downY = event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			// moveX = event.getRawX();
			moveY = event.getRawY();

			// mParams.x += (int) (moveX - downX);
			mParams.y += (int) (moveY - downY);

			mWM.updateViewLayout(mView, mParams);

			// downX = moveX ;
			downY = moveY;
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return false;
	}

}
