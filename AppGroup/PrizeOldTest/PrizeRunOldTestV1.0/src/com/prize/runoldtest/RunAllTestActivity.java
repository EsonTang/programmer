package com.prize.runoldtest;

import com.prize.runoldtest.ddr.DdrActivity;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.OldTestResult;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RunAllTestActivity extends Activity {
    private ListView listview_runall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_all_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  
	               | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  
	               | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RunAllTestActivity.this, android.R.layout.simple_list_item_1);
        listview_runall = (ListView)findViewById(R.id.run_all_list);
        listview_runall.setAdapter(adapter);

        adapter.add("6 Hours");
        adapter.add("12 Hours");
        adapter.add("4 Hours");

        listview_runall.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                // TODO Auto-generated method stub
                if (position == 0)
                	
               
                {
                	SharedPreferences msharedPreferences =getSharedPreferences("sixtesttime", RunAllTestActivity.MODE_PRIVATE); 
                	Editor editor = msharedPreferences.edit();
             		editor.putInt("sixtesttimes", 1);		
             		editor.commit();
                	DataUtil.FlagCpu=true;
                	 DataUtil.isSixTest=true;
                    Log.i("main", "pos = " + position + " Run All 6HourTest");
                    Intent intent = new Intent(RunAllTestActivity.this, RunAll6HourActivity.class);
                    OldTestResult.CleanTestResult();
                    startActivity(intent);
                }
                if (position == 1)
                {
                	 SharedPreferences sharedPreferences =getSharedPreferences("twlftesttime", RunAllTestActivity.MODE_PRIVATE); //˽�����
             		Editor editor = sharedPreferences.edit();
             		editor.putInt("twlftesttimes", 2);		
             		editor.commit();
                	DataUtil.FlagCpu=true;
                	DataUtil. isTwlfTest=true;
                    Log.i("main", "pos = " + position + "Run All 12Hour Test");
                    Intent intent = new Intent(RunAllTestActivity.this, RunAll12HourActivity.class);
                    OldTestResult.CleanTestResult();
                    startActivity(intent);
                }
                else if(position==2){
                	 SharedPreferences sharedPreferences =getSharedPreferences("fourtesttime", RunAllTestActivity.MODE_PRIVATE); //˽�����
              		Editor editor = sharedPreferences.edit();
              		editor.putInt("fourtesttimes", 1);		
              		editor.commit();
                 	DataUtil.FlagCpu=true;
                 	DataUtil. isfourTest=true;
                     Log.i("main", "pos = " + position + "Run All 4Hour Test");
                     Intent intent = new Intent(RunAllTestActivity.this, RunAll4HoursActivity.class);
                     OldTestResult.CleanTestResult();
                     startActivity(intent);
                }
            }
        });
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//SysApplication.getInstance().deleteActivity(this);
	}
    
    
}
