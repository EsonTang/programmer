package com.mediatek.gallery3d.video;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.gallery3d.R;

public class SlowMotionHooker extends MovieHooker {
    private static final String TAG = "Gallery2/VideoPlayer/SlowMotionHooker";

    private static final int MENU_SLOW_MOTION = 1;

    private MediaPlayerWrapper mMediaPlayerWrapper;
    private MenuItem mMenuSlowMotion;
    private int mCurrentSpeed;
    private int mNextSpeed;

    private int mSupportedFps;
    private int mCurrentSpeedIndex;
    private int[] mCurrentSpeedRange;

    private SlowMotionItem mSlowMotionItem;

    @Override
    public void setParameter(String key, Object value) {
        super.setParameter(key, value);
        Log.v(TAG, "setParameter(" + key + ", " + value + ")");
        if (value instanceof MediaPlayerWrapper) {
            mMediaPlayerWrapper = (MediaPlayerWrapper) value;
            mMediaPlayerWrapper.setSlowMotionSpeed(mCurrentSpeed);
            // update current speed range.
            updateCurrentSpeedRange();
        }
    }

    private void updateCurrentSpeedRange() {
        if (mMediaPlayerWrapper == null || mSlowMotionItem == null) {
            return;
        }
        mSupportedFps = mMediaPlayerWrapper.getFps();
        mCurrentSpeedRange = mSlowMotionItem
                .getSupportedSpeedRange(mSupportedFps);
        mCurrentSpeedIndex = mSlowMotionItem.getCurrentSpeedIndex(
                mCurrentSpeedRange, mCurrentSpeed);
    }

    private void refreshSlowMotionSpeed(final int speed) {
        if (getMovieItem() != null) {
            mSlowMotionItem.updateItemUri(getMovieItem().getUri());
            mSlowMotionItem.setSpeed(speed);
            mSlowMotionItem.updateItemToDB();
        }
    }

    private void updateSlowMotionSpeed() {
        if (mSlowMotionItem != null) {
            mSupportedFps = mMediaPlayerWrapper.getFps();
            mCurrentSpeedRange = mSlowMotionItem
                    .getSupportedSpeedRange(mSupportedFps);
            mCurrentSpeedIndex = mSlowMotionItem.getCurrentSpeedIndex(
                    mCurrentSpeedRange, mCurrentSpeed);
            updateSlowMotionIcon(mCurrentSpeedIndex);
        }
    }

    private void updateSlowMotionIcon(int index) {
        if (index < 0 || index > mCurrentSpeedRange.length) {
            Log.e(TAG, "updateSlowMotionIcon index is invalide index = "
                    + index);
            return;
        }
        if (mSlowMotionItem == null) {
            Log.e(TAG, "updateSlowMotionIcon, mSlowMotionItem is null");
            return;
        }
        int speed = mCurrentSpeedRange[index];
        int speedResource = mSlowMotionItem.getSpeedIconResource(speed);
        Log.v(TAG, "updateSlowMotionIcon(" + index + ")" + "speed " + speed
                + " speedResource " + speedResource);
        if (mMenuSlowMotion != null) {
            if (mSlowMotionItem.isSlowMotionVideo()) {
                mMenuSlowMotion.setIcon(speedResource);
                refreshSlowMotionSpeed(speed);
                mMediaPlayerWrapper.setSlowMotionSpeed(speed);
                mMenuSlowMotion.setVisible(true);
            } else {
                mMenuSlowMotion.setVisible(false);
            }
        }
        mCurrentSpeed = speed;
    }

    private void initialSlowMotionIcon(final int speed) {
        Log.v(TAG, "initialSlowMotionIcon() speed " + speed);
        if (mMenuSlowMotion != null && mSlowMotionItem != null) {
            mCurrentSpeed = speed;
            if (mCurrentSpeed != 0) {
                updateSlowMotionSpeed();
            } else {
                mMenuSlowMotion.setVisible(false);
            }
        }
    }

    @Override
    public void onMovieItemChanged(final IMovieItem item) {
        Log.v(TAG, "onMovieItemChanged() " + mMenuSlowMotion);
        if (mMenuSlowMotion != null && item != null) {
            mSlowMotionItem = item.getSlowMotionItem();
            initialSlowMotionIcon(mSlowMotionItem.getSpeed());
        }
    }

    @Override
    public void setVisibility(boolean visible) {
        if (mMenuSlowMotion != null && mSlowMotionItem != null
                && mSlowMotionItem.isSlowMotionVideo()
                && mSupportedFps != SlowMotionItem.INVALID_FPS) {
            mMenuSlowMotion.setVisible(visible);
            Log.v(TAG, "setVisibility() visible=" + visible);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.v(TAG, "onCreateOptionsMenu()");
        mMenuSlowMotion = menu.add(MENU_HOOKER_GROUP_ID,
                getMenuActivityId(MENU_SLOW_MOTION), 0,
                R.string.slow_motion_speed);
        mMenuSlowMotion.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        if (getMovieItem() != null) {
            mSlowMotionItem = getMovieItem().getSlowMotionItem();
            if (MtkVideoFeature.isForceAllVideoAsSlowMotion()) {
                if (mSlowMotionItem.getSpeed() == 0) {
                    initialSlowMotionIcon(SlowMotionItem.SLOW_MOTION_QUARTER_SPEED);
                } else {
                    initialSlowMotionIcon(mSlowMotionItem.getSpeed());
                }
            } else {
                initialSlowMotionIcon(mSlowMotionItem.getSpeed());
            }
        } else {
            Log.d(TAG, "getMovieItem() is null, set slow motion menu invisible");
            mMenuSlowMotion.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.v(TAG, "onPrepareOptionsMenu()");
        if (mSlowMotionItem != null && !mSlowMotionItem.isSlowMotionVideo()) {
            mMenuSlowMotion.setVisible(false);
        } else if (mSupportedFps == SlowMotionItem.INVALID_FPS) {
            updateSlowMotionSpeed();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (getMenuOriginalId(item.getItemId())) {
        case MENU_SLOW_MOTION:
            Log.v(TAG, "onOptionsItemSelected()");
            if (mSupportedFps == SlowMotionItem.INVALID_FPS) {
                updateSlowMotionSpeed();
            }
            mCurrentSpeedIndex++;
            int index = mCurrentSpeedIndex % mCurrentSpeedRange.length;
            updateSlowMotionIcon(index);
            return true;
        default:
            return false;
        }
    }

}
