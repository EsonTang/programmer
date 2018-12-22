package com.prize.runoldtest;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.util.Const;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class McActivity extends Activity {
    private int mc_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mc);

        Intent intent=getIntent();
        String message=intent.getStringExtra(Const.EXTRA_MESSAGE);
        //TextView textView=(TextView)findViewById(R.id.tvShow);
        //textView.setText(message);
        mc_time = Integer.parseInt(message);
    }

    TimerTask task = new TimerTask(){
        public void run(){
            try {
                ManualTestActivity.FlagSMc = true;
                Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    Timer timer = new Timer();
    protected void onStart() {
        super.onStart();

        timer.schedule(task, mc_time*3000);
    }
}
