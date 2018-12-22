package com.prize.appcenter.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.TopicItemBean;
import com.prize.app.constants.Constants;
import com.prize.app.database.CoverTable;
import com.prize.app.database.dao.CoverDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsKeyInstallingPageItemBean;
import com.prize.app.net.datasource.base.AppsKeyInstallingPageListData;
import com.prize.app.net.datasource.base.CoverData;
import com.prize.app.net.datasource.base.CoverItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.datamgr.AppListDataManager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.model.ExposureBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FrontCoverActivity extends RootActivity implements
        View.OnClickListener {
    private ImageView frontCover;
    private View toNext;
    private boolean isToMainFrame = false;
    private String TAG = "FrontCoverActivity";
    private ServiceToken mToken;
    private static boolean isOnbackTo = false;
    /****
     * modify by longbaoxiu 余兴要求由2000—>3000 20160602 2500-->3.1 modify
     *****/
    final long DEFAULT_DEALY = 2500;
    final long NOTREQUEST_DEALY = 1000;

    private long mDelayed = DEFAULT_DEALY;
    private boolean isLoadCoverData = true;
    private AppListDataManager listDataManager;
    private ArrayList<AppsKeyInstallingPageItemBean> onekeyLists = new ArrayList<AppsKeyInstallingPageItemBean>();
    private boolean isNeedQuestAdData = true;
    private int[] pageIndext = new int[5];
    private String loacal_versionCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        loacal_versionCode = DataStoreUtils
                .readLocalInfo(DataStoreUtils.VERSION_CODE);
        String currentCode = AppManagerCenter.getLocalVersionCode();

        if (TextUtils.isEmpty(loacal_versionCode)
                || !loacal_versionCode.equals(currentCode)) {
            isNeedQuestAdData = false;
            ImageLoader.getInstance().clearMemoryCache();
            mToken = AIDLUtils.bindToService(this);
            doRequestKeyInstallData();
            DataStoreUtils.saveLocalInfo(DataStoreUtils.VERSION_CODE,
                    currentCode);
        }

        setContentView(R.layout.loading_cover);
        // 封面View
        frontCover = (ImageView) findViewById(R.id.loading_img);
        if (isLoadCoverData) {
            frontCover.setOnClickListener(this);
        } else {
            mDelayed = NOTREQUEST_DEALY;
        }

        toNext = this.findViewById(R.id.to_next);
        isToMainFrame = false;
        toNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MTAUtil.onStartupSkipClick();
                gotoMainActivityOnclick();
            }

        });

        frontCover.setScaleType(ScaleType.FIT_XY);
        if (isLoadCoverData) {
            mHandler.postDelayed(mTimerOutTask, DEFAULT_DEALY);
            mStartPostTime = System.currentTimeMillis();

            /* modify by longbaoxiu 余兴要求除非重新打开应用（重启Application才显示广告）20160602 ****/
            if (isNeedQuestAdData && BaseApplication.isOnCreate || isNeedQuestAdData && isBetweenOneH()) {
                new DisplayImageLoacalTask().execute();
                BaseApplication.isOnCreate = false;
                DataStoreUtils.saveLocalInfo("TIME", System.currentTimeMillis() + "");
            }
        }
    }

    /**
     * 是否超过1小时
     *
     * @return boolean
     */
    private boolean isBetweenOneH() {
        String lastTime = DataStoreUtils.readLocalInfo("TIME");
        return TextUtils.isEmpty(lastTime) || ((System.currentTimeMillis() - Long.parseLong(lastTime)) > 1000 * 60 * 60);
    }

    private void putdate(ContentValues cv, CoverItemBean item) {
        cv.put(CoverTable.COVERID, item.id);
        cv.put(CoverTable.ASSOCIATEID, item.associateId);
        cv.put(CoverTable.ADTYPE, item.adType);
        cv.put(CoverTable.TITLE, item.title);
        cv.put(CoverTable.IMAGEURL, item.imageUrl);
        cv.put(CoverTable.URL, item.url);
        cv.put(CoverTable.DESCRIPTION, item.description);
        cv.put(CoverTable.CREATETIME, item.createTime);
        cv.put(CoverTable.STATUS, item.status);
        cv.put(CoverTable.POSITION, item.position);
        cv.put(CoverTable.SECONDS, item.seconds);
        cv.put(CoverTable.STARTTIME, item.startTime);
        cv.put(CoverTable.ENDTIME, item.endTime);

    }

    private boolean b = false;
    ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            /* 请求加载图片超过2秒 */
            long mStartLoadImgTime = System.currentTimeMillis();
            long delay = DEFAULT_DEALY - (mStartLoadImgTime - mStartPostTime);
            if (isOnbackTo)
                mHandler.removeCallbacks(mTimerOutTask);
            mHandler.postDelayed(mTimerOutTask, delay);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view,
                                    FailReason failReason) {

            long delay = System.currentTimeMillis();
            long time = delay - mStartPostTime;
            long okTime = time < 1000 ? 1000 - time : 0;
            gotoMainActivity(okTime);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            synchronized (lock) {
                if (isOnbackTo) {
                    mHandler.removeCallbacks(mTimerOutTask);
                    mHandler.removeCallbacks(mRunnable);
                }
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = loadedImage;
                mHandler.sendMessage(msg);

                long delay = System.currentTimeMillis();
                long time = delay - mStartPostTime;
                long okTime = time < mDelayed ? mDelayed - time : 0;

                if (mDelayed != DEFAULT_DEALY) {
                    mHandler.removeCallbacks(mTimerOutTask);
                    b = true;
                    gotoMainActivity(okTime);
                    return;
                }
                if (mDelayed == DEFAULT_DEALY && !isOnbackTo) {
                    mHandler.removeCallbacks(mTimerOutTask);
                    b = true;
                    gotoMainActivity(okTime);
                    return;
                }
                if (isOnbackTo) {
                    gotoMainActivity(okTime);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    };

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case AppListDataManager.KEY_INSALL_FAILURE:
                break;
            case AppListDataManager.KEY_INSALL_SUCCESS:
                AppsKeyInstallingPageListData oneKeyPages = (AppsKeyInstallingPageListData) obj;
                if (oneKeyPages != null && oneKeyPages.onekeylist != null
                        && oneKeyPages.onekeylist.size() > 0) {
//                    pageIndext=new int[oneKeyPages.onekeylist.size()];
                    onekeyLists.addAll(oneKeyPages.onekeylist);
                }
                break;
        }
    }

    /**
     * 启动页的图片路径
     */
    private String mUrl;

    private boolean isfirst = false;
    private Object lock = new Object();

    private class DisplayImageLoacalTask extends
            AsyncTask<Void, Void, ArrayList<CoverItemBean>> {

        @Override
        protected ArrayList<CoverItemBean> doInBackground(Void... params) {
            ArrayList<CoverItemBean> coverList;
            /* modify by longbaoxiu 余兴要求启动页保证最少显示0.5s---20160602 ****/
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            coverList = CoverDAO.queryAll();
            if (JLog.isDebug) {
                JLog.i(TAG, "DisplayImageLoacalTask-coverList.size()=" + coverList.size());
            }
            if (coverList != null && coverList.size() > 0) {
                getImgUrl(coverList);
                ImageLoader.getInstance().displayImage(mUrl, frontCover,
                        UILimageUtil.getLoadingLoptions(),
                        mImageLoadingListener);
            } else {
                isfirst = true;
                long delay = System.currentTimeMillis();
                long time = delay - mStartPostTime;
                long okTime = time < DEFAULT_DEALY ? DEFAULT_DEALY - time : 0;

                forceGotoMainActivity(okTime);
            }

            requestData();
            return coverList;
        }

        @Override
        protected void onPostExecute(ArrayList<CoverItemBean> coverList) {
        }
    }

    private void getImgUrl(ArrayList<CoverItemBean> bean) {

        ArrayList<CoverItemBean> coveritems = isShowCoverPaper(bean);
        CoverItemBean result = null;
        if (coveritems != null && coveritems.size() > 0) {
            int choiceId = (int) (Math.random() * coveritems.size());
            result = coveritems.get(choiceId);

            String url = result != null ? result.imageUrl : null;

            long seconds = result != null ? ((result.seconds) * 1000) : 0;
            mDelayed = seconds != 0 ? seconds : DEFAULT_DEALY;

            mUrl = url;
            frontCover.setTag(result);
        }
        frontCover.setTag(result);
    }

    private ArrayList<CoverItemBean> isShowCoverPaper(
            ArrayList<CoverItemBean> coverList) {

        String startTime;
        String endTime;
        ArrayList<CoverItemBean> coveritems = new ArrayList<CoverItemBean>();

        for (int i = 0; i < coverList.size(); i++) {

            startTime = coverList.get(i).startTime;
            endTime = coverList.get(i).endTime;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // long l = 24*60*60*1000-1; //每天的毫秒数
                long startMills = sdf.parse(startTime).getTime();
                long endMills = sdf.parse(endTime).getTime();
                long currentMills = new Date().getTime();
                if (currentMills > startMills && currentMills < endMills) {
                    coveritems.add(coverList.get(i));
                } else {
                    CoverDAO.deleOverdueData(coverList.get(i));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return coveritems;
    }

    private long mStartPostTime = 0;
    Handler mHandler = new MyHander(this);
    private Runnable mTimerOutTask = new Runnable() {
        @Override
        public void run() {
            if (onekeyLists.size() > 0) {
                for (int i = 0; i < onekeyLists.size() && i <= 4; i++) {//最多不能超过5页数据，防止后台人员添加多，增加页数判断 modify by：龙宝修 2.8版本
                    AppsKeyInstallingPageItemBean pageItem = onekeyLists.get(i);
                    if (isNewVersion) {
                        CommonUtils.filterAndRemoveInstalled(pageItem.apps);
                    } else {
                        pageItem.apps = CommonUtils.filterInstalled(pageItem.apps);
                    }

                    if (pageItem.apps.size() < 3) {
                        pageIndext[i] = 0;
                        continue;
                    }
                    pageIndext[i] = 1;
                }

                ArrayList<AppsKeyInstallingPageItemBean> onekeyLists_rm = new ArrayList<AppsKeyInstallingPageItemBean>();

                for (int i = 0; i < pageIndext.length && i < onekeyLists.size(); i++) {
                    if (pageIndext[i] == 0) {
                        onekeyLists_rm.add(onekeyLists.get(i));
                    }
                }

                onekeyLists.removeAll(onekeyLists_rm);

                if (onekeyLists.size() > 0) {
                    fadeToOnkeyFragmentActivity(onekeyLists);
                } else {
                    fadeToMainActivity();
                }
            } else {
                fadeToMainActivity();
            }
        }
    };

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (listDataManager != null) {
            listDataManager.setNullListener();
        }
        AIDLUtils.unbindFromService(mToken);
        super.onDestroy();
    }

    private void gotoMainActivity(long delay) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isToMainFrame) {
                    return;
                }
                isToMainFrame = true;
                fadeToMainActivity();
                finish();
            }
        }, delay);
    }

    private void gotoMainActivityOnclick() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            if (isToMainFrame) {
                return;
            }
            isToMainFrame = true;
            fadeToMainActivity();
        }
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (isToMainFrame) {
//                    return;
//                }
//                isToMainFrame = true;
//                fadeToMainActivity();
//                finish();
//            }
//        }, delay);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            if (isToMainFrame) {
                return;
            }
            isToMainFrame = true;
            if (onekeyLists.size() > 0) {
                fadeToOnkeyFragmentActivity(onekeyLists);
            } else {
                fadeToMainActivity();
            }
            finish();
        }
    };

    private void forceGotoMainActivity(long delay) {
        mHandler.postDelayed(mRunnable, delay);
    }


    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public String getActivityName() {
        return "FrontCoverActivity";
    }

    @Override
    public void onClick(View v) {
        CoverItemBean bean = (CoverItemBean) v.getTag();
        if (bean == null) {
            return;
        }
        if (!b) {
            return;
        }
        switch (bean.adType) {
            case "web":
                if (isToMainFrame) {
                    return;
                }
                isToMainFrame = true;
                Intent i = new Intent(FrontCoverActivity.this,
                        WebViewActivity.class);
                i.putExtra(WebViewActivity.P_URL, bean.url);
                this.startActivityForResult(i, 0);
                if (!TextUtils.isEmpty(bean.url)) {
                    MTAUtil.onWelcomeAds(bean.url, "web");
                    MTAUtil.onUMWelcomeAds(bean.url, "web");
                }
                break;
            case "topic":
                gotoTopic(bean);
                if (!TextUtils.isEmpty(bean.associateId)) {
                    MTAUtil.onWelcomeAds(bean.associateId, "topic");
                    MTAUtil.onUMWelcomeAds(bean.associateId, "topic");
                }
                break;
            case "app":
                gotoAppDetail(bean.associateId);
                if (!TextUtils.isEmpty(bean.associateId)) {
                    MTAUtil.onWelcomeAds(bean.associateId, "app");
                    MTAUtil.onUMWelcomeAds(bean.associateId, "app");
                }
            case "pointapplist":
                if (isToMainFrame) {
                    return;
                }
                isToMainFrame = true;
                Intent intent = new Intent(this, PersonalEarnPointsActivity.class);
                intent.putExtra(Constants.FROM, "startPage");
                this.startActivity(intent);
                if (!TextUtils.isEmpty(bean.associateId)) {
                    MTAUtil.onWelcomeAds(bean.associateId, "pointapplist");
                    MTAUtil.onUMWelcomeAds(bean.associateId, "pointapplist");
                }
                finish();
                break;
            case "pointgoods":
                if (isToMainFrame) {
                    return;
                }
                isToMainFrame = true;
                Intent intent1 = new Intent(this, PersonalPointsMallActivity.class);
                intent1.putExtra(Constants.FROM, "startPage");
                this.startActivity(intent1);

                if (!TextUtils.isEmpty(bean.associateId)) {
                    MTAUtil.onWelcomeAds(bean.associateId, "pointgoods");
                    MTAUtil.onUMWelcomeAds(bean.associateId, "pointgoods");
                }
                finish();
                break;
        }
    }

    public void gotoAppDetail(String appId) {
        if (isToMainFrame) {
            return;
        }
        ExposureBean bean = new ExposureBean();
        bean.gui = Constants.STARTPAGE_GUI;
        bean.appId = appId;
        bean.widget = "default";
        isToMainFrame = true;
        Intent intent = new Intent(this,
                AppDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("AppsItemBean", null);
        bundle.putSerializable("pageInfo", new Gson().toJson(bean));
        bundle.putString("appid", appId);
        intent.putExtra("bundle", bundle);
        this.startActivityForResult(intent, 0);
    }

    private void gotoTopic(CoverItemBean bean) {
        if (isToMainFrame) {
            return;
        }
        isToMainFrame = true;
        Intent intent = new Intent(this,
                TopicDetailActivity.class);
        TopicItemBean topicItemBean = new com.prize.app.beans.TopicItemBean();
        topicItemBean.description = bean.description;
        topicItemBean.title = bean.title;
        topicItemBean.imageUrl = bean.imageUrl;
        topicItemBean.id = bean.associateId;
        Bundle b = new Bundle();
        b.putSerializable("bean", topicItemBean);
        intent.putExtras(b);
        this.startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        isToMainFrame = false;
        gotoMainActivity(0);
        this.finish();
        super.onActivityResult(arg0, arg1, arg2);
    }

    /***
     * 解析返回结果
     *
     * @param result 服务器返回结果
     */
    private void onResponse(String result) {
        JLog.i(TAG, "onSuccess--result=");
        isOnbackTo = true;

        try {
            JSONObject obi = new JSONObject(result);
            int code = obi.getInt("code");
            JLog.i(TAG, "onSuccess--code=" + code);
            boolean display = false;
            if (code == 0) {
                isOnbackTo = true;
                String response = new JSONObject(result).getString("data");
                CoverData coverData = GsonParseUtils.parseSingleBean(response,
                        CoverData.class);
                ArrayList<CoverItemBean> coverbean = coverData.ads;

                List<ContentValues> values = new ArrayList<>();
                if (JLog.isDebug) {
                    JLog.i(TAG, "onSuccess--coverbean.size()=" + coverbean.size());
                }
                for (int i = 0; i < coverbean.size(); i++) {
                    ContentValues cv = new ContentValues();
                    putdate(cv, coverbean.get(i));
                    if (!display)
                        display = CoverDAO.isupdate(cv);
                    values.add(cv);
                }
                JLog.i(TAG, "onSuccess--display=" + display);
                if (display) {
                    getImgUrl(coverbean);
                    mHandler.removeCallbacks(mTimerOutTask);
                    final ImageView img = isfirst ? new ImageView(
                            FrontCoverActivity.this) : frontCover;
                    JLog.i(TAG, "onSuccess--result-ImageLoader");
                    ImageLoader.getInstance().displayImage(mUrl, img,
                            UILimageUtil.getLoadingLoptions(),
                            mImageLoadingListener);
                }

                CoverDAO.updateCoverDb(values);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            JLog.i(TAG, "onSuccess--JSONException=" + e.getMessage());

        }
    }

    private void fadeToMainActivity() {
        JLog.i(TAG, "fadeToMainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void fadeToOnkeyFragmentActivity(ArrayList<AppsKeyInstallingPageItemBean> itemBeans) {
        JLog.i(TAG, "fadeToOnkeyFragmentActivity");
        Intent intent = new Intent(this, OneKeyUpdateActivity.class);
        if (isNewVersion) {
            intent = new Intent(this, OneKeyFragmentActivity.class);
        }
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putParcelableArrayListExtra("datas", itemBeans);
        intent.putExtra("page_flag", pageIndext);
        intent.putExtra("isNewVersion", isNewVersion);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    /**
     * 请求启动页的数据
     */
    private void requestData() {
        String url = Constants.GIS_URL + "/recommand/startupads";
        RequestParams params = new RequestParams(url);
        params.setConnectTimeout(3000);
        try {
            onResponse(XExtends.http().
                    postSync(params, String.class));
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private boolean isNewVersion = false;

    /**
     * 方法描述：获得一键安装的数据
     */
    private void doRequestKeyInstallData() {
        if (listDataManager == null) {
            listDataManager = new AppListDataManager(this);
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "loacal_versionCode=" + loacal_versionCode);
        }
        if (TextUtils.isEmpty(loacal_versionCode) || DataStoreUtils.DEFAULT_VALUE.equals(loacal_versionCode)) {
            listDataManager.doKeyInstallingNetSource(TAG, String.valueOf(0));
            isNewVersion = true;
        } else {
            listDataManager.doKeyInstallingNetSource(TAG, String.valueOf(2));
        }
    }

    private static class MyHander extends Handler {
        private WeakReference<FrontCoverActivity> mActivities;

        MyHander(FrontCoverActivity mActivity) {
            this.mActivities = new WeakReference<FrontCoverActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final FrontCoverActivity activity = mActivities.get();
            if (activity != null) {
                if (msg != null) {
                    switch (msg.what) {
                        case 0:
                            if (msg.obj != null) {
                                activity.toNext.setVisibility(View.VISIBLE);
                                activity.frontCover.setImageBitmap((Bitmap) msg.obj);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

}