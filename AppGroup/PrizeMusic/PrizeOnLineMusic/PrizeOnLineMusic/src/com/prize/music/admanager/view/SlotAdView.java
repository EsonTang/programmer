
package com.prize.music.admanager.view;

import java.util.zip.Inflater;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.app.util.PackageUtils;
import com.prize.music.R;
import com.prize.music.admanager.bean.AdDetailsInfo;
import com.prize.music.admanager.presenter.AdJumpManager;
import com.prize.music.admanager.presenter.AdNetManager;
import com.prize.music.admanager.presenter.AdNetManager.setOnDownloadedLinstener;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 插屏广告
 *
 * @author huangchangguo 2016.11.8
 */
public class SlotAdView {

	private static final String TAG = "huang-SlotAdView";
	private static int num = 5;
	private View mView;
	private TextView mHide;
	private Context mContext;
	private WindowManager mWM;
	private ImageView mSplashIv;
	private RelativeLayout mSlotRl;
	private AdDetailsInfo mAdDetailsInfo;
	private WindowManager.LayoutParams mParams;

	public SlotAdView(Context context) {
		this.mContext = context;
		mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		// defined that sets up the layout params appropriately.
		
		mParams = new WindowManager.LayoutParams();
//		// 指定宽高
		mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.setTitle("SlotAdView");
		mParams.gravity = Gravity.CENTER;		
		// 指定格式，是否有焦点，是否可以触摸
		mParams.flags = 
					//WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
				WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON|
				WindowManager.LayoutParams.FLAG_FULLSCREEN;
		mParams.format = PixelFormat.TRANSLUCENT;
		//mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
	}

	public void show(final String bannerUrl, AdDetailsInfo adDetailsInfo, final String ids) {

		// if (bannerUrl == null && adDetailsInfo == null) {
		// return;
		// }
		if (mView != null)
			return;
		this.mAdDetailsInfo = adDetailsInfo;
		mView = LayoutInflater.from(mContext).inflate( R.layout.ad_splash_view, null, false);
		// mView.setVisibility(View.GONE);
	
		mHide = (TextView) mView.findViewById(R.id.ad_splash_clock);
		mSplashIv = (ImageView) mView.findViewById(R.id.ad_splash_iv);

		CountDownTimer timer = new CountDownTimer(5000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				mHide.setText(millisUntilFinished / 1000 - 1 + "");
				JLog.i(TAG, "onTick:--" + millisUntilFinished / 1000 + "---" + millisUntilFinished);
			}

			@Override
			public void onFinish() {
				hide(true);
			}
		};
		timer.start();
		// Target<GlideDrawable> into =
		// Glide.with(context).load(bannerUrl).into(mSlotIv);

		// 监听图片加载是否失败
		Glide.with(mContext).load(bannerUrl)
				// .preload()
				// .placeholder(R.drawable.slot_ad_bg_deful)
				.into(new GlideDrawableImageViewTarget(mSplashIv) {
					@Override
					public void onResourceReady(GlideDrawable arg0, GlideAnimation<? super GlideDrawable> arg1) {
						super.onResourceReady(arg0, arg1);
						try {
							mView.setVisibility(View.VISIBLE);

						} catch (Exception e) {
						}

						Log.i(TAG, "SlotView-show:onResourceReady!");
					}

					@Override
					public void onLoadFailed(Exception e, Drawable errorDrawable) {
						super.onLoadFailed(e, errorDrawable);
						Log.i(TAG, "SlotView-show:onLoadFailed!");
						// 统计
						reLoadView(mSplashIv, bannerUrl, ids);
						// hide();

					}
				});
		// mSlotIv.setOnTouchListener(this);
		mSplashIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				onClickEvent(ids);
			}
		});
		mHide.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 去除重复的广告、展示成功后一段时间后再展示
				hide(true);
			}
		});

		try {
			if (mView != null)
				mWM.addView(mView, mParams);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void reLoadView(final ImageView v, String url, final String ids) {
		Glide.with(mContext).load(url).into(new SimpleTarget<GlideDrawable>() {
			@Override
			public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
				try {

					v.setImageDrawable(resource);
					mView.setVisibility(View.VISIBLE);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onLoadFailed(Exception e, Drawable errorDrawable) {
				super.onLoadFailed(e, errorDrawable);
				//hide(true);

			}
		});
	}

	public void hide(Boolean isToMain) {
		if (isToMain) {
			AdJumpManager.onJumpMainActivity(mContext);
		} 
		if (mView != null) {
			if (mView.getParent() != null) {
				mWM.removeView(mView);
			}
			mView = null;
		}
	}

	public Boolean isGetView() {
		if (mView != null)
			return true;
		return false;
	}

	private void onClickEvent(String ids) {
		if (mAdDetailsInfo == null || mAdDetailsInfo.detailType == -1 || mAdDetailsInfo.jumpUrl == null) {
			hide(true);
			return;
		}
		int detailTypeKey = mAdDetailsInfo.detailType;
		String jumpUrl = mAdDetailsInfo.jumpUrl;
		String apkName = mAdDetailsInfo.apkName;
		String netPckgName = mAdDetailsInfo.packageName;
		String appId = mAdDetailsInfo.appId;
		AppsItemBean appInfo = mAdDetailsInfo.appInfo;
		switch (detailTypeKey) {
		case 1:
			// 跳转URL
			if (jumpUrl != null)
				AdJumpManager.onJumpAdWebActivity(mContext, jumpUrl);
			hide(false);
			break;
		case 2:
			// 跳转下载
			try {
				Boolean isApkExist = AdNetManager.checkApkIsExist(mContext, netPckgName, apkName);
				JLog.i(TAG, "downloadApk-isApkExist:" + isApkExist);
				if (isApkExist) {
					PackageUtils.installNormal(mContext, AdNetManager.getFilePath(apkName));
				} else {
					Toast.makeText(mContext, "应用准备中...", Toast.LENGTH_SHORT).show();
					AdNetManager.downloadApk(mContext, jumpUrl, apkName, new AdNetManager.setOnDownloadedLinstener() {

						@Override
						public void downloadedLinstener(String path) {
							if (path == null)
								return;
							PackageUtils.installNormal(mContext, path);
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			hide(false);
			break;
		case 3:
			AdJumpManager.openAppDetail(mContext, netPckgName, jumpUrl, appId);
			hide(false);
			break;
		case 4:
			AdJumpManager.openAppDetail(mContext, netPckgName, jumpUrl, appId);
			hide(false);
			break;
		case 5:
			AdJumpManager.onJumpAdAppDownloadService(mContext, appInfo, true);
			hide(false);
			break;

		default:
			break;
		}	
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

	// ┏┓ ┏┓
	// ┏┛┻━━━┛┻┓
	// ┃ ┃
	// ┃ ━ ┃
	// ┃ ┳┛ ┗┳ ┃
	// ┃ ┃
	// ┃ ┻ ┃
	// ┃ ┃
	// ┗━┓ ┏━┛
	// ┃ ┃ 神兽保佑
	// ┃ ┃ 代码无BUG！
	// ┃ ┗━━━┓
	// ┃ ┣┓
	// ┃ ┏┛
	// ┗┓┓┏━┳┓┏┛
	// ┃┫┫ ┃┫┫
	// ┗┻┛ ┗┻┛

}
