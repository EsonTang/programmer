package com.android.lpserver;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import com.android.lpserver.job.AccessbilityJob;
import com.android.lpserver.job.WechatAccessbilityJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class QiangHongBaoService extends AccessibilityService {
    private static final String TAG = "QiangHongBaoService";

    private static final Class[] ACCESSBILITY_JOBS= {WechatAccessbilityJob.class};

    private static QiangHongBaoService service;

    private List<AccessbilityJob> mAccessbilityJobs;
    private HashMap<String, AccessbilityJob> mPkgAccessbilityJobMap;
    private SharedPreferences.Editor edit;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mAccessbilityJobs = new ArrayList<>();
        mPkgAccessbilityJobMap = new HashMap<>();

        for(Class clazz : ACCESSBILITY_JOBS) {
            try {
                Object object = clazz.newInstance();
                if(object instanceof AccessbilityJob) {
                    AccessbilityJob job = (AccessbilityJob) object;
                    job.onCreateJob(this);
                    mAccessbilityJobs.add(job);
                    mPkgAccessbilityJobMap.put(job.getTargetPackageName(), job);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SharedPreferences packNameSp = getSharedPreferences("PACK_NAME",MODE_PRIVATE);
        edit = packNameSp.edit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(mPkgAccessbilityJobMap != null) {
            mPkgAccessbilityJobMap.clear();
        }
        if(mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            for (AccessbilityJob job : mAccessbilityJobs) {
                job.onStopJob();
            }
            mAccessbilityJobs.clear();
        }

        service = null;
        mAccessbilityJobs = null;
        mPkgAccessbilityJobMap = null;
        //send a broadcast for disconnect service
        Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        sendBroadcast(intent);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        service = this;
        //send a broadcast for connect service
        Intent intent = new Intent(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        sendBroadcast(intent);
    }

    /**
     *  When we listen to the name of the package or the interface changes,
     *  it will be captured by the onAccessibilityEvent method
     * @param event
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "event--->" + event );
        }
        String pkn = String.valueOf(event.getPackageName());
        edit.putString("pack_name",pkn);
        edit.commit();
        if(mAccessbilityJobs != null && !mAccessbilityJobs.isEmpty()) {
            for (AccessbilityJob job : mAccessbilityJobs) {
                if(pkn.equals(job.getTargetPackageName()) && job.isEnable()) {
                    job.onReceiveJob(event);
                }
            }
        }
    }

    public Config getConfig() {
        return Config.getConfig(this);
    }

    /** Receive notification bar event*/
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @TargetApi(Build.VERSION_CODES.N)
    public static void handeNotificationPosted(IStatusBarNotification notificationService) {
        if(notificationService == null) {
            return;
        }
        if(service == null || service.mPkgAccessbilityJobMap == null) {
            return;
        }
        String pack = notificationService.getPackageName();
        Log.d(TAG,"QiangHongBaoService...."+pack);
        AccessbilityJob job = service.mPkgAccessbilityJobMap.get(pack);
        if(job == null) {
            return;
        }
        job.onNotificationPosted(notificationService);
    }

    /**
     * To determine whether the current service is running
     * */
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TargetApi(Build.VERSION_CODES.N)
    public static boolean isRunning() {
        if(service == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) service.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = service.getServiceInfo();
        if(info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if(i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if(!isConnect) {
            return false;
        }
        return true;
    }

    /** Quickly read the notification bar service is started*/
    public static boolean isNotificationServiceRunning() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        }
        //Part of the phone does not have NotificationService services
        try {
            return QHBNotificationService.isRunning();
        } catch (Throwable t) {}
        return false;
    }


}
