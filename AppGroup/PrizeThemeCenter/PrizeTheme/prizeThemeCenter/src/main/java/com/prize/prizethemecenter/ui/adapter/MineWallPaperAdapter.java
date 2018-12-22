package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.PreviewActivity;
import com.prize.prizethemecenter.activity.WallDetailActivity;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTask;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.io.File;
import java.util.ArrayList;


/**
 *
 * Created by Fanghui
 * 已下载壁纸适配器
 */
public class MineWallPaperAdapter extends BaseAdapter{


    private ArrayList<DownloadInfo> items = new ArrayList<>();

    public Activity context;

    private boolean isEdit = false;//是否是编辑模式

    private boolean isPressed = false;//按钮是否是在使用

    private UIDownLoadListener listener = null;
    /***
     *
     * @param activity
     */
    public MineWallPaperAdapter(Activity activity,boolean pIsEdit){
        context = activity;
        this.isEdit = pIsEdit;
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(theme_Id + "_");
                if(task != null){
                    if(task.gameDownloadState == DownloadState.STATE_DOWNLOAD_SUCESS || task.gameDownloadState==DownloadState.STATE_DOWNLOAD_INSTALLED)
                    notifyDataSetChanged();
                }else {
                    notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public DownloadInfo getItem(int position) {
        if(position>=items.size()){
            return null;
        }
        return items.get(position);
    }
    public ArrayList<DownloadInfo> getItemList() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(ArrayList<DownloadInfo> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }

    public void addData(ArrayList<DownloadInfo> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
             convertView = LayoutInflater.from(context).inflate(R.layout.item_mine_local_layout, null);
             viewHolder = new ViewHolder();
             viewHolder.mCornerImageView = (CornerImageView) convertView.findViewById(R.id.mine_item_iv);
             viewHolder.mTrackIv = (ImageView) convertView.findViewById(R.id.mine_item_track_iv);
             viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.mine_item_checkBox);
             viewHolder.mItemBtnFL = (FrameLayout) convertView.findViewById(R.id.use_FL);
             viewHolder.mApplyIv = (ImageView) convertView.findViewById(R.id.use_IV);
             viewHolder.mCheckBox.setClickable(false);
             viewHolder.mCheckBox.setEnabled(false);
             viewHolder.mCheckBox.setFocusable(false);
             if (!isEdit){
                viewHolder.mCheckBox.setVisibility(View.GONE);
                viewHolder.mItemBtnFL.setVisibility(View.VISIBLE);
             }else {
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                viewHolder.mItemBtnFL.setVisibility(View.GONE);
             }
             viewHolder.mItemTitleTv = (TextView) convertView.findViewById(R.id.mine_item_title_tv);
             viewHolder.mItemBtnTv = (TextView) convertView.findViewById(R.id.mine_item_btn_tv);
             convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final DownloadInfo bean = getItem(position);
        int state = 0;
        if (bean != null) {
            state = bean.currentState;
        }

        if(bean!=null && bean.getThumbnail()!=null){
            ImageLoader.getInstance().displayImage(bean.getThumbnail(),viewHolder.mCornerImageView,
                    UILimageUtil.getMineImgOption(), null);
        }
        if (bean != null) {
            viewHolder.mItemTitleTv.setText(bean.getTitle());
        }
        if (isEdit) {
            try {
                if (bean.isChecked) {
                    viewHolder.mCheckBox.setChecked(true);
                    viewHolder.mCheckBox.setVisibility(View.VISIBLE);
                    viewHolder.mCornerImageView.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                } else {
                    viewHolder.mCheckBox.setChecked(false);
                    viewHolder.mCheckBox.setVisibility(View.GONE);
                    viewHolder.mCornerImageView.setColorFilter(null);
                }
                viewHolder.mCornerImageView.invalidate();
            } catch (Exception pE) {
                pE.printStackTrace();
            }
            viewHolder.mItemBtnTv.setVisibility(View.GONE);
        }
        if (!isEdit) {
            if (viewHolder.mItemBtnFL.getVisibility()==View.GONE){
                viewHolder.mItemBtnFL.setVisibility(View.VISIBLE);
            }
            viewHolder.mCornerImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainApplication.curContext, WallDetailActivity.class);
                    intent.putExtra("wallID", bean.getThemeID().substring(0, bean.getThemeID().length()-1));
                    intent.putExtra("minPic",bean.getThumbnail());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.curContext.startActivity(intent);
                }
            });
        }
        if (state == 7){
            viewHolder.mTrackIv.setVisibility(View.VISIBLE);
            viewHolder.mTrackIv.setImageResource(R.drawable.icon_is_used);
            if (!isEdit) {
                viewHolder.mItemBtnTv.setTextColor(context.getResources().getColor(R.color.textcolor_969696));
                viewHolder.mItemBtnTv.setText(context.getString(R.string.installed));
                Drawable drawable = context.getResources().getDrawable(R.drawable.common_use_btn);
                viewHolder.mApplyIv.setBackground(drawable);
                isPressed = true;
                viewHolder.mApplyIv.setEnabled(!isPressed);
            }
        }
        else if (state ==6){
            viewHolder.mTrackIv.setVisibility(View.INVISIBLE);
            if (!isEdit) {
                viewHolder.mItemBtnTv.setText(R.string.use);
                viewHolder.mItemBtnTv.setTextColor(context.getResources().getColor(R.color.text_color_33cccc));
                Drawable drawable = context.getResources().getDrawable(R.drawable.common_use_btn);
                viewHolder.mApplyIv.setBackground(drawable);
                isPressed = false;
                viewHolder.mApplyIv.setEnabled(!isPressed);
            }
        }
        if (!isEdit&&!isPressed) {
            viewHolder.mApplyIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String downloadPath = FileUtils.getDownloadPathMine(bean.themeID, 2);
                    if (downloadPath != null&&new File(downloadPath).exists()) {
                        Intent intent = new Intent(context, PreviewActivity.class);
                        intent.putExtra("wallID", bean.getThemeID().substring(0,bean.getThemeID().length()-1));
                        intent.putExtra("localWallPath", downloadPath);
                        intent.putExtra("wallType", bean.getWallType());
                        intent.putExtra("activityType", 0);
                        context.startActivity(intent);
                    }
                }
            });
        }
        return convertView;
    }

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    public void removeDownLoadHandler() {
        AppManagerCenter.removeDownloadRefreshHandle(listener);
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AppManagerCenter.setDownloadRefreshHandle(listener);
    }

    public static class ViewHolder{
        public CornerImageView mCornerImageView;
        public ImageView mTrackIv;
        public CheckBox mCheckBox;
        public TextView mItemTitleTv;
        public TextView mItemBtnTv;
        public FrameLayout mItemBtnFL;
        public ImageView mApplyIv;
    }

}
