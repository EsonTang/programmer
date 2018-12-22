package com.android.prize;

import java.util.ArrayList;

import com.android.camera.ui.PickerButton;
import com.android.camera.ui.PickerButton.Listener;
import com.android.prize.FlashMenu.FlashMenuOnListerern;
import com.mediatek.camera.setting.preference.IconListPreference;

import android.content.Context;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class HdrMenu extends RelativeLayout{

	private static final String TAG = "HdrMenu";

	private ViewGroup menuContain;
	
	private boolean isShowChild = false;

	private int selectHdrValue;
	
	Scroller mScroller=null; 
	
	private PickerButton mHdrPickerButton;
	
	private HdrMenuOnListerern mHdrMenuOnListerern;
	
	private Listener mListener;

	private IconListPreference mPreference;
	
	private boolean isRuningAnimation = false;
	
	public interface HdrMenuOnListerern{
		public boolean onStart();
		public boolean onCompleteAnimation(boolean isVisible);
	}

	public HdrMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mScroller=new Scroller(context,new DecelerateInterpolator()); 
	}
	
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			selectHdrValue =  (Integer) v.getTag();
			isShowChild = !isShowChild;
			Log.d(TAG, "onClick isShowChild:"+isShowChild);
			if (isShowChild) {
				startAnimationsOut(400);
			} else {
				onItemClick(v, selectHdrValue);
				startAnimationsIn(400);
			}

		}
	};

	public void initView(ViewGroup viewGroup,PickerButton mPickerButton) {
		// TODO Auto-generated method stub
		menuContain = viewGroup;
		mHdrPickerButton = mPickerButton;
		if (menuContain != null) {
			mHdrPickerButton.setOnClickListener(mOnClickListener);
			mHdrPickerButton.setTag(0);
			for (int i = 0; i < menuContain.getChildCount(); i++) {
				menuContain.getChildAt(i).setOnClickListener(mOnClickListener);
				menuContain.getChildAt(i).setTag(i+1);
			}
		}
	}
	
	protected void startAnimationsIn(int time) {
		
		// TODO Auto-generated method stub
		mScroller.startScroll(0, 0, menuContain.getWidth(), 0, time);
		invalidate();
	}

	protected void startAnimationsOut(int time) {
		
		// TODO Auto-generated method stub
		if(mHdrMenuOnListerern != null){
			mHdrMenuOnListerern.onCompleteAnimation(isShowChild);
    	}
		mScroller.startScroll(menuContain.getWidth(), 0, -menuContain.getWidth(), 0, time);
		for(int i=0; i< menuContain.getChildCount(); i++){
			menuContain.getChildAt(i).setVisibility(View.VISIBLE);
		}
		invalidate();
	}

	@Override
	public void computeScroll() {
		
		if (mScroller.computeScrollOffset()) { 
			isRuningAnimation = true;
            menuContain.scrollTo(mScroller.getCurrX(), 0);  
            postInvalidate();  
        }else {
        	isRuningAnimation = false;
        	if(mHdrMenuOnListerern != null){
        		Log.d(TAG, "computeScroll isShowChild:"+isShowChild);
        		mHdrMenuOnListerern.onCompleteAnimation(isShowChild);
        	}
        }
	}
	
	public void setListener(Listener listener) {
        mListener = listener;
    }
    
    public void reloadValue() {
        if (mPreference != null)
            mPreference.reloadValue();
    }
    
    public void initialize(IconListPreference pref) {
        Log.d(TAG, "initialize(" + pref + ")");
        mPreference = pref;
        reloadPreference();
    }
    
    public void reloadPreference() {
        Log.d(TAG, "reloadPreference() " + mPreference);
        if (mPreference == null || mPreference.getEntries() == null
                || mPreference.getEntries().length <= 1 || !mPreference.isEnabled()
                || mPreference.isShowInSetting()) {
        	mHdrPickerButton.setVisibility(View.GONE);
        	setVisibility(View.GONE);
        } else {
            String value = mPreference.getOverrideValue();
            if (value == null) {
                value = mPreference.getValue();
            }
            int index = mPreference.findIndexOfValue(value);
            int[] icons = mPreference.getIconIds();
            if (icons != null) {
                if (index >= 0 && index < icons.length) {
                	mHdrPickerButton.setImageResource(icons[index]);
                } else {
                    index = getValidIndexIfNotFind(value);
                    mHdrPickerButton.setImageResource(icons[index]);
                }
            }
            
            selectHdrValue = index;
            mHdrPickerButton.setVisibility(View.VISIBLE);
            setVisibility(View.VISIBLE);
        }
    }
    
    public void updateView() {
        if (mPreference == null || mPreference.getEntries() == null
                || mPreference.getEntries().length <= 1 || !mPreference.isEnabled()) {
        	mHdrPickerButton.setVisibility(View.GONE);
        	setVisibility(View.GONE);
           
        } else {
        	mHdrPickerButton.setVisibility(View.VISIBLE);
        	setVisibility(View.VISIBLE);
        }
    }
    
   
    public void onItemClick(View v, int value) {
        if (mPreference == null || value >= mPreference.getEntryValues().length) {
            Log.w(TAG, "onClick() why mPreference is null?", new Throwable());
            return;
        } else {
            if (!mPreference.isEnabled()) {
                Log.i(TAG, "onClick() mPreference's enable = false ,return this click event");
                return;
            }
        }
        CharSequence[] values = mPreference.getEntryValues();
        mPreference.setOverrideValue(null, false);
        if (mListener != null && mListener.onPicked(mHdrPickerButton, mPreference, values[value].toString())) {
            // clear override value after user changed it
            //mPreference.setOverrideValue(null, false);
        	mPreference.setValueIndex(value);// should be checked
            reloadPreference();
        }
    }
    
    public void setValue(String value) {
        Log.v(TAG, "setValue(" + value + ") mPreference=" + mPreference);
        if (mPreference != null && value != null && !value.endsWith(mPreference.getValue())) {
            mPreference.setValue(value);
            reloadPreference();
        }
    }
    
    protected int getValidIndexIfNotFind(String value) {
        return 0;
    }

	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		/*prize-xuchunming-20160311-solve monkey error -start*/
		mHdrPickerButton.setEnabled(enabled);
		/*prize-xuchunming-20160311-solve monkey error -end*/
		for(int i=0; i < menuContain.getChildCount(); i++){
			((PickerButton)menuContain.getChildAt(i)).setEnabled(enabled);
		}
	}

	public void setClickable(boolean enabled) {
		
		// TODO Auto-generated method stub
		/*prize-xuchunming-20160311-solve monkey error -start*/
		mHdrPickerButton.setClickable(enabled);
		/*prize-xuchunming-20160311-solve monkey error -end*/
		for(int i=0; i < menuContain.getChildCount(); i++){
			menuContain.getChildAt(i).setClickable(enabled);
		}
	}
	
	public ViewGroup getContian(){
		return menuContain;
	}
	
	public void reLayoutFlashMenu(String value){
		
	}
	
	public void setFlashMenuOnListerern(HdrMenuOnListerern mHdrMenuOnListerern){
		this.mHdrMenuOnListerern = mHdrMenuOnListerern;
	}
	
	public void hideMenu(){
		if(isShowChild == true){
			menuContain.scrollTo(menuContain.getWidth(), 0);
			isShowChild = false;
		}
	}
	
	public boolean isRuningAnimation(){
		return this.isRuningAnimation;
	}
}
