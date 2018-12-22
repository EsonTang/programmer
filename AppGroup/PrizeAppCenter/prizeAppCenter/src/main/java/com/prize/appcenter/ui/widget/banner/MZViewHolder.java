package com.prize.appcenter.ui.widget.banner;

import android.content.Context;
import android.view.View;

/*
 * Created by longbaoxiu on 17/8/16.
 */

public interface MZViewHolder<T> {
    /**
     *  创建View
     * @param context Context
     * @return View
     */
    View createView(Context context);

    /**
     * 绑定数据
     * @param context
     * @param position
     * @param data
     */
    void onBind(Context context, int position, T data);
}
