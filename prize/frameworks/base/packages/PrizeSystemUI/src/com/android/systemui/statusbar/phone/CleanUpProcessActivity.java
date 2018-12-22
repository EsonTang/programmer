/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：通知栏实现一键清理的Activity
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-4-14
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
package com.android.systemui.statusbar.phone;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import com.android.systemui.R;
import android.content.Context;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import com.android.systemui.recents.RecentsActivity;
import android.os.Handler;

// Nav bar color customized feature. prize-linkh-20170906 @{
import com.mediatek.common.prizeoption.PrizeOption;
// @}

/**
* 类描述：通知栏实现一键清理的Activity
* @author liufan
* @version V1.0
*/
public class CleanUpProcessActivity extends Activity {
    private static final String TAG = "liufan";
    private ImageView rotateImage;
    private Animation animation;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarTrans();
        setContentView(R.layout.activity_cleanup_process);       
       
        rotateImage=(ImageView)findViewById(R.id.imageView_cleanup);
        animation=AnimationUtils.loadAnimation(CleanUpProcessActivity.this, R.anim.cleanup_process_anim);//加载动画             
        animation.setAnimationListener(new AnimationListener(){

            @Override
            public void onAnimationStart(Animation animation) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        PhoneStatusBar.isCleanActivityFinish = false;
                        /*Looper.prepare();
                        killAll(getApplicationContext());//动画开始时杀死进程
                        Looper.loop();*/
                        mHandler.removeCallbacks(r);
                        mHandler.postDelayed(r, 10000);
                        while(!PhoneStatusBar.isCleanActivityFinish){
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHandler.removeCallbacks(r);
                finish();//动画停止后结束当前activity
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }   
        });    
        rotateImage.setAnimation(animation);
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            PhoneStatusBar.isCleanActivityFinish = true;
        }
    };

    /**
     * 设置为沉浸式
     */
    private void setStatusBarTrans() {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            // Nav bar color customized feature. prize-linkh-20170906 @{
            if (PrizeOption.PRIZE_NAVBAR_COLOR_CUST) {
                window.setDisableCustNavBarColor(true);
            } // @}
        }
    }
    
    /**
     * 杀死后台进程
     */
    public void killAll(Context context){

        final long used_before = RecentsActivity.getAvailMemory(CleanUpProcessActivity.this);
        final long total = RecentsActivity.totalMemoryB == 0 ? RecentsActivity.getTotalMemory() : RecentsActivity.totalMemoryB;
        //获取一个ActivityManager 对象
        ActivityManager activityManager = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        //获取系统中所有正在运行的进程
        List<RunningAppProcessInfo> appProcessInfos = activityManager
                .getRunningAppProcesses();
        int count=0;//被杀进程计数
        String nameList="";//记录被杀死进程的包名
        long beforeMem = getAvailMemory(CleanUpProcessActivity.this);//清理前的可用内存
        Log.i(TAG, "清理前可用内存为 : " + beforeMem);
        
        for (RunningAppProcessInfo appProcessInfo:appProcessInfos) {
            nameList="";          
            //if( appProcessInfo.processName.contains("com.android.system")
            //        ||appProcessInfo.pid==android.os.Process.myPid())//跳过系统 及当前进程
            //    continue;
            Log.i(TAG, "appProcessInfo.processName--------------------->"+appProcessInfo.processName);
            Log.i(TAG, "appProcessInfo.pid--------------------->"+appProcessInfo.pid);
            if(appProcessInfo.processName.contains("com.prize.weather")){//屏蔽天气APP
                continue;
            }
            if(appProcessInfo.processName.contains("com.android.dialer")){//屏蔽电话
                continue;
            }
            if(appProcessInfo.processName.contains("android.process.acore")){//屏蔽电话 07-27-add
                continue;
            }
            if(appProcessInfo.processName.contains("com.android.floatwindow")){
                continue;
            }
			if(appProcessInfo.processName.contains("com.android.prizefloatwindow")){
                continue;
            }
            if(appProcessInfo.processName.contains("com.android.gallery3d")){
                continue;
            }

            if(appProcessInfo.processName.contains("com.prize.luckymonkeyhelper")){
                continue;
            }
            if (appProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                String[] pkNameList=appProcessInfo.pkgList;//进程下的所有包名
                for(int i=0;i<pkNameList.length;i++){
                    String pkName=pkNameList[i];
                    activityManager.killBackgroundProcesses(pkName);//杀死该进程
                    count++;//杀死进程的计数+1
                    nameList+="  "+pkName;               
                }
            }
            Log.i(TAG, nameList+"---------------------");
        }              
        
        //long afterMem = getAvailMemory(CleanUpProcessActivity.this);//清理后的内存占用
        //Toast.makeText(CleanUpProcessActivity.this, "杀死 " + (count+1) + " 个进程, 释放"
        //        + formatFileSize(afterMem - beforeMem) + "内存", Toast.LENGTH_LONG).show();
        final long used_after = RecentsActivity.getAvailMemory(CleanUpProcessActivity.this);
        long rel = used_before - used_after;
        rel = RecentsActivity.numericConversions(rel, RecentsActivity.MEMORY_UNIT_MB) < 1 ? 0 : rel;
        RecentsActivity.showCleanResultByToast(CleanUpProcessActivity.this, 
            RecentsActivity.numericConversions(rel, RecentsActivity.MEMORY_UNIT_MB) + RecentsActivity.MEMORY_UNIT_MB,
            RecentsActivity.numericConversions(total - used_after, RecentsActivity.MEMORY_UNIT) + RecentsActivity.MEMORY_UNIT);
        //Log.i(TAG, "清理后可用内存为 : " + afterMem);
        //Log.i(TAG, "清理进程数量为 : " + count+1);
        
    }

    
  /**
   * 获取可用内存大小
   */
    private long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi); 
        return mi.availMem;
    }
    
    /**
     * 字符串转换 long-string KB/MB
     */
    private String formatFileSize(long number){
        return Formatter.formatFileSize(CleanUpProcessActivity.this, number);
    }
   
}
