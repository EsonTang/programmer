/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
 */
/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.Window;
import android.view.WindowManager;
import android.media.AudioManager;

import com.android.gallery3d.R;
import com.android.gallery3d.app.CommonControllerOverlay.State;
import com.mediatek.gallery3d.ext.IActivityHooker;
import com.mediatek.gallery3d.ext.IRewindAndForwardExtension;
import com.mediatek.gallery3d.video.DefaultMovieItem;
import com.mediatek.gallery3d.video.IMovieItem;
import com.mediatek.gallery3d.video.MediaPlayerWrapper;
import com.mediatek.gallery3d.video.MovieUtils;
import com.mediatek.gallery3d.video.ExtensionHelper;
import com.mediatek.gallery3d.video.IContrllerOverlayExt;
import com.mediatek.gallery3d.video.MtkVideoFeature;
import com.mediatek.gallery3d.video.SlowMotionBar;
import com.mediatek.gallery3d.video.SlowMotionController;
import com.prize.ui.ISlideController;
import com.prize.gallery3d.video.VideoZoomController;
import com.prize.ui.SlideView;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener, ISlideController {

    private static final String TAG = "Gallery2/VideoPlayer/MovieControllerOverlay";
    private static final boolean LOG = true;

    private boolean mHidden;
    private final Handler mHandler;
    private final Runnable mStartHidingRunnable;
    private final Animation mHideAnimation;
    private Context mContext;
    private IRewindAndForwardExtension mControllerRewindAndForwardExt;
    private OverlayExtension mOverlayExt;
    // / M: View used to show logo picture from metadata
    private ImageView mLogoView;
    private LogoViewExt mLogoViewExt = new LogoViewExt();
    private SlowMotionController mSlowMotionController;
    private IMovieItem mMovieItem;
    
    private int mCurrentTime;

    private int mTotalTime;
    public static final int SLIDE_TIME_STEP = 1000;

    private SlideViewExt mSlideViewExt = new SlideViewExt();
    

    private final Runnable mCenterStartSlideRunnable;
    private final Animation mCenterSlideHideAnimation;

    public MovieControllerOverlay(Context context) {
        super(context);

        mHandler = new Handler();
        mStartHidingRunnable = new Runnable() {
            @Override
            public void run() {
                startHiding();
            }
        };
        
        mCenterStartSlideRunnable = new Runnable() {
            
            @Override
            public void run() {
                
                startSlideHiding();
            }
        };
        
        mCenterSlideHideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        mCenterSlideHideAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                
                hideSlide(ISlideController.GESTURE_SLIDE_VOLUME);
            }
        });

        mHideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        mHideAnimation.setAnimationListener(this);

        hide();
    }

    public MovieControllerOverlay(Context context,
            MediaPlayerWrapper playerWrapper, IMovieItem movieItem) {
        super(context);
        mContext = context;
        setMediaPlayerWrapper(playerWrapper);
        mMovieItem = movieItem;
        mHandler = new Handler();
        mStartHidingRunnable = new Runnable() {
            @Override
            public void run() {
                if (mListener != null && mListener.powerSavingNeedShowController()) {
                    hide();
                } else {
                    startHiding();
                }
            }
        };
        
        mCenterStartSlideRunnable = new Runnable() {
            
            @Override
            public void run() {
                
                startSlideHiding();
            }
        };
        
        mCenterSlideHideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        mCenterSlideHideAnimation.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                
                hideSlide(ISlideController.GESTURE_SLIDE_VOLUME);
            }
        });

        mHideAnimation = AnimationUtils
                .loadAnimation(context, R.anim.player_out);
        mHideAnimation.setAnimationListener(this);

        mControllerRewindAndForwardExt = ExtensionHelper
                .getRewindAndForwardExtension(context);
        addRewindAndForwardView();
        
        mSlideViewExt.init(context);
        
        mOverlayExt = new OverlayExtension();

        mLogoViewExt.init(context);
        hide();

        createSlowMotionControllerIfNeeded(mMovieItem);
    }

    public Animation getHideAnimation() {
        return mHideAnimation;
    }

    public boolean isPlayPauseEanbled() {
    	return mPlayControlExt.isPlayPauseEanbled();
    }

    public boolean isTimeBarEnabled() {
        return mTimeBar.getScrubbing();
    }

    public IRewindAndForwardExtension getRewindAndForwardExtension() {
        return mControllerRewindAndForwardExt;
    }

    // / M: when RewindAndForwardView was removed, should call
    // addRewindAndForwardView again, or RewindAndForwardView will not be show@{
    public void addRewindAndForwardView() {
        if (mControllerRewindAndForwardExt != null
                && mControllerRewindAndForwardExt.getView() != null) {
            LinearLayout.LayoutParams wrapContent = new LinearLayout.LayoutParams(
                    mControllerRewindAndForwardExt.getPaddingRight(),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            final ViewParent parent = mControllerRewindAndForwardExt.getView()
                    .getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mControllerRewindAndForwardExt
                        .getView());
            }
            addView(mControllerRewindAndForwardExt.getView(), wrapContent);
        }
    }

    // /@}
    public void showPlaying() {
        if (!mOverlayExt.handleShowPlaying()) {
            mState = State.PLAYING;
//            showMainView(mPlayPauseReplayView);
            refreshPlayPause();
        }
        Log.v(TAG, "showPlaying() state=" + mState);
    }
    
    public boolean onBack() {
    	if (mIsLock) {
    		show();
    		mPlayControlExt.onLockKey();
    	}
    	return mIsLock;
    }

    public boolean onMenu() {
    	if (mIsLock) {
    		show();
    		mPlayControlExt.onLockKey();
    	}
    	return mIsLock;
    }

    public void showPaused() {
        if (!mOverlayExt.handleShowPaused()) {
            mState = State.PAUSED;
//          showMainView(mPlayPauseReplayView);
            refreshPlayPause();
        }
        Log.v(TAG, "showPaused() state=" + mState);
    }

    public void showEnded() {
        mOverlayExt.onShowEnded();
        mState = State.ENDED;
//        showMainView(mPlayPauseReplayView);
        refreshPlayPause();
        Log.v(TAG, "showEnded() state=" + mState);
    }
    
    public void showSlide(int type) {
        if (LOG) {
            Log.v(TAG, "showSlide() type=" + type);
        }
        mSlideViewExt.onShowSlide(type);
        if (type != ISlideController.GESTURE_SLIDE_PROGRESS) {
            mSlideViewExt.showSlide(type);
        }
    }
    

    public void hideSlide(int type) {
    
        mSlideViewExt.onHideSlide(type);
    }

    /**
     * Show loading icon.
     *
     * @param isHttp Whether the video is a http video or not.
     */
    public void showLoading(boolean isHttp) {
        mOverlayExt.onShowLoading(isHttp);
        mState = State.LOADING;
        showMainView(mLoadingView);
        Log.v(TAG, "showLoading() state=" + mState);
    }

    public void showErrorMessage(String message) {
        mOverlayExt.onShowErrorMessage(message);
        mState = State.ERROR;
        int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
        mErrorView.setPadding(padding, mErrorView.getPaddingTop(), padding,
                mErrorView.getPaddingBottom());
        mErrorView.setText(message);
        showMainView(mErrorView);
    }

    @Override
    protected void createTimeBar(Context context) {
//        mTimeBar = new TimeBar(context, this);
    	mTimeBar = mPlayControlExt.createTimeBar(context, this);
        // / M: set timebar id for test case @{
        int mTimeBarId = 8;
        mTimeBar.setId(mTimeBarId);
        // / @}
    }
    
    @Override
	protected void createPlayControlExt(Context context) {
		
    	mPlayControlExt = new PlayControlExt(context, this, false);
	}

    @Override
    public void setTimes(int currentTime, int totalTime, int trimStartTime,
            int trimEndTime) {
    	mCurrentTime = currentTime;
        mTotalTime = totalTime;
        mTimeBar.setTime(currentTime, totalTime, trimStartTime, trimEndTime);
    }

    public void setSlowMotionBarTimes(int currentTime, int totalTime) {
        if (mSlowMotionController != null) {
            mSlowMotionController.setTime(currentTime, totalTime);
        }
    }

    @Override
    public void hide() {
        boolean wasHidden = mHidden;
        mHidden = true;
        if (mListener == null
                || (mListener != null && !mListener.powerSavingNeedShowController())) {
//            mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        	mPlayControlExt.hidePlayControl();
            mLoadingView.setVisibility(View.INVISIBLE);
            // /M:pure video only show background
            if (!mOverlayExt.handleHide()) {
                setVisibility(View.INVISIBLE);
            }
            if (mMainView != null) {
            	mMainView.setVisibility(View.INVISIBLE);
            }
            mBackground.setVisibility(View.INVISIBLE);
            mTimeBar.setVisibility(View.INVISIBLE);
            if (mControllerRewindAndForwardExt != null
                    && mControllerRewindAndForwardExt.getView() != null) {
                mControllerRewindAndForwardExt.hide();
            }

            if (mSlowMotionController != null) {
                mSlowMotionController.setVisibility(View.INVISIBLE);
            }
        }
        // /@}
        setFocusable(true);
        requestFocus();
        if (mListener != null && (wasHidden != mHidden || isLock())) {
            mListener.onHidden();
        }

        Log.v(TAG, "hide() wasHidden=" + wasHidden + ", hidden=" + mHidden);
    }

    private void showMainView(View view) {
    	if (!isLock()) { 
    		mMainView = view;
            mErrorView.setVisibility(mMainView == mErrorView ? View.VISIBLE
                    : View.INVISIBLE);
            mLoadingView.setVisibility(mMainView == mLoadingView ? View.VISIBLE
                    : View.INVISIBLE);
//          mPlayPauseReplayView.setVisibility(
//          mMainView == mPlayPauseReplayView ? View.VISIBLE : View.INVISIBLE);
            mPlayControlExt.unShowPlayPause();
            mOverlayExt.onShowMainView();
            show();
    	}
    }
    
    private void refreshPlayPause() {

    	if (!isLock()) { 
    		mErrorView.setVisibility(View.INVISIBLE);
            mLoadingView.setVisibility(View.INVISIBLE);
            mMainView = null;
            mPlayControlExt.showPlayPause();
            mOverlayExt.onShowMainView();
    	}
    	show();
    }

    @Override
    public void show() {
    	if (!isLock()) {
    		if (mListener != null) {
                boolean wasHidden = mHidden;
                mHidden = false;
                updateViews();
                setVisibility(View.VISIBLE);
                setFocusable(false);
                if (mListener != null && wasHidden != mHidden) {
                    mListener.onShown();
                }
                maybeStartHiding();
                Log.v(TAG, "show() wasHidden=" + wasHidden + ", hidden="
                            + mHidden + ", listener=" + mListener);
            }
    	} else {
    		boolean wasHidden = mHidden;
    		mHidden = false;
            lockView();
            maybeStartHiding();
            Log.v(TAG, "show() lockView wasHidden=" + wasHidden + ", hidden="
                    + mHidden + ", listener=" + mListener);
    	}
        
    }
    
    public void unLock() {
    	mIsLock = false;
    	mPlayControlExt.unLock();
    }
    
    public boolean isLock() {
    	return mIsLock;
    }
    
    private boolean mIsLock;
    @Override
	public void lock(boolean isLock) {
		mIsLock = isLock;
		super.lock(isLock);
		if (mIsLock) {
			lockView();
		} else {
			unLockView();
		}
	}
    private void lockView() {
    	boolean wasHidden = mHidden;
        mHidden = true;
        if (mListener == null
                || (mListener != null && !mListener.powerSavingNeedShowController())) {
//            mPlayPauseReplayView.setVisibility(View.INVISIBLE);
            mLoadingView.setVisibility(View.INVISIBLE);
            // /M:pure video only show background
            if (!mOverlayExt.handleHide()) {
//                setVisibility(View.INVISIBLE);
            }
            if (mMainView != null) {
            	mMainView.setVisibility(View.INVISIBLE);
            }
            mBackground.setVisibility(View.INVISIBLE);
            mTimeBar.setVisibility(View.INVISIBLE);
            if (mControllerRewindAndForwardExt != null
                    && mControllerRewindAndForwardExt.getView() != null) {
                mControllerRewindAndForwardExt.hide();
            }

            if (mSlowMotionController != null) {
                mSlowMotionController.setVisibility(View.INVISIBLE);
            }
        }
        // /@}
        mPlayControlExt.lockView();
        setVisibility(View.VISIBLE);
        setFocusable(true);
        requestFocus();
        if (mListener != null && (wasHidden != mHidden || isLock())) {
            mListener.onHidden();
        }
        Log.v(TAG, "lockView wasHidden=" + wasHidden + ", hidden="
                + mHidden + ", listener=" + mListener);
    }

    private void unLockView() {
    	show();
    }

    private void maybeStartHiding() {
        cancelHiding();
        if (mState == State.PLAYING) {
            mHandler.postDelayed(mStartHidingRunnable, 6000);
        }
        Log.v(TAG, "maybeStartHiding() state=" + mState);
    }

    private void startHiding() {
        startHideAnimation(mBackground);
        startHideAnimation(mTimeBar);
        if (mControllerRewindAndForwardExt != null
                && mControllerRewindAndForwardExt.getView() != null) {
            mControllerRewindAndForwardExt.startHideAnimation();
        }
//        startHideAnimation(mPlayPauseReplayView);
        mPlayControlExt.startHideAnimation(mHideAnimation);
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(mHideAnimation);
        }
    }

    private void cancelHiding() {
        mHandler.removeCallbacks(mStartHidingRunnable);
        mBackground.setAnimation(null);
        mTimeBar.setAnimation(null);
        if (mControllerRewindAndForwardExt != null
                && mControllerRewindAndForwardExt.getView() != null) {
            mControllerRewindAndForwardExt.cancelHideAnimation();
        }
//        mPlayPauseReplayView.setAnimation(null);
        mPlayControlExt.cancelHiding();
    }
    
    private void maybeStartSlideHiding(int type) {
        if (type == ISlideController.GESTURE_SLIDE_PROGRESS) {
            cancelHiding();
            mHandler.postDelayed(mStartHidingRunnable, 6000);
        } else {
            cancelSlideHiding();
            mHandler.postDelayed(mCenterStartSlideRunnable, 3000);
        }
        if (LOG) {
            Log.v(TAG, "maybeStartHiding() state=" + mState);
        }
    }
    private void startSlideHiding() {
        mSlideViewExt.startHiding();
    }
    private void startSlideHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(mCenterSlideHideAnimation);
        }
    }
    private void cancelSlideHiding() {
    	mHandler.removeCallbacks(mCenterStartSlideRunnable);
        mSlideViewExt.cancelHiding();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        hide();
    }

    public void onClick(View view) {
        Log.v(TAG, "onClick(" + view + ") listener=" + mListener
                    + ", state=" + mState + ", canReplay=" + mCanReplay);
        /*if (mListener != null) {
            if (view == mPlayPauseReplayView) {
                // / M: when state is retry connecting error, user can replay
                // video
                if (mState == State.ENDED
                        || mState == State.RETRY_CONNECTING_ERROR) {
                    mListener.onReplay();
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                }
            }
        } else {
            if (mControllerRewindAndForwardExt != null
                    && mControllerRewindAndForwardExt.getView() != null) {
                mControllerRewindAndForwardExt.onClick(view);
            }
        }*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mHidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        int width = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay().getWidth();
        Rect insets = mWindowInsets;
        int pl = insets.left; // the left paddings
        int pr = insets.right;
        int pt = insets.top;
        int pb = insets.bottom;

        int h = bottom - top;
        int w = right - left;

        int y = h - pb;
        // Put both TimeBar and Background just above the bottom system
        // component.
        // But extend the background to the width of the screen, since we don't
        // care if it will be covered by a system component and it looks better.

        // Needed, otherwise the framework will not re-layout in case only the
        // padding is changed
        if (mControllerRewindAndForwardExt != null
                && mControllerRewindAndForwardExt.getView() != null) {
            mBackground.layout(0, y - mTimeBar.getPreferredHeight() - mControllerRewindAndForwardExt.getHeight() - mPlayControlExt.getTimeBarBottom(), w, y);
            mTimeBar.layout(pl + pr, y - mTimeBar.getPreferredHeight() - mControllerRewindAndForwardExt.getHeight() - mPlayControlExt.getTimeBarBottom(), w - pr, y - mControllerRewindAndForwardExt.getHeight() - mPlayControlExt.getTimeBarBottom());
            mControllerRewindAndForwardExt.onLayout(pr, width, y, pr);
        } else {
            mBackground.layout(0, y - mTimeBar.getPreferredHeight() - mPlayControlExt.getTimeBarBottom(), w, y);
            mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight() - mPlayControlExt.getTimeBarBottom(), w - pr - mPlayControlExt.getAddedRightPadding(), y - mPlayControlExt.getTimeBarBottom());
        }
        if (mSlowMotionController != null) {
            mSlowMotionController.onLayout(insets, w, h);
        }
        // Put the play/pause/next/ previous button in the center of the screen
//        layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h);
//        layoutCenteredView(mAudioOnlyView, 0, 0, w, h);
        mPlayControlExt.layout(w, h, pr, pl, y);
        if (mMainView != null) {
            layoutCenteredView(mMainView, 0, 0, w, h);
        }
    }

    protected void updateViews() {
        if (mHidden) {
            return;
        }
        mBackground.setVisibility(View.VISIBLE);
        mTimeBar.setVisibility(View.VISIBLE);
//        mPlayPauseReplayView
//                .setImageResource(mState == State.PAUSED ? R.drawable.videoplayer_play
//                        : mState == State.PLAYING ? R.drawable.videoplayer_pause
//                                : R.drawable.videoplayer_reload);
        mPlayControlExt.showPlayControl();
        mPlayControlExt.updatePlayPause(mState);
        if (mControllerRewindAndForwardExt != null
                && mControllerRewindAndForwardExt.getView() != null) {
            mControllerRewindAndForwardExt.show();
        }
        if (!mOverlayExt.handleUpdateViews()) {
//            mPlayPauseReplayView.setVisibility((
//                    mState != State.LOADING && mState != State.ERROR
//                            && !(mState == State.ENDED && !mCanReplay)) ? View.VISIBLE : View.GONE);
        	mPlayControlExt.updatePlayPauseVisible(mState, mCanReplay);
        }
        if (mSlowMotionController != null) {
            mSlowMotionController.setVisibility(View.VISIBLE);
        }
        requestLayout();
        Log.v(TAG, "updateViews() state=" + mState + ", canReplay="
                    + mCanReplay);
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        if (mSlowMotionController != null) {
            mSlowMotionController.setScrubbing(false);
        }
        cancelHiding();
        super.onScrubbingStart();
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
        super.onScrubbingMove(time);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        if (mSlowMotionController != null) {
            mSlowMotionController.setScrubbing(true);
        }
        maybeStartHiding();
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }

    public void onSlowMotionScrubbingStart(int currentTime, int totalTime) {
        mTimeBar.setSlowMotionBarStatus(SlowMotionBar.SCRUBBERING_START);
        cancelHiding();
        super.onScrubbingStart();
    }

    public void onSlowMotionScrubbingMove(int currentTime, int totalTime) {
        cancelHiding();
        super.onScrubbingMove(currentTime);
        mTimeBar.setTime(currentTime, totalTime, 0, 0);
    }

    public void onSlowMotionScrubbingEnd(int currentTime, int totalTime) {
        mTimeBar.setSlowMotionBarStatus(SlowMotionBar.SCRUBBERING_END);
        maybeStartHiding();
        super.onScrubbingEnd(currentTime, 0, 0);
        mTimeBar.setTime(currentTime, totalTime, 0, 0);
    }

    public void refreshSlowMotionMovieInfo(IMovieItem info) {
        createSlowMotionControllerIfNeeded(info);
        if (mSlowMotionController != null) {
            mSlowMotionController.refreshMovieInfo(info);
        }
    }

    public IContrllerOverlayExt getOverlayExt() {
        return mOverlayExt;
    }

    private class OverlayExtension implements IContrllerOverlayExt {
        private State mLastState = State.PLAYING;
        private String mPlayingInfo;
        // The logo picture from metadata
        private Drawable mLogoPic;
        // for pause feature
        private boolean mCanPause = true;
        private boolean mEnableScrubbing = false;

        public void showBuffering(boolean fullBuffer, int percent) {
            Log.v(TAG, "showBuffering(" + fullBuffer + ", " + percent
                    + ") " + "lastState=" + mLastState + ", state="
                    + mState);
            if (fullBuffer) {
                // do not show text and loading
                mTimeBar.setSecondaryProgress(percent);
                return;
            }
            if (mState == State.PAUSED || mState == State.PLAYING) {
                mLastState = mState;
            }
            if (percent >= 0 && percent < 100) { // valid value
                mState = State.BUFFERING;
                int msgId = R.string.media_controller_buffering;
                String text = String.format(getResources().getString(msgId),
                        percent);
                mTimeBar.setInfo(text);
                showMainView(mLoadingView);
            } else if (percent == 100) {
                mState = mLastState;
                mTimeBar.setInfo(null);
//                showMainView(mPlayPauseReplayView); // restore play pause state
                refreshPlayPause(); // restore play pause state
            } else { // here to restore old state
                mState = mLastState;
                mTimeBar.setInfo(null);
            }
        }

        // set buffer percent to unknown value
        public void clearBuffering() {
            Log.v(TAG, "clearBuffering()");
            mTimeBar.setSecondaryProgress(TimeBar.UNKNOWN);
            showBuffering(false, TimeBar.UNKNOWN);
        }

        public void onCancelHiding() {
            cancelHiding();
        }

        public void showReconnecting(int times) {
            clearBuffering();
            mState = State.RETRY_CONNECTING;
            int msgId = R.string.VideoView_error_text_cannot_connect_retry;
            String text = getResources().getString(msgId, times);
            mTimeBar.setInfo(text);
//            showMainView(mLoadingView);
            refreshPlayPause(); // restore play pause state
            Log.v(TAG, "showReconnecting(" + times + ")");
        }

        public void showReconnectingError() {
            clearBuffering();
            mState = State.RETRY_CONNECTING_ERROR;
            int msgId = R.string.VideoView_error_text_cannot_connect_to_server;
            String text = getResources().getString(msgId);
            mTimeBar.setInfo(text);
//            showMainView(mPlayPauseReplayView);
            refreshPlayPause();
            Log.v(TAG, "showReconnectingError()");
        }

        public void setPlayingInfo(boolean liveStreaming) {
            int msgId;
            if (liveStreaming) {
                msgId = R.string.media_controller_live;
            } else {
                msgId = R.string.media_controller_playing;
            }
            mPlayingInfo = getResources().getString(msgId);
            Log.v(TAG, "setPlayingInfo(" + liveStreaming + ") playingInfo="
                    + mPlayingInfo);
        }

        public void setCanPause(boolean canPause) {
            this.mCanPause = canPause;
            Log.v(TAG, "setCanPause(" + canPause + ")");
        }

        public void setCanScrubbing(boolean enable) {
            mEnableScrubbing = enable;
            mTimeBar.setScrubbing(enable);
            if (mSlowMotionController != null) {
                mSlowMotionController.setScrubbing(enable);
            }
            Log.v(TAG, "setCanScrubbing(" + enable + ")");
        }

        // /M:for only audio feature.
        private boolean mAlwaysShowBottom;

        public void setBottomPanel(boolean alwaysShow, boolean foreShow) {
            mAlwaysShowBottom = alwaysShow;
            if (!alwaysShow) { // clear background
                mAudioOnlyView.setVisibility(View.INVISIBLE);
                setBackgroundColor(Color.TRANSPARENT);
                // Do not show mLogoView when change from audio-only video to
                // A/V video.
                if (mLogoPic != null) {
                    Log.v(TAG, "setBottomPanel() dissmiss orange logo picuture");
                    mLogoPic = null;
                    mLogoView.setImageDrawable(null);
                    mLogoView.setBackgroundColor(Color.TRANSPARENT);
                    mLogoView.setVisibility(View.GONE);
                }
            } else {
                // Don't set the background again when there is a logo picture
                // of the audio-only video
                if (mLogoPic != null) {
                    mAudioOnlyView.setVisibility(View.INVISIBLE);
                    mLogoView.setImageDrawable(mLogoPic);
                } else {
                    setBackgroundColor(Color.BLACK);
                    mAudioOnlyView.setVisibility(View.VISIBLE);
                }
                if (foreShow) {
                    setVisibility(View.VISIBLE);
                    // show();//show the panel
                    // hide();//hide it for jelly bean doesn't show control when
                    // enter the video.
                }
            }
            Log.v(TAG, "setBottomPanel(" + alwaysShow + ", " + foreShow + ")");
        }

        public boolean handleHide() {
            Log.v(TAG, "handleHide() mAlwaysShowBottom" + mAlwaysShowBottom);
            return mAlwaysShowBottom;
        }

        /**
         * Set the picture which get from metadata.
         *
         * @param byteArray The picture in byteArray.
         */
        public void setLogoPic(byte[] byteArray) {
            Drawable backgound = MovieUtils.bytesToDrawable(byteArray);
            setBackgroundDrawable(null);
            mLogoView.setBackgroundColor(Color.BLACK);
            mLogoView.setImageDrawable(backgound);
            mLogoView.setVisibility(View.VISIBLE);
            mLogoPic = backgound;
        }

        public boolean isPlayingEnd() {
            Log.v(TAG, "isPlayingEnd() state=" + mState);
            boolean end = false;
            if (State.ENDED == mState || State.ERROR == mState
                    || State.RETRY_CONNECTING_ERROR == mState) {
                end = true;
            }
            return end;
        }

        /**
         * Show playing information will be ignored when there is buffering
         * information updated.
         *
         * @return True if mState is changed from PLAYING to BUFFERING during
         *         showPlaying is called.
         */
        public boolean handleShowPlaying() {
            if (mState == State.BUFFERING) {
                mLastState = State.PLAYING;
                return true;
            }
            return false;
        }

        public boolean handleShowPaused() {
            mTimeBar.setInfo(null);
            if (mState == State.BUFFERING) {
                mLastState = State.PAUSED;
                return true;
            }
            return false;
        }

        /**
         * Show a information when loading or seeking
         *
         * @param isHttp Whether the video is a http video or not.
         */
        public void onShowLoading(boolean isHttp) {
            int msgId;
            if (isHttp) {
                msgId = R.string.VideoView_info_buffering;
            } else {
                msgId = R.string.media_controller_connecting;
            }
            String text = getResources().getString(msgId);
            mTimeBar.setInfo(text);
        }

        public void onShowEnded() {
            clearBuffering();
            mTimeBar.setInfo(null);
        }

        public void onShowErrorMessage(String message) {
            clearBuffering();
        }

        public boolean handleUpdateViews() {
//            mPlayPauseReplayView
//                    .setVisibility((mState != State.LOADING
//                            && mState != State.ERROR
//                            &&
//                            // !(state == State.ENDED && !canReplay) && //show
//                            // end when user stopped it.
//                            mState != State.BUFFERING
//                            && mState != State.RETRY_CONNECTING && !(mState != State.ENDED
//                            && mState != State.RETRY_CONNECTING_ERROR && !mCanPause))
//                            // for live streaming
//                            ? View.VISIBLE
//                            : View.GONE);
        	 mPlayControlExt.handlePlayPause(mState, mCanPause);

            if (mPlayingInfo != null && mState == State.PLAYING) {
                mTimeBar.setInfo(mPlayingInfo);
            }
            return true;
        }

        public void onShowMainView() {
            Log.v(TAG, "onShowMainView() enableScrubbing="
                    + mEnableScrubbing + ", state=" + mState);
            if (mEnableScrubbing
                    && (mState == State.PAUSED || mState == State.PLAYING)) {
                mTimeBar.setScrubbing(true);
                if (mSlowMotionController != null) {
                    mSlowMotionController.setScrubbing(true);
                }
            } else {
                mTimeBar.setScrubbing(false);
                if (mSlowMotionController != null) {
                    mSlowMotionController.setScrubbing(false);
                }
            }
        }
    }

    // /M:Add LogoView for audio-only video.
    class LogoViewExt {
        private void init(Context context) {
            if (context instanceof MovieActivity) {
                // Add logo picture
                RelativeLayout movieView = (RelativeLayout) ((MovieActivity) mContext)
                        .findViewById(R.id.movie_view_root);
                FrameLayout.LayoutParams matchParent = new FrameLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
                mLogoView = new ImageView(mContext);
                mLogoView.setAdjustViewBounds(true);
                mLogoView.setMaxWidth(((MovieActivity) mContext)
                        .getWindowManager().getDefaultDisplay().getWidth());
                mLogoView.setMaxHeight(((MovieActivity) mContext)
                        .getWindowManager().getDefaultDisplay().getHeight());
                movieView.addView(mLogoView, matchParent);
                mLogoView.setVisibility(View.GONE);
            }
        }
    }

    // / @}
    
    /*PRIZE-Touch screen to adjust brightness, volume, progress-wanzhijuan-2015-3-30-start*/
    /**
     * 
     **
	 * class description: touch screen to adjust the brightness, volume, interface management schedule
	 * @author wanzhijuan
	 * @version version
     */
    class SlideViewExt {
        
        /** Display schedule control interface**/
        private boolean mIsShowTime;
        /** Actual volume control**/
        private AudioManager mAudioManager;
        /** Maximum sound */
        private int mMaxVolume;
        /** Management for the actual adjustment of brightness**/
        private Window mWindow;
        /** Touch screen to adjust brightness, volume, progress of the overall layout of the interface**/
        private SlideView mSlideView;
        /** The pace of volume control**/
        private int mStepVolume;
        
        /**
         * 
         * Method description: change the volume size
         * @param Parameter name: isAdd Description: true to increase the volume, false reduce the volume
         * @return Return type int Description: after adjusting the volume size
         * @see Class / class / class complete complete # method
         */
        private int onVolumeSlide(boolean isAdd) {
            int mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (isAdd) {
                mVolume += mStepVolume;
            } else {
                mVolume -= mStepVolume;
            }
            if (mVolume > mMaxVolume) {
                mVolume = mMaxVolume;
            } else if (mVolume < 0) {
                mVolume = 0;
            }
            return mVolume;
        }

        /**
         * 
		 * method description: changing the brightness of the screen
		 * @param parameter name: isAdd Description: true increases the brightness of the screen, false to reduce the brightness of the screen
		 * @return return type float Description: adjust the screen brightness
		 * / class / @see class name complete complete class # method name
         */
        private float onBrightnessSlide(boolean isAdd) {
            float mBrightness = mWindow.getAttributes().screenBrightness;
            if (isAdd) {
                mBrightness += 0.1;
            } else {
                mBrightness -= 0.1;
            }
            if (mBrightness > 1.0f) {
                mBrightness = 1.0f;
            } else if (mBrightness <= 0.0f) {
                mBrightness = 0.0f;
            }   
            return mBrightness;
        }
        
        /**
         * 
		 * method description: get the current screen brightness
		 * @param parameter name Description
		 * @return return type float Description: screen brightness
		 * / class / @see class name complete complete class # method name

         */
        private float getBrightness() {
            return mWindow.getAttributes().screenBrightness;
        }
        
        /**
         * 
		 * method description: get the current volume
		 * @param parameter name Description
		 * @return return type int Description: volume size
		 * / class / @see class name complete complete class # method name
         */
        private int getVolume() {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        
        /**
         * 
		 * method description: initialization
		 * @param parameter name Context description of the context of the environment
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        private void init(Context context) {
            if (context instanceof MovieActivity) {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                mWindow = ((MovieActivity) mContext).getWindow();
                mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mStepVolume = mMaxVolume / 10;
                if (mStepVolume < 1) {
                    mStepVolume = 1;
                }
                RelativeLayout movieView =
                        (RelativeLayout) ((MovieActivity) mContext).findViewById(R.id.movie_view_root);
                mSlideView = new SlideView(context);
                movieView.addView(mSlideView);
                
                RelativeLayout.LayoutParams slideParams = (android.widget.RelativeLayout.LayoutParams) mSlideView.getLayoutParams();
                slideParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                mSlideView.setVisibility(View.GONE);
            }
        }
        
        /**
         * 
		 * method description: display layout
		 * @param parameter name: Type Description: the current touch screen adjustment is the brightness, volume or progress
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void showSlide(int type) {
            if (type == VideoZoomController.GESTURE_SLIDE_VOLUME) {
                mSlideView.setVisibility(View.VISIBLE);
                showVolumeView(getVolume());
            } else if (type == VideoZoomController.GESTURE_SLIDE_BRIGHTNESS) {
                mSlideView.setVisibility(View.VISIBLE);
                showBrightnessView(getBrightness());
            }
        }

        /**
         * 
		 * method description: touch screen volume, brightness adjustment to cancel the timing hidden interface
		 * @param parameter name: description:
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void cancelHiding() {
            
            mSlideView.setAnimation(null);
        }

		/* *
		*
		* method description: touch screen volume, brightness for animation settings
		* @param parameter name: description:
		* @return return type description
		*  class / @see class name complete complete class # method name
		*/

        public void startHiding() {
            
            startSlideHideAnimation(mSlideView);
        }
        
		/* *
		*
		* method description: change the screen brightness / volume size
		* @param parameter name: Type Description: mark the current touch screen adjustment is the brightness, the volume parameter name: isAdd Description: true increases the screen brightness / volume, false reduce the screen brightness / volume
		* @return return type description
		* class / @see class name complete complete class # method name
		*/

        public void showVolumeBrightness(int type, boolean isAdd) {
            if (type == ISlideController.GESTURE_SLIDE_VOLUME) {
                int volume = onVolumeSlide(isAdd);
                showVolumeView(volume);
            } else if (type == ISlideController.GESTURE_SLIDE_BRIGHTNESS) {
                float brightness = onBrightnessSlide(isAdd);
                showBrightnessView(brightness);
            }
        }
        
        /**
         * 
		 * method description: set the volume value
		 * @param parameter name volume to indicate volume value
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        private void showVolumeView(int volume) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            //Change progress bar
            mSlideView.setProgress(true, volume * 100 / mMaxVolume);         
        }
        
        /**
         * 
		 * method description: set screen brightness
		 * @param parameter name brightness to illustrate the value of the screen brightness
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        private void showBrightnessView(float brightness) {
            WindowManager.LayoutParams lpa = mWindow.getAttributes();
            lpa.screenBrightness = brightness;
            mWindow.setAttributes(lpa);
            
            mSlideView.setProgress(false, (int) (brightness * 100));
        }

        /**
         * 
		 * method description: set the tag
		 * @param parameter name: Type Description: the current touch screen adjustment is the brightness, volume or progress
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        private void onShowSlide(int type) {
            if (type == ISlideController.GESTURE_SLIDE_PROGRESS) {
                mIsShowTime = true;
            } 
        }
        
        /**
         * 
		 * Description:  method after the end of regulation, to hide the corresponding interface and mark
		 * @param type parameter name description tag is the volume, brightness, or schedule
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        private void onHideSlide(int type) {
            if (type == ISlideController.GESTURE_SLIDE_PROGRESS) {
                mIsShowTime = false;
            } else {
                mSlideView.setVisibility(View.GONE);
            }
        }
        
        /**
         * 
		 * method description: mark the current is not in progress adjustment
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        private boolean isShowTime() {
            if (LOG) {
                Log.v(TAG, "isShowTime() isShowTime=" + mIsShowTime);
            }
            return mIsShowTime;
        }
    }
    
    /*PRIZE-Touch screen to adjust brightness, volume, progresswanzhijuan-2015-3-30-end*/

    public IActivityHooker getRewindAndForwardHooker() {
        return (IActivityHooker) getRewindAndForwardExtension();
    }

    public void updateSlowMotionSpeed() {
        if (mSlowMotionController != null) {
            mSlowMotionController.refreshSlowMotionSpeed();
        }
    }

    public void createSlowMotionControllerIfNeeded(IMovieItem movieItem) {
        Log.d(TAG, "createSlowMotionControllerIfNeed");
        if (mSlowMotionController == null && movieItem.isSlowMotion()) {
            mSlowMotionController = new SlowMotionController(mContext,
                    mMediaPlayerWrapper, new SlowMotionBar.Listener() {

                        @Override
                        public void onScrubbingStart(int currentTime,
                                int totalTime) {
                            onSlowMotionScrubbingStart(currentTime, totalTime);
                        }

                        @Override
                        public void onScrubbingMove(int currentTime,
                                int totalTime) {
                            onSlowMotionScrubbingMove(currentTime, totalTime);
                        }

                        @Override
                        public void onScrubbingEnd(int currentTime,
                                int totalTime) {
                            onSlowMotionScrubbingEnd(currentTime, totalTime);
                        }
                    });
            LayoutParams matchParent = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView(mSlowMotionController);
        } else if (mSlowMotionController != null && !movieItem.isSlowMotion()) {
            removeView(mSlowMotionController);
            mSlowMotionController = null;
        }
    }

    /*PRIZE-Touch screen to adjust brightness, volume, progresswanzhijuan-2015-3-30-start*/
    @Override
    public void onSlideStart(int type) {
        if (LOG) {
            Log.v(TAG, "onSlideStart() type=" + type);
        }
        if (mListener != null && type == ISlideController.GESTURE_SLIDE_PROGRESS) {
            if (mHidden) {
                show();
            }
            mListener.onSlideStart();
        } 
        showSlide(type);   
    }

    @Override
    public void onSlideMove(int type, boolean inc) {
        
        if (LOG) {
            Log.v(TAG, "onSlideMove() type=" + type + " inc=" + inc);
        }
        if (type == ISlideController.GESTURE_SLIDE_PROGRESS) {
            if (mTotalTime >= 0) {
                if (inc) {
                    mCurrentTime = mCurrentTime + SLIDE_TIME_STEP;
                    if (mCurrentTime >= mTotalTime) {
                        mCurrentTime = mTotalTime;
                    }
                } else {
                    mCurrentTime = mCurrentTime - SLIDE_TIME_STEP;
                    if (mCurrentTime <= 0) {
                        mCurrentTime = 0;
                    }
                }
                if (mListener != null) {
                    mListener.onSlideMove(mCurrentTime);
                }
            }
        } else {
            mSlideViewExt.showVolumeBrightness(type, inc);
        }
    }

    @Override
    public void onSlideEnd(int type) {
        
        if (LOG) {
            Log.v(TAG, "onSlideEnd() type=" + type);
        }
        if (mListener != null && type == ISlideController.GESTURE_SLIDE_PROGRESS) {
            mListener.onSlideEnd(mCurrentTime, 0, 0);
        }
        maybeStartSlideHiding(type);
    }

    @Override
    public void setDragTimes(int position) {
        
        mCurrentTime = position;
        if (mSlideViewExt.isShowTime()) {
            if (!mHidden) {
                setTimes(mCurrentTime, mTotalTime, 0, 0);
            }
        }
    }
    /*PRIZE-Touch screen to adjust brightness, volume, progress-wanzhijuan-2015-3-30-end*/

	public boolean isHidden(){
	    Log.w(TAG,"isHidden = " + mHidden);
	    return mHidden;
	}
}
