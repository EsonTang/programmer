package com.prize.appcenter.ui.dialog;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.ShortcutUtil;

/**
 * *
 * 提示创建快捷键弹出提示dialog
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class ShortCutCautionDialog extends Dialog implements
        View.OnClickListener {

    public ShortCutCautionDialog(Context context) {
        super(context);
    }

    private Context mContext;


    public ShortCutCautionDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window == null)
            return;
        window.setGravity(Gravity.CENTER);
        window.addFlags(Window.FEATURE_NO_TITLE);
        window.setWindowAnimations(R.style.popwindow_anim_style);
        setContentView(R.layout.fragment_shortcut_dialog);
        window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        ImageView add_neg = (ImageView) findViewById(R.id.add_neg);
        ImageView head_img = (ImageView) findViewById(R.id.head_img);
        add_neg.setOnClickListener(this);
        head_img.setOnClickListener(this);

    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_neg:
                this.dismiss();
                break;
            case R.id.head_img:// 创建快捷键
                ShortcutUtil.createShortCut(mContext.getApplicationContext(),"必备软件");
                this.dismiss();
                break;
            default:
                this.dismiss();
                break;
        }
    }

}
