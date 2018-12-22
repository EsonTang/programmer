/*******************************************
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.beans.Person;
import com.prize.app.beans.PointsMallAddressBean;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarPointsActivity;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.custmerxutils.XExtends;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Title:    个人中心-积分商城
 * Desc:    2.0积分系统-积分商城
 * Version:    应用市场2.0
 * Created by huangchangguo
 * on   2016/8/15  15:36
 * <p/>
 * Update Description:  更新描述
 * Updater:   更新者
 * Update Time:   更新时间
 */

public class PersonalPointsMallAddressActivity extends ActionBarPointsActivity {
    private final int GETRESQUESTDATA = 1;
    private final String TAG = "PersonalPointsMallAddressActivity";
    private RelativeLayout mDefalutRlyt;
    private LinearLayout mAddressRlyt;
    private TextView mSubmitBtn;
    private EditText mFullAddress, mCodePostEt, mPhoneEt, mConsigneeEt;
    private String mOrderNum;
    private String mUserId;
    private Callback.Cancelable cancelable;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int key = msg.what;
            switch (key) {
                //获得请求的数据
                case GETRESQUESTDATA:
                    PointsMallAddressBean mAddressData = (PointsMallAddressBean) msg.obj;
                    if (mAddressData != null) {
                        mConsigneeEt.setText(String.valueOf(mAddressData.name));
                        //光标移动到末尾
                        mConsigneeEt.setSelection(mAddressData.name.length());

                        mPhoneEt.setText(String.valueOf(mAddressData.tel));
                        mCodePostEt.setText(String.valueOf(mAddressData.postcode));
                        mFullAddress.setText(String.valueOf(mAddressData.address));
                        if (mUserId == null) {
                            mUserId = String.valueOf(mAddressData.accountId);
                        }

                    } else {
                        //如果返回的地址为空，则弹出小键盘提示用户手动输入
                        initData();
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_points_mall_fill_address);
        WindowMangerUtils.changeStatus(getWindow());
        mOrderNum = getIntent().getStringExtra("orderId");
        rule.setVisibility(View.GONE);
        setTitle(getString(R.string.fillin_delivery_address));
        initView();
        Person person = CommonUtils.queryUserPerson(this);
        if (person != null && person.getUserId() != null) {
            mUserId = person.getUserId();
        }
        requestData();
    }

    private void initView() {

        mConsigneeEt = (EditText) findViewById(R.id.points_mall_address_user_et);
        mSubmitBtn = (TextView) findViewById(R.id.points_mall_address_submit_btn);
        mPhoneEt = (EditText) findViewById(R.id.points_mall_address_phone_et);
        mFullAddress = (EditText) findViewById(R.id.points_mall_address_full_et);
        mCodePostEt = (EditText) findViewById(R.id.points_mall_address_code_post_et);
        //加载失败默认页
        mAddressRlyt = (LinearLayout) findViewById(R.id.points_mall_address_llyt);
        mDefalutRlyt = (RelativeLayout) findViewById(R.id.points_mall_address_defalutRlyt);
        ImageView mDefalutRImage = (ImageView) findViewById(R.id.points_mall_address_defaultImg);

        //提交
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitData();
            }
        });

        //失败页面，点击重新加载
        mDefalutRImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWaiting();
                requestData();
            }
        });

    }

    private void initData() {

        //弹出键盘
        mConsigneeEt.setFocusable(true);
        mConsigneeEt.setFocusableInTouchMode(true);
        mConsigneeEt.requestFocus();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mConsigneeEt
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mConsigneeEt, 0);
            }
        }, 300);
    }

    /**
     * desc:请求数据
     */
    private void requestData() {

        RequestParams requestParams = new RequestParams(Constants.GIS_URL + "/point/getaddress");
        requestParams.addBodyParameter("accountId", mUserId);
        // MD5加密校验key
        String sign = Verification.getInstance().getSign(requestParams.getBodyParams());
        requestParams.addBodyParameter("sign", sign);

        cancelable= XExtends.http().post(requestParams, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                hideWaiting();
                try {
                    isShowErrorView(false);
                    String response = new JSONObject(result).getString("data");
                    String addressJson = new JSONObject(response).getString("address");
                    PointsMallAddressBean addressBean;
                    addressBean = new Gson().fromJson(addressJson, PointsMallAddressBean.class);
                    Message msg = Message.obtain();
                    msg.what = GETRESQUESTDATA;
                    msg.obj = addressBean;
                    mHandler.sendMessage(msg);

                } catch (JSONException e) {
                    isShowErrorView(true);
                    e.printStackTrace();

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i(TAG, "onFailure=" + ex.getMessage());
                hideWaiting();
                isShowErrorView(true);

            }
        });

    }

    /**
     * desc:提交数据到服务器
     */
    private void submitData() {
        //获取电话号码
        String mConsigneeEtText = mConsigneeEt.getText().toString();
        String  mPhoneText = mPhoneEt.getText().toString();
        String  mPostEtText = mCodePostEt.getText().toString();
        String mAddressText = mFullAddress.getText().toString();
        if (TextUtils.isEmpty(mConsigneeEtText)) {
            ToastUtils.showToast("收件人不能为空");
            return;
        }
        if (TextUtils.isEmpty(mPhoneText)) {
            ToastUtils.showToast("电话不能为空");
            return;
        }
        if (TextUtils.isEmpty(mAddressText)) {
            ToastUtils.showToast("地址不能为空");
            return;
        }

        //设置提交按钮不可用
        isAvailableSubmitBtn(false);
        RequestParams requestParams = new RequestParams(Constants.GIS_URL + "/point/setaddress");

        requestParams.addBodyParameter("orderId", String.valueOf(mOrderNum));
        requestParams.addBodyParameter("accountId", String.valueOf(mUserId));
        requestParams.addBodyParameter("name", String.valueOf(mConsigneeEtText));
        requestParams.addBodyParameter("tel", String.valueOf(mPhoneText));
        requestParams.addBodyParameter("postcode", String.valueOf(mPostEtText));
        requestParams.addBodyParameter("address", String.valueOf(mAddressText));

        String sign = Verification.getInstance().getSign(requestParams.getBodyParams());
        requestParams.addBodyParameter("sign", sign);

        XExtends.http().post(requestParams, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {

                try {
                    ToastUtils.showToast("提交成功！");
                    Intent intent = new Intent();
                    intent.putExtra("orderId",mOrderNum);
                    setResult(2,intent);
                    finish();
                } catch (Exception e) {
                    ToastUtils.showToast("提交失败，请重试！");
                    isAvailableSubmitBtn(true);
                    e.printStackTrace();

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastUtils.showToast("提交失败，请重试！");
                isAvailableSubmitBtn(true);
            }

        });

    }


    /**
     * 设置提交按钮是否可用
     */
    private void isAvailableSubmitBtn(boolean isAvailable) {
        if (isAvailable) {
            mSubmitBtn.setEnabled(true);
        } else {
            mSubmitBtn.setEnabled(false);
        }
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



    public void isShowErrorView(boolean isShowErrorView) {
        if (isShowErrorView) {
            mAddressRlyt.setVisibility(View.GONE);
            mDefalutRlyt.setVisibility(View.VISIBLE);
        } else {
            mDefalutRlyt.setVisibility(View.GONE);
            mAddressRlyt.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cancelable !=null){
            cancelable.cancel();
        }
        if(mHandler !=null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

}