package com.prize.prizeappoutad.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.prizeappoutad.R;
import com.prize.prizeappoutad.utils.AppManager;

public class WebviewActivity extends BaseActivity {
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_URL = "url";

	private WebView webview;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		AppManager.getAppManager().addActivity(this);
		String url = this.getIntent().getStringExtra(EXTRA_URL);
		String title = this.getIntent().getStringExtra(EXTRA_TITLE);

		webview = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowFileAccess(true);

		webview.loadUrl(url);
		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				webview.loadUrl(url);
				return true;
			}
		});
		mTitle = (TextView) findViewById(R.id.title_id);
		ImageView titleBack = (ImageView) findViewById(R.id.actionbar_title_back);
		mTitle.setText(title);
		titleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

}
