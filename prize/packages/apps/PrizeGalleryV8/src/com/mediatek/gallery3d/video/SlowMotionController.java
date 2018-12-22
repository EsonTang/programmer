
package com.mediatek.gallery3d.video;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.android.gallery3d.R;
import com.android.gallery3d.app.MovieActivity;

public class SlowMotionController extends ViewGroup {

    private static final String TAG = "Gallery2/VideoPlayer/SlowMotionController";

    // for speed view.
    private static final int MARGIN = 10; // dip

    private int mActionBarHeight;
    private Context mContext;
    private MediaPlayerWrapper mMediaPlayerWrapper;
    private SlowMotionBar mSlowMotionBar;
    private SlowMotionSpeed mSlowMotionSpeed;
    // if op02 load(mHasRewindAndForward == true), use SlowMotionHooker replace
    // SlowMotionSpeed
    private final boolean mHasRewindAndForward;
    private Rect mWindowInsets;
    View mActionBarView;
    private int mSpeedPadding;
    private int mSpeedWidth;

    public SlowMotionController(Context context,
            MediaPlayerWrapper playerWrapper, SlowMotionBar.Listener listener) {
        super(context);
        mContext = context;
        mMediaPlayerWrapper = playerWrapper;
        setBackgroundResource(R.drawable.actionbar_translucent);
        mHasRewindAndForward = MtkVideoFeature
                .isRewindAndForwardSupport(context);

        // for speedview layout
        if (mHasRewindAndForward) {
            mSpeedWidth = 0;
            mSpeedPadding = 0;
        } else {
            DisplayMetrics metrics = mContext.getResources()
                    .getDisplayMetrics();
            Bitmap screenButton = BitmapFactory.decodeResource(
                    mContext.getResources(), R.drawable.m_ic_media_bigscreen);
            mSpeedWidth = screenButton.getWidth();
            mSpeedPadding = (int) (metrics.density * MARGIN);
            screenButton.recycle();
        }

        mSlowMotionBar = new SlowMotionBar(context, mMediaPlayerWrapper, listener,
                getSpeedViewPadding());
        LayoutParams matchParent = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        LayoutParams wrapContent = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        addView(mSlowMotionBar, matchParent);
        if (!mHasRewindAndForward) {
            mSlowMotionSpeed = new SlowMotionSpeed(context);
        }
        getActionBarHeight();
    }

    // refresh slowmotion speed icon
    public void refreshSlowMotionSpeed() {
        if (mSlowMotionSpeed != null) {
            mSlowMotionSpeed.updateSlowMotionSpeed();
        }
    }

    public void refreshMovieInfo(IMovieItem info) {
        mSlowMotionBar.refreshMovieInfo(info);
        if (mSlowMotionSpeed != null) {
            mSlowMotionSpeed.refreshMovieInfo(info);
        }
    }

    public void setTime(int currentTime, int totalTime) {
        mSlowMotionBar.setTime(currentTime, totalTime);
    }

    public void setScrubbing(boolean enable) {
        mSlowMotionBar.setScrubbing(enable);
    }

    public void onContextChange(Context context) {
        mContext = context;
        getActionBarHeight();
    }

    @Override
    public void setVisibility(int visibility) {
        if (mSlowMotionBar.setBarVisibility(visibility)) {
            // if return true from slow motion bar, indicator current video is
            // slow motion video.
            if (mSlowMotionSpeed != null) {
                mSlowMotionSpeed.setVisibility(visibility);
            }
            super.setVisibility(visibility);
        } else {
            // if return false from slow motion bar, indicator current video is
            // normal video.
            // slow motion ui is invisible for normal video.
            if (mSlowMotionSpeed != null) {
                mSlowMotionSpeed.setVisibility(GONE);
            }
            super.setVisibility(GONE);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        int pl = mWindowInsets.left; // the left paddings
        int pr = mWindowInsets.right;
        int pt = mWindowInsets.top;
        int pb = mWindowInsets.bottom;

        if (mHasRewindAndForward) {
            if (mSlowMotionBar != null) {
                mSlowMotionBar.layout(pl + pr, 0, r - pr, b - t);
            }
        } else {
            if (mSlowMotionBar != null && mSlowMotionSpeed != null) {
                mSlowMotionBar
                        .layout(pl,
                                0,
                                r
                                        - pr
                                        - mSlowMotionSpeed
                                                .getAddedRightPadding(), b - t);
                mSlowMotionSpeed.onLayout(r, pr, b - t);
            }
        }

    }

    private int getSpeedViewPadding() {
        return mSpeedPadding * 2 + mSpeedWidth;
    }

    // Get action bar height to layout slowmotion bar.
    private void getActionBarHeight() {
        Window window = ((Activity) mContext).getWindow();
        View v = window.getDecorView();
        mActionBarView = (View) v
                .findViewById(com.android.internal.R.id.action_bar);
        Log.v(TAG, "MovieControllerOverlay mActionBarView = "
                + mActionBarView);
        ViewTreeObserver vto = mActionBarView.getViewTreeObserver();
        mActionBarHeight = ((Activity) mContext).getActionBar().getHeight();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = ((Activity) mContext).getActionBar().getHeight();
                if (height != mActionBarHeight) {
                    mActionBarHeight = height;
                    SlowMotionController.this.requestLayout();
                    Log.v(TAG, "onGlobalLayout action bar height = "
                            + ((Activity) mContext).getActionBar().getHeight());
                }
            }
        });
    }

    public void onLayout(Rect windowInsets, int w, int h) {
        mWindowInsets = windowInsets;
        Log.v(TAG, "onLayout() windowInsets = " + windowInsets + " w = " + w
                + " h = " + h + " mActionBarHeight " + mActionBarHeight);
        super.layout(0, mActionBarHeight, w, 2 * mActionBarHeight);
    }

    class SlowMotionSpeed implements View.OnClickListener {
        private ImageView mSpeedView;
        private int mCurrentSpeed;
        private IMovieItem mMovieItem;
        private SlowMotionItem mSlowMotionItem;

        private int mSupportedFps = -1;
        private int mCurrentSpeedIndex;
        private int[] mCurrentSpeedRange;

        public SlowMotionSpeed(Context context) {
            LayoutParams wrapContent = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            // add speedView
            mSpeedView = new ImageView(context);
            mSpeedView.setScaleType(ScaleType.CENTER);
            mSpeedView.setFocusable(true);
            mSpeedView.setClickable(true);
            mSpeedView.setOnClickListener(this);
            addView(mSpeedView, wrapContent);

            if (mContext instanceof MovieActivity) {
                mMovieItem = ((MovieActivity) mContext).getMovieItem();
                mSlowMotionItem = mMovieItem.getSlowMotionItem();
                if (!mSlowMotionItem.isSlowMotionVideo()) {
                    Log.d(TAG,
                            "is not slow motion video ,set visibility as gone, return");
                    setVisibility(GONE);
                    return;
                }
                mCurrentSpeed = mSlowMotionItem.getSpeed();
                if (MtkVideoFeature.isForceAllVideoAsSlowMotion()) {
                    if (mCurrentSpeed == 0) {
                        mCurrentSpeed = SlowMotionItem.SLOW_MOTION_QUARTER_SPEED;
                    }
                }
                updateSlowMotionSpeed();
            } else {
                setVisibility(GONE);
            }
        }

        public void refreshMovieInfo(IMovieItem info) {
            Log.v(TAG, "refreshMovieInfo");
            mMovieItem = info;
            mSlowMotionItem = mMovieItem.getSlowMotionItem();
            if (!mSlowMotionItem.isSlowMotionVideo()) {
                Log.d(TAG,
                        "is not slow motion video ,set visibility as gone, return");
                setVisibility(GONE);
                return;
            }
            mCurrentSpeed = mSlowMotionItem.getSpeed();
            if (MtkVideoFeature.isForceAllVideoAsSlowMotion()) {
                if (mCurrentSpeed == 0) {
                    mCurrentSpeed = SlowMotionItem.SLOW_MOTION_QUARTER_SPEED;
                }
            }
            updateSlowMotionSpeed();
        }

        public void updateSlowMotionSpeed() {
            if (mSlowMotionItem != null) {
                mSupportedFps = mMediaPlayerWrapper.getFps();
                mCurrentSpeedRange = mSlowMotionItem
                        .getSupportedSpeedRange(mSupportedFps);

                // correct current speed if not in valid range
                if (mCurrentSpeed != SlowMotionItem.NORMAL_VIDEO_SPEED &&
                        !isExistedInRange(mCurrentSpeedRange, mCurrentSpeed)) {
                    Log.w(TAG, "current speed [" + mCurrentSpeed
                        + "] is invalid, update to " + SlowMotionItem.SLOW_MOTION_NORMAL_SPEED);
                    mCurrentSpeed = SlowMotionItem.SLOW_MOTION_NORMAL_SPEED;
                    mSlowMotionItem.setSpeed(mCurrentSpeed);
                }

                mCurrentSpeedIndex = mSlowMotionItem.getCurrentSpeedIndex(
                        mCurrentSpeedRange, mCurrentSpeed);
                updateSlowMotionIcon(mCurrentSpeedIndex);
            }
        }

        private boolean isExistedInRange(int[] range, int speed) {
            if (range != null) {
                int len = range.length;
                for (int i = 0; i < len; i++) {
                    if (range[i] == speed) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void updateSlowMotionIcon(int index) {
            if (index < 0 || index > mCurrentSpeedRange.length) {
                Log.v(TAG, "updateSlowMotionIcon index is invalide index = "
                        + index);
                return;
            }
            int speed = mCurrentSpeedRange[index];
            int speedResource = mSlowMotionItem.getSpeedIconResource(speed);
            Log.v(TAG, "updateSlowMotionIcon(" + index + ")" + "speed "
                    + speed + " speedResource " + speedResource);
            if (mSpeedView != null) {
                if (mSlowMotionItem.isSlowMotionVideo()
                        && mMediaPlayerWrapper != null) {
                    mSpeedView.setImageResource(speedResource);
                    refreshSlowMotionSpeed(speed);
                    mMediaPlayerWrapper.setSlowMotionSpeed(speed);
                } else {
                    mSpeedView.setVisibility(GONE);
                }
            }
            mCurrentSpeed = speed;
        }

        private void refreshSlowMotionSpeed(final int speed) {
            if (mSlowMotionItem != null) {
                mSlowMotionItem.updateItemUri(mMovieItem.getUri());
                mSlowMotionItem.setSpeed(speed);
                mSlowMotionItem.updateItemToDB();
            }
        }

        public void setVisibility(int visibility) {
            mSpeedView.setVisibility(visibility);
        }

        @Override
        public void onClick(View v) {
            Log.v(TAG, "onClick()");
            if (mSupportedFps == SlowMotionItem.INVALID_FPS) {
                updateSlowMotionSpeed();
            }
            if (mCurrentSpeedRange == null) {
                Log.w(TAG, "onClick, mCurrentSpeedRange is null");
                return;
            }
            mCurrentSpeedIndex++;
            int index = mCurrentSpeedIndex % mCurrentSpeedRange.length;
            updateSlowMotionIcon(index);
        }

        public void clearInfo() {

        }

        public void onHide() {
            mSpeedView.setVisibility(View.INVISIBLE);
        }

        public void onShow() {
            mSpeedView.setVisibility(View.VISIBLE);
        }

        public void onLayout(int width, int paddingRight, int yTop) {
            // layout screen view position
            int sw = getAddedRightPadding();
            mSpeedView.layout(width - paddingRight - sw, 0, width
                    - paddingRight, yTop);
        }

        public int getAddedRightPadding() {
            return getSpeedViewPadding();
        }
    }
}
