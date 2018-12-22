package com.prize.prizethemecenter.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.constants.Constants;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.DownloadInfo;
import com.prize.prizethemecenter.manage.DownloadState;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;
import com.prize.prizethemecenter.ui.utils.BitmapUtils;
import com.prize.prizethemecenter.ui.utils.CommonUtils;
import com.prize.prizethemecenter.ui.utils.Constant;
import com.prize.prizethemecenter.ui.utils.DBUtils;
import com.prize.prizethemecenter.ui.utils.FileUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.UIUtils;
import com.prize.prizethemecenter.ui.zoomcropimage.CropImageLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 预览界面
 * Created by Administrator on 2016/10/17.
 */
public class PreviewActivity extends Activity implements View.OnClickListener {

    @InjectView(R.id.screen_preview_TV)
    TextView screenPreviewTV;
    @InjectView(R.id.launcher_preview_TV)
    TextView launcherPreviewTV;
    @InjectView(R.id.isScroll_TV)
    TextView isScrollTV;
    @InjectView(R.id.cancel_TV)
    TextView cancelTV;
    @InjectView(R.id.use_TV)
    TextView useTV;
    @InjectView(R.id.image_preview_layout)
    CropImageLayout imagePreviewLayout;
    @InjectView(R.id.img_preview)
    ImageView imgPreview;
    @InjectView(R.id.lock_LL)
    LinearLayout lockLL;
    @InjectView(R.id.days_TV)
    TextView daysTV;
    @InjectView(R.id.week_TV)
    TextView weekTV;
    @InjectView(R.id.time_TV)
    TextView timeTV;
    @InjectView(R.id.bottom_RL_container_two)
    RelativeLayout bottomRLContainerTwo;

    private AlertDialog rightPopupWindow = null;
    private boolean isScreenChoose = true;

    private String SHOT_CUT_PICTURE_PATH = "/storage/emulated/0/launcher.png";

    public static Bitmap cut;
    public static Bitmap bitmap;
    public String wallType;

    public String wallID;
    private String mLocalWallPath;
    private int mActivityType;
    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.preview_detail_layout);
        ButterKnife.inject(this);
        bottomRLContainerTwo.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
        initView();
        setPreviewTvTextColor(R.color.text_color_33cccc, R.color.white, isScreenChoose);
        setListener();
//        new TimeThread().start();
        StartTimer();
    }

    private void StartTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message=new Message();
                message.what=1;
                mHandler.sendMessage(message);
            }
        },30000);
    }

//    class TimeThread extends Thread {
//        @Override
//        public void run() {
//            do {
//                try {
//                    Thread.sleep(1000);
//                    Message msg = new Message();
//                    msg.what = 1;  //消息(一个整型值)
//                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            } while (true);
//        }
//    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    setTime();
                    break;
                default:
                    break;
            }
        }
    };

    private void initView() {
        cancelTV.getBackground().setAlpha(216);
        useTV.getBackground().setAlpha(216);
        screenPreviewTV.setSelected(true);
        //设置锁屏截图
        lockLL.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
        setTime();

        wallID = getIntent().getStringExtra("wallID");
        wallType = getIntent().getStringExtra("wallType");
        if (getIntent().getStringExtra("localWallPath") != null)
            mLocalWallPath = getIntent().getStringExtra("localWallPath").trim();
        mActivityType = getIntent().getIntExtra("activityType", 0);
        String path = null;
        if (mActivityType == 0) {
            path = FileUtils.getDir("wallpaper") + wallID +2+".zip";
        }
        if (bitmap == null && mActivityType == 0) {
            bitmap = BitmapUtils.getWallpaper(this, path);

            //avoid crash for wrong dimension  add by pengy
            int width = CommonUtils.getWallWidth();
            boolean isRightWallpaper = false;
            if(wallType.equals("2")){
                isRightWallpaper = 2*width== bitmap.getWidth()? true:false;
            }else{
                isRightWallpaper = width == bitmap.getWidth()?true:false;
            }
            /**delete failed wallpaper file  by pengy start*/
            if(bitmap==null || isRightWallpaper==false){
                ToastUtils.showToast(getResources().getString(R.string.failed_to_read));
                FileUtils.recursionDeleteFile(new File(path));
                DBUtils.deleteDownloadById(wallID+"2");
                DownloadTaskMgr.getInstance().clearTask(wallID+"2");
                this.finish();
            }
            /**delete failed wallpaper file  by pengy end*/
        }
        if (mLocalWallPath != null && mActivityType == 1) {
            bitmap = ImageLoader.getInstance().loadImageSync("file://" + mLocalWallPath);
        }
        if (bitmap == null)
            return;
//        JLog.i("hu","bitmap.getHeight()=="+bitmap.getHeight()+"---bitmap.getWidth()=="+bitmap.getWidth()+"--wallType=="+wallType);
        imagePreviewLayout.setBitmap(bitmap);
        //设置壁纸
        if (wallType.equals("2")) {
            imagePreviewLayout.setOutputSize(bitmap.getWidth() / 2, bitmap.getHeight());
        } else {
            imagePreviewLayout.setOutputSize(bitmap.getWidth(), bitmap.getHeight());
        }
        //设置桌面截图
        if (BitmapUtils.isExistFile(SHOT_CUT_PICTURE_PATH)) {
//            BitmapFactory.Options opt = new BitmapFactory.Options();
//            opt.inJustDecodeBounds = true;
            if (cut == null) {
                cut = BitmapFactory.decodeFile(SHOT_CUT_PICTURE_PATH);
            }
            imgPreview.setImageBitmap(cut);

        }
    }

    private void setTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
        Date date = new Date(System.currentTimeMillis());
        String days = formatter.format(date);

        SimpleDateFormat formatterTwo = new SimpleDateFormat("HH:mm");
        Date dateTwo = new Date(System.currentTimeMillis());
        String time = formatterTwo.format(dateTwo);

        String week = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        if ("1".equals(week)) {
            week = "星期天";
        } else if ("2".equals(week)) {
            week = "星期一";
        } else if ("3".equals(week)) {
            week = "星期二";
        } else if ("4".equals(week)) {
            week = "星期三";
        } else if ("5".equals(week)) {
            week = "星期四";
        } else if ("6".equals(week)) {
            week = "星期五";
        } else if ("7".equals(week)) {
            week = "星期六";
        }
        daysTV.setText(days);
        weekTV.setText(week);
        timeTV.setText(time);
    }


    private void setListener() {
        cancelTV.setOnClickListener(this);
        screenPreviewTV.setOnClickListener(this);
        launcherPreviewTV.setOnClickListener(this);
        isScrollTV.setOnClickListener(this);
        useTV.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_TV:
                this.finish();
                break;
            case R.id.use_TV:  //应用
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
            case R.id.screen_preview_TV:
                if (!isScreenChoose) {
                    screenPreviewTV.setSelected(true);
                    isScreenChoose = true;
                    launcherPreviewTV.setSelected(false);
                    setPreviewTvTextColor(R.color.text_color_33cccc, R.color.white, isScreenChoose);
                    isScrollTV.setVisibility(View.INVISIBLE);
                    imgPreview.setVisibility(View.GONE);
                    lockLL.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.launcher_preview_TV:
                if (isScreenChoose) {
                    launcherPreviewTV.setSelected(true);
                    isScreenChoose = false;
                    screenPreviewTV.setSelected(false);
                    setPreviewTvTextColor(R.color.white, R.color.text_color_33cccc, isScreenChoose);
                    if (wallType.equals("2")) {
                        isScrollTV.setVisibility(View.VISIBLE);
                    } else {
                        isScrollTV.setVisibility(View.INVISIBLE);
                    }

                    imgPreview.setVisibility(View.VISIBLE);
                    lockLL.setVisibility(View.GONE);
                }
                break;
            case R.id.isScroll_TV:
                if (isScrollTV.isSelected()) {
                    isScrollTV.setSelected(false);
                } else {
                    isScrollTV.setSelected(true);
                }
                break;
            case R.id.launcher_TV:   //桌面
                if (isScrollTV.isSelected()) {
                    Intent intent = new Intent();
                    intent.setAction(Constants.BRD_ACTION_APPLY_SCROLL_LAUNCHER);
                    if (mActivityType == 0) {
                        intent.putExtra("isScroll", true);
                    }
                    intent.putExtra("wallType", wallType);
                    sendBroadcast(intent);
                    BitmapUtils.applyType(PreviewActivity.this, 1, bitmap, null, mActivityType, true);
                } else {
                    //bug 25492
                    BitmapUtils.applyType(PreviewActivity.this, 1, imagePreviewLayout.crop(), null, mActivityType, false);
                }
                rightPopupWindow.dismiss();
                break;
            case R.id.screen_lock_RL:   //锁屏
                LockScreen(2);
                break;
            case R.id.all_TV:   //全部
                if (mActivityType == 0) {
                    LockScreen(3);
                } else if (mActivityType == 1) {
                    BitmapUtils.applyType(this, 3, bitmap, mLocalWallPath, mActivityType, false);
                }
                break;
        }
    }

    private void setPreviewTvTextColor(int pWhite, int pText_color_33cccc, boolean isScreenChoose) {
        try {
            //bug 27543
            if(mLocalWallPath==null || Constant.SPECIAL_THEME==null){
                return;
            }
            if (new File(mLocalWallPath).getParentFile().getCanonicalPath().equals(new File(Constant.SPECIAL_THEME).getParentFile().getCanonicalPath())) {
                if (!isScreenChoose) {
                    screenPreviewTV.setTextColor(pWhite);
                    launcherPreviewTV.setTextColor(pText_color_33cccc);
                } else {
                    screenPreviewTV.setTextColor(pText_color_33cccc);
                    launcherPreviewTV.setTextColor(pWhite);
                }
            }
        } catch (Exception pE) {
            pE.printStackTrace();
        }
    }

    private void LockScreen(int type) {
        Bitmap cacheBitmap = imagePreviewLayout.crop();
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        if (BitmapUtils.isExistFile(FileUtils.getDir("cache") + "/" + s + "_wall" + ".zip")) {
            s = random.nextInt(max) % (max - min + 1) + min;
        }
        File file = BitmapUtils.createFile(FileUtils.getDir("cache"), s + "_wall" + ".zip");
        BitmapUtils.saveBitmapToFile(cacheBitmap, file);
        if (type == 3 && isScrollTV.isSelected()) {
            Intent intent = new Intent();
            intent.setAction(Constants.BRD_ACTION_APPLY_SCROLL_LAUNCHER);
            if (mActivityType == 0) {
                intent.putExtra("isScroll", true);
            }
            intent.putExtra("wallType", wallType);
            sendBroadcast(intent);
            if (bitmap != null) {
                BitmapUtils.setScollBitmap(bitmap);
            }
            BitmapUtils.applyType(PreviewActivity.this, type, cacheBitmap, file.getAbsolutePath(), mActivityType, true);
        } else {
            BitmapUtils.applyType(PreviewActivity.this, type, cacheBitmap, file.getAbsolutePath(), mActivityType, false);
        }
        rightPopupWindow.dismiss();
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
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        imgPreview = null;
        imagePreviewLayout = null;
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        mTimer.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setToLauncher();
        if (cut != null) {
            cut.recycle();
            cut = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置桌面截图
        if (BitmapUtils.isExistFile(SHOT_CUT_PICTURE_PATH)) {
            if (cut == null) {
                cut = BitmapFactory.decodeFile(SHOT_CUT_PICTURE_PATH);
            }
            imgPreview.setImageBitmap(cut);
        }
    }

    private void setToLauncher() {
        sendBroadcast(new Intent(Constants.BRD_ACTION_APPLY_LAUNCHER));
    }

    public void setApplyType(int type) {
        if (type == 1 || type == 3) {
            DownloadInfo mDownloadInfo = DBUtils.findDownloadById(wallID+2);
            DBUtils.saveOrUpdateDownload(wallID, 2);
            mDownloadInfo.setCurrentState(7);
            UIUtils.updateLocalStates("wallpaperId",PreviewActivity.this);
            DownloadTaskMgr.getInstance().setDownlaadTaskState(wallID,2);
            DownloadTaskMgr.getInstance().notifyRefreshUI(DownloadState.STATE_DOWNLOAD_INSTALLED, wallID);
        }
    }
}
