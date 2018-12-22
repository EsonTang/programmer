package com.prize.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.gallery3d.app.CommonControllerOverlay;
import com.android.gallery3d.app.TimeBar;
import com.android.gallery3d.app.CommonControllerOverlay.State;
import com.android.gallery3d.R;

public class OldPlayPauseScreenExt implements CommonControllerOverlay.IControl {
    
    private ImageView mPlayPauseIm;
    private int mScreenPadding;
    private int mScreenWidth;

    private static final int MARGIN = 10; // dip
    private Context mContext;

    private int mControllerButtonPosition;
    public OldPlayPauseScreenExt(Context context, ViewGroup parent, View.OnClickListener listener, boolean isShowScreenView) {
        
        mContext = context; 
        
        // add mPlayPauseIm
        mPlayPauseIm = new ImageView(context);
        mPlayPauseIm.setContentDescription(
                context.getResources().getString(R.string.accessibility_play_video));
        mPlayPauseIm.setBackgroundResource(R.drawable.bg_vidcontrol);
        mPlayPauseIm.setScaleType(ScaleType.CENTER);
        mPlayPauseIm.setFocusable(true);
        mPlayPauseIm.setClickable(true);
        
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        LayoutParams wrapContent =
            new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        //for screen layout
        Bitmap screenButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.m_ic_media_bigscreen);
        mScreenWidth = screenButton.getWidth();
        mScreenPadding = (int) (metrics.density * MARGIN);
        screenButton.recycle();
    }
    
    public ImageView getPlayPause() {
        return mPlayPauseIm;
    }

    @Override
    public void updateLayout(int w, int h, int paddingRight, int pl, int yPosition, int timeBarHeight) {
        
        layoutCenteredView(mPlayPauseIm, 0, 0, w, h);
        
        int sw = getAddedRightPadding();
        int sepratorPosition = (w - paddingRight - sw - mControllerButtonPosition) / 2 + mControllerButtonPosition;
        int sepratorWidth = 2;
    }
    
    protected void layoutCenteredView(View view, int l, int t, int r, int b) {
        int cw = view.getMeasuredWidth();
        int ch = view.getMeasuredHeight();
        int cl = (r - l - cw) / 2;
        int ct = (b - t - ch) / 2;
        view.layout(cl, ct, cl + cw, ct + ch);
    }
    
    @Override
    public int getAddedRightPadding() {
        return mScreenPadding * 2 + mScreenWidth;
    }

    @Override
    public void updatePlayPause(State state) {
        
        mPlayPauseIm.setImageResource(
                state == State.PAUSED ? R.drawable.videoplayer_play :
                    state == State.PLAYING ? R.drawable.videoplayer_pause :
                        R.drawable.videoplayer_reload);
    }

    @Override
    public int getBottomHeight() {
        
        return 0;
    }

    @Override
    public void showPlayPause() {
        
        mPlayPauseIm.setVisibility(View.VISIBLE);
    }

    @Override
    public void unShowPlayPause() {
        
        mPlayPauseIm.setVisibility(View.GONE);
    }

    @Override
    public void setControllerButtonPosition(int position) {
        
        mControllerButtonPosition = position;
    }

    @Override
    public void show() {
        mPlayPauseIm.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        mPlayPauseIm.setVisibility(View.GONE);
    }

    @Override
    public void cancelHiding() {
        mPlayPauseIm.setAnimation(null);
    }

    @Override
    public void startHideAnimation(Animation hideAnimation) {
        
        if (mPlayPauseIm.getVisibility() == View.VISIBLE) {
        	mPlayPauseIm.startAnimation(hideAnimation);
        }
    }
    
    @Override
    public TimeBar createTimeBar(Context context,
            com.android.gallery3d.app.TimeBar.Listener listener) {
        
        return new TimeBar(context, listener);
    }
    
    @Override
    public void handlePlayPause(State state, boolean canPause) {
        mPlayPauseIm
        .setVisibility((state != State.LOADING
                && state != State.ERROR
                &&
                // !(state == State.ENDED && !canReplay) && //show
                // end when user stopped it.
                state != State.BUFFERING
                && state != State.RETRY_CONNECTING && !(state != State.ENDED
                && state != State.RETRY_CONNECTING_ERROR && !canPause))
        // for live streaming
        ? View.VISIBLE
                : View.GONE);
    }
    
    @Override
    public void updatePlayPauseVisible(State state, boolean canRePlay) {
        mPlayPauseIm.setVisibility(
                (state != State.LOADING && state != State.ERROR &&
                !(state == State.ENDED && !canRePlay))
                ? View.VISIBLE : View.GONE);
    }

	@Override
	public void lockView() {
		
	}

	@Override
	public void onLockKey() {
		
	}
	
	@Override
	public void unLock() {
		
	}

}

