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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.app.beans.FeedBackTipsDetailBean;
import com.prize.appcenter.R;

import java.util.ArrayList;

/**
 * Desc: 意见反馈-提示页面的adapter
 * <p>
 * Created by huangchangguo
 * Date:  2016/9/6 17:45
 */

public class FeedbackTipsListViewAdapter extends BaseAdapter {

    private Context mCtx;
    private ArrayList<FeedBackTipsDetailBean> datas = new ArrayList<>();

    public FeedbackTipsListViewAdapter(Context mCtx) {
        super();
        this.mCtx = mCtx;
    }

    @Override
    public int getCount() {

        if (datas.size() > 0) {
            return datas.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {

        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 添加livtview的数据
     */
    public void addData(ArrayList<FeedBackTipsDetailBean> data) {
        if (data != null) {
            datas.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 获得livtview的数据
     */
    public FeedBackTipsDetailBean getItemData(int position) {
        if (datas != null) {
            return datas.get(position);
        }
        return null;
    }

    /**
     * 获得livtview的数据
     */
    public ArrayList<FeedBackTipsDetailBean> getData() {
        if (datas != null) {
            return datas;
        }
        return null;
    }

    /**
     * 添加livtview的数据
     */
    public void setData(ArrayList<FeedBackTipsDetailBean> data) {
        if (data != null) {
            datas.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mCtx).inflate(
                    R.layout.activity_feedback_listview_item, null);
            viewHolder.tipsQuestion = (TextView) convertView
                    .findViewById(R.id.fedbck_common_tips_item);
            viewHolder.tipsNum = (TextView) convertView
                    .findViewById(R.id.fedbck_common_tips_num);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        FeedBackTipsDetailBean data = getItemData(position);
        viewHolder.tipsQuestion.setText(data.question);
        viewHolder.tipsNum.setText(position + 1 + "、");
        return convertView;
    }


    private class ViewHolder {
        public TextView tipsQuestion;
        public TextView tipsNum;
    }
}
