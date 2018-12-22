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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
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
public class SlotAdView {

	private static final String TAG = "huang-SlotAdView";
	private View mView;
	private TextView mHide;
	private Context context;
	private WindowManager mWM;
	private ImageView mSlotIv;
	private RelativeLayout mSlotRl;
	private AdDetailsInfo mAdDetailsInfo;
	private WindowManager.LayoutParams mParams;

	public SlotAdView(Context context) {
		this.context = context;
		mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		// defined that sets up the layout params appropriately.
		mParams = new WindowManager.LayoutParams();
		// 指定宽高
		mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mParams.setTitle("SlotAdView");
		mParams.gravity = Gravity.CENTER;
		// 指定格式，是否有焦点，是否可以触摸
		mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		mParams.format = PixelFormat.TRANSLUCENT;
		mParams.type = WindowManager.LayoutParams.TYPE_TOAST;
	}

	private boolean isShow = false;

	public void show(String bannerUrl, AdDetailsInfo adDetailsInfo) {

		if (bannerUrl == null && adDetailsInfo == null) {
			return;
		}
		if (mView != null)
			return;
		MTAUtils.onJumpSlotAd(context);
		this.mAdDetailsInfo = adDetailsInfo;
		mView = View.inflate(context, R.layout.view_slot, null);
		mView.setVisibility(View.GONE);
		// mView = View.inflate(context, R.layout.activity_main, null);
		mHide = (TextView) mView.findViewById(R.id.slot_tv_hide);
		// mSlotRl = (RelativeLayout) mView.findViewById(R.id.slot_rl);
		mSlotIv = (ImageView) mView.findViewById(R.id.slot_iv);

		// Target<GlideDrawable> into =
		// Glide.with(context).load(bannerUrl).into(mSlotIv);
		JLog.i(TAG, "SlotView-show:waiting!");
		// 监听图片加载是否失败
		Glide.with(context).load(bannerUrl)
				.into(new GlideDrawableImageViewTarget(mSlotIv) {
					@Override
					public void onResourceReady(GlideDrawable arg0,
							GlideAnimation<? super GlideDrawable> arg1) {
						mView.setVisibility(View.VISIBLE);
						PreferencesUtils.clearOutAd(context);
						Log.i(TAG, "SlotView-show:onResourceReady!");
						super.onResourceReady(arg0, arg1);
					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
						hide();
						Log.i(TAG, "SlotView-show:onLoadFailed!");
						super.onLoadFailed(e, errorDrawable);
					}
				});
		// mSlotIv.setOnTouchListener(this);
		mSlotIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickEvent();
			}
		});
		mHide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hide();
			}
		});
		if (Constants.JLog) {
			int versionCode = 0;
			String versionName = null;
			try {
				PackageInfo pi = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				versionCode = pi.versionCode;
				versionName = pi.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();

			}
			Toast.makeText(
					context,
					"versionName:" + versionName + " versionCode:"
							+ versionCode, Toast.LENGTH_LONG).show();
		}

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
	// case R.id.slot_tv_hide:
	// hide();
	// break;
	// case R.id.slot_rl:
	// onClickEvent();
	// break;
	//
	// case R.id.slot_iv:
	// Log.i(TAG, "  onClick:slot_iv -----!");
	// break;
	// default:
	// break;
	// }
	// }

	private void onClickEvent() {
		int detailTypeKey = mAdDetailsInfo.detailType;
		Log.i(TAG, "onClick:detailType(1:onJumpAdWebView||2:onJumpDownload):"
				+ detailTypeKey);
		MTAUtils.onClickSlotAd(context);
		if (detailTypeKey == 1) {
			// 跳转URL
			String jumpUrl = mAdDetailsInfo.jumpUrl;
			if (jumpUrl != null)
				JumpingManager.onJumpAdWebView(context, jumpUrl);
			// JumpingManager.onJumpAdWebView2(context, jumpUrl, "详情");
			// JumpingManager.onJumpOtherExplorer(context, jumpUrl);
		} else if (detailTypeKey == 2) {
			// 跳转下载
			AppsItemBean appInfo = mAdDetailsInfo.appInfo;
			if (appInfo != null)
				JumpingManager.onJumpAdAppDownloadService(context, appInfo,
						false);
		}
		hide();

	}

	// float downX, moveX;
	// float downY, moveY;

	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	// switch (event.getAction()) {
	// case MotionEvent.ACTION_DOWN:
	// // downX = event.getRawX();
	// downY = event.getRawY();
	// break;
	// case MotionEvent.ACTION_MOVE:
	// // moveX = event.getRawX();
	// moveY = event.getRawY();
	// // mParams.x += (int) (moveX - downX);
	// mParams.y += (int) (moveY - downY);
	// // 更新当前View的位置
	// mWM.updateViewLayout(mView, mParams);
	// // downX = moveX;
	// downY = moveY;
	// break;
	// case MotionEvent.ACTION_UP:
	// break;
	// }
	// return false;
	// }

}
