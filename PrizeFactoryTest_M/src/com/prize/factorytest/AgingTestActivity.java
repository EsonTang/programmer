package com.prize.factorytest;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.graphics.Color;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;  
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;
import com.prize.factorytest.util.AgingTestItems;
import android.widget.EditText;

import java.io.File;
import java.io.IOException; 
import java.io.FileInputStream;
import java.io.InputStreamReader;  
import java.io.FileOutputStream;

public class AgingTestActivity extends Activity {
	private static final String TAG = "AgingTestActivity";
	private static final int MSG_START_FROM_REBOOT=0;
	private static final int MSG_REFRESH_SECONDS=1;
	private static final String KEY_REBOOT_RESULT = "reboot_result";
	private static final String KEY_SLEEP_RESULT = "sleep_result";
	private static final String KEY_FAIL_RESULT = "fail";
	private static final String KEY_PASS_RESULT = "pass";
	private LayoutInflater mInflater;
	private GridViewAdapter mGridViewAdapter;
	public static String[] agingtest_items = new String[6];
	private Button startButton;
	private FactoryTestApplication app;
	private TextView duration_tv;
	private EditText duration_hour;
	private EditText duration_minute;
	private TextView reboot_tv;
	private TextView sleep_tv;
	private EditText reboot_minite;
	private EditText sleep_minute;
	private CheckBox reboot_cb;
	private CheckBox sleep_cb;
	private boolean bReboot = false;
	private boolean bSleep =false;
	
	private CountDownTimer mCountDownTimer;
	private CountDownTimer mSleepCountDownTimer;
	private TextView messageText;
	
	private PowerManager manager;
	
	private String duration_tv_bak;
	private String reboot_tv_bak;
	private String sleep_tv_bak;
	public	String aging_text;
			
	private static final String filePath = "/data/prize_backup/";
	private static final String fileName = "prize_factory_data";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agingtest);
		mInflater = LayoutInflater.from(this);
		startButton = (Button) findViewById(R.id.agingteststart_bt);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {	
				if(bReboot){
					app.getSharePref().putValue("reboot_selected", "1");
				}
				if(bSleep){
					app.getSharePref().putValue("sleep_selected", "1");
				}
				handleStartTest();
			}
		});			
		agingtest_items=getResources().getStringArray(R.array.aging_test_items);
		GridView grid = (GridView) findViewById(R.id.agingtest_gv);
		mGridViewAdapter = new GridViewAdapter(this,
				android.R.layout.simple_list_item_1, agingtest_items);
		grid.setAdapter(mGridViewAdapter);
		
		app = (FactoryTestApplication) getApplication();		
		duration_tv= (TextView) findViewById(R.id.duration_detail_tv);
		reboot_tv =(TextView) findViewById(R.id.reboot_report_tv);
		sleep_tv =(TextView) findViewById(R.id.sleep_report_tv);
				
		HashMap<String,String> houtminute= seconds2hourminute(Long.parseLong(app.getSharePref().getValue("parallel_time")));
		
		duration_hour =(EditText) findViewById(R.id.hour_edt);
		duration_minute =(EditText) findViewById(R.id.minute_edt);
		duration_hour.setText(houtminute.get("hour"));
		duration_minute.setText(houtminute.get("minute"));
		
		reboot_minite =(EditText) findViewById(R.id.reboot_minite_et);
		reboot_minite.setText(seconds2minute("reboot_time"));
		
		sleep_minute =(EditText) findViewById(R.id.sleep_minite_et);
		sleep_minute.setText(seconds2minute("sleep_time"));
		

		if(getIntent().hasExtra("reboot")&&getIntent().getExtras().getBoolean("reboot")){
			mHandler.sendEmptyMessage(MSG_START_FROM_REBOOT);
		}
	} 
	private void initRebootSleep(){
		reboot_cb=(CheckBox) findViewById(R.id.serial_reboot_cb);
		reboot_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {          
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				bReboot=arg1;
			}
		});
		reboot_cb.setChecked(bReboot);
		sleep_cb=(CheckBox) findViewById(R.id.serial_sleep_cb);
		sleep_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {          
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				bSleep=arg1;
			}
		});
		sleep_cb.setChecked(bSleep);
	}	
	
	@Override
	protected void onResume() {
		initRebootSleep();
		//add liup aging recovery backup
		creatFile();
		readFileValue();
		//add liup aging recovery backup

		super.onResume();		
	}
	
	private void creatFile() {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (Exception e) {
		}
	
		file = new File(filePath + fileName);
        if(!file.exists()){  
            try {  
                file.createNewFile();
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }else {  
        }  
		
		int status = -1;  
		try {  
			Process p = Runtime.getRuntime().exec("chmod 777 " + filePath + fileName);  
			status = p.waitFor();  
		} catch (IOException e) {  
			e.printStackTrace();  
		} catch (InterruptedException e) {  
			e.printStackTrace();  
		}  
		if (status == 0) {       
			Log.e("liup","chmod succeed");
		} else {      
			Log.e("liup","chmod failed");
		}    
		 
	}
	
	private void writeFile(String data) {
		try {
			FileOutputStream fout = new FileOutputStream("/data/prize_backup/prize_factory_data");
			byte[] bytes = data.getBytes();
			fout.write(bytes);
			fout.flush();				
			fout.close();
			Log.e("liup","writeFile succcess");
		} catch (Exception e) {
		}
	}
	
	private void readFileValue() {
		String fileData = null;
		String resultReboot = app.getSharePref().getValue(KEY_REBOOT_RESULT);
		String resultSleep = app.getSharePref().getValue(KEY_REBOOT_RESULT);
		try {
			FileInputStream mFileInputStream = new FileInputStream(filePath + fileName);
			InputStreamReader mInputStreamReader = new InputStreamReader(mFileInputStream, "UTF-8");
			char[] input = new char[mFileInputStream.available()];
			mInputStreamReader.read(input);
			mInputStreamReader.close();
			mFileInputStream.close();
			fileData = new String(input);
		} catch (Exception e) {
		}
		
		if(null != fileData){
			String[] temp = null;
			temp = fileData.split("-");
			if(temp.length == 3){
				duration_tv_bak = temp[0];
				reboot_tv_bak = temp[1];
				sleep_tv_bak = temp[2];
			}else{
				duration_tv_bak = getLastDuration();
				reboot_tv_bak = getText(KEY_REBOOT_RESULT,resultReboot);
				sleep_tv_bak = getText(KEY_SLEEP_RESULT,resultSleep);
			}
		}else{
			duration_tv_bak = getLastDuration();
			reboot_tv_bak = getText(KEY_REBOOT_RESULT,resultReboot);
			sleep_tv_bak = getText(KEY_SLEEP_RESULT,resultSleep);
		}
		duration_tv.setText(duration_tv_bak);
		
		reboot_tv.setText(reboot_tv_bak);
		
		if(null!= reboot_tv_bak && reboot_tv_bak.contains(KEY_PASS_RESULT)){
			reboot_tv.setTextColor(Color.GREEN);
		}else{
			reboot_tv.setTextColor(Color.RED);
		}
		
		sleep_tv.setText(sleep_tv_bak);
		if(null!= sleep_tv_bak && sleep_tv_bak.contains(KEY_PASS_RESULT)){
			sleep_tv.setTextColor(Color.GREEN);
		}else{
			sleep_tv.setTextColor(Color.RED);
		}
	}
	
	private String seconds2minute(String key){
		String seconds=app.getSharePref().getValue(key);
		long minute = Long.parseLong(seconds)/60;
		return String.valueOf(minute);
	}
	private HashMap<String,String> seconds2hourminute(long seconds){
		long hour =0;
		long minute =0;
		HashMap<String,String> hourminute= new HashMap<String,String>();
		hour=seconds/3600;
		minute=(seconds-(seconds/3600)*3600)/60;
		hourminute.put("hour", String.valueOf(hour));
		hourminute.put("minute", String.valueOf(minute));
		return hourminute;
	}
	private void setTestResult(String key,String result){
		if(key.equals(KEY_REBOOT_RESULT)){
			app.getSharePref().putValue(key,result);
			setTestReportTextView(KEY_REBOOT_RESULT);
		}
		if(key.equals(KEY_SLEEP_RESULT)){
			app.getSharePref().putValue(key,result);
			setTestReportTextView(KEY_SLEEP_RESULT);
		}
	}
	private void resetTestResult(){
		if(hasReboot())
			setTestResult(KEY_REBOOT_RESULT,KEY_FAIL_RESULT);
		if(hasSleep())
			setTestResult(KEY_SLEEP_RESULT,KEY_FAIL_RESULT);
	}
	private void handleStartTest(){
		if(hasParallelItem()){
			app.getSharePref().putValue("parallel_time", String.valueOf(getParallelTime()));
		}
		if(hasSleep()){
			app.getSharePref().putValue("sleep_time", String.valueOf(getSleepTime()));
		}
		if(hasReboot()){
			app.getSharePref().putValue("reboot_time", String.valueOf(getRebootTime()));			
		}
		setButton(false);
		resetTestResult();
		if(hasReboot()){
			app.getSharePref().putValue("reboot_currenttimemillis", String.valueOf(System.currentTimeMillis()));
			startReboot();
		}else if(hasSleep()){
			startSleepTest();
		}else if(hasParallelItem()){
			startParallelTest();
		}else{
			setButton(true);
		}
	}
	private void handleReboot(){
		long start=Long.parseLong(app.getSharePref().getValue("reboot_currenttimemillis"));
		long reboottime=Long.parseLong(app.getSharePref().getValue("reboot_time"));	
		if(System.currentTimeMillis()-start<reboottime*1000){
			showDialog();
			mCountDownTimer = new CountDownTimer(5*1000, 1000) {
				@Override
				public void onFinish() {
					mHandler.sendEmptyMessage(MSG_REFRESH_SECONDS);
					startReboot();
				}
				@Override
				public void onTick(long arg0) {
					mHandler.sendEmptyMessage(MSG_REFRESH_SECONDS);
				}
			}.start();
		}else{
			app.getSharePref().putValue("reboot_selected", "0");
			setTestResult(KEY_REBOOT_RESULT,KEY_PASS_RESULT);
			if(hasSleep()){
				startSleepTest();
			}else if(hasParallelItem()){
				startParallelTest();
			}else{
				setButton(true);
			}
		}
	}
	private void setButton(boolean active){
		startButton.setEnabled(active);
		startButton.setClickable(active);
	}
	private void startSleepTest(){
		manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mSleepCountDownTimer = new CountDownTimer(getSleepTime()*1000, 3000) {
			@Override
			public void onFinish() {
				if(!manager.isScreenOn()){
					manager.wakeUp(SystemClock.uptimeMillis());
				}
				app.getSharePref().putValue("sleep_selected", "0");
				setTestResult(KEY_SLEEP_RESULT,KEY_PASS_RESULT);
				setButton(true);
				if(hasParallelItem()){
					startParallelTest();
				}else{
				}
			}
			@Override
			public void onTick(long arg0) {
				if(manager.isScreenOn()){
					manager.goToSleep(SystemClock.uptimeMillis());
				}else{
					manager.wakeUp(SystemClock.uptimeMillis());
				}
			}
		}.start();
	}
	private void showDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(AgingTestActivity.this);
		TextView title = new TextView(AgingTestActivity.this);
		title.setText(R.string.serial_agingtest_reboot);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        
        builder.setCustomTitle(title);
        builder.setMessage("5");
        builder.setCancelable(false);
		builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface,int i) {
				if(mCountDownTimer!=null){
					mCountDownTimer.cancel();
				}
				setButton(true);
			}
		});
		AlertDialog alert = builder.show();
		messageText = (TextView)alert.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        messageText.setTextColor(Color.RED);
	}
	
	
	private String getText(String key,String result){
		String r=new String();
		if(key.equals(KEY_REBOOT_RESULT)){
			if(result.equals(KEY_PASS_RESULT)){
				r=getString(R.string.serial_agingtest_reboot)+":pass";
			}else if(result.equals(KEY_FAIL_RESULT)){
				r=getString(R.string.serial_agingtest_reboot)+":fail";
			}else{
				r=getString(R.string.serial_agingtest_reboot)+":untest";
			}
		}
		if(key.equals(KEY_SLEEP_RESULT)){
			if(result.equals(KEY_PASS_RESULT)){
				r=getString(R.string.serial_agingtest_sleep)+":pass";
			}else if(result.equals(KEY_FAIL_RESULT)){
				r=getString(R.string.serial_agingtest_sleep)+":fail";
			}else{
				r=getString(R.string.serial_agingtest_sleep)+":untest";
			}
		}
		return r;
	}
	private void setTestReportTextView(String key) {
		// TODO Auto-generated method stub
		String resultReboot = app.getSharePref().getValue(KEY_REBOOT_RESULT);
		String resultSleep = app.getSharePref().getValue(KEY_SLEEP_RESULT);
		if(key.equals(KEY_REBOOT_RESULT)){
			reboot_tv.setText(getText(KEY_REBOOT_RESULT,resultReboot));
			setTextViewCorlor(reboot_tv,resultReboot);
		}
		if(key.equals(KEY_SLEEP_RESULT)){
			sleep_tv.setText(getText(KEY_SLEEP_RESULT,resultSleep));
			setTextViewCorlor(sleep_tv,resultSleep);
		}
		//add liup aging recovery backup
		duration_tv_bak = getLastDuration();
		reboot_tv_bak = getText(KEY_REBOOT_RESULT,resultReboot);
		sleep_tv_bak = getText(KEY_SLEEP_RESULT,resultSleep);
		String aging_result_text = duration_tv_bak + "-" + reboot_tv_bak + "-" + sleep_tv_bak;
		Log.e("liup","reboot_tv_bak = " + reboot_tv_bak);
		writeFile(aging_result_text);
		//add liup aging recovery backup
	}
	private void setTextViewCorlor(TextView tv,String result){
		if(result.equals(KEY_PASS_RESULT)){
			tv.setTextColor(Color.GREEN);
		}else{
			tv.setTextColor(Color.RED);
		}	
	}
	private boolean hasSleep(){
		if(app.getSharePref().getValue("sleep_selected").equals("0"))
			return false;
		
		if(getSleepTime()<1){
			return false;
		} 
		
		return true;
	}
	
	//return time unit is seconds
	private long getSleepTime(){
		String sleep=sleep_minute.getText().toString();		
		if(sleep_minute.getText().toString().length()<1){
			sleep="0";
		}
		long duration =Integer.parseInt(sleep)*60;
		return duration;
	}
	private boolean hasReboot(){
		if(app.getSharePref().getValue("reboot_selected").equals("0"))
			return false;
		
		if(getRebootTime()<1){
			return false;
		} 
		
		return true;
	}
	//return time unit is seconds
	private long getRebootTime(){
		String reboot=reboot_minite.getText().toString();
		if(reboot_minite.getText().toString().length()<1){
			reboot="0";
		}
		long duration =Integer.parseInt(reboot)*60;
		return duration;
	}
	private long getParallelTime(){
		String minute=duration_minute.getText().toString();
		String hour=duration_hour.getText().toString();		
		if(duration_hour.getText().toString().length()<1){
			hour="0";
		}
		if(duration_minute.getText().toString().length()<1){
			minute="0";
		}
		long duration =Integer.parseInt(hour)*3600+Integer.parseInt(minute)*60;
		return duration;
	}
	private boolean hasParallelItem(){
		boolean hasParallel = app.getSharePref().getValue("video_speaker_selected").equals("1")
				||app.getSharePref().getValue("video_receiver_selected").equals("1")
				||app.getSharePref().getValue("vibrate_selected").equals("1")
				||app.getSharePref().getValue("mic_loop_selected").equals("1")
				||app.getSharePref().getValue("front_camera_selected").equals("1")
				||app.getSharePref().getValue("back_camera_selected").equals("1");
		if(!hasParallel){
			return false;
		}
		if(getParallelTime()<1){
			return false;
		}
		return true;
	}
	
	private void startReboot(){
		Intent rebootIntent = new Intent(Intent.ACTION_REBOOT);
		rebootIntent.putExtra("nowait", 1);
		rebootIntent.putExtra("interval", 1);
		rebootIntent.putExtra("window", 0);
        sendBroadcast(rebootIntent); 
	}
	private void startParallelTest(){	
		setButton(true);
		Intent intent = new Intent().setClass(
				AgingTestActivity.this, AgingTestImplActivity.class);
		intent.putExtra("AGINGTEST_ITEMS", loadTransData());  								
		intent.putExtra("AGINGTEST_DURATION", getParallelTime());  
		startActivity(intent);
	}
	private String getLastDuration(){
		StringBuilder duration = new StringBuilder();
		duration.append(getString(R.string.last_duration_detail)+"\n");
		duration.append(agingtest_items[0]+":"+app.getSharePref().getValue("video_speaker_duration")+"\n");
		duration.append(agingtest_items[1]+":"+app.getSharePref().getValue("video_receiver_duration")+"\n");
		duration.append(agingtest_items[2]+":"+app.getSharePref().getValue("vibrate_duration")+"\n");
		duration.append(agingtest_items[3]+":"+app.getSharePref().getValue("mic_loop_duration")+"\n");
		duration.append(agingtest_items[4]+":"+app.getSharePref().getValue("front_camera_duration")+"\n");
		duration.append(agingtest_items[5]+":"+app.getSharePref().getValue("back_camera_duration")+"\n");
		return duration.toString();
	}
	private AgingTestItems loadTransData(){
		AgingTestItems mAgingTestItems = new AgingTestItems();
		mAgingTestItems.setvideoAndSpeaker(app.getSharePref().getValue("video_speaker_selected").equals("1"));
		mAgingTestItems.setvideoAndReceiver(app.getSharePref().getValue("video_receiver_selected").equals("1"));
		mAgingTestItems.setVibrate(app.getSharePref().getValue("vibrate_selected").equals("1"));
		mAgingTestItems.setMicLoop(app.getSharePref().getValue("mic_loop_selected").equals("1"));
		mAgingTestItems.setFrontCamera(app.getSharePref().getValue("front_camera_selected").equals("1"));
		mAgingTestItems.setBackCamera(app.getSharePref().getValue("back_camera_selected").equals("1"));
		return mAgingTestItems;
	}
	private class GridViewAdapter extends ArrayAdapter<String> {
		private String[] theItems;

		GridViewAdapter(Context context, int resource, String[] items) {
			super(context, resource, items);
			theItems = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			CheckBox label;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.aging_item, null);
				WindowManager wm = getWindowManager();
				int width = wm.getDefaultDisplay().getWidth();
				int height = wm.getDefaultDisplay().getHeight();
				convertView.setLayoutParams(new GridView.LayoutParams((width - 10) / 3, height / 11));
			}
			label = (CheckBox) convertView.findViewById(R.id.agingtest_item);
			label.setOnCheckedChangeListener(new OnCheckedChangeListener() {          
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					// TODO Auto-generated method stub
					setCheckValue(theItems,arg0.getText().toString(),arg1);
				}
			});
			label.setText(theItems[position]);
			setCheckValue(label,position);
			return convertView;
		}
	}
	private void setCheckValue(String [] parallel,String cb,boolean isCheck){
		int position = searchCbId(parallel,cb);
		switch (position){
			case 0:
				app.getSharePref().putValue("video_speaker_selected",isCheck?"1":"0");
				break;
			case 1:
				app.getSharePref().putValue("video_receiver_selected",isCheck?"1":"0");
				break;
			case 2:
				app.getSharePref().putValue("vibrate_selected",isCheck?"1":"0");
				break;
			case 3:
				app.getSharePref().putValue("mic_loop_selected",isCheck?"1":"0");
				break;
			case 4:
				app.getSharePref().putValue("front_camera_selected",isCheck?"1":"0");
				break;
			case 5:
				app.getSharePref().putValue("back_camera_selected",isCheck?"1":"0");
				break;
			default:
				break;
		}
	}
	private int searchCbId(String[] parallel, String cb) {
		// TODO Auto-generated method stub
		int index = 0;
		for(index=0;index<parallel.length;index++){
			if(parallel[index].equals(cb))
				break;
		}
		return index;
	}

	private void setCheckValue(CheckBox cb,int position){
		switch (position){
			case 0:
				cb.setChecked(app.getSharePref().getValue("video_speaker_selected").equals("1"));
				break;
			case 1:
				cb.setChecked(app.getSharePref().getValue("video_receiver_selected").equals("1"));
				break;
			case 2:
				cb.setChecked(app.getSharePref().getValue("vibrate_selected").equals("1"));
				break;
			case 3:
				cb.setChecked(app.getSharePref().getValue("mic_loop_selected").equals("1"));
				break;
			case 4:
				cb.setChecked(app.getSharePref().getValue("front_camera_selected").equals("1"));
				break;
			case 5:
				cb.setChecked(app.getSharePref().getValue("back_camera_selected").equals("1"));
				break;
			default:
				break;
		}
	}
	Handler mHandler = new Handler() {  
        public void handleMessage(Message msg) {   
             switch (msg.what) {   
                  case MSG_START_FROM_REBOOT:   
                	  handleReboot();
                       break;
                  case MSG_REFRESH_SECONDS:
                	  int next = Integer.parseInt(messageText.getText().toString())-1;
                	  messageText.setText(String.valueOf(next));
                	  break;
             }   
             super.handleMessage(msg);   
        }   
   };
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mCountDownTimer!=null){
			mCountDownTimer.cancel();
		}
		if(mSleepCountDownTimer!=null){
			mSleepCountDownTimer.cancel();
		}
		super.onDestroy();
	}  
}
