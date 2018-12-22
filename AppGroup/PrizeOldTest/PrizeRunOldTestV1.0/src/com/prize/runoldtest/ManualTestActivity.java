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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

public class ManualTestActivity extends Activity {
    private Button ButtonOk;
    private Button ButtonCancel;
    private   ScrollView scrollView = null;
   private EditText editTextCpu;
   private EditText editTextLcd;
   private EditText editTextD;
   private EditText editTextEmmc;
   private EditText editTextSr;
   private EditText editTextVideo;
   private EditText editTextcamera;
   private EditText editTextreboot;
   private EditText editTextddr;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);
        scrollView= (ScrollView) findViewById(R.id.sv_testitem);
        scrollView.setVerticalScrollBarEnabled(true);
        ButtonOk = (Button) findViewById(R.id.bt_ok);
        ButtonCancel = (Button) findViewById(R.id.bt_cancel);       
        editTextCpu=(EditText) findViewById(R.id.et_cputest);        
        editTextLcd=(EditText) findViewById(R.id.et_lcdtest);      
        editTextD=(EditText) findViewById(R.id.et_3dtest);      
        editTextEmmc=(EditText) findViewById(R.id.et_emmctest);        
        editTextSr=(EditText) findViewById(R.id.et_srtest);        
        editTextVideo=(EditText) findViewById(R.id.et_videotest);        
        editTextcamera=(EditText) findViewById(R.id.et_cameratest);       
        editTextreboot=(EditText) findViewById(R.id.et_reboottest);        
        editTextddr=(EditText) findViewById(R.id.et_ddrtest);
        
        Intent intent=getIntent();
        int isReactActivity= intent.getIntExtra(Const.REACT_ACTIVITY,0);
        if(isReactActivity==1){
         setEditViewContent();	
        }
        DataUtil.FlagDdr=true;
        SharedPreferences sharedPreferences =getSharedPreferences("activityname", ManualTestActivity.MODE_PRIVATE); //˽�����
		Editor editor = sharedPreferences.edit();
		editor.putInt("activitynamenumber", DataUtil.AUTOTESTACTIVITY);		
		editor.commit();
        ButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManualTestActivity.this, CpuTestActivity.class);
                boolean iscorrect=    SaveCurruentEditData();
                if(iscorrect){
                	OldTestResult.CleanTestResult();
                	String testTime = editTextCpu.getText().toString();
                    int CpuTime = Integer.parseInt(testTime);
                    intent.putExtra(Const.EXTRA_MESSAGE,CpuTime);
                
                    
                    startActivity(intent);
                }
            }
        });

        ButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            	finish();
                /*Intent intent = new Intent(ManualTestActivity.this, VideoActivity.class);
                startActivity(intent);*/
                //try {
                //    Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);

                // }catch (Exception e){
                //}
            }
        });
    }
   
    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		 DataUtil.isManualTst=false;
		OldTestResult.CleanTestResult();
		DataUtil.resetFlag();
		
	}

	private boolean SaveCurruentEditData(){
    	
    	boolean ContentCorrect=true;
    	SharedPreferences sharedPreferences =getSharedPreferences("savedata", ManualTestActivity.MODE_PRIVATE);
 		Editor editor = sharedPreferences.edit();
    	
         String testTime = editTextCpu.getText().toString().trim();
         String message =editTextLcd.getText().toString().trim();
         String message1 = editTextD.getText().toString().trim();
         String message2 = editTextEmmc.getText().toString().trim();
         String message3 = editTextSr.getText().toString().trim();
         String message4 = editTextVideo.getText().toString().trim();
         String message5 = editTextcamera.getText().toString().trim();
         String message6 = editTextreboot.getText().toString().trim();
         String message7 = editTextddr.getText().toString().trim();
         
         
         
         
         if(testTime.equals("")||message.equals("")||message1.equals("")||message2.equals("")
        		 ||message3.equals("")||message4.equals("")||message5.equals("")||message6.equals("")
        		 ||message7.equals("")){
        	 ContentCorrect=false;
        	 ShowDialog();
        	 
         }else{
        	 editor.putString("cputest", testTime);	
        	 editor.putString("lcdtest", message);
        	 editor.putString("3dtestt", message1);
        	 editor.putString("emmctest", message2);
        	 editor.putString("srtest", message3);
        	 editor.putString("videotest", message4);
        	 editor.putString("cameratest", message5);
        	 editor.putString("reboottest", message6);
        	 editor.putString("rddrtest", message7);
        	 editor.commit();
         }
 		
 		 return ContentCorrect;
    }
	private void ShowDialog(){
		String str1=this.getResources().getString(R.string.notice);
		String str2=this.getResources().getString(R.string.mcontent);
		String str3=this.getResources().getString(R.string.sure);
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	 builder.setTitle(str1) ;
    	 builder.setMessage(str2) ;
    	 builder.setPositiveButton(str3,  null );
    	 builder.show(); 
    }
    private void setEditViewContent(){
    	SharedPreferences sharedPreferences =getSharedPreferences("savedata", ManualTestActivity.MODE_PRIVATE); 
    	String editTextCpustr=sharedPreferences.getString("cputest","");
    	if(!editTextCpustr.equals("")){
    		editTextCpu.setText(editTextCpustr);
    	}
    	String editTextLcdstr=sharedPreferences.getString("lcdtest","");
    	if(!editTextLcdstr.equals("")){
    		editTextLcd.setText(editTextLcdstr);
    	}
    	String editTextDstr=sharedPreferences.getString("3dtestt","");
    	if(!editTextDstr.equals("")){
    		editTextD.setText(editTextDstr);
    	}
    	String editTextEmmcstr=sharedPreferences.getString("emmctest","");
    	if(!editTextEmmcstr.equals("")){
    		editTextEmmc.setText(editTextEmmcstr);
    	}
    	String editTextSrstr=sharedPreferences.getString("srtest","");
    	if(!editTextSrstr.equals("")){
    		editTextSr.setText(editTextSrstr);
    	}
    	String  editTextVideostr=sharedPreferences.getString("videotest","");
    	if(!editTextVideostr.equals("")){
    		editTextVideo.setText(editTextVideostr);
    	}
    	String editTextcamerastr=sharedPreferences.getString("cameratest","");
    	if(!editTextcamerastr.equals("")){
    		editTextcamera.setText(editTextcamerastr);
    	} 
    	String   editTextrebootstr=sharedPreferences.getString("reboottest","");
    	if(!editTextrebootstr.equals("")){
    		editTextreboot.setText(editTextrebootstr);
    	} 
    	String editTextddrstr=sharedPreferences.getString("rddrtest","");
    	if(!editTextddrstr.equals("")){
    		editTextddr.setText(editTextddrstr);
    	} 
    	  
    	
    }

    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
        LogToFile. writeToFile(LogToFile.VERBOSE,"ManualTest:onstart~~DataUtil.FlagVideo:"+DataUtil.FlagVideo,"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE,"ManualTest:onstart~~DataUtil.FlagCamera:"+DataUtil.FlagCamera,"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE,"ManualTest:onstart~~DataUtil.FlagDdr:"+DataUtil.FlagDdr,"\n");
        LogToFile. writeToFile(LogToFile.VERBOSE,"ManualTest:onstart~~DataUtil.FlagRebootFinish:"+DataUtil.FlagRebootFinish,"\n");
        if (DataUtil.FlagLcd) {
        	DataUtil.FlagLcd = false;
            Intent intent = new Intent(this, LcdActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_lcdtest);
            String message = editText.getText().toString();
            int LcdTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,LcdTime);
            startActivity(intent);
        }
        else if (DataUtil.Flag3D) {
        	DataUtil.Flag3D = false;
            Intent intent = new Intent(this, Test3DActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_3dtest);
            String message = editText.getText().toString();
            int SolidTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,SolidTime);
            startActivity(intent);
        }
        else if (DataUtil.FlagEmmc) {
        	DataUtil.FlagEmmc = false;
            Intent intent = new Intent(this, EmmcActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_emmctest);
            String message = editText.getText().toString();
            int EmmcTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,EmmcTime);
            startActivity(intent);
        }
      /*  else if (FlagMem) {
            FlagMem = false;
            Intent intent = new Intent(this, DdrActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_memtest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }*/
        else if (DataUtil.FlagSr) {
        	DataUtil.FlagSr = false;
            Intent intent = new Intent(this, SlpRsmActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_srtest);
            String message = editText.getText().toString();
            int MessageValue=Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,MessageValue);
            startActivity(intent);
        }
        else if (DataUtil.FlagVideo) {
        	DataUtil.FlagVideo = false;
            Intent intent = new Intent(this, VideoActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_videotest);
            String message = editText.getText().toString();
            int videoTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,videoTime);
            startActivity(intent);
        }else if(DataUtil.FlagCamera){
        	DataUtil.FlagCamera = false;
            Intent intent = new Intent(this, CameraTestActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_cameratest);
            String message = editText.getText().toString();
            int MessageValue=Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,MessageValue);
            startActivity(intent);
        }
        else if (DataUtil.FlagReboot) {
        	DataUtil.FlagReboot = false;
            Intent intent = new Intent(this, RebootActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_reboottest);
            String message = editText.getText().toString();
            int MessageValue=Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,MessageValue);
            startActivity(intent);
        }else if(DataUtil.FlagDdr&&DataUtil.FlagRebootFinish&&!DataUtil.ManualTestFinish){
        	DataUtil.FlagDdr = false;
        	
            Intent intent = new Intent(this, DdrActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_ddrtest);
            String message = editText.getText().toString();
            int messageValue=Integer.parseInt(message);
            SharedPreferences sharedPreferences =getSharedPreferences("manulddrtime", ManualTestActivity.MODE_PRIVATE); 
	  		Editor editor = sharedPreferences.edit();
			editor.putInt("manulddrtimes",messageValue);		
			editor.commit();
            intent.putExtra(Const.EXTRA_MESSAGE,1);
            startActivity(intent);
        }else if(DataUtil.ManualTestFinish){
        	DataUtil.ManualTestFinish=false;
        	showTestResult();
        }
    }
    AlertDialog dialog=null;
    private void showTestResult(){
    	
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
				 ManualTestActivity.this.finish();
			}
		} );
    	  dialog = builder.show();
    	
    	
    	
    	/*String str1=this.getResources().getString(R.string.testresult);
    	String message=getresultmessage();
	
		String str3=this.getResources().getString(R.string.sure);
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
    	 builder.setTitle(str1) ;
    	 builder.setMessage(message) ;
    	 builder.setPositiveButton(str3,  null );
    	 builder.show();*/
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
		 LogToFile. writeToFile(LogToFile.VERBOSE,"ManualTestonDestroy:","\n");
		DataUtil.resetFlag();
		
		
	}
}
