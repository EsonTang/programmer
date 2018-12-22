/*
* Base on 'BatteryMeterViewDefined.java'.
* Creating it for simply modify some codes for
* avoiding memeory leatek and keep thread-safe accessing.
* prize-linkh-20150905
*/

package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
//import android.os.Handler.Callback;
//import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.android.systemui.statusbar.policy.BatteryController;
import com.mediatek.systemui.statusbar.util.BatteryHelper;
import com.android.systemui.R;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix; 

import android.app.StatusBarManager;
import com.mediatek.common.prizeoption.PrizeOption;
import com.android.systemui.statusbar.phone.PrizeStatusBarStyle;
import android.os.SystemProperties;
    
public class BatteryMeterViewDefinedNew extends ImageView implements DemoMode,
        BatteryController.BatteryStateChangeCallback {
    public static final String TAG = "BatteryMeterViewDefinedNew";
    private static final boolean DEBUG = false;
    public static final String ACTION_LEVEL_TEST = "com.android.systemui.BATTERY_LEVEL_TEST";

    private BatteryController mBatteryController;
    private boolean mPowerSaveEnabled;
    
    private Context mContext;
    private int mLevel;
    private boolean mPluggedIn;
    private boolean mCharging;
    private boolean mCharged;
    private boolean mIsShowLevel = false;
    //Bitmap bitmap1 = null;
    //Bitmap bitmap2 = null;
    //Bitmap bitmap3 = null;
    
    Bitmap mChargeEmptyBm = null;
    Bitmap mChargingBm = null;
    Bitmap mBatteryLowBm = null;
    
    private final int PADDING = 1 ;
    private static final int FULL = 96;
    private static final int  MSG_UPDATE_CHARGING_ANIMATION = 0;
    private static final int  MSG_REQUEST_QUITING = 1;
    private static final int  QUIT_CHARGING_THREAD_DELAY = 5 * 60 * 1000; // minutes.
    
    private Thread t = null;
    private String mWarningString;
    private MyHandler mMainHandler = new MyHandler();
    final Paint paint ;
    final Paint mWarningTextPaint;
    private float mWarningTextHeight;
    int mWidth;
    int mHeight;
    //Bitmap newBitmap = null;
    Canvas mCanvas = new Canvas();
    Matrix mMatrix = null;
    int[] images = { 1, 2, 3, 4};

    //add for status bar style. prize-linkh-20150902.
    int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    private Bitmap mChargeEmptyBmBlack = null;
    private Bitmap mChargingBmBlack = null;

    //add for avoiding memeory leak and ensure thread-safe accessing.
    private Canvas mCanvasForChargingThread = null;
    private Bitmap mBitmapForChargingThread = null;
    private Thread mChargingThread = null;
    private boolean mRequestQuiting = false;

    //add for debug battery level
    private boolean mInDebugBatteryLevelMode = false;
    private static final String PROP_DEBUG_BATTERY_LEVEL = "sys.dbg_battery_level";    
    private static final String PROP_BATTERY_LEVEL_TEST = "sys.battery_level_test";
    
    private class BatteryTracker extends BroadcastReceiver {
        public static final int UNKNOWN_LEVEL = -1;

        // current battery status
        int level = UNKNOWN_LEVEL;
        String percentStr;
        int plugType;
        boolean plugged;
        int health;
        int status;
        String technology;
        int voltage;
        int temperature;
        boolean testmode = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                mLevel = (int)(100f
                        * intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                        / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
                mPluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;

                final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                mCharged = status == BatteryManager.BATTERY_STATUS_FULL;
                mCharging = mCharged || status == BatteryManager.BATTERY_STATUS_CHARGING;
                Log.i(TAG,"1.battery level = " + mLevel + ",status = " + status+" mCharging = "+mCharging);

                updateIconView();
            }
        }
    }

    BatteryTracker mTracker = new BatteryTracker();

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(ACTION_LEVEL_TEST);
        final Intent sticky = getContext().registerReceiver(mTracker, filter);
        if (sticky != null) {
            // preload the battery level
            mTracker.onReceive(getContext(), sticky);
        }
        //mBatteryController.addStateChangedCallback(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        getContext().unregisterReceiver(mTracker);
        mBatteryController.removeStateChangedCallback(this);
    }

    public BatteryMeterViewDefinedNew(Context context) {
        this(context, null, 0);
    }

    public BatteryMeterViewDefinedNew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryMeterViewDefinedNew(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setDither(true);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWarningString = context.getString(R.string.battery_meter_very_low_overlay_symbol);

        //mMatrix=new Matrix(); 
        //mMatrix.postRotate(-180); 
        // 防止出现Immutable bitmap passed to Canvas constructor错误
        mChargeEmptyBm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stat_sys_battery_charging_0_prize).copy(Bitmap.Config.ARGB_8888, true);
        mChargingBm = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.stat_sys_battery_charging_prize)).getBitmap();
        //mBatteryLowBm = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.stat_sys_battery_low)).getBitmap();

        //add for status bar style. prize-linkh-20150902.
        if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
            mChargeEmptyBmBlack = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stat_sys_battery_charging_0_gray_prize).copy(Bitmap.Config.ARGB_8888, true);
            mChargingBmBlack = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.stat_sys_battery_charging_gray_prize)).getBitmap();            
            Log.d(TAG, "BatteryMeterViewDefinedNew(). this =" + this);
        }
        // for debug battery level conviently
        mInDebugBatteryLevelMode = SystemProperties.getBoolean(PROP_DEBUG_BATTERY_LEVEL, false);
        
        mWidth = mChargeEmptyBm.getWidth();
        mHeight= mChargeEmptyBm.getHeight();
        
        Typeface font = Typeface.create("sans-serif-condensed", Typeface.BOLD);
        mWarningTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWarningTextPaint.setColor(Color.RED);
        font = Typeface.create("sans-serif", Typeface.BOLD);
        mWarningTextPaint.setTypeface(font);
        mWarningTextPaint.setTextAlign(Paint.Align.CENTER);
        mWarningTextPaint.setTextSize(mWidth * 0.4f);
        mWarningTextHeight = -mWarningTextPaint.getFontMetrics().ascent;
    }

    public void setBatteryController(BatteryController batteryController) {
        mBatteryController = batteryController;
        mPowerSaveEnabled = mBatteryController.isPowerSave();
    }

    
    //add for status bar style. prize-linkh-20150902.
    public void onStatusBarStyleChanged(int style) {    
        Log.d(TAG, "onStatusBarStyleChanged(). style=" + style);
        if(mCurStatusBarStyle != style) {
            mCurStatusBarStyle = style;
            updateIconView();
        }
    }

    /*PRIZE-for keyguard inverse color-liufan-2018-02-28-start*/
    private boolean isInKeyguard = false;
    public void setInKeyguard(boolean inKeyguard){
        isInKeyguard = inKeyguard;
    }
    /*PRIZE-for keyguard inverse color-liufan-2018-02-28-end*/

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        // TODO: Use this callback instead of own broadcast receiver.
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        mPowerSaveEnabled = mBatteryController.isPowerSave();
        invalidate();
    }

    

    private boolean mDemoMode;
    private BatteryTracker mDemoTracker = new BatteryTracker();

    @Override
    public void dispatchDemoCommand(String command, Bundle args) {
        if (!mDemoMode && command.equals(COMMAND_ENTER)) {
            mDemoMode = true;
            mDemoTracker.level = mTracker.level;
            mDemoTracker.plugged = mTracker.plugged;
        } else if (mDemoMode && command.equals(COMMAND_EXIT)) {
            mDemoMode = false;
            postInvalidate();
        } else if (mDemoMode && command.equals(COMMAND_BATTERY)) {
           String level = args.getString("level");
           String plugged = args.getString("plugged");
           if (level != null) {
               mDemoTracker.level = Math.min(Math.max(Integer.parseInt(level), 0), 100);
           }
           if (plugged != null) {
               mDemoTracker.plugged = Boolean.parseBoolean(plugged);
           }
           postInvalidate();
        }
    }

    void recycleBitmapIfNecessary() {
        Drawable d = getDrawable();
        if(DEBUG)Log.d(TAG, "recycleBitmapIfNecessary(). drawable?  " +d);
        if(d != null && d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable)d;
            Bitmap bm = bd.getBitmap();
            if(bm != null && !bm.isRecycled() && bm.isMutable()) {
                //set this image view to null bitmap, and recycle the old bitmap.
                if(DEBUG)Log.d(TAG, "recycle bitmap -  " + bm);
                setImageDrawable(null);
                bm.recycle();
            }
        }
    }
    void updateIconView() {      
      if(DEBUG)Log.d(TAG, "updateIconView(). level=" + mLevel + ", mCharging=" + mCharging + ", style=" + mCurStatusBarStyle);
      
      if(mInDebugBatteryLevelMode) {
          int level = SystemProperties.getInt(PROP_BATTERY_LEVEL_TEST,  -1);
          if(level >= 0 && level <= 100) {
              Log.d(TAG, "updateIconView(): force to set battery level to " + level);
              mLevel = level;
          }
      }

      if( !mCharging && mLevel <= 5) {
            //setImageBitmap(Bitmap.createBitmap(bitmap3,0,0,mWidth, mHeight,mMatrix,true));
            
            recycleBitmapIfNecessary();
            setImageResource(R.drawable.stat_sys_battery_low_prize);
       }
       else if( !mCharging && mLevel < 100 || mLevel == 100){
          Bitmap sourceBm = mChargeEmptyBm;
          
          if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
                int color = Color.WHITE;
                if(mLevel > 15) {
                    /*PRIZE-for keyguard inverse color-liufan-2018-02-28-start*/
                    if(isInKeyguard){
                        color = mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_WHITE ? Color.WHITE : Color.BLACK;
                    } else {
                        color = PrizeStatusBarStyle.getInstance(getContext()).getColor(mCurStatusBarStyle);
                    }
                    /*PRIZE-for keyguard inverse color-liufan-2018-02-28-end*/
                } else {
                    color = Color.RED;
                }
                paint.setColor(color);

                if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_WHITE) {
                    sourceBm = mChargeEmptyBm;
                } else if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY) {
                    sourceBm = mChargeEmptyBmBlack;
                }
          } else {
              paint.setColor(mLevel>15 ? Color.WHITE : Color.RED);
          }
          
          //if(newBitmap != null && !newBitmap.isRecycled() ) newBitmap.recycle();
          
          if(DEBUG)Log.d(TAG, "Not Charging or full:  source bitmap- " + sourceBm + ", isMutable? " + sourceBm.isMutable());
          Bitmap newBm = Bitmap.createBitmap(sourceBm); //the bitmap is mutable. So the system will create a new bitmap for us.
          if(DEBUG)Log.d(TAG, "Not Charging or full: create a new bitmap- " + newBm + ", isMutable? " + newBm.isMutable());

          mCanvas.setBitmap(newBm);
          //mCanvas.drawRect(((FULL-(mLevel > FULL ? FULL : mLevel))/100.0F)*(mWidth*0.85F-2*PADDING)+(mWidth*0.13F+PADDING), mWidth*0.05F+PADDING,mWidth*0.95F-PADDING, mHeight-mWidth*0.05F-PADDING, paint);  

          float left = mWidth*0.05F + PADDING;
          float top = mWidth*0.05F + PADDING;
          float right = mWidth - (((FULL-(mLevel > FULL ? FULL : mLevel))/100.0F)*(mWidth*0.85F-2*PADDING)+(mWidth*0.13F+PADDING));
          float bottom = mHeight-mWidth*0.05F-PADDING;
          mCanvas.drawRect(left, top , right, bottom, paint);

          mCanvas.save(Canvas.ALL_SAVE_FLAG);
          // 存储新合成的图片
          mCanvas.restore();

          Bitmap dstBm = Bitmap.createBitmap(newBm,0,0,mWidth, mHeight,null,true);
          if(DEBUG)Log.d(TAG, "Not Charging or full: create a new bitmap- " + dstBm + ", isMutable? " + dstBm.isMutable());
          
          recycleBitmapIfNecessary();
          setImageBitmap(dstBm); //before set a new bitmap, the old bitmap should be recycled ??          
          if(DEBUG)Log.d(TAG, "Not Charging or full: new bitmapDrawable -  " + getDrawable());
          if(DEBUG)Log.d(TAG, "Not Charging or full: recycle bitmap -  " + newBm);
          newBm.recycle();          
          mCanvas.setBitmap(null);
          
          //bitmap1.recycle();
          //if(newBitmap != null && !newBitmap.isRecycled())   newBitmap.recycle();
      } else if(mCharging && mLevel < 100){
        if(DEBUG)Log.d(TAG, "mChargingThread =" + mChargingThread);
        if(DEBUG)Log.d(TAG, "this =" + this);        

        if(mChargingThread == null) {
             mRequestQuiting = false;
             mChargingThread = new Thread(new Runnable(){

              public void run(){
                String name = Thread.currentThread().getName();
                Log.d(TAG, name +":  running...");
                int num = 0 ;
                boolean hasRequestQuit = false;
                while(!mRequestQuiting){                    
                    if(DEBUG)Log.d(TAG, name +":  update charging animation...");
                    if(mCharging && mLevel < 100) {
                       if(hasRequestQuit) {
                            mMainHandler.removeMessages(MSG_REQUEST_QUITING);
                            hasRequestQuit = false;
                       }
                       //canvas.drawRect(((4 -i)/4.0F)*(w-PADDING)+(w*0.1F+PADDING), PADDING,w-PADDING, h-PADDING, paint);
                       mMainHandler.removeMessages(MSG_UPDATE_CHARGING_ANIMATION);
                       Message msg = mMainHandler.obtainMessage(MSG_UPDATE_CHARGING_ANIMATION);
                       Bundle bundle = new Bundle();    
                       bundle.putInt("index",images[num]);  //往Bundle中存放数据  
                       msg.setData(bundle);//mes利用Bundle传递数据  
                       mMainHandler.sendMessage(msg);
                       num++;
                       if(num >= images.length){
                           num =0;
                       }
                   } else {
                        //When the charger isn't connecting to the phone, we request quiting this charging thread
                        // in a delay time.
                        if(!mCharging && !hasRequestQuit) {
                            hasRequestQuit = true;
                            mMainHandler.sendEmptyMessageDelayed(MSG_REQUEST_QUITING, QUIT_CHARGING_THREAD_DELAY);                            
                        }
                   }
                   
                   try  
                   {                    
                        Thread.sleep(1000);  
                   }  
                   catch (InterruptedException e)  
                   {  
                        Log.d(TAG, "e= "+e);
                   }
                }
             }
           });
           
           mChargingThread.start();
        }
         
       }
       //bitmap1.recycle();
    }
 
    class MyHandler extends Handler {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {

            case MSG_UPDATE_CHARGING_ANIMATION:
                
                if(!mCharging || mLevel >= 100) {
                    break;
                }
                
                int index = msg.getData().getInt("index",0);
                //if(DEBUG) Log.d(TAG, "MainHandler-->handleMessage-->thread id =" +     Thread.currentThread().getId()+" index = "+index);
                paint.setColor(0xFF48cb00);
                //canvas.drawRect(((4 -index)/4.0F)*(mWidth*0.85F-2*PADDING)+(mWidth*0.13F+PADDING), mWidth*0.05F+PADDING,mWidth*0.95F-PADDING, mHeight-mWidth*0.05F-PADDING, paint);                              
                //Bitmap bitmap2 = ((BitmapDrawable) getResources().getDrawable(R.drawable.stat_sys_battery_charging)).getBitmap();                
                //int w_2 = bitmap2.getWidth();
                //int h_2 = bitmap2.getHeight();            
                //canvas.drawBitmap(bitmap2, Math.abs(mWidth - w_2) / 2, Math.abs(mHeight - h_2) / 2, paint);

                Bitmap sourceBm = mChargeEmptyBm;
                Bitmap sourceChargingBm = mChargingBm;

                if(PrizeOption.PRIZE_STATUSBAR_INVERSE_COLOR) {
                    if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_WHITE) {
                        sourceBm = mChargeEmptyBm;
                        sourceChargingBm = mChargingBm;
                    } else if(mCurStatusBarStyle == StatusBarManager.STATUS_BAR_INVERSE_GRAY) {
                        sourceBm = mChargeEmptyBmBlack;
                        sourceChargingBm = mChargingBmBlack;
                    }
                }              
                if(DEBUG)Log.d(TAG, "Charging, source bitmap- " + sourceBm + ", isMutable? " + sourceBm.isMutable());
                Bitmap newBm = Bitmap.createBitmap(sourceBm); //the bitmap is mutable. So the system will create a new bitmap for us.
                if(DEBUG)Log.d(TAG, "Charging, create a bitmap- " + newBm + ", isMutable? " + newBm.isMutable());
                //if(DEBUG)Log.d(TAG, "Charging: 11 same bitmap ? " + newBm.sameAs(sourceBm));

                mCanvas.setBitmap(newBm);
                
                //mCanvas.drawRect(((4 -index)/4.0F)*(mWidth*0.85F-2*PADDING)+(mWidth*0.13F+PADDING), mWidth*0.05F+PADDING,mWidth*0.95F-PADDING, mHeight-mWidth*0.05F-PADDING, paint);                              

                float left = mWidth*0.05F + PADDING;
                float top = mWidth*0.05F + PADDING;
                float right = mWidth - (((4 -index)/4.0F)*(mWidth*0.85F-2*PADDING)+(mWidth*0.13F+PADDING));
                float bottom = mHeight-mWidth*0.05F-PADDING;                
                mCanvas.drawRect(left, top , right, bottom, paint);                

                //draw charging indicator icon.
                int w_2 = sourceChargingBm.getWidth();
                int h_2 = sourceChargingBm.getHeight();            
                mCanvas.drawBitmap(sourceChargingBm, Math.abs(mWidth - w_2) / 2, Math.abs(mHeight - h_2) / 2, paint);
                
                mCanvas.save(Canvas.ALL_SAVE_FLAG);
                // 存储新合成的图片
                mCanvas.restore();

                Bitmap dstBm = Bitmap.createBitmap(newBm,0,0,mWidth, mHeight,null,true);
                if(DEBUG)Log.d(TAG, "Charging, create a dst bitmap- " + dstBm + ", isMutable? " + dstBm.isMutable());
                
                recycleBitmapIfNecessary();
                setImageBitmap(dstBm); //before set a new bitmap, the old bitmap should be recycled ??                
                if(DEBUG)Log.d(TAG, "Charging: new bitmapDrawable -  " + getDrawable());
                if(DEBUG)Log.d(TAG, "Charging: recycle bitmap-  " + newBm);
                newBm.recycle();          
                mCanvas.setBitmap(null);
                
                //if(mCharging && mLevel  < 100)                
                //if(newBitmap != null && !newBitmap.isRecycled())   newBitmap.recycle();
                break;
          case MSG_REQUEST_QUITING:            
                Log.d(TAG, "MyHandler(): Msg.what=MSG_REQUEST_QUITING. Request quiting charging thread...");
                if(mChargingThread != null) {
                    mRequestQuiting = true;
                    mChargingThread = null;
                }                
        }
     }
   }
}
