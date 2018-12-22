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
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.appcenter.R;

/**
 **
 * 垃圾清理勾选提示框
 * 
 * @author 聂礼刚
 * @version V1.0
 */
public class ClearTrashCheckDialog extends AlertDialog implements View.OnClickListener {
	private Button add_neg;
	private Button sureBtn;
	public final int CANCEL = 0;
	public final int SURE = 1;

	public ClearTrashCheckDialog(Context context, int theme) {

		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_edit_name);
		TextView content_tv = (TextView) findViewById(R.id.content_tv);
		String content = "您勾选内容为<font color=#12b7f5>谨慎清理内容</font>，删除后该内容无法查看或使用";
		content_tv.setText(Html.fromHtml(content));
		TextView title_tv = (TextView) findViewById(R.id.title_tv);
		findViewById(R.id.blue_line).setBackgroundColor(BaseApplication.curContext.getResources().getColor(R.color.text_color_12b7f5));
		title_tv.setText(R.string.clear_sdk_clear_check_title);
		add_neg = (Button) findViewById(R.id.add_neg);
		add_neg.setText(R.string.cancel);
		sureBtn = (Button) findViewById(R.id.sure_Btn);
		sureBtn.setText(R.string.clear_sdk_clear_check_yes);
		sureBtn.setOnClickListener(this);
		add_neg.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.add_neg:
				mOnButtonClick.onClick(CANCEL);
				break;
			case R.id.sure_Btn:
				mOnButtonClick.onClick(SURE);
				break;

		}
	}

	/**
	 * 点击button后的回调
	 *
	 * @author longbaoxiu
	 * @version V1.0
	 */
	public interface OnButtonClick {
		void onClick(int which);
	}

	public OnButtonClick mOnButtonClick;

	public void setmOnButtonClick(OnButtonClick mOnButtonClick) {
		this.mOnButtonClick = mOnButtonClick;
	}

}
