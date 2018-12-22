
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：支持悬浮功能,修剪界面
 *当前版本：
 *作 者：wanzhijuan
 *完成日期：2015-6-30
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize.ui;

import android.content.Context;
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

public class TrimVideoPlayPauseExt implements CommonControllerOverlay.IControl {
    
    /** play pause**/
    private ImageView mPlayPauseIm;
    private Context mContext;

    private int mControllerButtonPosition;
    public TrimVideoPlayPauseExt(Context context, ViewGroup parent) {
        
        mContext = context; 
        
        // add mPlayPauseIm
        mPlayPauseIm = new ImageView(context);
        mPlayPauseIm.setContentDescription(
                context.getResources().getString(R.string.accessibility_play_video));
//        mPlayPauseIm.setBackgroundResource(R.drawable.bg_vidcontrol);
        mPlayPauseIm.setScaleType(ScaleType.CENTER);
        mPlayPauseIm.setFocusable(true);
        mPlayPauseIm.setClickable(true);
    }
    
    public ImageView getPlayPause() {
        return mPlayPauseIm;
    }

    @Override
    public void updateLayout(int w, int h, int paddingRight, int pl, int yPosition, int timeBarHeight) {
        
        layoutCenteredView(mPlayPauseIm, 0, 0, w, h);
    }
    
    /**
     * 
     * Method description: View Center display
     * @param parameter name l description position relative, to parent Left
     * @param parameter name t description position relative, to parent Top
     * r Right position @param parameter name, relative to parent
     * B Bottom position @param parameter name, relative to parent
     * @return return type specification
     * / class / @see class name complete complete class # method name
     */
    protected void layoutCenteredView(View view, int l, int t, int r, int b) {
        int cw = view.getMeasuredWidth();
        int ch = view.getMeasuredHeight();
        int cl = (r - l - cw) / 2;
        int ct = (b - t - ch) / 2;
        view.layout(cl, ct, cl + cw, ct + ch);
    }
    
    @Override
    public int getAddedRightPadding() {
        return 0;
    }

    @Override
    public void updatePlayPause(State state) {
        
        mPlayPauseIm.setImageResource(
                state == State.PAUSED ? R.drawable.sel_trim_play :
                    state == State.PLAYING ? R.drawable.sel_trim_pause :
                        R.drawable.sel_trim_reload);
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
    	
    	// when play animation , Isn't Judage show
    	
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

    /*PRIZE-funtion lock bug:452-wanzhijuan-2015-5-12-start*/
	@Override
	public void lockView() {
		
	}

	@Override
	public void onLockKey() {
		
	}
	
	@Override
	public void unLock() {
		
	}
	/*PRIZE-funtion lock bug:452-wanzhijuan-2015-5-12-end*/

}

