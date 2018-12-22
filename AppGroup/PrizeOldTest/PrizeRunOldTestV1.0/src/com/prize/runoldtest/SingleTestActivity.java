package com.prize.runoldtest;

import com.prize.runoldtest.camera.CameraTestActivity;
import com.prize.runoldtest.cpu.CpuTestActivity;
import com.prize.runoldtest.ddr.DdrActivity;
import com.prize.runoldtest.emmc.EmmcActivity;
import com.prize.runoldtest.flight.FlightActivity;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SingleTestActivity extends Activity implements OnClickListener{
	
	private Button lcd;
	private Button flashlight;
	private Button playvideo;
	private Button emmc;
	private Button i2c;
	//private Button audio;
	//private Button tp;
	private Button camera;
	private Button test3d;
	private Button reboot;
	private Button rwbgpower;
	//private Button sensor;
	private Button ddr;
	
	private Button sr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_test);
	//	DataUtil.addDestoryActivity(SingleTestActivity.this, "SingleTestActivity");
		SharedPreferences sharedPreferences =getSharedPreferences("activityname", ManualTestActivity.MODE_PRIVATE); //˽�����
		Editor editor = sharedPreferences.edit();
		editor.putInt("activitynamenumber", DataUtil.SINGLEACTIVITY);		
		editor.commit();
		initView();
		showRebootTestResult();
	}
	
	
	private void showRebootTestResult(){
		 Intent intent=getIntent();
		if(intent.hasExtra(Const.SIGLE_REBOOTACTIVITY_FINISH)){
			boolean isreboottest=intent.hasExtra(Const.SIGLE_REBOOTACTIVITY_FINISH);
			if(isreboottest){
				DataUtil.resetFlag();
				String message="RebootTest:PASS";
				 String str1=this.getResources().getString(R.string.testresult);
					String str3=this.getResources().getString(R.string.sure);
					 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
			    	 builder.setTitle(str1) ;
			    	 builder.setMessage(message) ;
			    	 builder.setPositiveButton(str3,  null );
			    	 builder.show();
			}
		} 
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
	}
	
	private void initView(){
		lcd = (Button)findViewById(R.id.lcd);
		lcd.setOnClickListener(this);
		flashlight = (Button)findViewById(R.id.flashlight);
		flashlight.setOnClickListener(this);
		playvideo = (Button)findViewById(R.id.playvideo);
		playvideo.setOnClickListener(this);		
		emmc = (Button)findViewById(R.id.emmc);
		emmc.setOnClickListener(this);
		i2c = (Button)findViewById(R.id.i2c);
		i2c.setOnClickListener(this);
		//audio = (Button)findViewById(R.id.audio);
		//audio.setOnClickListener(this);
		//tp = (Button)findViewById(R.id.tp);
		//tp.setOnClickListener(this);
		camera = (Button)findViewById(R.id.camera);
		camera.setOnClickListener(this);
		test3d = (Button)findViewById(R.id.test3d);
		test3d.setOnClickListener(this);
		rwbgpower = (Button)findViewById(R.id.rwbgpower);
		rwbgpower.setOnClickListener(this);
		//sensor = (Button)findViewById(R.id.sensor);
		//sensor.setOnClickListener(this);
		ddr = (Button)findViewById(R.id.ddr);
		ddr.setOnClickListener(this);
		sr=(Button)findViewById(R.id.sr);
		sr.setOnClickListener(this);
		reboot = (Button)findViewById(R.id.reboot);
		reboot.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lcd:
			OldTestResult.LcdTestresult=false;
			startActivity(LcdActivity.class);
			break;
		case R.id.flashlight:
			startActivity(FlightActivity.class);
			break;
		case R.id.playvideo:
			OldTestResult.VideoTestresult=false;
			startActivity(VideoActivity.class);
			break;
		case R.id.emmc:
			OldTestResult.EmmcTestresult=false;
			
			Intent thisintent = new Intent(SingleTestActivity.this, EmmcActivity.class);
			thisintent.putExtra(Const.EXTRA_MESSAGE, 1);
			SingleTestActivity.this.startActivityForResult(thisintent, 1);
			//startActivity(EmmcActivity.class);
			break;
		case R.id.i2c:
			
			break;
	//	case R.id.audio:
			
		//	break;
	//	case R.id.tp:
			
		//	break;
		case R.id.camera:
			OldTestResult.CameraTestresult=false;
			startActivity(CameraTestActivity.class);
			break;
		case R.id.test3d:
			OldTestResult.DTestresult=false;
			startActivity(Test3DActivity.class);
			break;
		case R.id.reboot:
			OldTestResult.RebootTestresult=false;
			
			Intent intent = new Intent(SingleTestActivity.this, RebootActivity.class);
			intent.putExtra(Const.EXTRA_MESSAGE, 5);
			SingleTestActivity.this.startActivityForResult(intent, 1);
			
		//	startActivity(RebootActivity.class);
			break;
		case R.id.rwbgpower:
			startActivity(CpuTestActivity.class);
			break;
	//	case R.id.sensor:
			
		//	break;
		case R.id.ddr:
			OldTestResult.DdrTestresult=false;
			Intent mintent = new Intent(SingleTestActivity.this, DdrActivity.class);
			mintent.putExtra(Const.EXTRA_MESSAGE, 1);
			SingleTestActivity.this.startActivityForResult(mintent, 1);
			break;
		case R.id.sr:
			OldTestResult.SrTestresult=false;
			startActivity(SlpRsmActivity.class);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		OldTestResult.CleanTestResult();
		super.onDestroy();
	}
	 
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//getIntent().hasExtra(Const.EXTRA_MESSAGE)
		LogToFile. writeToFile(LogToFile.VERBOSE, "SingleTestActivity", "onActivityResult..."+"\n");
		if(data!=null){
			if(data.hasExtra("result")){
				LogToFile. writeToFile(LogToFile.VERBOSE, "SingleTestActivity", "data is not null.."+"\n");
				DataUtil.resetFlag();
				 String result = data.getExtras().getString("result").trim();//得到新Activity 关闭后返回的数据
				 String str1=this.getResources().getString(R.string.testresult);
					String str3=this.getResources().getString(R.string.sure);
					 AlertDialog.Builder builder = new AlertDialog.Builder(this);  
			    	 builder.setTitle(str1) ;
			    	 builder.setMessage(result) ;
			    	 builder.setPositiveButton(str3,  null );
			    	 builder.show();
			}
		}
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		DataUtil.resetFlag();
	}

	private void startActivity(Class<?> cls){
		LogUtil.e(cls.getName());
		Intent intent = new Intent(SingleTestActivity.this, cls);
		intent.putExtra(Const.EXTRA_MESSAGE, 20);
		SingleTestActivity.this.startActivityForResult(intent, 1);
	}
}