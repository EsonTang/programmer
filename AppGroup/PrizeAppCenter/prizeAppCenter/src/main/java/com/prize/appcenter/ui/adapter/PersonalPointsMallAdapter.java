package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.prize.app.beans.ParentPointsMallItemBean;
import com.prize.appcenter.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc: 积分商城adapter
 * Created by huangchangguo
 * Date:  2016/8/19 17:24
 */

public class PersonalPointsMallAdapter extends BaseAdapter {
    private List<ParentPointsMallItemBean> parentitems = new ArrayList<ParentPointsMallItemBean>();
    private Context mContext;

    public PersonalPointsMallAdapter(Context context) {
        this.mContext = context;

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
    public void setData(List<ParentPointsMallItemBean> data) {
        if (data != null) {
            parentitems = data;
        }
        notifyDataSetChanged();
    }

    /**
     * 添加item
     */
    public void addData(ArrayList<ParentPointsMallItemBean> data) {
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
    public ParentPointsMallItemBean getItem(int position) {
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
        SubSecKillPointsMallAdapter secKillPointsMallAdapter;
        SubCommPointsMallAdapter subCommPointsMallAdapter;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_personal_points_mall_item, null);
            viewHolder = new ViewHolder();
            secKillPointsMallAdapter = new SubSecKillPointsMallAdapter(mContext);
            subCommPointsMallAdapter = new SubCommPointsMallAdapter(mContext);
            viewHolder.hot_cats_gv = (GridView) convertView
                    .findViewById(R.id.hot_cats_gv);
            viewHolder.title_Tv = (TextView) convertView
                    .findViewById(R.id.title_Tv);
            convertView.setTag(viewHolder);
            convertView.setTag(R.id.secKill_mall_items,secKillPointsMallAdapter);
            convertView.setTag(R.id.subComm_mall_items,subCommPointsMallAdapter);
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ParentPointsMallItemBean item = getItem(position);
        if (item == null)
            return convertView;
        viewHolder.title_Tv.setText(item.title);
        if (item.saleFlag == 1) {
            secKillPointsMallAdapter= (SubSecKillPointsMallAdapter) convertView.getTag(R.id.secKill_mall_items);
            viewHolder.hot_cats_gv.setAdapter(secKillPointsMallAdapter);
            secKillPointsMallAdapter.setData(item.goods);
        }else{
            subCommPointsMallAdapter= (SubCommPointsMallAdapter) convertView.getTag(R.id.subComm_mall_items);
            viewHolder.hot_cats_gv.setAdapter(subCommPointsMallAdapter);
            subCommPointsMallAdapter.setData(item.goods);
        }
        return convertView;
    }
//
//    //按钮点击抽奖功能，需求进详情页才抽奖,已不用
//
//    private void GotoLottery(final int position) {
//
//        PointsMallItemDataBean itemData = parentitems.get(position);
//        final int goodsId = itemData.id;
//        Person person = CommonUtils.queryUserPerson(mContext);
//        String userId = null;
//        if (person != null && person.getUserId() != null) {
//            userId = person.getUserId();
//        }
//        //如果账户ID为空，则点击登录
//        JLog.i("GotoLottery", "  goodsId:" + goodsId + "  userId:" + userId);
//        if (userId == null) {
//            //跳转到登录页
//            Intent intent = new Intent(mContext, LoginActivityNew.class);
//            mContext.startActivity(intent);
//            // UIUtils.gotoActivity(LoginActivityNew.class);
//            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right,
//                    R.anim.fade_out);
//            return;
//        }
//        final String finalUserId = userId;
//        //抽奖
//        if (itemData.type == 0) {
//            if (mLotteryDialog == null) {
//                mLotteryDialog = new PointsLotteryDialog(mContext, R.style.add_dialog);
//            }
//            mLotteryDialog.show();
//            mLotteryDialog.setContent("确定要使用" + itemData.points + "积分进行抽奖吗？");
//            mLotteryDialog.setSureBtn("确定");
//            mLotteryDialog.setmOnButtonClic(new PointsLotteryDialog.OnButtonClic() {
//                @Override
//                public void onClick(int which) {
//                    mLotteryDialog.dismiss();
//                    switch (which) {
//                        case 0:
//                            //取消
//                            break;
//                        case 1:
//                            //确定-请求网络得到结果
//                            PointsLotteryUtils.RequstPointsLottery(mContext, finalUserId, goodsId, new PointsLotteryUtils.ResultLinstener() {
//                                @Override
//                                public void getresult(String data, final AppsItemBean bean) {
//                                    if (data == null) {
//                                        ToastUtils.showToast("网络超时，请重试！");
//                                    } else {
//                                        //抽奖结果
//                                        PointsLotteryData lotteryData = new Gson().fromJson(data, PointsLotteryData.class);
//                                        //                                        Message msg = Message.obtain();
//                                        //                                        msg.obj = lotteryData;
//                                        //                                        msg.what = LOTTERY;
//                                        //                                        mHandler.sendMessage(msg);
//
//
//                                    }
//                                }
//                            });
//                            break;
//                    }
//                }
//            });
//        } else {
//            //兑换
//            PointsLotteryUtils.RequstPointsLottery(mContext, finalUserId, goodsId, new PointsLotteryUtils.ResultLinstener() {
//                @Override
//                public void getresult(String data, final AppsItemBean bean) {
//                    if (data == null) {
//                        ToastUtils.showToast("网络超时，请重试！");
//                    } else {
//                        //兑换结果
//                        PointsLotteryData lotteryData = new Gson().fromJson(data, PointsLotteryData.class);
//                        Message msg = Message.obtain();
//                        //                        msg.obj = lotteryData;
//                        //                        msg.arg1 = position;
//                        //                        msg.what = DOEXCHANGE;
//                        //                        mHandler.sendMessage(msg);
//                    }
//                }
//            });
//
//        }
//
//
//    }

    private static class ViewHolder {
        // 积分商城item图标
        GridView hot_cats_gv;
//        ScrollLineGridView hot_cats_gv;
        //积分商城item名字
        TextView title_Tv;

    }
}
