package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.SurfaceControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrizeShotAndBlurThread extends Thread{
    private static final String TAG = "PrizeShotAndBlurThread";

    private final Context mContext;

    private static final String screenDirPath = "/storage/emulated/0/"+"ScreenShot";
    private static final String picPath = screenDirPath + "/"+ "ScreenShot" +".png";

    private static final float scaleFactor = 8;
    private static final float radius = 10;

    private Handler mHandler;

    public PrizeShotAndBlurThread(final Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void run() {
        Log.d(TAG,"run() ScreenShot and blur Start");
        long startMs = System.currentTimeMillis();
        Bitmap bitmap = screenshot();

//        savePic(bitmap,picPath);

        Bitmap mBlurBitmap = null;
        if(bitmap != null){
            mBlurBitmap = PrizeFastBlur.blurScale(bitmap,scaleFactor,radius);
            bitmap.recycle();
            bitmap = null;
        }
        Log.d(TAG,"Take：------>"+(System.currentTimeMillis() - startMs) + "ms");
        Message msg = mHandler.obtainMessage(PrizeShotBlurHandler.SHOT_BLUR_COMPLETED);
        msg.obj = mBlurBitmap;
        mHandler.sendMessage(msg);
    }


    private Bitmap screenshot(){
        long time1 = System.currentTimeMillis();
        Bitmap mScreenBitmap = null;
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = mWindowManager.getDefaultDisplay();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels , mDisplayMetrics.heightPixels };
        if (dims[0]>dims[1]) {
            mScreenBitmap = SurfaceControl.screenshot((int) dims[1], (int) dims[0]);
            Matrix matrix = new Matrix();
            matrix.reset();
            int rotation = mDisplay.getRotation();
            if(rotation==3){//rotation==3 
                matrix.setRotate(90);
            }else{//rotation==1 
                matrix.setRotate(-90);
            }
            Bitmap bitmap = mScreenBitmap;
            mScreenBitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(), bitmap.getHeight(),matrix, true);
            Log.e(TAG,"mScreenBitmap------------rotation-------->"+mScreenBitmap+", width---->"
                    +mScreenBitmap.getWidth()+", height----->"+mScreenBitmap.getHeight());
            bitmap.recycle();
            bitmap = null;
        }else{
            mScreenBitmap = SurfaceControl.screenshot((int) dims[0], ( int) dims[1]);
        }
        long time2 = System.currentTimeMillis();
        Log.d(TAG,"ScreenShot Take：------>"+(time2-time1) + "ms");
        return mScreenBitmap;
    }

    /**
     * save pic
     *
     * @param b
     * @param strFileName
     */
    private static void savePic(Bitmap b, String strFileName) {

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
