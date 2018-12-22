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

public class RunAll12HourActivity extends Activity {
	
	//private static int AlltestTime=4;//除了ddr之外的所有测试项的测试大循环次数
	//private static int DdrTestTime=2;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_all12_hour);
        
        DataUtil.FlagDdr=true;
        SharedPreferences sharedPreferences =getSharedPreferences("twlftesttime", RunAllTestActivity.MODE_PRIVATE); 
  		int testtime=sharedPreferences.getInt("twlftesttimes", 0);
  		Intent mintent=getIntent();
      	boolean isddract=	mintent.getBooleanExtra("isddract", false);
      	 LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity", "oncreat DataUtil.camera:"+DataUtil.FlagCamera+"\n");
		/* if(testtime>0&&isddract){
			 DataUtil.FlagCpu=true;
		 }*/
		 LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity", "oncreat DataUtil.FlagCpu:"+DataUtil.FlagCpu+"\n");
        DataUtil.addDestoryActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
       
        SharedPreferences msharedPreferences =getSharedPreferences("activityname", ManualTestActivity.MODE_PRIVATE); //私有数据
		Editor editor = msharedPreferences.edit();//获取编辑器
		editor.putInt("activitynamenumber", DataUtil.TELVEHOURSTESTACTIVITY);		
		editor.commit();//提交修改	
    }


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		 LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity  onConfigurationChanged","\n");
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
		super.onStart();
		
		
		 LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity", "onstatrt--DataUtil.FlagtlfDdrFinalFinish:"+DataUtil.FlagtlfDdrFinalFinish+"\n");
		/* Editor editor = sharedPreferences.edit();
  		editor.putInt("twlftesttimes", 2);		
  		editor.commit();*/
		 LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity", "onstart--DataUtil.FlagCamera:"+DataUtil.FlagCamera+"\n");
		 //  if(AlltestTime>=1){
			   if(DataUtil.FlagCpu){
	        	//	AlltestTime--;
				   SharedPreferences sharedPreferences =getSharedPreferences("twlftesttime", RunAll12HourActivity.MODE_PRIVATE); 
				   int times=sharedPreferences.getInt("twlftesttimes", 0);
				   Editor editor = sharedPreferences.edit();
			  		editor.putInt("twlftesttimes", times-1);		
			  		editor.commit();
	      			  DataUtil.FlagCpu = false;
	      			  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	        		Intent intent2 = new Intent(RunAll12HourActivity.this, CpuTestActivity.class);       
	                String message2 =getResources().getString(R.string.cpu_time);
	                int CpuTime = Integer.parseInt(message2);              
	                intent2.putExtra(Const.EXTRA_MESSAGE,CpuTime);
	                startActivity(intent2);
	          	}
			   else  if (DataUtil.FlagLcd) {
	        		 // if(AlltestTime==0){
	        			  DataUtil.FlagLcd = false;
	        		//  }
	        			  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, LcdActivity.class);          
	                  String lcdTimestr =getResources().getString(R.string.lcd_time);
	                  int LcdTime = Integer.parseInt(lcdTimestr);
	                  
	                  intent.putExtra(Const.EXTRA_MESSAGE,LcdTime);
	                  startActivity(intent);
	              }
	              else if (DataUtil.Flag3D) {
	            	 // if(AlltestTime==0){
	            		  DataUtil.Flag3D = false;
	            	//  }
	            		  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, Test3DActivity.class);         
	                  String ThreedTimestr =getResources().getString(R.string.Threed_time);
	                  int ThreedTime = Integer.parseInt(ThreedTimestr);
	                 
	                  intent.putExtra(Const.EXTRA_MESSAGE,ThreedTime);
	                  startActivity(intent);
	              }
	              else if (DataUtil.FlagEmmc) {
	            	 // if(AlltestTime==0){
	            		  DataUtil.FlagEmmc = false;
	            	//  }
	            		  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, EmmcActivity.class);           
	                  String time =getResources().getString(R.string.emmc_time);
	                  int EmmcTime = Integer.parseInt(time);
	                  
	                  intent.putExtra(Const.EXTRA_MESSAGE,EmmcTime);
	                  startActivity(intent);
	              } 
	              else if (DataUtil.FlagSr) {
	            	//  if(AlltestTime==0){
	            		  DataUtil. FlagSr = false;
	            	//  }
	            		  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, SlpRsmActivity.class);          
	                  String message = getResources().getString(R.string.sr_circle);
	                  int circle = Integer.parseInt(message);
	                  intent.putExtra(Const.EXTRA_MESSAGE,circle);
	                  startActivity(intent);
	              } 
	              else if (DataUtil.FlagVideo) {
	            	 // if(AlltestTime==0){
	            		  DataUtil.FlagVideo = false;
	            	 // }
	            		  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, VideoActivity.class);       
	                  String message = getResources().getString(R.string.video_time);
	                  int videoTime = Integer.parseInt(message);
	                  
	                  intent.putExtra(Const.EXTRA_MESSAGE,videoTime);
	                  startActivity(intent);
	              }
	              else if(DataUtil.FlagCamera){
	            	//  if(AlltestTime==0){
	            		  DataUtil.FlagCamera = false;
	            	 // }
	            		  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, CameraTestActivity.class);       
	                  String message = getResources().getString(R.string.camera_circle);
	                  int Cameracircle = Integer.parseInt(message);
	                  intent.putExtra(Const.EXTRA_MESSAGE,Cameracircle);
	                  startActivity(intent);
	              }
	              else if (DataUtil.FlagReboot) {
	            	 // if(AlltestTime==0){
	            		  DataUtil.FlagReboot = false;
	            	//  }
	            		  DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
	                  Intent intent = new Intent(this, RebootActivity.class);        
	                  String message = getResources().getString(R.string.reboot_circle);
	                  int rebootcircle = Integer.parseInt(message);
	                  intent.putExtra(Const.EXTRA_MESSAGE,rebootcircle);
	                  startActivity(intent);
	                 /* if(AlltestTime>=1){
	                		DataUtil.FlagCpu=true;
	                	}*/
	              }	else if(DataUtil.FlagDdr&&DataUtil.FlagRebootFinish){
	            	  DataUtil.FlagDdr=false;
	                	Intent intent = new Intent(this, DdrActivity.class);   
	                	 DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll6HourActivity");
	                	 String message = getResources().getString(R.string.ddr_cricle);
	                     int ddr_cricle = Integer.parseInt(message);
	                     intent.putExtra(Const.EXTRA_MESSAGE,ddr_cricle);
	                     startActivity(intent);
	                  startActivity(intent);
	              }else if(DataUtil.FlagtlfDdrFinalFinish){
	            	  DataUtil.FlagtlfDdrFinalFinish=false;
	            	  
	              	showTestResult();
	              	
	              }
	    
		   
		   
		   
		 /*  else 
	        {
	        	if(DataUtil.FlagDdr&&DdrTestTime>=1){//ddr之外的测试大循环两次之后进行单次ddr测试
		        	DdrTestTime--;
		        	if(DdrTestTime==0){
		        		DataUtil.FlagDdr=false;
		        	}
		        	 DataUtil.addBackPressActivity(RunAll12HourActivity.this, "RunAll12HourActivity");
		        	Intent intent = new Intent(this, DdrActivity.class);        
		             startActivity(intent);
		        } else{
		        	showTestResult();
		        }	
	        }*/
	        	
	        	
	        	
	}


	
	  AlertDialog dialog=null;
	    private void showTestResult(){
	    	
	    	
	    	LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity", "showTestResult"+"\n");
	    	String str1=this.getResources().getString(R.string.testresult);
	    	String message=getresultmessage();
	    	
			String str3=this.getResources().getString(R.string.sure);
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
	    	 builder.setTitle(str1) ;
	    	 builder.setMessage(message) ;
	    	 builder.setPositiveButton(str3,  new OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					 OldTestResult.CleanTestResult();
					RunAll12HourActivity.this.finish();
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
		LogToFile. writeToFile(LogToFile.VERBOSE, "RunAll12HourActivity", "onDestroy  resetFlag FlagCamera:"+DataUtil.FlagCamera+"\n");
		DataUtil.resetFlag();
		 DataUtil. isTwlfTest=false;
	}
    
    
    
}
