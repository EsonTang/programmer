package com.prize.prizeappoutad.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;
import com.prize.prizeappoutad.R;
import com.prize.prizeappoutad.constants.Constants;
import com.prize.prizeappoutad.utils.AppManager;
import com.prize.prizeappoutad.utils.JLog;
import com.prize.prizeappoutad.utils.MTAUtils;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

/**
 * 实时开屏，广告实时请求并且立即展现
 */

public class RSplashFragment extends Fragment {
	// private TextView timeView;
	public boolean canJumpImmediately = false;
	private RelativeLayout adsBgDef;
	private Timer timer = new Timer();
	private TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			// 自关闭
			jump();
		}
	};

	public View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.splash, container, false);
		JLog.i("huang-RSplashFragment", "      onCreate-onCreate");
		timer.schedule(timerTask, 6 * 1000);

		RelativeLayout adsParent = (RelativeLayout) view
				.findViewById(R.id.adsRl);
		adsBgDef = (RelativeLayout) view.findViewById(R.id.splash_bg);

		if (Constants.JLog) {

			int versionCode = 0;
			String versionName = null;
			try {
				PackageInfo pi = getActivity().getPackageManager()
						.getPackageInfo(getActivity().getPackageName(), 0);
				versionCode = pi.versionCode;
				versionName = pi.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();

			}
			Toast.makeText(
					getActivity(),
					"versionName:" + versionName + " versionCode:"
							+ versionCode, Toast.LENGTH_LONG).show();
		}
		// the observer of AD
		SplashAdListener listener = new SplashAdListener() {
			@Override
			public void onAdDismissed() {
				JLog.i("huang-RSplashActivity", "onAdDismissed");
				jumpWhenCanClick(); // 跳转至您的应用主界面
			}

			@Override
			public void onAdFailed(String arg0) {
				Log.i("huang-RSplashActivity", "onAdFailed");
				jump();
				MTAUtils.splashAdFailed(getActivity());
			}

			@Override
			public void onAdPresent() {
				JLog.i("huang-RSplashActivity", "onAdPresent");
				adsBgDef.setVisibility(View.GONE);
				// Toast.makeText(RSplashActivity.this, " versionCode:1 ",
				// Toast.LENGTH_LONG).show();
				MTAUtils.splashAdPresent(getActivity());
			}

			@Override
			public void onAdClick() {
				JLog.i("huang-RSplashActivity", "onAdClick");
				// 设置开屏可接受点击时，该回调可用
				// MTA统计
				MTAUtils.splashAdDownload(getActivity());
			}
		};

		String adPlaceId = "2975007"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
		if (adsParent != null) {
			new SplashAd(getActivity(), adsParent, listener, adPlaceId, true);
		}
		return view;
	};

	/**
	 * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。
	 * 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
	 */
	private void jumpWhenCanClick() {
		JLog.d("test", "this.hasWindowFocus():"
				+ getActivity().hasWindowFocus());
		if (canJumpImmediately) {
			jump();
		} else {
			canJumpImmediately = true;
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		canJumpImmediately = false;
		StatService.onPause(getActivity());
		MobclickAgent.onPause(getActivity());
	}

	/**
	 * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
	 */
	private void jump() {
		// this.startActivity(new Intent(RSplashActivity.this,
		// MainActivity.class));

		JLog.i("huang", "------jump-finish！");

		getActivity().finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		// 统计
		MobclickAgent.onResume(getActivity());
		StatService.onResume(getActivity());
		if (canJumpImmediately) {
			jumpWhenCanClick();
		}
		canJumpImmediately = true;
	}
}
