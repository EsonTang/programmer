package com.prize.container.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.util.LogUtil;
import com.prize.container.ContainerSavePage;

import java.util.ArrayList;
import com.android.gallery3d.R;

public class HorizontalScrollLayoutAdapter {

    protected static final String TAG = "HorizontalScrollLayoutAdapter";
    private LayoutInflater mInflater;
    private ContainerSavePage.Model mModel;
    private int mLayoutId;
    private SelectHorizontalScrollerLayout mAdapterView;
    private ArrayList<String> mPathList;
    private boolean[] mSelectPathArr;

    public HorizontalScrollLayoutAdapter(Context context, ContainerSavePage.Model model, int layoutId, SelectHorizontalScrollerLayout adapterView) {
        mLayoutId = layoutId;
        mInflater = LayoutInflater.from(context);
        this.mModel = model;
        mAdapterView = adapterView;
    }

    public int getCount() {
        return mModel.getTotalCount();
    }

    public Object getItem(int position) {
        return mModel.getScreenNail(position).getBitmap();
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(mLayoutId, parent, false);
            viewHolder.mIm = (ImageView) convertView.findViewById(R.id.im);
            viewHolder.mMaskIm = (ImageView) convertView.findViewById(R.id.im_mask);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        ScreenNail screenNail = mModel.getScreenNail(position);
        if (mPathList == null) {
            mPathList = new ArrayList<>(getCount());
            mSelectPathArr = new boolean[getCount()];
        }
        MediaItem item = mModel.getMediaItem(position);
        if (item != null) {
            LogUtil.i(TAG, "getView bitmap item=" + item.getPath() + " position=" + position);
            Path path = item.getPath();
            mPathList.add(position, path.toString());
            viewHolder.mIm.setImageBitmap(mAdapterView.getBitmapByPath(path));
        }
        return convertView;
    }

    public void updateView(int position, View convertView) {
        LogUtil.i(TAG, "updateView bitmap convertView=" + convertView + " position=" + position);
        if (convertView != null) {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            String path = mPathList.get(position);
            if (path != null) {
                viewHolder.mIm.setImageBitmap(mAdapterView.getBitmapByPath(path));
                if (mSelectPathArr[position]) {
                    viewHolder.mMaskIm.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.mMaskIm.setVisibility(View.GONE);
                }
            } else {
                MediaItem item = mModel.getMediaItem(position);
                if (item != null) {
                    LogUtil.i(TAG, "updateView bitmap item=" + item.getPath() + " position=" + position);
                    path = item.getPath().toString();
                    mPathList.set(position, path);
                    viewHolder.mIm.setImageBitmap(mAdapterView.getBitmapByPath(path));
                    if (mSelectPathArr[position]) {
                        viewHolder.mMaskIm.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.mMaskIm.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    public void toggle(String path) {
        if (mPathList != null) {
            int index = mPathList.indexOf(path);
            mSelectPathArr[index] = !mSelectPathArr[index];
        }
    }

    private class ViewHolder {
        ImageView mIm;
        ImageView mMaskIm;
    }

}

