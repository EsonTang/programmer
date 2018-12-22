package com.android.launcher3;

import com.android.gallery3d.util.LogUtils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class FitSystemWindow extends FrameLayout {

	public FitSystemWindow(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public FitSystemWindow(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public FitSystemWindow(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.requestFitSystemWindows();
		}
	
	

	public FitSystemWindow(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	
    public boolean fitSystemWindowWithPrizeScrollLayout(Rect insets) {
        final int n = getChildCount();
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (child instanceof Insettable) {
                ((Insettable)child).setInsets(insets);
            } else {
                flp.topMargin += (insets.top - mInsets.top);
                flp.leftMargin += (insets.left - mInsets.left);
                flp.rightMargin += (insets.right - mInsets.right);
                flp.bottomMargin += (insets.bottom - mInsets.bottom);
            }
            if (child instanceof FitSystemWindow)
            child.setLayoutParams(flp);
        }
        
        mInsets.set(insets);
        return true; // I'll take it from here
    }

    int bottom = 0;
     boolean first = true;
     int height=-1;
    private final Rect mInsets = new Rect();
	@Override
    protected boolean fitSystemWindows(final Rect insets) {
		if(Launcher.isSupportLeftnavbar) {
			insets.bottom=insets.bottom;
		}else {
			insets.bottom=0;
		}
		LogUtils.i("zhouerlong", "test---begin");
        final int n = getChildCount();
   	 Launcher l =(Launcher) getContext();
   	 if(l.ismPaused()) {
  		LogUtils.i("zhouerlong", "test----pause");
   		 return true;
   	 }

		LogUtils.i("zhouerlong", "test--no --pause");
        for (int i = 0; i < n; i++) {
            final View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (child instanceof Insettable) {
                ((Insettable)child).setInsets(insets);
            } else {
                    flp.topMargin += (insets.top - mInsets.top);
                flp.leftMargin += (insets.left - mInsets.left);
                flp.rightMargin += (insets.right - mInsets.right);
		                int bot=0;
						if(child instanceof PageIndicator) {
				            /*if(first&&flp.bottomMargin>0)  {
				                bottom=flp.bottomMargin;
				                first=false;
				            }*/
		                	 bot = (int) (insets.bottom/1.6f - mInsets.bottom/1.6f);
		                	 PageIndicator p = (PageIndicator) child;
		                	 bottom+=bot;
//		                	 if(!l.getworkspace().isInSpringLoadMoed()) {
				                    flp.bottomMargin += bot;
//		                	 }
				                    child.requestLayout();
		                }/*else if(child.getId() == R.id.overview_panel) {
//		                	bot=Launcher.navigationBarHeight;

		                	 bot =insets.bottom - mInsets.bottom;
		                	if(height==-1) {

			                	height = (int) (flp.height+bot);
		                	}
		                	
//		                    flp.bottomMargin = (int) bot;
//		                	flp.bottomMargin+=(int) bot;
		                	flp.height+=bot;
		                }*/else {

		                	 bot =insets.bottom - mInsets.bottom;
			                    flp.bottomMargin += bot;
		                }
            }
            if (child instanceof FitSystemWindow)
            child.setLayoutParams(flp);
        }
        	if(l.getSimple_main()!=null) {
            	((Insettable)l.getSimple_main()).setInsets(insets);
        	}
        
        mInsets.set(insets);
        return true; // I'll take it from here
    }

}
