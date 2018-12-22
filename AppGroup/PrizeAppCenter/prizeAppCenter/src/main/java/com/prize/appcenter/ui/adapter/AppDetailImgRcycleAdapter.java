package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.app.beans.ClientInfo;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppDetailImgActivity;
import com.prize.appcenter.ui.util.ImageUtil;
import com.prize.appcenter.ui.util.UILimageUtil;


/**
 * 详情页的截图adapter
 * longbaoxiu
 */
public class AppDetailImgRcycleAdapter extends RecyclerView.Adapter<AppDetailImgRcycleAdapter.ViewHolder> {
    private String[] paths;
    private Context mContext;
    private View.OnClickListener mOnImgClickListener;
    private int leftMargin;
    private LinearLayout.LayoutParams params;

    public AppDetailImgRcycleAdapter(Context mContext) {
        this.mContext = mContext;
        mOnImgClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppDetailImgRcycleAdapter.this.mContext, AppDetailImgActivity.class);
                intent.putExtra("paths", AppDetailImgRcycleAdapter.this.paths);
                intent.putExtra("index", (Integer) v.getTag());
                AppDetailImgRcycleAdapter.this.mContext.startActivity(intent);
            }
        };
        int w;
        int h;
        if (ClientInfo.getInstance().screenHeight >= 2000) {
            w = 270 * 2;
            h = 478 * 2;
            leftMargin = 16;
        } else {
            w = mContext.getResources().getInteger(R.integer.image_wight);
            leftMargin = mContext.getResources().getInteger(R.integer.leftMargin);
            h = mContext.getResources().getInteger(R.integer.image_height);
        }
        params = new LinearLayout.LayoutParams(w, h);
    }

    private boolean isNewStyle = false;

    public void setPaths(String[] paths, boolean isNewStyle) {
        this.paths = paths;
        this.isNewStyle = isNewStyle;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 实例化展示的view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appdetail_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        ImageLoader.getInstance().displayImage(paths[position], viewHolder.itemImg, isNewStyle? UILimageUtil
                .getNoLoadLoptions():UILimageUtil.getUINoChcheLoptions(R.drawable.detail_big_icon_defualt), null);
        viewHolder.itemImg.setTag(position);
        viewHolder.itemImg.setDrawingCacheEnabled(true);
        viewHolder.itemImg.setOnClickListener(mOnImgClickListener);
    }


    @Override
    public int getItemCount() {
        return paths == null ? 0 : paths.length;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImg;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImg = (ImageView) itemView.findViewById(R.id.img_id);
            params.leftMargin = leftMargin;
            itemImg.setLayoutParams(params);
        }
    }

    private static class MyImageLoadingListener implements ImageLoadingListener {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage == null) {
                return;
            }
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();
            if (loadedImage != null && view != null
                    && width > height) {
                Bitmap bitmap = ImageUtil.adjustPhotoRotation(loadedImage, 90);
                if (bitmap != null) {
                    ((ImageView) view).setImageBitmap(bitmap);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    }

}
