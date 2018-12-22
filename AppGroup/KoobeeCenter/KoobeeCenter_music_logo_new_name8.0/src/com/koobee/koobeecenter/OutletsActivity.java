package com.koobee.koobeecenter;


import java.io.File;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.koobee.koobeecenter.db.AsyncBase;
import com.koobee.koobeecenter.utils.FileUtils;
import com.koobee.koobeecenter.utils.WindowMangerUtils;
import com.koobee.koobeecenter02.R;

/**
 * 
 * @author Administrator 全国售后
 */
public class OutletsActivity extends Activity implements
		AsyncBase.OnDataAvailable {
	protected static final String TAG = "OutletsActivity";
	private WebView mContentWebV;
	private ImageView mErrImv;
	private String mUrl = "http://salenode.szprize.cn/";

	@SuppressLint({ "NewApi", "ResourceAsColor", "SetJavaScriptEnabled" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
		WindowMangerUtils.initStateBar(getWindow(), this);
		setContentView(R.layout.outlets_main);
		WindowMangerUtils.changeStatus(getWindow());
		initUI();
		//webView设置
		final WebSettings webSettings = mContentWebV.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setDefaultTextEncodingName("utf-8");
		saveData(webSettings);
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		//防止硬件加速闪屏
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mContentWebV.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		
		mContentWebV.setVisibility(View.VISIBLE);
		mContentWebV.loadUrl(mUrl);
		mContentWebV.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("http:") || url.startsWith("https:")) {

					return false;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				Log.d(TAG, url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
//					view.loadUrl("file:///android_asset/html/koobee_sale.html");
				mErrImv.setVisibility(View.VISIBLE);
				mContentWebV.setVisibility(View.GONE);
				if (mContentWebV.canGoBack()) {
					mContentWebV.goBack();
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);

			}

		});
		
		mContentWebV.setWebChromeClient(new WebChromeClient() {
			// 扩充缓存的容量
			@Override
			public void onReachedMaxAppCacheSize(long spaceNeeded,
					long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
				quotaUpdater.updateQuota(spaceNeeded * 2);
			}
			
		});
		initStatusBar();
	}
	private void initStatusBar() {
		Window window = getWindow();
		window.setStatusBarColor(getResources().getColor(R.color.color_fafafa));

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		try {
			Class statusBarManagerClazz = Class.forName("android.app.StatusBarManager");
			Field grayField = statusBarManagerClazz.getDeclaredField("STATUS_BAR_INVERSE_GRAY");
			Object gray = grayField.get(statusBarManagerClazz);
			Class windowManagerLpClazz = lp.getClass();
			Field statusBarInverseField = windowManagerLpClazz.getDeclaredField("statusBarInverse");
			statusBarInverseField.set(lp,gray);
			getWindow().setAttributes(lp);
		} catch (Exception e) {
		}
	}
	private void initUI() {
		TextView titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText(R.string.alloutlets);
		mContentWebV = (WebView) findViewById(R.id.content_webV);
		mErrImv = (ImageView) findViewById(R.id.imv_err);
	}


	public void back_clk(View v) {
		finish();
	}

	@Override
	public void finish() {

		super.finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public void onDataBack(int id, Object object) {

		// TODO Auto-generated method stub
		finish();

	}

	/**
	 * 返回上一次浏览的页面
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mContentWebV.canGoBack()) {
			mContentWebV.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * HTML数据存储
	 */
	private void saveData(WebSettings mWebSettings) {
		mWebSettings.setDomStorageEnabled(true);
		mWebSettings.setDatabaseEnabled(true);
		mWebSettings.setAppCacheEnabled(true);
		mWebSettings.setAppCacheMaxSize(1024 * 1024 * 16);
		String appCachePath = getApplicationContext().getCacheDir()
				.getAbsolutePath();
		mWebSettings.setAppCachePath(appCachePath);
		File fileRoot = new File(FileUtils.getExternalStoragePath());
		if (!fileRoot.exists()) {
			fileRoot.mkdirs();
		}
		mWebSettings.setDatabasePath(FileUtils.getExternalStoragePath());
	}

	/***
	 * 防止WebView加载内存泄漏
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mContentWebV.removeAllViews();
		mContentWebV.destroy();
	}

}
