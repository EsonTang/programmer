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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.SettingUtils;
import com.android.camera.bridge.CameraAppUiImpl;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.mediatek.camera.setting.preference.ListPreference;
//import com.mediatek.common.featureoption.FeatureOption;

// A popup window that shows one camera setting. The title is the name of the
// setting (ex: white-balance). The entries are the supported values (ex:
// daylight, incandescent, etc).
public class SettingSublistLayout extends RotateLayout implements AdapterView.OnItemClickListener {
    private static final String TAG = "SettingSublistLayout";

    private ListPreference mPreference;
    private Listener mListener;
    private MyAdapter mAdapter;
    private LayoutInflater mInflater;
    private ListView mSettingList;
    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
    private TextView mDialogTitle; 
    private TextView mCancelBtn; 
    private View mTopView; // Top of the View, simulation of the external dialog box, click Cancel
    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/ 
    private ListPreference mRadioPreference; // Preview Size
    private CameraActivity mCameraActivity;
    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/ 
    
    private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			dismiss();
		}
	};
	/*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
    
    public interface Listener {
        void onSettingChanged(boolean changed);
        
        void onSettingChanged(ListPreference preference);
    }

    public SettingSublistLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCameraActivity = (CameraActivity) context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSettingList = (ListView) findViewById(R.id.settingList);
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
        mDialogTitle = (TextView) findViewById(R.id.tv_title);
        mCancelBtn = (TextView) findViewById(R.id.btn_cancel);
        mTopView = findViewById(R.id.top);
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
       
    }

    private class MyAdapter extends BaseAdapter {
        private int mSelectedIndex;
        
        private int totalNumber;
        public MyAdapter() {
        	totalNumber = mPreference.getEntries().length;
        }

        @Override
        public int getCount() {
            return totalNumber;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.setting_sublist_item_prize, null);
                holder = new ViewHolder();
                holder.mTextView = (TextView) convertView.findViewById(R.id.title);
                holder.mDivider = convertView.findViewById(R.id.divider);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int iconId = mPreference.getIconId(position);
            holder.mTextView.setText(mPreference.getEntries()[position]);
            /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
            // Set the text color, remove radio buttons, images
            if (getSelectedIndex() == position) {
            	holder.mTextView.setSelected(true);
            } else {
            	holder.mTextView.setSelected(false);
            }
            if (position == (totalNumber-1)) {
            	holder.mDivider.setVisibility(View.INVISIBLE);
			} else {
				holder.mDivider.setVisibility(View.VISIBLE);
			}
            /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
            // SettingUtils.setEnabledState(convertView,
            // mPreference.isEnabled(position));
            return convertView;
        }

        public void setSelectedIndex(int index) {
            mSelectedIndex = index;
        }

        public int getSelectedIndex() {
            return mSelectedIndex;
        }
    }

    private class ViewHolder {
        TextView mTextView;
        View mDivider;
    }

    public void initialize(ListPreference preference) {
        mPreference = preference;
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/ 
        if (mPreference.getKey().equals(SettingConstants.KEY_PICTURE_SIZE)) {
        	String key = SettingConstants.getSettingKey(SettingConstants.ROW_SETTING_PICTURE_RATIO); 
        	mRadioPreference = mCameraActivity.getISettingCtrl().getListPreference(key);
        }
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/ 
        mAdapter = new MyAdapter();
        mSettingList.setAdapter(mAdapter);
        mSettingList.setOnItemClickListener(this);
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
        mCancelBtn.setOnClickListener(mClickListener);
        mTopView.setOnClickListener(mClickListener);
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
        reloadPreference();
    }
    
    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
    /**
     * 
     * Cancel dialog
     */
    private void dismiss() {
    	if (mListener != null) {
            mListener.onSettingChanged(false);
        }
    }
    /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
    
    // The value of the preference may have changed. Update the UI.
    public void reloadPreference() {
        String value = mPreference.getOverrideValue();
        if (value == null) {
            mPreference.reloadValue();
            value = mPreference.getValue();
        }
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
        mDialogTitle.setText(mPreference.getTitle());
        /*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
        int index = mPreference.findIndexOfValue(value);
        if (index != -1) {
            mAdapter.setSelectedIndex(index);
			/*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/
            mSettingList.setSelection(index);
			/*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/
        } else {
            Log.e(TAG, "Invalid preference value.");
            mPreference.print();
        }
        Log.i(TAG, "reloadPreference() mPreference=" + mPreference + ", index=" + index);
    }

    public void setSettingChangedListener(Listener listener) {
        mListener = listener; // should be rechecked
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
        Log.d(TAG,
                "onItemClick(" + index + ", " + id + ") and oldIndex = "
                        + mAdapter.getSelectedIndex());
        boolean realChanged = index != mAdapter.getSelectedIndex();
        if (realChanged) {
        	/*PRIZE-modify setting show-wanzhijuan-2016-04-26-start*/ 
        	if (mRadioPreference != null) {
            	String size = (String) mPreference.getEntryValues()[index];
            	boolean is43 = SettingDataBase.is4Divide3(size);
            	/*prize-add-18:9 full screen-xiaoping-20180423-start*/
                String selectRadio = SettingDataBase.getPictureRatio(size);
            	String radio = mCameraActivity.getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO);
            	Log.d(TAG,"radio: "+radio+",selectRadio: "+selectRadio+",size: "+size);
            	boolean oldis43 = radio.equals(SettingDataBase.PICTURE_RATIO_4_3);
            	if (/*is43 != oldis43*/selectRadio != radio) {
//            		mRadioPreference.setValue(is43 ? SettingDataBase.PICTURE_RATIO_4_3 : SettingDataBase.PICTURE_RATIO_16_9);
            	/*prize-add-18:9 full screen-xiaoping-20180423-end*/
            		mRadioPreference.setValue(selectRadio);
            		if (mListener != null) {
                        mListener.onSettingChanged(mRadioPreference);
                    }
            	}
            }
        	/*PRIZE-modify setting show-wanzhijuan-2016-04-26-end*/ 
            mPreference.setValueIndex(index);

        }
        if (mListener != null) {
            mListener.onSettingChanged(realChanged);
        }
    }
}
