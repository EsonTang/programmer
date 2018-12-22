package com.prize.runoldtest;

import com.prize.runoldtest.camera.CameraTestActivity;
import com.prize.runoldtest.cpu.CpuTestActivity;
import com.prize.runoldtest.ddr.DdrActivity;
import com.prize.runoldtest.emmc.EmmcActivity;
import com.prize.runoldtest.lcd.LcdActivity;
import com.prize.runoldtest.reboot.RebootActivity;
import com.prize.runoldtest.sleeprsm.SlpRsmActivity;
import com.prize.runoldtest.test3d.Test3DActivity;
import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.OldTestResult;

import com.prize.runoldtest.video.VideoActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

public class RunAll4HoursActivity extends Activity{
	 private String TAG="4HoursTest";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.activity_run_all4_hour);
	        DataUtil.FlagDdr=true;
	        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "RunAll4HourActivity  oncreat:"+"\n");
	        DataUtil.addDestoryActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
	     //   SysApplication.getInstance().addActivity(this);
	        
	        SharedPreferences sharedPreferences =getSharedPreferences("activityname", ManualTestActivity.MODE_PRIVATE); //私有数据
			Editor editor = sharedPreferences.edit();//获取编辑器
			editor.putInt("activitynamenumber", DataUtil.FOURTESTACTIVITY);		
			editor.commit();//提交修改	
	}

	 @Override
		public void onConfigurationChanged(Configuration newConfig) {
			// TODO Auto-generated method stub
			super.onConfigurationChanged(newConfig);
			LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "4HoursTest onConfigurationChanged.."+"\n");
		}
	
	
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		DataUtil.resetFlag();
		finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DataUtil.resetFlag();
		 DataUtil.isfourTest=false;
		 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "6HoursTest Ondestroy");
		// SysApplication.getInstance().deleteActivity(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub

        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	               | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	               | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagVideo is true:"+(DataUtil.FlagVideo==true)+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagDdr:"+DataUtil.FlagDdr+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagRebootFinish:"+DataUtil.FlagRebootFinish+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagsixDdrFinalFinish:"+DataUtil.FlagsixDdrFinalFinish+"\n");
        	if(DataUtil.FlagCpu){
        		SharedPreferences sharedPreferences =getSharedPreferences("fourtesttime", RunAll4HoursActivity.MODE_PRIVATE); 
				int times=sharedPreferences.getInt("fourtesttimes", 0);
				Editor editor = sharedPreferences.edit();
			  	editor.putInt("fourtesttimes", times-1);		
			  	editor.commit();
      			DataUtil.FlagCpu = false;
        		Intent intent2 = new Intent(RunAll4HoursActivity.this, CpuTestActivity.class);       
                String message2 =getResources().getString(R.string.cpu_time_four);
                int CpuTime = Integer.parseInt(message2);              
                intent2.putExtra(Const.EXTRA_MESSAGE,CpuTime);
                DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                startActivity(intent2);
          	}
        	else  if (DataUtil.FlagLcd) {
        		DataUtil.FlagLcd = false;
                Intent intent = new Intent(this, LcdActivity.class);          
                String lcdTimestr =getResources().getString(R.string.lcd_time_four);
                int LcdTime = Integer.parseInt(lcdTimestr);
                DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                intent.putExtra(Const.EXTRA_MESSAGE,LcdTime);
                startActivity(intent);
              }
              else if (DataUtil.Flag3D) {
            	DataUtil.Flag3D = false; 
                Intent intent = new Intent(this, Test3DActivity.class);         
                String ThreedTimestr =getResources().getString(R.string.Threed_time_four);
                int ThreedTime = Integer.parseInt(ThreedTimestr);
                DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                intent.putExtra(Const.EXTRA_MESSAGE,ThreedTime);
                startActivity(intent);
              }
              else if (DataUtil.FlagEmmc) {
            	//  if(AlltestTime==0){
            		  DataUtil.FlagEmmc = false;
            	//  }
            	  
                  Intent intent = new Intent(this, EmmcActivity.class);           
                  String time =getResources().getString(R.string.emmc_time_four);
                  int EmmcTime = Integer.parseInt(time);
                  DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                  intent.putExtra(Const.EXTRA_MESSAGE,EmmcTime);
                  startActivity(intent);
              } 
              else if (DataUtil.FlagSr) {
            	//  if(AlltestTime==0){
            		  DataUtil. FlagSr = false;
            	//  }
            		  DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                  Intent intent = new Intent(this, SlpRsmActivity.class);          
                  String message = getResources().getString(R.string.sr_circle_four);
                  int circle = Integer.parseInt(message);
                  intent.putExtra(Const.EXTRA_MESSAGE,circle);
                  startActivity(intent);
              } 
              else if (DataUtil.FlagVideo) {
            	//  if(AlltestTime==0){
            		  DataUtil.FlagVideo = false;
            	//  }
            	  
                  Intent intent = new Intent(this, VideoActivity.class);       
                  String message = getResources().getString(R.string.video_time_four);
                  int videoTime = Integer.parseInt(message);
                  DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                  intent.putExtra(Const.EXTRA_MESSAGE,videoTime);
                  startActivity(intent);
              }
              else if(DataUtil.FlagCamera){
            	//  if(AlltestTime==0){
            		  DataUtil.FlagCamera = false;
            	//  }
            		  DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                  Intent intent = new Intent(this, CameraTestActivity.class);       
                  String message = getResources().getString(R.string.camera_circle_four);
                  int Cameracircle = Integer.parseInt(message);
                  intent.putExtra(Const.EXTRA_MESSAGE,Cameracircle);
                  startActivity(intent);
              }
              else if (DataUtil.FlagReboot) {
            	//  if(AlltestTime==0){
            		  DataUtil.FlagReboot = false; 
            	//  }
            		  DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
                  Intent intent = new Intent(this, RebootActivity.class);        
                  String message = getResources().getString(R.string.reboot_circle_four);
                  int rebootcircle = Integer.parseInt(message);
                  intent.putExtra(Const.EXTRA_MESSAGE,rebootcircle);
                  startActivity(intent);
                 /* if(AlltestTime==1){
              		DataUtil.FlagCpu=true;
              	}*/
              }	else if(DataUtil.FlagDdr&&DataUtil.FlagRebootFinish){
            	  DataUtil.FlagDdr=false;
              	Intent intent = new Intent(this, DdrActivity.class);   
              	 DataUtil.addBackPressActivity(RunAll4HoursActivity.this, "RunAll4HoursActivity");
              	 String message = getResources().getString(R.string.ddr_cricle_four);
                   int ddr_cricle = Integer.parseInt(message);
                   intent.putExtra(Const.EXTRA_MESSAGE,ddr_cricle);
                   startActivity(intent);
                startActivity(intent);
            }else if(DataUtil.FlagfourDdrFinalFinish){
            	DataUtil.FlagfourDdrFinalFinish=false;
            	showTestResult();
            }
   
    
	}

	
	
	AlertDialog dialog=null;
    private void showTestResult(){
    	String str1=this.getResources().getString(R.string.testresult);
    	String message=getresultmessage();
    	LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll4HourActivity", "showTestResult"+"\n");
		String str3=this.getResources().getString(R.string.sure);
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	 builder.setTitle(str1) ;
    	 builder.setMessage(message) ;
    	 builder.setPositiveButton(str3,  new OnClickListener() {
			
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				 OldTestResult.CleanTestResult();
				 RunAll4HoursActivity.this.finish();
			}
		} );
    	  dialog = builder.show();
    }
    
    
private String getresultmessage(){
    	
        
    	String message=getResources().getString(R.string.cputestresult)+defineResult(true)+"\n"
    			+getResources().getString(R.string.lcdtestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.dtestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.emmctestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.srtestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.videotestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.cameratestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.reboottestresult)+defineResult(true)+"\n"+
    			getResources().getString(R.string.ddrtestresult)+defineResult(true)+"\n";
    	
		return message;
    	
    }
private String defineResult(boolean result){
	String str="";
	if(result){
		 str="PASS";
	}else{
		str="FAIL";
	}
	return str;
}

    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	 
	 
	 
	 
	 
}
