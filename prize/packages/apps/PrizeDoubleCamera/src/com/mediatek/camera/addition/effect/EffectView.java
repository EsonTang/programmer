/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.camera.addition.effect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.R;
import com.android.camera.manager.CombinViewManager;
import com.android.camera.manager.ViewManager;

import com.mediatek.camera.addition.effect.EffectLayout.OnScrollListener;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.platform.IModuleCtrl;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.preference.ListPreference;
import com.mediatek.camera.ui.CameraView;
import com.mediatek.camera.ui.RotateImageView;
import com.mediatek.camera.ui.UIRotateLayout;
import com.mediatek.camera.util.Log;
import com.mediatek.camera.util.Util;
import com.prize.ui.CenterHorizontalScroll;
import com.prize.ui.CenterHorizontalScroll.OnItemChangeListener;
import com.prize.ui.MulitPointTouchListener;
import com.prize.ui.MulitPointTouchListener.ViewTouchListener;

public class EffectView extends ViewManager implements ICameraView {

    private static final String TAG = "EffectView";

    public static final int SHOW_EFFECT = 0;
    public static final int HIDE_EFFECT = 1;
    public static final int ON_SIZE_CHANGED = 2;
    public static final int ON_EFFECT_DONE = 3;
    public static final int ON_CAMERA_CLOSE = 4;
    public static final int ON_CAMERA_OPEN_DONE = 5;
    public static final int CLOSE_EFFECT = 6;

    private static final int MSG_DELAY_ROTATE = 1;
    private static final int MSG_DISPLAY = 2;
    private static final int MSG_HIDE_EFFECT = 3;
    private static final int DELAY_ROTATE = 50;
    private static final int DELAY_MSG_REMOVE_GRID_MS = 3000;

    private static final String MTK_CONTROL_EFFECT_MODE_OFF = "none";
    private static final String MTK_CONTROL_EFFECT_MODE_MONO = "mono";
    private static final String MTK_CONTROL_EFFECT_MODE_NEGATIVE = "negative";
    private static final String MTK_CONTROL_EFFECT_MODE_SOLARIZE = "solarize";
    private static final String MTK_CONTROL_EFFECT_MODE_SEPIA = "sepia";
    private static final String MTK_CONTROL_EFFECT_MODE_POSTERIZE = "posterize";
    private static final String MTK_CONTROL_EFFECT_MODE_WHITEBOARD = "whiteboard";
    private static final String MTK_CONTROL_EFFECT_MODE_BLACKBOARD = "blackboard";
    private static final String MTK_CONTROL_EFFECT_MODE_AQUA = "aqua";
    private static final String MTK_CONTROL_EFFECT_MODE_SEPIAGREEN = "sepiagreen";
    private static final String MTK_CONTROL_EFFECT_MODE_SEPIABLUE = "sepiablue";

    private static final String MTK_CONTROL_EFFECT_MODE_NASHVILLE = "nashville"; // LOMO
    private static final String MTK_CONTROL_EFFECT_MODE_HEFE = "hefe";
    private static final String MTK_CONTROL_EFFECT_MODE_VALENCIA = "valencia";
    private static final String MTK_CONTROL_EFFECT_MODE_XPROII = "xproll";
    private static final String MTK_CONTROL_EFFECT_MODE_LOFI = "lofi";
    private static final String MTK_CONTROL_EFFECT_MODE_SIERRA = "sierra";
    private static final String MTK_CONTROL_EFFECT_MODE_KELVIN = "kelvin";
    private static final String MTK_CONTROL_EFFECT_MODE_WALDEN = "walden";
    private static final String MTK_CONTROL_EFFECT_MODE_F1977 = "f1977"; // LOMO
    private static final String MTK_CONTROL_EFFECT_MODE_NUM = "num";

    private int mNumsOfEffect = 0;
    private int mSelectedPosition = 0;

    private boolean mShowEffects = false;
    private boolean mNeedScrollToFirstPosition = false;
    private boolean mNeedStartFaceDetection = false;
    private boolean mEffectsDone = false;
    private boolean mMirror = false;

    private Animation mFadeIn;
    private Animation mFadeOut;
    private CenterHorizontalScroll mEffectScrollView;
    private TextView mCloseTv;
    private Listener mListener;
    private ViewGroup mEffectsLayout;

    private ListPreference mEffectPreference;
    private CharSequence[] mEffectEntryValues;
    private CharSequence[] mEffectEntries;

    private ICameraAppUi mICameraAppUi;
    private IModuleCtrl mIModuleCtrl;
    private boolean mIsClose;

     private static final String[] mEffectName = {
        MTK_CONTROL_EFFECT_MODE_OFF,
        MTK_CONTROL_EFFECT_MODE_MONO,
        MTK_CONTROL_EFFECT_MODE_NEGATIVE,
        MTK_CONTROL_EFFECT_MODE_SOLARIZE,
        MTK_CONTROL_EFFECT_MODE_SEPIA,
        MTK_CONTROL_EFFECT_MODE_POSTERIZE,
        MTK_CONTROL_EFFECT_MODE_WHITEBOARD,
        MTK_CONTROL_EFFECT_MODE_BLACKBOARD,
        MTK_CONTROL_EFFECT_MODE_AQUA,
        MTK_CONTROL_EFFECT_MODE_SEPIAGREEN,
        MTK_CONTROL_EFFECT_MODE_SEPIABLUE,
        MTK_CONTROL_EFFECT_MODE_NASHVILLE,
        MTK_CONTROL_EFFECT_MODE_HEFE,
        MTK_CONTROL_EFFECT_MODE_VALENCIA,
        MTK_CONTROL_EFFECT_MODE_XPROII,
        MTK_CONTROL_EFFECT_MODE_LOFI,
        MTK_CONTROL_EFFECT_MODE_SIERRA,
        MTK_CONTROL_EFFECT_MODE_KELVIN,
        MTK_CONTROL_EFFECT_MODE_WALDEN,
        MTK_CONTROL_EFFECT_MODE_F1977,
        MTK_CONTROL_EFFECT_MODE_NUM,
    };

    public interface Listener {
        public void onInitialize();

        public void onSurfaceAvailable(Surface surface, int width, int height, int effectIndex);

        public void onUpdateEffect(int pos, int effectIndex);

        public void onReceivePreviewFrame(boolean received);

        public void onRelease();

        public void onItemClick(String value);

        public void hideEffect(boolean anmiation, int animationTime);
    }

    public EffectView(CameraActivity activity) {
    	/*prize-xuchunming-adjust layout at 18:9 project-start*/
        super(activity,VIEW_LAYER_TOP);
        /*prize-xuchunming-adjust layout at 18:9 project-end*/
        Log.i(TAG, "[EffectView]constructor...");
    }

    @Override
    public void init(Activity activity, ICameraAppUi cameraAppUi, IModuleCtrl moduleCtrl) {
        Log.i(TAG, "[init]...");
        mIModuleCtrl = moduleCtrl;
        mICameraAppUi = cameraAppUi;
    }
    
    @Override
    protected View getView() {
    	mEffectsLayout = (ViewGroup) inflate(R.layout.lomo_effects_prize);
    	initialEffect();
        return mEffectsLayout;
    }

    @Override
    public boolean update(int type, Object... args) {
        switch (type) {
        case SHOW_EFFECT:
            mEffectPreference = (ListPreference) args[0];
            mMirror = (Boolean) args[1];
            /*prize-xuchunming-20161109-bugid:23411-start*/
            mICameraAppUi.dismissInfo();
            /*prize-xuchunming-20161109-bugid:23411-end*/
            showEffect();
            break;

        case HIDE_EFFECT:
            boolean animation = (Boolean) args[0];
            int animationTime = (Integer) args[1];
            hideEffect(animation, animationTime);
            break;
            
        case CLOSE_EFFECT:
            boolean anim = (Boolean) args[0];
            int animTime = (Integer) args[1];
            closeEffect(anim, animTime);
            break;

        case ON_SIZE_CHANGED:
            break;

        case ON_EFFECT_DONE:
            onEffectsDone();
            break;

        case ON_CAMERA_CLOSE:
            break;
            
        case ON_CAMERA_OPEN_DONE:
        	//mEffectPreference = (ListPreference) args[0];
            //mMirror = (Boolean) args[1];
            //refreshEffect();
        	break;
        default:
            break;
        }
        return false;
    }

    @Override
    public void setListener(Object listener) {
        mListener = (Listener) listener;
    }
    
    @Override
    public void reset() {
    	
    }

    public boolean onBackPressed() {
        Log.i(TAG, "[onBackPressed]");
        if (mShowEffects) {
            hideEffect(true, DELAY_MSG_REMOVE_GRID_MS);
            return true;
        } else {
            return false;
        }
    }

    public void showEffect() {
        Log.i(TAG, "[showEffect]..., start");
        mIsClose = false;
        show();
        mShowEffects = true;

        // need to reload value for the case of switch camera
        mEffectPreference.reloadValue();
        String value = mEffectPreference.getValue();
        mSelectedPosition = mEffectPreference.findIndexOfValue(value);
        Log.i(TAG, "refreshEffect showEffect value=" + value + " mSelectedPosition=" + mSelectedPosition);
        /*prize-xuchunming-20161102-bugid:23967-start*/
        if(mSelectedPosition >= 0 && mSelectedPosition < mEffectEntryValues.length){
            mEffectScrollView.toPosition(mSelectedPosition, mMirror);
       
	        if (mEffectsDone) {
	            startFadeInAnimation(mEffectsLayout);
	        }
	        if (mListener != null ) {
	            mListener.onItemClick(mEffectEntryValues[mSelectedPosition].toString());
	        }
        }else{
	        Log.i(TAG, "[showEffect]... error mSelectedPosition:"+mSelectedPosition);
	    }
        /*prize-xuchunming-20161102-bugid:23967-end*/
       
        Log.i(TAG, "[showEffect]..., end");
    }
    
    public void refreshEffect() {
    	Log.i(TAG, "refreshEffect mShowEffects=" + mShowEffects + " mMirror=" + mMirror);
    	if (mShowEffects) {

            // need to reload value for the case of switch camera
            mEffectPreference.reloadValue();
            String value = mEffectPreference.getValue();
            mSelectedPosition = mEffectPreference.findIndexOfValue(value);
            Log.i(TAG, "refreshEffect value=" + value + " mSelectedPosition=" + mSelectedPosition);
            mEffectScrollView.toPosition(mSelectedPosition, mMirror);
    	}
    }

    public void hideEffect(boolean animation, long delay) {
        Log.i(TAG, "hideEffect(), animation:" + animation + ", mEffectsLayout:" +
                "" + mEffectsLayout);
        mIsClose = true;
        if(mShowEffects == false){
        	return;
        }
        mShowEffects = false;
        if (animation) {
            startFadeOutAnimation(mEffectsLayout);
        }
        mICameraAppUi.restoreViewState();
        hide();
        if (mListener != null) {
            mListener.onItemClick(MTK_CONTROL_EFFECT_MODE_OFF);
        }
    }
    
    public void closeEffect(boolean animation, long delay) {
        Log.i(TAG, "hideEffect(), animation:" + animation + ", mEffectsLayout:" +
                "" + mEffectsLayout);
        mIsClose = true;
        if (mShowEffects == false){
        	return;
        }
        mShowEffects = false;
        if (animation) {
            startFadeOutAnimation(mEffectsLayout);
        }
        mICameraAppUi.restoreViewState();
        hide();
        mEffectPreference.setValue(MTK_CONTROL_EFFECT_MODE_OFF);
        if (mListener != null) {
            mListener.onItemClick(MTK_CONTROL_EFFECT_MODE_OFF);
        }
    }

    public boolean isShowEffects() {
        return mShowEffects;
    }

    public void onEffectsDone() {
        Log.i(TAG, "onEffectsDone()");
        mMainHandler.sendEmptyMessage(MSG_DISPLAY);
        mEffectsDone = true;
    }

    protected void startFadeInAnimation(View view) {
        if (mFadeIn == null) {
            mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.gird_effects_fade_in);
        }
        if (view != null && mFadeIn != null) {
            view.startAnimation(mFadeIn);
            mFadeIn = null;
        }
    }

    protected void startFadeOutAnimation(View view) {
        if (mFadeOut == null) {
            mFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.grid_effects_fade_out);
            mFadeOut.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // show();
                }
            });
        }
        if (view != null) {
            view.startAnimation(mFadeOut);
            mFadeOut = null;
        }
    }

    protected Handler mMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage(), msg:" + msg);
            switch (msg.what) {
            case MSG_DELAY_ROTATE:
                break;
            case MSG_HIDE_EFFECT:
                hideEffect(false, 0);
                break;
            case MSG_DISPLAY:
                if (mEffectsLayout != null) {
                    startFadeInAnimation(mEffectsLayout);
                    mEffectsLayout.setAlpha(1.0f);
                }
                break;
            default:
                break;
            }
        };
    };

    private List<String> getEffectEntryList() {
    	List<String> effectEntryList = new ArrayList<String>(mEffectEntries.length);
    	Log.i(TAG, "[getEffectEntryList] lenth=" + mEffectEntryValues.length);
    	for (int i = 0; i < mEffectEntries.length; i++) {
    		Log.i(TAG, "[getEffectEntryList] mEffectEntries=" + mEffectEntries[i] + " effectEntryValue=" + mEffectEntryValues[i]);
    		effectEntryList.add((String) mEffectEntries[i]);
    	}
    	return effectEntryList;
    }
    
	private ViewTouchListener mViewTouchListener = new ViewTouchListener() {
		
		@Override
		public void singleClick(MotionEvent e) {
            /*prize-modify-bugid:47691 the click position and focus position are inconsistent-xiaoping-20180116-start*/
            //getContext().getCameraActor().getonSingleTapUpListener().onSingleTapUp(null, (int)e.getX(), (int)e.getY());
            Log.d(TAG,"singleClick,getX: "+e.getX()+",getY: "+e.getY()+",getRawX: "+e.getRawX()+",getRawY: "+e.getRawY()+"getTop: "+getContext().getPreviewSurfaceView().getTop());
            int x;
            int y;
            int marginTop = getContext().getPreviewSurfaceView() != null ?  getContext().getPreviewSurfaceView().getTop() : 0;
            if(getContext().getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO) != null && getContext().getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO).equals("1.7778"))
            {
                x = (int)e.getX();
                y = (int)e.getY();
            }else {
                x = (int)e.getX();
                y = (int)e.getRawY() - marginTop;
            }
            getContext().getCameraActor().getonSingleTapUpListener().onSingleTapUp(null, x, y);
            /*prize-modify-bugid:47691 the click position and focus position are inconsistent-xiaoping-20180116-end*/
		}
		
		@Override
		public void scale(float scale) {
			/*prize-xuchuming-20180427-bugid:55041-start*/
			//mICameraAppUi.zoom(scale);
			/*prize-xuchuming-20180427-bugid:55041-end*/
		}
		
	};  

    private void initialEffect() {
        Log.i(TAG, "[initialEffect]mEffectsLayout:" + mEffectsLayout + ", mMirror:" + mMirror);
        mEffectEntryValues = mEffectPreference.getEntryValues();
        mEffectEntries = mEffectPreference.getEntries();
        mNumsOfEffect = mEffectPreference.getEntryValues().length;
        Log.i(TAG, "nums of effect:" + mNumsOfEffect);
        mEffectScrollView = (CenterHorizontalScroll) mEffectsLayout.findViewById(R.id.chs_effect);
        MulitPointTouchListener mulitPointTouchListener = new MulitPointTouchListener(getContext(), mEffectScrollView);
        mulitPointTouchListener.setViewTouchListener(mViewTouchListener);
        mEffectsLayout.setOnTouchListener(mulitPointTouchListener);
        mEffectScrollView.initAdapter(getEffectEntryList());
        mEffectScrollView.setOnItemChangeListener(new OnItemChangeListener() {
			
			@Override
			public void itemChange(int position) {
				if (!mIsClose) {
					mEffectPreference.setValue(mEffectEntryValues[position].toString());
	                if (mListener != null) {
	                    mListener.onItemClick(mEffectEntryValues[position].toString());
	                }
				}
			}
		});
        mCloseTv = (TextView) mEffectsLayout.findViewById(R.id.tv_close);
        mCloseTv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mIsClose = true;
				mEffectPreference.setValue(MTK_CONTROL_EFFECT_MODE_OFF);
				if (mListener != null) {
                    mListener.onItemClick(MTK_CONTROL_EFFECT_MODE_OFF);
                }
				mICameraAppUi.closeCombinView(CombinViewManager.COMBIN_EFFECT);
			}
		});
    }
    
    public void showCloseTv(){
		if(mCloseTv != null){
			mCloseTv.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideCloseTv(){
		if(mCloseTv != null){
			mCloseTv.setVisibility(View.INVISIBLE);
		}
	}

}
