package com.prize.runoldtest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.UsbService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RunInMainActivity extends Activity {
	private final static String ACTION = "android.hardware.usb.action.USB_STATE";//USB是否connect状态监听，而不是知否插入USB线监听
    private ListView ListView_Main;
    private final static String ACTION3 = "android.intent.action.ACTION_POWER_DISCONNECTED";
    Intent show=null;
    /** 存储的文件名 */  
  //  public static final String DATABASE = "Database";  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_in_main);
       
         show = new Intent(this, UsbService.class);
        show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
        startService(show);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RunInMainActivity.this, android.R.layout.simple_list_item_1);
        ListView_Main = (ListView)findViewById(R.id.main_list);
        ListView_Main.setAdapter(adapter);

        adapter.add("RunAll Test");
        adapter.add("Manual Test");
        adapter.add("SingleTestActivity");

        ListView_Main.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                // TODO Auto-generated method stub
                if (position == 0)
                {
                    Log.i("main", "pos = " + position + " Run All Test");
                    Intent intent = new Intent(RunInMainActivity.this, RunAllTestActivity.class);
                    startActivity(intent);
                }
                if (position == 1)
                {
                    Log.i("main", "pos = " + position + "Manual Test");
                    Intent intent = new Intent(RunInMainActivity.this, ManualTestActivity.class);
                    startActivity(intent);
                }
                if (position == 2)
                {
                    Log.i("main", "pos = " + position + "SingleTestActivity");
                    Intent intent = new Intent(RunInMainActivity.this, SingleTestActivity.class);
                    startActivity(intent);
                }
            }
        });
        
       /*IntentFilter filter = new IntentFilter();
		
		filter.addAction(ACTION3);  
		registerReceiver(PowerConnectionReceiver, filter);*/
        LogToFile.init(RunInMainActivity.this);
        Broadcast.SteTag(false);
    }
    
    
   
    //add-zhuxiaoli-监听USB拔插-静态监听-发现此监听器监听不到usb拔插，所以使用下面的动态广播接收器可以监听到
  public class broadcast extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction(); 
			/*if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")){
				Log.e("RunInMainActivity" ,"USB_连接成功");
				Toast.makeText(getApplicationContext(), "USB_连接成功！", Toast.LENGTH_SHORT).show();
			}else if(action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")){
				Log.e("RunInMainActivity" ,"USB_断开");
				Toast.makeText(getApplicationContext(), "USB_断开！", Toast.LENGTH_SHORT).show();
				if(show!=null){
	        		show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
	                startService(show);
	        	}else{
	        		show = new Intent(RunInMainActivity.this, UsbService.class);
	        		show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
	                startService(show);
	        	}
			}*/
			
			
			/*if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {  // 设备插入  
				Log.e("RunInMainActivity" ,"USB_attached");
				Toast.makeText(getApplicationContext(), "USB_连接成功！", Toast.LENGTH_SHORT).show();
	  
	        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {  // 设备拔出  
	        	Toast.makeText(getApplicationContext(), "USB_断开！", Toast.LENGTH_SHORT).show();
	        	 Log.e("RunInMainActivity" ,"USB_detached");
	        	if(show!=null){
	        		show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
	                startService(show);
	        	}else{
	        		show = new Intent(RunInMainActivity.this, UsbService.class);
	        		show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
	                startService(show);
	        	}
	        	

	        }  */
			/*if(action.equals(Intent.ACTION_POWER_DISCONNECTED)){
				 Log.e("RunInMainActivity" ,"USB_detached");
				Toast.makeText(getApplicationContext(), "power_断开！", Toast.LENGTH_SHORT).show();
			}*/
		}
    	
    }
  
  /*BroadcastReceiver  PowerConnectionReceiver =new BroadcastReceiver() {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction(); 
		if(action.equals(Intent.ACTION_POWER_DISCONNECTED)){
			Log.e("RunInMainActivity" ,"动态广播USB_detached");
			Toast.makeText(getApplicationContext(), "动态广播power_断开！", Toast.LENGTH_SHORT).show();
			//show = new Intent(RunInMainActivity.this, UsbService.class); 
			//show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
			//startService(show);
			 SharedPreferences sharepreference = getSharedPreferences(DATABASE,  
		                Activity.MODE_PRIVATE);
		        Editor editor = sharepreference.edit();
		        editor.putString("testenable", "false");  
		        editor.commit();
			if(show!=null){
				 show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
			        startService(show);
			}else{
				show = new Intent(RunInMainActivity.this, UsbService.class);
		        show.putExtra(UsbService.ACTION, UsbService.SHOW_USB_INFO);
		        startService(show);
			}
		}
	}

	
	  
  };*/

    protected void onDestroy() {
        super.onDestroy();
        Intent hide = new Intent(this, UsbService.class);
        hide.putExtra(UsbService.ACTION, UsbService.HIDE_USB_INFO);
        startService(hide);
        try {
            BufferedWriter bufWriter = null;
            bufWriter = new BufferedWriter(new FileWriter(UsbService.WAKE_PATH));
            bufWriter.write("1");  // 写操作
            bufWriter.close();
            Toast.makeText(getApplicationContext(),
                    "开始充电",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Broadcast.SteTag(true);
    }
}
