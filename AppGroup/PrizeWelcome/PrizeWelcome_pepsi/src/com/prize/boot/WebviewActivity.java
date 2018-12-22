package com.prize.boot;


import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

public class WebviewActivity extends BaseActivity{

	private TextView mContentTv;
//	private static final String FILE_PATH = "file:///android_asset/html/statement.html";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		mContentTv = (TextView) findViewById(R.id.tv_content);
		loadData();
	}
	
	private void loadData() {
		mContentTv.setText(R.string.agreement_content);
		mContentTv.setMovementMethod(ScrollingMovementMethod.getInstance());
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.title_id) {
			finish();
		}
	}

}
