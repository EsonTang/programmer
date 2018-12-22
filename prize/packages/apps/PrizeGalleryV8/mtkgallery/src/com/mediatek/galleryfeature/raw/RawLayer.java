package com.mediatek.galleryfeature.raw;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.gallery3d.R;
import com.mediatek.galleryframework.base.Layer;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.Player;
import com.mediatek.galleryframework.gl.MGLView;
import com.mediatek.galleryframework.util.MtkLog;

/**
 * When current photo is raw format, use this class to show raw indicator at
 * the right-bottom corner.
 */
public class RawLayer extends Layer {
    private static final String TAG = "MtkGallery2/RawLayer";

    private Activity mActivity;
    private ViewGroup mRawIndicator;
    private boolean mIsFilmMode;
    private MediaData mMediaData;

    @Override
    public void onCreate(Activity activity, ViewGroup root) {
        MtkLog.i(TAG, "<onCreate>");
        mActivity = activity;
        LayoutInflater flater = LayoutInflater.from(activity);
        mRawIndicator = (ViewGroup) flater.inflate(R.layout.m_raw, null, false);
        mRawIndicator.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume(boolean isFilmMode) {
        MtkLog.i(TAG, "<onResume>");
        mIsFilmMode = isFilmMode;
        updateIndicatorVisibility();
    }

    @Override
    public void onPause() {
        MtkLog.i(TAG, "<onPause>");
        mRawIndicator.setVisibility(View.INVISIBLE);
    }

    public void onDestroy() {
        MtkLog.i(TAG, "<onDestroy>");
    }

    @Override
    public void setData(MediaData data) {
        mMediaData = data;
    }

    @Override
    public void setPlayer(Player player) {
    }

    @Override
    public View getView() {
        return mRawIndicator;
    }

    @Override
    public MGLView getMGLView() {
        return null;
    }

    @Override
    public void onChange(Player player, int what, int arg, Object obj) {
    }

    @Override
    public void onFilmModeChange(boolean isFilmMode) {
        mIsFilmMode = isFilmMode;
        updateIndicatorVisibility();
    }

    private void updateIndicatorVisibility() {
        if (mIsFilmMode) {
            mRawIndicator.setVisibility(View.INVISIBLE);
        } else {
            mRawIndicator.setVisibility(View.VISIBLE);
        }
    }
}
