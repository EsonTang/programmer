package com.prize.flash;

import java.lang.reflect.Method;

import com.prize.flash.FlashSwitch.SwitchLister;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class FlashLightMainActivity extends Activity {

	private static final String TAG = "FlashLightMainActivity";

	private FrameLayout flash_bg;

	private TextView flash_button;

	private boolean onPause;

	private FlashLightObserver mFlashLightObserver;
	
	private BatteryBroadcastReciver reciver;
	
	private IntentFilter intentFilter;
	
	private int mCurrentLevel = 50;
	
	private FlashSwitch mFlashSwitch;
	
	private SwitchLister mSwitchLister;

	private FrameLayout.LayoutParams mFlashSwitchLp;
	/*prize-xuchunming-20160217-bugid:12135-start*/
	private boolean isLayoutChange = false;
	/*prize-xuchunming-20160217-bugid:12135-end*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.flashlight);
		flash_bg = (FrameLayout) findViewById(R.id.flash_bg);
		
		mFlashSwitch = (FlashSwitch)findViewById(R.id.flash_switch);
		
		mFlashSwitchLp = (LayoutParams) mFlashSwitch.getLayoutParams();
		
		flash_bg.setEnabled(false);
		
		mSwitchLister = new SwitchLister() {
			
			public void switchOn() {
				
				// TODO Auto-generated method stub
				setOpneStatus();
			}
			
			public void switchOff() {
				
				// TODO Auto-generated method stub
				setCloseStatus();

            }
		};
		
		mFlashSwitch.setSwitchLister(mSwitchLister);

		mFlashLightObserver = new FlashLightObserver(new Handler());
		mFlashLightObserver.startObserving();
		
		reciver = new BatteryBroadcastReciver(); 
		//创建一个过滤器 
		intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED); 
		
		flash_bg.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			public void onGlobalLayout() {
				
				// TODO Auto-generated method stub
				/*prize-xuchunming-20160217-bugid:12135-start*/
				if(isLayoutChange == true){
					isLayoutChange = false;
					return ;
				}
				isLayoutChange = true;
				/*prize-xuchunming-20160217-bugid:12135-end*/
				WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
				int heightDiff = flash_bg.getHeight() - wm.getDefaultDisplay().getHeight();
			    if(heightDiff > 0){
			       //大小超过0时，一般为隐藏虚拟键盘事件
			    	if(mFlashSwitch.isSwitch()){
			    		flash_bg.setBackground(getResources().getDrawable(R.drawable.flashligh_open));
			    	}else{
			    		flash_bg.setBackground(getResources().getDrawable(R.drawable.flashligh_close));
			    	}
			    	mFlashSwitchLp.bottomMargin = (int) getResources().getDimension(R.dimen.flash_button_bottommargin);
			    }else{
			        //大小小于等于0时，为显示虚拟键盘或虚拟键盘隐藏
			    	if(mFlashSwitch.isSwitch()){
			    		flash_bg.setBackground(getResources().getDrawable(R.drawable.flashligh_open_nav));
			    	}else{
			    		flash_bg.setBackground(getResources().getDrawable(R.drawable.flashligh_close_nav));
			    	}
			    	mFlashSwitchLp.bottomMargin = (int) getResources().getDimension(R.dimen.flash_button_bottommargin_nav);
			    }
			    
			    mFlashSwitch.setLayoutParams(mFlashSwitchLp);
			}
		});
		
		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(  
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));  
 
	}

	
	@Override
	protected void onResume() {

		// TODO Auto-generated method stub
		onPause = false;
		mFlashSwitch.setSwitchStatue("on");
		setOpneStatus();
		registerReceiver(reciver, intentFilter); 
		super.onResume();

	}

	public void setCloseStatus() {
		// TODO Auto-generated method stub
		flash_bg.setEnabled(false);
		closeFlash();
	}

	public void setOpneStatus() {
		// TODO Auto-generated method stub
		if(reciver.isLowPower()){
			flash_bg.setEnabled(false);
			mFlashSwitch.turnOff();
		}else{
			flash_bg.setEnabled(true);
			/** sysflash、other、字段在systemui中有observe监听*/
			startFlash();
		}
		
	}

	@Override
	protected void onPause() {

		// TODO Auto-generated method stub
		onPause = true;
		unregisterReceiver(reciver); 
		super.onPause();

	}

	@Override
	protected void onStop() {

		// TODO Auto-generated method stub
		//relaseFlashLight();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		relaseFlashLight();
		mFlashLightObserver.stopObserving();
		unregisterReceiver(mHomeKeyEventReceiver); 

		super.onDestroy();
	}

	private class FlashLightObserver extends ContentObserver {
        //prize-modify-by-zhongweilin
		//private final Uri FLASHLIGHT_MODE_URI = Uri.parse("content://com.android.flash/systemflashs/fromesystemui");
        private final Uri FLASHLIGHT_MODE_URI = Settings.System.getUriFor(Settings.System.PRIZE_FLASH_STATUS);

		public FlashLightObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			Log.d("xucm", "onChange() onPause:"+onPause);
			if (!onPause) {
                //prize-modify-by-zhongweilin
				//int isSystemFlashOn = SystemProperties.getInt("persist.sys.prizeflash",0);
                int isSystemFlashOn = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, -1);
				Log.d("xucm", "isSystemFlashOn:"+isSystemFlashOn);
				try{
					mFlashSwitch.setSwitchLister(null);
					if (isSystemFlashOn == 1) { // 开
						mFlashSwitch.turnOn();
						flash_bg.setEnabled(true);

					} else {
						
						mFlashSwitch.turnOff();
						flash_bg.setEnabled(false);

					}
					mFlashSwitch.setSwitchLister(mSwitchLister);
				}catch(Exception e){
					mFlashSwitch.setSwitchLister(mSwitchLister);
				}
			}
		}

		public void startObserving() {
			final ContentResolver cr = getContentResolver();
			cr.unregisterContentObserver(this);
			cr.registerContentObserver(FLASHLIGHT_MODE_URI, false, this);

		}

		public void stopObserving() {
			final ContentResolver cr = getContentResolver();
			cr.unregisterContentObserver(this);
		}
	}
	
	private void startFlash() { 
        /*modify-by-zhongweilin
		ContentValues values = new ContentValues();  
        values.put("flashstatus","1"); 
		getContentResolver().update(FlashProvider.CONTENT_URI, values, null, null);
        */
        Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 1);
	} 
	
	private void closeFlash() {  
        /*modify-by-zhongweilin
		ContentValues values = new ContentValues();  
        values.put("flashstatus","0"); 
		getContentResolver().update(FlashProvider.CONTENT_URI, values, null, null);
        */
        Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 0);
       
	} 
	
	public void relaseFlashLight(){
        /*modify-by-zhongweilin
    	ContentValues values = new ContentValues();  
        values.put("flashstatus","2"); 
        getContentResolver().update(FlashProvider.CONTENT_URI, values, null, null);
        */
        Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_FLASH_STATUS, 2);
    }
	
	private static String oldMsg;  
    protected static Toast toast   = null;  
    private static long oneTime=0;  
    private static long twoTime=0;  
      
    public static void showToast(Context context, String s){      
        if(toast==null){   
            toast =Toast.makeText(context, s, Toast.LENGTH_SHORT);  
            toast.show();  
            oneTime=System.currentTimeMillis();  
        }else{  
            twoTime=System.currentTimeMillis();  
            if(s.equals(oldMsg)){  
                if(twoTime-oneTime>Toast.LENGTH_SHORT){  
                    toast.show();  
                }  
            }else{  
                oldMsg = s;  
                toast.setText(s);  
                toast.show();  
            }         
        }  
        oneTime=twoTime;  
    }  
      
      
    public static void showToast(Context context, int resId){     
        showToast(context, context.getString(resId));  
    }  
	
	public class BatteryBroadcastReciver extends BroadcastReceiver {
		private boolean isLowPower = false;

		@Override
		public void onReceive(Context context, Intent intent) {

			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				// 得到系统当前电量
				int level = intent.getIntExtra("level", 0);
				mCurrentLevel = level;
				// 当电量小于15%时触发
				if (level <=15) {
					isLowPower = true;
					//wangyunhe hide
					//Toast.makeText(context,
					//		context.getString(R.string.flashlowlightremind),
					//		Toast.LENGTH_SHORT).show();
					setCloseStatus();
				} else {
					isLowPower = false;
				}

			}
		}

		public boolean isLowPower() {
			return isLowPower;
		}
	}
	
	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {  
        String SYSTEM_REASON = "reason";  
        String SYSTEM_HOME_KEY = "homekey";  
        String SYSTEM_HOME_KEY_LONG = "recentapps";  
           
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {  
                String reason = intent.getStringExtra(SYSTEM_REASON);  
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {  
                     //表示按了home键,程序到了后台  
                	Log.d("xucm","press home");
                	relaseFlashLight();
					mFlashSwitch.turnOff();
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){  
                    //表示长按home键,显示最近使用的程序列表  
                	Log.d("xucm","long press home");
                	//prize-public-bug:12924-close flash-20160321-pengcancan
                	relaseFlashLight();
					mFlashSwitch.turnOff();
                }  
            }   
        }  
    };  
    
   
}
