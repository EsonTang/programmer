package com.prize.appcenter.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.SingGameResData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.adapter.LocalListGridViewAdapter;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

/**
 * 桌面文件加的界面
 */
public class FolderActivity extends FragmentActivity implements ServiceConnection {
//    private LinearLayout background_Llyt;
    private GridView contentView;
    protected View waitView = null;
    private View reloadView = null;
    private LocalListGridViewAdapter localAdapter;
    private Bitmap destBitmap;
    private Bitmap srcBitmap;
    private int textColor;
    private boolean isWhiteBackGound = false;
    //    private int bgColor;
//    private BlurHandler mBlurHandler;
    private ServiceToken mToken;
    private TextView bar_title;
    private ImageButton action_refresh, action_go_downQueen;
    private long time = 0L;
    private Callback.Cancelable cancelable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (!BaseApplication.isThird) {
            WindowMangerUtils.initStateBar(getWindow(), this);
        }
        JLog.i("MainActivity", "FolderActivity-onCreate");
        setContentView(R.layout.activity_folder);
//        background_Llyt = (LinearLayout) findViewById(R.id.background_Llyt);
        contentView = (GridView) findViewById(R.id.gridview_local_app_id);
        bar_title = (TextView) findViewById(R.id.bar_title);
        action_refresh = (ImageButton) findViewById(R.id.action_refresh);
        action_go_downQueen = (ImageButton) findViewById(R.id.action_go_downQueen);

        waitView = findViewById(R.id.loading_Llyt_id);
        reloadView = findViewById(R.id.reload_Llyt);
        reloadView.setVisibility(View.GONE);
        reloadView.setBackgroundColor(Color.TRANSPARENT);
        waitView.setBackgroundColor(Color.TRANSPARENT);
        mToken = AIDLUtils.bindToService(this, this);
//        mBlurHandler = new BlurHandler(this);
        setWallpaperBackground();
        localAdapter = new LocalListGridViewAdapter(this);
        contentView.setAdapter(localAdapter);
        requestrecommandData();
        setOnclickListener();
    }

    private void setOnclickListener() {
        action_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastDoubleClick()) {
                    return;
                }
                if (ClientInfo.getAPNType(getApplicationContext()) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    return;
                }
                showWaiting();
                requestrecommandData();
            }
        });
        action_go_downQueen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CommonUtils.isFastDoubleClick()) {
                    return;
                }
                Intent intent = new Intent(FolderActivity.this,
                        AppDownLoadQueenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.FROM, "folder");
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 返回推荐列表数据
     */
    private void requestrecommandData() {
        RequestParams entity = new RequestParams(Constants.GIS_URL + "/recommand/desktop");
        cancelable = XExtends.http().post(entity, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {// AppDetailRecommandData
                try {
                    JSONObject o = new JSONObject(result);
                    if (o.getInt("code") == 0) {
                        SingGameResData.SingleGamesBean data = new Gson()
                                .fromJson(o.getString("data"), SingGameResData.SingleGamesBean.class);
                        processRecommandData(data);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                if (localAdapter != null && localAdapter.getCount() <= 0) {
                    loadingFailed(new ReloadFunction() {
                        @Override
                        public void reload() {
                            requestrecommandData();
                        }
                    });
                }
            }

        });

    }

    /**
     * @param data SingleGamesBean
     */
    protected void processRecommandData(SingGameResData.SingleGamesBean data) {
        if (data != null) {
            if (!TextUtils.isEmpty(data.title)) {
                bar_title.setText(data.title);

            }
            localAdapter.setData(CommonUtils.filterNeedSizeInstalled(data.apps, 15));
        }
        hideWaiting();
        if (reloadView != null) {
            reloadView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置壁纸为背景
     */
    private void setWallpaperBackground() {
        // 获取壁纸管理器
//        WallpaperManager wallpaperManager = WallpaperManager
//                .getInstance(this.getApplicationContext());
//        // 获取当前壁纸
//        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
//        // 将Drawable,转成Bitmap
//        background_Llyt.setBackground(wallpaperDrawable);
//        srcBitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
//        new BlurThread(this).start();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        localAdapter.setDownlaodRefreshHandle();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
//
//    private static class BlurThread extends Thread {
//        private final WeakReference<FolderActivity> weakReference;
//
//        BlurThread(FolderActivity scrub) {
//            weakReference = new WeakReference<FolderActivity>(scrub);
//        }
//
//        @Override
//        public void run() {
//            super.run();
//            FolderActivity scrub = weakReference.get();
//            if (scrub != null) {
//                scrub.destBitmap = BlurPic.blurScale(scrub.srcBitmap, 10);
//
//
//                //add by zhouerlong prizeTheme add
//                int left = scrub.srcBitmap.getWidth() / 12;
//                int top = scrub.srcBitmap.getHeight() / 12;
//                //add by zhouerlong prizeTheme add
//                int right = scrub.srcBitmap.getWidth() - left;
//                int bottom = scrub.srcBitmap.getHeight() - top;
//                int lenth = (right - left) * (bottom - top);
//                int[][] srcBuffer = new int[scrub.srcBitmap.getWidth()][scrub.srcBitmap.getHeight()];
//                long rc = 0;
//                long gc = 0;
//                long bc = 0;
//                int m = 5;
//                for (int x = left; x < right; x += m) {
//                    for (int y = top; y < bottom; y += m) {
//                        int color = scrub.srcBitmap.getPixel(x, y);
//                        srcBuffer[x][y] = color;
//                        int r = Color.red(color);
//                        rc += r;
//                        int g = Color.green(color);
//                        gc += g;
//                        int b = Color.blue(color);
//                        bc += b;
//                    }
//                }
//                int l = lenth / m / m;
//                float rP = rc / (float) l;
//                float gP = gc / (float) l;
//                float bP = bc / (float) l;
//                Color c = new Color();
//                float f = (rP + gP + bP) / 3;
////                int s = (int) ((rP + gP + bP) / 3f);
//                int m1;
//
//                if (f >= 190) {//壁纸是白色
//                    if (f < 220) {
//                        f = 220;
//                    }
//                    scrub.isWhiteBackGound = true;
//                    m1 = c.rgb(255 - (int) f, 255 - (int) f, 255 - (int) f);
//                } else {
//                    m1 = c.rgb(255, 255, 255);
//                }
//                if (m1 > 255) {
//                    m1 = 255;
//                }
//                scrub.textColor = m1;
////                scrub.bgColor = s;
//                scrub.mBlurHandler.sendEmptyMessage(0);
//
//
//            }
//
//        }
//    }

//    private static class BlurHandler extends Handler {
//        private final WeakReference<FolderActivity> weakReference;
//
//        BlurHandler(FolderActivity scrub) {
//            weakReference = new WeakReference<FolderActivity>(scrub);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            FolderActivity scrub = weakReference.get();
//            if (scrub != null) {
//                if (msg.what == 0) {
//                    scrub.background_Llyt.setBackground(new BitmapDrawable(scrub
//                            .getResources(), scrub.destBitmap));
//                    scrub.localAdapter.setTextColor(scrub.textColor);
//                    scrub.bar_title.setTextColor(scrub.textColor);
//                    scrub.action_refresh.getBackground().mutate().setColorFilter(scrub.textColor, PorterDuff.Mode.SRC_ATOP);
//                    scrub.action_go_downQueen.getBackground().mutate().setColorFilter(scrub.textColor, PorterDuff.Mode.SRC_ATOP);
//                    if (scrub.isWhiteBackGound) {
//                        WindowMangerUtils.changeStatus(scrub.getWindow());
//                    }
//                }
//            }
//
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (localAdapter != null) {
            localAdapter.setDownlaodRefreshHandle();
            localAdapter.setIsActivity(true);
            localAdapter.notifyDataSetChanged();
        }
        if (time == 0) {
            time = System.currentTimeMillis();
        } else {
            if ((System.currentTimeMillis() - time) > Constants.REFRESH_TIME) {//间隔大于5分钟则刷新
                if (ClientInfo.networkType == ClientInfo.NONET) return;
                requestrecommandData();
                time = System.currentTimeMillis();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (localAdapter != null) {
            localAdapter.setIsActivity(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancelable != null) {
            cancelable.cancel();
        }
//        if (mBlurHandler != null) {
//            mBlurHandler.removeCallbacksAndMessages(null);
//        }
        if (localAdapter != null) {
            localAdapter.removeDownLoadHandler();
        }
        if (mToken != null) {
            AIDLUtils.unbindFromService(mToken);
        }
    }


    /**
     * 显示等待框
     */
    private void showWaiting() {
        if (waitView == null)
            return;
        action_refresh.setEnabled(false);
//        ProgressBar gifWaitingView = (ProgressBar) waitView
//                .findViewById(R.id.gif_waiting);
        waitView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

    /**
     * 隐藏等待框
     */
    private void hideWaiting() {
        if (waitView == null)
            return;
        action_refresh.setEnabled(true);
        waitView.setVisibility(View.GONE);
//        ProgressBar gifWaitingView = (ProgressBar) waitView
//                .findViewById(R.id.gif_waiting);
        contentView.setVisibility(View.VISIBLE);
    }

    /**
     * 重新加载数据
     *
     * @author prize
     */
    public interface ReloadFunction {
        void reload();
    }

    /**
     * 加载失败
     */
    public void loadingFailed(final ReloadFunction reload) {
        if (null == reload || null == reloadView) {
            return;
        }
        JLog.i("FolderActivity", "loadingFailed");
        hideWaiting();
        LinearLayout reloadLinearLayout = (LinearLayout) reloadView
                .findViewById(R.id.reload_Llyt);
        reloadLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
                showWaiting();
                reload.reload();
            }
        });
        reloadView.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    }

    @Override
    public void finish() {
        super.finish();
    }
}
