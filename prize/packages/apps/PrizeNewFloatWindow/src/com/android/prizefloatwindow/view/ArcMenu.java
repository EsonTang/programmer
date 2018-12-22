package com.android.prizefloatwindow.view;

import com.android.prizefloatwindow.R;
import com.android.prizefloatwindow.config.Config;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


public class ArcMenu extends FrameLayout {
    private ArcLayout mArcLayout;

    private TextView mHintView;
    private ViewGroup controlLayout;
    private OnModeSeletedListener mOnModeSeleter;
    private int mPosition;

    public ArcMenu(Context context) {
        super(context);
        init(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        applyAttrs(attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    public ArcMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        applyAttrs(attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }
    private void init(Context context) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.arc_menu, this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mArcLayout = (ArcLayout) findViewById(R.id.item_layout);

        controlLayout = (ViewGroup) findViewById(R.id.control_layout);
        controlLayout.setClickable(true);
        controlLayout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                	Log.d("snail_animal", "------controlLayout----onTouch-----ACTION_DOWN------");
                   // mHintView.startAnimation(createHintSwitchAnimation(mArcLayout.isExpanded()));
                    mArcLayout.switchState(true,mPosition);
                    mArcLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mOnModeSeleter != null){
                            	//Log.d("snail_animal", "------controlLayout----onTouch-----ACTION_DOWN----onModeSelected--");
                            	mOnModeSeleter.onModeSelected();
                            }
                        }
                    },150);//150

                }

                return false;
            }
        });
        this.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
		    		Log.d("snail_", "----------setOnKeyListener---KEYCODE_BACK-------");
		    	} 
				return false;
			}
		});

        mHintView = (TextView) findViewById(R.id.control_hint);
    }

    private void applyAttrs(AttributeSet attrs) { 
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ArcLayout, 0, 0);

            float fromDegrees = a.getFloat(R.styleable.ArcLayout_fromDegrees, ArcLayout.DEFAULT_FROM_DEGREES);
            float toDegrees = a.getFloat(R.styleable.ArcLayout_toDegrees, ArcLayout.DEFAULT_TO_DEGREES);
            mArcLayout.setArc(fromDegrees, toDegrees);

            int defaultChildSize = mArcLayout.getChildSize();
            int newChildSize = a.getDimensionPixelSize(R.styleable.ArcLayout_childSize, defaultChildSize);
            mArcLayout.setChildSize(newChildSize);

            a.recycle();
        }
    }
    public void applySizeChange(float persents){
        //setArcLayoutSize(Config.DEFAULT_MAX_LENGTH);
        setMenuSize(Config.DEFAULT_MIN_LENGTH);
        setHintViewSize(Config.DEFAULT_MIN_LENGTH);//DEFAULT_MIN_LENGTH
    }

    public void setMenuSize(int radiu){
        mArcLayout.setChildSize(radiu);
    }

    public void setHintViewSize(int width){
        FrameLayout.LayoutParams layoutParams= (LayoutParams) mHintView.getLayoutParams();
        if (layoutParams!=null){
            layoutParams.width=width;
            layoutParams.height=width;
            mHintView.setLayoutParams(layoutParams);
            mHintView.setMaxWidth(width);
            mHintView.setMaxHeight(width);
        }
    }
    public void setArcLayoutSize(int width){
        ViewGroup.LayoutParams layoutParams= (LayoutParams) mArcLayout.getLayoutParams();
        if (layoutParams!=null){
            layoutParams.width=width;
            layoutParams.height=width;
            mArcLayout.setLayoutParams(layoutParams);
        }
    }

    public void addItem(View item, OnClickListener listener) {
        mArcLayout.addView(item);
        item.setOnClickListener(getItemClickListener(listener));
    }
    public void updateArcMenuItemTexts(int[] imgs) {
    	//String[] contentDiscription  = texts;
    	
		for(int i=0;i< imgs.length;i++){
			View view = mArcLayout.getChildAt(i);
			ImageView img = (ImageView)view.findViewById(R.id.arc_menu_item_img);
			img.setImageResource(imgs[i]);
		}
	}
    private OnClickListener getItemClickListener(final OnClickListener listener) {
        return new OnClickListener() {

            @Override
            public void onClick(final View viewClicked) {
                Animation animation = bindItemAnimation(viewClicked, true, 350);
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                itemDidDisappear();
                            }
                        }, 0);
                    }
                });

                final int itemCount = mArcLayout.getChildCount(); 
                for (int i = 0; i < itemCount; i++) {
                    View item = mArcLayout.getChildAt(i);
                    if (viewClicked != item) {
                        bindItemAnimation(item, false, 300);
                    }
                }

                mArcLayout.invalidate();
               // mHintView.startAnimation(createHintSwitchAnimation(true));

                if (listener != null) {
                    listener.onClick(viewClicked);
                }
            }
        };
    }

    private Animation bindItemAnimation(final View child, final boolean isClicked, final long duration) {
        Animation animation = createItemDisapperAnimation(duration, isClicked);
        child.setAnimation(animation);

        return animation;
    }

    private void itemDidDisappear() {
        final int itemCount = mArcLayout.getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View item = mArcLayout.getChildAt(i);
            item.clearAnimation();
        }
        mArcLayout.switchState(false,mPosition);
    }

    private static Animation createItemDisapperAnimation(final long duration, final boolean isClicked) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(1.0f, isClicked ? 2.0f : 0.0f, 1.0f, isClicked ? 2.0f : 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f));
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

        animationSet.setDuration(duration);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setFillAfter(true);

        return animationSet; 
    }

    private static Animation createHintSwitchAnimation(final boolean expanded) {
        Animation animation = new RotateAnimation(expanded ? 180 : 0, expanded ? 0 : 180, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setStartOffset(0);
        animation.setDuration(300);    
        animation.setFillAfter(true);

        return animation;
    }

    public void performClickShowMenu(int position) { 
        mPosition = position;
        Log.d("snail_animal", "-----performClickShowMenu------isExpanded=="+mArcLayout.isExpanded());
        mHintView.startAnimation(createHintSwitchAnimation(mArcLayout.isExpanded()));
        mArcLayout.switchState(true,position);
    }
    /**
     * 根据按钮位置改变子菜单方向
     */
    public void refreshPathMenu(int position) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mArcLayout.getLayoutParams();
        FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) controlLayout.getLayoutParams();

        switch (position) {
            case PathMenu.LEFT_TOP://左上
                params1.gravity = Gravity.LEFT | Gravity.TOP;
                params1.leftMargin = Config.dp2px(5);
                params1.topMargin = Config.dp2px(5);
                params.leftMargin = Config.dp2px(5);
                params.topMargin = Config.dp2px(5);
                params.gravity = Gravity.LEFT | Gravity.TOP;
                mArcLayout.setArc(0, 90, position);
                break;
            case PathMenu.LEFT_CENTER://左中
                params1.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                params1.leftMargin = Config.dp2px(5);
                params.leftMargin = Config.dp2px(5);
                params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                mArcLayout.setArc(270, 270 + 180, position);
                break;
            case PathMenu.LEFT_BOTTOM://左下
                params1.gravity = Gravity.LEFT | Gravity.BOTTOM;
                params1.leftMargin = Config.dp2px(5);
                params1.bottomMargin = Config.dp2px(5);
                params.leftMargin = Config.dp2px(5);
                params.bottomMargin = Config.dp2px(5);
                params.gravity = Gravity.LEFT | Gravity.BOTTOM;
                mArcLayout.setArc(270, 360, position);
                break;
            case PathMenu.RIGHT_TOP://右上
                params1.gravity = Gravity.RIGHT | Gravity.TOP;
                params.gravity = Gravity.RIGHT | Gravity.TOP;
                params1.rightMargin = Config.dp2px(5);
                params1.topMargin = Config.dp2px(5);
                params.rightMargin = Config.dp2px(5);
                params.topMargin = Config.dp2px(5);
                mArcLayout.setArc(180, 90, position);
                break;
            case PathMenu.RIGHT_CENTER://右中
                params1.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                //if(ArcTipViewController.getInstance().getRotation() == Surface.ROTATION_270){
                //	params1.rightMargin = Config.dp2px(5)+75;
                 //   params.rightMargin = Config.dp2px(5)+75;
                //}else {
                	params1.rightMargin = Config.dp2px(5);
                    params.rightMargin = Config.dp2px(5);
				//}
                mArcLayout.setArc(270, 90, position);
                break;
            case PathMenu.RIGHT_BOTTOM://右下
                params1.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                params1.rightMargin = Config.dp2px(5);
                params1.bottomMargin = Config.dp2px(5);
                params.rightMargin = Config.dp2px(5);
                params.bottomMargin = Config.dp2px(5);
                mArcLayout.setArc(270, 180, position);
                break;

            case PathMenu.CENTER_TOP://上中
                params1.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                params1.topMargin = Config.dp2px(5);
                params.topMargin = Config.dp2px(5);
                mArcLayout.setArc(0, 180, position);
                break;
            case PathMenu.CENTER_BOTTOM://下中
                params1.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                params1.bottomMargin = Config.dp2px(5);
                params.bottomMargin = Config.dp2px(5);
                mArcLayout.setArc(180, 360, position);
                break;
            case PathMenu.CENTER:
                params1.gravity = Gravity.CENTER;
                params.gravity = Gravity.CENTER;
                mArcLayout.setArc(0, 360, position);
                break;
        }
        controlLayout.setLayoutParams(params1);
        mArcLayout.setLayoutParams(params);
    }

    public void reset() {
        //mHintView.startAnimation(createHintSwitchAnimation(mArcLayout.isExpanded()));
        if (mArcLayout.isExpanded()) {
            mArcLayout.switchState(false);
            mArcLayout.setExpand(false);
        }
        
    }

    public void removeAllItemViews() {
        mArcLayout.removeAllViews();
    }

    public TextView getHintView() { 
        return mHintView;
    } 

    public interface OnModeSeletedListener{
        void onModeSelected();
        void onBackClick();
    } 
    public void setOnModeSeletedListener(OnModeSeletedListener onModeSeletedListener) {
        mOnModeSeleter = onModeSeletedListener;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
    		Log.d("snail_", "----------dispatchKeyEvent-----KEYCODE_BACK------");
    	} 
    	return super.dispatchKeyEvent(event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO Auto-generated method stub
    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
    		Log.d("snail_", "----------onKeyDown---KEYCODE_BACK-------");
    	} 
    	return super.onKeyDown(keyCode, event);
    }
}

