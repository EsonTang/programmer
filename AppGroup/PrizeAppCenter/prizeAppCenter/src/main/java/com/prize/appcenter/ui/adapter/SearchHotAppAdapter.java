/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.ui.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.HotKeyBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.util.UILimageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * 热搜应用adapter
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchHotAppAdapter extends BaseAdapter {
    private Activity activity;
    private List<HotKeyBean> list = new ArrayList<HotKeyBean>();

    public SearchHotAppAdapter(RootActivity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {

        return list.size();
    }

    @Override
    public HotKeyBean getItem(int position) {
        if (list.size() == 0) {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public void setList(List<HotKeyBean> list) {
        // this.list.clear();
        // notifyDataSetChanged();
        this.list = list;
        notifyDataSetChanged();
    }

//    public void clearAddAllDatas(List<HotKeyBean> datas) {
//        list.clear();
//        list.addAll(datas);
//        notifyDataSetChanged();
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(activity).inflate(
                    R.layout.item_search_hotapp, null);
            holder.textView = (TextView) convertView
                    .findViewById(R.id.name_app_Tv);
            holder.app_imageView = (ImageView) convertView
                    .findViewById(R.id.app_imageView);
            convertView.setTag(holder);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (list.size() == 0) {
            return convertView;
        }
        HotKeyBean bean = getItem(position);
        if (bean.ikey.equals("text") && !TextUtils.isEmpty(bean.searchWord)) {
            holder.app_imageView.clearAnimation();
            holder.app_imageView.setVisibility(View.GONE);
            holder.textView.setText(bean.searchWord);
            if (bean!=null&&!TextUtils.isEmpty(bean.colorValue)&&bean.colorValue.startsWith("#")) {
                holder.textView.setTextColor(Color.parseColor(bean.colorValue));
            }
        } else {
            holder.textView.setText(bean.searchWord);
            if (bean!=null&&!TextUtils.isEmpty(bean.colorValue)&&bean.colorValue.startsWith("#")) {
                holder.textView.setTextColor(Color.parseColor(bean.colorValue));
            }
            holder.app_imageView.setVisibility(View.VISIBLE);
            if (bean != null && bean.data != null && bean.data.app != null
                    && !TextUtils.isEmpty(bean.data.app.iconUrl)) {
                ImageLoader.getInstance().displayImage(bean.data.app.iconUrl,
                        holder.app_imageView, UILimageUtil.getUILoptions(),
                        null);
            } else {
                holder.app_imageView.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView textView;
        ImageView app_imageView;
    }
}
