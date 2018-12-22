package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.FontDetailActivity;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import org.xutils.ex.DbException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.myfont.fontsdk.FounderFont;
import me.myfont.fontsdk.bean.Font;
import me.myfont.fontsdk.callback.FontChangeCallback;


/**
 * Created by Fanghui
 * 字体适配器
 */
public class MineFontAdapter extends BaseAdapter {

    private ArrayList<DownloadInfo> items = new ArrayList<>();

    public Activity context;

    private boolean isEdit = true;//是否是编辑模式

    private boolean isPressed = false;//按钮是否是在使用

    String md5;
    private boolean isSuccess = false;

    /***
     * @param activity
     */
    public MineFontAdapter(Activity activity,boolean pIsEdit) {
        context = activity;
        this.isEdit = pIsEdit;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public DownloadInfo getItem(int position) {
        if (position >= items.size()) {
            return null;
        }
        return items.get(position);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_font_layout, null);
            viewHolder = new ViewHolder();
            viewHolder.mCornerImageView = (CornerImageView) convertView.findViewById(R.id.font_logo);
            viewHolder.mTrackIv = (ImageView) convertView.findViewById(R.id.isNew);
            viewHolder.mCheckBox = (CheckBox) convertView.findViewById(R.id.mine_item_checkBox);
            viewHolder.mItemTitleTv = (TextView) convertView.findViewById(R.id.font_title);
            viewHolder.mItemBtnFL = (FrameLayout) convertView.findViewById(R.id.use_FL);
            viewHolder.mApplyTv = (TextView) convertView.findViewById(R.id.use_TV);
            viewHolder.mApplyIv = (ImageView) convertView.findViewById(R.id.use_IV);
            viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            viewHolder.mItemBtnFL.setVisibility(View.VISIBLE);
            viewHolder.mApplyIv.setVisibility(View.VISIBLE);
            viewHolder.mCheckBox.setClickable(false);
            viewHolder.mCheckBox.setEnabled(false);
            viewHolder.mCheckBox.setFocusable(false);
            if (!isEdit){
                viewHolder.mCheckBox.setVisibility(View.GONE);
            }else {
                viewHolder.mCheckBox.setVisibility(View.VISIBLE);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final DownloadInfo bean = getItem(position);
        int state = 0;
        if (bean != null) {
            state = bean.currentState;
        }
        if (bean != null && bean.getThumbnail() != null) {
            ImageLoader.getInstance().displayImage(bean.getThumbnail(), viewHolder.mCornerImageView,
                    UILimageUtil.getMineImgOption(), null);
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
            viewHolder.mApplyTv.setVisibility(View.GONE);
            viewHolder.mApplyIv.setVisibility(View.GONE);
        }
        if (!isEdit) {
            if (viewHolder.mApplyTv.getVisibility()==View.GONE){
                viewHolder.mApplyTv.setVisibility(View.VISIBLE);
            }
            if (viewHolder.mApplyIv.getVisibility()==View.GONE){
                viewHolder.mApplyIv.setVisibility(View.VISIBLE);
            }
            viewHolder.mCornerImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainApplication.curContext, FontDetailActivity.class);
                    String fontID = bean.getThemeID().substring(0,bean.getThemeID().length()-1);
                    intent.putExtra("fontID", fontID);
                    intent.putExtra("minPic",bean.getThumbnail());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.curContext.startActivity(intent);
                }
            });
        }
        if (state == 7) {
            viewHolder.mTrackIv.setVisibility(View.VISIBLE);
            viewHolder.mTrackIv.setImageResource(R.drawable.icon_is_used);
            if (!isEdit) {
                viewHolder.mApplyTv.setTextColor(context.getResources().getColor(R.color.textcolor_969696));
                viewHolder.mApplyTv.setText(context.getString(R.string.installed));
                isPressed = true;
                viewHolder.mApplyIv.setEnabled(!isPressed);
            }
        }
        else if (state ==6){
            viewHolder.mTrackIv.setVisibility(View.INVISIBLE);
            if (!isEdit) {
//                if (DBUtils.isDownloadAndPay(bean.themeID)){
//                    viewHolder.mApplyTv.setText(R.string.common_use);
//                    viewHolder.mApplyIv.setImageResource(R.drawable.item_font_pressed);
//                }else {
                    viewHolder.mApplyTv.setText(R.string.use);
                    viewHolder.mApplyTv.setTextColor(context.getResources().getColor(R.color.text_color_33cccc));
//                    viewHolder.mApplyIv.setImageResource(R.drawable.item_font_normal);
//                }
                isPressed = false;
                viewHolder.mApplyIv.setEnabled(!isPressed);
            }
        }
        if (bean != null) {
            viewHolder.mItemTitleTv.setText(bean.getTitle());
        }
        if (!isEdit) {
            viewHolder.mApplyIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String downloadPath = FileUtils.getDownloadPathMine(bean.themeID, 3);
                    if (downloadPath != null&&new File(downloadPath).exists()) {
                        String path = new File(downloadPath).getAbsolutePath();
                        File file = new File(path);
                        if (!file.exists())
                            return;
                        try {
                            md5 =  MD5Util.getFileMD5String(file);
                            JLog.i("hu", "frommd5==" + md5 + "--md5==" + md5);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final Font mFont = new Font();
                        mFont.fontLocalPath = path;
                        mFont.md5 = md5;
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FounderFont.getInstance().changeKooBeeSystemFont(mFont, new FontChangeCallback() {
                                    @Override
                                    public void onSuccess() {
                                        ToastUtils.showToast("字体切换成功");
                                        isSuccess = true;
                                        DBUtils.updateLoadedState(3,bean.themeID);
                                        for(DownloadInfo l :items) {
                                            l.currentState=6;
                                        }
                                        bean.setCurrentState(7);
                                        notifyDataSetChanged();
                                        //                            viewHolder.mApplyTv.setText(context.getString(R.string.installed));
                                        String  themID = bean.themeID.substring(0,bean.themeID.length()-1);
                                        DownloadTaskMgr.getInstance().setDownlaadTaskState(themID,3);
                                        DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED,bean.themeID+3);
                                        try {
                                            DBUtils.updateLocalFontTable();
                                        } catch (DbException pE) {
                                            pE.printStackTrace();
                                        }
//                                if(context instanceof MineActivity){
//                                    ((MineActivity) context).showProgressBar(isSuccess);
//                                }
                                    }

                                    @Override
                                    public void onFailed(String msg) {
                                        ToastUtils.showToast("字体切换失败");
                                        isSuccess = false;
                                        JLog.i("hu", "onFailed==" + msg);
                                    }
                                });
                            }
                        });


//                        if(context instanceof MineActivity){
//                            ((MineActivity) context).showProgressBar(isSuccess);
//                        }
                    }
                }
            });
        }
        return convertView;
    }

    public static class ViewHolder {
        public CornerImageView mCornerImageView;
        public ImageView mTrackIv;
        public CheckBox mCheckBox;
        public TextView mItemTitleTv;
        public TextView mApplyTv;
        public ImageView mApplyIv;
        public FrameLayout mItemBtnFL;
    }
}
