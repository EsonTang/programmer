/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.cloud.widgets;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

public class ProDialog extends ProgressDialog {
	private String msg;
	private Context mCtx;

	public ProDialog(Context context) {
		super(context);
	}

	public ProDialog(Context context, int theme) {
		super(context, theme);
	}

	public ProDialog(Context context, int theme, String msg) {
		super(context, theme);
		this.msg = msg;
		this.mCtx = context;
		initData();
	}

	private void initData() {
		this.setMessage(msg);
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(true);
		// this.setOnKeyListener(new DialogInterface.OnKeyListener() {
		// @Override
		// public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent
		// event) {
		// if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		// {
		// return true;
		// } else {
		// return false; //默认返回 false
		// }
		// }
		// });
	}

}
