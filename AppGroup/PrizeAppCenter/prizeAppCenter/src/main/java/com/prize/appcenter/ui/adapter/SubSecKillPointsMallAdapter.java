package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.PointsMallItemDataBean;
import com.prize.app.util.DisplayUtil;
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

public class SubSecKillPointsMallAdapter extends BaseAdapter {
    private List<PointsMallItemDataBean> parentitems = new ArrayList<PointsMallItemDataBean>();
    private Context mContext;
    private View.OnClickListener nClickListener;
    private RelativeLayout.LayoutParams params;
    public SubSecKillPointsMallAdapter(Context context) {
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
                ((Activity)mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        };
        params= new RelativeLayout.LayoutParams((int) (ClientInfo.getInstance().screenWidth*0.416), (int) (ClientInfo.getInstance().screenHeight*0.145));
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.setMargins(0, (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,3),0,0);
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
                    R.layout.sub_item_seckill, null);
            viewHolder = new ViewHolder();
            viewHolder.itemIcon = (CustomImageView) convertView
                    .findViewById(R.id.points_mall_item_icon);
            viewHolder.itemName = (TextView) convertView
                    .findViewById(R.id.points_mall_item_title);
            viewHolder.itemPoint = (TextView) convertView
                    .findViewById(R.id.points_mall_item_point);
            viewHolder.original_point = (TextView) convertView
                    .findViewById(R.id.original_point);
            viewHolder.secKill_Tv = (TextView) convertView
                    .findViewById(R.id.secKill_Tv);
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

        viewHolder.itemName.setText(item.title);
        viewHolder.secKill_Tv.setText(item.saleTag);
        viewHolder.secKill_Tv.setId(R.id.secKill_Tv);
        params.addRule(RelativeLayout.BELOW,R.id.secKill_Tv);
        viewHolder.itemIcon.setLayoutParams(params);
        ImageLoader.getInstance().displayImage(item.iconUrl,
                viewHolder.itemIcon, UILimageUtil.getUILoptions(R.drawable.points_mall_item_def_icon), null);
        viewHolder.itemPoint.setText(mContext.getString(R.string.point_value_string, item.salePoints));
        viewHolder.original_point.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG| Paint.ANTI_ALIAS_FLAG);
        viewHolder.original_point.setText(mContext.getString(R.string.point_value_string, item.points));
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
        // 积分商城原始需要的积分
        TextView original_point;
        // 积分商城秒杀剩余时间
        TextView secKill_Tv;

    }
}
