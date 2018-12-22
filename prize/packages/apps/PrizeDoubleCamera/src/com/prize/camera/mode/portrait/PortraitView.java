package com.prize.camera.mode.portrait;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
import com.mediatek.camera.mode.facebeauty.FaceBeautyMode;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.IModuleCtrl;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.ui.CameraView;
/**
 * Created by prize on 2018/4/26.
 */

public class PortraitView extends CameraView implements View.OnClickListener {
    private static final String TAG = "PortraitView";
    private View mView;
    private IModuleCtrl mIMoudleCtrl;
    private ICameraAppUi mICameraAppUi;
    private LinearLayout compositeBeautiful;
    private LinearLayout smoothingBeautiful;
    private LinearLayout slimmingBeautiful;
    private LinearLayout catchlightBeautiful;
    private LinearLayout eyesEnlargementBeautiful;
    private CameraActivity mContext;
    private View focuseView;
    private int currentIndex = 0;
    private String currentValue = MTK_CONTROL_EFFECT_MODE_MONOCHROMATIC_LIGHT;
    private static final String MTK_CONTROL_EFFECT_MODE_OFF = "none";
    private static final String MTK_CONTROL_EFFECT_MODE_MONOCHROMATIC_LIGHT = "mono";
    private static final String MTK_CONTROL_EFFECT_MODE_NATURAL_LIGHT = "valencia";
    private static final String MTK_CONTROL_EFFECT_MODE_STUDIO_LIGHT = "hefe";
    private static final String MTK_CONTROL_EFFECT_MODE_CONTOUR_LIGHT = "xproll";
    private static final String MTK_CONTROL_EFFECT_MODE_STAGE_LIGHT = "lofi";
    private String[] portaritModes = {MTK_CONTROL_EFFECT_MODE_MONOCHROMATIC_LIGHT,MTK_CONTROL_EFFECT_MODE_NATURAL_LIGHT,MTK_CONTROL_EFFECT_MODE_STUDIO_LIGHT,
                                        MTK_CONTROL_EFFECT_MODE_CONTOUR_LIGHT,MTK_CONTROL_EFFECT_MODE_STAGE_LIGHT};
    private boolean mIsShowSetting = false;
    public PortraitView(Activity activity) {
        super(activity);
    }

    @Override
    protected View getView() {
        return mView;
    }

    @Override
    public void init(Activity activity, ICameraAppUi cameraAppUi, IModuleCtrl moduleCtrl) {
        super.init(activity, cameraAppUi, moduleCtrl);
        Log.i(TAG,"init");
        mContext = (CameraActivity)mActivity;
        mICameraAppUi = cameraAppUi;
        mIMoudleCtrl = moduleCtrl;
        setOrientation(mIMoudleCtrl.getOrientationCompensation());
        mView = inflate(R.layout.portarit_item);
        initUi(mView);

    }

    public void initUi(View view) {
        // TODO Auto-generated method stub
        compositeBeautiful = (LinearLayout)view.findViewById(R.id.portrait_mono);
        smoothingBeautiful = (LinearLayout)view.findViewById(R.id.portrait_natural);
        slimmingBeautiful = (LinearLayout)view.findViewById(R.id.portrait_studio_light);
        catchlightBeautiful = (LinearLayout)view.findViewById(R.id.portrait_contour_light);
        eyesEnlargementBeautiful = (LinearLayout)view.findViewById(R.id.portrait_stage_light);

        compositeBeautiful.setOnClickListener(this);
        smoothingBeautiful.setOnClickListener(this);
        slimmingBeautiful.setOnClickListener(this);
        catchlightBeautiful.setOnClickListener(this);
        eyesEnlargementBeautiful.setOnClickListener(this);
    }

    @Override
    public void show() {
        Log.i(TAG,"show");
        Log.i(TAG, "[show]...,mIsShowSetting = " + mIsShowSetting);
        if (!mIsShowSetting) {
	        super.show();
	        setOrientation(mIMoudleCtrl.getOrientationCompensation());
	        Log.i(TAG,"currentValue: "+currentValue+",currentIndex: "+currentIndex+",KEY_PORTRAIT_EFFECT_MODE: "+mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_EFFECT_MODE)
	                +",KEY_PORTRAIT_SELECTED_VALUE: "+mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_SELECTED_VALUE));
	        if (mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_SELECTED_VALUE) != null){
	            currentValue = mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_SELECTED_VALUE).getValue();
	        }
	        if (MTK_CONTROL_EFFECT_MODE_MONOCHROMATIC_LIGHT.equals(currentValue)) {
	            focuseView = compositeBeautiful;
	        } else if (MTK_CONTROL_EFFECT_MODE_NATURAL_LIGHT.equals(currentValue)) {
	            focuseView = smoothingBeautiful;
	        } else if (MTK_CONTROL_EFFECT_MODE_STUDIO_LIGHT.equals(currentValue)) {
	            focuseView = slimmingBeautiful;
	        } else if (MTK_CONTROL_EFFECT_MODE_CONTOUR_LIGHT.equals(currentValue)) {
	            focuseView = catchlightBeautiful;
	        } else if (MTK_CONTROL_EFFECT_MODE_STAGE_LIGHT.equals(currentValue)) {
	            focuseView = eyesEnlargementBeautiful;
	        }
	        setPortraitEffectMode(currentValue);
	        setUiHightlight(focuseView);
        }
    }

    @Override
    public void hide() {
        Log.i(TAG,"hide");
        closePortraitEffectView();
        super.hide();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG,"getId: "+v.getId());
        switch (v.getId()) {
            case R.id.portrait_mono:
                setPortraitEffectMode(MTK_CONTROL_EFFECT_MODE_MONOCHROMATIC_LIGHT);
                break;
            case R.id.portrait_natural:
                setPortraitEffectMode(MTK_CONTROL_EFFECT_MODE_NATURAL_LIGHT);
                break;
            case R.id.portrait_studio_light:
                setPortraitEffectMode(MTK_CONTROL_EFFECT_MODE_STUDIO_LIGHT);
                break;
            case R.id.portrait_contour_light:
                setPortraitEffectMode(MTK_CONTROL_EFFECT_MODE_CONTOUR_LIGHT);
                break;
            case R.id.portrait_stage_light:
                setPortraitEffectMode(MTK_CONTROL_EFFECT_MODE_STAGE_LIGHT);
                break;
        }
        setUiHightlight(v);
    }


    public void setUiHightlight(View v) {
        if(v != null){
            clearSeletor();
            setSeletor(((ViewGroup)v));
        }
    }

    private void setSeletor(ViewGroup v) {
        v.getChildAt(0).setSelected(true);
        ((TextView)v.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.main_color));

    }

    public void clearSeletor(){
        compositeBeautiful.getChildAt(0).setSelected(false);
        smoothingBeautiful.getChildAt(0).setSelected(false);
        slimmingBeautiful.getChildAt(0).setSelected(false);
        catchlightBeautiful.getChildAt(0).setSelected(false);
        eyesEnlargementBeautiful.getChildAt(0).setSelected(false);
        ((TextView)compositeBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)smoothingBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)slimmingBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)catchlightBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
        ((TextView)eyesEnlargementBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
    }

    public void setPortraitEffectMode(String value) {
        Log.i(TAG,"setPortraitEffectMode,value: "+value);
        if ( mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_EFFECT_MODE) != null) {
            mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_PORTRAIT_SELECTED_VALUE,value);
            mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_SELECTED_VALUE).setValue(value);
            mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_PORTRAIT_EFFECT_MODE,value);
            mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_EFFECT_MODE).setValue(value);
            mContext.applyParametersToServer();
        }
    }

    public void closePortraitEffectView(){
        Log.d(TAG, "closePortraitEffectView,KEY_PORTRAIT_EFFECT_MODE: "+ mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_EFFECT_MODE));
        if ( mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_EFFECT_MODE) != null) {
            mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_PORTRAIT_EFFECT_MODE,MTK_CONTROL_EFFECT_MODE_OFF);
            mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_PORTRAIT_EFFECT_MODE).setValue(MTK_CONTROL_EFFECT_MODE_OFF);
            mContext.applyParametersToServer();
        }
    }

    public void hidePortraitEffectView() {
        Log.i(TAG,"hidePortraitEffectView");
        /*prize-modify-bugid；57001 reset view state on click home key -xiaoping-20180505-start*/
        mICameraAppUi.restoreViewState();
        /*prize-modify-bugid；57001 reset view state on click home key -xiaoping-20180505-end*/
        super.hide();
    }

    public void setIsShowSetting(boolean isShowSetting) {
    	mIsShowSetting = isShowSetting;
    }
    
    /*prize-modify-bugid:61679 portrait invalid function-xiaoping-20180611-start*/
    public void hidePortraitItem() {
    	Log.i(TAG,"hidePortraitItem");
    	super.hide();
    }
    /*prize-modify-bugid:61679 portrait invalid function-xiaoping-20180611-end*/  
}
