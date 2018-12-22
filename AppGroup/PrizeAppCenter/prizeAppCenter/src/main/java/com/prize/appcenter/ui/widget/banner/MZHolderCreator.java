package com.prize.appcenter.ui.widget.banner;


/*
 * Created by longbaoxiu on 17/8/16.
 */

public interface MZHolderCreator<VH extends MZViewHolder> {
    /**
     * 创建ViewHolder
     * @return VH
     */
    VH createViewHolder();
}
