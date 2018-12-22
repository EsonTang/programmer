package com.prize.prizenavigation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.prize.prizenavigation.NavigationApplication;
import com.prize.prizenavigation.R;
import com.prize.prizenavigation.bean.NaviDatas;
import com.prize.prizenavigation.utils.UILimageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by liukun on 2017/3/6.
 */
public class TitleListAdapter extends BaseAdapter {
    /**list数据源*/
    private List<NaviDatas.ListBean> datas = new ArrayList<NaviDatas.ListBean>();

    public TitleListAdapter(List<NaviDatas.ListBean> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView=LayoutInflater.from(NavigationApplication.getContext()).inflate(R.layout.fragment_list_item,null);
            viewHolder=new ViewHolder();
            viewHolder.imageView= (SimpleDraweeView) convertView.findViewById(R.id.fra_list_iv);
            viewHolder.textView= (TextView) convertView.findViewById(R.id.fra_list_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder= (ViewHolder) convertView.getTag();
        }
        String listUrl=datas.get(position).getList_icon_url();
//        String tag = (String) viewHolder.imageView.getTag();
//        if(datas!=null && listUrl!=null&&(tag==null||!tag.equals(listUrl))){
//            ImageLoader.getInstance().displayImage(listUrl,viewHolder.imageView,
//                    UILimageUtil.getNaviFragmentSmallCacheUILoptions(), UILimageUtil.setTagHolder(viewHolder.imageView,listUrl));
//        }
//        UILimageUtil.displayImg(NavigationApplication.getContext(),listUrl,viewHolder.imageView);
        UILimageUtil.displayImg(listUrl,viewHolder.imageView);
        viewHolder.textView.setText(datas.get(position).getTitle());
        return convertView;
    }

    public static class ViewHolder{
        private SimpleDraweeView imageView;
        private TextView textView;
    }
}
