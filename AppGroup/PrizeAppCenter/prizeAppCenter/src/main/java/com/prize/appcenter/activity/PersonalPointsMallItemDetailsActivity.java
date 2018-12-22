/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
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
package com.prize.appcenter.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.Person;
import com.prize.app.beans.PointsMallItemDataBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.PointsLotteryData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarPointsActivity;
import com.prize.appcenter.ui.dialog.PointsLotteryDialog;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/**
 * Title:    个人中心-积分商城-奖品详情
 * Desc:    2.0积分系统-积分商城
 * Version:    应用市场2.0
 * Created by huangchangguo
 * on   2016/8/15  15:36
 * <p/>
 * Update Description:  更新描述
 * Updater:   更新者
 * Update Time:   更新时间
 */

public class PersonalPointsMallItemDetailsActivity extends ActionBarPointsActivity {
    private final String TAG = "PersonalPointsMallItemDetailsActivity";
    private TextView mRule, mDesc, mWay;
    private TextView mDetailsBtn, mDetailsPoint;
    private PointsMallItemDataBean mData;
    private ImageView mDetailBanner;
    private PointsLotteryDialog mLotteryDialog;
    private final int LOTTERY = 1;
    private final int DOEXCHANGE = 2;
    private Context mContext;
    private Callback.Cancelable cancelable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_mall_item_details);
        WindowMangerUtils.changeStatus(getWindow());
        mData = (PointsMallItemDataBean) getIntent().getSerializableExtra("ItemData");
        mContext = this;
        setTitle(getString(R.string.prize_detail));
        initView();
        initListener();
        initData();

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int key = msg.what;
            switch (key) {
                //抽奖结果
                case LOTTERY:
                    mDetailsBtn.setText(getString(R.string.luckdraw));
                    mDetailsBtn.setEnabled(true);
                    final PointsLotteryData lotteryData = (PointsLotteryData) msg.obj;
                    String lotterymsg = lotteryData.msg;
                    final int lotteryresult = lotteryData.result;
                    final String lotteryOrderId = lotteryData.orderId;
                    //未中奖
                    //积分不足或则售罄，返回-2，直接弹土司
                    if (lotteryresult == -2) {
                        if (!TextUtils.isEmpty(lotterymsg) && getString(R.string.points_no_enough).equals(lotterymsg)) {
                            if (mLotteryDialog == null) {
                                mLotteryDialog = new PointsLotteryDialog(mContext, R.style.add_dialog);
                                mLotteryDialog.setCanceledOnTouchOutside(false);
                            }

                            mLotteryDialog.show();
                            mLotteryDialog.setSureBtn(getString(R.string.earn_points));
                            mLotteryDialog.setContent(getString(R.string.points_no_enough_for_lottery_earnpoints));

                            mLotteryDialog.setmOnButtonClic(new PointsLotteryDialog.OnButtonClic() {
                                @Override
                                public void onClick(int which) {
                                    mLotteryDialog.dismiss();
                                    switch (which) {
                                        case 0:
                                            //取消
                                            break;
                                        case 1:
                                            //跳转到赚取积分界面
                                            Intent intent = new Intent(PersonalPointsMallItemDetailsActivity.this
                                                    , PersonalEarnPointsActivity.class);
                                            startActivity(intent);
                                            break;
                                    }
                                }
                            });

                            mDetailsBtn.setEnabled(true);
                            mDetailsBtn.setText(getString(R.string.luckdraw));
                        } else {
                            ToastUtils.showToast(lotterymsg);
                            mDetailsBtn.setEnabled(true);
                            mDetailsBtn.setText(getString(R.string.luckdraw));
                        }

                        return;
                    }
                    if (mLotteryDialog == null) {
                        mLotteryDialog = new PointsLotteryDialog(mContext, R.style.add_dialog);
                        mLotteryDialog.setCanceledOnTouchOutside(false);
                    }

                    mLotteryDialog.show();

                    if (lotteryresult == 1 || lotteryresult == 0) {
                        //虚拟物品
                        mLotteryDialog.setSureBtn(getString(R.string.confirm));
                    } else if (lotteryresult == 2) {
                        //实物
                        mLotteryDialog.setSureBtn(getString(R.string.fillin_address));
                    }
                    mLotteryDialog.setContent(lotterymsg);

                    mLotteryDialog.setmOnButtonClic(new PointsLotteryDialog.OnButtonClic() {
                        @Override
                        public void onClick(int which) {
                            mLotteryDialog.dismiss();
                            switch (which) {
                                case 0:
                                    mDetailsBtn.setEnabled(true);
                                    mDetailsBtn.setText(getString(R.string.luckdraw));
                                    //取消
                                    break;
                                case 1:
                                    mDetailsBtn.setEnabled(true);
                                    mDetailsBtn.setText(getString(R.string.luckdraw));
                                    //虚拟物品
                                    if (lotteryresult == 2) {
                                        //实物
                                        //跳转到地址填写界面
                                        Intent intent = new Intent(PersonalPointsMallItemDetailsActivity.this
                                                , PersonalPointsMallAddressActivity.class);
                                        intent.putExtra("orderId", lotteryOrderId);
                                        startActivity(intent);
                                    }
                                    break;
                            }
                        }
                    });

                    break;
                //兑换结果
                case DOEXCHANGE:
                    PointsLotteryData doexchangeData = (PointsLotteryData) msg.obj;
                    String doexchangemsg = doexchangeData.msg;
                    final String doexchangeorderId = doexchangeData.orderId;
                    final int doexchangeResult = doexchangeData.result;
                    //  ToastUtils.showToast(doexchangemsg);
                    //积分不足或或不可用，服务器返回-2，直接弹土司
                    if (doexchangeResult == -2) {
                        if (!TextUtils.isEmpty(doexchangemsg) && getString(R.string.points_no_enough).equals(doexchangemsg)) {
                            if (mLotteryDialog == null) {
                                mLotteryDialog = new PointsLotteryDialog(mContext, R.style.add_dialog);
                                mLotteryDialog.setCanceledOnTouchOutside(false);
                            }

                            mLotteryDialog.show();
                            mLotteryDialog.setSureBtn(getString(R.string.earn_points));
                            mLotteryDialog.setContent(getString(R.string.points_no_enough_earnpoints));

                            mLotteryDialog.setmOnButtonClic(new PointsLotteryDialog.OnButtonClic() {
                                @Override
                                public void onClick(int which) {
                                    mLotteryDialog.dismiss();
                                    switch (which) {
                                        case 0:
                                            //取消
                                            break;
                                        case 1:
                                            //跳转到赚取积分界面
                                            Intent intent = new Intent(PersonalPointsMallItemDetailsActivity.this
                                                    , PersonalEarnPointsActivity.class);
                                            startActivity(intent);
                                            break;
                                    }
                                }
                            });

                            mDetailsBtn.setEnabled(true);
                            mDetailsBtn.setText(getString(R.string.exchange));
                        } else {
                            ToastUtils.showToast(doexchangemsg);
                            mDetailsBtn.setEnabled(true);
                            mDetailsBtn.setText(getString(R.string.exchange));
                        }
                        return;
                    }
                    if (mLotteryDialog == null) {
                        mLotteryDialog = new PointsLotteryDialog(mContext, R.style.add_dialog);
                        mLotteryDialog.setCanceledOnTouchOutside(false);
                    }
                    mLotteryDialog.show();
                    mLotteryDialog.setContent(doexchangemsg);
                    if (!TextUtils.isEmpty(doexchangemsg) && doexchangemsg.length() > 20) {
                        WindowManager.LayoutParams params = mLotteryDialog.getWindow().getAttributes();
                        WindowManager m = mLotteryDialog.getWindow().getWindowManager();
                        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
                        params.width = (int) (d.getWidth() * 0.9);    //宽度设置为屏幕的0.5
                        //设置生效
                        mLotteryDialog.getWindow().setAttributes(params);
                    }
                    if (doexchangeResult == 1) {
                        //虚拟物品
                        mLotteryDialog.setSureBtn(getString(R.string.confirm));
                    } else if (doexchangeResult == 2) {
                        //实物
                        mLotteryDialog.setSureBtn(getString(R.string.fillin_address));
                    }
                    mLotteryDialog.setmOnButtonClic(new PointsLotteryDialog.OnButtonClic() {
                        @Override
                        public void onClick(int which) {
                            mLotteryDialog.dismiss();
                            switch (which) {
                                case 0:
                                    //取消
                                    mDetailsBtn.setEnabled(true);
                                    mDetailsBtn.setText(getString(R.string.exchange));
                                    break;
                                case 1:
                                    //跳转到地址填写界面
                                    mDetailsBtn.setEnabled(true);
                                    mDetailsBtn.setText(getString(R.string.exchange));
                                    if (doexchangeResult == 2) {
                                        //实物
                                        //跳转到地址填写界面
                                        Intent intent = new Intent(PersonalPointsMallItemDetailsActivity.this
                                                , PersonalPointsMallAddressActivity.class);
                                        intent.putExtra("orderId", doexchangeorderId);
                                        startActivity(intent);
                                    }
                                    break;
                            }
                        }
                    });

                    break;
            }
        }
    };

    private void initView() {

        mDetailBanner = (ImageView) findViewById(R.id.points_mall__banner_iv);
        mRule = (TextView) findViewById(R.id.points_mall_details_rule_tv);
        mDesc = (TextView) findViewById(R.id.points_mall_details_desc_tv);
        mWay = (TextView) findViewById(R.id.points_mall_details_way_tv);
        mDetailsBtn = (TextView) findViewById(R.id.points_mall_item_details_btn);
        mDetailsPoint = (TextView) findViewById(R.id.points_mall_item_details_point);

    }

    private void initData() {
        if (mData != null) {
            if (mData.bannerUrl != null) {
                ImageLoader.getInstance().displayImage(mData.bannerUrl,
                        mDetailBanner, UILimageUtil.getUILoptions(R.drawable.points_mall_item_details_header_banner), null);
            }
            if (mData.rule != null) {
                mRule.setText(mData.rule);
            } else {
                mRule.setText("");
            }

            if (mData.description != null) {
                mDesc.setText(mData.description);
            } else {
                mDesc.setText("");
            }

            if (mData.introduction != null) {
                mWay.setText(mData.introduction);
            } else {
                mWay.setText("");
            }
            //设置积分
            if (mData.saleFlag == 1) {//秒杀商品
                mDetailsPoint.setText(getString(R.string.point_value_string, mData.salePoints));
            } else {
                mDetailsPoint.setText(getString(R.string.point_value_string, mData.points));
            }
            //设置按钮功能
            if (mData.type == 0) {
                mDetailsBtn.setText(R.string.luckdraw);
            } else {
                mDetailsBtn.setText(R.string.exchange);
            }
        }

    }

    private void initListener() {


        mDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GotoLottery();
            }
        });
    }

    private void GotoLottery() {

        if (mData == null) {
            return;
        }
        final int goodsId = mData.id;
        Person person = CommonUtils.queryUserPerson(mContext);
        String userId = null;
        if (person != null && person.getUserId() != null) {
            userId = person.getUserId();
        }
        //如果账户ID为空，则点击登录
        if (userId == null) {
            //跳转到登录页
            Intent intent = new Intent(mContext, LoginActivityNew.class);
            mContext.startActivity(intent);
            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.fade_out);
            return;
        }
        final String finalUserId = userId;
        //设置不可重复点击
        mDetailsBtn.setEnabled(false);
        //抽奖
        if (mData.type == 0) {
            mDetailsBtn.setText(getString(R.string.luckdrawing));
            requestData(finalUserId, goodsId, new ResultLinstener() {
                @Override
                public void getresult(String data) {

                    if (data == null) {
                        mDetailsBtn.setEnabled(true);
                        mDetailsBtn.setText(getString(R.string.luckdraw));
                        //状态 1
                        ToastUtils.showToast(R.string.net_error);
                    } else {
                        //抽奖结果
                        PointsLotteryData lotteryData = new Gson().fromJson(data, PointsLotteryData.class);
                        Message msg = Message.obtain();
                        msg.obj = lotteryData;
                        msg.what = LOTTERY;
                        mHandler.sendMessageDelayed(msg, 1000);
                    }
                }
            });

        } else {
            mDetailsBtn.setText(getString(R.string.exchangeing));
            requestData(finalUserId, goodsId, new ResultLinstener() {
                @Override
                public void getresult(String data) {

                    if (data == null) {
                        mDetailsBtn.setText(getString(R.string.exchange));
                        mDetailsBtn.setEnabled(true);
                        ToastUtils.showToast(R.string.net_error);
                    } else {
                        //兑换结果
                        PointsLotteryData lotteryData = new Gson().fromJson(data, PointsLotteryData.class);
                        Message msg = Message.obtain();
                        msg.obj = lotteryData;
                        msg.what = DOEXCHANGE;
                        mHandler.sendMessageDelayed(msg, 500);
                    }
                }
            });

        }


    }

    private interface ResultLinstener {

        void getresult(String data);
    }

    //请求网络
    private void requestData(String userId, int goodsId, final ResultLinstener resultLinstener) {
        RequestParams    mParams = new RequestParams(Constants.GIS_URL + "/point/doexchange");
        mParams.addBodyParameter("accountId", String.valueOf(userId));
        mParams.addBodyParameter("goodsId", String.valueOf(goodsId));
        cancelable = XExtends.http().post(mParams, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String data = new JSONObject(result).getString("data");
                    resultLinstener.getresult(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                    resultLinstener.getresult(null);
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                resultLinstener.getresult(null);

            }
        });
    }

    @Override
    public String getActivityName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancelable != null) {
            cancelable.cancel();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}