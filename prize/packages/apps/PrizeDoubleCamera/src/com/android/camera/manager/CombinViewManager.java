package com.android.camera.manager;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.adapter.CombinAdapter;
import com.android.camera.ui.RotateImageView;
import com.mediatek.camera.setting.SettingConstants;
import com.prize.setting.NavigationBarUtils;


/*prize-xuchunming-20171123-arcsoft switch-start*/
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.android.camera.FeatureSwitcher;
/*prize-xuchunming-20171123-arcsoft switch-end*/
public class CombinViewManager extends ViewManager implements OnClickListener,
		OnItemClickListener, CameraActivity.OnParametersReadyListener, CameraActivity.OnPreferenceReadyListener {

	private static final String TAG = "CombinViewManager";

	private RotateImageView mCombinPicker;
	private View mCombinPickerContain;

	protected ViewGroup mCombinLayout;

	private RotateImageView mCombinBack;
	
	private GridView mGrideView;
	
	private CombinAdapter mCombinAdapter;
	private CombinListener mCombinListener;
	
	public static final int COMBIN_NORMAL = 0;
	public static final int COMBIN_EFFECT = 1;
	public static final int COMBIN_WATERMARK = 2;
	public static final int COMBIN_GIF = 5;
	public static final int COMBIN_GYBOKEH = 3;
	public static final int COMBIN_BACK = 3;
	private static final int COMBIN_NUM_ALL = 4;
	
	private static final int COMBIN_MESSAGE_HIDE = 100;
	
	private Animation mFadeIn;
    private Animation mFadeOut;
    private int mCurrentMode = COMBIN_NORMAL;
    private boolean mPreferenceReady;
	private boolean mIsAsd;
	/*prize-xuchunming-20171123-arcsoft switch-start*/
	private int screenWidth;
	/*prize-xuchunming-20171123-arcsoft switch-end*/
	private int[] mNormalIcons;
	private int[] mHightLightIcons;
	private int[] mTitles;
	private int mCurrentCameraId;
    private Handler  mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch(msg.what){
    		case COMBIN_MESSAGE_HIDE :
    			startFadeOutAnimation(mCombinLayout);
    			break;
    		}
    	};
    };
    
	private static final int[] MODE_ICONS_HIGHTLIGHT = new int[COMBIN_NUM_ALL];
	static {
		MODE_ICONS_HIGHTLIGHT[COMBIN_NORMAL] = R.drawable.prize_combin_nomral_select;
		MODE_ICONS_HIGHTLIGHT[COMBIN_EFFECT] = R.drawable.prize_combin_effect_select;
		MODE_ICONS_HIGHTLIGHT[COMBIN_WATERMARK] = R.drawable.prize_combin_watermark_select;
		MODE_ICONS_HIGHTLIGHT[COMBIN_GYBOKEH] = R.drawable.prize_combin_gybokeh_select;
		
	};
	private static final int[] MODE_ICONS_NORMAL = new int[COMBIN_NUM_ALL];
	static {
		MODE_ICONS_NORMAL[COMBIN_NORMAL] = R.drawable.prize_combin_nomral;
		MODE_ICONS_NORMAL[COMBIN_EFFECT] = R.drawable.prize_combin_effect;
		MODE_ICONS_NORMAL[COMBIN_WATERMARK] = R.drawable.prize_combin_watermark;
		MODE_ICONS_NORMAL[COMBIN_GYBOKEH] = R.drawable.prize_combin_gybokeh;
		
	};

	private static final int[] MODE_TITLE = new int[COMBIN_NUM_ALL];
	static {
		MODE_TITLE[COMBIN_NORMAL] = R.string.prize_accessibility_switch_to_normal;
		MODE_TITLE[COMBIN_EFFECT] = R.string.prize_accessibility_switch_to_effect;
		MODE_TITLE[COMBIN_WATERMARK] = R.string.prize_accessibility_switch_to_watermark;
		MODE_TITLE[COMBIN_GYBOKEH] = R.string.prize_setting_gybokeh;
	};

	/*prize-add-bugid:42825 should not have SLR on front camera-xiaoping-20171120-start*/
	private static final int[] MODE_ICONS_HIGHTLIGHT_FRONT = new int[COMBIN_NUM_ALL];
	static {
		MODE_ICONS_HIGHTLIGHT_FRONT[COMBIN_NORMAL] = R.drawable.prize_combin_nomral_select;
		MODE_ICONS_HIGHTLIGHT_FRONT[COMBIN_EFFECT] = R.drawable.prize_combin_effect_select;
		MODE_ICONS_HIGHTLIGHT_FRONT[COMBIN_WATERMARK] = R.drawable.prize_combin_watermark_select;
		MODE_ICONS_HIGHTLIGHT_FRONT[COMBIN_BACK] = R.drawable.prize_combin_back_select;

	};

	private static final int[] MODE_ICONS_NORMAL_FRONT = new int[COMBIN_NUM_ALL];
	static {
		MODE_ICONS_NORMAL_FRONT[COMBIN_NORMAL] = R.drawable.prize_combin_nomral;
		MODE_ICONS_NORMAL_FRONT[COMBIN_EFFECT] = R.drawable.prize_combin_effect;
		MODE_ICONS_NORMAL_FRONT[COMBIN_WATERMARK] = R.drawable.prize_combin_watermark;
		MODE_ICONS_NORMAL_FRONT[COMBIN_BACK] = R.drawable.prize_combin_back;

	};
	
	private static final int[] MODE_TITLE_FRONT = new int[COMBIN_NUM_ALL];
	static {
		MODE_TITLE_FRONT[COMBIN_NORMAL] = R.string.prize_accessibility_switch_to_normal;
		MODE_TITLE_FRONT[COMBIN_EFFECT] = R.string.prize_accessibility_switch_to_effect;
		MODE_TITLE_FRONT[COMBIN_WATERMARK] = R.string.prize_accessibility_switch_to_watermark;
		MODE_TITLE_FRONT[COMBIN_BACK] = R.string.prize_setting_back;
	};
	/*prize-add-bugid:42825 should not have SLR on front camera-xiaoping-20171120-end*/

	public interface CombinListener {
        public boolean onCombinModeChange(int lastMode, int newMode);
    }

	public CombinViewManager(CameraActivity context, CombinListener combinListener) {
		super(context, VIEW_LAYER_SETTING);
		mCombinListener = combinListener;
		context.addOnPreferenceReadyListener(this);
		context.addOnParametersReadyListener(this);
		/*prize-xuchunming-20171123-arcsoft switch-start*/
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(metric);
		screenWidth = metric.widthPixels;
		/*prize-xuchunming-20171123-arcsoft switch-end*/
	}

	@Override
	protected View getView() {
		// TODO Auto-generated method stub
		View view = inflate(R.layout.combin_view);
		mCombinPicker = (RotateImageView) view.findViewById(R.id.combin_picker);
		mCombinPicker.setOnClickListener(this);
		mCombinPickerContain = view.findViewById(R.id.combin_picker_contain);
        //NavigationBarUtils.adpaterNavigationBar(getContext(), mCombinPickerContain);
		return view;
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		if((getContext().getCurrentMode() == ModePicker.MODE_PHOTO 
				|| getContext().getCurrentMode() == ModePicker.MODE_WATERMARK || getContext().getCurrentMode() == ModePicker.MODE_BOKEH) &&
				getContext().isNonePickIntent() == true && 
				getContext().getCameraAppUI().isFlashMenuShowing() == false){
			super.show();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.combin_picker:
			showCombinView();
			break;
		case R.id.combin_back:
			startFadeOutAnimation(mCombinLayout);
			break;
		}
		
	}

	public void showCombinView() {
		if (getContext().getCameraAppUI().isNormalViewState() == true) {
			initCombin();
			if (mCombinLayout.getParent() == null) {
				getContext().addView(mCombinLayout, VIEW_LAYER_OVERLAY);
			}
			startFadeInAnimation(mCombinLayout);
		}
	}

	public void hideCombinView() {
		if (mCombinLayout != null && mCombinLayout.getParent() != null) {
			getContext().removeView(mCombinLayout, VIEW_LAYER_OVERLAY);
		}
		startFadeOutAnimation(mCombinLayout);
	}

	public void initCombin() {
		if (mCombinLayout == null || mCurrentCameraId != getContext().getCameraId()) {
			mCombinLayout = (ViewGroup) getContext().inflate(
					R.layout.combin_contain, VIEW_LAYER_OVERLAY);
			mCombinBack = (RotateImageView) mCombinLayout
					.findViewById(R.id.combin_back);
			mGrideView = (GridView) mCombinLayout
					.findViewById(R.id.combin_gridView);
			switchCamera(getContext().getCameraId());
			mCombinAdapter = new CombinAdapter(getContext(),mNormalIcons , mHightLightIcons, mTitles);
			mCombinAdapter.setCurrentSelet(mCurrentMode);
			mGrideView.setAdapter(mCombinAdapter);
			mGrideView.setOnItemClickListener(this);
			mCombinBack.setOnClickListener(this);
		} else {
			mCombinAdapter.setCurrentSelet(mCurrentMode);
			mCombinAdapter.notifyDataSetChanged();
		}
	}

	/*prize-add-bugid:42825 should not have SLR on front camera-xiaoping-20171120-start*/
	public void switchCamera(int cameraid) {
		if(cameraid == 0){
			mTitles = MODE_TITLE;
			mNormalIcons = MODE_ICONS_NORMAL;
			mHightLightIcons = MODE_ICONS_HIGHTLIGHT;
		} else {
			mTitles = MODE_TITLE_FRONT;
			mNormalIcons = MODE_ICONS_NORMAL_FRONT;
			mHightLightIcons = MODE_ICONS_HIGHTLIGHT_FRONT;
		}
		mCurrentCameraId = cameraid;
	}
	/*prize-add-bugid:42825 should not have SLR on front camera-xiaoping-20171120-end*/

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mIsAsd && position == COMBIN_EFFECT) {
			return;
		}
		setCurrentMode(position);
		mHandler.removeMessages(COMBIN_MESSAGE_HIDE);
		mHandler.sendEmptyMessageDelayed(COMBIN_MESSAGE_HIDE, 50);
	}

	public void setCurrentMode(int position) {
		if (getContext().getCameraId() == 1 && position == COMBIN_BACK) {
			startFadeOutAnimation(mCombinLayout);
			return;
		}
    	if(mCombinAdapter != null){
    		mCombinAdapter.setCurrentSelet(position);
    		mCombinAdapter.notifyDataSetChanged();
    	}
    	if (mCurrentMode != position) {
    		int oleMode = mCurrentMode;
    		mCurrentMode = position;
			if (mCombinListener != null) {
				mCombinListener.onCombinModeChange(oleMode, position);
			}
		}
    }

	/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
    public void setSelectMode(int position) {
		if(mCombinAdapter != null){
			mCombinAdapter.setCurrentSelet(position);
			mCurrentMode = position;
		}
	}
	/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-end*/

	protected void startFadeInAnimation(View view) {
		view.setVisibility(View.VISIBLE);
        if (mFadeIn == null) {
            mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.combin_popup_grow_fade_in);
        }
        if (view != null && mFadeIn != null) {
            view.startAnimation(mFadeIn);
        }
    }
	
	protected void startFadeOutAnimation(View view) {
        if (mFadeOut == null) {
            mFadeOut = AnimationUtils.loadAnimation(getContext(),
                    R.anim.combin_popup_shrink_fade_out);
        }
        if (view != null && mFadeOut != null) {
           // view.startAnimation(mFadeOut);
        }
        view.setVisibility(View.GONE);
    }

	private void hideMode() {
		if (mCombinListener != null) {
			if (mCurrentMode == COMBIN_WATERMARK || mCurrentMode == COMBIN_GYBOKEH) {
				mCombinListener.onCombinModeChange(COMBIN_NORMAL, COMBIN_NORMAL);
			} else {
				mCombinListener.onCombinModeChange(mCurrentMode, COMBIN_NORMAL);
			}
		}
	}

	private void showMode() {
		if (mCombinListener != null) {
			mCombinListener.onCombinModeChange(COMBIN_NORMAL, mCurrentMode);
		}
	}

	public void onModePickerChange(int newMode, int oldMode) {
		if (oldMode == ModePicker.MODE_PHOTO) {
    		hideMode();
    	} else if (newMode == ModePicker.MODE_PHOTO && oldMode != ModePicker.MODE_WATERMARK && oldMode != ModePicker.MODE_BOKEH) {
    		showMode();
    	}
	}
	
	public void onShowMode() {
		if (getContext().getCurrentMode() == ModePicker.MODE_PHOTO) {
			showMode();
		}
	}
	
	@Override
	public boolean collapse(boolean force) {
		if (mCombinLayout != null && mCombinLayout.getVisibility() == View.VISIBLE) {
			startFadeOutAnimation(mCombinLayout);
			return true;
		}
        return false;
    }

	public void closeCombinView(int mode) {
		mCurrentMode = COMBIN_NORMAL;
		if (mCombinListener != null) {
			mCombinListener.onCombinModeChange(mode, COMBIN_NORMAL);
		}
	}

	public boolean canShowMode() {
		if (mCurrentMode == COMBIN_NORMAL || mCurrentMode == COMBIN_GIF) {
			return true;
		}
		return false;
	}

	public void reStore(){
		/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
		if (mCurrentMode == COMBIN_GYBOKEH) {
			setSelectMode(COMBIN_NORMAL);
		/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-end*/
		} else {
			setCurrentMode(COMBIN_NORMAL);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		if(mCombinPicker != null){
			mCombinPicker.setEnabled(enabled);
		}
		
	}
	 
	public void onChangeNavigationBar(boolean isShow){
	    if(isShow == true){
	    	NavigationBarUtils.adpaterNavigationBar(getContext(), mCombinPickerContain);
	    }else{
	    	NavigationBarUtils.adpaterNavigationBar(getContext(), mCombinPickerContain,0);
	    }
	}

	@Override
	public void onCameraParameterReady() {
		// TODO Auto-generated method stub
		 if (!mPreferenceReady) {
	            return;
	     }
		 refresh();
	}

	@Override
	public void onPreferenceReady() {
		// TODO Auto-generated method stub
		mPreferenceReady = true;
	}
	
	 @Override
	public void onRefresh() {
		Log.d(TAG, "onRefresh(), mPreferenceReady:" + mPreferenceReady);
		onChangeNavigationBar(NavigationBarUtils.isShowNavigationBar(getContext()));
		if (!mPreferenceReady) {
			return;
		}
		if (getContext().getISettingCtrl().getListPreference(SettingConstants.KEY_CONBIN_MANAGER) != null
				&& getContext().getISettingCtrl().getListPreference(SettingConstants.KEY_CONBIN_MANAGER).isEnabled() == true) {
			mCombinPicker.setVisibility(View.VISIBLE);
		} else {
			mCombinPicker.setVisibility(View.GONE);
		}

		refreshView();
		/* prize-xuchunming-20171123-arcsoft switch-start */
		adpaterSelfie();
		/* prize-xuchunming-20171123-arcsoft switch-end */
	  
	}
	public synchronized void refreshView() {
    	String value = getContext().getISettingCtrl().getSettingValue(SettingConstants.KEY_ASD);
    	mIsAsd = "on".equals(value) ? true : false;
    	if (mIsAsd && mCurrentMode == COMBIN_EFFECT) {
    		closeCombinView(COMBIN_EFFECT);
    	}
    }
	/*prize-xuchunming-20171123-arcsoft switch-start*/
	private void adpaterSelfie() {
		if(FeatureSwitcher.isArcsoftSelfieSupported() == false) {
			if(mCombinPicker != null && mCombinPicker.getVisibility() == View.VISIBLE) {
				RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCombinPicker.getLayoutParams();
				if(getContext().getCameraId() == 0){
					lp.setMarginEnd((int)getContext().getResources().getDimension(R.dimen.combin_picker_marginend));
				}else if(getContext().getCameraId() == 1){
					int marginEnd = screenWidth/2 - (int)getContext().getResources().getDimension(R.dimen.combin_width)/2;
					Log.d(TAG, "marginend:"+marginEnd);
					lp.setMarginEnd(marginEnd);
				}
				mCombinPicker.setLayoutParams(lp);
			}
		}
	}
	/*prize-xuchunming-20171123-arcsoft switch-end*/
}
