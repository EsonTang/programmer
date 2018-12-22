package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.PointsMallItemDataBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.PersonalPointsMallItemDetailsActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.CustomImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc: 积分商城adapter
 * Created by huangchangguo
 * Date:  2016/8/19 17:24
 */

public class SubCommPointsMallAdapter extends BaseAdapter {
    private List<PointsMallItemDataBean> parentitems = new ArrayList<PointsMallItemDataBean>();
    private Context mContext;
    private View.OnClickListener nClickListener;
//    private RelativeLayout.LayoutParams params;

    public SubCommPointsMallAdapter(Context context) {
        this.mContext = context;
        nClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object o = v.getTag(R.id.itembean_mall);
                if (o == null) return;
                PointsMallItemDataBean bean = (PointsMallItemDataBean) o;
                Intent intent = new Intent(mContext, PersonalPointsMallItemDetailsActivity.class);
                intent.putExtra("ItemData", bean);
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        };
//        params= new RelativeLayout.LayoutParams((int) (ClientInfo.getInstance().screenWidth*0.416), (int) (ClientInfo.getInstance().screenHeight*0.145));
//        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
    }

    /**
     * 清空item
     */
    public void clearAll() {
        parentitems.clear();
        notifyDataSetChanged();
    }

    /**
     * 设置初始值
     */
    public void setData(List<PointsMallItemDataBean> data) {
        if (data != null) {
            parentitems = data;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加item
     */
    public void addData(ArrayList<PointsMallItemDataBean> data) {
        if (data != null) {
            parentitems.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        if (parentitems != null && parentitems.size() > 0) {
            return parentitems.size();
        } else {
            return 0;
        }
    }

    @Override
    public PointsMallItemDataBean getItem(int position) {
        if (parentitems != null && parentitems.size() > 0) {
            return parentitems.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.sub_item_personal_points_mall, null);
            viewHolder = new ViewHolder();
            viewHolder.itemIcon = (CustomImageView) convertView
                    .findViewById(R.id.points_mall_item_icon);
            viewHolder.itemName = (TextView) convertView
                    .findViewById(R.id.points_mall_item_title);
            viewHolder.itemPoint = (TextView) convertView
                    .findViewById(R.id.points_mall_item_point);
            convertView.setTag(viewHolder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final PointsMallItemDataBean item = getItem(position);
        if (item == null)
            return convertView;
        convertView.setOnClickListener(nClickListener);
        convertView.setTag(R.id.itembean_mall, item);
//        viewHolder.itemIcon.setLayoutParams(params);
        ImageLoader.getInstance().displayImage(item.iconUrl,
                viewHolder.itemIcon, UILimageUtil.getUILoptions(R.drawable.points_mall_item_def_icon), null);
        viewHolder.itemName.setText(item.title);
        viewHolder.itemPoint.setText(mContext.getString(R.string.point_value_string, item.points));
//        if (item.type == 0) {
//            viewHolder.itemGetbtn.setText(mContext.getString(R.string.luckdraw));
//        } else {
//            viewHolder.itemGetbtn.setText(mContext.getString(R.string.exchange));
//        }


        return convertView;
    }

    private static class ViewHolder {
        // 积分商城item图标
        CustomImageView itemIcon;
        //积分商城item名字
        TextView itemName;
        // 积分商城item积分
        TextView itemPoint;

    }
}
