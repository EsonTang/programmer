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
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.OldTestResult;
import com.prize.runoldtest.video.VideoActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;

public class RunAll6HourActivity extends Activity {
	
	
    private String TAG="6HoursTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_all6_hour);
        DataUtil.FlagDdr=true;
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "RunAll6HourActivity  oncreat:"+"\n");
       // DataUtil.FlagCpu=true;
        DataUtil.addDestoryActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
       
        SharedPreferences sharedPreferences =getSharedPreferences("activityname", ManualTestActivity.MODE_PRIVATE); //私有数据
		Editor editor = sharedPreferences.edit();//获取编辑器
		editor.putInt("activitynamenumber", DataUtil.SIXHOURSTESTACTIVIT);		
		editor.commit();//提交修改	
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "6HoursTest onConfigurationChanged.."+"\n");
	}

	protected void onStart(){

        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	               | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	               | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
       // LogUtil.e("onStartManualTestActivity");
      //  LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "6HoursTest Onstart..."+AlltestTime+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagVideo is true:"+(DataUtil.FlagVideo==true)+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagDdr:"+DataUtil.FlagDdr+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagRebootFinish:"+DataUtil.FlagRebootFinish+"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "DataUtil.FlagsixDdrFinalFinish:"+DataUtil.FlagsixDdrFinalFinish+"\n");
      //  if(AlltestTime>=1){
        	// LogToFile. writeToFile(LogToFile.VERBOSE, TAG, AlltestTime+"6HoursTest begin..."+"\n");
        	/*if(AlltestTime==1){
        		DataUtil.FlagCpu=true;
        	}*/
        	if(DataUtil.FlagCpu){
        		 SharedPreferences sharedPreferences =getSharedPreferences("sixtesttime", RunAll6HourActivity.MODE_PRIVATE); 
				   int times=sharedPreferences.getInt("sixtesttimes", 0);
				   Editor editor = sharedPreferences.edit();
			  		editor.putInt("sixtesttimes", times-1);		
			  		editor.commit();
        		
      			  DataUtil.FlagCpu = false;
      		  
        		Intent intent2 = new Intent(RunAll6HourActivity.this, CpuTestActivity.class);       
                String message2 =getResources().getString(R.string.cpu_time);
                int CpuTime = Integer.parseInt(message2);              
                intent2.putExtra(Const.EXTRA_MESSAGE,CpuTime);
                DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                startActivity(intent2);
          	}
        	else  if (DataUtil.FlagLcd) {
        		//  if(AlltestTime==0){
        			  DataUtil.FlagLcd = false;
        		 // }
        		  
                  Intent intent = new Intent(this, LcdActivity.class);          
                  String lcdTimestr =getResources().getString(R.string.lcd_time);
                  int LcdTime = Integer.parseInt(lcdTimestr);
                  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  intent.putExtra(Const.EXTRA_MESSAGE,LcdTime);
                  startActivity(intent);
              }
              else if (DataUtil.Flag3D) {
            	//  if(AlltestTime==0){
            		  DataUtil.Flag3D = false; 
            	 // }
            	  
                  Intent intent = new Intent(this, Test3DActivity.class);         
                  String ThreedTimestr =getResources().getString(R.string.Threed_time);
                  int ThreedTime = Integer.parseInt(ThreedTimestr);
                  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  intent.putExtra(Const.EXTRA_MESSAGE,ThreedTime);
                  startActivity(intent);
              }
              else if (DataUtil.FlagEmmc) {
            	//  if(AlltestTime==0){
            		  DataUtil.FlagEmmc = false;
            	//  }
            	  
                  Intent intent = new Intent(this, EmmcActivity.class);           
                  String time =getResources().getString(R.string.emmc_time);
                  int EmmcTime = Integer.parseInt(time);
                  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  intent.putExtra(Const.EXTRA_MESSAGE,EmmcTime);
                  startActivity(intent);
              } 
              else if (DataUtil.FlagSr) {
            	//  if(AlltestTime==0){
            		  DataUtil. FlagSr = false;
            	//  }
            		  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  Intent intent = new Intent(this, SlpRsmActivity.class);          
                  String message = getResources().getString(R.string.sr_circle);
                  int circle = Integer.parseInt(message);
                  intent.putExtra(Const.EXTRA_MESSAGE,circle);
                  startActivity(intent);
              } 
              else if (DataUtil.FlagVideo) {
            	//  if(AlltestTime==0){
            		  DataUtil.FlagVideo = false;
            	//  }
            	  
                  Intent intent = new Intent(this, VideoActivity.class);       
                  String message = getResources().getString(R.string.video_time);
                  int videoTime = Integer.parseInt(message);
                  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  intent.putExtra(Const.EXTRA_MESSAGE,videoTime);
                  startActivity(intent);
              }
              else if(DataUtil.FlagCamera){
            	//  if(AlltestTime==0){
            		  DataUtil.FlagCamera = false;
            	//  }
            		  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  Intent intent = new Intent(this, CameraTestActivity.class);       
                  String message = getResources().getString(R.string.camera_circle);
                  int Cameracircle = Integer.parseInt(message);
                  intent.putExtra(Const.EXTRA_MESSAGE,Cameracircle);
                  startActivity(intent);
              }
              else if (DataUtil.FlagReboot) {
            	//  if(AlltestTime==0){
            		  DataUtil.FlagReboot = false; 
            	//  }
            		  DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
                  Intent intent = new Intent(this, RebootActivity.class);        
                  String message = getResources().getString(R.string.reboot_circle);
                  int rebootcircle = Integer.parseInt(message);
                  intent.putExtra(Const.EXTRA_MESSAGE,rebootcircle);
                  startActivity(intent);
                 /* if(AlltestTime==1){
              		DataUtil.FlagCpu=true;
              	}*/
              }	else if(DataUtil.FlagDdr&&DataUtil.FlagRebootFinish){
            	  DataUtil.FlagDdr=false;
              	Intent intent = new Intent(this, DdrActivity.class);   
              	 DataUtil.addBackPressActivity(RunAll6HourActivity.this, "RunAll6HourActivity");
              	 String message = getResources().getString(R.string.ddr_cricle);
                   int ddr_cricle = Integer.parseInt(message);
                   intent.putExtra(Const.EXTRA_MESSAGE,ddr_cricle);
                   startActivity(intent);
                startActivity(intent);
            }else if(DataUtil.FlagsixDdrFinalFinish){
            	DataUtil.FlagsixDdrFinalFinish=false;
            	showTestResult();
            }
   
    }

    AlertDialog dialog=null;
    private void showTestResult(){
    	String str1=this.getResources().getString(R.string.testresult);
    	String message=getresultmessage();
    	LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll6HourActivity", "showTestResult"+"\n");
		String str3=this.getResources().getString(R.string.sure);
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	 builder.setTitle(str1) ;
    	 builder.setMessage(message) ;
    	 builder.setPositiveButton(str3,  new OnClickListener() {
			
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				 OldTestResult.CleanTestResult();
				RunAll6HourActivity.this.finish();
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		DataUtil.resetFlag();
		 DataUtil.isSixTest=false;
		 LogToFile. writeToFile(LogToFile.VERBOSE, TAG, "6HoursTest Ondestroy");
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		DataUtil.resetFlag();
		finish();
	}
    
    
}
