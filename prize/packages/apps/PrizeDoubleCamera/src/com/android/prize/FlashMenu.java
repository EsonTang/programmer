/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.android.prize;

import java.util.ArrayList;

import com.android.camera.ui.PickerButton;
import com.android.camera.ui.PickerButton.Listener;
import com.android.camera.ui.RotateImageView;
import com.mediatek.camera.setting.preference.IconListPreference;
import com.mediatek.camera.setting.preference.ListPreference;

import android.content.Context;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Scroller;

import com.android.camera.CameraActivity;
import com.android.camera.R;

import com.android.camera.ui.PickerButton.Listener;

public class FlashMenu extends RelativeLayout implements CameraActivity.OnContinueShotListener{

	private static final String TAG = "FlashMenu";

	private ViewGroup menuContain;
	
	private boolean isShowChild = false;

	private int selectFlashValue;
	
	private PickerButton mFlashPickerButton;
	
	private Listener mListener;

	private IconListPreference mPreference;
  
	private  ArrayList<Integer> flashSort;
	
	Scroller mScroller=null; 
	
	FlashMenuOnListerern mFlashMenuOnListerern;
	
	private boolean isRuningAnimation = false;
	
	/*prize-xuchunming-20180508-bugid:54875-start*/
	private int  mRestorContinueshotFlash = 0;
	/*prize-xuchunming-20180508-bugid:54875-end*/
	public interface FlashMenuOnListerern{
		public void onShowFlashMenu();
		public void onHideFlashMenu();
		public void onCompleteAnimation(boolean isShow);
	}
	
	public FlashMenu(Context context, AttributeSet attrs){
		 super(context, attrs);  
	     mScroller=new Scroller(context,new DecelerateInterpolator()); 
	     /*prize-xuchunming-20180508-bugid:54875-start*/
	     ((CameraActivity)context).addOnContinueShotListeners(this);
	     /*prize-xuchunming-20180508-bugid:54875-end*/
	}
    

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "onClick isShowChild:"+isShowChild);
			if (isShowChild == false) {
				/*prize-xuchunming-20180503-bugid:56870-start*/
				if(((CameraActivity)getContext()).isBatteryLow() == true) {
					((CameraActivity)getContext()).showLowPowerInfo();
					return;
				}
				/*prize-xuchunming-20180503-bugid:56870-end*/
				startAnimationsOut(400);
			} else {
				startAnimationsIn(400);
				/*prize-xuchunming-20180503-bugid:56870-start*/
				if(((CameraActivity)getContext()).isBatteryLow() == true) {
					((CameraActivity)getContext()).showLowPowerInfo();
					return;
				}
				/*prize-xuchunming-20180503-bugid:56870-end*/
				selectFlashValue =  (Integer) v.getTag();
				onItemClick(v, selectFlashValue);
			}

		}
	};

	public void initView(ViewGroup viewGroup,PickerButton mPickerButton) {
		// TODO Auto-generated method stub
		menuContain = viewGroup;
		mFlashPickerButton = mPickerButton;
		if (menuContain != null) {
			mFlashPickerButton.setOnClickListener(mOnClickListener);
			mFlashPickerButton.setTag(0);
			for (int i = 0; i < menuContain.getChildCount(); i++) {
				menuContain.getChildAt(i).setOnClickListener(mOnClickListener);
				menuContain.getChildAt(i).setTag(i+1);
			}
		}
	}
	
	protected void startAnimationsIn(int time) {
		
		// TODO Auto-generated method stub
		Log.d(TAG, "startAnimationsIn");
		isRuningAnimation = true;
		if(mFlashMenuOnListerern != null){
    		mFlashMenuOnListerern.onHideFlashMenu();
    	}
		resetFlashOrder(selectFlashValue);
		if(isLayoutRtl() == true){
			mScroller.startScroll(0, 0, -menuContain.getWidth(), 0, time);
		}else{
			mScroller.startScroll(0, 0, menuContain.getWidth(), 0, time);
		}
		invalidate();
	}

	protected void startAnimationsOut(int time) {
		
		// TODO Auto-generated method stub
		Log.d(TAG, "startAnimationsIn");
		isRuningAnimation = true;
		if(mFlashMenuOnListerern != null){
    		mFlashMenuOnListerern.onShowFlashMenu();
    	}
		if(isLayoutRtl() == true){
			mScroller.startScroll(-menuContain.getWidth(), 0, menuContain.getWidth(), 0, time);
		}else{
			mScroller.startScroll(menuContain.getWidth(), 0, -menuContain.getWidth(), 0, time);
		}
		
		menuContain.setVisibility(View.VISIBLE);
		/*for(int i=0; i< menuContain.getChildCount(); i++){
			menuContain.getChildAt(i).setVisibility(View.VISIBLE);
		}*/
		invalidate();
	}

	@Override
	public void computeScroll() {
		
		if (mScroller.computeScrollOffset()) { 
            menuContain.scrollTo(mScroller.getCurrX(), 0);  
            Log.d(TAG, "computeScroll mScroller.getCurrX() = "+mScroller.getCurrX());
            postInvalidate();  
        }else {
        	Log.d(TAG, "computeScroll isRuningAnimation = "+isRuningAnimation+",mFlashMenuOnListerern="+mFlashMenuOnListerern);
        	if(mFlashMenuOnListerern != null && isRuningAnimation == true){
        		isShowChild = !isShowChild;
        		mFlashMenuOnListerern.onCompleteAnimation(isShowChild);
        		isRuningAnimation = false;
        		Log.d(TAG, "onCompleteAnimation isShowChild:"+isShowChild);
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
        initFlashOrder();
        //reloadPreference();
    }
    
    public void reloadPreference() {
        Log.d(TAG, "reloadPreference() " + mPreference);
        if (mPreference == null || mPreference.getEntries() == null
                || mPreference.getEntries().length <= 1 || !mPreference.isEnabled()
                || mPreference.isShowInSetting()) {
        	mFlashPickerButton.setVisibility(View.GONE);
        	setVisibility(View.GONE);
        	if(mFlashMenuOnListerern != null && isRuningAnimation == true){
        		mFlashMenuOnListerern.onCompleteAnimation(false);
        		isRuningAnimation = false;
        		isShowChild = false;
        	}   
        } else {
            String value = mPreference.getOverrideValue();
            if (value == null) {
                value = mPreference.getValue();
            }
            int index = mPreference.findIndexOfValue(value);
            int[] icons = mPreference.getIconIds();
            if (icons != null) {
                if (index >= 0 && index < icons.length) {
                	mFlashPickerButton.setImageResource(icons[index]);
                } else {
                    index = getValidIndexIfNotFind(value);
                    mFlashPickerButton.setImageResource(icons[index]);
                }
            }
            
            selectFlashValue = index;
            initFlashOrder();
            resetFlashOrder(selectFlashValue);
            mFlashPickerButton.setTag(index);
            mFlashPickerButton.setVisibility(View.VISIBLE);
            setVisibility(View.VISIBLE);
        }
    }
    
    public void updateView() {
        reloadPreference();
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
        mPreference.setValueIndex(value);
        if (mListener != null && mListener.onPicked(mFlashPickerButton, mPreference, values[value].toString())) {
            // clear override value after user changed it
            //mPreference.setOverrideValue(null, false);
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
		mFlashPickerButton.setEnabled(enabled);
		/*prize-xuchunming-20160311-solve monkey error -end*/
		for(int i=0; i < menuContain.getChildCount(); i++){
			((PickerButton)menuContain.getChildAt(i)).setEnabled(enabled);
		}
	}

	public void setClickable(boolean enabled) {
		
		// TODO Auto-generated method stub
		/*prize-xuchunming-20160311-solve monkey error -start*/
		mFlashPickerButton.setClickable(enabled);
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
	
	public void initFlashOrder(){
		if(mPreference != null){
			if(flashSort == null){
				flashSort = new ArrayList<Integer>();
			}else{
				flashSort.clear();
			}
			
			for(int i= 0; i< mPreference.getEntryValues().length; i++){
				Log.d(TAG, "mPreference.getEntries()[i]:"+mPreference.getEntries()[i]);
				flashSort.add(i);
			}
			
		}
	}
	
	
	 /**
	 * 方法描述：重置flashe菜单选项的位置
	 * @param selectValue 当前的flash模式
	 * @return 返回类型 说明
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void resetFlashOrder(int selectValue){
		
			Log.d(TAG, "selectValue:"+selectValue);
			if(mPreference == null){
				Log.d(TAG, "selectValue mPreference == null return ");
				return;
			}
			flashSort.set(flashSort.indexOf(selectValue), flashSort.get(0));
			
			flashSort.set(0, selectValue);
			
			int[] icons = mPreference.getIconIds();
			
			mFlashPickerButton.setTag(flashSort.get(0));
			mFlashPickerButton.setImageResource(icons[flashSort.get(0)]);
			
			for(int i= 0; i< menuContain.getChildCount(); i++){
				if(i >= flashSort.size() -1){
					menuContain.getChildAt(i).setVisibility(View.INVISIBLE);
				}else{
					menuContain.getChildAt(i).setTag(flashSort.get(i+1));
					((PickerButton)menuContain.getChildAt(i)).setImageResource(icons[flashSort.get(i+1)]);
					menuContain.getChildAt(i).setVisibility(View.VISIBLE);
				}
			}
	}
	
	public void setFlashMenuOnListerern(FlashMenuOnListerern mFlashMenuOnListerern){
		this.mFlashMenuOnListerern = mFlashMenuOnListerern;
	}
	
	public void hideMenu(){
		if(isShowChild == true && isRuningAnimation == false){
			startAnimationsIn(400);
		}
	}
	
	public boolean isRuningAnimation(){
		return this.isRuningAnimation;
	}
	
	public boolean isShow(){
		return isShowChild;
	}
	
	/*prize-xuchunming-20180503-bugid:56870-start*/
	public void setFlashOff() {
		Log.d(TAG,"setFlashOff");
		if (menuContain != null) {
			for (int i = 0; i < menuContain.getChildCount(); i++) {
				int flashValue =  (Integer) menuContain.getChildAt(i).getTag();
				if(flashValue == 2) {
					onItemClick(menuContain.getChildAt(i), 2);
				}
				
			}
		}
	}
	/*prize-xuchunming-20180503-bugid:56870-end*/

	/*prize-xuchunming-20180508-bugid:54875-start*/
	@Override
	public void onContinueShot(boolean isShoting) {
		// TODO Auto-generated method stub
		Log.d(TAG, "mFlashPickerButton.getTag():"+(Integer)mFlashPickerButton.getTag());
		if((Integer) mFlashPickerButton.getTag() == 3) {
			return;
		}
		if(isShoting == true) {
			mRestorContinueshotFlash = (Integer) mFlashPickerButton.getTag();
			setFlash(2);
		}else {
			setFlash(mRestorContinueshotFlash);
		}
	}
	
	public void setFlash(int value){  //0 auto, 1 on, 2 off, 3 torch
		if (menuContain != null) {
			for (int i = 0; i < menuContain.getChildCount(); i++) {
				int flashValue =  (Integer) menuContain.getChildAt(i).getTag();
				if(flashValue == value) {
					onItemClick(menuContain.getChildAt(i), value);
				}
				
			}
		}
	}
	/*prize-xuchunming-20180508-bugid:54875-end*/
}
