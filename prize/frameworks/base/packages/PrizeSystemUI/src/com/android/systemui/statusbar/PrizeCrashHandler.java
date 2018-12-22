package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/15.
 */

public class PrizeCrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "haokantest";

    private static final boolean DEBUG = true;

    private static final String PATH = Environment.getExternalStorageDirectory().getPath()+"/exceptionCatch/log/";

    private static final String FILE_NAME = "catch";

    private static final String FLIE_NAME_SUFFIX = ".log";

    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;

    private Context mContext;
    private CrashCallBack mCrashCallBack;

    public PrizeCrashHandler() {

    }

    public interface CrashCallBack{
        public void onCrashOccured(Throwable ex);
    }

    public void setCrashCallBack(CrashCallBack callback){
        this.mCrashCallBack = callback;
    }
    /**
     * 静态内部类单例
     * @return
     */
    public static PrizeCrashHandler getInstance(){
        Log.d(TAG, "get crash instance.");
        return InstanceHolder.sInstance;
    }

    private static class InstanceHolder{
        private static PrizeCrashHandler sInstance = new PrizeCrashHandler();
    }

    public void init(Context context){
        Log.d(TAG, "init UncaughtExceptionnHandler");
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context.getApplicationContext();
    }

    /**
     * 程序崩溃会调用该方法
     */
    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        /*try{
            Log.d(TAG, "uncaughtException ... exception = " + ex);
            dumpExceptionToSDCard(ex);
            // uploadExceptionToServer(ex);  //需要把异常上传到服务器，编写逻辑 解除该行注释
            if(mCrashCallBack != null){
                mCrashCallBack.onCrashOccured();
            }
        }catch(IOException e){
            e.printStackTrace();
        }*/

        if (!handleException(ex) && mDefaultCrashHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultCrashHandler.uncaughtException(t, ex);
        } else {
            Log.d(TAG, "uncaughtException ... exception = " + ex);
            //dumpExceptionToSDCard(ex);
            //退出程序
            /*android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);*/
        }
    }

    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }

        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                if(mCrashCallBack != null){
                    mCrashCallBack.onCrashOccured(ex);
                }
                Looper.loop();
            }
        }.start();

        return true;
    }

    private void dumpExceptionToSDCard(final Throwable ex) throws IOException{
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if(DEBUG){
                Log.w(TAG, "sdcard unmounted,skip dump exception");
                return;
            }
        }

        File dir = new File(PATH);
        if(!dir.exists()){
            Log.d(TAG, "log dir not exists. ready create");
            dir.mkdirs();

        }

        long current = System.currentTimeMillis();
        final String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        final File file = new File(PATH + FILE_NAME + FLIE_NAME_SUFFIX);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try{
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
                    pw.println(time);
                    dumpPhoneInfo(pw,ex);
                    pw.close();
                }catch(Exception e){
                    Log.e(TAG, "dump crash info failed : " + Log.getStackTraceString(e));
                }
            }
        }).start();
    }

    private void dumpPhoneInfo(PrintWriter pw, Throwable ex)throws PackageManager.NameNotFoundException {
        Log.d(TAG, "dumpPhoneInfo ... start");
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        pw.print("App version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        //Android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print(" _ sdk: ");
        pw.println(Build.VERSION.SDK_INT);

        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //CPU架构
        pw.print("CPU ABI : ");
        pw.println(Build.CPU_ABI);
        pw.println();
        //异常信息
        ex.printStackTrace(pw);


    }

    private void uploadExceptionToServer(Throwable ex){
        //上传到服务器逻辑代码
    }



}
