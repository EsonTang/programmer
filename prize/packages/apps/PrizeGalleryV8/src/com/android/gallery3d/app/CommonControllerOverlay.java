/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.mediatek.gallery3d.video.MediaPlayerWrapper;
import com.prize.ui.OldPlayPauseScreenExt;
import com.prize.ui.OptionTools;
import com.prize.ui.PlayControllerOverlay;
import com.prize.ui.TrimVideoPlayPauseExt;

/**
 * The common playback controller for the Movie Player or Video Trimming.
 */
public abstract class CommonControllerOverlay extends FrameLayout implements
        ControllerOverlay,
        OnClickListener,
        TimeBar.Listener {

    public enum State {
        PLAYING,
        PAUSED,
        ENDED,
        ERROR,
        LOADING, //mean connecting
        BUFFERING,
        RETRY_CONNECTING,
        RETRY_CONNECTING_ERROR
    }

    protected static final float ERROR_MESSAGE_RELATIVE_PADDING = 1.0f / 6;

    protected Listener mListener;

    protected final View mBackground;
    protected TimeBar mTimeBar;

    protected View mMainView;
    protected final LinearLayout mLoadingView;
    protected final TextView mErrorView;
    /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
    /**Play pause, screen mode switch, the progress of the management, in order to be compatible with the original function, do the differences in PlayControlExt processing**/
//    protected final ImageView mPlayPauseReplayView;
    protected PlayControlExt mPlayControlExt;
    /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
//    protected final ImageView mPlayPauseReplayView;
    // / M: [FEATURE.ADD] Audio only video@{
    protected final ImageView mAudioOnlyView;
    // / @}

    protected State mState;

    protected boolean mCanReplay = true;
    protected MediaPlayerWrapper mMediaPlayerWrapper;

    public void setMediaPlayerWrapper(MediaPlayerWrapper mediaPlayerWrapper) {
        this.mMediaPlayerWrapper = mediaPlayerWrapper;
        onPlayerWrapperChanged();
    }

    protected void onPlayerWrapperChanged() {

    }

    public void setSeekable(boolean canSeek) {
        mTimeBar.setSeekable(canSeek);
    }

    public CommonControllerOverlay(Context context) {
        super(context);

        mState = State.LOADING;
        // TODO: Move the following layout code into xml file.
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams matchParent =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        // / M: [FEATURE.ADD] Audio only video@{
        mAudioOnlyView = new ImageView(context);
        mAudioOnlyView.setImageResource(R.drawable.ic_media_audio_only_video);
        mAudioOnlyView.setScaleType(ScaleType.CENTER);
        addView(mAudioOnlyView, wrapContent);
        mAudioOnlyView.setVisibility(View.GONE);
        // / @}
        mBackground = new View(context);
        mBackground.setBackgroundColor(context.getResources().getColor(R.color.darker_transparent));
        addView(mBackground, matchParent);
        
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // The primary is to add play pause, suspend function is to add (screen mode switch, up and down video, play pause, jump to the suspension window£©
        createPlayControlExt(context);
        addView(mPlayControlExt.getFullView(), wrapContent);
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/

        // Depending on the usage, the timeBar can show a single scrubber, or
        // multiple ones for trimming.
        createTimeBar(context);
        addView(mTimeBar, wrapContent);
        mTimeBar.setContentDescription(
                context.getResources().getString(R.string.accessibility_time_bar));
        mLoadingView = new LinearLayout(context);
        mLoadingView.setOrientation(LinearLayout.VERTICAL);
        mLoadingView.setGravity(Gravity.CENTER_HORIZONTAL);
        ProgressBar spinner = new ProgressBar(context);
        spinner.setIndeterminate(true);
        mLoadingView.addView(spinner, wrapContent);
     // / M: mark it for mediatek info feature.
//        TextView loadingText = createOverlayTextView(context);
//        loadingText.setText(R.string.loading_video);
//        mLoadingView.addView(loadingText, wrapContent);
        addView(mLoadingView, wrapContent);

        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // move to PlayControlExt Unified processing
        /*mPlayPauseReplayView = new ImageView(context);
        // M: bug fix
        // mPlayPauseReplayView.setImageResource(R.drawable.ic_vidcontrol_play);
        mPlayPauseReplayView.setContentDescription(
                context.getResources().getString(R.string.accessibility_play_video));
        mPlayPauseReplayView.setBackgroundResource(R.drawable.bg_vidcontrol);
        mPlayPauseReplayView.setScaleType(ScaleType.CENTER);
        mPlayPauseReplayView.setFocusable(true);
        mPlayPauseReplayView.setClickable(true);
        mPlayPauseReplayView.setOnClickListener(this);
        addView(mPlayPauseReplayView, wrapContent);*/
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/

        mErrorView = createOverlayTextView(context);
        addView(mErrorView, matchParent);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(params);

        //hide();
    }

    abstract protected void createPlayControlExt(Context context);
    abstract protected void createTimeBar(Context context);

    private TextView createOverlayTextView(Context context) {
        TextView view = new TextView(context);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(0xFFFFFFFF);
        view.setPadding(0, 15, 0, 15);
        return view;
    }

    @Override
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void setCanReplay(boolean canReplay) {
        this.mCanReplay = canReplay;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void showPlaying() {
        mState = State.PLAYING;
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // In order to handle the display of the play pause button
//        showMainView(mPlayPauseReplayView);
        refreshPlayPause();
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
    }

    @Override
    public void showPaused() {
        mState = State.PAUSED;
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // In order to handle the display of the play pause button
//        showMainView(mPlayPauseReplayView);
        refreshPlayPause();
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
    }

    @Override
    public void showEnded() {
        mState = State.ENDED;
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // In order to handle the display of the play pause button
//      if (mCanReplay)  showMainView(mPlayPauseReplayView);
        if (mCanReplay) refreshPlayPause();
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
    }

    @Override
    public void showLoading() {
        mState = State.LOADING;
        showMainView(mLoadingView);
    }

    @Override
    public void showErrorMessage(String message) {
        mState = State.ERROR;
        int padding = (int) (getMeasuredWidth() * ERROR_MESSAGE_RELATIVE_PADDING);
        mErrorView.setPadding(
                padding, mErrorView.getPaddingTop(), padding, mErrorView.getPaddingBottom());
        mErrorView.setText(message);
        showMainView(mErrorView);
    }

    @Override
    public void setTimes(int currentTime, int totalTime,
            int trimStartTime, int trimEndTime) {
        mTimeBar.setTime(currentTime, totalTime, trimStartTime, trimEndTime);
    }

    public void hide() {
    	/*PRIZE-Support suspension function--wanzhijuan-2015-4-13-start*/
        //  In order to handle the display of the play pause button
//        mPlayPauseReplayView.setVisibility(View.INVISIBLE);
        mPlayControlExt.hidePlayControl();
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
        mLoadingView.setVisibility(View.INVISIBLE);
        mBackground.setVisibility(View.INVISIBLE);
        mTimeBar.setVisibility(View.INVISIBLE);
        setVisibility(View.INVISIBLE);
        setFocusable(true);
        requestFocus();
    }

    private void showMainView(View view) {
        mMainView = view;
        mErrorView.setVisibility(mMainView == mErrorView ? View.VISIBLE : View.INVISIBLE);
        mLoadingView.setVisibility(mMainView == mLoadingView ? View.VISIBLE : View.INVISIBLE);
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // Display error, load and other buttons when the play button is hidden
//        mPlayPauseReplayView.setVisibility(
//                mMainView == mPlayPauseReplayView ? View.VISIBLE : View.INVISIBLE);
        mPlayControlExt.unShowPlayPause();
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
        show();
    }
    
    /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
    /**
     * 
	 * method description: display the pause button, refresh the interface
	 * @param parameter name Description
	 * @return return type description
	 * / class / @see class name complete complete class # method name
     */
    private void refreshPlayPause() {
        mErrorView.setVisibility(View.INVISIBLE);
        mLoadingView.setVisibility(View.INVISIBLE);
        mMainView = null;
        mPlayControlExt.showPlayPause();
        show();
    }
    /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/

    @Override
    public void show() {
        updateViews();
        setVisibility(View.VISIBLE);
        setFocusable(false);
    }

    @Override
    public void onClick(View view) {
    	/*PRIZE-Support suspension function--wanzhijuan-2015-4-13-start*/
        // Play pause button listener in PlayControlExt processing
        /*if (mListener != null) {
            if (view == mPlayPauseReplayView) {
                /// M: when state is retry connecting error, user can replay video
                if (mState == State.ENDED || mState == State.RETRY_CONNECTING_ERROR) {
                    if (mCanReplay) {
                        mListener.onReplay();
                    }
                } else if (mState == State.PAUSED || mState == State.PLAYING) {
                    mListener.onPlayPause();
                }
            }
        }*/
    	/*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    // The paddings of 4 sides which covered by system components. E.g.
    // +-----------------+\
    // | Action Bar | insets.top
    // +-----------------+/
    // | |
    // | Content Area | insets.right = insets.left = 0
    // | |
    // +-----------------+\
    // | Navigation Bar | insets.bottom
    // +-----------------+/
    // Please see View.fitSystemWindows() for more details.
    protected final Rect mWindowInsets = new Rect();

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        // We don't set the paddings of this View, otherwise,
        // the content will get cropped outside window
        mWindowInsets.set(insets);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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
        mBackground.layout(0, y - mTimeBar.getBarHeight(), w, y);
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // mPlayControlExt.getTimeBarBottom()The vacated position is displayed as the bottom button
        mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight() - mPlayControlExt.getTimeBarBottom(), w - pr, y - mPlayControlExt.getTimeBarBottom());
//        mTimeBar.layout(pl, y - mTimeBar.getPreferredHeight(), w - pr, y);

        // Put the play/pause/next/ previous button in the center of the screen
//      layoutCenteredView(mPlayPauseReplayView, 0, 0, w, h);
      // Put the play/pause/next/ previous button in the center of the screen
      // Display play pause, screen mode switch difference button
      mPlayControlExt.layout(w, h, pr, pl, y);
      /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/

        if (mMainView != null) {
            layoutCenteredView(mMainView, 0, 0, w, h);
        }
    }

    protected void layoutCenteredView(View view, int l, int t, int r, int b) {
        int cw = view.getMeasuredWidth();
        int ch = view.getMeasuredHeight();
        int cl = (r - l - cw) / 2;
        int ct = (b - t - ch) / 2;
        view.layout(cl, ct, cl + cw, ct + ch);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    protected void updateViews() {
        mBackground.setVisibility(View.VISIBLE);
        mTimeBar.setVisibility(View.VISIBLE);
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
        // Updated play suspend state extracted unified processing
        mPlayControlExt.updatePlayPause(mState);
        mPlayControlExt.updatePlayPauseVisible(mState, mCanReplay);
        /*Resources resources = getContext().getResources();
        int imageResource = R.drawable.ic_vidcontrol_reload;
        String contentDescription = resources.getString(R.string.accessibility_reload_video);
        if (mState == State.PAUSED) {
            imageResource = R.drawable.ic_vidcontrol_play;
            contentDescription = resources.getString(R.string.accessibility_play_video);
        } else if (mState == State.PLAYING) {
            imageResource = R.drawable.ic_vidcontrol_pause;
            contentDescription = resources.getString(R.string.accessibility_pause_video);
        }

        mPlayPauseReplayView.setImageResource(imageResource);
        mPlayPauseReplayView.setContentDescription(contentDescription);
        mPlayPauseReplayView.setVisibility(
                (mState != State.LOADING && mState != State.ERROR &&
                !(mState == State.ENDED && !mCanReplay))
                ? View.VISIBLE : View.GONE);*/
        /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-end*/
        requestLayout();
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        /// M: add if for safe
        if (mListener != null) {
            mListener.onSeekStart();
        }
    }

    @Override
    public void onScrubbingMove(int time) {
        /// M: add if for safe
        if (mListener != null) {
            mListener.onSeekMove(time);
        }
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        /// M: add if for safe
        if (mListener != null) {
            mListener.onSeekEnd(time, trimStartTime, trimEndTime);
        }
    }
    
    /*PRIZE-Support suspension function-wanzhijuan-2015-4-13-start*/
    
    /*PRIZE-function lock bug:452-wanzhijuan-2015-5-12-start*/
    public void lock(boolean isLock) {
    	//Notification to the upper
    	mListener.onLockScreen(isLock);
    }
    /*PRIZE-function loc bug:452-wanzhijuan-2015-5-12-end*/
    /**
     * 
     **
     * Description: floating window function UI is added on the next video, jump to the floating window, placed in the progress bar below, and pause playback, the screen mode switch placed together, to be compatible with the native function in different processing
     * @author auther
     * @version version
     */
    class PlayControlExt {
        /**Primary function, here is the pause ImageView; floating window is contains video, jump to the floating window, play pause, screen mode switch of the parent layout **/
        private View mFullView;
        /**play pause **/
        private ImageView mPlayPauseIm;
        /** Screen mode switch management**/
        /** isOld trueIs a primary function,false**/
        public boolean isOld = false;
        /** Polymorphic interface for managing native function and suspension window function**/
        private IControl mControl;
        public PlayControlExt(Context context, ViewGroup parent, boolean isTrimVideo) {
            this(context, parent, OptionTools.isOptionVideo(), isTrimVideo);
        }
        
        public PlayControlExt(Context context, ViewGroup parent, boolean isNew, boolean isTrimVideo) {
            initControlView(context, parent, isNew, isTrimVideo);
        }
        
        private void initControlView(Context context, ViewGroup parent, boolean isNew, boolean isTrimVideo) {
        	if (isNew) {
        		if (isTrimVideo) {
        			newTrimControlView(context, parent);
        		} else { // Support suspension function
        			newControlView(context, parent);
        		}
        	} else { // Primary function
        		oldControlView(context, parent, !isTrimVideo);
        	}
        }
        
        private void newTrimControlView(Context context, ViewGroup parent) {
        	TrimVideoPlayPauseExt trimVideoPlayPauseExt = new TrimVideoPlayPauseExt(context, CommonControllerOverlay.this);
            mControl = trimVideoPlayPauseExt;
            mPlayPauseIm = trimVideoPlayPauseExt.getPlayPause();
            mFullView = mPlayPauseIm;
            mPlayPauseIm.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    
                    if (mState == State.ENDED || mState == State.RETRY_CONNECTING_ERROR) {
                        if (mCanReplay) {
                            mListener.onReplay();
                        }
                    } else if (mState == State.PAUSED || mState == State.PLAYING) {
                        mListener.onPlayPause();
                    }
                }
            });
        }
        
        private void oldControlView(Context context, ViewGroup parent, boolean isShowScreen) {
        	OldPlayPauseScreenExt oldPlayPauseScreenExt = new OldPlayPauseScreenExt(context, CommonControllerOverlay.this, new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                }
            }, isShowScreen);
            mControl = oldPlayPauseScreenExt;
            mPlayPauseIm = oldPlayPauseScreenExt.getPlayPause();
            mFullView = mPlayPauseIm;
            mPlayPauseIm.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    
                    if (mState == State.ENDED || mState == State.RETRY_CONNECTING_ERROR) {
                        if (mCanReplay) {
                            mListener.onReplay();
                        }
                    } else if (mState == State.PAUSED || mState == State.PLAYING) {
                        mListener.onPlayPause();
                    }
                }
            });
        }
        
        private void newControlView(Context context, ViewGroup parent) {
        	PlayControllerOverlay mPlayControllerOverlay = new PlayControllerOverlay(context);
            mPlayControllerOverlay.createLockScreen(parent);
            mPlayControllerOverlay.setListener(new PlayControllerOverlay.Listener() {
                
                @Override
                public void onControlPlayPre() {
                    
                    mListener.onPlayPre();
                }
                
                @Override
                public void onControlPlayNext() {
                    
                    mListener.onPlayNext();
                }
                
                @Override
                public void onControlPlay() {
                    
                    if (mState == State.ENDED || mState == State.RETRY_CONNECTING_ERROR) {
                        mListener.onReplay();
                    } else if (mState == State.PAUSED || mState == State.PLAYING) {
                        mListener.onPlayPause();
                    }
                }
                

				@Override
				public void onLockScreen(boolean isLock) {
					
					/*PRIZE- bug:452-wanzhijuan-2015-5-12-start*/
					lock(isLock);
					/*PRIZE- bug:452-wanzhijuan-2015-5-12-end*/
				}
            });
            mControl = mPlayControllerOverlay;
            mPlayPauseIm = mPlayControllerOverlay.getPlayPauseView(); 
            mFullView = mPlayControllerOverlay;
        }
        
        /**
         * 
		 * method description: pause button
		 * @param parameter name Description
		 * @return return type that play pause button
		 * / class / @see class name complete complete class # method name
         */
        public ImageView getPlayPauseIm() {
            return mPlayPauseIm;
        }
        
        /**
         * 
		 * method description: when the native function, here is the play pause ImageView; the suspension window is contains the up and down video, jumps to the suspended window, the play pause, the screen mode switch the parent layout
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public View getFullView() {
            return mFullView;
        }
        
        /**
         * 
		 * method description: update play pause status
		 * @param parameter name Description play status
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void updatePlayPause(State status) {
            if (mControl != null) {
                mControl.updatePlayPause(status);
            }
        }
        
        /**
         * 
		 * method description: when the primary function, here is 0; the suspension window is included in the video, jump to the suspension window, play pause, screen mode switch to the height of the parent layout
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public int getTimeBarBottom() {
            if (mControl != null) {
                return mControl.getBottomHeight();
            }
            return 0;
        }
        
        /**
         * 
	 	 * method description: Layout
		 * @param parameter name w description width
		 * @param parameter name h description high
		 * @param parameter name PR description paddings right
		 * @param parameter name PL description paddings left
		 * @param parameter name y description y
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void layout(int w, int h, int pr, int pl, int y) {
            if (mControl != null) {
                mControl.updateLayout(w, h, pr, pl, y, mTimeBar.getBarHeight());
            }
        }
        
        /**
         * 
		 * method description: display play pause
   		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void showPlayPause() {
            if (mControl != null) {
                mControl.showPlayPause();
            }
        }
        
        /**
         * 
		 * method description: hide play pause
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void unShowPlayPause() {
            if (mControl != null) {
                mControl.unShowPlayPause();
            }
        }
        
        /**
         * 
		 * method description: play pause button is Enabled
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name

         */
        public boolean isPlayPauseEanbled() {
            return mPlayPauseIm.isEnabled();
        }

        /**
         * 
		 * method description: hide layout
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void hidePlayControl() {
            
            if (mControl != null) {
                mControl.hide();
            }
        }

        /**
         * 
		 * method description: start hiding animation
		 * @param parameter name hideAnimation description animation
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void startHideAnimation(Animation hideAnimation) {
            
            if (mControl != null) {
                mControl.startHideAnimation(hideAnimation);
            }
        }

        /**
         * 
		 * method description: hide animation
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void cancelHiding() {
            if (mControl != null) {
                mControl.cancelHiding();
            }
        }

        /**
         * 
		 * method description: set play pause Enabled
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void setPlayPauseEnabled(boolean isEnabled) {
            
            mPlayPauseIm.setEnabled(isEnabled);
        }

        /**
         * 
		 * method description: display layout
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public void showPlayControl() {
            if (mControl != null) {
                mControl.show();
            }
        }

        /**
         * 
		 * method description: the native code is transplanted here, in order to display the layout of the native screen mode switch button
		 * @param parameter name Description
  		 * @return return type description
 		 * / class / @see class name complete complete class # method name
         */
        public void setControllerButtonPosition(int position) {
            
            if (mControl != null) {
                mControl.setControllerButtonPosition(position);
            }
        }

        /**
         * 
		 * method description: there are differences between the native and suspended window of the progress bar, where differences in management
		 * @param parameter name context description context
		 * @param parameter name listener description progress bar drag and drop monitor
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public TimeBar createTimeBar(Context context,
                TimeBar.Listener listener) {
            
            if (mControl != null) {
                TimeBar timeBar = mControl.createTimeBar(context, listener);
                return timeBar;
                /// M: set timebar id for test case @{
            }
            return new TimeBar(context, listener);
        }

        /**
         * 
		 * method description: here transplanted native code, in order to screen mode switch button native layout display right margin
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
        public int getAddedRightPadding() {
            
            if (mControl != null) {
                return mControl.getAddedRightPadding();
            }
            return 0;
        }

        /**
         * The method described here is: * in order to be compatible with the native function
		 * @param state playback state parameter name
		 * @param canPause indicates whether the parameter name can be suspended
		 * @return return type specification
		 * / class / @see class name complete complete class # method name

         */
        public void handlePlayPause(State state, boolean canPause) {
            
            if (mControl != null) {
                mControl.handlePlayPause(state, canPause);
            }
        }

        /**
         * The method described here is: * in order to be compatible with the native function
		 * @param state playback state parameter name
		 * @param canPause indicates whether the parameter name can be suspended
		 * @return return type specification
		 * / class / @see class name complete complete class # method name
         */
        public void updatePlayPauseVisible(State state, boolean canPause) {
            
            if (mControl != null) {
                mControl.updatePlayPauseVisible(state, canPause);
            }
        }
        /*PRIZE-funtionbug:452-wanzhijuan-2015-5-12-start*/
        /**
         * 
		 * method description: when the function locks, displays the processing
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
         */
		public void lockView() {
			
			if (mControl != null) {
                mControl.lockView();
            }
		}

		/**
		 * 
		 * method description: when the function locks, returns the key, the Menu key processing
		 * @param parameter name Description
		 * @return return type description
		 * / class / @see class name complete complete class # method name
		 */
		public void onLockKey() {
			if (mControl != null) {
                mControl.onLockKey();
            }
		} 
		
		public void unLock() {
			if (mControl != null) {
                mControl.unLock();
            }
		}
		/*PRIZE-funtion lock bug:452-wanzhijuan-2015-5-12-end*/
    }
    
    /**
     * 
     **
     * Class description: play pause, screen mode switch, the difference between the old and new features to deal with
     * @author auther
     * @version version
     */
    public interface IControl {
        /**
         * 
         * Method description: update play pause status
         * @param Parameter name Description play status
         * @return return type description
         * @see / class / @see class name complete complete class # method name
         */
        void updateLayout(int w, int h, int pr, int pl, int y, int timeBarHeight);
        /**
         * 
         * Method description:When the screen is locked, the return is not processed.
         * @param Parameter name Description
         * @return  return type description
         * @see  / class / @see class name complete complete class # method name
         */
        void onLockKey();

		void lockView();

        void setControllerButtonPosition(int position);

        void updatePlayPause(State state);

        int getBottomHeight();

        void showPlayPause();
     
        void unShowPlayPause();
       
        void show();
       
        void hide();
    
        void cancelHiding();
        
        void startHideAnimation(Animation hideAnimation);
      
        TimeBar createTimeBar(Context context, TimeBar.Listener listener);
       
        int getAddedRightPadding();
      
        void handlePlayPause(State state, boolean canPause);
       
        void updatePlayPauseVisible(State state, boolean canRePlay);
        
        void unLock();
    }
    /*PRIZE-supportion suppusion funtion-wanzhijuan-2015-4-13-end*/
}
