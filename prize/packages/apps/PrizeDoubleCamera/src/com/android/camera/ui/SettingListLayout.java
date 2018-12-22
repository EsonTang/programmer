/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.camera.ActivityBase;
import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.SettingUtils;
import com.android.camera.manager.ModePicker;
import com.android.camera.ui.InLineSettingSwitchVirtual;

import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.preference.ListPreference;

import java.util.ArrayList;
import com.prize.setting.IViewType;

//import com.mediatek.common.featureoption.FeatureOption;

/* A popup window that contains several camera settings. */
/*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
public class SettingListLayout extends FrameLayout implements InLineSettingItem.Listener,
        OnScrollListener {
    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
    @SuppressWarnings("unused")
    private static final String TAG = "SettingListLayout";

    private Listener mListener;
    private ArrayList<IViewType> mListItem = new ArrayList<IViewType>();
    private ArrayAdapter<IViewType> mListItemAdapter;
    private InLineSettingItem mLastItem;
    private ListView mSettingList;

    public interface Listener {
        void onSettingChanged(SettingListLayout settingList, ListPreference preference);
        void onStereoCameraSettingChanged(SettingListLayout settingList, ListPreference preference,
                int index, boolean showing);
        void onRestorePreferencesClicked();
        void onVoiceCommandChanged(int index);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSettingList = (ListView) findViewById(R.id.settingList);
    }
    
    private class SettingsListAdapter extends ArrayAdapter<IViewType> {
        LayoutInflater mInflater;

        public SettingsListAdapter() {
            super(SettingListLayout.this.getContext(), 0, mListItem);
            mInflater = LayoutInflater.from(getContext());
        }
        
        private int getSettingLayoutId(IViewType viewType) {
            // If the preference is null, it will be the only item , i.e.
            // 'Restore setting' in the popup window.
        	if(viewType == null){
        		Log.d(TAG,"viewType = null" );
        		return R.layout.in_line_setting_sublist;
        	}
        	
            if (viewType.getViewType() == IViewType.VIEW_TYPE_TITLE) {
            	Log.d(TAG,"viewType = " + viewType);
            	return R.layout.in_line_setting_title_prize;
            } else {
            	// Currently, the RecordLocationPreference is the only setting
                // which applies the on/off switch.
            	ListPreference pref = (ListPreference) viewType;
                if (isSwitchSettingItem(pref)) {
                    Log.d(TAG,"viewType is  isSwitchSettingItem pref. " + pref.getTitle());
                    return R.layout.in_line_setting_switch;
                } else if (isVirtualSettingItem(pref)) {
                	Log.d(TAG,"viewType is  isVirtualSettingItem "  + pref.getTitle());
                    return R.layout.in_line_setting_virtual;
                }else if (isSwitchVirtualItem(pref)) {
                	Log.d(TAG,"viewType is isSwitchVirtualItem " + pref.getTitle());
                    return R.layout.in_line_setting_switch_virtual;
                }
                Log.d(TAG,"viewType is default else " + pref.getTitle());
                return R.layout.in_line_setting_sublist;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IViewType viewType = mListItem.get(position);
            Log.d(TAG,"mListItem.size() = " + mListItem.size());
            int viewLayoutId = getSettingLayoutId(viewType);
            if (viewType != null && viewType.getViewType() == IViewType.VIEW_TYPE_CONTENT) {
            	ListPreference pref = (ListPreference) viewType;
                if (convertView != null) {
                    if (pref == null) {
                        if (!(convertView instanceof InLineSettingRestore)) {
                            convertView = null;
                        }
                    } else if (isSwitchSettingItem(pref)) {
                        if (!(convertView instanceof InLineSettingSwitch)) {
                            convertView = null;
                        }
                    } else if (isVirtualSettingItem(pref)) {
                        if (!(convertView instanceof InLineSettingVirtual)) {
                            convertView = null;
                        }
                    } else if (isSwitchVirtualItem(pref)) {
                        if (!(convertView instanceof InLineSettingSwitchVirtual)) {
                            convertView = null;
                        }
                    } else {
                        if (!(convertView instanceof InLineSettingSublist)) {
                            convertView = null;
                        }
                    }
                    if (convertView != null) {
                        ((InLineSettingItem) convertView).initialize(pref);
                        SettingUtils.setEnabledState(convertView,
                                (pref == null ? true : pref.isEnabled()));
                        return convertView;
                    }
                }
                
                InLineSettingItem view = (InLineSettingItem) mInflater.inflate(viewLayoutId, parent,
                        false);
                view.initialize(pref); // no init for restore one
                view.setSettingChangedListener(SettingListLayout.this);
                SettingUtils.setEnabledState(convertView, (pref == null ? true : pref.isEnabled()));
                return view;
        	} else {
            	if (convertView != null && convertView instanceof InLineSettingTitle) {
            		((InLineSettingItem) convertView).setTitleText(viewType);
            		return convertView;
            	}
            	InLineSettingItem viewLayout = (InLineSettingItem) mInflater.inflate(viewLayoutId, parent,
                        false);
            	viewLayout.setTitleText(viewType);
        		return viewLayout;
            }
        } 
    }

    private boolean isSwitchSettingItem(ListPreference pref) {
        		/*prize-xuchunming-20170420-add frontcamera mirror switch-end*/
        return SettingConstants.KEY_RECORD_LOCATION.equals(pref.getKey())
                || SettingConstants.KEY_VIDEO_RECORD_AUDIO.equals(pref.getKey())
                || SettingConstants.KEY_VIDEO_EIS.equals(pref.getKey())
                || SettingConstants.KEY_VIDEO_3DNR.equals(pref.getKey())
                || SettingConstants.KEY_CAMERA_ZSD.equals(pref.getKey())
                || SettingConstants.KEY_VOICE.equals(pref.getKey())
                || SettingConstants.KEY_CAMERA_FACE_DETECT.equals(pref.getKey())
                || SettingConstants.KEY_HDR.equals(pref.getKey())
                || SettingConstants.KEY_GESTURE_SHOT.equals(pref.getKey())
                || SettingConstants.KEY_SMILE_SHOT.equals(pref.getKey())
                || SettingConstants.KEY_SLOW_MOTION.equals(pref.getKey())
                || SettingConstants.KEY_CAMERA_AIS.equals(pref.getKey())
                || SettingConstants.KEY_ASD.equals(pref.getKey())
                || SettingConstants.KEY_DNG.equals(pref.getKey())
                || SettingConstants.KEY_CAMERA_MUTE.equals(pref.getKey())
                || SettingConstants.KEY_GRIDE.equals(pref.getKey())
                || SettingConstants.KEY_TOUCHSHUTTER.equals(pref.getKey())
                /*prize-xuchunming-20170420-add frontcamera mirror switch-start*/
                || SettingConstants.KEY_PRIZE_FLIP.equals(pref.getKey())
                /*add koobee watermark*/
                || SettingConstants.KEY_KOOBEE_WATERMARK.equals(pref.getKey());
    }

    private boolean isVirtualSettingItem(ListPreference pref) {
        return SettingConstants.KEY_IMAGE_PROPERTIES.equals(pref.getKey())
                || SettingConstants.KEY_FACE_BEAUTY_PROPERTIES.equals(pref.getKey());
    }

    private boolean isSwitchVirtualItem(ListPreference pref) {
        return SettingConstants.KEY_DUAL_CAMERA_MODE.equals(pref.getKey());
    }

    public void setSettingChangedListener(Listener listener) {
        mListener = listener;
    }

    public SettingListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void initialize(ArrayList<IViewType> listItems) {
        mListItem = listItems;

        mListItemAdapter = new SettingsListAdapter();
        mSettingList.setAdapter(mListItemAdapter);
		/*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
        //mSettingList.setOnItemClickListener(this);
		/*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
        mSettingList.setSelector(android.R.color.transparent);
        mSettingList.setOnScrollListener(this);
        if(getContext() instanceof CameraActivity && (((CameraActivity)getContext()).getCurrentMode() == ModePicker.MODE_PREVIDEO || ((CameraActivity)getContext()).getCurrentMode() == ModePicker.MODE_SHORT_PREVIDEO)){
        	for(int i = 0; i < listItems.size(); i++){
        		if(listItems.get(i).getTitle().equals(getContext().getResources().getString(R.string.pref_gesture_video))){
        			mSettingList.setSelectionFromTop(i, 0);
        		}
        	}
        }
        
    }

    @Override
    public void onSettingChanged(InLineSettingItem item, ListPreference preference) {
        if (mLastItem != null && mLastItem != item) {
            mLastItem.collapseChild();
        }
        /*prize-xucm-20160125-bugid:11573-start*/
        if (mListener != null && getContext() instanceof CameraActivity && !((CameraActivity)getContext()).isActivityOnpause()) {
        /*prize-xucm-20160125-bugid:11573-end*/ 
        	preference.setOverrideValue(null, false);
            mListener.onSettingChanged(this, preference);
        }
    }

    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
    @Override
	public void onSettingChanged(ListPreference preference) {
		
    	if (mListener != null) {
            preference.setOverrideValue(null, false);
            mListener.onSettingChanged(this, preference);
        }
    }
	/*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/

    public void reloadPreference() {
        int count = mSettingList.getChildCount();
        for (int i = 0; i < count; i++) {
            IViewType pref = mListItem.get(i);
            if (pref != null) {
                InLineSettingItem settingItem = (InLineSettingItem) mSettingList.getChildAt(i);
                settingItem.reloadPreference();
            }
        }
    }

    @Override
    public void onDismiss(InLineSettingItem item) {
        Log.v(TAG, "onDismiss(" + item + ") mLastItem=" + mLastItem);
        mLastItem = null;
    }

    @Override
    public boolean onShow(InLineSettingItem item) {
        Log.d(TAG, "onShow(" + item + ") mLastItem=" + mLastItem);
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
        boolean isShow = false;
        if (mLastItem != null/* && mLastItem != item*/) {
            mLastItem.collapseChild();
            isShow = true;
            mLastItem = null;
        } else if (!(item instanceof InLineSettingSwitch)){
        	mLastItem = item;
        }
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
        return isShow;
    }

    @Override
    public void onVoiceCommandChanged(int index) {
        if (mListener != null) {
            mListener.onVoiceCommandChanged(index);
        }
    }

    @Override
    public void onStereoCameraSettingChanged(InLineSettingItem item, ListPreference preference,
            int index, boolean showing) {
        if (mLastItem != null && mLastItem != item) {
            mLastItem.collapseChild();
        }
        if (mListener != null) {
            mListener.onStereoCameraSettingChanged(this, preference, index, showing);
        }
    }
    public boolean collapseChild() {
        boolean collapse = false;
        if (mLastItem != null) {
            collapse = mLastItem.collapseChild();
            /*prize-xuchunming-20160222-deal monkey error-start*/
            mListItemAdapter = null;
            /*prize-xuchunming-20160222-deal monkey error-end*/
        }
        Log.d(TAG, "collapseChild() return " + collapse);
        return collapse;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        Log.d(TAG, "onScroll(" + firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount
                + ")");
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG, "onScrollStateChanged(" + scrollState + ")");
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            collapseChild();
        }
    }

	public void onChangeNavigationBar(boolean isShow) {
		if (mLastItem != null) {
            mLastItem.onChangeNavigationBar(isShow);
        }
	}
}
