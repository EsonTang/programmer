/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.appcenter.R;

import java.util.HashMap;

public class popListviewAdapter extends BaseAdapter {

    private String[] groupList;
    Context mContext;
    private HashMap<String, Boolean> statusHashMap;
    private int     currentGroupId = -1;
    private int     selectGroupId  = -1;
    private int     currentChildId = -1;
    private int[][] selectId       = {{}, {}};

    public void setCurrentChildId(int currentChildId) {
        this.currentChildId = currentChildId;
    }

    public void setSelectGroupId(int selectGroupId) {
        this.selectGroupId = selectGroupId;
    }


    public void putValue2Map(String str, boolean paramBoolean) {
        statusHashMap.put(str, paramBoolean);
        notifyDataSetChanged();
    }

    public popListviewAdapter(String[] groupList, Context mContext) {
        super();
        this.groupList = groupList;
        this.mContext = mContext;
        //		statusHashMap = new HashMap<String, Boolean>();
        //		for (int i = 0; i < childList.length; i++) {// 初始时,让所有的子选项均未被选中
        //			for (int a = 0; a < childList[i].length; a++) {
        //				statusHashMap.put(childList[i][a], false);
        //			}
        //		}
    }


    @Override
    public int getCount() {

        if (groupList.length > 0) {
            return groupList.length;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {

        return groupList[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String str = groupList[position];
        ViewHolder localViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.layout_parent, null);
            localViewHolder = new ViewHolder();
            localViewHolder.mTextTitle = ((TextView) convertView
                    .findViewById(R.id.parent_textview));
            convertView.setTag(localViewHolder);
        } else {
            localViewHolder = (ViewHolder) convertView.getTag();
        }
        if (str != null) {
            localViewHolder.mTextTitle.setText(str+"");
        } else {
            localViewHolder.mTextTitle.setText("");
        }
        return convertView;
    }

    //	@Override
    //	public View getGroupView(int groupPosition, boolean isExpanded,
    //			View convertView, ViewGroup parent) {
    //		String str = groupList[groupPosition];
    //		ViewHolder localViewHolder;
    //		if (convertView == null) {
    //			convertView = LayoutInflater.from(mContext).inflate(
    //					R.layout.layout_parent, null);
    //			localViewHolder = new ViewHolder();
    //			localViewHolder.mTextTitle = ((TextView) convertView
    //					.findViewById(R.id.parent_textview));
    //			convertView.setTag(localViewHolder);
    //		} else {
    //			localViewHolder = (ViewHolder) convertView.getTag();
    //
    //		}
    //		if (isExpanded) {
    //			localViewHolder.mTextTitle.setBackgroundColor(this.mContext
    //					.getResources().getColor(R.color.app_title));
    //			currentGroupId = groupPosition;
    //		} else {
    //			localViewHolder.mTextTitle.setBackgroundColor(color.transparent);
    //		}
    //		localViewHolder.mTextTitle.setText(str);
    //		return convertView;
    //	}
    //
    //	@Override
    //	public View getChildView(int groupPosition, int childPosition,
    //			boolean isLastChild, View convertView, ViewGroup parent) {
    //		String str = childList[groupPosition][childPosition];
    //		final ViewHolderRadioButton localViewHolder;
    //		if (convertView == null) {
//    		convertView = LayoutInflater.from(mContext).inflate(
//   					R.layout.layout_children, null);
    //			localViewHolder = new ViewHolderRadioButton();
    //			localViewHolder.radioButton = (CheckBox) convertView
    //					.findViewById(R.id.second_textview);
    //			convertView.setTag(localViewHolder);
    //		} else {
    //			localViewHolder = (ViewHolderRadioButton) convertView.getTag();
    //
    //		}
    //		localViewHolder.radioButton.setText(str);
    //
    //		Boolean nowStatus = statusHashMap
    //				.get(childList[groupPosition][childPosition]);// 当前状态
    //		localViewHolder.radioButton.setChecked(nowStatus);
    //		if (selectGroupId == groupPosition && currentChildId == childPosition) {
    //			localViewHolder.radioButton.setTextColor(Color.WHITE);
    //			localViewHolder.radioButton.setBackgroundColor(Color
    //					.parseColor("#3AC2CF"));
    //		} else {
    //			localViewHolder.radioButton.setTextColor(mContext.getResources()
    //					.getColor(R.color.text_color_737373));
    //			localViewHolder.radioButton.setBackgroundColor(this.mContext
    //					.getResources().getColor(R.color.app_title));
    //		}
    //
    //		return convertView;
    //	}
    //
    //	@Override
    //	public boolean isChildSelectable(int groupPosition, int childPosition) {
    //
    //		// TODO Auto-generated method stub
    //		return true;
    //	}


    private class ViewHolder {
        TextView mTextTitle;


    }

//    private class ViewHolderRadioButton {
//        CheckBox radioButton;
//
//    }
}
