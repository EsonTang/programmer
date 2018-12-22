package com.prize.appcenter.ui.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.prize.app.net.datasource.base.AppDetailData.GiftsItem;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;

public class lookupGiftDialog extends Dialog implements
		android.view.View.OnClickListener {
	private Context mContext;
	private GiftsItem item;
	private AppsItemBean appItem;
	private String code;
	private TextView mCopyTextView;
	public lookupGiftDialog(Context context, int theme, GiftsItem i,
			AppsItemBean appItem, String code) {
		super(context, theme);
		this.item = i;
		this.code = code;
		mContext = context;
		this.appItem = appItem;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Window window = getWindow();
		window.setGravity(Gravity.CENTER);
		setContentView(R.layout.lookupgift);
		findViewById(R.id.cancel).setOnClickListener(this);
		findViewById(R.id.open_game).setOnClickListener(this);
		findViewById(R.id.copy).setOnClickListener(this);
		mCopyTextView = (TextView) findViewById(R.id.copy_edit);
		TextView usage = (TextView) findViewById(R.id.textView3);
		mCopyTextView.setText(code);
		usage.setText(item.usage);

	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	/**
	 * 复制激活码
	 */
	private void copyFromCode() {
		try {
			ClipboardManager cmb = (ClipboardManager) mContext
					.getSystemService(Context.CLIPBOARD_SERVICE);
			JLog.i("0000", "item.activationCode=" + item.activationCode
					+ "---code=" + code);
			if (!TextUtils.isEmpty(code)) {
				cmb.setPrimaryClip(ClipData.newPlainText(null, code.trim()));
			} else {
				cmb.setPrimaryClip(ClipData.newPlainText(null,
						item.activationCode.trim()));
			}
			ToastUtils.showToast(R.string.copy_ok);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.open_game:

			UIUtils.startGame(appItem);
			this.dismiss();
			break;
		case R.id.copy:
			copyFromCode();
			break;
		default:
			this.dismiss();
			break;
		}
	}

}
