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

package com.prize.appcenter.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.appcenter.R;

/**
 **
 * 删除下载任务提示框
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DelTaskDialog extends AlertDialog implements OnClickListener {
	private Button add_neg;
	private Button sureBtn;
	public final int CANCEL = 0;
	public final int SURE = 1;

	public DelTaskDialog(Context context, int theme) {

		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_edit_name);
		TextView content_tv = (TextView) findViewById(R.id.content_tv);
		content_tv.setText(R.string.dialog_tip_download_delete);
		TextView title_tv = (TextView) findViewById(R.id.title_tv);
		findViewById(R.id.blue_line).setBackgroundColor(BaseApplication.curContext.getResources().getColor(R.color.text_color_12b7f5));
		title_tv.setText(R.string.tip);
		add_neg = (Button) findViewById(R.id.add_neg);
		add_neg.setText(R.string.delete);
		sureBtn = (Button) findViewById(R.id.sure_Btn);
		sureBtn.setText(R.string.cancel);
		sureBtn.setOnClickListener(this);
		add_neg.setOnClickListener(this);
		setCanceledOnTouchOutside(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_neg:
			mOnButtonClic.onClick(CANCEL);
			break;
		case R.id.sure_Btn:
			mOnButtonClic.onClick(SURE);
			break;

		}
	}

	/**
	 * 点击button后的回调
	 * 
	 * @author longbaoxiu
	 * @version V1.0
	 */
	public static interface OnButtonClic {
		void onClick(int which);
	}

	public OnButtonClic mOnButtonClic;

	public void setmOnButtonClic(OnButtonClic mOnButtonClic) {
		this.mOnButtonClic = mOnButtonClic;
	}

}
