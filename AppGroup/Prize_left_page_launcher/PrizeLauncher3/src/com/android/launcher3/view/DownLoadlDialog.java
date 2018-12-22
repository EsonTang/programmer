package com.android.launcher3.view;

import com.android.launcher3.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DownLoadlDialog extends AlertDialog implements
		android.view.View.OnClickListener {

	private Button OK;
	private Button cancel;

	public DownLoadlDialog(Context context,int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_dialog);

		OK = (Button) findViewById(R.id.uninstall_ex);
		cancel = (Button) findViewById(R.id.cancel_uninstall_ex);

		OK.setOnClickListener(this);
		cancel.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.uninstall_ex:
			onItemClick.onClick(true);
			break;
		case R.id.cancel_uninstall_ex:
			onItemClick.onClick(false);
			break;
		}
	}

	/** 点击的回调 */
	public static interface OnUninstallClick {
		void onClick(boolean which);
	}

	private OnUninstallClick onItemClick;

	public void setOnItemClick(OnUninstallClick onItemClick) {
		this.onItemClick = onItemClick;
	}
}
