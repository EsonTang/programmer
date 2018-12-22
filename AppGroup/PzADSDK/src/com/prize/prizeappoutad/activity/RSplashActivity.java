package com.prize.prizeappoutad.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
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

public class RSplashActivity extends Activity {
	// private TextView timeView;
	public boolean canJumpImmediately = false;
	private RelativeLayout adsBgDef;
	private Timer timer = new Timer();
	private boolean isClicked = false;
	private TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			// 自关闭
			if (!isClicked) {
				jump();
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// View inflater = View.inflate(RSplashActivity.this, R.layout.splash,
		// null);
		setContentView(R.layout.splash);
		JLog.i("huang-RSplashActivity", "      onCreate-onCreate");
		AppManager.getAppManager().addActivity(this);
		// 计时6秒后关闭，bug：开屏广告初始化失败，splash不关闭
		timer.schedule(timerTask, (long) (6 * 1000));

		// 初始化有米的SDK
		// AdManager.getInstance(this).init("febaccd6019726e4",
		// "7d30197ef58de79f", true, true);

		RelativeLayout adsParent = (RelativeLayout) findViewById(R.id.adsRl);
		adsBgDef = (RelativeLayout) findViewById(R.id.splash_bg);
		// adsBgDef = (RelativeLayout)findViewById(R.id.splash_bg_deful_rl);
		if (Constants.JLog) {

			int versionCode = 0;
			String versionName = null;
			try {
				PackageInfo pi = getApplicationContext().getPackageManager()
						.getPackageInfo(
								getApplicationContext().getPackageName(), 0);
				versionCode = pi.versionCode;
				versionName = pi.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();

			}
			Toast.makeText(
					RSplashActivity.this,
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
				MTAUtils.splashAdFailed(RSplashActivity.this);
				jump();
			}

			@Override
			public void onAdPresent() {
				JLog.i("huang-RSplashActivity", "onAdPresent");
				MTAUtils.splashAdPresent(RSplashActivity.this);
				adsBgDef.setVisibility(View.GONE);
				// Toast.makeText(RSplashActivity.this, " versionCode:1 ",
				// Toast.LENGTH_LONG).show();
			}

			@Override
			public void onAdClick() {
				JLog.i("huang-RSplashActivity", "onAdClick");
				// 设置开屏可接受点击时，该回调可用
				isClicked = true;
				// MTA统计
				MTAUtils.splashAdDownload(RSplashActivity.this);
			}
		};

		String adPlaceId = "2975007"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
		if (adsParent != null) {
			new SplashAd(this, adsParent, listener, adPlaceId, true);
		}
	}

	/**
	 * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。
	 * 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
	 */
	private void jumpWhenCanClick() {
		JLog.d("test", "this.hasWindowFocus():" + this.hasWindowFocus());
		if (canJumpImmediately) {
			jump();
		} else {
			canJumpImmediately = true;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		canJumpImmediately = false;
		StatService.onPause(this);
		MobclickAgent.onPause(this);
	}

	/**
	 * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
	 */
	private void jump() {
		// this.startActivity(new Intent(RSplashActivity.this,
		// MainActivity.class));

		JLog.i("huang", "------jump-finish！");
		AppManager.getAppManager().finishActivity(this);

		// finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 统计
		MobclickAgent.onResume(this);
		StatService.onResume(this);
		if (canJumpImmediately) {
			jumpWhenCanClick();
		}
		canJumpImmediately = true;
	}

	// // 广告倒计时计时器
	// private final CountDownTimer mCountDownTimer = new CountDownTimer(
	// time * 1000 + 200, 1000) {
	//
	// public void onTick(long millisUntilFinished) {
	// timeView.setText(millisUntilFinished / 1000 + "");
	// /*
	// * mHandler.post(new Runnable(){
	// *
	// * @Override public void run() { if(isShowReciprocal){
	// * JLog.i("NLG0606", "timeView: "+timeView);
	// * //timeView.setBackground(textView_shapeDrawable);
	// * timeView.setBackgroundDrawable(textView_shapeDrawable);
	// * timeView.setText(time + ""); } } });
	// */
	// }
	//
	// public void onFinish() {
	// //jump();
	// }
	//
	// };

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

}
