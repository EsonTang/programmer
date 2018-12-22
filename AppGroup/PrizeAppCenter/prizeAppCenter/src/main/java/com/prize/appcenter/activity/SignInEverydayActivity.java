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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.SignInEveryDayData;
import com.prize.appcenter.fragment.PromptDialogFragment;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.SignInRuleAdapter;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.widget.ScrollListView;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/**
 * *
 * 每日签到
 *
 * @author 聂礼刚
 * @version V1.0
 */
public class SignInEverydayActivity extends ActionBarNoTabActivity {
    private TextView mSignInText;
    private TextView mGotoPointsMall;
    private ImageView mSignInRule1;
    private ImageView mSignInRule2;
    private ImageView mSignInRule3;
    private ImageView mSignInBt;
    private ScrollListView mRules;
    private Callback.Cancelable mCancelable;
    private Callback.Cancelable mCancelable2;
    private SignInEveryDayData data;
    private SignInEveryDayData respData;  //点击签到后返回数据
    private Person mPerson;
    private PromptDialogFragment mPromptDialogFragment;

    private IntentFilter mIntentFilter;
    private LocalBroadcastManager mLocalBroadcastManager;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_sign_in_everyday);
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.sign_in_everyday);
        findViewById();
        setListener();
        mPerson = CommonUtils.queryUserPerson(this);
        requestData();
    }

    private void findViewById() {
        mSignInText = (TextView) findViewById(R.id.sign_text_tv);
        mGotoPointsMall = (TextView) findViewById(R.id.goto_points_mall_tv);
        mSignInRule1 = (ImageView) findViewById(R.id.score_description_1);
        mSignInRule2 = (ImageView) findViewById(R.id.score_description_2);
        mSignInRule3 = (ImageView) findViewById(R.id.score_description_3);
        mSignInBt = (ImageView) findViewById(R.id.sign_in_bt);
        mRules = (ScrollListView) findViewById(R.id.rules_lv);
    }

    private void setListener() {
        mGotoPointsMall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unRegisterLocalBroadcastReceiver();
                Intent intent = new Intent(SignInEverydayActivity.this, PersonalPointsMallActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
            }
        });
    }

    @Override
    public String getActivityName() {
        return "SignInEverydayActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    private void requestData() {
        showWaiting();
        RequestParams params = new RequestParams(Constants.GIS_URL + "/point/signdetail");
        if (mPerson != null && !TextUtils.isEmpty(mPerson.getUserId())) {
            params.addBodyParameter("accountId", mPerson.getUserId());
            params.addBodyParameter("tel", mPerson.getPhone());
        }else {
            registerLocalBroadcastReceiver(); //注册登录广播
        }
        mCancelable = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        data = new Gson().fromJson(res, SignInEveryDayData.class);

                        if (data != null) {
                            if (data.details.images.length > 0) {
                                ImageLoader.getInstance().displayImage(data.details.images[0],
                                        mSignInRule1, UILimageUtil.getNoLoadLoptions(), null);
                                if (data.details.images.length > 1) {
                                    ImageLoader.getInstance().displayImage(data.details.images[1],
                                            mSignInRule2, UILimageUtil.getNoLoadLoptions(), null);
                                }
                                if (data.details.images.length > 2) {
                                    ImageLoader.getInstance().displayImage(data.details.images[2],
                                            mSignInRule3, UILimageUtil.getNoLoadLoptions(), null);
                                }
                            }

                            if (data.details.rules.length > 0) {
                                SignInRuleAdapter adapter = new SignInRuleAdapter(data.details.rules, SignInEverydayActivity.this);
                                mRules.setAdapter(adapter);
                                mRules.setFocusable(false);
                            }

                            if (data.details.resp.status == 1) {
                                mSignInBt.setClickable(false);
                                mSignInBt.setBackgroundResource(R.drawable.sign_in_signed);
                            } else {
                                mSignInBt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (mPerson != null && !TextUtils.isEmpty(mPerson.getUserId())) {
                                            mSignInBt.setClickable(false);
                                            mSignInBt.setBackgroundResource(R.drawable.btn_signin_press);
                                            requestSignInData();
                                        } else {
                                            Intent intent = new Intent(SignInEverydayActivity.this, LoginActivityNew.class);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.fade_in,
                                                    R.anim.fade_out);
                                        }
                                    }
                                });
                            }

                            mSignInText.setText(data.details.resp.text);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }

                    });
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                loadingFailed(new ReloadFunction() {

                    @Override
                    public void reload() {
                        requestData();
                    }

                });
            }

        });

    }

    private void requestSignInData() {
        if(mPerson==null)
            return;
        RequestParams params = new RequestParams(Constants.GIS_URL + "/point/signin");
        params.addBodyParameter("accountId", mPerson.getUserId());
        params.addBodyParameter("tel", mPerson.getPhone());
        params.addBodyParameter("stamp", System.currentTimeMillis() + "");

        // MD5加密校验key
        String sign = Verification.getInstance().getSign(params.getBodyParams());
        params.addBodyParameter("sign", sign);
        mCancelable2 = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    String msg = o.getString("msg");
                    if (0 == code) {
                        String resp = o.getString("data");
                        respData = new Gson().fromJson(resp, SignInEveryDayData.class);

                        if (!TextUtils.isEmpty(msg)) {

                            if (mPromptDialogFragment == null || !mPromptDialogFragment.isAdded()) {
                                mPromptDialogFragment = PromptDialogFragment.newInstance(getString(R.string.tip),
                                        msg,
                                        getString(R.string.earn_points),
                                        getString(R.string.cancel),
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(SignInEverydayActivity.this, PersonalEarnPointsActivity.class);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.fade_in,
                                                        R.anim.fade_out);
                                            }
                                        });
                            }

                            if (mPromptDialogFragment != null && !mPromptDialogFragment.isAdded()) {
                                mPromptDialogFragment.show(getSupportFragmentManager(), "signInDialog");
                            }

                        }

                        if (respData != null) {
                            if(respData.resp.status == 1){
                                mSignInBt.setClickable(false);
                                mSignInBt.setBackgroundResource(R.drawable.sign_in_signed);
                                mSignInText.setText(respData.resp.text);
                            }
                        }

                    }else{
                        mSignInBt.setClickable(true);
                        mSignInBt.setBackgroundResource(R.drawable.btn_signin_normal);
                        ToastUtils.showToast(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    mSignInBt.setClickable(true);
                    mSignInBt.setBackgroundResource(R.drawable.btn_signin_normal);
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                mSignInBt.setClickable(true);
                mSignInBt.setBackgroundResource(R.drawable.btn_signin_normal);
            }

        });

    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mPerson = CommonUtils.queryUserPerson(SignInEverydayActivity.this);
            requestSignInData();
            unRegisterLocalBroadcastReceiver();
        }
    }

    /**
     * 注册本地广播接收者
     */
    private void registerLocalBroadcastReceiver(){
        mIntentFilter=new IntentFilter(Constants.ACTION_LOGIN_SUCCESS);
        mLocalBroadcastReceiver=new LocalBroadcastReceiver();
        mLocalBroadcastManager= LocalBroadcastManager.getInstance(SignInEverydayActivity.this);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, mIntentFilter);
    }

    /**
     * 取消本地广播的注册
     */
    private void unRegisterLocalBroadcastReceiver(){
        if (mLocalBroadcastManager!=null) {
            if (mLocalBroadcastReceiver!=null) {
                mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
            }
        }
    }

    @Override
    protected void initActionBar() {
        findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
        super.initActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPerson == null) {
            mPerson = CommonUtils.queryUserPerson(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mCancelable2 != null) {
            mCancelable2.cancel();
        }
        unRegisterLocalBroadcastReceiver();
    }
}
