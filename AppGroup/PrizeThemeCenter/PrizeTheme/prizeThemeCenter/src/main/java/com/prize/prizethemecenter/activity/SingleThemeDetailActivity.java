package com.prize.prizethemecenter.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iapppay.interfaces.callback.IPayResultCallback;
import com.iapppay.sdk.main.IAppPay;
import com.iapppay.sdk.main.IAppPayOrderUtils;
import com.prize.app.beans.ClientInfo;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.cloud.bean.Person;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.fragment.SingleThemeDetailFragment;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTask;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.request.SimpleThemeDetailRequest;
import com.prize.prizethemecenter.request.ThemeBuyHistoryRequest;
import com.prize.prizethemecenter.request.ThemeDownloadHistoryRequest;
import com.prize.prizethemecenter.response.SingleThemeDetailResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.PayConfig;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.DownLoadButton;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.io.File;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by bxh on 2016/9/6.
 */
public class SingleThemeDetailActivity extends ActionBarNoTabActivity implements View.OnClickListener {

    @InjectView(R.id.container)
    FrameLayout container;
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    @InjectView(R.id.container_reload)
    FrameLayout containerReload;
    @InjectView(R.id.bt_download)
    DownLoadButton btDownload;
    @InjectView(R.id.bt_pay)
    Button btPay;
    @InjectView(R.id.bt_free_apply)
    Button btFreeApply;
    @InjectView(R.id.rl_isFree)
    LinearLayout rlIsFree;
    @InjectView(R.id.view_devision)
    View viewDevision;
    @InjectView(R.id.Rlyt_souce_missing)
    RelativeLayout RlytSouceMissing;
    @InjectView(R.id.bottom_id)
    RelativeLayout bottomId;

    private SimpleThemeDetailRequest request;
    private SingleThemeDetailResponse response = null;
    private Intent intent;
    private String themeId;
    private SingleThemeDetailFragment themeDetailFgm;
    private Bundle mBundle;
    private static String TAG = "bian";
    public static final String RECEIVER_ACTION = "appley_theme_ztefs";
    private AlertDialog rightPopupWindow = null;

    public String picPath;
    private String isPay;
    private String themePrice;
    private String name;
    private SingleThemeItemBean.ItemsBean data;
    private String payID;
    private ThemeBuyHistoryRequest buyRequest;
    private ThemeDownloadHistoryRequest downloadRequest;
    private String isBuy;
    private String status;  //商品是否下架
    private UIDownLoadListener listener;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (btDownload != null) {
                btDownload.invalidate();
            }
        }
    };
    private boolean isPushBack;
    private List<String> payThemeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(window, this);
        setContentView(R.layout.activity_appdetail);
        WindowMangerUtils.changeStatus(getWindow());
        setUpWindowTrisience();
        ButterKnife.inject(this);
        intent = getIntent();
        if (intent != null) {
            mBundle = intent.getBundleExtra("bundle");
            if (mBundle != null) {
                themeId = mBundle.getString("themeID", null);
                if (mBundle.getString("minPic", null) != null)
                    picPath = mBundle.getString("minPic", null).trim();
                mBundle.putString("themeID", themeId);
            }
            if (intent.getStringExtra("themePath") != null) {
                String[] themePath = intent.getStringExtra("themePath").split("/");
                themeId = themePath[themePath.length - 1].replace(".zip", "");
                themeId = themeId.substring(0, themeId.length() - 1);
            }
        }
        themeDetailFgm = new SingleThemeDetailFragment();
		//add by zhouerlong
        if (themeDetailFgm != null && !themeDetailFgm.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, themeDetailFgm)
                    .commitAllowingStateLoss();

        }
		//add by zhouerlong
        setTitle("主题详情");
        // 接收推送默认开启
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
        if (!DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            initPushData();
        }
        themeDetailFgm.setArguments(mBundle);
        Log.d(TAG, "onCreate " + ClientInfo.getInstance().getModel());
        initLoadVIew();
        loadData();

        btDownload.setCompleteCallBack(new DownLoadButton.CompleteCallBack() {
            @Override
            public void onStates() {
//                Intent intent = new Intent(RECEIVER_ACTION);
//                intent.putExtra("themePath", new File(FileUtils.getDir("theme"), themeId + ".zip").getAbsolutePath());
//				sendBroadcast(intent);
//                if (!themePrice.equals("0.0") && isBuy.equals("0"))
//                    DBUtils.updatePriceToThemeTable(data, false);
                if (themePrice.equals("0.0") || isBuy.equals("1")) {
                    DBUtils.updatePriceToThemeTable(data, true);
                }
                PushDownloadHistory();
                initDownloadButton();
            }
        });

    }

    private void PushDownloadHistory() {
        downloadRequest = new ThemeDownloadHistoryRequest();
        if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
            return;
        }
        Person person = UIUtils.queryUserPerson(this);
        if (person == null)
            return;
        downloadRequest.theme_id = Integer.parseInt(data.getId());
        downloadRequest.userid = CommonUtils.queryUserId();
        downloadRequest.model = ClientInfo.getInstance().getModel();
        downloadRequest.user_name = person.getRealName();
        downloadRequest.user_icon = person.getAvatar();
        JLog.i("hu", "PushDownloadHistory " + downloadRequest.user_name + "::" + downloadRequest.user_icon);
        x.http().post(downloadRequest, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess ---->PushDownloadHistory");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d(TAG, "onError ");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d(TAG, "onCancelled ");
            }

            @Override
            public void onFinished() {
                Log.d(TAG, "onFinished ");
            }
        });
    }

    private void initPushData() {
        Log.d(TAG, "initPushData: " + "aaaaaa");
        XGPushConfig.enableDebug(this, true);
        XGPushClickedResult xgPushClickedResult = XGPushManager
                .onActivityStarted(this);
        if (xgPushClickedResult != null) {
            String pushJson = xgPushClickedResult.getCustomContent();
            isPushBack = true;
            Log.d("TPush", "SingleThemeDetailActivity: " + pushJson);
            String pic = null;
            if (!TextUtils.isEmpty(pushJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(pushJson);
                    themeId = jsonObject.getString("themeID").trim();
                    picPath = jsonObject.getString("minPic").trim();
                    if (mBundle == null)  mBundle = new Bundle();
                    mBundle.putString("themeID", themeId);
//                    String picPaths = UILimageUtil.getPicPath(this, picPath);
//                    picPath = UILimageUtil.formatDirPic(picPaths);
                    Log.d(TAG, "initPushData: " + picPath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void initLoadVIew() {
        View someView = findViewById(R.id.container);
        View root = someView.getRootView();
        root.setBackgroundColor(getResources().getColor(android.R.color.white));
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        View reload_layout = LayoutInflater.from(this).inflate(R.layout.reload_layout, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        LinearLayout reloadView = (LinearLayout) reload_layout.findViewById(R.id.reload_Llyt);
        loadingView.setGravity(Gravity.CENTER);
        reloadView.setGravity(Gravity.CENTER);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerReload.addView(reload_layout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//        DownloadManager.getInstance().addDownloadListener(btDownload);
        btDownload.setOnClickListener(this);
        btPay.setOnClickListener(this);
        btFreeApply.setOnClickListener(this);
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                mHandler.sendEmptyMessage(0);
            }
        };
        AppManagerCenter.setDownloadRefreshHandle(listener);
    }

    private void loadData() {
        if (response == null) {
            containerWait.setVisibility(View.VISIBLE);
            container.setVisibility(View.INVISIBLE);
            showWaiting();
            initData();
        } else {
            viewDevision.setVisibility(View.VISIBLE);
            containerWait.setVisibility(View.GONE);
            hideWaiting();
            container.setVisibility(View.VISIBLE);
        }
    }


    private void initData() {
        getDetailData();
    }

    private void getDetailData() {
        request = new SimpleThemeDetailRequest();
//        if (themeId == null) return;
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            request.userId = CommonUtils.queryUserId();
        }
        if (themeId != null) {
            request.themeId = Integer.parseInt(themeId);
        }
        x.http().post(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000 && obj.get("msg").equals("ok")) {
                        containerWait.setVisibility(View.GONE);
                        container.setVisibility(View.VISIBLE);
                        hideWaiting();
                        response = CommonUtils.getObject(result, SingleThemeDetailResponse.class);
                        data = response.data.getItems().get(0);
                        themeDetailFgm.initData(data);
                        isPay = data.getIs_pay();
                        isBuy = data.getIs_buy();
                        status = data.getStatus();

                        themePrice = data.getPrice();
                        name = data.getName();
                        initView();
                        initDownloadButton();
                        //设置缩略图
                        if (picPath == null) {
                            picPath = data.ad_pictrue;
                        }
                        data.setThumbnail(picPath);
                        JLog.i("hu", "data.getThumbnail()==" + data.getThumbnail());
                        btDownload.setData(data, 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

                containerWait.setVisibility(View.GONE);
                hideWaiting();
                loadingFailed(new ReloadFunction() {
                    @Override
                    public void reload() {
                        loadData();
                    }
                });
            }

            @Override
            public void onCancelled(CancelledException cex) {


            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void initView() {
        if (status != null && !status.equals("1")) {
            RlytSouceMissing.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
            bottomId.setVisibility(View.GONE);
        }else {
            RlytSouceMissing.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            bottomId.setVisibility(View.VISIBLE);
        }
    }

    private void initDownloadButton() {
        String sInfoFormat = getResources().getString(R.string.pay_text_show);
        sInfoFormat = String.format(sInfoFormat, themePrice);
        int state = -1;
        if (data == null) return;
        DownloadInfo info = DBUtils.findDownloadById(data.getId() + 1);
        if (info != null) state = info.currentState;
        boolean isDownload = DBUtils.isDownload(data.getId() + 1);
        if (isDownload) {
            btFreeApply.setText(R.string.Apply_immediately);
        } else {
            btFreeApply.setText(R.string.apply_free);
        }

        try {
            viewDevision.setVisibility(View.VISIBLE);
            if (isPay.equals("0") || (state == 6 && DBUtils.isDownloadAndPay(data.getId() + 1)) || isBuy.equals("1") || (state >= 0 && state < 6)) {
                btDownload.setVisibility(View.VISIBLE);
                rlIsFree.setVisibility(View.GONE);
            } else {
                btPay.setText(sInfoFormat);
                rlIsFree.setVisibility(View.VISIBLE);
                btDownload.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getActivityName() {
        return null;
    }


    private void setUpWindowTrisience() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
            TransitionSet mtransitionset = new TransitionSet();
            mtransitionset.setDuration(3000);
            getWindow().setEnterTransition(mtransitionset);
            getWindow().setExitTransition(mtransitionset);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_pay:
                if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
                    UIUtils.jumpToLoginActivity();
                    return;
                }
                if (TextUtils.isEmpty(themePrice)) return;
//                if (rightPopupWindow != null) {
//                    if (!rightPopupWindow.isShowing()) {
//                        rightPopupWindow.show();
//                    } else {
//                        rightPopupWindow.dismiss();
//                    }
//                } else {
//                    initPop();
//                }
                StartPay();
                break;
            case R.id.bt_download:
                btDownload.OnClick(SingleThemeDetailActivity.this);
                break;
            case R.id.bt_free_apply:
                DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(data.getId() + 1);
                if (btFreeApply.getText().equals("免费试用")) {
                    btDownload.setVisibility(View.VISIBLE);
                    btDownload.OnClick(SingleThemeDetailActivity.this);
                    rlIsFree.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(RECEIVER_ACTION);
                    intent.putExtra("themePath", new File(FileUtils.getDir("theme"), themeId + 1 + ".zip").getAbsolutePath());
                    intent.putExtra("freeApply", true);
                    if (task != null && task.loadGame != null) {
                        intent.putExtra("freeName", task.loadGame.getName());
                    }
//                    MainApplication.curContext.sendBroadcast(intent);
//                        DBUtils.CancellUsedType(1);
                    DownloadTaskMgr.getInstance().setDownlaadTaskState(themeId, 1);
                    DBUtils.saveOrUpdateDownload(themeId, 1);
                    UIUtils.backToLauncher(MainApplication.curContext,intent);
                    DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED, themeId + 1);
                }
                break;
//            case R.id.add_neg:
//                rightPopupWindow.dismiss();
//                break;
//            case R.id.sure_Btn:
//                rightPopupWindow.dismiss();
//                payID = CommonUtils.queryThemePayMoney(themePrice);
//                SFCommonSDKInterface.pay(SingleThemeDetailActivity.this, payID, new SFIPayResultListener() {
//                    @Override
//                    public void onCanceled(String s) {
//                        Log.d(TAG, "onCanceled ");
//                    }
//
//                    @Override
//                    public void onFailed(String s) {
//                        Log.d(TAG, "onFailed ");
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//                        rlIsFree.setVisibility(View.GONE);
//                        btDownload.setVisibility(View.VISIBLE);
//                        DBUtils.updatePriceToThemeTable(data, true);
//                        PushPayHistory();
//                        Log.d(TAG, "onSuccess ");
//                    }
//                });
//                break;
        }

    }

    /**
     * 支付流程
     */
    private void StartPay() {
//        String cporderid = System.currentTimeMillis() + "";
              /*prize add by bianxinhao 2017年3月13日15:27:06  start */
        String cporderid =  CommonUtils.queryUserId()+ data.getId()+"1";
        /*prize add by bianxinhao 2017年3月13日15:27:06  end*/
        String param = PayConfig.getTransdata(themeId, "主题支付成功", 1, Float.parseFloat(themePrice), cporderid, "主题" + name);
        IAppPay.startPay(SingleThemeDetailActivity.this, param, iPayResultCallback);
    }

    /**
     * 支付结果回调
     */
    IPayResultCallback iPayResultCallback = new IPayResultCallback() {

        @Override
        public void onPayResult(int resultCode, String signvalue, String resultInfo) {
            // TODO Auto-generated method stub
            switch (resultCode) {
                case IAppPay.PAY_SUCCESS:
                    //调用 IAppPayOrderUtils 的验签方法进行支付结果验证
                    boolean payState = IAppPayOrderUtils.checkPayResult(signvalue, PayConfig.publicKey);
                    if (payState) {
                        //支付成功  上传服务器
                        rlIsFree.setVisibility(View.GONE);
                        btDownload.setVisibility(View.VISIBLE);
                        DBUtils.updatePriceToThemeTable(data, true);
                        /**广播关闭主题试用*/
                        sendFreeToLuancher();
                        PushPayHistory();
                        isBuy = "1";
                        Toast.makeText(SingleThemeDetailActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                    }
                    break;
                case IAppPay.PAY_ING:
                    Toast.makeText(SingleThemeDetailActivity.this, "成功下单", Toast.LENGTH_LONG).show();
                    break;
                case 3001:
                case 6110:
                    DBUtils.updatePriceToThemeTable(data, true);
                    /**广播关闭主题试用*/
                    sendFreeToLuancher();
                    PushPayHistory();
                    isBuy = "1";
                    initDownloadButton();
                    Toast.makeText(SingleThemeDetailActivity.this, "商户订单已经支付成功", Toast.LENGTH_LONG).show();
                default:
                    Toast.makeText(SingleThemeDetailActivity.this, resultInfo, Toast.LENGTH_LONG).show();
                    break;
            }
            JLog.i("hu", "FontDetailActivity ==  requestCode:" + resultCode + ",signvalue:" + signvalue + ",resultInfo:" + resultInfo);
        }
    };

    private void sendFreeToLuancher() {
        DownloadTask task = DownloadTaskMgr.getInstance().getDownloadTask(data.getId() + data.getType());
        Intent intent = new Intent(RECEIVER_ACTION);
        intent.putExtra("themePath", new File(FileUtils.getDir("theme"), data.getId() + 1 + ".zip").getAbsolutePath());
        intent.putExtra("freeApply", false);
        if (task != null && task.loadGame != null) {
            intent.putExtra("freeName", task.loadGame.getTitle());
        }
        MainApplication.curContext.sendBroadcast(intent);
    }


    private void PushPayHistory() {
        buyRequest = new ThemeBuyHistoryRequest();
        if (TextUtils.isEmpty(CommonUtils.queryUserId()) || TextUtils.isEmpty(themePrice)) {
            return;
        }
        Person person = UIUtils.queryUserPerson(this);
        if (person == null)
            return;
        buyRequest.userid = CommonUtils.queryUserId();
        buyRequest.theme_id = Integer.parseInt(data.getId());
        buyRequest.model = ClientInfo.getInstance().getModel();
        buyRequest.price = themePrice;
        buyRequest.user_name = person.getRealName();
        buyRequest.user_icon = person.getAvatar();
        JLog.i("hu", "PushPayHistory " + buyRequest.user_name + "::" + buyRequest.user_icon);
        x.http().post(buyRequest, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess --->PushPayHistory" + result.toString());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.d(TAG, "onError " + ex.toString());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.d(TAG, "onCancelled ");
            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void initPop() {
        rightPopupWindow = new AlertDialog.Builder(this,
                R.style.wallpaper_use_dialog_style).create();
        rightPopupWindow.show();
        View loginwindow = this.getLayoutInflater().inflate(
                R.layout.popwindow_pay_layout, null);
        TextView neg = (TextView) loginwindow.findViewById(R.id.add_neg);
        TextView sure = (TextView) loginwindow.findViewById(R.id.sure_Btn);
        TextView content = (TextView) loginwindow.findViewById(R.id.content_tv);
        String payContent = getResources().getString(R.string.pay_content);
        payContent = String.format(payContent, "主题《" + data.getName() + "》", themePrice);
        content.setText(payContent);
        neg.setOnClickListener(this);
        sure.setOnClickListener(this);

        Window window = rightPopupWindow.getWindow();
        WindowMangerUtils.changeStatus(window);
        window.setContentView(loginwindow);
        WindowManager.LayoutParams p = window.getAttributes();
        p.width = 600;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(p);
        window.setGravity(Gravity.CENTER);
        rightPopupWindow.setContentView(loginwindow);
    }


//add by zhouerlong  comment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        themeDetailFgm.onActivityResult(requestCode,resultCode,data);
    }

//add by zhouerlong  comment
    @Override
    protected void onResume() {
        super.onResume();
        if (themeDetailFgm != null && !themeDetailFgm.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, themeDetailFgm)
                    .commitAllowingStateLoss();
        }

        //add by zhouerlong
        ViewGroup f= (ViewGroup) findViewById(R.id.container);
        if(f.getChildCount()>1) {
            f.removeViewAt(0);
        }
        //add by zhouerlong
        payThemeList = RootActivity.getPayThemeList();
        if (data != null && payThemeList != null && payThemeList.contains(data.getId())) {
            isBuy = "1";
            initDownloadButton();
        }
//        initDownloadButton();
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
        AppManagerCenter.removeDownloadRefreshHandle(listener);
        mHandler.removeMessages(0);
        XGPushManager.onActivityStoped(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isPushBack) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                // 监控返回键
                UIUtils.gotoActivity(MainActivity.class);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
