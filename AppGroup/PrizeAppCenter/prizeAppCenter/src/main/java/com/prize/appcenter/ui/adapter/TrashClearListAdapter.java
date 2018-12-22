
package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.qihoo.cleandroid.sdk.TrashClearUtils;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashClearCategory;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

import java.util.ArrayList;
import java.util.List;

public class TrashClearListAdapter extends BaseExpandableListAdapter {

    public static interface OnClickCallback {

        void onCategorySelectedChanged(TrashClearCategory cate);

        void onTrashInfoSelectedChanged(TrashInfo trashInfo);
    }

    private final PackageManager mPackageManager;

    private final Context mContext;

    private List<TrashClearCategory> mCateList = new ArrayList<TrashClearCategory>();

    private OnClickCallback mOnClickCallback;

    private final Drawable mDefaultActivityIcon;

    private final LayoutInflater mInflater;

    public TrashClearListAdapter(Context context, List<TrashClearCategory> cateList) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        mCateList = cateList;
        mDefaultActivityIcon = mPackageManager.getDefaultActivityIcon();
        mInflater = LayoutInflater.from(context);
    }

    public void setOnClickCallback(OnClickCallback onClickListener) {
        mOnClickCallback = onClickListener;
    }

    public void refresh(List<TrashClearCategory> cateList) {
        mCateList = cateList;
        notifyDataSetChanged();
    }

    @Override
    public TrashInfo getChild(int groupPosition, int childPosition) {

        return mCateList.get(groupPosition).trashInfoList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mCateList.get(groupPosition).trashInfoList.size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) mInflater.inflate(R.layout.clear_trash_clear_list_item_level1, parent, false);
        View itemView = view.findViewById(R.id.item);
        TrashInfo trashInfo = getChild(groupPosition, childPosition);

        setTrashInfoView(trashInfo, itemView);

        return view;
    }

    private void setTrashInfoView(TrashInfo trashInfo, View itemView) {

        ImageView icon = (ImageView) itemView.findViewById(R.id.running_app_icon);
        TextView leftTopText = (TextView) itemView.findViewById(R.id.left_top_text);
        TextView leftBottomText = (TextView) itemView.findViewById(R.id.left_bottom_text);
        TextView rightText = (TextView) itemView.findViewById(R.id.right_text);
        final ImageView selectedView = (ImageView) itemView.findViewById(R.id.checked_view);
        selectedView.setVisibility(View.VISIBLE);
        selectedView.setTag(trashInfo);
        selectedView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnClickCallback != null) {
                    mOnClickCallback.onTrashInfoSelectedChanged((TrashInfo) v.getTag());
                }
            }
        });

        View middleView = itemView.findViewById(R.id.midle_view);
        middleView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectedView.performClick();

            }
        });

        leftBottomText.setVisibility(View.GONE);
        switch (trashInfo.type) {

            case TrashClearEnv.CATE_SYSTEM_TEMP:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_temp_file);
                break;
            case TrashClearEnv.CATE_SYSTEM_THUMBNAIL:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_thumbnail);
                break;
            case TrashClearEnv.CATE_SYSTEM_INVALID_THUMBNAIL:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_thumbnail_invalid);
                break;
            case TrashClearEnv.CATE_SYSTEM_LOG:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_log_file);
                break;
            case TrashClearEnv.CATE_SYSTEM_LOSTDIR:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_lost_dir);
                break;
            case TrashClearEnv.CATE_SYSTEM_EMPTYDIR:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_empty_dir);
                break;
            case TrashClearEnv.CATE_SYSTEM_BAK:
                trashInfo.desc = mContext.getString(R.string.clear_sdk_bak);
                break;

            case TrashClearEnv.CATE_APP_SD_CACHE:
                if (TextUtils.isEmpty(trashInfo.packageName)) {
                    int appTypeFromDB = trashInfo.bundle.getInt(TrashClearEnv.dbType);
                    if (appTypeFromDB == TrashClearEnv.DB_APPTYPE_COMMON) {// 显示通用目录的图标
                        icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_trash_common));
                    } else {
                        icon.setImageDrawable(mDefaultActivityIcon);
                    }
                } else {
                    icon.setImageDrawable(TrashClearUtils.getApplicationIcon(trashInfo.packageName, mPackageManager));
                }
                icon.setVisibility(View.VISIBLE);

                break;
            case TrashClearEnv.CATE_APP_SYSTEM_CACHE:
                if (trashInfo.desc == null) {
                    trashInfo.desc = TrashClearUtils.getAppName(trashInfo.packageName, mPackageManager);
                }

                icon.setImageDrawable(TrashClearUtils.getApplicationIcon(trashInfo.packageName, mPackageManager));
                icon.setVisibility(View.VISIBLE);

                break;
            case TrashClearEnv.CATE_APK:
                String typeDesc = "";
                switch (trashInfo.dataType) {
                    case TrashClearEnv.APK_TYPE_REPEAT:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_repeat);
                        break;
                    case TrashClearEnv.APK_TYPE_DAMAGED:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_damged);
                        break;
                    case TrashClearEnv.APK_TYPE_OLDER:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_older);
                        break;
                    case TrashClearEnv.APK_TYPE_INSTALLED:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_installed);
                        break;
                    case TrashClearEnv.APK_TYPE_UNINSTALLED:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_uninstalled);
                        break;
                    case TrashClearEnv.APK_TYPE_BACKUPED:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_backup);
                        break;
                    default:
                        break;
                }
                leftBottomText.setText(typeDesc + trashInfo.bundle.getString(TrashClearEnv.apkVersionName));
                leftBottomText.setVisibility(View.VISIBLE);

                Drawable drawable = TrashClearUtils.loadApkIcon(mContext, trashInfo.bundle.getInt(TrashClearEnv.apkIconID), trashInfo.path);
                if (drawable == null) {
                    icon.setImageDrawable(mDefaultActivityIcon);
                } else {
                    icon.setImageDrawable(drawable);
                }

                break;
            case TrashClearEnv.CATE_UNINSTALLED:

                    icon.setImageDrawable(mDefaultActivityIcon);
                    icon.setVisibility(View.VISIBLE);
                break;
            case TrashClearEnv.CATE_BIGFILE:

                    if (trashInfo.packageName != null) {
                        icon.setImageDrawable(TrashClearUtils.getApplicationIcon(trashInfo.packageName, mPackageManager));
                    } else {
                        icon.setImageDrawable(mDefaultActivityIcon);
                    }
                    icon.setVisibility(View.VISIBLE);

                    String src = trashInfo.bundle.getString(TrashClearEnv.src);
                    if (src != null) {
                        leftBottomText.setText(mContext.getString(R.string.clear_sdk_come_from, src));
                        leftBottomText.setVisibility(View.VISIBLE);
                    }

                break;

            default:
                break;
        }

        if (TrashClearEnv.CATE_BIGFILE == trashInfo.type && TrashClearEnv.BIGFILE_OTHER.equals(trashInfo.desc)) {
            leftTopText.setText(R.string.clear_sdk_bigfile_other);
        } else {
            leftTopText.setText(trashInfo.desc);
        }

        rightText.setText(TrashClearUtils.getHumanReadableSizeMore(trashInfo.size));

        // 设置勾选状态
        if (trashInfo.isInWhiteList) {
            selectedView.setImageResource(R.drawable.common_whitelist_lock);
        } else {
            selectedView.setImageResource(trashInfo.isSelected ? R.drawable.node_selected : R.drawable.node_not_selected);
        }

    }

    @Override
    public TrashClearCategory getGroup(int groupPosition) {
        return mCateList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mCateList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        final TrashClearCategory category = getGroup(groupPosition);
        GroupHolder holder;
        if(convertView == null){
            holder = new GroupHolder();

            convertView = mInflater.inflate(R.layout.clear_trash_clear_list_cate_item, null, false);
            holder.textView = (TextView) convertView.findViewById(R.id.group_title);
            holder.arrowView = (ImageView) convertView.findViewById(R.id.arrow_iv);
            holder.checkedView = (ImageView) convertView.findViewById(R.id.group_title_checked);
            holder.sizeView = (TextView) convertView.findViewById(R.id.size_tv);
            holder.divider = convertView.findViewById(R.id.divider_d7d7d7);

            convertView.setTag(holder);
        }else {
            holder = (GroupHolder) convertView.getTag();
        }

        if(isExpanded){
            holder.divider.setVisibility(View.GONE);
            holder.arrowView.setImageDrawable(mContext.getDrawable(R.drawable.node_expanded));
        }else {
            holder.divider.setVisibility(View.VISIBLE);
            holder.arrowView.setImageDrawable(mContext.getDrawable(R.drawable.node_not_expanded));
        }

        holder.checkedView.setVisibility(View.VISIBLE);
        holder.checkedView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mOnClickCallback != null) {
                    mOnClickCallback.onCategorySelectedChanged(category);
                }
            }
        });
        switch (category.type) {
            case TrashClearEnv.CATE_SYSTEM:
                category.desc = mContext.getString(R.string.clear_sdk_trash_system);
                break;
            case TrashClearEnv.CATE_CACHE:
                category.desc = mContext.getString(R.string.clear_sdk_trash_cache);
                break;
            case TrashClearEnv.CATE_ADPLUGIN:
                category.desc = mContext.getString(R.string.clear_sdk_trash_adplugin);
                break;
            case TrashClearEnv.CATE_APK:
                category.desc = mContext.getString(R.string.clear_sdk_trash_apk);
                break;
            case TrashClearEnv.CATE_UNINSTALLED:
                category.desc = mContext.getString(R.string.clear_sdk_trash_uninstalled);
                break;
            case TrashClearEnv.CATE_BIGFILE:
                category.desc = mContext.getString(R.string.clear_sdk_trash_bigfile);
                break;

            default:
                break;
        }
        holder.textView.setText(category.desc);

        holder.sizeView.setText(TrashClearUtils.getHumanReadableSizeMore(category.size));

        if (category.size == category.selectedSize) {
            holder.checkedView.setImageResource(R.drawable.node_selected);
        } else if (category.selectedSize > 0) {
            holder.checkedView.setImageResource(R.drawable.node_half_selected);
        } else {
            holder.checkedView.setImageResource(R.drawable.node_not_selected);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public class GroupHolder{
        TextView textView;
        ImageView arrowView;
        ImageView checkedView;
        TextView sizeView;
        View divider;
    }
}
