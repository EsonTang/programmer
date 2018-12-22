package com.prize.runoldtest.receiver;

import com.prize.runoldtest.ManualTestActivity;
import com.prize.runoldtest.PrizeRunOldTestApplication;
import com.prize.runoldtest.RunAll12HourActivity;
import com.prize.runoldtest.RunAll4HoursActivity;
import com.prize.runoldtest.RunAll6HourActivity;
import com.prize.runoldtest.RunAllTestActivity;
import com.prize.runoldtest.RunInMainActivity;
import com.prize.runoldtest.SingleTestActivity;
import com.prize.runoldtest.ddr.DdrActivity;
import com.prize.runoldtest.ddr.DdrSingleActivity;
import com.prize.runoldtest.reboot.RebootActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.WindowManager;

public class PrizeOrderReceiver extends BroadcastReceiver {

	private Context mcontext;
	@Override
	public void onReceive(Context context, Intent intent) {
		mcontext=context;
		LogUtil.e("reboot start Activity.......");
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			PrizeRunOldTestApplication app = (PrizeRunOldTestApplication) context.getApplicationContext();
			boolean bReboot = app.getSharePref().getValue(Const.SHARE_PREF_REBOOT_SELECTE).equals("1");
			boolean mDdrReboot = app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST).equals("1");
			int ddr_circles = Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES));
			if(mDdrReboot){
				if(ddr_circles == -1){
					LogUtil.e("PrizeOrderReceiver mDdrReboot is true, Start DdrSingleActivity");
					Intent intentFactory = new Intent(context, DdrSingleActivity.class);
					intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentFactory);
					return;
				}
				LogUtil.e("PrizeOrderReceiver mDdrReboot is true");
				Intent mintent = new Intent(context, RunInMainActivity.class);
				mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mcontext.startActivity(mintent);
				boolean isreboot =false;
				boolean isddrAct=true;
				ActNewActivity(isreboot,isddrAct);
				
				Intent intentAtaInfo = new Intent(context, DdrActivity.class);
				intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentAtaInfo);
			}else if (bReboot) {
				LogUtil.e("PrizeOrderReceiver  mDdrReboot："+mDdrReboot);
				Intent intentAtaInfo = new Intent(context, RebootActivity.class);
				intentAtaInfo.putExtra("reboot", true);
				intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentAtaInfo);
			}else{
				int  i=Integer.parseInt(app.getSharePref().getValue(Const.SHARE_PREF_REBOOT_TOTALTIMES));
				if(i>0){
					DataUtil.FlagRebootFinish=true;
					app.getSharePref().putValue(Const.SHARE_PREF_REBOOT_TOTALTIMES, "0");
					Intent mintent = new Intent(context, RunInMainActivity.class);
					mintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mcontext.startActivity(mintent);
					boolean isreboot =true;//this is mean is reboot test and  it is finish
					ActNewActivity(isreboot,false);
				}
			}
		} else {
			String number = intent.getExtras().getString("input");
			if (number.equals("*#8822#")) {
				Intent intentFactory = new Intent(context, RunInMainActivity.class);
				intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentFactory);
			} else if(number.equals("*#8824#")){
				Intent intentFactory = new Intent(context, DdrSingleActivity.class);
				intentFactory.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intentFactory.putExtra(Const.EXTRA_MESSAGE, 1);
				context.startActivity(intentFactory);
			}
		}
	}
	private  void ActNewActivity(boolean isreboot ,boolean isddract){
		
		LogToFile. writeToFile(LogToFile.VERBOSE, "", "RebootTest final fiish act acitivy"+"\n");
		SharedPreferences share=mcontext.getSharedPreferences("activityname", Context.MODE_PRIVATE);
		int activityname=share.getInt("activitynamenumber",0);
		Editor editor =share.edit();
		editor.putInt("activitynamenumber", 0);		
		editor.commit();
		LogToFile. writeToFile(LogToFile.VERBOSE, "", ""+activityname+"\n");
		LogUtil.e("PrizeOrderReceiver  ActNewActivity activityname：  "+activityname);
		Intent intentAtaInfo;
		Intent thisintent;
		/*mcontext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	               | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	               | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  */
		
		
		/*KeyguardManager keyguardManager = (KeyguardManager)mcontext
                .getSystemService(mcontext.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        
       keyguardLock.reenableKeyguard();
       keyguardLock.disableKeyguard();*/
		//intentAtaInfo.putExtra("reboot", true);
		
		switch(activityname){
		case 0:
			break;
		case DataUtil.AUTOTESTACTIVITY:
			SharedPreferences sharedPreferences =mcontext.getSharedPreferences("manulddrtime",Context.MODE_PRIVATE); 
	  		int ddrtesttime=sharedPreferences.getInt("manulddrtimes", 0);
	  		if(ddrtesttime>0){
	  			
	  			editor.putInt("activitynamenumber", 1);		
	  			editor.commit();
	  		}
			
			DataUtil.FlagRebootFinish=true;
			DataUtil.isManualTst=true;
			LogUtil.e("PrizeOrderReceiver  Switch AUTOTESTACTIVITY：  ");
			intentAtaInfo = new Intent(mcontext, ManualTestActivity.class);
			intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intentAtaInfo.putExtra(Const.REACT_ACTIVITY, 1);
			
			mcontext.startActivity(intentAtaInfo);
			break;
		case DataUtil.SIXHOURSTESTACTIVIT:
			DataUtil.FlagRebootFinish=true;
			 DataUtil.isSixTest=true;
			LogUtil.e("PrizeOrderReceiver  Switch SIXHOURSTESTACTIVIT：  ");
			thisintent=new Intent(mcontext, RunAllTestActivity.class);
			thisintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mcontext.startActivity(thisintent);
			intentAtaInfo=new  Intent(mcontext, RunAll6HourActivity.class);
			intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mcontext.startActivity(intentAtaInfo);
			break;
		case DataUtil.TELVEHOURSTESTACTIVITY:
			DataUtil.FlagRebootFinish=true;
			DataUtil. isTwlfTest=true;
			LogUtil.e("PrizeOrderReceiver  Switch TELVEHOURSTESTACTIVITY：  ");
			thisintent=new Intent(mcontext, RunAllTestActivity.class);
			thisintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mcontext.startActivity(thisintent);
			intentAtaInfo=new  Intent(mcontext, RunAll12HourActivity.class);
			intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(isddract){
				intentAtaInfo.putExtra("isddract", true);
			}
			mcontext.startActivity(intentAtaInfo);
			break;
		case DataUtil.SINGLEACTIVITY:
			LogUtil.e("PrizeOrderReceiver  Switch SINGLEACTIVITY：  ");
			intentAtaInfo=new  Intent(mcontext, SingleTestActivity.class);
			intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if(isreboot){
				intentAtaInfo.putExtra(Const.SIGLE_REBOOTACTIVITY_FINISH, isreboot);
			}
			mcontext.startActivity(intentAtaInfo);
			break;
		case DataUtil.FOURTESTACTIVITY:
			DataUtil.FlagRebootFinish=true;
			 DataUtil.isfourTest=true;
			LogUtil.e("PrizeOrderReceiver  Switch FOURHOURSTESTACTIVIT：  ");
			thisintent=new Intent(mcontext, RunAllTestActivity.class);
			thisintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mcontext.startActivity(thisintent);
			intentAtaInfo=new  Intent(mcontext, RunAll4HoursActivity.class);
			intentAtaInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mcontext.startActivity(intentAtaInfo);
			
			break;
		default:
			
			break;
		}
	}
}
