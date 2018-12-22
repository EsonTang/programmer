package com.prize.prizethemecenter.ui.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.util.JLog;
import com.prize.cloud.activity.MainActivityCloud;
import com.prize.cloud.bean.Person;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.DownLoadQueenActivity;
import com.prize.prizethemecenter.activity.FeedbackExActivity;
import com.prize.prizethemecenter.activity.MainActivity;
import com.prize.prizethemecenter.activity.MineActivity;
import com.prize.prizethemecenter.activity.RootActivity;
import com.prize.prizethemecenter.activity.SettingActivity;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.fragment.PromptDialogFragment;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.CircleImageViewTwo;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 首页我的
 *
 * @author Administrator pengyang
 */
public class MinePage extends BasePage implements OnClickListener {

    private static final String TAG = "MinePage";
    @InjectView(R.id.topic_num)
    TextView mTopicNum;
    @InjectView(R.id.wallpaper_num)
    TextView mWallpaperNum;
    @InjectView(R.id.font_num)
    TextView mFontNum;
    @InjectView(R.id.login_out_Btn)
    TextView mLoginOutBtn;
    private TextView tvDownloadNum;
    private RelativeLayout download_RL;
    private RelativeLayout feedback_RL;
    private RelativeLayout setting_RL;
    private RelativeLayout topic_RL;
    private RelativeLayout wallpaper_RL;
    private RelativeLayout font_RL;
    private CircleImageViewTwo mLogHeadImg;
    private PromptDialogFragment df;
    private View view;
    private Context mContext;
    private Person person;
    private TextView mUserPhone;
    private boolean loginFlag = false;
    private List<DownloadInfo> downloadingTask;
    private ArrayList<DownloadInfo> list;
    private ArrayList<DownloadInfo> list1;
    private ArrayList<DownloadInfo> list2;

    public MinePage(RootActivity activity) {
        super(activity);
        this.activity = (MainActivity) activity;
    }

    @Override
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        view = inflater.inflate(R.layout.mime_page, null);
        ButterKnife.inject(this, view);
        mContext = this.view.getContext();
        init();
        return this.view;
    }

    private void init() {
        feedback_RL = (RelativeLayout) view.findViewById(R.id.feedback_RL);
        feedback_RL.setOnClickListener(this);
        topic_RL = (RelativeLayout) view.findViewById(R.id.topic_RL);
        topic_RL.setOnClickListener(this);
        wallpaper_RL = (RelativeLayout) view.findViewById(R.id.wallpaper_RL);
        wallpaper_RL.setOnClickListener(this);
        font_RL = (RelativeLayout) view.findViewById(R.id.font_RL);
        font_RL.setOnClickListener(this);
        download_RL = (RelativeLayout) view.findViewById(R.id.download_RL);
        download_RL.setOnClickListener(this);
        setting_RL = (RelativeLayout) view.findViewById(R.id.setting_RL);
        setting_RL.setOnClickListener(this);
        mLogHeadImg = (CircleImageViewTwo) view.findViewById(R.id.headImg_id);
        mLogHeadImg.setOnClickListener(this);
        mUserPhone = (TextView) view.findViewById(R.id.user_id);
        tvDownloadNum = (TextView) view.findViewById(R.id.tv_download_num);
        MainApplication application = (MainApplication) ((Activity) mContext).getApplication();
        application.setLoginCallBack(mLoginDataCallBack);
        mLoginOutBtn.setOnClickListener(this);
        setLoginBtnVisiblity();
    }

    private void initThemeData() {
        try {
            list = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(1);
            list1 = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(2);
            list2 = (ArrayList<DownloadInfo>) DBUtils.findAllDownloadedTask(3);
            mTopicNum.setText(list.size() + "");
            mWallpaperNum.setText(list1.size() + "");
            mFontNum.setText(list2.size() + "");
            Log.d(TAG, "init: " + list.size() + ":" + list1.size() + ":" + list2.size());
        } catch (Exception pE) {
            pE.printStackTrace();
        }
    }

    private MainApplication.LoginDataCallBack mLoginDataCallBack = new MainApplication.LoginDataCallBack() {

        @Override
        public void setPerson(Person person) {
            queryUserId();

        }
    };

    /**
     * 方法描述：查询是否登录云账号
     */
    private void queryUserId() {
        person = UIUtils.queryUserPerson(mContext);
        JLog.i(TAG, "queryUserId-person=" + person);
        processAccountState();

    }

    /**
     * 处理是否登录过云账户号的逻辑
     */
    private void processAccountState() {
        if (person != null) {
            JLog.i(TAG, "这里是我的-----------person---------" + person.toString());

            if (TextUtils.isEmpty(person.getUserId())) {
                //没有登录
                loginFlag = false;
                mUserPhone.setText(R.string.please_login);
                setLoginBtnVisiblity();
                mLogHeadImg.setImageResource(R.drawable.person_img);
            } else {
                //已经登录
                loginFlag = true;
                setLoginBtnVisiblity();
                // 1：显示电话号码，如果为空则显示名字
                if (!TextUtils.isEmpty(person.getPhone())) {
                    mUserPhone.setText(person.getPhone());
                } else if (!TextUtils.isEmpty(person.getRealName())) {
                    mUserPhone.setText(person.getRealName());
                } else  {
                    mUserPhone.setText(person.getUserId());
                }

                ImageLoader.getInstance().displayImage(person.getAvatar(),
                        mLogHeadImg, UILimageUtil.getPersonHeadImg());
//                if (!TextUtils.isEmpty(person.getAvatar())) {
////                    DisplayLargerImageOptions options = new DisplayLargerImageOptions.Builder()
////                            .showImageOnFail(R.drawable.person_img)
////                            .cacheInMemory(true).cacheOnDisk(true).build();
//
//                } else {
//                    mLogHeadImg.setImageResource(R.drawable.person_img);
//                }
            }
        } else {
            loginFlag = false;
            mUserPhone.setText(R.string.please_login);
            mLogHeadImg.setImageResource(R.drawable.person_img);
        }

    }

    public void setLoginBtnVisiblity() {
        if (loginFlag) {
            mLoginOutBtn.setVisibility(View.VISIBLE);
        } else {
            mLoginOutBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.download_RL:
                intent = new Intent(activity, DownLoadQueenActivity.class);
                activity.startActivity(intent);
                MTAUtil.onDownloadClick();
                break;
            case R.id.feedback_RL:
                intent = new Intent(activity, FeedbackExActivity.class);
                activity.startActivity(intent);
                MTAUtil.onFeedbackClick();
                break;
            case R.id.setting_RL:
                intent = new Intent(activity, SettingActivity.class);
                activity.startActivity(intent);
                MTAUtil.onSettingClick();
                break;
            case R.id.topic_RL:
                intent = new Intent(activity, MineActivity.class);
                intent.putExtra(MineActivity.mTabId, 0);
                MainApplication.curContext.startActivity(intent);
                break;
            case R.id.wallpaper_RL:
                intent = new Intent(activity, MineActivity.class);
                intent.putExtra(MineActivity.mTabId, 1);
                MainApplication.curContext.startActivity(intent);
            break;
            case R.id.font_RL:
                intent = new Intent(activity, MineActivity.class);
                intent.putExtra(MineActivity.mTabId, 2);
                MainApplication.curContext.startActivity(intent);
                break;
            case R.id.headImg_id:
                intent = new Intent(activity, MainActivityCloud.class);
                activity.startActivity(intent);
                break;
            case R.id.login_out_Btn:
                if (loginFlag) {

                    // 更换自定义的fragment提示框，bug提出说dialog太慢了
                    if (df == null || !df.isAdded()) {
                        df = PromptDialogFragment
                                .newInstance(
                                        activity.getResources().getString(
                                                R.string.caution),
                                        activity.getResources().getString(
                                                R.string.login_out_tips),
                                        activity.getString(R.string.alert_button_yes),
                                        null, mDeletePromptListener);
                    }
                    if (df != null && !df.isAdded()) {
                        df.show(((FragmentActivity) activity).getSupportFragmentManager(), "sureDialog");
                    }
                } else {
                    ToastUtils.showToast(activity.getString(R.string.not_loginclound));
                }
                break;

            default:
                break;
        }
    }

    /**
     * 编辑后退出提示框
     */
    private OnClickListener mDeletePromptListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            df.dismissAllowingStateLoss();
            loginFlag = false;
            setLoginBtnVisiblity();
            // 点击确认
            logout();
            queryUserId();
        }
    };

    /**
     * 第三方版退出账号弹出对话框，第三方版需打开注销内容
     */
    public void logout() {
        UIUtils.logout(activity);
        MainApplication.getInstance().queryPerson();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void onActivityCreated() {

    }

    @Override
    public String getPageName() {
        return null;
    }

    @Override
    public void onDestroy() {
        ((MainApplication) ((Activity) mContext).getApplication())
                .setLoginCallBack(null);
        mLoginDataCallBack = null;
    }

    @Override
    public void addToHistory(String text) {
    }

    @Override
    public void showHistory() {

    }

    @Override
    public void onResume() {
        downloadingTask = DBUtils.findDownloadingTask();
        if (downloadingTask == null || downloadingTask.size() == 0) {
            tvDownloadNum.setVisibility(View.GONE);
            tvDownloadNum.setText("");
        } else {
            tvDownloadNum.setVisibility(View.VISIBLE);
            tvDownloadNum.setText(downloadingTask.size() + "");
        }
        queryUserId();
        initThemeData();
        super.onResume();
    }


}
