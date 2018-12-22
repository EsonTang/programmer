package com.android.camera;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2016/12/9.
 * For 
 */

public class CamApplicaion extends Application {

    private static final String TAG = "CamApplicaion";
    private static CamApplicaion instance;

    public int getSwitchToCamera() {
        if (cameraActivityRef != null && cameraActivityRef.get() != null){
            Log.d(TAG,"[getSwitchToCamera]");
            return cameraActivityRef.get().getPendingSwitchCameraId();
        }
        return -1;
    }

    public boolean isCameraSwitchingDone() {
        if (cameraActivityRef != null && cameraActivityRef.get() != null){
            Log.d(TAG,"[getCurrentCameraId]");
            return cameraActivityRef.get().isCameraSwitchingDone();
        }
        return true;
    }

    public static CamApplicaion getInstance() {
        return instance;
    }

    private WeakReference<CameraActivity> cameraActivityRef = null;



    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        Log.d(TAG,"[onCreate]");
    }

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
        super.onTerminate();
    }

    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.d(TAG, "CameraActivityCreated.");
            if (activity instanceof CameraActivity){
                cameraActivityRef = new WeakReference<CameraActivity>((CameraActivity) activity);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity.getClass() == CameraActivity.class){
                Log.d(TAG, "CameraActivityDestroyed.");
                cameraActivityRef = null;
            }
        }
    };
}
