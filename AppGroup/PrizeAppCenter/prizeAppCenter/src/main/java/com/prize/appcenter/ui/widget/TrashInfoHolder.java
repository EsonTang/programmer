package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.qihoo.cleandroid.sdk.TrashClearUtils;
import com.prize.qihoo.lib.atv.model.TreeNode;
import com.qihoo360.mobilesafe.opti.env.clear.TrashClearEnv;
import com.qihoo360.mobilesafe.opti.i.trashclear.TrashInfo;

public class TrashInfoHolder extends TreeNode.BaseNodeViewHolder<TrashInfo> {
	private Context mContext;
	private PackageManager mPackageManager;
	private Drawable mDefaultActivityIcon;	
	private OnClickCallback mOnClickCallback;
    private boolean mNeedCheckTip;
    public int mLevel;
	
    public TrashInfoHolder(Context context, int level, boolean isNeedCheckTip) {
		super(context);
		mContext = context;
		mLevel = level;
        mNeedCheckTip = isNeedCheckTip;
        mPackageManager = mContext.getPackageManager();
        mDefaultActivityIcon = mPackageManager.getDefaultActivityIcon();
		// TODO Auto-generated constructor stub
	}

	public View createNodeView(final TreeNode node, final TrashInfo trashInfo) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.clear_trash_clear_list_item_level2, null, false);

        ImageView icon = (ImageView) view.findViewById(R.id.running_app_icon);
        TextView leftTopText = (TextView) view.findViewById(R.id.left_top_text);
        TextView leftBottomText = (TextView) view.findViewById(R.id.left_bottom_text);
        TextView rightText = (TextView) view.findViewById(R.id.right_text);
        final ImageView selectedView = (ImageView) view.findViewById(R.id.checked_view);
 
        selectedView.setVisibility(View.VISIBLE);
        //selectedView.setTag(trashInfo);
        selectedView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                boolean flag = mNeedCheckTip && !trashInfo.isSelected;

                if (mOnClickCallback != null) {
                    // 设置勾选状态
                    if (trashInfo.isInWhiteList) {
                        ((ImageView) v).setImageResource(R.drawable.common_whitelist_lock);
                    } else {
                        mOnClickCallback.onTrashInfoSelectedChanged(v, trashInfo, node, flag);
                    }
                }
            }
        });

        if(node.isLeaf()){
	        View middleView = view.findViewById(R.id.midle_view);
	        middleView.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	/*if (mOnClickCallback != null) {
	            		mOnClickCallback.onTrashInfoItemClick(trashInfo); 
	                }*/
                    selectedView.performClick();
	            }
	        });
        }

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
                if (mLevel == 0) {
                    if (TextUtils.isEmpty(trashInfo.packageName)) {
                        int appTypeFromDB = trashInfo.bundle.getInt(TrashClearEnv.dbType);
                        if (appTypeFromDB == TrashClearEnv.DB_APPTYPE_COMMON) {// 显示通用目录的图标
                            icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_trash_common));
                        } else {
                            icon.setImageDrawable(mDefaultActivityIcon);
                        }
                        //icon.setImageDrawable(mDefaultActivityIcon);
                    } else {
                        icon.setImageDrawable(TrashClearUtils.getApplicationIcon(trashInfo.packageName, mPackageManager));
                    }
                    icon.setVisibility(View.VISIBLE);
                } else {
                    icon.setVisibility(View.INVISIBLE);
                }

                break;
            case TrashClearEnv.CATE_APP_SYSTEM_CACHE:
                if (trashInfo.desc == null) {
                    trashInfo.desc = TrashClearUtils.getAppName(trashInfo.packageName, mPackageManager);
                }
                if (mLevel == 0) {
                    icon.setImageDrawable(TrashClearUtils.getApplicationIcon(trashInfo.packageName, mPackageManager));
                    icon.setVisibility(View.VISIBLE);
                } else {
                    icon.setVisibility(View.INVISIBLE);
                    // 应用系统缓存不能进行单个清理
                    selectedView.setVisibility(View.INVISIBLE);
                }
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
                    case TrashClearEnv.APK_TYPE_DATELINE:
                        typeDesc = mContext.getString(R.string.clear_sdk_apk_type_dateline);
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

                if (mLevel == 0) {
                    icon.setImageDrawable(mDefaultActivityIcon);
                    icon.setVisibility(View.VISIBLE);
                } else {
                    icon.setVisibility(View.INVISIBLE);
                }

                break;
            case TrashClearEnv.CATE_BIGFILE:

                if (mLevel == 0) {
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
                } else {
                    icon.setVisibility(View.INVISIBLE);
                    boolean isOtherBigFile = trashInfo.bundle.getBoolean(TrashClearEnv.isOtherBigFile);
                    if (isOtherBigFile) {
                        String src = trashInfo.bundle.getString(TrashClearEnv.src);
                        if (src != null) {
                            leftBottomText.setText(mContext.getString(R.string.clear_sdk_come_from, src));
                            leftBottomText.setVisibility(View.VISIBLE);
                        }
                    }
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

        return view;
    }
	
	public static interface OnClickCallback {

        void onTrashInfoSelectedChanged(View view, TrashInfo trashInfo, TreeNode node, boolean isNeedCheckTip);

        void onTrashInfoItemClick(TrashInfo trashInfo);
    }
	
	public void setOnClickCallback(OnClickCallback onClickListener) {
        mOnClickCallback = onClickListener;
    }
}
    
