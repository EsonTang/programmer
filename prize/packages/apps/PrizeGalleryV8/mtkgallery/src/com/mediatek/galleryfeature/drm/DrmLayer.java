package com.mediatek.galleryfeature.drm;

import android.app.Activity;
import android.content.DialogInterface;
import android.drm.DrmManagerClient;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.mediatek.galleryfeature.drm.DeviceMonitor.ConnectStatus;
import com.mediatek.galleryframework.base.Layer;
import com.mediatek.galleryframework.base.MediaCenter;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.Player;
import com.mediatek.galleryframework.gl.MGLView;
import com.mediatek.galleryframework.util.MtkLog;
import com.mediatek.omadrm.OmaDrmUtils;

/**
 * Call Drm Dialog for cousume the drm right.
 */
public class DrmLayer extends Layer implements DeviceMonitor.DeviceConnectListener {
    private static final String TAG = "MtkGallery2/DrmLayer";
    private boolean mInFilmMode;
    private Activity mActivity;
    private Menu mOptionsMenu;
    private MediaData mMediaData;
    private MediaCenter mMediaCenter;

    private Layer mRealLayer;
    private Player mRealPlayer = null;
    private DrmPlayer mPlayer;

    // another device plug
    private DeviceMonitor mDeviceMonitor;
    private boolean mIsFullScreenPreviewState;
    private String mCurrentFile;

    /**
     * Constructor.
     * @param mc
     *            The media center.
     */
    public DrmLayer(MediaCenter mc) {
        mMediaCenter = mc;
    }

    @Override
    public void onCreate(Activity activity, ViewGroup root) {
        Log.d(TAG, " <onCreate> this = " + this);
        mActivity = activity;
        mDeviceMonitor = new DeviceMonitor(activity);
        mDeviceMonitor.setConnectListener(this);
    }

    @Override
    public void onResume(boolean isFilmMode) {
        mInFilmMode = isFilmMode;
    }

    @Override
    public void onPause() {
        unbindRealLayer();
    }

    @Override
    public void onActivityResume() {
        if (mDeviceMonitor != null) {
            mDeviceMonitor.start();
        }
    }

    @Override
    public void onActivityPause() {
        if (mPlayer != null) {
            mIsFullScreenPreviewState = mPlayer.isFullScreenPreview();
        }
        if (mDeviceMonitor != null) {
            mDeviceMonitor.stop();
        }
    }

    @Override
    public void onDestroy() {
        mDeviceMonitor = null;
    }

    @Override
    public void setData(MediaData data) {
        mMediaData = data;
        if (mMediaData == null) {
            unbindRealLayer();
        }
    }

    @Override
    public void setPlayer(Player player) {
        mPlayer = (DrmPlayer) player;
        if (mPlayer == null) {
            unbindRealLayer();
        }
        onDeviceConnected(mDeviceMonitor.getConnectedStatus());
    }

    @Override
    public void onDeviceConnected(ConnectStatus status) {
        if (mPlayer != null) {
            mPlayer.setDrmDisplayLimit(status);
        }
        if (status != ConnectStatus.DISCONNECTED) {
            bindRealLayer();
        } else {
            unbindRealLayer();
        }
    }

    @Override
    public void onFilmModeChange(boolean isFilmMode) {
        if (isFilmMode == mInFilmMode) {
            return;
        }
        mInFilmMode = isFilmMode;
        if (mRealLayer != null) {
            mRealLayer.onFilmModeChange(isFilmMode);
        }
        if (!mInFilmMode) {
            showDrmDialog();
        }
    }

    @Override
    public View getView() {
        if (mRealLayer != null) {
            return mRealLayer.getView();
        }
        return null;
    }

    @Override
    public MGLView getMGLView() {
        if (mRealLayer != null) {
            return mRealLayer.getMGLView();
        }
        return null;
    }

    @Override
    public boolean onSingleTapUp(float x, float y) {
        // if other device connected, do nothing
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return false;
        }
        // if has consumed, do nothing
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            return mRealLayer.onSingleTapUp(x, y);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        return true;
    }

    @Override
    public void onChange(Player player, int message, int arg, Object data) {
        switch (message) {
            case DrmPlayer.MSG_PREPARED:
                break;
            case DrmPlayer.MSG_CONSUMED:
                bindRealLayer();
                break;
            case DrmPlayer.MSG_SHOW_DIALOG:
                if (!mInFilmMode && mPlayer != null) {
                    String currentFile = mMediaData.filePath;
                    if ((currentFile != null && !currentFile.equals(mCurrentFile))) {
                        showDrmDialog();
                        mCurrentFile = currentFile;
                    } else if (mIsFullScreenPreviewState) {
                        mPlayer.doFullScreenPreview();
                        mIsFullScreenPreviewState = false;
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("<onChange>, message not define, messge = "
                        + message);
        }
    }

    @Override
    public boolean onDoubleTap(float x, float y) {
        // if other device connected, no right to zoom in & zoom out
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return true;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            return mRealLayer.onDoubleTap(x, y);
        }
        // not enable zoom in & zoom out for video
        if (mMediaData.isVideo) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(float dx, float dy, float totalX, float totalY) {
        // if other device connected, do nothing
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return false;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            return mRealLayer.onScroll(dx, dy, totalX, totalY);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // if other device connected, do nothing
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return false;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            return mRealLayer.onFling(e1, e2, velocityX, velocityY);
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(float focusX, float focusY) {
        // if other device connected, no right to zoom in & zoom out
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return true;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            return mRealLayer.onScaleBegin(focusX, focusY);
        }
        // not enable zoom in & zoom out for video
        if (mMediaData.isVideo) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onScale(float focusX, float focusY, float scale) {
        // if other device connected, no right to zoom in & zoom out
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return true;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            return mRealLayer.onScale(focusX, focusY, scale);
        }
        // not enable zoom in & zoom out for video
        if (mMediaData.isVideo) {
            return true;
        }
        return false;
    }

    @Override
    public void onScaleEnd() {
        // if other device connected, no right to zoom in & zoom out
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            mRealLayer.onScaleEnd();
        }
    }

    @Override
    public void onDown(float x, float y) {
        // if other device connected, do nothing
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            mRealLayer.onDown(x, y);
        }
    }

    @Override
    public void onUp() {
        // if other device connected, do nothing
        if (mDeviceMonitor.getConnectedStatus() != ConnectStatus.DISCONNECTED) {
            return;
        }
        if (mRealLayer != null && mPlayer.isFullScreenPreview()) {
            mRealLayer.onUp();
        }
    }

    // if show dialog, return true, else return false
    private boolean showDrmDialog() {
        if (mMediaData.isVideo) {
            MtkLog.i(TAG, "<showDrmDialog> Current media is video, ignore");
            return false;
        }
        Log.d(TAG, " showDrmDialog mMediaData.filePath:" + mMediaData.filePath);
        final DrmManagerClient drmManagerClient = DrmHelper.getOmaDrmClient(mActivity);
        OmaDrmUtils.showConsumerDialog(mActivity, drmManagerClient, mMediaData.filePath,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (DialogInterface.BUTTON_POSITIVE == which) {
                            if (mPlayer != null) {
                                mPlayer.doFullScreenPreview();
                            }
                        }
                    }
                });
        return true;
    }

    private void bindRealLayer() {
        // when mPlayer == null, return
        if (mPlayer == null) {
            MtkLog.i(TAG, "<bindRealLayer> mPlayer == null, return");
            return;
        }
        // get real layer and real player
        mRealPlayer = mPlayer.getRealPlayer();
        if (mRealPlayer == null) {
            MtkLog.i(TAG, "<bindRealLayer> mRealPlayer == null, return");
            return;
        }
        mRealLayer = mMediaCenter.getRealLayer(mMediaData);
        if (mRealLayer == null) {
            MtkLog.i(TAG, "<bindRealLayer> mRealLayer == null, return");
            return;
        }

        // bind
        mRealLayer.setPlayer(mRealPlayer);
        mRealLayer.setData(mMediaData);
        mRealPlayer.registerPlayListener(mRealLayer);
        mRealLayer.onPrepareOptionsMenu(mOptionsMenu);
        mRealLayer.onResume(mInFilmMode);
    }

    private void unbindRealLayer() {
        if (mRealPlayer != null) {
            mRealPlayer.unRegisterPlayListener(mRealLayer);
        }
        if (mRealLayer != null) {
            mRealLayer.onPause();
            mRealLayer.setPlayer(null);
            mRealLayer.setData(null);
        }
        mRealPlayer = null;
        mRealLayer = null;
    }
}
