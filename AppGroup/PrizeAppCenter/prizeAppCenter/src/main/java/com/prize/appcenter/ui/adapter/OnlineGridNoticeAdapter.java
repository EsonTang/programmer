package com.prize.appcenter.ui.adapter;

import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.AppHeadCategories;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppGameListActivity;
import com.prize.appcenter.activity.OnlineGameListActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.SingleGameActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;

/**
 * *
 * 单机网游的分类入口
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class OnlineGridNoticeAdapter extends BaseAdapter {

    /**
     * 列表项
     */
    private ArrayList<AppHeadCategories> items = new ArrayList<AppHeadCategories>();
    private RootActivity mContext;

    public OnlineGridNoticeAdapter(RootActivity mContext) {
        this.mContext = mContext;
    }

    /**
     * 设置列表项
     *
     * @param data ArrayList<Navbars>
     */
    public void setData(ArrayList<AppHeadCategories> data) {
        if (null == data) {
            return;
        }

        items = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public AppHeadCategories getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.online_grid_item, null);
            holder = new ViewHolder();
            holder.image= (ImageView) convertView.findViewById(R.id.notice_item_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
       final AppHeadCategories bean = items.get(position);
        ImageLoader.getInstance().displayImage(bean.imageUrl, holder.image,
                UILimageUtil.getUILoptions(R.drawable.bg_ad), null);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CategoryAppGameListActivity.class);
                intent.putExtra(CategoryAppGameListActivity.selectPos, bean.cIdpos);
                intent.putExtra(CategoryAppGameListActivity.parentID, bean.catId);
                intent.putExtra(CategoryAppGameListActivity.typeName, bean.pCatName);
                intent.putExtra(CategoryAppGameListActivity.subtypeName, bean.catName);
                intent.putExtra(CategoryAppGameListActivity.SUBTYPEID, bean.catId);
                intent.putExtra(CategoryAppGameListActivity.tags, bean.tags);
                mContext.startActivity(intent);
                if(mContext instanceof OnlineGameListActivity){
                    MTAUtil.onOnLineGameClass(bean.showText);
                }
                if(mContext instanceof SingleGameActivity){
                    MTAUtil.onSingleGameClass(bean.showText);
                }
            }
        });
        return convertView;
    }

    private static class ViewHolder {
        ImageView image;
    }

    /**
     * 重写原因 ViewPager在Android4.0上有兼容性错误
     * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
     * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
     * http://blog.csdn.net/guxiao1201/article/details/8818734
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
