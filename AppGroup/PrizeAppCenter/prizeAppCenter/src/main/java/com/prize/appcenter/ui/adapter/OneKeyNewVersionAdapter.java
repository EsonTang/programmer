package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.CustomImageView;

import java.util.ArrayList;

public class OneKeyNewVersionAdapter extends BaseAdapter {

    private ArrayList<AppsItemBean> datas;
    private Context ctx;

    private ArrayList<AppsItemBean> checks = new ArrayList<AppsItemBean>();

    private View.OnClickListener mOnClickListener;

    public OneKeyNewVersionAdapter(Context context, ArrayList<AppsItemBean> datas) {
        ctx = context;
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               AppsItemBean data= (AppsItemBean) v.getTag();
                if(data==null)return;
                if (checks.contains(data)) {
                    checks.remove(data);
                    v.setSelected(false);
                } else {
                    checks.add(data);
                    v.setSelected(true);
                }
                if (JLog.isDebug) {
                    JLog.i("FrontCoverActivity", "rightHolder-checks.size()=" + checks.size()+"data.name="+data.name);
                }
            }
        };
        this.datas = datas;
        for (AppsItemBean item : datas) {
            if (item.isCheck) {
                checks.add(item);
            }
        }
    }

    public ArrayList<AppsItemBean> getChecks() {
        return checks;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public AppsItemBean getItem(int position) {
        if(datas==null||position<0||position>=getCount())return null;
        return datas.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ViewHolder rightHolder;
        final AppsItemBean data = datas.get(position);
        int type = getItemViewType(position);
        switch (type) {
            case 0:
                if (convertView == null) {
                    viewHolder = new ViewHolder();
                    convertView = LayoutInflater.from(ctx).inflate(
                            R.layout.item_onekey_left, null, true);
                    viewHolder.appItem_Iv = (CustomImageView) convertView
                            .findViewById(R.id.appItem_Iv);
                    viewHolder.category_Tv = (TextView) convertView
                            .findViewById(R.id.category_Tv);
                    viewHolder.brief_Tv = (TextView) convertView
                            .findViewById(R.id.brief_Tv);
                    viewHolder.num_user_Tv = (TextView) convertView
                            .findViewById(R.id.num_user_Tv);
                    viewHolder.appItem_name_id = (TextView) convertView
                            .findViewById(R.id.appItem_name_id);
                    viewHolder.item_Llyt = (LinearLayout) convertView
                            .findViewById(R.id.left_Llyt);
                    convertView.setTag(viewHolder);
                }
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.item_Llyt.setTag(data);
                viewHolder.item_Llyt.setOnClickListener(mOnClickListener);

                if (!TextUtils.isEmpty(data.largeIcon)) {
                    ImageLoader.getInstance().displayImage(data.largeIcon,
                            viewHolder.appItem_Iv, UILimageUtil.getUILoptions(), null);
                } else {
                    ImageLoader.getInstance().displayImage(data.iconUrl,
                            viewHolder.appItem_Iv, UILimageUtil.getUILoptions(),
                            null);
                }
                if (TextUtils.isEmpty(data.categoryName)) {
                    viewHolder.category_Tv.setText("");
                    viewHolder.category_Tv.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.category_Tv.setText(data.categoryName);
                    viewHolder.category_Tv.setVisibility(View.VISIBLE);
                }
                if (null != data.downloadTimesFormat) {
                    String user = data.downloadTimesFormat.replace("次", "人");
                    viewHolder.num_user_Tv.setText(ctx.getString(R.string.person_use, user));
                }
                viewHolder.appItem_name_id.setText(data.name);
                if (TextUtils.isEmpty(data.brief)) {
                    viewHolder.brief_Tv.setText("");
                    viewHolder.brief_Tv.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.brief_Tv.setText("“"+data.brief+"”");
                    viewHolder.brief_Tv.setVisibility(View.VISIBLE);
                }
                if (checks.contains(data)) {
                    viewHolder.item_Llyt.setSelected(true);
                } else {
                    viewHolder.item_Llyt.setSelected(false);
                }
                break;

            case 1:
                if (convertView == null) {
                    rightHolder = new ViewHolder();
                    convertView = LayoutInflater.from(ctx).inflate(
                            R.layout.item_onekey_right, null, true);
                    rightHolder.appItem_Iv = (CustomImageView) convertView
                            .findViewById(R.id.appItem_Iv);
                    rightHolder.category_Tv = (TextView) convertView
                            .findViewById(R.id.category_Tv);
                    rightHolder.brief_Tv = (TextView) convertView
                            .findViewById(R.id.brief_Tv);
                    rightHolder.num_user_Tv = (TextView) convertView
                            .findViewById(R.id.num_user_Tv);
                    rightHolder.appItem_name_id = (TextView) convertView
                            .findViewById(R.id.appItem_name_id);
                    rightHolder.item_Llyt = (LinearLayout) convertView
                            .findViewById(R.id.right_Llyt);
                    convertView.setTag(rightHolder);
                }
                rightHolder = (ViewHolder) convertView.getTag();
                rightHolder.item_Llyt.setTag(data);
                rightHolder.item_Llyt.setOnClickListener(mOnClickListener);
                if (!TextUtils.isEmpty(data.largeIcon)) {
                    ImageLoader.getInstance().displayImage(data.largeIcon,
                            rightHolder.appItem_Iv, UILimageUtil.getUILoptions(), null);
                } else {
                    ImageLoader.getInstance().displayImage(data.iconUrl,
                            rightHolder.appItem_Iv, UILimageUtil.getUILoptions(),
                            null);
                }
                if (TextUtils.isEmpty(data.categoryName)) {
                    rightHolder.category_Tv.setText("");
                    rightHolder.category_Tv.setVisibility(View.INVISIBLE);
                } else {
                    rightHolder.category_Tv.setText(data.categoryName);
                    rightHolder.category_Tv.setVisibility(View.VISIBLE);
                }
                if (null != data.downloadTimesFormat) {
                    String user = data.downloadTimesFormat.replace("次", "人");
                    rightHolder.num_user_Tv.setText(ctx.getString(R.string.person_use, user));
                }
                rightHolder.appItem_name_id.setText(data.name);
                if (TextUtils.isEmpty(data.brief)) {
                    rightHolder.brief_Tv.setText("");
                    rightHolder.brief_Tv.setVisibility(View.INVISIBLE);
                } else {
                    rightHolder.brief_Tv.setText("“"+data.brief+"”");
                    rightHolder.brief_Tv.setVisibility(View.VISIBLE);
                }
                if (checks.contains(data)) {
                    rightHolder.item_Llyt.setSelected(true);
                } else {
                    rightHolder.item_Llyt.setSelected(false);
                }
                break;
        }


        return convertView;
    }


    private class ViewHolder {
        CustomImageView appItem_Iv;
        TextView appItem_name_id;
        TextView num_user_Tv;
        TextView category_Tv;
        TextView brief_Tv;
        LinearLayout item_Llyt;
    }

}
