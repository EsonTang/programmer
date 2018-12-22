package com.prize.runoldtest;

import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ManualTestActivity extends Activity {
    private Button ButtonOk;
    private Button ButtonCancel;
    public static boolean FlagLcd=false;
    public static boolean Flag3D=false;
    public static boolean FlagEmmc=false;
    public static boolean FlagMem=false;
    public static boolean FlagSr=false;
    public static boolean FlagVideo=false;
    public static boolean FlagMc=false;
    public static boolean FlagSMc=false;
    public static boolean FlagReboot=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_test);

        ButtonOk = (Button) findViewById(R.id.bt_ok);
        ButtonCancel = (Button) findViewById(R.id.bt_cancel);

        ButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManualTestActivity.this, CpuTestActivity.class);
                EditText editTextCpu=(EditText) findViewById(R.id.et_cputest);
                String message = editTextCpu.getText().toString();
                int CpuTime = Integer.parseInt(message);
                intent.putExtra(Const.EXTRA_MESSAGE,CpuTime);
                startActivity(intent);
            }
        });

        ButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ManualTestActivity.this, VideoActivity.class);
                startActivity(intent);
                //try {
                //    Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);

                // }catch (Exception e){
                //}
            }
        });
    }

    protected void onStart() {
        super.onStart();
        LogUtil.e("onStartManualTestActivity");
        if (FlagLcd) {
            FlagLcd = false;
            Intent intent = new Intent(this, LcdActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_lcdtest);
            String message = editText.getText().toString();
            int LcdTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,LcdTime);
            startActivity(intent);
        }
        else if (Flag3D) {
            Flag3D = false;
            Intent intent = new Intent(this, Test3DActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_3dtest);
            String message = editText.getText().toString();
            int SolidTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,SolidTime);
            startActivity(intent);
        }
        else if (FlagEmmc) {
            FlagEmmc = false;
            Intent intent = new Intent(this, EmmcActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_emmctest);
            String message = editText.getText().toString();
            int EmmcTime = Integer.parseInt(message);
            intent.putExtra(Const.EXTRA_MESSAGE,EmmcTime);
            startActivity(intent);
        }
        else if (FlagMem) {
            FlagMem = false;
            Intent intent = new Intent(this, DdrActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_memtest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }
        else if (FlagSr) {
            FlagSr = false;
            Intent intent = new Intent(this, SlpRsmActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_srtest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }
        else if (FlagVideo) {
            FlagVideo = false;
            Intent intent = new Intent(this, VideoActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_videotest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }
        else if (FlagMc) {
            FlagMc = false;
            Intent intent = new Intent(this, McActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_mctest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }
        else if (FlagSMc) {
            FlagSMc = false;
            Intent intent = new Intent(this, SMcActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_smctest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }
        else if (FlagReboot) {
            FlagReboot = false;
            Intent intent = new Intent(this, RebootActivity.class);
            EditText editText=(EditText) findViewById(R.id.et_reboottest);
            String message = editText.getText().toString();
            intent.putExtra(Const.EXTRA_MESSAGE,message);
            startActivity(intent);
        }
    }
}
