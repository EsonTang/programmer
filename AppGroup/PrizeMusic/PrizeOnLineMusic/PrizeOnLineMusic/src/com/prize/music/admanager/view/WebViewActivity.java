package com.prize.music.admanager.view;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Stack;

import org.xutils.common.util.LogUtil;

import com.prize.app.beans.ClientInfo;
import com.prize.music.R;
import com.prize.music.admanager.Configs;
import com.prize.music.admanager.presenter.AdJumpManager;
import com.prize.music.helpers.utils.CommonClickUtils;
import com.prize.music.helpers.utils.PreferencesUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


/***
 * 内嵌WebView页面
 *
 */
public class WebViewActivity extends Activity {

	private final String TAG = "WebViewActivity";
	
	private WebView mWebView;
	
	private ProgressBar mProgressBar;
	
	private TextView titleView;
	
	private String mUrl = null;
	/**需要加载的URL(string)*/
	public static final String P_URL = "p_loadUrl";
	
	/**需要加载的应用的URL(string)*/
	public static final String P_APP_URL = "p_appUrl";
	
	Stack<String> urlStack = new Stack<String>();
	
	private int count = 0;
	/**activity是否结束*/
	private boolean isDestroy = false;
	
	private View mBottomView, mErrorView;
	
	private String mTitle = null;
	
	private String preUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		initStatusBar();
		
		/*if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		}*/
    	// BUG解决:状态栏反转必须要在setContentView之前调用
		changeStatus(getWindow());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);      
        initView();
	}
	
	/***
	 * 改变状态栏反色
	 * @param mWindow
	 */
	public static void changeStatus(Window mWindow) {
		LayoutParams lp = mWindow.getAttributes();
		Field[] fields = LayoutParams.class.getDeclaredFields();
		boolean b = false;
		for (int i = 0; i < fields.length; i++) {// 暂时如此处理，防止在不同的手机crash
			if (fields[i].getName().equals("statusBarInverse")) {
				b = true;
				break;
			}
		}
		
		if (b) {
			lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
			mWindow.setAttributes(lp);
		}
	}
	
	protected void initStatusBar() {
		Window window = getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(getResources().getColor(
					R.color.status_color));
		}
	}
	
//	public static boolean isIntentAvailable(Context context, Intent intent) {
//        final PackageManager packageManager = context.getPackageManager();
//        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
//                GET_ACTIVITIES);
//        return list.size() > 0;
//    }
	
	private class MyWebViewDownLoadListener implements DownloadListener{   
		  
        @Override  
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,   
                                    long contentLength) {
            Uri uri = Uri.parse(url);   
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);   
            startActivity(intent);
            finish();
        }   
    }  
	
	/***
	 * 初始化控件
	 */
	private void initView() {
		
		Intent it = getIntent();
		
		if (it != null) {
			mUrl = it.getStringExtra(P_URL);
			
			if (TextUtils.isEmpty(mUrl)) {
				finish();
				return;
			}
		}
		
		mHandler = new WebViewHandler(this);
		
		mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
		
		titleView = (TextView) findViewById(R.id.tv_title);
		
		
		mWebView = (WebView) findViewById(R.id.web_view);
		
		mErrorView = findViewById(R.id.error_lay);
		
		//下载文件
		mWebView.setDownloadListener(new MyWebViewDownLoadListener());
		
		// 打开JS可用
		setJsEnabled(true);
		
		WebChromeClient webChrome = new WebChromeClient() {  
            @Override  
            public void onReceivedTitle(WebView view, String title) {  
                super.onReceivedTitle(view, title);  
                LogUtil.i("===>onReceivedTitle==" + title);
                mTitle = title;
                titleView.setText(title);  
            }  
            
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            	LogUtil.i("===>onProgressChanged==" + newProgress);
            	super.onProgressChanged(view, newProgress);
            	if (newProgress > 80) {
            		if (newProgress > 89) {
            			if (mHandler != null)
            			mHandler.removeMessages(INCREASE_MSG);
        				mProgressBar.setProgress(0);
            		}
            		else
            			mProgressBar.setProgress(newProgress);
            	}
            }
  
        };
        mWebView.setWebChromeClient(webChrome);
        
        WebViewClient client = new WebViewClient() {
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				LogUtil.i("===>shouldOverrideUrlLoading==" + url);
				/*//if (count > 0) {
					if (!url.equals(preUrl) && !urlStack.contains(url))
						urlStack.push(url);
				//}
				preUrl = url;
				count ++;
				view.loadUrl(url);
				view.loadUrl(url);
				return true;*/
				try {
					if (url.startsWith("weixin://wap/pay?")) {
						Uri uri = Uri.parse(url);
						Intent intent =new Intent(Intent.ACTION_VIEW, uri);
						//boolean isIntent = isIntentAvailable(getApplicationContext(), intent);
						//if(isIntent){
//							startActivity(intent);
//						}else{
//						    Toast t = Toast.makeText(getApplicationContext(), "未检测到微信客户端，请安装后重试", Toast.LENGTH_LONG);
//						    t.setGravity(Gravity.BOTTOM, 0, 100);
//						    t.show();
//						}
//						return true;
		            }
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!url.startsWith("http")) {
	                super.shouldOverrideUrlLoading(view, url);
	                return true;
				}
				return false;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				LogUtil.i("===>onPageFinished==");
				if (mHandler != null)
				mHandler.removeMessages(INCREASE_MSG);
				mProgressBar.setProgress(0);
				super.onPageFinished(view, url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				LogUtil.i("===>onPageStarted==");
				if (TextUtils.isEmpty(mTitle))
					titleView.setText(getString(R.string.loading));
				// mProgressBar.setProgress(0);
				mProgressBar.setVisibility(View.VISIBLE);
				// 虚拟加载进度条
				if (mHandler != null)
				mHandler.sendEmptyMessage(INCREASE_MSG);
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				if(errorCode == -10) return;
				super.onReceivedError(view, errorCode, description, failingUrl);
				LogUtil.i("===>onReceivedError==errorCode" + errorCode);
				mWebView.setVisibility(View.GONE);
				mErrorView.setVisibility(View.VISIBLE);
				// 加载出错后的处理
			}
			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				// TODO Auto-generated method stub
				//super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}
		};
		mWebView.setWebViewClient(client);
		
		String ua = PreferencesUtils.getString(this, Configs.UA);	
		mWebView.getSettings().setUserAgentString(ua);	
		
		
		if (TextUtils.isEmpty(preUrl))
			urlStack.push(mUrl);
		mWebView.loadUrl(mUrl);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void setJsEnabled(boolean isEnabled) {
		WebSettings settings = mWebView.getSettings();
		settings.setJavaScriptEnabled(isEnabled);
		if (isEnabled) {
			mWebView.addJavascriptInterface(new JsObject(), "javaObject");
		}
		
		settings.setLoadWithOverviewMode(true);
		settings.setDomStorageEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setDefaultTextEncodingName("utf-8");
		settings.setTextSize(WebSettings.TextSize.NORMAL);
		//settings.setUseWideViewPort(true);
		mWebView.requestFocusFromTouch();
	}
	@Override
	public void onBackPressed() {
		if (CommonClickUtils.isFastDoubleClick()) 
		return;
		// TODO Auto-generated method stub
		if (mWebView.canGoBack()) {//urlStack.size() > 1 && 
			mWebView.goBack();
//			urlStack.pop();
//			mWebView.loadUrl(urlStack.peek());
		}
		else {
			hideInputMethod();			
			//super.onBackPressed();
			AdJumpManager.onJumpMainActivity(this);
			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);  
			finish();
		}		
	}
	
	/** 
     * Hides the input method. 
     */  
    protected void hideInputMethod() {  
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);  
        if (imm != null) {
        	View v = getCurrentFocus();
        	if (v != null)
        		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }  
    }
    
	@SuppressWarnings("deprecation")
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
			case R.id.btn_refresh:
				if(isFastClick(2000)) return;
				mWebView.loadUrl(mUrl);
				break;
			case R.id.btn_back:
				hideInputMethod();
				AdJumpManager.onJumpMainActivity(getApplicationContext());
				finish();
				break;
			case R.id.btn_reload:
				if (mHandler != null)
				mHandler.resetTimes();
				mProgressBar.setProgress(0);
				if(ClientInfo.getInstance().getAPNType(getApplicationContext())!=ClientInfo.NONET){
					mWebView.clearView();
					mWebView.loadUrl(mUrl);
					mWebView.setVisibility(View.VISIBLE);
					mErrorView.setVisibility(View.GONE);
				}else{
					Toast t = Toast.makeText(getApplicationContext(), "网络错误，请检查网络", Toast.LENGTH_SHORT);
				    t.setGravity(Gravity.BOTTOM, 0, 100);
				    t.show();
				}
				break;
			case R.id.btn_set_net:
				Intent aIt = new Intent(android.provider.Settings.ACTION_SETTINGS);
				startActivity(aIt);
				aIt = null;
				break;
		}
	}
	
	 /**
     * 重复点击
     */

    private static long lastClickTime;

    public synchronized static boolean isFastClick(long tweentime) {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < tweentime) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
    
	/***
	 * 模拟增加进度
	 */
	void increaseProgress() {
		int progress = mProgressBar.getProgress();
		if (progress > 80)
			return;
		
		progress += 10;
		if (progress > 80)
			progress = 80;
		mProgressBar.setProgress(progress);
	}
	/**模拟增加进度条进度*/
	private static final int INCREASE_MSG = 1;
	
	private static WebViewHandler mHandler;
	
	class WebViewHandler extends Handler {
		WeakReference<WebViewActivity> ref = null;
		
		private int times = 0;
		
		public WebViewHandler(WebViewActivity act) {
			ref = new WeakReference<WebViewActivity>(act);
		}
		
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case INCREASE_MSG:
				if (null == ref)
					return;
				times ++;
				WebViewActivity a = ref.get();
				if (a != null) {
					a.increaseProgress();
				}
				if (times < 9) {
					removeMessages(INCREASE_MSG);
					sendEmptyMessageDelayed(INCREASE_MSG, 120);
				}
				break;
			}
		}
		
		public void resetTimes() {
			times = 0;
		}
		
		public void stopAdd() {
			times = 9;
		}
	}
	
	class JsObject {
		@JavascriptInterface
		public String toString() {
			return "donotInject";
		}
	}
	@Override
	protected void onDestroy() {
		if (mWebView != null) {
			RelativeLayout a = (RelativeLayout)mWebView.getParent();
			if (a != null)
				a.removeView(mWebView);
			mWebView.removeAllViews();
			mWebView.clearFormData();
			mWebView.clearHistory();
			mWebView.clearSslPreferences();
			mWebView.clearView();
			mWebView.destroy();
			mWebView = null;
		}
		urlStack = null;
		mHandler = null;
		isDestroy = true;
		super.onDestroy();
//		System.exit(0);

	}
}
