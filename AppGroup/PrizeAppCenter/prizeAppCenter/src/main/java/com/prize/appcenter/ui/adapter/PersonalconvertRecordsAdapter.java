package com.prize.appcenter.ui.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.net.datasource.base.ConvertRecordsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.PersonalConvertRecordsActivity;
import com.prize.appcenter.activity.PersonalPointsMallAddressActivity;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.CustomImageView;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * Desc: 积分商城-兑换记录
 * <p/>
 * Created by huangchangguo
 * Date:  2016/8/25 11:36
 */

public class PersonalconvertRecordsAdapter extends BaseAdapter {
    private PersonalConvertRecordsActivity mContext;
    private ArrayList<ConvertRecordsItemBean> items = new ArrayList<ConvertRecordsItemBean>();
    private HashSet<Integer> hashSet = new HashSet<Integer>();
    private OnClickListener mClickListener;
    private RelativeLayout.LayoutParams paraWrap;
    private RelativeLayout.LayoutParams paraWMatch;
    public PersonalconvertRecordsAdapter(PersonalConvertRecordsActivity context) {
        mContext = context;
        mClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() != null) {
                    ConvertRecordsItemBean bean = (ConvertRecordsItemBean) v.getTag();
                    CommonUtils.copyText(bean.copyText,mContext);
                    ToastUtils.showToast("复制成功");
                }

            }
        };
    }


    /**
     * 这是数据
     */
    public void setData(ArrayList<ConvertRecordsItemBean> data) {
        if (data != null) {
            items = data;
        }
        notifyDataSetChanged();
    }


    /**
     * 添加新游戏列表到已有集合中
     */
    public void addData(ArrayList<ConvertRecordsItemBean> data) {
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    /**
     * 清空列表
     */
    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 获得item
     */
    public ArrayList<ConvertRecordsItemBean> gesItemsData() {
        if (items != null) {
            return items;
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ConvertRecordsItemBean getItem(int position) {
        if (position < 0 || items.isEmpty() || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (mContext == null) {
            return convertView;
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.activity_personal_convert_records_listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.itenIcon = (CustomImageView) convertView
                    .findViewById(R.id.convert_records_content_iv);
            viewHolder.orderName = (TextView) convertView
                    .findViewById(R.id.convert_records_content_title);
            viewHolder.orderNum = (TextView) convertView
                    .findViewById(R.id.convert_records_top_orderNum);
            viewHolder.orderPoints = (TextView) convertView
                    .findViewById(R.id.convert_records_content_point);
            viewHolder.orderStatus = (TextView) convertView
                    .findViewById(R.id.convert_records_top_orderStatus);
            viewHolder.orderAccount = (TextView) convertView
                    .findViewById(R.id.convert_records_into_account_tv);
            viewHolder.orderAddress = (TextView) convertView
                    .findViewById(R.id.convert_records_to_setaddress_tv);
            viewHolder.orderAddressAdd = (TextView) convertView
                    .findViewById(R.id.convert_records_to_setaddressadd_tv);
            viewHolder.copy_Tv = (TextView) convertView
                    .findViewById(R.id.copy_Tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ConvertRecordsItemBean orderBean = getItem(position);

        if (orderBean != null) {

            if (orderBean.istatus == 0) {
                viewHolder.orderStatus.setText("处理中");
            } else {
                viewHolder.orderStatus.setText("已完成");
            }

            //图标
            if (orderBean.goods != null && orderBean.goods.iconUrl != null) {
                ImageLoader.getInstance().displayImage(orderBean.goods.iconUrl,
                        viewHolder.itenIcon, UILimageUtil.getUILoptions(), null);
            } else {
                viewHolder.itenIcon.setImageResource(R.drawable.points_mall_item_def_icon);
            }

            //订单号
            if (orderBean.id != 0) {
                viewHolder.orderNum.setText("订单号：" + orderBean.id);
            }
            //积分
            if (orderBean.points != 0) {
                viewHolder.orderPoints.setText(orderBean.points + "积分");
            }
            //名字
            if (orderBean.goods != null && orderBean.goods.title != null) {
                viewHolder.orderName.setText(orderBean.goods.title);
            }

            paraWrap= (RelativeLayout.LayoutParams) viewHolder.orderAccount.getLayoutParams();
            paraWrap.width= RelativeLayout.LayoutParams.WRAP_CONTENT;
            //设置发放的账户状态,不需要邮寄
            if (orderBean.needPost == 0 || hashSet.contains(orderBean.id)) {
                viewHolder.orderAddress.setVisibility(View.GONE);
                viewHolder.orderAddressAdd.setVisibility(View.GONE);

                if (orderBean.result == 0 || orderBean.result == 1) {
                    viewHolder.orderAccount.setTextColor(mContext.getResources().getColor(R.color.text_color_404040));
                    if (!TextUtils.isEmpty(orderBean.copyText)) {
                        viewHolder.copy_Tv.setVisibility(View.VISIBLE);
                        int index = orderBean.mark.indexOf(orderBean.copyText);
                        SpannableStringBuilder ssb = new SpannableStringBuilder(orderBean.mark);
                        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#fa514f")), index, orderBean.copyText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        viewHolder.orderAccount.setText(ssb);
                        viewHolder.copy_Tv.setOnClickListener(mClickListener);
                        viewHolder.copy_Tv.setTag(orderBean);
                    } else {
                        viewHolder.orderAccount.setText(orderBean.mark);
                        viewHolder.copy_Tv.setVisibility(View.GONE);
                        viewHolder.orderAccount.setGravity(Gravity.LEFT);
                        if(orderBean.goods!=null&&orderBean.goods.type==0){//抽奖
                            viewHolder.orderAccount.setTextColor(Color.parseColor("#737373"));
                            viewHolder.orderAccount.setGravity(Gravity.CENTER);
                            paraWMatch=(RelativeLayout.LayoutParams)viewHolder.orderAccount.getLayoutParams();
                            paraWMatch.width= RelativeLayout.LayoutParams.MATCH_PARENT;
                        }
                    }
                } else {
//                    viewHolder.orderAccount.setTextColor(mContext.getResources().getColor(R.color.text_color_fa514f));
                    viewHolder.orderAccount.setTextColor(mContext.getResources().getColor(R.color.text_color_404040));
                    if (!TextUtils.isEmpty(orderBean.copyText)) {
                        viewHolder.copy_Tv.setVisibility(View.VISIBLE);
                        int index = orderBean.mark.indexOf(orderBean.copyText);
                        SpannableStringBuilder ssb = new SpannableStringBuilder(orderBean.mark);
                        ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#fa514f")), index, index + orderBean.copyText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        viewHolder.orderAccount.setText(ssb);
                        viewHolder.copy_Tv.setOnClickListener(mClickListener);
                        viewHolder.copy_Tv.setTag(orderBean);
                    } else {
                        viewHolder.copy_Tv.setVisibility(View.GONE);
                        viewHolder.orderAccount.setText(orderBean.mark);
                        viewHolder.orderAccount.setGravity(Gravity.LEFT);
                        if(orderBean.goods!=null&&orderBean.goods.type==0){//抽奖
                            viewHolder.orderAccount.setTextColor(Color.parseColor("#737373"));
                            viewHolder.orderAccount.setGravity(Gravity.CENTER);
                            paraWMatch=(RelativeLayout.LayoutParams)viewHolder.orderAccount.getLayoutParams();
                            paraWMatch.width= RelativeLayout.LayoutParams.MATCH_PARENT;
                        }
                    }
                }

            } else {
                viewHolder.orderAccount.setTextColor(mContext.getResources().getColor(R.color.text_color_404040));

                if (!TextUtils.isEmpty(orderBean.copyText)) {
                    viewHolder.copy_Tv.setVisibility(View.VISIBLE);
                    int index = orderBean.mark.indexOf(orderBean.copyText);
                    SpannableStringBuilder ssb = new SpannableStringBuilder(orderBean.mark);
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#fa514f")), index, index + orderBean.copyText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    viewHolder.orderAccount.setText(ssb);
                    viewHolder.copy_Tv.setOnClickListener(mClickListener);
                    viewHolder.copy_Tv.setTag(orderBean);
                } else {
                    viewHolder.orderAccount.setText(orderBean.mark);
                    viewHolder.copy_Tv.setVisibility(View.GONE);
                    viewHolder.orderAccount.setGravity(Gravity.LEFT);
                    if(orderBean.goods!=null&&orderBean.goods.type==0){//抽奖
                        viewHolder.orderAccount.setTextColor(Color.parseColor("#737373"));
                        viewHolder.orderAccount.setGravity(Gravity.CENTER);
                        paraWMatch=(RelativeLayout.LayoutParams)viewHolder.orderAccount.getLayoutParams();
                        paraWMatch.width= RelativeLayout.LayoutParams.MATCH_PARENT;
                    }
                }
                viewHolder.orderAddress.setVisibility(View.GONE);
                viewHolder.orderAddressAdd.setVisibility(View.GONE);

                if (orderBean.result == 0 && orderBean.addressId == 0) {
                    //需要邮寄
                    viewHolder.orderAddress.setVisibility(View.VISIBLE);
                    viewHolder.orderAddressAdd.setVisibility(View.VISIBLE);
                    // viewHolder.orderAddressAdd.setText(",请先补充");
                    //下划线
                    viewHolder.orderAddress.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                    //抗锯齿
                    viewHolder.orderAddress.getPaint().setAntiAlias(true);
                    viewHolder.orderAddress.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转到地址填写
                            Intent intent = new Intent(mContext
                                    , PersonalPointsMallAddressActivity.class);
                            intent.putExtra("orderId", orderBean.id + "");
                            // mContext.startActivity(intent);
                            mContext.startActivityForResult(intent, 1);
                        }
                    });
                }
            }
        }

        return convertView;
    }

    public void add2Set(Integer aog0) {
        hashSet.add(aog0);
        notifyDataSetChanged();
    }

    public void clearSet() {
        hashSet.clear();
        hashSet = null;
    }

    static class ViewHolder {
        // 兑换的item的图标
        CustomImageView itenIcon;
        //订单编号
        TextView orderNum;
        // 订单状态
        TextView orderStatus;
        // 订单名字
        TextView orderName;
        // 兑换消耗的积分
        TextView orderPoints;
        //发放账户状态
        TextView orderAccount;
        //填写地址
        TextView orderAddress;
        TextView orderAddressAdd;
        TextView copy_Tv;
    }

    //public void onItemClick(int position) {
    //        if (position < 0 || position >= items.size()) {
    //            return;
    //        }
    //        ConvertRecordsItemBean item = items.get(position);
    //        if (null != item) {
    //            // 跳转到详细界面
    //            UIUtils.gotoAppDetail(item.id);
    //        }
    //    }
    //
    //
    //    public interface OnClickCallBack {
    //        public void onClickItem(ImageView view);
    //    }
    //
    //    public OnClickCallBack onClickCallBack;
    //
    //    public void setOnClickCallBackListener(OnClickCallBack onClickCallBack) {
    //        this.onClickCallBack = onClickCallBack;
    //    }
}
