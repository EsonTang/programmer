package com.prize.prizethemecenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.prize.prizethemecenter.fragment.SingleFontDetailFragment;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.request.FontBuyHistoryRequest;
import com.prize.prizethemecenter.request.FontDetailRequest;
import com.prize.prizethemecenter.request.FontDownloadHistoryRequest;
import com.prize.prizethemecenter.response.SingleThemeDetailResponse;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.PayConfig;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.DownLoadButton;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 字体详情
 * Created by pengy on 2016/11/1.
 */
public class FontDetailActivity extends ActionBarNoTabActivity implements View.OnClickListener {


    private static final String TAG = "pengy";
    @InjectView(R.id.container)
    FrameLayout container;
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    @InjectView(R.id.container_reload)
    FrameLayout containerReload;
//add by zhouerlong  comment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fontFragment.onActivityResult(requestCode,resultCode,data);
    }
//add by zhouerlong   comment

    @InjectView(R.id.bt_download)
    DownLoadButton btDownload;
    @InjectView(R.id.buy_TV)
    TextView buyTV;
    @InjectView(R.id.line_View)
    View lineView;
    @InjectView(R.id.Rlyt_souce_missing)
    RelativeLayout RlytSouceMissing;
    @InjectView(R.id.bottom_id)
    RelativeLayout bottomId;

    private FontDetailRequest request = null;
    private FontBuyHistoryRequest buyRequest;
    private FontDownloadHistoryRequest downloadRequest;
    private SingleThemeDetailResponse response = null;
    private String fontID;

    private SingleFontDetailFragment fontFragment;

    private SingleThemeItemBean.ItemsBean bean;

    /**
     * 是否购买
     */
    private String isPay = null;
    /**
     * 计费点ID 从0开始
     */
    private String payID = null;
    /**
     * 字体价格
     */
    private String price = null;

    private AlertDialog rightPopupWindow = null;

    private String picPath;

    private String isBuy;
    private String name;
    private String status;
    private boolean isPushBack;
    private UIDownLoadListener listener;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (btDownload != null) {
                btDownload.invalidate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.activity_font_appdetail);
        WindowMangerUtils.changeStatus(getWindow());
        ButterKnife.inject(this);

        if (getIntent() != null) {
            fontID = getIntent().getStringExtra("fontID");
            picPath = getIntent().getStringExtra("minPic");
            isPushBack = getIntent().getBooleanExtra("isPush",false);
            if (DataStoreUtils.readShareInfo(DataStoreUtils.FONT_DETAIL_KEY)&&isPushBack){
                DataStoreUtils.removeShareInfo(DataStoreUtils.FONT_DETAIL_KEY);
                DataStoreUtils.saveShareInfo(DataStoreUtils.FONT_DETAIL_KEY,isPushBack);
            }
        }
        if (DataStoreUtils.readShareInfo(DataStoreUtils.FONT_DETAIL_KEY)){
            fontID = DataStoreUtils.readLocalInfo(DataStoreUtils.FONT_DETAIL_ID);
            picPath = DataStoreUtils.readLocalInfo(DataStoreUtils.FONT_DETAIL_URL);
        }
        fontFragment = new SingleFontDetailFragment();
        if (fontFragment != null && !fontFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fontFragment)
                    .commitAllowingStateLoss();
        }

        setTitle("字体详情");
        initLoadVIew();
        // 接收推送默认开启
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
        if (!DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            initPushData();
        }
        loadData();

    }

    private void initPushData() {
        XGPushClickedResult xgPushClickedResult = XGPushManager
                .onActivityStarted(this);
        if (xgPushClickedResult != null) {
            String pushJson = xgPushClickedResult.getCustomContent();
            isPushBack = true;
            if (DataStoreUtils.readShareInfo(DataStoreUtils.FONT_DETAIL_KEY)){
                DataStoreUtils.removeShareInfo(DataStoreUtils.FONT_DETAIL_KEY);
            }
            DataStoreUtils.saveShareInfo(DataStoreUtils.FONT_DETAIL_KEY,isPushBack);
            if (!TextUtils.isEmpty(pushJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(pushJson);
                    fontID = jsonObject.getString("fontID").trim();
                    if (!TextUtils.isEmpty(DataStoreUtils.readLocalInfo(DataStoreUtils.FONT_DETAIL_ID))){
                        DataStoreUtils.removeLocalInfo(DataStoreUtils.FONT_DETAIL_ID);
                    }
                    DataStoreUtils.saveLocalInfo(DataStoreUtils.FONT_DETAIL_ID,fontID);
                    picPath = jsonObject.getString("minPic").trim();
                    if (!TextUtils.isEmpty(DataStoreUtils.readLocalInfo(DataStoreUtils.FONT_DETAIL_URL))){
                        DataStoreUtils.removeLocalInfo(DataStoreUtils.FONT_DETAIL_URL);
                    }
                    DataStoreUtils.saveLocalInfo(DataStoreUtils.FONT_DETAIL_URL,picPath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void loadData() {
        if (response == null) {
            containerWait.setVisibility(View.VISIBLE);
            showWaiting();
            initData();
        } else {
            lineView.setVisibility(View.VISIBLE);
            containerWait.setVisibility(View.GONE);
            hideWaiting();
            container.setVisibility(View.VISIBLE);
        }
    }

    private void initData() {
        request = new FontDetailRequest();
        if (fontID != null) {
            request.fontId = Integer.parseInt(fontID);
        }
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            request.userId = CommonUtils.queryUserId();
        }
        x.http().post(request, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        response = CommonUtils.getObject(result, SingleThemeDetailResponse.class);
                        containerWait.setVisibility(View.GONE);
                        hideWaiting();
                        bean = response.data.getItems().get(0);

                        price = bean.getPrice();
                        isPay = bean.getIs_pay();
                        isBuy = bean.getIs_buy();
                        name = bean.getName();
                        status = bean.getStatus();
                        initView();
                        fontFragment.setFontData(bean);

                        //设置缩略图
                        if (picPath == null) {
                            picPath = bean.ad_pictrue;
                        }
                        bean.setThumbnail(picPath);
                        btDownload.setData(bean, 3);
                        initDownloadButton();
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

    /**
     * 初始化DownloadButton
     */
    private void initDownloadButton() {
        String sInfoFormat = getResources().getString(R.string.pay_text_show);
        sInfoFormat = String.format(sInfoFormat, price);
        int state = 0;
        DownloadInfo info = DBUtils.findDownloadById(bean.getId());
        if (info != null) state = info.currentState;
        JLog.i("hu", "isPay==" + isPay + "--state=" + state + "--isBuy==" + isBuy);
        lineView.setVisibility(View.VISIBLE);
        if (isPay.equals("0") || state == 4 || isBuy.equals("1")) {
            btDownload.setVisibility(View.VISIBLE);
            buyTV.setVisibility(View.INVISIBLE);
        } else {
            buyTV.setText(sInfoFormat);
            buyTV.setVisibility(View.VISIBLE);
            btDownload.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rightPopupWindow != null)rightPopupWindow.dismiss();
        AppManagerCenter.removeDownloadRefreshHandle(listener);
        mHandler.removeMessages(0);
        XGPushManager.onActivityStoped(this);
        isPushBack = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }

    private void initLoadVIew() {
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        View reload_layout = LayoutInflater.from(this).inflate(R.layout.reload_layout, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        LinearLayout reloadView = (LinearLayout) reload_layout.findViewById(R.id.reload_Llyt);
        loadingView.setGravity(Gravity.CENTER);
        reloadView.setGravity(Gravity.CENTER);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerReload.addView(reload_layout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//        DownloadManager.getInstance().addDownloadListener(btDownload);
        listener = new UIDownLoadListener() {
            @Override
            public void onRefreshUI(int theme_Id) {
                mHandler.sendEmptyMessage(0);
            }
        };
        AppManagerCenter.setDownloadRefreshHandle(listener);
        btDownload.setOnClickListener(this);
        btDownload.setCompleteCallBack(new DownLoadButton.CompleteCallBack() {
            @Override
            public void onStates() {
                //设置系统字体库
                PushDownloadHistory();
            }
        });
        buyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
                    UIUtils.jumpToLoginActivity();
                    return;
                }
                if (TextUtils.isEmpty(price)) {
                    return;
                }
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
            }
        });
    }
    /***
     * 上传字体下载记录
     */
    private void PushDownloadHistory() {
        downloadRequest = new FontDownloadHistoryRequest();
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            downloadRequest.userid = CommonUtils.queryUserId();
        } else {
            downloadRequest.userid = "0";
        }
        Person person = UIUtils.queryUserPerson(this);
        if(person==null)
            return;
        downloadRequest.font_id = Integer.parseInt(bean.getId());
        downloadRequest.model = ClientInfo.getInstance().getModel();
        downloadRequest.user_name = person.getRealName();
        downloadRequest.user_icon = person.getAvatar();
        JLog.i("hu", "PushDownloadHistory " + downloadRequest.user_name + "::" + downloadRequest.user_icon);
        x.http().post(downloadRequest, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, " 下载记录download Success" + result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i(TAG, " 下载记录Throwable" + ex + "--isOnCallback==" + isOnCallback);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 上传购买记录
     */
    private void PushPayHistory() {
        buyRequest = new FontBuyHistoryRequest();
        if (TextUtils.isEmpty(CommonUtils.queryUserId())) {
            return;
        }
        Person person = UIUtils.queryUserPerson(this);
        if(person==null)
            return;
        buyRequest.userid = CommonUtils.queryUserId();
        buyRequest.font_id = Integer.parseInt(bean.getId());
        buyRequest.price = bean.getPrice();
        buyRequest.model = ClientInfo.getInstance().getModel();
        buyRequest.user_name = person.getRealName();
        buyRequest.user_icon = person.getAvatar();
        JLog.i(TAG, buyRequest.user_name + "===PushPayHistory==" + buyRequest.user_icon + "--price==" + price);

        x.http().post(buyRequest, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, result + "11111onSuccess");
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i(TAG, "111ex==" + ex + "====111isOnCallback=" + isOnCallback);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                JLog.i(TAG, "1111CancelledException==" + cex);
            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Override
    public String getActivityName() {
        return "FontDetailActivity";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_download:
                MTAUtil.onClickSingleFont();
                btDownload.OnClick(FontDetailActivity.this);
                break;
            case R.id.add_neg:
//                rightPopupWindow.dismiss();
                break;
//            case R.id.sure_Btn:
//                rightPopupWindow.dismiss();
//                payID = CommonUtils.queryPayMoney(price);
//                SFCommonSDKInterface.pay(FontDetailActivity.this, payID, new SFIPayResultListener() {
//                    @Override
//                    public void onCanceled(String s) {
//                        JLog.i(TAG, s + "onCanceled");
//                    }
//
//                    @Override
//                    public void onFailed(String s) {
//                        JLog.i(TAG, s + "onFailed");
//                    }
//
//                    @Override
//                    public void onSuccess(String s) {
//                        //支付成功  上传服务器
//                        buyTV.setVisibility(View.INVISIBLE);
//                        btDownload.setVisibility(View.VISIBLE);
//                        PushPayHistory();
//                        JLog.i(TAG, s + "onSuccess");
//                    }
//                });

//                break;
        }
    }

    /**支付流程*/
    private void StartPay() {
//        String cporderid = System.currentTimeMillis()   + "";
      /*prize add by bianxinhao 2017年3月13日15:27:06  start */
        String cporderid =  CommonUtils.queryUserId()+ bean.getId()+"3";
        /*prize add by bianxinhao 2017年3月13日15:27:06  end*/
        String param = PayConfig.getTransdata(fontID, "字体支付成功" , 2 , Float.parseFloat(price) , cporderid,"字体"+name);
        IAppPay.startPay(FontDetailActivity.this, param, iPayResultCallback);
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
                    if(payState){
                        //支付成功  上传服务器
                        buyTV.setVisibility(View.INVISIBLE);
                        btDownload.setVisibility(View.VISIBLE);
                        PushPayHistory();
                        Toast.makeText(FontDetailActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                    }
                    break;
                case IAppPay.PAY_ING:
                    Toast.makeText(FontDetailActivity.this, "成功下单", Toast.LENGTH_LONG).show();
                    break ;
                case 3001:
                case 6110:
                    buyTV.setVisibility(View.INVISIBLE);
                    btDownload.setVisibility(View.VISIBLE);
                    PushPayHistory();
                    isBuy = "1";
                    initDownloadButton();
                    Toast.makeText(FontDetailActivity.this, "商户订单已经支付成功", Toast.LENGTH_LONG).show();
                default:
                    Toast.makeText(FontDetailActivity.this, resultInfo, Toast.LENGTH_LONG).show();
                    break;
            }
            JLog.i("hu", "FontDetailActivity ==  requestCode:" + resultCode + ",signvalue:" + signvalue + ",resultInfo:" + resultInfo);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fontFragment != null && !fontFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fontFragment)
                    .commitAllowingStateLoss();
        }
        ViewGroup root = (ViewGroup)findViewById(R.id.container);
        if (root.getChildCount()>1) root.removeViewAt(0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DataStoreUtils.readShareInfo(DataStoreUtils.FONT_DETAIL_KEY)||isPushBack){
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                isPushBack = false;
                DataStoreUtils.removeShareInfo(DataStoreUtils.FONT_DETAIL_KEY);
                // 监控返回键
                UIUtils.gotoActivity(MainActivity.class);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showProgressBar(final boolean isSuccess){
        if (rightPopupWindow != null) {
            if (!rightPopupWindow.isShowing()) {
                rightPopupWindow.show();
            } else {
                rightPopupWindow.dismiss();
            }
        } else {
            initPop(this);
        }
        new Handler().postDelayed(new Runnable(){

            public void run() {
                rightPopupWindow.dismiss();
                if(isSuccess){
                    ToastUtils.showToast(getString(R.string.changing_font_suc));
                }else{
                    ToastUtils.showToast(getString(R.string.changing_font_failed));
                }
            }
        }, 5000);
    }

    private  void initPop(Activity context) {
        rightPopupWindow = new AlertDialog.Builder(context).create();
        rightPopupWindow.show();
        View loginwindow = context.getLayoutInflater().inflate(
                R.layout.popwindow_setwallpaper_layout, null);
        TextView textView = (TextView) loginwindow.findViewById(R.id.launcher_TV);
        textView.setText("正在更换字体");
        Window window = rightPopupWindow.getWindow();
        window.setContentView(loginwindow);
        WindowManager.LayoutParams p = window.getAttributes();
        WindowManager wm = (WindowManager) MainApplication.curContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        if(width<=720){
            p.width = 600;
        }else{
            p.width = 900;
        }
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(p);
        window.setGravity(Gravity.CENTER);
    }
}
