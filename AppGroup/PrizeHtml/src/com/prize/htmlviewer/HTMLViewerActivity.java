/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prize.htmlviewer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Color;
import java.lang.reflect.Field;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import android.view.View.OnLongClickListener;

import android.os.Build;
import java.io.File;
/**
 * Simple activity that shows the requested HTML page. This utility is
 * purposefully very limited in what it supports, including no network or
 * JavaScript.
 */
public class HTMLViewerActivity extends Activity {
    private static final String TAG = "HTMLViewer";

    private WebView mWebView;
    private View mLoading;
    /// M: add auto-detector for HTML viewer
    private static final String ENCODING_AUTO_DETECTED = "auto-detector";
	
	private static final String FILE_PATH = "file:///android_asset/html/statement.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
		initStatusBar();
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setBackgroundColor(getResources().getColor(R.color.cotent_color));
        mLoading = findViewById(R.id.loading);

        mWebView.setWebViewClient(new ViewClient());
		mWebView.setOnLongClickListener(new OnLongClickListener() {  
            
          @Override  
          public boolean onLongClick(View v) {  
              return true;  
          }  
		});

        WebSettings s = mWebView.getSettings();
        s.setUseWideViewPort(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setSavePassword(false);
        s.setSaveFormData(false);
        s.setBlockNetworkLoads(true);

        // Javascript is purposely disabled, so that nothing can be
        // automatically run.
        s.setJavaScriptEnabled(false);
        /// M: add auto-detector for HTML viewer
        //s.setDefaultTextEncodingName("utf-8");
        s.setDefaultTextEncodingName(ENCODING_AUTO_DETECTED);

		final Intent intent = getIntent();
		String url = intent.getDataString();
		String url2 = url.substring(7);
		url = "file"+url2;
        mWebView.loadUrl(url==null? FILE_PATH:url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }
	
	public void onClick(View v) {
		if (v.getId() == R.id.title_id) {
			finish();
		}
	}

    private class ViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            mLoading.setVisibility(View.GONE);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                WebResourceRequest request) {
            final Uri uri = request.getUrl();
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())
                    && uri.getPath().endsWith(".gz")) {
                Log.d(TAG, "Trying to decompress " + uri + " on the fly");
                try {
                    final InputStream in = new GZIPInputStream(
                            getContentResolver().openInputStream(uri));
                    final WebResourceResponse resp = new WebResourceResponse(
                            getIntent().getType(), "utf-8", in);
                    resp.setStatusCodeAndReasonPhrase(200, "OK");
                    return resp;
                } catch (IOException e) {
                    Log.w(TAG, "Failed to decompress; falling back", e);
                }
            }
            return null;
        }
    }
	
	private void initStatusBar() {
        Window window = getWindow();
//        window.requestFeature(Window.FEATURE_NO_TITLE);
//        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(getResources().getColor(R.color.title_color));

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
}
