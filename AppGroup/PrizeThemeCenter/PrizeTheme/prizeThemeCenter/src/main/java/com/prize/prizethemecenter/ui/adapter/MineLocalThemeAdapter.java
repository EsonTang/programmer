package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.LocalThemeBean;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import java.io.File;
import java.util.ArrayList;


/**
 *
 * Created by Fanghui
 * 本地主题等适配器
 */
public class MineLocalThemeAdapter extends BaseAdapter{

    private  ArrayList<LocalThemeBean> items = new ArrayList<>();

    public  Activity context;

    public static final String RECEIVER_ACTION = "appley_theme_ztefs";

    private boolean isPressed = false;//按钮是否是在使用

    /***
     *
     * @param activity
     */
    public MineLocalThemeAdapter(Activity activity){
        context = activity;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LocalThemeBean getItem(int position) {
        if(position>=items.size()){
            return null;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDatas(ArrayList<LocalThemeBean> data) {
        this.items = data;
    }

    public void setData(ArrayList<LocalThemeBean> data) {
        if (data != null) {
            items = data;
        }
        setDatas(data);
        notifyDataSetChanged();
    }

    public void addData(ArrayList<LocalThemeBean> data) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
             convertView = LayoutInflater.from(context).inflate(R.layout.item_mine_local_layout, null);
             viewHolder = new ViewHolder();
             viewHolder.mCornerImageView = (CornerImageView) convertView.findViewById(R.id.mine_item_iv);
             viewHolder.mTrackIv = (ImageView) convertView.findViewById(R.id.mine_item_track_iv);
             viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.mine_item_checkBox);
             viewHolder.mItemTitleTv = (TextView) convertView.findViewById(R.id.mine_item_title_tv);
             viewHolder.mItemBtnTv = (TextView) convertView.findViewById(R.id.mine_item_btn_tv);
             viewHolder.mItemBtnFL = (FrameLayout) convertView.findViewById(R.id.use_FL);
             viewHolder.mApplyIv = (ImageView) convertView.findViewById(R.id.use_IV);
             viewHolder.mItemBtnFL.setVisibility(View.VISIBLE);
             convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mCheckBox.setVisibility(View.GONE);
        final LocalThemeBean bean = getItem(position);
        if(bean!=null && bean.getIconPath()!=null){
            ImageLoader.getInstance().displayImage("file://"+bean.getIconPath(),viewHolder.mCornerImageView,
                    UILimageUtil.getMineImgOption(), null);
        }

        if (Integer.parseInt(bean.getIsSelected())==1){
            viewHolder.mTrackIv.setVisibility(View.VISIBLE);
            viewHolder.mTrackIv.setImageResource(R.drawable.icon_is_used);
            viewHolder.mItemBtnTv.setTextColor(context.getResources().getColor(R.color.textcolor_969696));
            viewHolder.mItemBtnTv.setText(R.string.installed);
            isPressed = true;
            viewHolder.mApplyIv.setEnabled(!isPressed);
        }else {
            viewHolder.mTrackIv.setVisibility(View.GONE);
            viewHolder.mItemBtnTv.setText(R.string.use);
            viewHolder.mItemBtnTv.setTextColor(context.getResources().getColor(R.color.text_color_33cccc));
            isPressed = false;
            viewHolder.mApplyIv.setEnabled(!isPressed);
        }
        viewHolder.mApplyIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bean.getThemePath() != null&&new File(bean.getThemePath()).exists()) {
                    Intent intent = new Intent(RECEIVER_ACTION);
                    intent.putExtra("themePath",bean.getThemePath());
//                    context.sendBroadcast(intent);
                    DBUtils.cancelLoadedState(1);
                    DownloadTaskMgr.getInstance().setDownloadTaskState(1);
                    UIUtils.backToLauncher(context,intent);
                }
            }
        });

        viewHolder.mItemTitleTv.setText(bean.getName());
        return convertView;
    }

    public static class ViewHolder{
        CornerImageView mCornerImageView;
        ImageView mTrackIv;
        CheckBox mCheckBox;
        TextView mItemTitleTv;
        TextView mItemBtnTv;
        FrameLayout mItemBtnFL;
        ImageView mApplyIv;
    }
}
