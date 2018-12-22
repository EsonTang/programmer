package com.prize.ui;
import java.util.concurrent.locks.LockSupport;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import com.android.gallery3d.R;
import com.android.gallery3d.app.CommonControllerOverlay;
import com.android.gallery3d.app.TimeBar;
import com.android.gallery3d.app.CommonControllerOverlay.IControl;
import com.android.gallery3d.app.CommonControllerOverlay.State;
import com.android.gallery3d.app.TimeBar.Listener;
import com.prize.sticker.WordResource;
import com.prize.util.DensityUtil;
import com.prize.util.LogTools;
//prize-wuliang-20180418 lockview
import android.content.res.Configuration;
import com.mediatek.common.prizeoption.PrizeOption;

/**
 * The common playback controller for the Movie Player or Video Trimming.
 */
public class PlayControllerOverlay extends FrameLayout implements
    View.OnClickListener, CommonControllerOverlay.IControl, OnCheckedChangeListener {
    
    public interface Listener {
        void onControlPlay();
        void onControlPlayNext();
        void onControlPlayPre();
        void onLockScreen(boolean isLock);
    }
    
    protected static final String TAG = "Gallery2/PlayControllerOverlay";
    private Listener mListener;
    protected final ImageView mPlayPauseReplayIm;
    protected final ImageView mPlayNextIm;
    protected final ImageView mPlayPreIm;
    protected CheckBox mLockScreenCb;
    private int mVPadding;
    private int mHeight;
    private Context mContext;
    
    
    private static final int PADDING_BOTTOM = 10; // dip
    
    public PlayControllerOverlay(Context context) {
        this(context, null);
    }
    
    public void setListener(Listener listener) {
        mListener = listener;
    }
    
    public ImageView getPlayPauseView() {
        return mPlayPauseReplayIm;
    }

    public PlayControllerOverlay(Context context, Listener listener) {
        super(context);
        mContext = context;
        mListener = listener;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        LayoutParams wrapContent =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        LayoutParams matchParent =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mPlayPauseReplayIm = new ImageView(context);
        mPlayPauseReplayIm.setImageResource(R.drawable.new_videoplayer_play);
        mPlayPauseReplayIm.setScaleType(ScaleType.CENTER);
        mPlayPauseReplayIm.setFocusable(true);
        mPlayPauseReplayIm.setClickable(true);
        mPlayPauseReplayIm.setOnClickListener(this);
        addView(mPlayPauseReplayIm, wrapContent);
        
        mPlayNextIm = new ImageView(context);
        mPlayNextIm.setImageResource(R.drawable.videoplayer_next);
        mPlayNextIm.setScaleType(ScaleType.CENTER);
        mPlayNextIm.setFocusable(true);
        mPlayNextIm.setClickable(true);
        mPlayNextIm.setOnClickListener(this);
        addView(mPlayNextIm, wrapContent);
        
        mPlayPreIm = new ImageView(context);
        mPlayPreIm.setImageResource(R.drawable.videoplayer_pre);
        mPlayPreIm.setScaleType(ScaleType.CENTER);
        mPlayPreIm.setFocusable(true);
        mPlayPreIm.setClickable(true);
        mPlayPreIm.setOnClickListener(this);
        addView(mPlayPreIm, wrapContent);
        
        mVPadding = (int) (metrics.density * PADDING_BOTTOM);
        
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
    }
    
    @Override
    public void onClick(View view) {
        LogTools.i(TAG, "onClick() view=" + view);
        if (view == mPlayNextIm) {
            mListener.onControlPlayNext();
        } else if (view == mPlayPauseReplayIm) {
            mListener.onControlPlay();
        } else if (view == mPlayPreIm) {
            mListener.onControlPlayPre();
        } 
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
        int imH = mPlayPauseReplayIm.getMeasuredHeight();
        int imW = mPlayPauseReplayIm.getMeasuredWidth();
        int imTop = h - mVPadding - pb - imH;
        int imBotton = h - mVPadding - pb;
        
        int space = (w - imW * 5) / 2;
        if (space < 0) {
            space = 0;
        }
        int leftP = (170 * space) / 430;
        int rightP = (80 * space) / 430;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            
        } else {
            leftP = (20 * space) / 430;
        }
        
        LogTools.i(TAG, "onLayout() imH=" + imH + " imW=" + imW + " h=" + h + " w=" + w + " pb=" + pb + " mVPadding=" + mVPadding);
        mPlayPauseReplayIm.layout((w - imW) / 2, imTop, (w - imW) / 2 + imW, imBotton);
        mPlayPreIm.layout((w - imW) / 2 - rightP - imW, imTop, (w - imW) / 2 - rightP, imBotton);
        mPlayNextIm.layout((w - imW) / 2 + imW + rightP, imTop, (w - imW) / 2 + 2 * imW + rightP, imBotton);
        
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
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LogTools.i(TAG, "onMeasure() widthSize=" + widthSize + " heightSize=" + heightSize + " mHeight=" + mHeight);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        mHeight = mPlayNextIm.getMeasuredHeight() + mVPadding;
        setMeasuredDimension(widthSize, mHeight);
    }

    public boolean isPlayPauseEanbled() {
        
        return mPlayPauseReplayIm.isEnabled();
    }

    public void setPlayPauseEnabled(boolean isEnabled) {
        
        mPlayPauseReplayIm.setEnabled(isEnabled);
    }
    
    public int getPreferredHeight() {
        return mHeight;
    }

    public void setPlayPauseResource(State mState) {
        
        mPlayPauseReplayIm.setImageResource(
                mState == State.PAUSED ? R.drawable.new_videoplayer_play :
                    mState == State.PLAYING ? R.drawable.new_videoplayer_pause :
                        R.drawable.new_videoplayer_play);
    }

    @Override
    public void updateLayout(int w, int h, int pr, int pl, int y, int timeBarHeight) {
        
        layout(pl, y - getPreferredHeight(), w - pr, y);
        layoutLockScreen(w, h);
    }

    @Override
    public void updatePlayPause(State state) {
        
        setPlayPauseResource(state);
    }

    @Override
    public int getBottomHeight() {
        
        return getPreferredHeight();
    }

    @Override
    public void showPlayPause() {
        
//        setVisibility(View.VISIBLE);
    }

    @Override
    public void unShowPlayPause() {
        
//        setVisibility(View.GONE);
    }
    
    @Override
    public void setControllerButtonPosition(int position) {
        
        
    }

    @Override
    public void show() {
        
        setVisibility(View.VISIBLE);
        mLockScreenCb.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(View.INVISIBLE);
        mLockScreenCb.setVisibility(View.INVISIBLE);
    }

    @Override
    public void cancelHiding() {
        setAnimation(null);
        mLockScreenCb.setAnimation(null);
    }

    @Override
    public void startHideAnimation(Animation hideAnimation) {
        
    	if (getVisibility() == View.VISIBLE) {
    		startAnimation(hideAnimation);
    	}
    	
    	if (mLockScreenCb.getVisibility() == View.VISIBLE) {
    		mLockScreenCb.startAnimation(hideAnimation);
    	}
    }

    @Override
    public TimeBar createTimeBar(Context context,
            com.android.gallery3d.app.TimeBar.Listener listener) {
        
        return new TimeSeekBar(context, listener);
    }

    @Override
    public int getAddedRightPadding() {
        return 0;
    }
    
    @Override
    public void handlePlayPause(State state, boolean canPause) {
        
    }
    
    @Override
    public void updatePlayPauseVisible(State state, boolean canRePlay) {
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		// TODO 
		if (mListener != null) {
			mListener.onLockScreen(isChecked);
		}
		LogTools.i(TAG, "onCheckedChanged() isChecked=" + isChecked);
	}
	
	private void layoutLockScreen(int w, int h) {
		int lockHeight = mLockScreenCb.getMeasuredHeight();
        int lockWidth = mLockScreenCb.getMeasuredWidth();
        int lockTop = (h - lockHeight) / 2;

        int lockLeft = DensityUtil.dip2px(mContext, 10);
        //priz-wuliang-20180418  lockview start
        Configuration mConfiguration = getContext().getResources().getConfiguration();
        if (mConfiguration != null && mConfiguration.orientation == mConfiguration.ORIENTATION_LANDSCAPE
            && PrizeOption.PRIZE_NOTCH_SCREEN) {
    		lockLeft = DensityUtil.dip2px(mContext, 32);
        }
        //priz-wuliang-20180418  lockview end
        mLockScreenCb.layout(lockLeft, lockTop, lockLeft + lockWidth, lockTop + lockHeight);
	}

	public void createLockScreen(ViewGroup parentView) {
		
		LayoutParams wrapContent =
                new LayoutParams(WordResource.dp2px(mContext, 34), WordResource.dp2px(mContext, 34));
        mLockScreenCb = new CheckBox(mContext);
        mLockScreenCb.setButtonDrawable(0);
        mLockScreenCb.setBackgroundResource(R.drawable.sel_lock_screen);
        mLockScreenCb.setFocusable(false);
        mLockScreenCb.setOnCheckedChangeListener(this);
        parentView.addView(mLockScreenCb, wrapContent);
	}

	@Override
	public void lockView() {
		LogTools.i(TAG, "lockView()");
		setVisibility(View.INVISIBLE);
		mLockScreenCb.setVisibility(View.VISIBLE);
	}

	@Override
	public void onLockKey() {
		if (mLockScreenCb.getVisibility() == View.VISIBLE) {
    		mLockScreenCb.setAnimation(null);
    		mLockScreenCb.startAnimation(AnimationUtils
                .loadAnimation(mContext, R.anim.lock_back));
    	}
	}
	
	@Override
	public void unLock() {
		mLockScreenCb.setChecked(false);
	}
}