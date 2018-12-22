/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.appcenter.R;
import com.prize.appcenter.bean.RequireResData;

import java.util.ArrayList;
import java.util.List;


/**
 * 必备分类
 **
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class RequireCategoryAdapter extends BaseAdapter {
    private List<RequireResData.RequireBean> items = new ArrayList<RequireResData.RequireBean>();
    private int selectedPosition = 0;
    private Context context;// CategoryBean
    private Drawable drawable;
    private Drawable tranDrawable;
    public RequireCategoryAdapter(Context activity) {
        this.context = activity;
        drawable = activity.getResources().getDrawable(R.drawable.drawableleft_require);
        tranDrawable = new ColorDrawable( activity.getResources().getColor(android.R.color.transparent));
        tranDrawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight()); //设置边界
    }

    @Override
    public int getCount() {

        // TODO Auto-generated method stub
        return items == null ? 0 : items.size();
    }

    @Override
    public String getItem(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position).title;
    }

    @Override
    public long getItemId(int position) {

        // TODO Auto-generated method stub
        return position;
    }

    public void setSelectPostion(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    public void setData(List<RequireResData.RequireBean>  data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_category_layout, parent,false);
            viewHolder = new ViewHolder();
            viewHolder.categoryName_Tv = (TextView) convertView
                    .findViewById(R.id.categoryName_Tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String name = getItem(position);
        viewHolder.categoryName_Tv.setText(name);
        if (selectedPosition == position) {
            viewHolder.categoryName_Tv.setEnabled(false);
            viewHolder.categoryName_Tv.setCompoundDrawables(drawable,null,null,null);
            viewHolder.categoryName_Tv.setBackgroundColor(Color.WHITE);
        } else {
            viewHolder.categoryName_Tv.setEnabled(true);
            viewHolder.categoryName_Tv.setCompoundDrawables(tranDrawable,null,null,null);
            viewHolder.categoryName_Tv.setBackgroundColor(Color.parseColor("#f5f5f5"));
        }

        return convertView;
    }

    static class ViewHolder {
        // 榜单排行名称
        TextView categoryName_Tv;

    }
}
