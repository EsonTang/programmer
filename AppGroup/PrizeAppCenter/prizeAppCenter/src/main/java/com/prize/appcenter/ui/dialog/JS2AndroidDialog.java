/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.appcenter.R;

/**
 **
 * JS与android交互
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class JS2AndroidDialog extends AlertDialog implements OnClickListener {
    private Button add_neg;
    private Button sureBtn;
    public final int CANCEL = 0;
    public final int SURE = 1;

    public JS2AndroidDialog(Context context, int theme) {

        super(context, theme);
    }

    TextView content_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_edit_name);
        content_tv = (TextView) findViewById(R.id.content_tv);
        TextView title_tv = (TextView) findViewById(R.id.title_tv);
        findViewById(R.id.blue_line).setBackgroundColor(BaseApplication.curContext.getResources().getColor(R.color.text_color_12b7f5));
        title_tv.setText(R.string.tip);
        add_neg = (Button) findViewById(R.id.add_neg);
        add_neg.setText(R.string.cancel);
        sureBtn = (Button) findViewById(R.id.sure_Btn);
        sureBtn.setText(R.string.confirm);
        sureBtn.setOnClickListener(this);
        add_neg.setOnClickListener(this);
        setCanceledOnTouchOutside(false);
    }

    public void setContent(String content) {
        if (content_tv != null&& !TextUtils.isEmpty(content))
            content_tv.setText(content);
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
