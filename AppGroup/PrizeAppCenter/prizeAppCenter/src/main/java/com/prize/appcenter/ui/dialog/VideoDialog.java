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
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;

/**
 **
 * 仅允许在wifi环境下看视频的提示框
 * 
 * @author nieligang
 * @version V1.0
 */
public class VideoDialog extends AlertDialog implements OnClickListener {
	private Button add_neg;
	private Button sureBtn;
	public final int CANCEL = 0;
	public final int SURE = 1;

	public VideoDialog(Context context, int theme) {

		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.fragment_edit_name);
		TextView content_tv = (TextView) findViewById(R.id.content_tv);
		content_tv.setText(R.string.toast_tip_video_only_wifi);
		TextView title_tv = (TextView) findViewById(R.id.title_tv);
		findViewById(R.id.blue_line).setBackgroundColor(BaseApplication.curContext.getResources().getColor(R.color.text_color_12b7f5));
		title_tv.setText(R.string.tip);
		add_neg = (Button) findViewById(R.id.add_neg);
		add_neg.setText(R.string.cancel);
		sureBtn = (Button) findViewById(R.id.sure_Btn);
		sureBtn.setText(R.string.tuhao_go_on);
		sureBtn.setOnClickListener(this);
		add_neg.setOnClickListener(this);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		WindowManager m = getWindow().getWindowManager();
		Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
		JLog.i("0000","d.getWidth()="+d.getWidth());
		params.width = (int) (d.getWidth() * 0.9);    //宽度设置为屏幕的0.9
		    //设置生效
		getWindow().setAttributes(params);
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
