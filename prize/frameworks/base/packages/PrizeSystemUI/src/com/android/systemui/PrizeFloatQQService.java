package com.android.systemui;

import com.android.systemui.R;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.Service;  
import android.content.Context;
import android.content.Intent;  
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;  
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Point;
import android.os.Handler;  
import android.os.IBinder;  
import android.util.Log;  
import android.view.Gravity;  
import android.view.LayoutInflater;  
import android.view.MotionEvent;  
import android.view.View;  
import android.view.WindowManager;  
import android.view.View.OnClickListener;  
import android.view.View.OnTouchListener;  
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManager.LayoutParams;  
import android.widget.Button;  
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;  
import android.widget.TextView;
import android.widget.Toast;  
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.systemui.stackdivider.WindowManagerProxy;
import com.android.systemui.statusbar.phone.NavigationBarGestureHelper;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.RecentsImpl;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.view.Display;
import android.content.res.Configuration;

import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.component.PrizeOpenPendingIntentEvent;
import com.android.systemui.recents.events.activity.QQdockedTopTaskEvent;

public class PrizeFloatQQService extends Service  
{
	private static final String TAG = "FloatQQServiceSysUi";  
	private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
	
	public static final String ACTION_FLOATQQSERVICE = "action.com.prize.floatqqservice";
	public static final String KEY_MSG_HOST = "msg_host";
	public static final String KEY_MSG_NUM = "msg_num";
	public static final String HOST_QQ = "TencentQQ";
	public static final String HOST_MM = "TencentMM";
	public static final String PKG_QQ = "com.tencent.mobileqq";
	public static final String PKG_MM = "com.tencent.mm";
	
	private static final int NULL_VIEW = 0;
	private static final int QQ_VIEW = 1;
	private static final int MM_VIEW = 2;
	private static final int MUL_VIEW_TYPE = 64;//QQ MM together send msg
	
	private int mViewHostType = NULL_VIEW; //QQ_VIEW  MM_VIEW
	
	private boolean mViewIsCreate = false;
	private boolean mViewIsAdd = false;

	
    WindowManager.LayoutParams wmParams;  
    WindowManager mWindowManager;  
    
    FrameLayout mFloatLayout; 
    ImageView mFloatView;
    TextView  mText;
    int posx, posy;
    int mStartX, mStartY;
    int mLastX, mLastY;
    boolean mMoved;
    
    Context mContext;
    
    String msgHost;
      
    
      
    @Override  
    public void onCreate()   
    {  
        // TODO Auto-generated method stub  
        super.onCreate();  
        Log.i(TAG, "oncreat");  
        mContext = this;
        //ORIENTATION_PORTRAIT
        posx = 400;
        posy = 900;//200;
        if( getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ){
            posx = 1000;
            posy = 10;
        }
        
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE); 
        //EventBus.getDefault().register(this);
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(intent != null)
		{
			String action = intent.getAction();
			Log.i(TAG,"onStartCommand action:"+action);
			int type = NULL_VIEW;
			int msgNum = 1;
			
			if(ACTION_FLOATQQSERVICE.equals(action))
			{
				msgNum = intent.getIntExtra(KEY_MSG_NUM,1);
				msgHost = intent.getStringExtra(KEY_MSG_HOST);
				
				
				if( HOST_QQ.equals(msgHost)){
					type = QQ_VIEW;
				}else if( HOST_MM.equals(msgHost)){
					type = MM_VIEW;
				}else{
					type = QQ_VIEW;
				}
			}
			
			if( type != NULL_VIEW ){
				if(!mViewIsCreate){
					createFloatView(type, msgNum);
					updateView();
				}
				
				if( mViewHostType==NULL_VIEW ){
					mViewHostType = type;
					updateImgView(type);
					updateTextView(msgNum);
				}else{
					if( mViewHostType != type){
						mViewHostType=MUL_VIEW_TYPE;
						updateImgView(type);//change icon  ----- liyongli  need modify
					}else{
						updateTextView(msgNum); //change msg number
					}
				}
				Log.i(TAG,"onStartCommand mViewHostType :"+mViewHostType);
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
		}
		return super.onStartCommand(intent, flags, startId);	
    }
    
  
    @Override  
    public IBinder onBind(Intent intent)  
    {  
        // TODO Auto-generated method stub  
        return null;  
    }  
  
    
    //------------------------------------------------------------------
    private void convertToScreenCoordinates(MotionEvent event) {
        event.setLocation(event.getRawX(), event.getRawY());
    }

    private class imgOnTouchListener implements OnTouchListener{  
    	@Override  
        public boolean onTouch(View v, MotionEvent event2) 
        {
        	MotionEvent event = event2;
        	 convertToScreenCoordinates(event);
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    mLastX = mStartX;
                    mLastY = mStartY;
                    mHideHandler.removeCallbacks(mHideRunnable);
                    break;
                case MotionEvent.ACTION_MOVE:
                	int x = (int) event.getX();
                    int y = (int) event.getY();
                    if(!mMoved){
                    	if(Math.abs(y - mStartY) > 4 || Math.abs(y - mStartY) >4){
                    		mMoved=true;
                    	}
                    }
                    if(mMoved){
                    wmParams.x += x-mLastX;
                    wmParams.y += y-mLastY;
                    
                    mLastX = x;
                    mLastY = y;
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                    }
                	break;
                	
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                	if(!mMoved){
                		Log.i(TAG, "do   onClick  sth ");
                		toSplitScreenMode();
                	}
                	mMoved = false;
                	delayedHide(AUTO_HIDE_DELAY_MILLIS);
                	break;
            }
            return false;  //must false, or  OnClickListenner failed
        }
    }
    private class imgOnClickListener implements OnClickListener{
    	 @Override  
         public void onClick(View v)   
         {
             // TODO Auto-generated method stub  
    		 mHideHandler.removeCallbacks(mHideRunnable);
    		 Log.i(TAG, " onClick ");
             //Toast.makeText(PrizeFloatQQService.this, "onClick", Toast.LENGTH_SHORT).show();  
         }
    }
    
        protected void toSplitScreenMode() {
        	int metricsDockAction = MetricsEvent.ACTION_WINDOW_DOCK_LONGPRESS;

        int dockSide = WindowManagerProxy.getInstance().getDockSide();
        if (dockSide == WindowManager.DOCKED_INVALID) {
            int qqmmType = mViewHostType;
            //delayedHide(50);
            
            Log.d(TAG, "QQdockedTopTaskEvent --- btn click" ); 
            //mRecents.dockTopTask(NavigationBarGestureHelper.DRAG_MODE_NONE,
           //         ActivityManager.DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT, null, metricsDockAction);
           SystemServicesProxy ssp = SystemServicesProxy.getInstance(mContext);
           ActivityManager.RunningTaskInfo runningTask = ssp.getRunningTask();
           int dragMode = -1;
           int stackCreateMode = ActivityManager.DOCKED_STACK_CREATE_MODE_TOP_OR_LEFT;
           
           Point realSize = new Point();
           mContext.getSystemService(DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY)
                    .getRealSize(realSize);
           Rect initialBounds = new Rect(0, 0, realSize.x, realSize.y);
          

            
           RecentsImpl mImpl = new RecentsImpl(mContext);
/*           
           //debug , see if show black screen; open recents and entry split screen
           //mImpl.dockTopTask(runningTask.id, dragMode, stackCreateMode, initialBounds );
           if(false){
            //mImpl.qqDockTopTask(runningTask.id, dragMode, stackCreateMode, initialBounds, qqmmType);
            mImpl.dockTopTask__dbg(runningTask.id, dragMode, stackCreateMode, initialBounds );
            //ssp.moveTaskToDockedStack(runningTask.id, stackCreateMode, initialBounds);
            
            //open MM
            EventBus.getDefault().send(new PrizeOpenPendingIntentEvent( mViewHostType));
            
            //EventBus.getDefault().send(new QQdockedTopTaskEvent(dragMode, initialBounds, qqmmType));

	mHideHandler.removeCallbacks(mHideRunnable);
           removeView();
            return;
        }
*/

           //ssp.moveTaskToDockedStack(runningTask.id,  stackCreateMode, initialBounds );   // use  qqDockTopTask replace this
           mImpl.qqDockTopTask(runningTask.id, dragMode, stackCreateMode, initialBounds, qqmmType);
           
           
           //for open  1--qq,  2--mm,  move to DividerView onBusEvent(QQdockedTopTaskEvent 
           //EventBus.getDefault().send(new PrizeOpenPendingIntentEvent(mViewHostType));
           
            //resize stack
           EventBus.getDefault().send(new QQdockedTopTaskEvent(dragMode, initialBounds, qqmmType));          
           
           
           //end dismiss ICON
           mHideHandler.removeCallbacks(mHideRunnable);
           removeView();
/*           
          String strPkg, strCls;
          strPkg = "com.tencent.mobileqq";
		strCls = "com.tencent.mobileqq.activity.SplashActivity";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(strPkg, strCls);
		startActivity(intent);
*/		
        }
        
    }
    
    private void createFloatView(int type, int msgNum)  
    {
        //mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);  
        //Log.i(TAG, "mWindowManager--->" + mWindowManager);
        
        wmParams = new WindowManager.LayoutParams();  
          
        //wmParams.type = LayoutParams.TYPE_PHONE;
        wmParams.type=WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; 
        
        wmParams.format = PixelFormat.RGBA_8888; 
        
        wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;        

        wmParams.gravity = Gravity.LEFT | Gravity.TOP;         

        wmParams.x = posx;  
        wmParams.y = posy;  
  

        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;  
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  
  

     
        LayoutInflater inflater = LayoutInflater.from(getApplication());  

        mFloatLayout = (FrameLayout) inflater.inflate(R.layout.prize_qqfloat, null);

        mFloatView = (ImageView)mFloatLayout.findViewById(R.id.imageView);
        mText = (TextView)mFloatLayout.findViewById(R.id.numtext);
//        if(msgNum<2){
//        	mText.setVisibility(View.INVISIBLE);
//        }
        
        setImgView(type);

        mWindowManager.addView(mFloatLayout, wmParams); 
 
        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,  
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec  
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));  

        mViewIsCreate = true;
        mViewIsAdd = true;
    }
    
    private void updateView(){
    	//mWindowManager.addView(mFloatLayout, wmParams);

        mFloatView.setOnTouchListener(new imgOnTouchListener() );
        mFloatView.setOnClickListener(new imgOnClickListener() );
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
        
    }
    
    
	private void setImgView( int type) {
		Drawable able = null;
		if(type==QQ_VIEW){
			able = getIcon(PKG_QQ);
		}else if(type==MM_VIEW){
			able = getIcon(PKG_MM);
		}
		
		if (able != null) {
			mFloatView.setImageDrawable(able);
		}
	}
	
	
	private void updateImgView( int type )
	{
		setImgView(type);
//		mFloatView.setImageBitmap(Bitmap bmp);
//		mWindowManager.updateViewLayout(mFloatLayout, wmParams);
	}
	private void updateTextView(int num)
	{
		String str=""+num;
		//mText.setVisibility(View.VISIBLE);
		mText.setText(str);
		if(!mViewIsAdd){
			mWindowManager.addView(mFloatLayout, wmParams);
			mViewIsAdd = true;
		}
	}
	
	
	private void removeView(){
		mViewHostType = NULL_VIEW;
		if(mViewIsAdd){
		mViewIsAdd = false;
		mWindowManager.removeView(mFloatLayout);
		}
	}
	
    //--------- hide ----------------------
    Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			removeView();
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
	

      
    @Override  
    public void onDestroy()   
    {  
        // TODO Auto-generated method stub  
        super.onDestroy();  
        removeView();
        //EventBus.getDefault().unregister(this);
    }  
     

	private Drawable getIcon( String pkgIn ) {
		String pkg = "com.tencent.mobileqq";
		//pkg = "com.tencent.mm";
		pkg = pkgIn;

		// TODO Auto-generated method stub
		Drawable drawable = null;
		PackageManager mPackageManager = getPackageManager();
		PackageInfo mPackageInfo;
		try {
			mPackageInfo = mPackageManager.getPackageInfo(pkg, 0);
			// mPackageInfo.applicationInfo.loadIcon(getPackageManager());

			ApplicationInfo appInfo = mPackageInfo.applicationInfo;
			drawable = appInfo.loadIcon(mPackageManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return drawable;
	}
	
}  