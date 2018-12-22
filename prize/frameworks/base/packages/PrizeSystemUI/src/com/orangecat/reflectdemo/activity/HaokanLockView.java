package com.orangecat.reflectdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.app.PendingIntent;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
//import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import android.graphics.Rect;

import com.android.systemui.R;

import android.widget.TextView;
import android.widget.ImageView;
import android.view.MotionEvent;

import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.statusbar.phone.NotificationPanelView;

/**
 * Created by zw
 */
public class HaokanLockView extends RelativeLayout {
    // public static final String REMOTE_PACKAGE = "com.levect.lc.wingtech";
    public final String REMOTE_PACKAGE = "com.levect.lc.koobee";
    public final String REMOTE_CLASSNAME = "com.haokan.screen.lockscreen.detailpageview.DetailPage_MainView";
    private Context mContext;
    private LockPatternUtils mLockPatternUtils;
    private boolean isInitPanelView = false;

    public HaokanLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        NotificationPanelView.debugMagazine("MagazineLockView this.mContext 2");
    }

    public HaokanLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HaokanLockView(Context context) {
        super(context);
        this.mContext = context;
        NotificationPanelView.debugMagazine("MagazineLockView this.mContext 1");
    }

    public String getPackage(){
        return REMOTE_PACKAGE;
    }

    public String getMagazineViewClass(){
        return REMOTE_CLASSNAME;
    }

    private View mRemoteView;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        NotificationPanelView.debugMagazine("MagazineLockView onFinishInflate");
    }

    public void init(Context context) {
        mContext = context;
        initView();
        // registerScreenCallback();
        mLockPatternUtils = new LockPatternUtils(mContext);
    }

    public boolean isSecure() {
        if (mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mNotificationPanelView!=null&&mNotificationPanelView.IS_ShowNotification_WhenShowHaoKan&&mNotificationPanelView.getKeyguardBottomAreaView()!=null){
        View cameraView=mNotificationPanelView.getKeyguardBottomAreaView().getRightView();
            if(cameraView!=null){
                Rect rect=new Rect();
                cameraView.getHitRect(rect);
                final  int x=(int)ev.getX();
                final  int y=(int)ev.getY();
                if(rect.contains(x,y)){
                    NotificationPanelView.debugMagazine("MagazineLockView onInterceptTouchEvent return ture");
                    return  true; 
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public View getRemoteView() {
        //反射获取view
        try {
            long start = SystemClock.currentThreadTimeMillis();
            Context remoteContext = mContext.createPackageContext(REMOTE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            NotificationPanelView.debugMagazine("MagazineLockView remoteContext" + remoteContext);
            ClassLoader classLoader = remoteContext.getClassLoader();

            //替换累加器的parent
            // Field parent = ClassLoader.class.getDeclaredField("parent");
            // parent.setAccessible(true);
            // Object o = parent.get(classLoader);
            // Log.i("xsy", "ClassLoader parent = " + o);
            // parent.set(classLoader, ISystemUiView.class.getClassLoader());

            Class<?> clazz = classLoader.loadClass(REMOTE_CLASSNAME);
            Constructor constructor = clazz.getConstructor(Context.class, Context.class);
            Context applicationContext = remoteContext.getApplicationContext();
            NotificationPanelView.debugMagazine("MagazineLockView getRemoteView before");
            View remoteView = (View) constructor.newInstance(remoteContext, mContext.getApplicationContext());
            NotificationPanelView.debugMagazine("MagazineLockView getRemoteView after");
            return remoteView;
        } catch (Exception e) {
            NotificationPanelView.debugMagazine("getRemoteView Exception");
            e.printStackTrace();
        }
        return null;
    }

    public String getCurrentImageUri() {
        if (mRemoteView != null) {
            try {
                Method method = mRemoteView.getClass().getMethod("getCurrentImageUri");
                return (String) method.invoke(mRemoteView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    public void  onKeyCodeBack(){
        if (mRemoteView != null) {
            NotificationPanelView.debugMagazine("MagazineLockView mRemoteView.onKeyCodeBack");
            try {
                Method method = mRemoteView.getClass().getMethod("onKeyCodeBack");
                method.invoke(mRemoteView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshMagazineDescriptionColor(int color){
        if (mRemoteView != null) {
            NotificationPanelView.debugMagazine("MagazineLockView mRemoteView.refreshMagazineDescriptionColor");
            try {
                Method method = mRemoteView.getClass().getDeclaredMethod("refreshMagazineDescriptionColor",int.class);
                method.invoke(mRemoteView, color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startLockScreenWebView() {
        if (mRemoteView != null) {
            NotificationPanelView.debugMagazine("MagazineLockView mRemoteView.startLockScreenWebView");
            try {
                Method method = mRemoteView.getClass().getMethod("startLockScreenWebView");
                method.invoke(mRemoteView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void screenOn() {
        if (mRemoteView != null) {
            NotificationPanelView.debugMagazine("MagazineLockView mRemoteView.onScreenOn");
            try {
                Method method = mRemoteView.getClass().getMethod("onScreenOn");
                method.invoke(mRemoteView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void screenOff() {
        if (mRemoteView != null) {
            NotificationPanelView.debugMagazine("MagazineLockView mRemoteView.onScreenOff");
            try {
                Method method = mRemoteView.getClass().getMethod("onScreenOff");
                method.invoke(mRemoteView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setNotificationPanelView(View notificationPanelView) {
        NotificationPanelView.debugMagazine("MagazineLockView setNotificationPanelView");
        this.mNotificationPanelView = (NotificationPanelView) notificationPanelView;
        if (mRemoteView == null) {
            return;
        }
        try {
            Method method = mRemoteView.getClass().getMethod("setSystemUiView", View.class);
            method.invoke(mRemoteView, this);

            setNotificationUpperVisible(true);
            setNotificationUpperAlpha(1.0f);
            startActivityBySystemUI(null);
            setTitleAndUrl(null, null);
            dismissKeyguard(null);
            notifyInverseNumToSystemui(0);
            isShouldIgnoreInverse();
            clickDetails(null);
            isInitPanelView = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNotificationUpperVisible(boolean visible) {
        NotificationPanelView.debugMagazine("MagazineLockView setNotificationUpperVisible " + visible);
        if (mNotificationPanelView == null) {
            return;
        }
        if(!isInitPanelView){
            NotificationPanelView.debugMagazine("setNotificationUpperVisible: isInitPanelView not turn true");
            return ;
        }

        mNotificationPanelView.setNotificationUpperVisible(visible);

    }

    public void notifyInverseNumToSystemui(int inverse) {
        NotificationPanelView.debugMagazine("MagazineLockView notifyInverseNumToSystemui");
        if (mNotificationPanelView == null) {
            return;
        }
        if(!isInitPanelView){
            NotificationPanelView.debugMagazine("MagazineLockView not setNotificationPanelView");
            return ;
        }
        mNotificationPanelView.notifyInverseNumToSystemui(inverse);
    }

    public boolean isShouldIgnoreInverse() {
        NotificationPanelView.debugMagazine("MagazineLockView isShouldIgnoreInverse");
        if (mNotificationPanelView == null) {
            return false;
        }
        if(!isInitPanelView){
            NotificationPanelView.debugMagazine("MagazineLockView not isShouldIgnoreInverse");
            return false;
        }
        return mNotificationPanelView.isShouldIgnoreInverse();
    }

    public void clickDetails(Runnable runnable) {
        NotificationPanelView.debugMagazine("MagazineLockView clickDetails");
        if (mNotificationPanelView == null) {
            return ;
        }
        if(!isInitPanelView || runnable == null){
            NotificationPanelView.debugMagazine("MagazineLockView not clickDetails");
            return ;
        }
        mNotificationPanelView.clickDetails(runnable);
    }

    public void setNotificationUpperAlpha(float alpha) {
        NotificationPanelView.debugMagazine("MagazineLockView setNotificationUpperAlpha");
        if (mNotificationPanelView == null) {
            return;
        }
        if(!isInitPanelView){
            NotificationPanelView.debugMagazine("setNotificationUpperAlpha: isInitPanelView not turn true");
            return ;
        }
        mNotificationPanelView.setNotificationUpperAlpha(alpha);
    }

    public void startActivityBySystemUI(Intent intent) {
        NotificationPanelView.debugMagazine("MagazineLockView startActivityBySystemUI");
        if (mNotificationPanelView == null || intent == null) {
            return;
        }
        mNotificationPanelView.startActivityBySystemUI(intent);
    }

    public void setTitleAndUrl(String title, String urlName) {
        NotificationPanelView.debugMagazine("MagazineLockView startActivityBySystemUI==title," + title + ",=" + urlName);
        if (mNotificationPanelView == null) {
            return;
        }
        mNotificationPanelView.setTitleAndUrlName(title, urlName);
    }

    public void dismissKeyguard(String extra) {
        if (mNotificationPanelView == null || extra == null) {
            return;
        }
        mNotificationPanelView.dismissKeyguard();
    }


    private NotificationPanelView mNotificationPanelView;

    private RelativeLayout mRootView;

    private void initView() {
        NotificationPanelView.debugMagazine("MagazineLockView initView");
        mRemoteView = getRemoteView();
        if (mRemoteView == null) {
            
        } else {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            addView(mRemoteView, lp);
            NotificationPanelView.debugMagazine("MagazineLockView add mRemoteView");
        }

    }

    public void registerScreenCallback() {
        NotificationPanelView.debugMagazine("MagazineLockView registerScreenCallback 1");
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUpdateMonitorCallback);
        NotificationPanelView.debugMagazine("MagazineLockView registerScreenCallback 2");
    }

    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        @Override
        public void onUserSwitchComplete(int userId) {
        }

        @Override
        public void onStartedWakingUp() {
        }

        @Override
        public void onFinishedGoingToSleep(int why) {
        }

        @Override
        public void onScreenTurnedOn() {
            NotificationPanelView.debugMagazine("MagazineLockView onScreenTurnedOn");
        }

        @Override
        public void onScreenTurnedOff() {
            NotificationPanelView.debugMagazine("MagazineLockView onScreenTurnedOff");
        }

        @Override
        public void onKeyguardVisibilityChanged(boolean showing) {
        }

        @Override
        public void onFingerprintRunningStateChanged(boolean running) {
        }

        @Override
        public void onStrongAuthStateChanged(int userId) {
        }
    };
}
