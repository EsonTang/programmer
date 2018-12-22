package com.prize.appcenter.ui.adapter.ryclvadapter;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;

/**
 *RecyclerView adapter基类
 */
public abstract class BaseAppListRycVAdapter <T extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected Handler mHandler;
    protected Drawable drawable;
    protected ColorDrawable transparentDrawable;
    protected LinearLayout.LayoutParams param2 = null;
    protected LinearLayout.LayoutParams param;
    protected WeakReference<Activity> mActivities;


}
