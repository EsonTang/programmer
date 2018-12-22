package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;

/**
 * 弹幕item View
 * longbaoxiu
 *  2017/4/10.20:14
 *
 */

public class SubCardAppsinView extends LinearLayout{
    private ImageView appImgs_Iv;
    private TextView appName_Tv;
    public SubCardAppsinView(Context context) {
        this(context, null);
    }

    public SubCardAppsinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
    }

    private void initUI(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        View view = inflate(context, R.layout.sub_appsinview_layout, this);
        appImgs_Iv = (ImageView) view.findViewById(R.id.appImgs_Iv);
        appName_Tv = (TextView) view.findViewById(R.id.appName_Tv);
    }
    public void setData(AppsItemBean bean){
        if(bean==null||appImgs_Iv==null||appName_Tv==null)
            return;
        String url= TextUtils.isEmpty(bean.largeIcon)?bean.iconUrl:bean.largeIcon;
        ImageLoader.getInstance().displayImage(url, appImgs_Iv, UILimageUtil.getUILoptions());
        if(bean.name.length()>5){
            appName_Tv.setText(bean.name.substring(0,4)+"...");
        }else{
            appName_Tv.setText(bean.name);
        }

   }
}
