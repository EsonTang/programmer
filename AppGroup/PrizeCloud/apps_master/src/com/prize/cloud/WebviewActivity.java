package com.prize.cloud;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class WebviewActivity extends BaseActivity{
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_URL = "url";

	private WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		String url = this.getIntent().getStringExtra(EXTRA_URL);
		String title =this.getIntent().getStringExtra(EXTRA_TITLE);

		TextView tv = (TextView)findViewById(R.id.title_id);
		if(title!=null)
			tv.setText(title);


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
				
	}
}
