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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.Person;
import com.prize.app.beans.PointsMallDataBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.datasource.base.UsersPointsData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarPointsActivity;
import com.prize.appcenter.ui.adapter.PersonalPointsMallAdapter;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

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

public class PersonalPointsMallActivity extends ActionBarPointsActivity {
    private final String TAG = "PersonalPointsMallActivity";
    private RelativeLayout mDdefaultLlyt;
    private ListView mListView;
    private int currentIndex = 1;

    private PersonalPointsMallAdapter mMallAdapter;
    private Callback.Cancelable cancelable;
    private Callback.Cancelable reqHandler;
    private TextView ponit_Tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_points_mall);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(getString(R.string.points_mall));
        initView();
        initListener();
        initData();
    }

    public void goToIntent(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.sign_everyday_Tv:
                MTAUtil.onClickPointMallMenu(getString(R.string.sign_in_everyday));
                intent = new Intent(this, SignInEverydayActivity.class);
                break;
            case R.id.game_gift_Tv:
                MTAUtil.onClickPointMallMenu("游戏福利");
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("position", 3);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                return;
            default:
                MTAUtil.onClickPointMallMenu(getString(R.string.has_gift));
                intent = new Intent(this, PersonalEarnPointsActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void initView() {
        if (getIntent() != null) {
            from = getIntent().getStringExtra(Constants.FROM);
        }
        mDdefaultLlyt = (RelativeLayout) findViewById(R.id.points_mall_defalutRlyt_id);
        mListView = (ListView) findViewById(R.id.points_mall_listview);
        View mHeadView = LayoutInflater.from(this).inflate(R.layout.head_point_mall, null);
        ponit_Tv = (TextView) mHeadView.findViewById(R.id.ponit_Tv);
        ponit_Tv.setText("-?-");
        ImageView mHeadImageView = (ImageView) mHeadView.findViewById(R.id.banner_Iv);
        mHeadImageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (ClientInfo.getInstance().screenHeight * 0.24)));
        mListView.addHeaderView(mHeadView);

    }

    private void initData() {
        mMallAdapter = new PersonalPointsMallAdapter(this);
        mListView.setAdapter(mMallAdapter);
        requestData();


    }

    private void initListener() {
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

        if (rule != null) {
            rule.setBackgroundResource(R.drawable.btn_convert_records_sl);
            rule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MTAUtil.onClickCONVERT_RECORDS();
                    if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
                        UIUtils.gotoActivity(LoginActivityNew.class, PersonalPointsMallActivity.this);
                    } else {
                        Intent intent = new Intent(PersonalPointsMallActivity.this, PersonalConvertRecordsActivity.class);
                        intent.putExtra("userId", CommonUtils.queryUserId());
                        startActivity(intent);
                    }
                }
            });
        }

    }

    AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }
    };

    //请求数据
    private void requestData() {
        RequestParams mParams = new RequestParams(Constants.GIS_URL + "/point/goodsnew");
        cancelable = XExtends.http().post(mParams, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                hideWaiting();

                try {
                    String response = new JSONObject(result).getString("data");
                    PointsMallDataBean mPointsMallDataBean = new Gson().fromJson(response, PointsMallDataBean.class);
                    mMallAdapter.setData(mPointsMallDataBean.goods);
                } catch (JSONException e) {
                    //json解析一般不会错
                    if (currentIndex == 1) {
                        isShowDefaultView(true);
                    }
                    e.printStackTrace();

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //首次进入加载失败了
                if (currentIndex == 1) {
                    hideWaiting();
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }

                    });
                }

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


    /**
     * Desc: 联网请求获取用户积分+
     * 2.0积分系统
     * Created by huangchangguo
     * Date:  2016/8/16 13:59
     */
    private void requestUserPoints() {
        Person mPerson = CommonUtils.queryUserPerson(this);
        if (mPerson == null || TextUtils.isEmpty(mPerson.getUserId()))
            return;
        String userId = mPerson.getUserId();
        String userPhone = mPerson.getPhone();
        if (TextUtils.isEmpty(userPhone)) {
            return;
        }
        String url = Constants.GIS_URL + "/point/summary";
        RequestParams reqParams = new RequestParams(url);
        reqParams.addBodyParameter("accountId", userId);
        reqParams.addBodyParameter("tel", userPhone);
        reqHandler = XExtends.http().post(reqParams,
                new PrizeXutilStringCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                String resp = obj.optString("data");
                                UsersPointsData usersPointsData = new Gson().fromJson(resp,
                                        UsersPointsData.class);
                                ponit_Tv.setText(getString(R.string.point_value_string, usersPointsData.summary.points));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                    }
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestUserPoints();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }


    public void isShowDefaultView(boolean isShowDefaultView) {
        if (isShowDefaultView) {
            mListView.setVisibility(View.GONE);
            mDdefaultLlyt.setVisibility(View.VISIBLE);

        } else {
            mDdefaultLlyt.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancelable != null) {
            cancelable.cancel();
        }
        if (reqHandler != null) {
            reqHandler.cancel();
        }
    }

    //    @Override
//    public void onBackPressed() {
//        if (!TextUtils.isEmpty(from)) {
//            if ("push".equals(from) || "startPage".equals(from)) {
//                try {
//                    UIUtils.gotoMainActivity(PersonalPointsMallActivity.this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        super.onBackPressed();
//    }
    @Override
    public void finish() {
        UIUtils.gotoMainActivity(this, from);
        super.finish();
    }
}