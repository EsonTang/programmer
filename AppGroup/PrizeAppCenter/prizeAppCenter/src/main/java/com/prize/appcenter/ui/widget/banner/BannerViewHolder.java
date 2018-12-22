package com.prize.appcenter.ui.widget.banner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HomeAdBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;

/**
 *  longbaoxiu
 *  2017/10/12.21:38
 */

public class BannerViewHolder implements MZViewHolder<HomeAdBean>  {
    private ImageView mImageView;
    @Override
    public View createView(Context context) {
        // 返回页面布局文件
        View view = LayoutInflater.from(context).inflate(R.layout.banner_item, null);
        mImageView = (ImageView) view.findViewById(R.id.banner_image);
        return view;
    }

    @Override
    public void onBind(Context context, int position, HomeAdBean data) {
        // 数据绑定
        ImageLoader.getInstance().displayImage(data.imageUrl, mImageView,
                UILimageUtil.getHomeADCacheUILoptions(), null);
    }
}
