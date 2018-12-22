package com.prize.prizethemecenter.ui.adapter;

import android.app.Activity;
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
import com.prize.prizethemecenter.bean.LocalFontBean;
import com.prize.prizethemecenter.bean.table.LocalFontTable;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.FontModel;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.widget.CornerImageView;

import org.xutils.ex.DbException;

import java.io.File;
import java.util.ArrayList;

import me.myfont.fontsdk.FounderFont;
import me.myfont.fontsdk.bean.Font;
import me.myfont.fontsdk.callback.FontChangeCallback;


/**
 *
 * Created by Fanghui
 * 本地字体
 */
public class MineLocalFontAdapter extends BaseAdapter{

    private  ArrayList<LocalFontBean> items = new ArrayList<>();

    public  Activity context;
    private boolean isSuccess = false;
    private boolean isPressed = false;//按钮是否是在使用


    /***
     *
     * @param activity
     */
    public MineLocalFontAdapter(Activity activity){
        context = activity;
    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public LocalFontBean getItem(int position) {
        if(position>=items.size()){
            return null;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setDatas(ArrayList<LocalFontBean> data) {
        this.items = data;
    }

    public void setData(ArrayList<LocalFontBean> data) {
        if (data != null) {
            items = data;
        }
        setDatas(data);
        notifyDataSetChanged();
    }

    public void addData(ArrayList<LocalFontBean> data) {
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
             convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mCheckBox.setVisibility(View.GONE);
        final LocalFontBean bean = getItem(position);
        if (bean.getIsSelected()){
            viewHolder.mTrackIv.setVisibility(View.VISIBLE);
            viewHolder.mTrackIv.setImageResource(R.drawable.icon_is_used);
            viewHolder.mApplyTv.setTextColor(context.getResources().getColor(R.color.textcolor_969696));
            viewHolder.mApplyTv.setText(R.string.installed);
            isPressed = true;
            viewHolder.mApplyIv.setEnabled(!isPressed);
        }else {
            viewHolder.mTrackIv.setVisibility(View.GONE);
            viewHolder.mApplyTv.setTextColor(context.getResources().getColor(R.color.text_color_33cccc));
            viewHolder.mTrackIv.setVisibility(View.GONE);
            viewHolder.mApplyTv.setText(R.string.use);
            isPressed = false;
            viewHolder.mApplyIv.setEnabled(!isPressed);
        }
        if(bean!=null && bean.getIconPath()!=null){
            ImageLoader.getInstance().displayImage("assets://"+bean.getIconPath(),viewHolder.mCornerImageView,
                    UILimageUtil.getMineImgOption(), null);
        }
        if (!isPressed) {
            viewHolder.mApplyIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bean.getFontPath() != null&&new File(bean.getFontPath()).exists()) {
                        String path = new File(bean.getFontPath()).getAbsolutePath();
                        File file = new File(path);
                        if (!file.exists())
                            return;
                        String md5 = null;
                        try {
                            String frommd5 = MD5Util.getFileMD5String(file);
                            LocalFontTable localFontTable = MainApplication.getDbManager().selector(LocalFontTable.class).where("localFontId","==",bean.getFontId()).findFirst();
                            md5 =  localFontTable.md5;
                            if(!frommd5.equals(md5)){
                                File mFile = new File(localFontTable.getPath());
                                if(file.exists()){
                                    FileUtils.DeleteFile(mFile);
                                    FontModel fontModel = new FontModel(MainApplication.curContext);
                                    fontModel.loadLocalFont("font/");
                                }
                                ToastUtils.showToast("正在初始化，请稍后。。。");
                                return;
                            }
                            JLog.i("hu", "frommd5==" + md5 + "--md5==" + md5);
                        } catch (Exception e) {
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
                                        JLog.i("hu", "  on onSuccess ");
                                        isSuccess = true;
                                        try {
                                            DBUtils.saveLocalTable(bean.getFontId());
                                        } catch (DbException pE) {
                                            pE.printStackTrace();
                                        }
                                        isPressed = true;
                                        viewHolder.mApplyIv.setEnabled(!isPressed);
                                        bean.setIsSelected(true);
                                        notifyDataSetChanged();
                                        //                            DBUtils.CancellUsedType(3);
                                        DBUtils.cancelLoadedState(3);
                                        DownloadTaskMgr.getInstance().setDownloadTaskState(3);
                                    }

                                    @Override
                                    public void onFailed(String msg) {
                                        ToastUtils.showToast("字体切换失败");
                                        isSuccess =false;
                                        JLog.i("hu", "onFailed==" + msg);
                                    }
                                });
                            }
                        });
//                        if(context instanceof MineLocalFontActivity){
//                            ((MineLocalFontActivity) context).showProgressBar(isSuccess);
//                        }
                    }
                }
            });
        }
        viewHolder.mItemTitleTv.setText(bean.getName());
        return convertView;
    }

    public static class ViewHolder{
        public CornerImageView mCornerImageView;
        public ImageView mTrackIv;
        public CheckBox mCheckBox;
        public TextView mItemTitleTv;
        public TextView mApplyTv;
        public ImageView mApplyIv;
        public FrameLayout mItemBtnFL;

    }
}
