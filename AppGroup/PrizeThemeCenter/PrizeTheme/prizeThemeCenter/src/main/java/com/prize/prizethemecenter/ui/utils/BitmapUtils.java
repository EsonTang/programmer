package com.prize.prizethemecenter.ui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.prize.app.beans.ClientInfo;
import com.prize.app.util.JLog;
import com.prize.prizethemecenter.MainApplication;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.activity.MineLocalWallpaperActivity;
import com.prize.prizethemecenter.activity.PreviewActivity;
import com.prize.prizethemecenter.activity.WallDetailActivity;
import com.prize.prizethemecenter.manage.DownloadTaskMgr;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Administrator on 2016/10/17.
 */
public class BitmapUtils {

    private static final String KGWALLPAPER_SETTING_OFF_ACTION = "system.settings.changedwallpaper.off";
    private static Bitmap mBitmap;

    /**保存bitmap到文件中*/
    public static void saveBitmapToFile(Bitmap bitmap,File fileName){
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**文件转为Bitmap*/
    public static Bitmap convertToBitmap(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 返回为空
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        return bitmap;
    }


    /***
     * 设置壁纸
     * @param ctx
     * @param bitmap
     */
    public static boolean setWallpaperPrize(Context ctx, Bitmap bitmap) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ctx);
            wallpaperManager.setBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /***
     * 设置壁纸
     * @param ctx
     * @param in
     */
    public static boolean setWallpaperPrizeByStream(Context ctx, InputStream in) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(ctx);
            wallpaperManager.setStream(in);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /***
     * 设置壁纸
     * @param ctx
     * @param f 图片文件
     */
    public static boolean setWallpaperPrize(Context ctx, File f) {
        FileInputStream fin = null;
        try {
            if (f.exists()) {
                fin = new FileInputStream(f);
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(ctx);
                wallpaperManager.setStream(fin);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /***
     * 应用壁纸
     * @param type 1壁纸， 2 锁屏， 3 ALL
     */
    public static void applyType(Context context,int type, Bitmap bit, String path,int activityType,boolean isScroll) {
        new ApplyTask(context,type, bit, path,activityType,isScroll).execute();
    }

    /***
     * 应用壁纸或锁屏或ALL
     * @author fanjunchen
     *
     */
    static class  ApplyTask extends AsyncTask<Void, Void, Boolean> {

        private Context mCtx;

        private int type;

        private Bitmap bitmap;

        private String path;

        private boolean isLockSuccess = false;

        private int mActivityType;
		private boolean isScroll = false;

        public ApplyTask(Context context,int t, Bitmap bit, String p,int pType,boolean Scroll) {
            mCtx = context;
            type = t;
            bitmap = bit;
            path = p;
            mActivityType = pType;
			isScroll = Scroll;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean b = false;
            publishProgress();
            if (type == 1) {
                if(isScroll){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    InputStream isBm = new ByteArrayInputStream(baos.toByteArray());
                    b = BitmapUtils.setWallpaperPrizeByStream(mCtx,isBm);
                }else{
                    b = BitmapUtils.setWallpaperPrize(mCtx, bitmap);
                }
            }
            else if (type == 2) {
                // 应用锁屏壁纸
                ShutDownKGWALLPAPE(mCtx);
                try {
                    b = isLockSuccess = Settings.System.putString(mCtx.getContentResolver(), "keyguard_wallpaper", path);
                } catch (Exception pE) {
                    pE.printStackTrace();
                }
            }
            else if (type == 3) {
                ShutDownKGWALLPAPE(mCtx);
                if(isScroll && mBitmap!=null){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    InputStream is = new ByteArrayInputStream(out.toByteArray());
                    b = BitmapUtils.setWallpaperPrizeByStream(mCtx,is);
                }else{
                    b = BitmapUtils.setWallpaperPrize(mCtx, bitmap);
                }
                // 应用锁屏壁纸
                try{
                    isLockSuccess = Settings.System.putString(mCtx.getContentResolver(), "keyguard_wallpaper", path);
                }catch (Exception e){
                    e.printStackTrace();
                    JLog.i("hu",e.toString());
                }
            }
            return b;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            initPop((Activity) mCtx);
        }

        @Override
        public void onPostExecute(Boolean b) {
            if (b) {
                rightPopupWindow.dismiss();
                Toast.makeText(mCtx, mCtx.getString(R.string.wall_is_set), Toast.LENGTH_SHORT).show();
                if(mCtx instanceof WallDetailActivity) {
                     WallDetailActivity t = (WallDetailActivity) mCtx;
                     t.setApplyType(type);
                }
                else if(mCtx instanceof PreviewActivity &&mActivityType==0){
                    PreviewActivity p = (PreviewActivity) mCtx;
                    p.setApplyType(type);
                    UIUtils.updateLocalStates("wallpaperId",mCtx);
                }
                else if(mCtx instanceof PreviewActivity &&mActivityType==1){
                    PreviewActivity p = (PreviewActivity) mCtx;
                    Intent intent = new Intent();
                    intent.setAction(MineLocalWallpaperActivity.LOCAL_WALL_SELECTED_ACTION);
                    intent.putExtra("localWallPath", path);
                    p.sendBroadcast(intent);
                    DownloadTaskMgr.getInstance().setDownloadTaskState(2);
                    DBUtils.cancelLoadedState(2);
                }
            }else {
                ToastUtils.showToast(R.string.wall_set_failed);
            }
        }
    }

    public static void setScollBitmap(Bitmap bitmap){
        mBitmap = bitmap;
    }
    /**关闭系统百变壁纸*/
    public static void ShutDownKGWALLPAPE(Context context){
        Intent intent = new Intent(KGWALLPAPER_SETTING_OFF_ACTION);
        context.sendBroadcast(intent);
    }

    private static AlertDialog rightPopupWindow = null;
    private static void initPop(Activity context) {
        rightPopupWindow = new AlertDialog.Builder(context).create();
        rightPopupWindow.show();
        View loginwindow = context.getLayoutInflater().inflate(
                R.layout.popwindow_setwallpaper_layout, null);
        Window window = rightPopupWindow.getWindow();
        window.setContentView(loginwindow);
        WindowManager.LayoutParams p = window.getAttributes();
        WindowManager wm = (WindowManager) MainApplication.curContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        if(width<=720){
            p.width = 600;
        }else{
            p.width = 900;
        }
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;

        window.setAttributes(p);
        window.setGravity(Gravity.CENTER);
//        rightPopupWindow.setContentView(loginwindow);
    }


    /***
     * 创建某文件
     * @param dir
     * @param name
     * @return
     */
    public static File createFile(String dir, String name) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File file = new File(dirFile, name);
        return file;
    }

    /***
     * 文件是否存在
     * @param path
     * @return
     */
    public static boolean isExistFile(String path) {
        File f = new File(path);
        boolean b = f.exists();
        f = null;
        return b;
    }

    public static  Bitmap getWallpaper(Context applicationContext, String wallPath) {
        Resources resources = null;
        if(resources==null) {
            resources= getWallpaperRes(applicationContext,wallPath);
        }
        InputStream instr = null;
        Bitmap rettemp = null;
        int dpi = getWindowXP();
        try {
            if(resources!=null)
                if(dpi==1){
                    instr = resources.getAssets().open("wallpaper/h.jpg");
                }
                else if(dpi==2){
                    instr = resources.getAssets().open("wallpaper/x.jpg");
                    //\wallpaper\xhdpi\wallpaper\default_wallpaper.jpg
                }
                else if(dpi==3){
                    instr = resources.getAssets().open("wallpaper/xx.jpg");
                }
            if (instr != null) {
                rettemp = BitmapFactory.decodeStream(instr);
                instr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rettemp;
    }


    public static  Resources getWallpaperRes(Context context,String themePath) {
        Resources s=null;
        try {
            AssetManager asm = AssetManager.class.newInstance();
            AssetManager.class.getMethod("addAssetPath", String.class).invoke(asm, themePath);
            Resources res = context.getResources();
            s= new Resources(asm, res.getDisplayMetrics(), res.getConfiguration());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return s;
    }


    public static int getWindowXP() {
        int dpi = 0;
        int width = ClientInfo.screenWidth;
        int height = ClientInfo.screenHeight;
        if (width<540 && height <= 960) {  //hdpi
            dpi = 1;
        } else if (width>540 && width<=720 && height > 960 && height <= 1280) {  //xhdpi
            dpi = 2;
        } else if (width>720 && width<=1080 && height > 1280 && height <=1920) {  //xxxhdpi
            dpi =3;
        }
        return dpi;
    }
}
