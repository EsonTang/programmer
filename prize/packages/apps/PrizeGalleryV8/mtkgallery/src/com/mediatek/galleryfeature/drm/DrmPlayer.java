package com.mediatek.galleryfeature.drm;

import android.content.Context;

import com.android.gallery3d.app.Log;
import com.mediatek.galleryfeature.drm.DeviceMonitor.ConnectStatus;
import com.mediatek.galleryfeature.drm.DrmDataAdapter.DataListener;
import com.mediatek.galleryframework.base.MediaCenter;
import com.mediatek.galleryframework.base.MediaData;
import com.mediatek.galleryframework.base.Player;
import com.mediatek.galleryframework.base.ThumbType;
import com.mediatek.galleryframework.gl.MGLCanvas;
import com.mediatek.galleryframework.gl.MTexture;
import com.mediatek.galleryframework.util.MtkLog;

/**
 * Generate drm texture and control the texture lift.
 */
public class DrmPlayer extends Player implements Player.OnFrameAvailableListener, DataListener {
    private static final String TAG = "MtkGallery2/DrmPlayer";
    public static final String PLACE_HOLDER_COLOR = "#333333";

    public static final int MSG_PREPARED = 0;
    public static final int MSG_CONSUMED = 1;
    public static final int MSG_SHOW_DIALOG = 2;
    private MediaCenter mMediaCenter;
    private DrmItem mDrmItem;
    private Player mRealPlayer;
    // when other device plug in, limit the display of drm
    private ConnectStatus mDrmDisplayLimit = ConnectStatus.DISCONNECTED;
    private DrmProtectTexture mDrmDisplayLimitTexture;
    private DrmDataAdapter mDataAdapter;

    /**
     * Constructor.
     * @param context
     *            The context for drm texture.
     * @param md
     *            The date.
     * @param outputType
     *            The type of output.
     * @param mc
     *            The mediacenter.
     */
    public DrmPlayer(Context context, MediaData md, OutputType outputType, MediaCenter mc) {
        super(context, md, outputType);
        mMediaCenter = mc;
    }

    @Override
    public boolean onPrepare() {
        Log.d(TAG, " <onPrepare> ");
        mDrmItem = (DrmItem) mMediaCenter.getItem(mMediaData);
        if (mDrmItem == null) {
            MtkLog.i(TAG, "<onPrepare> mDrmItem == null, return false");
            return false;
        }
        prepareDisplayLimit();
        mDataAdapter = new DrmDataAdapter(mContext, mMediaData, mDrmItem, this);
        if (DrmHelper.isDrmFile(mMediaData.filePath)) {
            mDataAdapter.onPrepare();
        }
        mRealPlayer = mMediaCenter.getRealPlayer(mMediaData, ThumbType.MIDDLE);
        if (mRealPlayer != null) {
            mRealPlayer.setOnFrameAvailableListener(this);
            return mRealPlayer.prepare();
        }
        return true;
    }

    @Override
    public boolean onStart() {
        Log.d(TAG, " <onStart> ");
        sendNotify(MSG_SHOW_DIALOG);
        return true;
    }

    @Override
    public boolean onPause() {
        Log.d(TAG, " <onPause> ");
        boolean success = true;
        if (mRealPlayer != null && mRealPlayer.getState() == State.PLAYING) {
            success = mRealPlayer.pause();
        }
        mDataAdapter.onPause();
        return success;
    }

    @Override
    public boolean onStop() {
        Log.d(TAG, " <onStop> ");
        if (mRealPlayer != null && mRealPlayer.getState() == State.PLAYING) {
            return mRealPlayer.stop();
        }
        return true;
    }

    @Override
    public void onRelease() {
        Log.d(TAG, " <onRelease> ");
        if (mRealPlayer != null) {
            mRealPlayer.release();
            mRealPlayer = null;
        }
        mDataAdapter.onRelease();
        mDrmItem = null;
    }

    @Override
    public int getOutputWidth() {
        if (mDrmDisplayLimit != ConnectStatus.DISCONNECTED && mDrmDisplayLimitTexture != null) {
            return mDrmDisplayLimitTexture.getWidth();
        } else if (mRealPlayer != null && mRealPlayer.getOutputWidth() > 0) {
            return mRealPlayer.getOutputWidth();
        } else if (mDataAdapter != null && mDataAdapter.getTexture() != null) {
            return mDataAdapter.getTexture().getWidth();
        } else {
            return 0;
        }
    }

    @Override
    public int getOutputHeight() {
        if (mDrmDisplayLimit != ConnectStatus.DISCONNECTED && mDrmDisplayLimitTexture != null) {
            return mDrmDisplayLimitTexture.getHeight();
        } else if (mRealPlayer != null && mRealPlayer.getOutputHeight() > 0) {
            return mRealPlayer.getOutputHeight();
        } else if (mDataAdapter != null && mDataAdapter.getTexture() != null) {
            return mDataAdapter.getTexture().getHeight();
        } else {
            return 0;
        }
    }

    @Override
    public MTexture getTexture(MGLCanvas canvas) {
        if (mDrmDisplayLimit != ConnectStatus.DISCONNECTED) {
            if (mDrmDisplayLimitTexture != null) {
                mDrmDisplayLimitTexture.setProtectStatus(mDrmDisplayLimit);
            }
            return mDrmDisplayLimitTexture;
        } else if (mRealPlayer != null && mRealPlayer.getTexture(canvas) != null) {
            return mRealPlayer.getTexture(canvas);
        } else if (mDataAdapter != null) {
            return mDataAdapter.getTexture();
        } else {
            return null;
        }
    }

    /**
     * Set Drm display limit.
     * @param status
     *            The current connection status.
     */
    public void setDrmDisplayLimit(ConnectStatus status) {
        mDrmDisplayLimit = status;
        if (getState() == Player.State.PLAYING) {
            sendFrameAvailable();
        }
    }

    @Override
    public void onFrameAvailable(Player player) {
        if (mFrameAvailableListener != null) {
            mFrameAvailableListener.onFrameAvailable(this);
        }
    }

    public Player getRealPlayer() {
        return mRealPlayer;
    }

    /**
     * Decode High quality bitmap for full display.
     */
    public void doFullScreenPreview() {
        boolean hasRightsToshow =
                DrmHelper.hasRightsToShow(mContext, mMediaData.filePath, mMediaData.isVideo);
        if (mRealPlayer != null && hasRightsToshow) {
            mRealPlayer.start();
        }
        // 1. Consume right.
        // 2. If there has not right for display.
        //    Decode bitmap operation trigger Dialog.
        mDataAdapter.onStart();
    }

    /**
     * Check current state: on drm thumbnail or full image display.
     * @return Whether on full display state or not.
     */
    public boolean isFullScreenPreview() {
        return (mDataAdapter != null && mDataAdapter.isScreenNailReady())
                || (mRealPlayer != null && (mRealPlayer.getOutputHeight() > 0 && mRealPlayer
                        .getOutputHeight() > 0))
                || (DrmHelper.isFLDrm(mMediaData.drmMethod) && !mMediaData.isVideo);
    }

    private void prepareDisplayLimit() {
        if (mDrmDisplayLimitTexture == null) {
            mDrmDisplayLimitTexture = new DrmProtectTexture(mContext);
        }
    }

    @Override
    public void onDataUpdated() {
        sendFrameAvailable();
    }

}
