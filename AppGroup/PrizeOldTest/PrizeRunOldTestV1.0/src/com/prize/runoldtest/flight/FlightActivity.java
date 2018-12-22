package com.prize.runoldtest.flight;

import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.prize.runoldtest.ManualTestActivity;
import com.prize.runoldtest.R;
import com.prize.runoldtest.util.DataUtil;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class FlightActivity extends Activity implements OnClickListener {
	public static final boolean HasFlashlightFile = false;
	
	private Camera mycam = null;
	private Parameters camerPara = null;
	final byte[] LIGHTE_ON = { '2', '5', '5' };
	final byte[] LIGHTE_OFF = { '0' };
	private Button mTrunON_bt, mTrunOFF_bt;
	private TextView mStatus_tv;
	private long flight_time = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flashlight);
		mTrunON_bt = (Button) findViewById(R.id.flight_on_bt);
		mTrunOFF_bt = (Button) findViewById(R.id.flight_off_bt);
		mStatus_tv = (TextView) findViewById(R.id.flight_contxt_tv);
		mTrunON_bt.setOnClickListener(this);
		mTrunOFF_bt.setOnClickListener(this);
		trun_ON_Flashlight();
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		trun_OFF_Flashlight();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.flight_on_bt:
			trun_ON_Flashlight();
			break;
		case R.id.flight_off_bt:
			trun_OFF_Flashlight();
			break;
		default:
			break;
		}
	}
	
	protected void trun_ON_Flashlight() {
		if (!HasFlashlightFile) {
			try {
				mycam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				camerPara = mycam.getParameters();
				camerPara.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mycam.setParameters(camerPara);
				mStatus_tv.setText(R.string.flashlight_on);
				mTrunON_bt.setEnabled(false);
				mTrunOFF_bt.setEnabled(true);
			} catch (Exception e) {
			}
		} else {

			FileOutputStream flashlight;
			try {
				flashlight = new FileOutputStream(
						"/sys/class/leds/flashlight/brightness");
				flashlight.write(LIGHTE_ON);
				flashlight.close();

			} catch (Exception e) {
			}
		}
	}
	
	protected void trun_OFF_Flashlight() {
		if (!HasFlashlightFile) {
			try {
				mycam = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
				camerPara = mycam.getParameters();
				camerPara.setFlashMode(Parameters.FLASH_MODE_OFF);
				mycam.setParameters(camerPara);
				mStatus_tv.setText(R.string.flashlight_off);
				mTrunON_bt.setEnabled(true);
				mTrunOFF_bt.setEnabled(false);
			} catch (Exception e) {
			}
		} else {

			FileOutputStream flashlight;
			try {
				flashlight = new FileOutputStream(
						"/sys/class/leds/flashlight/brightness");
				flashlight.write(LIGHTE_OFF);
				flashlight.close();

			} catch (Exception e) {
			}
		}
	}
	
	@Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	trun_OFF_Flashlight();
    }
	
	TimerTask task = new TimerTask(){
        public void run(){
            try {
            	//DataUtil.FlagMc = true;
                Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
    Timer timer = new Timer();
    protected void onStart() {
        super.onStart();
        timer.schedule(task, flight_time*30000);
    }
}
