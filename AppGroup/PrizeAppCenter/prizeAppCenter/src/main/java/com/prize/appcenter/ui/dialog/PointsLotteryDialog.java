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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.prize.appcenter.R;

/**
 * *
 * 积分系统-提示框
 *
 * @author huangchangguo
 * @version V1.0
 */
public class PointsLotteryDialog extends AlertDialog implements OnClickListener {
    private Button add_neg;
    private Button sureBtn;
    public final int CANCEL = 0;
    public final int SURE   = 1;
    private TextView mContent_tv;
    private TextView mTitle_tv;

    public PointsLotteryDialog(Context context, int theme) {

        super(context, theme);
        // View view = LayoutInflater.from(context).inflate(
        // R.layout.fragment_edit_name, null);
        // setView(view, 0, 0, 0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_edit_name);
        mContent_tv = (TextView) findViewById(R.id.content_tv);
        mContent_tv.setText(R.string.toast_tip_download_only_wifi);
        mTitle_tv = (TextView) findViewById(R.id.title_tv);
        mTitle_tv.setText(R.string.tip);
        add_neg = (Button) findViewById(R.id.add_neg);
        add_neg.setText("取消");
        sureBtn = (Button) findViewById(R.id.sure_Btn);
        sureBtn.setText("确定");
        sureBtn.setOnClickListener(this);
        add_neg.setOnClickListener(this);
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

    //设置提示框内容
    public void setContent(String contentText) {
        if (contentText != null && mContent_tv != null) {
            mContent_tv.setText(contentText);
        }
    }

    //设置提示框内容
    public void setTitle(String titleText) {
        if (titleText != null && mTitle_tv != null) {
            mTitle_tv.setText(titleText);
        }
    }

    //设置提示框内容
    public void setSureBtn(String sureText) {
        if (sureText != null && sureBtn != null) {
            sureBtn.setText(sureText);
        }
    }

    /**
     * 点击button后的回调
     *
     * @author longbaoxiu
     * @version V1.0
     */
    public interface OnButtonClic {
        void onClick(int which);
    }

    public OnButtonClic mOnButtonClic;

    public void setmOnButtonClic(OnButtonClic mOnButtonClic) {
        this.mOnButtonClic = mOnButtonClic;
    }

}
