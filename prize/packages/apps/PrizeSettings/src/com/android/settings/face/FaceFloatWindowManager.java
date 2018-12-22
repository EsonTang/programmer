package com.android.settings.face;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import pingan.ai.paverify.widget.PaDtcSurfaceView;
import pingan.ai.paverify.mananger.PaFaceDetectorManager;
import pingan.ai.paverify.mananger.impl.IPaFaceDetector;
import pingan.ai.paverify.entity.PAFaceDetectorFrame;
import pingan.ai.paverify.camera.impl.IOpenCameraInfo;

import com.android.settings.R;
import com.android.settings.face.utils.FaceXmlData;
import com.android.settings.face.utils.SaveBitmapUtil;
import com.android.settings.face.utils.SaveListUtil;
import com.android.settings.face.utils.SpUtil;
import com.android.settings.face.utils.ThreadUtil;
import com.mediatek.common.prizeoption.PrizeOption;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/10/23.
 */

public class FaceFloatWindowManager {

    private static WindowManager mWindowManager = null;
    private static ActivityManager mActivityManager = null;
    //private PowerManager.WakeLock mFullScreenWakelock;
    private WindowManager.LayoutParams mLayoutParams, msurfacelayoutParams;
    private ImageView faceIcon;
    private TextView face_lock_text, face_lock_text2;
    private View rootView;
    private LayoutInflater layoutInflater;

    private FrameLayout surfaceviewLayout;
    private PaDtcSurfaceView mSurfaceView;
    private PaFaceDetectorManager manager;

    private boolean isCreate = false;

    //private static AnimationDrawable animationDrawable;

    private FaceFloatWindowManager() {
    }

    private static class FaceFloatWindowManagerHolder {
        private static final FaceFloatWindowManager instance = new FaceFloatWindowManager();
    }

    public static FaceFloatWindowManager getInstance() {
        return FaceFloatWindowManagerHolder.instance;
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    public void createFloatWindow(Context context, boolean screenoff) {
       /* if (isCreate) {
            return;
        }*/

        mHandler.removeMessages(3);
        if (manager != null && screenoff) {
            Log.i("tzm", "manageronpause");
            manager.onPause();
            manager.onDestory();
            manager = null;
        }
        if (null == mLayoutParams) {
            mLayoutParams = new LayoutParams();
            mLayoutParams.type = LayoutParams.TYPE_TOP_MOST;
            mLayoutParams.packageName = "com.android.settings";
            mLayoutParams.flags = LayoutParams.FLAG_HARDWARE_ACCELERATED | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.gravity = Gravity.CENTER;
            mLayoutParams.width = LayoutParams.MATCH_PARENT;
            mLayoutParams.height = LayoutParams.MATCH_PARENT;
        }
        if (null == msurfacelayoutParams) {
            msurfacelayoutParams = new LayoutParams();
            msurfacelayoutParams.type = LayoutParams.TYPE_TOP_MOST;
            msurfacelayoutParams.packageName = "com.android.settings";
            msurfacelayoutParams.flags = LayoutParams.FLAG_HARDWARE_ACCELERATED | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            msurfacelayoutParams.format = PixelFormat.RGBA_8888;
            msurfacelayoutParams.gravity = Gravity.CENTER;
            msurfacelayoutParams.width = LayoutParams.MATCH_PARENT;
            msurfacelayoutParams.height = LayoutParams.MATCH_PARENT;
        }

        initFaceImageView(context, mLayoutParams);
        initSurfaceView(context, msurfacelayoutParams, 1, 1 /** 4 / 3*/);

        try {
            Log.i("tzm", "addview");
            if (surfaceviewLayout.getParent() == null) {
                if (!isShowFaceAnimal(context)) {
                    //surfaceviewLayout.setBackgroundColor(Color.parseColor("#00000000"));
                    surfaceviewLayout.setBackground(null);
                } else {
                    //surfaceviewLayout.setBackgroundColor(Color.parseColor("#000000"));
                    //surfaceviewLayout.setBackgroundResource(R.drawable.faceicon_bg);
                }
                getWindowManager(context).addView(surfaceviewLayout, msurfacelayoutParams);
            }
            if (rootView.getParent() == null && isShowFaceAnimal(context)) {
                surfaceviewLayout.setBackgroundResource(R.drawable.faceicon_bg);
                faceIcon.setImageResource(R.drawable.face_animation);
                face_lock_text.setText(context.getString(R.string.face_show_text1));
                face_lock_text2.setText(context.getString(R.string.face_show_text2));
                getWindowManager(context).addView(rootView, mLayoutParams);
            }
            //initDetector(context);
            isCreate = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (screenoff) {
            //mHandler.removeMessages(3);
            face_lock_text.setText(context.getString(R.string.face_show_text1));
            stopAnimal(context);
        }
    }

    public void removeFaceIcon(Context context) {
        Log.i("tzm", "removeFaceIcon");
        if (rootView != null) {
            try {
                surfaceviewLayout.setBackground(null);
                stopAnimal(context);
                getWindowManager(context).removeView(rootView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeFloatWindow(Context context) {
        /*if (!isCreate) {
            return;
        }*/
        Log.i("tzm", "removeFloatWindow");

        if (surfaceviewLayout != null) {
            try {
                getWindowManager(context).removeView(surfaceviewLayout);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (manager != null) {
            manager.onPause();
            manager.onDestory();
            manager = null;
        }

        isCreate = false;
    }

    public boolean isShowFaceAnimal(Context context) {
        //return (boolean) (SpUtil.getData(context, "face_show_ani", true));
        return !PrizeOption.PRIZE_FACE_ID_KOOBEE && FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH).face_show_ani;
    }

    public void startAnimal(Context context) {
        if (!isShowFaceAnimal(context)) {
            return;
        }
        //faceIcon.setImageResource(R.drawable.face_animation);
        if (faceIcon == null) {
            return;
        }
        if (faceIcon.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) faceIcon.getDrawable()).start();
        }
    }

    public void stopAnimal(Context context) {
        if (!isShowFaceAnimal(context)) {
            return;
        }
        if (faceIcon == null) {
            return;
        }
        if (faceIcon.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) faceIcon.getDrawable()).stop();
        }

    }

    private void initFaceImageView(final Context context, LayoutParams mLayoutParams) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(context);
            rootView = layoutInflater.inflate(R.layout.face_lock, null);
            faceIcon = (ImageView) (rootView.findViewById(R.id.face_show_animal));
            face_lock_text = (TextView) (rootView.findViewById(R.id.face_show_text));
            face_lock_text2 = (TextView) (rootView.findViewById(R.id.face_show_text2));

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeFaceIcon(context);
                }
            });
            rootView.setLayoutParams(mLayoutParams);
        }
        /*if (faceIcon == null) {
            faceIcon = new ImageView(context);
            //faceIcon.setImageResource(R.drawable.faceicon_001);

            faceIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //removeFloatWindow(context);
                    removeFaceIcon(context);
                }
            });

            faceIcon.setScaleType(ImageView.ScaleType.CENTER);
            faceIcon.setLayoutParams(mLayoutParams);
        }*/
    }

    private void initSurfaceView(Context context, LayoutParams mlayoutParams, int x, int y) {
        if (surfaceviewLayout == null) {
            surfaceviewLayout = new FrameLayout(context);
            surfaceviewLayout.setLayoutParams(mlayoutParams);
            if (mSurfaceView == null) {
                mSurfaceView = new PaDtcSurfaceView(context);
                Log.i("tzm", "initSurfaceView");
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(x, y);
            mSurfaceView.setLayoutParams(layoutParams);
            surfaceviewLayout.addView(mSurfaceView);
        }

    }

    public void initDetectorRunThread(final Context context) {
        ThreadUtil.getmCacheThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                initDetector(context);
            }
        });
    }

    public void initDetector(Context context) {
        if (faceIcon == null || !isCreate) {
            return;
        }
        manager = new PaFaceDetectorManager(context, mSurfaceView, 1);
        manager.setCameraMode(Camera.CameraInfo.CAMERA_FACING_FRONT);
        //manager.seeLog(true);

        try {
            File dir = new File("/data/system/users/faceid/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (manager != null) {
            manager.setPath("/data/system/users/faceid/"); //  /data/system/users/faceid
            manager.setDetectType(1);
        }

        /*List<String> list = SaveListUtil.getList(context);
        int size = list.size();
        String[] arr = (String[]) list.toArray(new String[size]);*/
        //String[] arr = new String[]{"face1", "face2", "face3", "face4", "face5"};
        String[] arr = new String[]{"face1"};
        if (arr != null && arr.length > 0) {
            try {
                manager.setFaceIdArray(arr);
                startDetector(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //startAnimal(context);
    }

    private Context mcontext;

    public void setMcontext(Context mcontext) {
        this.mcontext = mcontext;
    }

    public Context getMcontext() {
        return mcontext;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mHandler.removeMessages(3);
                    removeFaceIcon(mcontext);
                    removeFloatWindow(mcontext);
                    break;
                case 2:
                    mHandler.removeMessages(3);
                    /*if (!PrizeOption.PRIZE_FACE_ID_KOOBEE) {
                        if (manager != null) {
                            manager.startDetector();
                        }
                        mHandler.sendEmptyMessageDelayed(3, 3000);
                    }*/
                    Intent intent = new Intent("com.faceid");
                    intent.putExtra("faceid", 1);
                    intent.putExtra("facesize", 1);
                    mcontext.sendBroadcast(intent);
                    face_lock_text.setText(mcontext.getString(R.string.face_unmatch));
                    break;
                case 3:
                    if (manager != null) {
                        manager.stopDetector();
                    }
                    Intent mintent = new Intent("com.faceid");
                    mintent.putExtra("faceid", 0);
                    mintent.putExtra("facesize", 1);
                    mcontext.sendBroadcast(mintent);
                    face_lock_text.setText(mcontext.getString(R.string.face_camera_text));
                    //mHandler.sendEmptyMessageDelayed(3, 3000);
                    break;
                case 20:
                    mHandler.removeMessages(20);
                    createFloatWindow(mcontext, true);
                    /*prize-xuchunming-20180301-bugid:51578-start*/
		    //getActivityManager(mcontext).forceStopPackage("com.mediatek.camera");
                    /*prize-xuchunming-20180301-bugid:51578-end*/
                    break;
                case 21:
                    mHandler.removeMessages(21);
                    initDetector(mcontext);
                    startAnimal(mcontext);
                    mcontext.sendBroadcast(new Intent("faceid_scerrnon"));
                    break;
                default:
                    break;
            }
        }
    };

    boolean isSuccess;

    public void startDetector(final Context context) {
        if (manager == null) {
            return;
        }
        //mcontext = context;
        if (manager != null) {
            manager.startPreview();
        }
        if (manager != null) {
            manager.startDetector();
        }
        mHandler.sendEmptyMessageDelayed(3, 3000);
        mSurfaceView.setCameraInfoCallback(new IOpenCameraInfo() {
            @Override
            public void setOpenCameraInfo(boolean isOpen) {
                if (!isOpen) {
                    //manager.stopDetector();
                    //removeFloatWindow(context);
                } else {
                }
            }
        });
        if (manager != null)
            manager.setOnFaceDetectorListener(new IPaFaceDetector() {
                @Override
                public void detectSuccess(PAFaceDetectorFrame paFaceDetectorFrame) {
                    //paFaceDetectorFrame.getLivnessHeadBitmap();
                    Log.i("tzm", "__detectSuccess");
                    /*if (!isSuccess) {
                        SaveBitmapUtil.saveBitmap2Sdcard(System.currentTimeMillis() + "", paFaceDetectorFrame.getLivnessBitmap());
                    }*/
                }

                @Override
                public void compareSuccess(boolean isFace, float score, String compareName) {
                    Log.i("tzm", "__compareSuccess");
                    isSuccess = isFace;
                    /*if (isFace) {
                    *//*Intent intent = new Intent("prize.set.keyguard.state");
                    intent.putExtra("hide", true);
                    intent.putExtra("secure", true);
                    context.sendBroadcast(intent);*//*
                   *//* KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
                    keyguardLock.disableKeyguard(); // 解锁
                    keyguardLock.reenableKeyguard();*//*
                        try {
                            WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
                            mHandler.removeMessages(3);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (manager != null) {
                            manager.stopDetector();
                        }
                        mHandler.sendEmptyMessage(2);
                    }*/
                }

                @Override
                public void liveSuccess(boolean isLive, float liveScore) {
                    Log.i("tzm", "__liveSuccess=" + isLive);
                    if (isLive && isSuccess) {
                        try {
                            WindowManagerGlobal.getWindowManagerService().dismissKeyguard();
                            mHandler.removeMessages(3);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (manager != null) {
                            manager.stopDetector();
                        }
                        mHandler.sendEmptyMessage(2);
                    }
                }

                @Override
                public void registerSuccess(boolean isReg) {

                }
            });
    }

}
