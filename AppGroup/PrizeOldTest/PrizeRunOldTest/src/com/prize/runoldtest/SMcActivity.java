package com.prize.runoldtest;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.util.Const;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class SMcActivity extends Activity {
    private int smc_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smc);

        Intent intent=getIntent();
        String message=intent.getStringExtra(Const.EXTRA_MESSAGE);
        //TextView textView=(TextView)findViewById(R.id.tvShow);
        //textView.setText(message);
        smc_time = Integer.parseInt(message);
    }

    TimerTask task = new TimerTask(){
        public void run(){
            try {
                ManualTestActivity.FlagReboot = true;
                Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    Timer timer = new Timer();
    protected void onStart() {
        super.onStart();

        timer.schedule(task, smc_time*3000);
    }
}
