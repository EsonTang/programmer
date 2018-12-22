package com.android.launcher3.search.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class SearchWebView extends TextView implements OnClickListener {

	private EditText mEdit;

	public SearchWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SearchWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnClickListener(this);
	}

	public SearchWebView(Context context) {
		super(context);
	}

	@Override
	public void onClick(View v) {
		String str = mEdit.getText().toString();

		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri uri = Uri.parse("http://www.google.com/#q=" + str);
		intent.setData(uri);
		if (!TextUtils.isEmpty(str)) {
			this.getContext().startActivity(intent);
		}else {

			 uri = Uri.parse("http://www.google.com/");
			intent.setData(uri);
			this.getContext().startActivity(intent);
		}
	}

	public void setEdit(EditText mEdit) {
		this.mEdit = mEdit;
	}

}
