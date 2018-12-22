package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.CheckImageView;
import com.prize.statistics.model.ExposureBean;

import java.util.ArrayList;
import java.util.List;

public class OneKeyInsallAdapter extends BaseAdapter {

    private ArrayList<AppsItemBean> datas;
    private Context ctx;

    private ArrayList<AppsItemBean> checks = new ArrayList<AppsItemBean>();

    // private int MAX_COUNT = 16;

    //private TextView text;

    class checkInfo {
        boolean check;
    }

    private ArrayList<checkInfo> checkPositionlist = new ArrayList<checkInfo>();
    private int checkCount = 0;
    private List<ExposureBean> mExposureBeans = new ArrayList<>();

    public OneKeyInsallAdapter(Context context, ArrayList<AppsItemBean> datas, int currentPage) {
        ctx = context;
        //checks.addAll(datas);
        boolean isNeedStatic = !TextUtils.isEmpty(CommonUtils.getNewTid())|| JLog.isDebug;
        this.datas = datas;
        ExposureBean pbean;
        for (AppsItemBean item : datas) {
            checkInfo c = new checkInfo();
            c.check = item.isCheck;
            checkPositionlist.add(c);
            if (isNeedStatic) {
                pbean = CommonUtils.formNewPagerExposure(item, "onekey", "default");
                if (!mExposureBeans.contains(pbean)) {
                    mExposureBeans.add(pbean);
                }
            }
            if (item.isCheck) {
                checks.add(item);
            }
        }
        //this.text = t;
    }

    public List<ExposureBean> getmExposureBeans() {
        return mExposureBeans;
    }

    public ArrayList<AppsItemBean> getChecks() {
        return checks;
    }

//	public ArrayList<AppsItemBean> getDatas() {
//		return datas;
//	}

    @Override
    public int getCount() {
        // if (datas.size() > MAX_COUNT) {
        // return MAX_COUNT;
        // }
        return datas.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(ctx).inflate(
                    R.layout.download_dialog_item, null);
            viewHolder.appitemImg = (CheckImageView) convertView
                    .findViewById(R.id.appItem_img_id);
            viewHolder.appItemName = (TextView) convertView
                    .findViewById(R.id.appItem_name_id);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AppsItemBean data = datas.get(position);

        if (!TextUtils.isEmpty(data.largeIcon)) {
            ImageLoader.getInstance().displayImage(data.largeIcon,
                    viewHolder.appitemImg, UILimageUtil.getUILoptions(), null);
        } else {

            if (data.iconUrl != null) {
                ImageLoader.getInstance().displayImage(data.iconUrl,
                        viewHolder.appitemImg, UILimageUtil.getUILoptions(),
                        null);
            }
        }

        viewHolder.appitemImg.setTag(data);
        viewHolder.appItemName.setText(data.name);
        //text.setText(ctx.getString(R.string.dialog_down_comment));

        // 全选
        if (checkPositionlist.get(position).check) {
            viewHolder.appitemImg.setCheck(true);
        } else {
            viewHolder.appitemImg.setCheck(false);
        }

        viewHolder.appitemImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CheckImageView iconView = (CheckImageView) v;
                AppsItemBean info = (AppsItemBean) iconView.getTag();
                if (!iconView.isCheck()) {
                    iconView.setCheck(true);
                    checkPositionlist.get(position).check = true;
                    info.isCheck = true;  //修改应用选中标志
                    if (!checks.contains(info)) {
                        checks.add(info);
                        checkCount++;
                    }
                } else {
                    if (checks.contains(info)) {
                        checks.remove(info);
                        checkCount--;
                    }
                    info.isCheck = false;
                    iconView.setCheck(false);
                    checkPositionlist.get(position).check = false;
                }
                checkCountCallBack.countCallBack(checks);
                v.invalidate();
            }
        });
        return convertView;
    }

    public interface CheckCountCallBack {
        void countCallBack(ArrayList<AppsItemBean> checks);
    }

    public CheckCountCallBack checkCountCallBack;

    public void setCheckCountCallBack(CheckCountCallBack checkCountCallBack) {
        this.checkCountCallBack = checkCountCallBack;
    }

    /*
     * public void show(){ int size = 0; for (AppsItemBean check : checks) {
     * size += Integer.valueOf(check.apkSize); }
     * text.setText(ctx.getString(R.string.dialog_down_comment,
     * String.valueOf(checks.size()), String.format("%1$.2f", size / (1024 *
     * 1024f)))); }
     */
    private class ViewHolder {
        public CheckImageView appitemImg;
        public TextView appItemName;
    }

}
