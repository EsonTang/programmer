package com.android.systemui.stackdivider;


import com.android.systemui.stackdivider.PrizeDividerMenu.MyBtnListen;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.R;

import android.widget.LinearLayout;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.RemoteException;


import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.app.ActivityManager.StackId.DOCKED_STACK_ID;

public class PrizeDividerMenu implements OnTouchListener {
    private static final String TAG = "DividerMenu";
    
    public  static final int BTN_CLOSE = 1;
    public  static final int BTN_EXCHANGE = 2;
    private static final int AUTO_HIDE_DELAY_MILLIS = 2500;
    //private  FrameLayout mFloatLayout = null;
    private  LinearLayout mFloatLayout = null;
    private  WindowManager mWindowManager;
    private  WindowManager.LayoutParams wmParams = null;
    private  boolean mLandscape;
    private  boolean mViewIsAdd = false;
    private  Button btnClose, btnExchange;
    private  Context mContext;
    private float prizeDpi; // prize-add-split screen-liyongli-20180207
    private int mScreenWidth; // prize-add-split screen-liyongli-20180207
    private int mScreenHeight; // prize-add-split screen-liyongli-20180207
    
    private final WindowManagerProxy mWindowManagerProxy = WindowManagerProxy.getInstance();
    
    public  void CreateMenu(boolean isLandscape, Context context) {
    /*    int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        int windowViewWidth = 200;//context.getResources().getDimensionPixelOffset(R.dimen.float_menu_width);
        int windowViewHeight = 80;//context.getResources().getDimensionPixelOffset(R.dimen.float_menu_height);
        */
        prizeDpi =  context.getResources().getDisplayMetrics().density;
        DisplayMetrics dm =context.getResources().getDisplayMetrics();  
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        
        Log.v(TAG, "-- CreateMenu --"+mWindowManager);
        if( mWindowManager!=null )
            return;
        
        mContext = context;
        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        
        wmParams = new WindowManager.LayoutParams();
        wmParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; 
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.setTitle("prizeDivMenu");
        
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                                              //|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//                                              |WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
//                                              |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        
        wmParams.flags =  WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLandscape = isLandscape;
        
        /*
        if( mLandscape ){
          wmParams.x = 602;
          wmParams.width = 594; //1280-84-wmParams.x;
          wmParams.y = 0;
          wmParams.height = WRAP_CONTENT;
        }else{
          wmParams.y = 632-6;
          wmParams.height = 568+2;////1280-80-wmParams.y;
          wmParams.x = 0;
          wmParams.width = WRAP_CONTENT;// wrap_content;
        }
        */
        
        wmParams.x = 60;
        wmParams.y = 100;
        wmParams.height = WRAP_CONTENT;
        wmParams.width = WRAP_CONTENT;

        
        if( mFloatLayout==null ){
            LayoutInflater inflater = LayoutInflater.from(context);
            if(mLandscape){
                mFloatLayout = (LinearLayout) inflater.inflate(R.layout.divider_pop_menu_landscape,
                    null);
            }else{
            mFloatLayout = (LinearLayout) inflater.inflate(R.layout.divider_pop_menu,
                    null);
            }
        Log.v(TAG, "-- mFloatLayout --"+mFloatLayout);
            mFloatLayout.setOnTouchListener(this);

        //mFloatView = (ImageView)mFloatLayout.findViewById(R.id.borderView);
        }
        
        MyBtnListen btnListen = new MyBtnListen();
        //btnFolderUp = (Button) findViewById(R.id.btn_leftTop);
        btnClose = (Button) mFloatLayout.findViewById(R.id.split_close);
        btnClose.setOnClickListener(btnListen);
        
        btnExchange = (Button) mFloatLayout.findViewById(R.id.split_change);
        btnExchange.setOnClickListener(btnListen);       
    }
    

    class MyBtnListen implements Button.OnClickListener
    {
        public void onClick(View v)
        {
            switch(v.getId())
            {
            case R.id.split_close:
                Log.v(TAG, "-- button close --");
                mFloatLayout.setVisibility(View.INVISIBLE);
                notifyBtnClickListener(BTN_CLOSE);
                break;
         
            case R.id.split_change:
                Log.v(TAG, "-- button Exchange --");
                mFloatLayout.setVisibility(View.INVISIBLE);
                notifyBtnClickListener(BTN_EXCHANGE);
                break;
            }
        }
    }
    
    //----------------------------------------------
    public interface OnBtnClickListener {
        void onBtnClick(int btnId);
    }
    private OnBtnClickListener mOnBtnClickListener;
    public void setOnBtnClickListener(OnBtnClickListener onBtnClickListener) {
        mOnBtnClickListener = onBtnClickListener;
    }
    private void notifyBtnClickListener(int btnId) {
        if (mOnBtnClickListener != null) {
            mOnBtnClickListener.onBtnClick( btnId);
        }
    }
    /*
    PrizeDividerMenu.setOnBtnClickListener(
            new PrizeDividerMenu.OnBtnClickListener() {
        @Override
        public void onBtnClick(int btnId) {
            Log.d(TAG, "--- lyl  onBtnClick  " + btnId );
        }
       });
       */
    //----------------------------------------------
    
    
    public  void AddMenuView(){
        if (!Settings.canDrawOverlays(mContext)) {
            Log.e(TAG, "-- AddMenuView --add window permission denied");
            return;
        }
        Log.v(TAG, "-- AddMenuView --"+mViewIsAdd);
        if(!mViewIsAdd){
        mViewIsAdd = true;
        mWindowManager.addView(mFloatLayout, wmParams);
        }
    }
    
    public  void RemoveMenuView(){
        Log.v(TAG, "-- RemoveMenuView --"+mViewIsAdd);
        if(mViewIsAdd){
        mViewIsAdd = false;
        mWindowManager.removeView(mFloatLayout);
        }
    }

    public void ToggleShowMenuView( Rect dockedRect, boolean isLandscape, boolean dockStackIsFocus) {
        if (!Settings.canDrawOverlays(mContext)) {
            Log.e(TAG, "-- ShowMenuView --add window permission denied");
            return;
        }
        if (mFloatLayout.getVisibility() == View.VISIBLE) {
            HideMenuView();
        } else {
            //res_prize/layout/divider_pop_menu.xml
            int gap = (int)( (10+10)*prizeDpi );//40;   layout_marginRight="10dip" + layout_marginLeft="10dip"
            int btnH = (int)( 40*prizeDpi );//80; //the button hi  layout_height="40dp"
            int centerH = (int)( 2*prizeDpi );//6; //the center line HI  PrizeDockedDividerBackground  layout_height">2dp
            int preX, preY, lcdWH, posXYmid;            
            if( !isLandscape ){
                lcdWH = mScreenWidth;
            }else{
                lcdWH = mScreenHeight;
            }
            int startXY = (int)( lcdWH/2 - (40+10)*prizeDpi ); //260; 
            posXYmid = (int)( (40+10)*prizeDpi );//100   layout_width="40dp" + layout_marginLeft="10dip"
            
            //2017/12/22   add fix  the menu position is error
            IActivityManager mIam;
            mIam = ActivityManagerNative.getDefault();
            try {
                ActivityManager.StackInfo stackInfo = mIam.getStackInfo(DOCKED_STACK_ID);
                if (stackInfo != null) {
                    dockedRect.set(stackInfo.bounds);
                    Log.d(TAG, "-- ShowMenuView --"+dockedRect);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
            }//2017/12/22
      

            if( !isLandscape ){// |
                //int screenWidth = mWindowManager.getDefaultDisplay().getWidth();    
                //wmParams.x = (screenWidth - mFloatLayout.getWidth()) / 2;
                wmParams.x = startXY;
                if( dockStackIsFocus ){
                    wmParams.y = dockedRect.bottom - gap - btnH;
                    //preY =  gap + btnH;
                    preY =  btnH;
                }else{
                    wmParams.y = dockedRect.bottom + centerH + gap;
                    //preY = - gap;
                    preY = 0;
                }
                preX = posXYmid;
            }else{// --
                //int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
                //wmParams.y = (screenHeight - mFloatLayout.getHeight()) / 2;
                
                wmParams.y = startXY;
                if( dockStackIsFocus ){
                    wmParams.x = dockedRect.right - gap - btnH;
                    //preX =  gap + btnH;
                    preX =  btnH;
                }else{
                    wmParams.x = dockedRect.right + centerH + gap;
                    //preX = - gap;
                    preX = 0;
                }
                preY = posXYmid;
            }
            Log.v(TAG, "-- ToggleShowMenuView --"+wmParams.x + "," +wmParams.y);
            mWindowManager.updateViewLayout(mFloatLayout, wmParams);
            ShowMenuView(preX, preY);
        }
    }
        
    public  void ShowMenuView(int preX, int preY){
        Log.v(TAG, "-- ShowMenuView --" + mFloatLayout.getWidth() +" "+mFloatLayout.getHeight());
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
        mFloatLayout.setVisibility(View.VISIBLE);

        //for anim  start/end  position
        //mFloatLayout.setPivotX(mFloatLayout.getWidth() / 2);
        //mFloatLayout.setPivotY(mFloatLayout.getHeight()/2);
        mFloatLayout.setPivotX(preX);
        mFloatLayout.setPivotY(preY);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatLayout, "scaleX", 0.1f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatLayout, "scaleY", 0.1f,1f);
        ObjectAnimator aplpha = ObjectAnimator.ofFloat(mFloatLayout, "alpha", 0.2f,1f);
        AnimatorSet an = new AnimatorSet();
        an.play(scaleX);
        an.play(scaleY);
        an.play(aplpha);
        an.setDuration(150);
        an.setInterpolator(new AccelerateInterpolator(1.5f)); //DecelerateInterpolator
        an.start();
    }
    
    public  void HideMenuView(){
        Log.v(TAG, "-- HideMenuView --");
        mHideHandler.removeCallbacks(mHideRunnable);
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFloatLayout, "scaleX", 1f, 0.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFloatLayout, "scaleY", 1f, 0.1f);
        ObjectAnimator aplpha = ObjectAnimator.ofFloat(mFloatLayout, "alpha", 1f, 0.2f);
        AnimatorSet an = new AnimatorSet();
        an.play(scaleX);
        an.play(scaleY);
        an.play(aplpha);
        an.setDuration(150);
        an.setInterpolator(new DecelerateInterpolator());
        an.start();
        
        an.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                ;//setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mFloatLayout.setVisibility(View.INVISIBLE);
            }
        });

    }
    
    public  void HideMenuViewNoAnim(){
        Log.v(TAG, "-- HideMenuViewNoAnim --");
        mHideHandler.removeCallbacks(mHideRunnable);
        mFloatLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        // TODO Auto-generated method stub
        //convertToScreenCoordinates(event);
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        int x = (int) event.getX();
        int y = (int) event.getY();
        
        
        if( action==MotionEvent.ACTION_DOWN ){
            Log.v(TAG, "-- onTouch --"+x+", "+y);
            mWindowManagerProxy.setResizing(false);
            HideMenuView();
        }

        return false;
    }
    
    //--------- hide ----------------------
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mWindowManagerProxy.setResizing(false);
            HideMenuView();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    //---------------------------------------------------------
    
    
    
}
