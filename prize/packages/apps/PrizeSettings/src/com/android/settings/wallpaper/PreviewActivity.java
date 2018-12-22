package com.android.settings.wallpaper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import com.android.settings.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.net.Uri;

/*prize-add-bugid:39747-yangming-2017_10_13-start*/
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.InputStream;
/*prize-add-bugid:39747-yangming-2017_10_13-end*/ 

import libcore.icu.LocaleData;
import android.provider.Settings;
import android.text.TextUtils;
import com.mediatek.common.prizeoption.PrizeOption;
import android.provider.MediaStore;
import android.database.Cursor;
import android.content.ContentResolver;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import android.os.Environment;
import android.preference.PreferenceManager;
public class PreviewActivity extends Activity implements View.OnClickListener{
	
    private TextView screenPreviewTV;
    private TextView launcherPreviewTV;
    private TextView isScrollTV;
    private TextView cancelTV;
    private TextView useTV;
    private CropImageLayout imagePreviewLayout;
    private ImageView imgPreview;
    private LinearLayout lockLL;
    private TextView daysTV;
    private TextView weekTV;
    private TextView timeTV;
    private RelativeLayout bottomRLContainerTwo;
    
    
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
    /*prize-add-bugid:39747-yangming-2017_10_13-start*/
    private DisplayImageOptions mDisplayImageOptions;
    private ImageSize targetSize;
    private int screenHeight;
    private int screenWidth;
    /*prize-add-bugid:39747-yangming-2017_10_13-end*/
    private AlertDialog magazinePopupWindow = null;
    private View mLoadingLayout;
    private static final String HAOKAN_PHOTO_PATH = "/Levect/com.levect.lc.koobee/.DCIM/";
	
	private static final Uri contentUrl = Uri.parse("content://com.android.settings.wallpaper.PrizeMagazineNetworkProvider");
    
    public static final String ACTION_PHOTO_UPDATED_NOTIFICATION = "photo_updated_notify_action";
    private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/AndroidClock.ttf";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i("setting", "PreviewActivity onCreate...");
    	super.onCreate(savedInstanceState);
    	
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.prize_wallpaper_preview_detail_layout);
        findViewById();
        bottomRLContainerTwo.setSystemUiVisibility(
                         View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                          | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        initImageLoader(this);
        initView();
        setPreviewTvTextColor(R.color.white, R.color.text_color_3478f6, isScreenChoose);
        setListener();
        StartTimer();
        setToLauncher();
    }
    
	private void findViewById() {
		screenPreviewTV = (TextView) findViewById(R.id.screen_preview_TV);
		launcherPreviewTV = (TextView) findViewById(R.id.launcher_preview_TV);
		isScrollTV = (TextView) findViewById(R.id.isScroll_TV);
		cancelTV = (TextView) findViewById(R.id.cancel_TV);
		useTV = (TextView) findViewById(R.id.use_TV);
		imagePreviewLayout = (CropImageLayout) findViewById(R.id.image_preview_layout);
		imgPreview = (ImageView) findViewById(R.id.img_preview);
		lockLL = (LinearLayout) findViewById(R.id.lock_LL);
		daysTV = (TextView) findViewById(R.id.days_TV);
		weekTV = (TextView) findViewById(R.id.week_TV);
		timeTV = (TextView) findViewById(R.id.time_TV);
		Typeface mClockTypeface = Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE);
        timeTV.setTypeface(mClockTypeface);
		bottomRLContainerTwo = (RelativeLayout) findViewById(R.id.bottom_RL_container_two);
		mLoadingLayout = (View) findViewById(R.id.layout_loading);
		
		
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
        lockLL.setVisibility(View.VISIBLE);
        imgPreview.setVisibility(View.GONE);
        setTime();
        
        wallID = getIntent().getStringExtra("wallID");
        wallType = getIntent().getStringExtra("wallType");
        if (getIntent().getStringExtra("localWallPath") != null)
            mLocalWallPath = getIntent().getStringExtra("localWallPath").trim();
        mActivityType = getIntent().getIntExtra("activityType", 0);
        if(wallType.equals("3")){
        	wallType = isBigBitmap(mLocalWallPath);
        }
        String path = null;
    	
        if (mActivityType == 0) {
        	path = WallpaperUtils.getDir("wallpaper") + wallID +2+".zip";
        }
        if (bitmap == null && mActivityType == 0) {
        	bitmap = WallpaperUtils.getWallpaper(this, path);
            
        	int width = 720;
            boolean isRightWallpaper = false;
            if(bitmap!=null){
                if(wallType.equals("2")){
                    isRightWallpaper = 2*width== bitmap.getWidth()? true:false;
                }else{
                    isRightWallpaper = width == bitmap.getWidth()?true:false;
                }
            }
            if(bitmap==null || isRightWallpaper==false){
            	Toast.makeText(this, getResources().getString(R.string.failed_to_read),
						Toast.LENGTH_SHORT).show();
                
                this.finish();
            }
        }
        
        if (mLocalWallPath != null && mActivityType == 4) {
            /*prize-change-bugid:39747-yangming-2017_10_13-start*/
            //bitmap = ImageLoader.getInstance().loadImageSync("file://" + mLocalWallPath, targetSize, mDisplayImageOptions);
            bitmap = BitmapFactory.decodeFile(mLocalWallPath);
            /*prize-change-bugid:39747-yangming-2017_10_13-end*/
        }else if(mLocalWallPath != null && mActivityType == 1) {
            Log.i("settings", "PreviewActivity from photo mActivityType mLocalWallPath = " + mLocalWallPath);
            /*prize-change-bugid:55830-2018_4_26-start*/
            //bitmap = ImageLoader.getInstance().loadImageSync("file://" + mLocalWallPath, targetSize, mDisplayImageOptions);
            try {
                bitmap = getPhotoBitmap(PreviewActivity.this, mLocalWallPath);
                Log.i("settings","loadImageSync getPhotoBitmap..  bitmap =" + bitmap.getByteCount());
            } catch (Exception e) {
                Log.i("settings","loadImageSync getPhotoBitmap..  Exception =" + e);
            }
            /*prize-change-bugid:55830-2018_4_26-end*/
        }
        if (bitmap == null)
            return;
        //imagePreviewLayout.setBitmap(fullScreenBitmap(bitmap));
        imagePreviewLayout.setBitmap(bitmap);
        /*prize-add-bugid:59336-bxh-2018_6_6-start*/ 
        float viewScale = getPreViewScale(bitmap);
        /*prize-add-bugid:55415-yangming-2018_4_27-start*/
//        if(PrizeOption.PRIZE_NOTCH_SCREEN && mActivityType == 4){
//            imagePreviewLayout.setScaleY(1.04f);
//        }
        imagePreviewLayout.setScaleY(viewScale);
        imagePreviewLayout.setScaleX(viewScale);
        /*prize-add-bugid:59336-bxh-2018_6_6-end*/ 
        /*prize-add-bugid:55415-yangming-2018_4_27-end*/
        imagePreviewLayout.setOutputSize(screenWidth, screenHeight);
        if (WallpaperUtils.isExistFile(SHOT_CUT_PICTURE_PATH)) {
            if (cut == null) {
                /*prize-change-bugid:40831-yangming-2017_10_18-start*/
                //cut = BitmapFactory.decodeFile(SHOT_CUT_PICTURE_PATH);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Bitmap.Config.RGB_565;
                option.inPurgeable = true;
                option.inInputShareable = true;
                Uri uri = Uri.parse("file://" + SHOT_CUT_PICTURE_PATH);
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    cut = BitmapFactory.decodeStream(is, null, option);
                } catch (Exception e) {
                    Log.e("wallpaper"," Bitmap cut openInputStream Exception = " + e);
                }
                /*prize-change-bugid:40831-yangming-2017_10_18-end*/
            }
            imgPreview.setImageBitmap(cut);

        }
    }
    
    private void setTime() {
        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.dateformat));
        Date date = new Date(System.currentTimeMillis());
        String days = formatter.format(date);

        SimpleDateFormat formatterTwo = new SimpleDateFormat("HH:mm");
        Date dateTwo = new Date(System.currentTimeMillis());
        String time = formatterTwo.format(dateTwo);

        String week = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        if ("1".equals(week)) {
            week = getString(R.string.sunday);
        } else if ("2".equals(week)) {
            week = getString(R.string.monday);
        } else if ("3".equals(week)) {
            week = getString(R.string.tuesday);
        } else if ("4".equals(week)) {
            week = getString(R.string.wednesday);
        } else if ("5".equals(week)) {
            week = getString(R.string.thursday);
        } else if ("6".equals(week)) {
            week = getString(R.string.friday);
        } else if ("7".equals(week)) {
            week = getString(R.string.saturday);
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
            finish();
            break;
 
        case R.id.use_TV:
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
                setPreviewTvTextColor(R.color.white, R.color.text_color_3478f6, isScreenChoose);
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
                setPreviewTvTextColor(R.color.white, R.color.text_color_3478f6, isScreenChoose);
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

        case R.id.launcher_TV:
            Intent intent = new Intent();
            intent.setAction("com.android.launcher3.action.SCROLL_WORKSPACE");
            intent.putExtra("wallType", wallType);
            /*prize-change-bugid:60082-bxh-2018_6_6-start*/
            if (isScrollTV.isSelected()) {
                intent.putExtra("isScroll", true);
                WallpaperUtils.applyType(PreviewActivity.this, 1, bitmap, mLocalWallPath, mActivityType, true);
            } else {
                intent.putExtra("isScroll", false);
                WallpaperUtils.applyType(PreviewActivity.this, 1, imagePreviewLayout.crop(), mLocalWallPath, mActivityType, false);
            }
            /*prize-change-bugid:60082-bxh-2018_6_6-end*/
            sendBroadcast(intent);
            rightPopupWindow.dismiss();
            break;
        case R.id.screen_lock_RL:
    		int changedMagazine = Settings.System.getInt(getContentResolver(),
					Settings.System.PRIZE_MAGAZINE_KGWALLPAPER_SWITCH , 0);
        	if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW && changedMagazine == 1){
        		//initMagazinePop();//prize-add-for-change magazine lockscreen-xiekui-201800820
                setMagazineWallpapers();
        	}else{
        		LockScreen(2);
        	}
            break;
            
        /*case R.id.all_TV:
        	
            if (mActivityType == 0) {
                LockScreen(3);
            } else if (mActivityType == 1 || mActivityType == 4) {
                Intent intent1 = new Intent();
                intent1.setAction("com.android.launcher3.action.SCROLL_WORKSPACE");
            	int max = 10000;
                int min = 1000;
                Random random = new Random();
                int s = random.nextInt(max) % (max - min + 1) + min;
                if (WallpaperUtils.isExistFile(WallpaperUtils.getDir("cache") + "/" + s + "_wall" + ".zip")) {	
                    s = random.nextInt(max) % (max - min + 1) + min;
                }
                File file = WallpaperUtils.createFile(WallpaperUtils.getDir("cache"), s + "_wall" + ".zip");
                WallpaperUtils.saveBitmapToFile(bitmap, file);
            	if(isScrollTV.isSelected()) {
                    intent1.putExtra("isScroll", true);
                    intent1.putExtra("wallType", wallType);
                    sendBroadcast(intent1);
                	rightPopupWindow.dismiss();
                	WallpaperUtils.applyType(this, 1, bitmap, mLocalWallPath, mActivityType, true);
                	setLockScreenWallpapaer();
            	}else{
                    intent1.putExtra("isScroll", false);
                    intent1.putExtra("wallType", wallType);
                    sendBroadcast(intent1);
                	rightPopupWindow.dismiss();
                	WallpaperUtils.applyType(this, 1, bitmap, mLocalWallPath, mActivityType, false);
                	setLockScreenWallpapaer();
            	}
            }
            
            break;*/
        case R.id.popu_lock_RL:
        	LockScreen(2);
        	break;
        
        case R.id.popu_magazine_RL:
        	setMagazineWallpapers();
        	break;
		}
	}
	
    private void setPreviewTvTextColor(int pWhite, int pText_color_3478f6, boolean isScreenChoose) {
        try {
            /*if (new File(mLocalWallPath).getParentFile().getCanonicalPath().equals(
            		new File("/system/media/config/wallpaper/wallpaper02/wallpaper02_icon.png")
                        .getParentFile().getCanonicalPath())) {*/
                if (!isScreenChoose) {
                    screenPreviewTV.setTextColor(getResources().getColor(pWhite));
                    launcherPreviewTV.setTextColor(getResources().getColor(pText_color_3478f6));
                    Log.i("setting", "!isScreenChoose");
                } else {
                    screenPreviewTV.setTextColor(getResources().getColor(pText_color_3478f6));
                    launcherPreviewTV.setTextColor(getResources().getColor(pWhite));
                    Log.i("setting", "isScreenChoose");
                }
            //}
        } catch (Exception pE) {
            pE.printStackTrace();
        }
    }
    

    private void LockScreen(int type) {
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        if (WallpaperUtils.isExistFile(WallpaperUtils.getDir("cache") + "/" + s + "_wall" + ".zip")) {	
            s = random.nextInt(max) % (max - min + 1) + min;
        }
        File file = WallpaperUtils.createFile(WallpaperUtils.getDir("cache"), s + "_wall" + ".zip");
        /*prize-change-bugid:60082-bxh-2018_6_6-start*/
      //  WallpaperUtils.saveBitmapToFile(bitmap, file);
        copy(mLocalWallPath, file.getAbsolutePath(), false);
        /*prize-change-bugid:60082-bxh-2018_6_6-end*/
        if (type == 3 && isScrollTV.isSelected()) {
            Intent intent = new Intent();
            intent.setAction("com.android.launcher3.action.SCROLL_WORKSPACE");
            if (mActivityType == 0) {
                intent.putExtra("isScroll", true);
            }
            intent.putExtra("wallType", wallType);
            sendBroadcast(intent);
            if (bitmap != null) {
            	WallpaperUtils.setScrollBitmap(bitmap);
            }
            /*prize-change-bugid:60082-bxh-2018_6_6-start*/
            WallpaperUtils.applyType(this, type, bitmap, file.getAbsolutePath(), mActivityType, isScrollTV.isSelected());
        } else{
        	WallpaperUtils.applyType(this, type, imagePreviewLayout.crop(), file.getAbsolutePath(), mActivityType, isScrollTV.isSelected());
        }
        /*prize-change-bugid:60082-bxh-2018_6_6-end*/
        if(rightPopupWindow != null){
            rightPopupWindow.dismiss();
        }
        if(magazinePopupWindow != null){
            magazinePopupWindow.dismiss();
        }
		//Toast.makeText(this, getResources().getString(R.string.wall_is_set), Toast.LENGTH_SHORT).show();
    }
    
    private void initPop() {
        rightPopupWindow = new AlertDialog.Builder(this,
                R.style.wallpaper_use_dialog_style).create();
        rightPopupWindow.show();
        View loginwindow = this.getLayoutInflater().inflate(
                R.layout.popwindow_tags_layout, null);
        loginwindow.setBackgroundColor(getResources().getColor(R.color.white));
        TextView launcherTV = (TextView) loginwindow.findViewById(R.id.launcher_TV);
        //TextView all_TV = (TextView) loginwindow.findViewById(R.id.all_TV);
        RelativeLayout screen_lock_RL = (RelativeLayout) loginwindow.findViewById(R.id.screen_lock_RL);
        launcherTV.setOnClickListener(this);
        //all_TV.setOnClickListener(this);
        screen_lock_RL.setOnClickListener(this);

        Window window = rightPopupWindow.getWindow();
        window.setContentView(loginwindow);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels; 
        WindowManager.LayoutParams p = window.getAttributes();
        p.width = screenWidth;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(p);
        window.setGravity(Gravity.CENTER | Gravity.BOTTOM); 

        rightPopupWindow.setContentView(loginwindow);
    }
    private void initMagazinePop() {
    	if(rightPopupWindow != null ){
    		rightPopupWindow.dismiss();
    	}
    	magazinePopupWindow = new AlertDialog.Builder(this,
                R.style.wallpaper_use_dialog_style).create();
    	magazinePopupWindow.show();
    	View loginwindow = this.getLayoutInflater().inflate(
                R.layout.magazine_popwindow_tags_layout, null);
    	loginwindow.setBackgroundColor(getResources().getColor(R.color.white));
        RelativeLayout popu_lock_RL = (RelativeLayout) loginwindow.findViewById(R.id.popu_lock_RL);
        RelativeLayout popu_magazine_RL = (RelativeLayout) loginwindow.findViewById(R.id.popu_magazine_RL);
        popu_lock_RL.setOnClickListener(this);
        popu_magazine_RL.setOnClickListener(this);
        
        Window window = magazinePopupWindow.getWindow();
        window.setContentView(loginwindow);
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels; 
        WindowManager.LayoutParams p = window.getAttributes();
        p.width = screenWidth;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(p);
        window.setGravity(Gravity.CENTER | Gravity.BOTTOM); 

        magazinePopupWindow.setContentView(loginwindow);
    }
    
    private void setMagazineWallpapers() {
        if (magazinePopupWindow != null) {
            magazinePopupWindow.dismiss();
        }
    	Uri uri = null;
    	if(mLocalWallPath != null){
    		uri = getUri(mLocalWallPath);
    		Log.i("haokantest", "add photo to magazine lockscreen mLocalWallPath = " + mLocalWallPath + ";;uri=" + uri);
    		// /storage/emulated/0/DCIM/Camera/IMG_20171108_172351.jpg
    	}
    	
    	if(uri != null){
    		
    		String path = Environment.getExternalStorageDirectory().toString() + HAOKAN_PHOTO_PATH;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        	String[] fileNames = getPhotoUriString();
            if(fileNames != null){
                for(int i = 0; i< fileNames.length; i++){
                    String inverseStr = fileNames[i].substring(5);
                    String timeStr = inverseStr.substring(inverseStr.indexOf("_")+1,inverseStr.length() - 4);
                    String lastPathSegment = timeStr.substring(timeStr.indexOf("_")+1,timeStr.length());
                    if(lastPathSegment.equals(uri.getLastPathSegment())){
                        Log.i("haokantest","uriStr[i] = " + fileNames[i] +";;;uri.getLastPathSegment() = " + uri.getLastPathSegment());
                        Toast.makeText(this,getString(R.string.photo_is_exist),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            int inverseNum = Utils.bindTextColor(bitmap);
            String picName = new StringBuilder(".img_")
            .append(inverseNum + "_" )
            .append(System.currentTimeMillis() + "_")
            .append(uri.getLastPathSegment())
            .append(".jpg")
            .toString();
            final File f = new File(dir, picName);
            if (!isFileExist(f.getPath())) {
            	checkLocalPhotos();
            saveBitmapToFile(this, bitmap, f, false);
            ContentValues values = new ContentValues();
			values.clear();
            values.put("isnewphoto",1);
            getContentResolver().update(contentUrl, values, null, null);
            Log.i("haokantest","SETTINGS !!! save photo update ContentProvider!!");
            }/*else{
            	magazinePopupWindow.dismiss();
        		Toast.makeText(this, getResources().getString(R.string.wall_is_set), Toast.LENGTH_SHORT).show();
        		finish();
        		return;
            }*/
    	}
    	else if(uri == null && wallType.equals("1")){
    		
    		String path = Environment.getExternalStorageDirectory().toString() + HAOKAN_PHOTO_PATH;
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String mLocalWallPathName = mLocalWallPath.replace("/", ".");
        	String[] fileNames = getPhotoUriString();
            if(fileNames != null){
                for(int i = 0; i< fileNames.length; i++){
                    String inverseStr = fileNames[i].substring(5);
                    String timeStr = inverseStr.substring(inverseStr.indexOf("_")+1,inverseStr.length() - 4);
                    String lastPathSegment = timeStr.substring(timeStr.indexOf("_")+1,timeStr.length());
                    if(lastPathSegment.equals(mLocalWallPathName.substring(0, mLocalWallPathName.length() - 4))){
                    	Log.i("haokantest","Preview lastPathSegment = " + lastPathSegment
                       		 + "Preview mLocalWallPath = " + mLocalWallPath);
                        Toast.makeText(this,getString(R.string.wallpaper_is_exist),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            int inverseNum = Utils.bindTextColor(bitmap);
            String picName = new StringBuilder(".img_")
            .append(inverseNum + "_" )
            .append(System.currentTimeMillis() + "_")
            .append(mLocalWallPathName)
            .toString();
            final File f = new File(dir, picName);
            if (!isFileExist(f.getPath())) {
            	checkLocalPhotos();
            	saveBitmapToFile(this, bitmap, f, false);
                ContentValues values = new ContentValues();
				values.clear();
                values.put("isnewphoto",1);
                getContentResolver().update(contentUrl, values, null, null);
                Log.i("haokantest","SETTINGS !! save local wallpapers update ContentProvider!!");
            }/*else{
            	magazinePopupWindow.dismiss();
        		Toast.makeText(this, getResources().getString(R.string.wall_is_set), Toast.LENGTH_SHORT).show();
        		finish();
        		return;
            }*/
    	}
    	Intent intent = new Intent();
    	intent.setAction(ACTION_PHOTO_UPDATED_NOTIFICATION);
    	intent.putExtra("isNewPhoto", true);
    	sendBroadcast(intent);
    	Log.i("haokantest","PrizeSettings PreviewActivity sendBroadcast update Photo");
    	finish();
			
    	Toast.makeText(PreviewActivity.this, getResources().getString(R.string.wall_is_set), Toast.LENGTH_SHORT).show();
    }
    private boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }
    
    private void checkLocalPhotos(){

        String path = Environment.getExternalStorageDirectory().toString() + HAOKAN_PHOTO_PATH;
        File dir = new File(path);
        if(dir.exists()){
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.startsWith(".img")) {
                        return true;
                    }
                    return false;
                }
            });

            if(files != null && files.length > 2){
            	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            	int fileNum = preferences.getInt("fileNum", 0);
            	File file;
            	if(fileNum > 2){
            		fileNum = 0;
            		file = new File(files[fileNum].getAbsolutePath());
            		preferences.edit().putInt("fileNum", fileNum + 1).apply();
            	}else{
            		file = new File(files[fileNum].getAbsolutePath());
            		preferences.edit().putInt("fileNum", fileNum + 1).apply();
            	}
            	Log.i("haokantest", "photos > 2 delete one dele fileNum = " + fileNum);
                file.delete();
            }

        }
    }
    private boolean saveBitmapToFile(Context context, Bitmap destBitmap, File f, final boolean notifySystem) {
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (fos == null) {
            return false;
        }
        destBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        boolean success = false;
        try {
            fos.flush();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /*if (notifySystem && f.exists() && success) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(f));
                context.sendBroadcast(intent);
            }*/
            try {
                if (fos != null) {
                	fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
    private Uri getUri(String path){
    	Uri uri = null;
    	if (path != null) {
    	path = Uri.decode(path);
    	ContentResolver cr = this.getContentResolver();
    	StringBuffer buff = new StringBuffer();
    	buff.append("(")
    	.append(MediaStore.Images.ImageColumns.DATA)
    	.append("=")
    	.append("'" + path + "'")
    	.append(")");
    	Cursor cur = cr.query(
    	MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
    	new String[] { MediaStore.Images.ImageColumns._ID },
    	buff.toString(), null, null);
    	int index = 0;
    	for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
    	index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
    	// set _id value
    	index = cur.getInt(index);
    	}
    	if (index == 0) {
    	//do nothing
    	} else {
    	Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
    	if (uri_temp != null) {
    	uri = uri_temp;
    	}
    	}
    	}
    	return uri;
    	}
    
    protected void onDestroy() {
    	
        mHandler.removeCallbacksAndMessages(null);
        imgPreview = null;
        imagePreviewLayout = null;
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        mTimer.cancel();
        super.onDestroy();
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        /*prize-delete-bugid:40831-yangming-2017_10_18-start*/
        //setToLauncher();
        /*prize-delete-bugid:40831-yangming-2017_10_18-end*/
        if (cut != null && !cut.isRecycled()) {
            cut.recycle();
            cut = null;
        }

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if(WallpaperUtils.isExistFile(SHOT_CUT_PICTURE_PATH)) {	
            if (cut == null) {
                /*prize-change-bugid:40831-yangming-2017_10_18-start*/
                //cut = BitmapFactory.decodeFile(SHOT_CUT_PICTURE_PATH);
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Bitmap.Config.RGB_565;
                option.inPurgeable = true;
                option.inInputShareable = true;
                Uri uri = Uri.parse("file://" + SHOT_CUT_PICTURE_PATH);
                try {
                    InputStream is = getContentResolver().openInputStream(uri);
                    cut = BitmapFactory.decodeStream(is, null, option);
                } catch (Exception e) {
                    Log.e("wallpaper"," Bitmap cut openInputStream Exception = " + e);
                }
                /*prize-change-bugid:40831-yangming-2017_10_18-end*/
            }
            imgPreview.setImageBitmap(cut);
        }
        Log.i("settings", "onResume bitmap = " + bitmap + ",,mLocalWallPath = " + mLocalWallPath);
    }
    
    private void setToLauncher() {
        sendBroadcast(new Intent("com.android.launcher3.action.PRELOAD_WORKSPACE"));
        Log.i("setting", "sendBroadcast launcher3 PRELOAD_WORKSPACE");
    }

    public void setApplyType(int type) {
        if (type == 1 || type == 3) {
        	Log.i("setting", "preview setApplyTyoe type = 1||3");
        }
    }

	private void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCacheSize(50 * 1024 * 1024)
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.build();
		ImageLoader.getInstance().init(config);
		/*prize-add-bugid:39747-yangming-2017_10_13-start*/
        mDisplayImageOptions = new DisplayImageOptions.Builder()
                 .bitmapConfig(Bitmap.Config.ARGB_8888)
                 .build();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
        targetSize = new ImageSize(screenWidth, screenHeight);
        Log.i("wallpapertest", "initImageLoader screenWidth= " + screenWidth + ";screenHeight" + screenHeight);
		/*prize-add-bugid:39747-yangming-2017_10_13-end*/
	}
    
	private Bitmap fullScreenBitmap(Bitmap bitmap){
		int bitmapHeight = bitmap.getHeight();
		int bitmapWidth = bitmap.getWidth();
		int statusBarHeight = -1;  
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");  
		if (resourceId > 0) {  
		    statusBarHeight = getResources().getDimensionPixelSize(resourceId);  
		} 
		int tempScreenHeight = screenHeight;
		int height = tempScreenHeight - bitmapHeight;
		int width = screenWidth - bitmapWidth;
		if(height < 0 && width < 0){
			Log.i("wallpaper","fullScreenBitmap return bitmap ***");
			return bitmap;
		}
		Log.i("wallpaper", "bitmapHeight = " + bitmapHeight + "::tempScreenHeight = " + tempScreenHeight + 
				 "::bitmapWidth = " + bitmapWidth + "::screenWidth = " + screenWidth);
		if(height >= width && bitmapHeight < tempScreenHeight){
			float scaleHeight = ((float) tempScreenHeight) / bitmapHeight;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleHeight, scaleHeight);
			/*prize-change-bugid:50534-yangming-2018_2_26-start*/
			//ZoomCropImageView.setScaleMin(1.0f, false, true);
			ZoomCropImageView.setScaleMin(1.0f, false, false);
			/*prize-change-bugid:50534-yangming-2018_2_26-end*/
			Bitmap newBitmap = Bitmap.createBitmap(bitmap,0, 0, bitmapWidth, bitmapHeight, matrix, true);
			return newBitmap;
		}
		if(width >= height && bitmapWidth < screenWidth){
			float scaleWidth = ((float) screenWidth) / bitmapWidth;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleWidth);
			/*prize-change-bugid:50534-yangming-2018_2_26-start*/
			//ZoomCropImageView.setScaleMin(1.0f, true, false);
			ZoomCropImageView.setScaleMin(1.0f, false, false);
			/*prize-change-bugid:50534-yangming-2018_2_26-end*/
			Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
			return newBitmap;
		}
		return bitmap;
	}
	private String isBigBitmap(String path){
		/*prize-change-bugid:39747-yangming-2017_10_13-start*/
		/*Bitmap pathBitmap = ImageLoader.getInstance().loadImageSync("file://" + path);
		int width = pathBitmap.getWidth();*/
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inJustDecodeBounds = true;
		Bitmap pathBitmap = BitmapFactory.decodeFile(path, option);
		int width = option.outWidth;
		/*prize-change-bugid:39747-yangming-2017_10_13-end*/
		Log.i("wallpaper", "isBigBitmap bitmap width = " + width + "screenWidth = " + screenWidth );
		if(width >= screenWidth * 2){
			return "2";
		}
		return "1";
	}
	private void setLockScreenWallpapaer(){
    	WallpaperUtils.ShutDownKGWALLPAPE(this);
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] getPhotoUriString(){

        String[] uriStr = null;
        String path = Environment.getExternalStorageDirectory().toString() +  "/Levect/" + "com.levect.lc.koobee" + "/" + ".DCIM/";//;Values.Path.PATH_DCIM_PIC;
        File dir = new File(path);
        if(dir.exists()){
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.startsWith(".img")) {
                        return true;
                    }
                    return false;
                }
            });

            if(files != null){
                uriStr = new String[files.length];
                for(int i = 0; i< files.length; i++){
                    uriStr[i] = files[i].getName();
                }
            }

        }
        return uriStr;
    }
	/*prize-add-bugid:55830-2018_4_26-start*/
	private Bitmap getPhotoBitmap(Context context, String path) throws FileNotFoundException,IOException{
		Bitmap photoBitmap;
		int be = 1;
		InputStream input = context.getContentResolver().openInputStream(Uri.parse("file://" + path));
		BitmapFactory.Options mOptions = new BitmapFactory.Options();
		mOptions.inJustDecodeBounds = true;
		mOptions.inDither = true;
		mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		BitmapFactory.decodeStream(input, null, mOptions);
		input.close();
		int bitmapHeight = mOptions.outHeight;
		int bitmapWidth = mOptions.outHeight;
		if(bitmapHeight > bitmapWidth && bitmapHeight > screenHeight){
			be = bitmapHeight / screenHeight;
		}else if(bitmapWidth > bitmapHeight && bitmapWidth > screenWidth){
			be = bitmapWidth / screenWidth;
		}else if(bitmapHeight == bitmapWidth  && bitmapHeight > screenWidth){
			be = bitmapHeight / screenHeight;
		}
		if(be <= 0) be = 1;
		Log.i("settings", "getPhotoBitmap bitmapHeight = " + bitmapHeight + ";;bitmapWidth = " + bitmapWidth);
		Log.i("settings", "getPhotoBitmap be = " + be);
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inDither = true;
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		bitmapOptions.inSampleSize = be;
		input = context.getContentResolver().openInputStream(Uri.parse("file://" + path));
		photoBitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		input.close();
		return photoBitmap;
	}
	/*prize-add-bugid:55830-2018_4_26-end*/
	
	 /*prize-add-bugid:60082-bxh-2018_6_6-start*/
	private boolean copy(String src, String des, boolean delete)
	{
		File file = new File(src);
		if (!file.exists()) { return false; }
		File desFile = new File(des);
		FileInputStream in = null;
		FileOutputStream out = null;
		try
		{
			in = new FileInputStream(file);
			out = new FileOutputStream(desFile);
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, count);
				out.flush();
			}
		}
		catch (Exception e)
		{
//			LogUtils.e(e);
			return false;
		}
		finally
		{
			close(in);
			close(out);
		}
		if (delete)
		{
			file.delete();
		}
		return true;
	}
	
	
	
	private  boolean close(Closeable io)
	{
		if (io != null)
		{
			try
			{
				io.close();
			}
			catch (IOException e)
			{
//				LogUtils.e(e);
			}
		}
		return true;
	}
	
	private float getPreViewScale(Bitmap map) {
		int width = map.getWidth();
		int height = map.getHeight();
		if (screenHeight/screenWidth <=3 && width < screenWidth || height < screenHeight) {
			float scaleX = ((float) screenWidth) / bitmap.getWidth();
			float scaleY = ((float) screenHeight) / bitmap.getHeight();
			return scaleY > scaleX ? scaleY : scaleX;
		}
		return 1.0f;
	}

	/*prize-add-bugid:60082-bxh-2018_6_6-end*/
}
