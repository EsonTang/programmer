package com.mediatek.contacts.aas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.R;

import com.mediatek.contacts.aassne.SimAasSneUtils;
import com.mediatek.contacts.simcontact.SlotUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.internal.telephony.uicc.AlphaTag;

import java.util.ArrayList;
import java.util.List;

public class AasTagInfoAdapter extends BaseAdapter {
    private final static String TAG = "CustomAasAdapter";
    public final static int MODE_NORMAL = 0;
    public final static int MODE_EDIT = 1;
    private int mMode = MODE_NORMAL;

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    //private int mSlotId = -1;
    private int mSubId = -1;
    private ToastHelper mToastHelper = null;
    private static AasTagInfoAdapter sInstance = null;

    private ArrayList<TagItemInfo> mTagItemInfos = new ArrayList<TagItemInfo>();

    public AasTagInfoAdapter(Context context, int subId) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSubId = subId;
        mToastHelper = new ToastHelper(context);
    }

    public void updateAlphaTags() {
        mTagItemInfos.clear();
        SimAasSneUtils.refreshAASList(mSubId);
        List<AlphaTag> list = SimAasSneUtils.getAAS(mSubId);
        for (AlphaTag tag : list) {
            TagItemInfo tagItemInfo = new TagItemInfo(tag);
            mTagItemInfos.add(tagItemInfo);
            Log.d(TAG, "[updateAlphaTags] getPbrIndex: " + tag.getPbrIndex() +
                    ",getRecordIndex: " + tag.getRecordIndex() + ",getAlphaTag: "
                    + tag.getAlphaTag());
        }
        notifyDataSetChanged();
        /*prize-add-fix bug[52008]-hpf-2018-3-5-start*/
        if(mOnDataChangeListener != null)
        mOnDataChangeListener.onChange(mTagItemInfos.size());
        /*prize-add-fix bug[52008]-hpf-2018-3-5-end*/
    }

    public void setMode(int mode) {
        Log.d(TAG, "[setMode] mode: " + mode);
        if (mMode != mode) {
            mMode = mode;
            if (isMode(MODE_NORMAL)) {
                for (TagItemInfo tagInfo : mTagItemInfos) {
                    tagInfo.mChecked = false;
                }
            }
            notifyDataSetChanged();
        }
    }

    public boolean isMode(int mode) {
        return mMode == mode;
    }

    @Override
    public int getCount() {
        return mTagItemInfos.size();
    }

    @Override
    public TagItemInfo getItem(int position) {
        return mTagItemInfos.get(position);
    }

    public void setChecked(int position, boolean checked) {
        TagItemInfo tagInfo = getItem(position);
        tagInfo.mChecked = checked;
        notifyDataSetChanged();
    }

    public void updateChecked(int position) {
        TagItemInfo tagInfo = getItem(position);
        tagInfo.mChecked = !tagInfo.mChecked;
        notifyDataSetChanged();
    }

    public void setAllChecked(boolean checked) {
        Log.d(TAG, "[setAllChecked] checked: " + checked);
        for (TagItemInfo tagInfo : mTagItemInfos) {
            tagInfo.mChecked = checked;
        }
        notifyDataSetChanged();
    }

    public void deleteCheckedAasTag() {
        for (TagItemInfo tagInfo : mTagItemInfos) {
            if (tagInfo.mChecked) {
                boolean success = SimAasSneUtils.removeUSIMAASById(mSubId,
                        tagInfo.mAlphaTag.getRecordIndex(), tagInfo.mAlphaTag.getPbrIndex());
                if (!success) {
                    String msg = mContext.getResources().getString(R.string.aas_delete_fail,
                            tagInfo.mAlphaTag.getAlphaTag());
                    mToastHelper.showToast(msg);
                    Log.d(TAG, "[deleteCheckedAasTag] delete failed:" +
                            tagInfo.mAlphaTag.getAlphaTag());
                }
            }
        }
        updateAlphaTags();
    }

    public int getCheckedItemCount() {
        int count = 0;
        if (isMode(MODE_EDIT)) {
            for (TagItemInfo tagInfo : mTagItemInfos) {
                if (tagInfo.mChecked) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /*prize-add for dido os8.0-hpf-2017-8-23-start*/
    public boolean isAllChecked() {
    	boolean allChecked = false;
    	int count = 0;
        if (isMode(MODE_EDIT)) {
            for (TagItemInfo tagInfo : mTagItemInfos) {
                if (tagInfo.mChecked) {
                    count++;
                }
            }
            if(count == getCount()){
            	allChecked = true;
            }else{
            	allChecked = false;
            }
        }
        return allChecked;
    }
    
    public boolean isHaveChecked() {
    	boolean haveChecked = false;
    	int count = 0;
        if (isMode(MODE_EDIT)) {
            for (TagItemInfo tagInfo : mTagItemInfos) {
                if (tagInfo.mChecked) {
                    count++;
                }
            }
            if(count == 0){
            	haveChecked = false;
            }else{
            	haveChecked = true;
            }
        }
        return haveChecked;
    }
    
    /*prize-add for dido os8.0-hpf-2017-8-23-end*/

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Boolean isExist(String text) {
        for (int i = 0; i < mTagItemInfos.size(); i++) {
            if (mTagItemInfos.get(i).mAlphaTag.getAlphaTag().equals(text)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFull() {
        final int maxCount = SlotUtils.getUsimAasCount(mSubId);
        Log.d(TAG, "[isFull] getCount: " + getCount() + ",maxCount=" + maxCount +
                ",sub: " + mSubId);
        return getCount() >= maxCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.custom_aas_item, null);
            TextView tagView = (TextView) convertView.findViewById(R.id.aas_item_tag);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.aas_edit);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.aas_item_check);
            viewHolder = new ViewHolder(tagView, imageView, checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        TagItemInfo tag = getItem(position);
        viewHolder.mTagView.setText(tag.mAlphaTag.getAlphaTag());

        if (isMode(MODE_NORMAL)) {
            viewHolder.mEditView.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setVisibility(View.GONE);
            // viewHolder.mEditView.setOnClickListener()
        } else {
            viewHolder.mEditView.setVisibility(View.GONE);
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setChecked(tag.mChecked);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView mTagView;
        ImageView mEditView;
        CheckBox mCheckBox;

        public ViewHolder(TextView textView, ImageView imageView, CheckBox checkBox) {
            mTagView = textView;
            mEditView = imageView;
            mCheckBox = checkBox;
        }
    }

    public static class TagItemInfo {
        AlphaTag mAlphaTag = null;
        boolean mChecked = false;

        public TagItemInfo(AlphaTag tag) {
            mAlphaTag = tag;
        }
    }
    
    /*prize-add-fix bug[52008]-hpf-2018-3-5-start*/
    private OnDataChangeListener mOnDataChangeListener;
    public interface OnDataChangeListener{
    	void onChange(int size);
    }
    
    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
    	this.mOnDataChangeListener = onDataChangeListener;
    }
    /*prize-add-fix bug[52008]-hpf-2018-3-5-end*/
}
