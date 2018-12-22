package com.prize.prizethemecenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.cloud.bean.Person;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.bean.SingleThemeItemBean;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.manage.UIDownLoadListener;
import com.prize.prizethemecenter.request.WallDownloadHistoryRequest;
import com.prize.prizethemecenter.request.WallpaperDetailRequest;
import com.prize.prizethemecenter.response.SingleThemeDetailResponse;
import com.prize.prizethemecenter.ui.utils.BitmapUtils;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.MTAUtil;
import com.prize.prizethemecenter.ui.utils.UILimageUtil;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.widget.DownLoadButton;
import com.prize.prizethemecenter.ui.widget.ImgForWallHorizontalScrollview;
import com.prize.prizethemecenter.ui.widget.view.MySeekBar;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.x;

import java.io.File;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 壁纸详情页
 * Created by pengyang on 2016/10/13.
 */
public class WallDetailActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "hu";
    @InjectView(R.id.action_bar_back)
    ImageButton actionBarBack;
    @InjectView(R.id.wallpaper_name_TV)
    TextView wallpaperNameTV;
    @InjectView(R.id.wallpaper_size_TV)
    TextView wallpaperSizeTV;
    @InjectView(R.id.container_wait)
    FrameLayout containerWait;
    @InjectView(R.id.container_reload)
    FrameLayout containerReload;
    @InjectView(R.id.wall_show_view)
    ImageView wallShowView;
    @InjectView(R.id.wall_container)
    RelativeLayout wallContainer;
    @InjectView(R.id.action_bar_no_tab)
    RelativeLayout actionBarNoTab;
    @InjectView(R.id.look_TV)
    TextView lookTV;
    @InjectView(R.id.use_TV)
    TextView useTV;
    @InjectView(R.id.bottom_RL)
    RelativeLayout bottomRL;
    @InjectView(R.id.seekBar)
    MySeekBar seekBar;
    @InjectView(R.id.wallpaper_View)
    ImgForWallHorizontalScrollview wallpaperView;
    @InjectView(R.id.wall_download_btn)
    DownLoadButton wallDownloadBtn;
    @InjectView(R.id.bottom_RL_container)
    RelativeLayout bottomRLContainer;

    private WallpaperDetailRequest request;
    private SingleThemeDetailResponse response;

    private WallDownloadHistoryRequest downloadRequest;

    /**
     * 壁纸ID
     */
    private String wallID;
    /**
     * 壁纸的滑动范围
     */
    private int widthRange;
    private boolean isShow = true;

    private AlertDialog rightPopupWindow = null;

    private SingleThemeItemBean.ItemsBean data;

    private static Bitmap bitmap;
    private String wallType;

    private String picPath;
    private UIDownLoadListener listener;
    private boolean isPushBack = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (wallDownloadBtn != null) {
                wallDownloadBtn.invalidate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.wallpaper_detail_layout);
        ButterKnife.inject(this);
        bottomRLContainer.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );

        if (getIntent() != null) {
            wallID = getIntent().getStringExtra("wallID");
            picPath = getIntent().getStringExtra("minPic");
        }
        initLoadVIew();
        // 接收推送默认开启
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
        if (!DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            initPushData();
        }
        loadData();
        setListener();
        setToLauncher();
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            this.getWindow().getDecorView().setSystemUiVisibility(
//                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
////                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            );
//        }
//        else{
//            this.getWindow().getDecorView().setSystemUiVisibility(
//                      View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//    }

    private void initPushData() {
        XGPushClickedResult xgPushClickedResult = XGPushManager
                .onActivityStarted(this);
        if (xgPushClickedResult != null) {
            String pushJson = xgPushClickedResult.getCustomContent();
            isPushBack = true;
            String pic=null;
            Log.d("TPush", "WallDetailActivity为：" + pushJson);
            if (!TextUtils.isEmpty(pushJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(pushJson);
                    wallID = jsonObject.getString("wallID").trim();
                    pic = jsonObject.getString("minPic").trim();
                    String picPaths = UILimageUtil.getPicPath(this, pic);
                    picPath = UILimageUtil.formatDirPic(picPaths);
                    Log.d(TAG, "initPushData: "+ picPath);
                    wallType = jsonObject.getString("wallType");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setListener() {
        actionBarBack.setOnClickListener(this);
        wallpaperNameTV.setOnClickListener(this);
        wallShowView.setOnClickListener(this);
        wallpaperNameTV.setOnClickListener(this);
//        DownloadManager.getInstance().addDownloadListener(wallDownloadBtn);
        wallDownloadBtn.setOnClickListener(this);
        wallpaperView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                float index = (float) scrollX / widthRange * 100;
                seekBar.setProgress((int) index);
            }
        });
        useTV.setOnClickListener(this);
        lookTV.setOnClickListener(this);
        lookTV.getBackground().setAlpha(216);

        wallDownloadBtn.setCompleteCallBack(new DownLoadButton.CompleteCallBack() {
            @Override
            public void onStates() {
                wallDownloadBtn.setVisibility(View.GONE);
                lookTV.setVisibility(View.VISIBLE);
                useTV.setVisibility(View.VISIBLE);
                useTV.getBackground().setAlpha(216);
                PushDownloadHistory();
            }
        });
    }

    /***
     * 上传壁纸下载记录
     */
    private void PushDownloadHistory() {
        downloadRequest = new WallDownloadHistoryRequest();
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            downloadRequest.userid = CommonUtils.queryUserId();
        } else {
            downloadRequest.userid = "0";
        }
        Person person = UIUtils.queryUserPerson(this);
        if(person==null)
            return;
        downloadRequest.wallpaper_id = Integer.parseInt(wallID);
        downloadRequest.model = ClientInfo.getInstance().getModel();
        downloadRequest.user_icon = person.getAvatar();
        downloadRequest.user_name = person.getRealName();
        JLog.i("hu", "PushDownloadHistory " + downloadRequest.user_name + "::" + downloadRequest.user_icon);
        x.http().post(downloadRequest, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, " wallpaper  download Success" + result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                JLog.i(TAG, "wallpaper Throwable" + ex + "--isOnCallback==" + isOnCallback);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }

    private void initLoadVIew() {
        View waiting_view = LayoutInflater.from(this).inflate(R.layout.waiting_view, null);
        View reload_layout = LayoutInflater.from(this).inflate(R.layout.reload_layout, null);
        LinearLayout loadingView = (LinearLayout) waiting_view.findViewById(R.id.loading_Llyt_id);
        LinearLayout reloadView = (LinearLayout) reload_layout.findViewById(R.id.reload_Llyt);
        loadingView.setGravity(Gravity.CENTER);
        reloadView.setGravity(Gravity.CENTER_HORIZONTAL);
        reloadView.setPadding(0,440,0,0);
        containerWait.addView(waiting_view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        containerReload.addView(reload_layout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        seekBar.setMax(100);
        seekBar.setProgress(50);
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
            containerReload.setVisibility(View.GONE);
            containerWait.setVisibility(View.VISIBLE);
            getWallData();
        } else {
            containerWait.setVisibility(View.GONE);
            containerReload.setVisibility(View.GONE);
            wallContainer.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取不同尺寸图片   // @1440w_1080h_2e   图片请求拼接规则
     *
     * @param wallType 1.单屏 2.双屏
     * @param wallPic  图片URL
     * @return //960*854(480),1440*960(720),1440*1280(720),2160*1920(1080)
     */
    public String getPicPX(String wallType, String wallPic) {
        int width = ClientInfo.screenWidth;
        int height = ClientInfo.screenHeight;
        if ("2".equals(wallType)) {
            width = width * 2;
        }
        if (height <= 960) {
            height = 960;
        } else if (height > 960 && height <= 1280) {
            height = 1280;
        } else if (height > 1280 && height <=1920) {
            height = 1920;
        }
        StringBuffer b = new StringBuffer(wallPic);
        String p = b.append("@").append(width).append("w_").append(height).append("h_2e.webp").toString();
//        JLog.i(TAG, "width=" + width + "--height=" + height + "--p=" + p);
        return p;
    }


    private void getWallData() {
        request = new WallpaperDetailRequest();
        request.wallpaperId = Integer.parseInt(wallID);

        x.http().post(request, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        response = CommonUtils.getObject(result,
                                SingleThemeDetailResponse.class);
                        if(response!=null){
                            wallContainer.setVisibility(View.VISIBLE);
                        }

                        data = response.data.getItems().get(0);
                        wallID = data.getId();
                        wallpaperNameTV.setText(data.getName());
                        wallpaperSizeTV.setText(data.getSize());
                        wallType = data.getWallpaper_type();
                        String wallPic = data.getWallpaper_pic();
                        //modify for fix 1080p mobile display 2016.11.28
                        ImageLoader.getInstance().displayImage(getPicPX(wallType, wallPic), wallShowView,
                                null, imageListener);
                        //设置缩略图
                        if (picPath == null) {
                            picPath = data.ad_pictrue;
                        }
                        data.setThumbnail(picPath);
                        wallDownloadBtn.setData(data, 2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                containerWait.setVisibility(View.GONE);
                wallContainer.setVisibility(View.INVISIBLE);
                containerReload.setVisibility(View.VISIBLE);
                containerReload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

    ImageLoadingListener imageListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            containerWait.setVisibility(View.GONE);
            WindowManager wm = (WindowManager) MainApplication.curContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            widthRange = loadedImage.getWidth() - width;
            if (loadedImage.getWidth() > width) {
                seekBar.setVisibility(View.VISIBLE);
            } else {
                seekBar.setVisibility(View.GONE);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 水平直接滚动widthRange/2，效果更平滑可使用smoothScrollTo(int x, int y)
                    wallpaperView.smoothScrollTo(widthRange / 2, 0);
                }
            }, 100);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.action_bar_back:
                this.finish();
                break;
            case R.id.action_bar_title:
                this.finish();
                break;
            case R.id.wall_show_view:
                if (isShow) {
                    actionBarNoTab.setVisibility(View.GONE);
                    actionBarNoTab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.out_to_up));
                    seekBar.setVisibility(View.GONE);
                    bottomRL.setVisibility(View.GONE);
                    bottomRL.startAnimation(AnimationUtils.loadAnimation(this, R.anim.out_to_down));
                    isShow = false;
                } else {
                    actionBarNoTab.setVisibility(View.VISIBLE);
                    actionBarNoTab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_up));
                    if(wallType.equals("2")){
                        seekBar.setVisibility(View.VISIBLE);
                    }else{
                        seekBar.setVisibility(View.GONE);
                    }
                    bottomRL.setVisibility(View.VISIBLE);
                    bottomRL.startAnimation(AnimationUtils.loadAnimation(this, R.anim.in_from_down));
                    isShow = true;
                }
                break;
            case R.id.wall_download_btn:
                    MTAUtil.onClickSingleWall();
                    wallDownloadBtn.OnClick(WallDetailActivity.this);
                break;
            case R.id.use_TV:
                //rightPopupWindow
                if (rightPopupWindow != null) {
                    if (!rightPopupWindow.isShowing()) {
                        rightPopupWindow.show();
                    } else {
                        rightPopupWindow.dismiss();
                    }
                } else {
                    initPop();
                }
                break;
            case R.id.look_TV:  //预览界面
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putExtra("wallID", wallID);
                intent.putExtra("wallType", wallType);
                intent.putExtra("localWallPath", picPath);
                intent.putExtra("activityType", 0);
                startActivity(intent);
                break;
            case R.id.launcher_TV:   //桌面
                setWallpaper(1);
                break;
            case R.id.screen_lock_RL:   //锁屏
                setWallpaper(2);
                break;
            case R.id.all_TV:   //全部
                setWallpaper(3);
                break;
            case R.id.wallpaper_name_TV:
                this.finish();
                break;
        }
    }

    private void setWallpaper(int type) {
        String wallPath = FileUtils.getDir("wallpaper") + wallID+2+ ".zip";
        if (bitmap == null) {
            bitmap = BitmapUtils.getWallpaper(this, wallPath);
        }
        /**防止壁纸包被锁屏删除 start by pengy*/
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        if (BitmapUtils.isExistFile(FileUtils.getDir("cache") + "/" + s + "_wall" + ".zip")) {
            s = random.nextInt(max) % (max - min + 1) + min;
        }
        File file = BitmapUtils.createFile(FileUtils.getDir("cache"), s + "_wall" + ".zip");
        BitmapUtils.saveBitmapToFile(bitmap, file);
        /**防止壁纸包被锁屏删除 end by pengy*/
        if (type == 1) {
            BitmapUtils.applyType(WallDetailActivity.this, type, bitmap, null,0,false);
        } else {
            BitmapUtils.applyType(WallDetailActivity.this, type, bitmap, file.getAbsolutePath(),0,false);
        }
        rightPopupWindow.dismiss();
    }

    private void setToLauncher() {
        sendBroadcast(new Intent(Constants.BRD_ACTION_APPLY_LAUNCHER));
    }

    private void initPop() {
        rightPopupWindow = new AlertDialog.Builder(this,
                R.style.wallpaper_use_dialog_style).create();
        rightPopupWindow.show();
        View loginwindow = this.getLayoutInflater().inflate(
                R.layout.popwindow_tags_layout, null);
        loginwindow.setBackgroundColor(getResources().getColor(R.color.white));
        TextView launcherTV = (TextView) loginwindow.findViewById(R.id.launcher_TV);
        TextView all_TV = (TextView) loginwindow.findViewById(R.id.all_TV);
        RelativeLayout screen_lock_RL = (RelativeLayout) loginwindow.findViewById(R.id.screen_lock_RL);
        launcherTV.setOnClickListener(this);
        all_TV.setOnClickListener(this);
        screen_lock_RL.setOnClickListener(this);

        Window window = rightPopupWindow.getWindow();
        // 设置窗口的内容页面,shrew_exit_dialog.xml文件中定义view内容
        window.setContentView(loginwindow);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        WindowManager.LayoutParams p = window.getAttributes();
        p.width = screenWidth;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(p);
        window.setGravity(Gravity.CENTER | Gravity.BOTTOM); // 此处可以设置dialog显示的位置
        rightPopupWindow.setContentView(loginwindow);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wallDownloadBtn != null) {
//            try {
            /**change downloadbutton state for delete file start by pengy*/
            if(!new File(FileUtils.getDir("wallpaper") + wallID +"2"+ ".zip").exists()) {
                wallDownloadBtn.setVisibility(View.VISIBLE);
                lookTV.setVisibility(View.GONE);
                useTV.setVisibility(View.GONE);
            }
            /**change downloadbutton state for delete file end by pengy*/
            wallDownloadBtn.invalidate();
//                wallDownloadBtn.startListener();
//            } catch (DbException e) {
//                e.printStackTrace();
//            }
        }
        StatService.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManagerCenter.removeDownloadRefreshHandle(listener);
        mHandler.removeMessages(0);

        if (PreviewActivity.bitmap != null && !PreviewActivity.bitmap.isRecycled()) {
            PreviewActivity.bitmap.recycle();
            PreviewActivity.bitmap = null;
            System.gc();
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
        if (PreviewActivity.cut != null && !PreviewActivity.cut.isRecycled()) {
            PreviewActivity.cut.recycle();
            PreviewActivity.cut = null;
            System.gc();
        }
        XGPushManager.onActivityStoped(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }

    public void setApplyType(int type) {
        if (type == 1 || type == 3) {
            DownloadInfo mDownloadInfo = DBUtils.findDownloadById(wallID+2);
            if(mDownloadInfo!=null){
                mDownloadInfo.setCurrentState(7);
            }
            DBUtils.saveOrUpdateDownload(wallID,2);
            DownloadTaskMgr.getInstance().setDownlaadTaskState(wallID,2);
            DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED, wallID);
            UIUtils.updateLocalStates("wallpaperId",WallDetailActivity.this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isPushBack){
            if(keyCode == KeyEvent.KEYCODE_BACK) {
                // 监控返回键
                UIUtils.gotoActivity(MainActivity.class);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
