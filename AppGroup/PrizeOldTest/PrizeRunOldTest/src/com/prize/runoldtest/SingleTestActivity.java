package com.prize.runoldtest;

import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SingleTestActivity extends Activity implements OnClickListener{
	
	private Button lcd;
	private Button flashlight;
	private Button playvideo;
	private Button emmc;
	private Button i2c;
	private Button audio;
	private Button tp;
	private Button camera;
	private Button test3d;
	private Button reboot;
	private Button rwbgpower;
	private Button sensor;
	private Button ddr;
	
	private Button sr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_test);
		DataUtil.addDestoryActivity(SingleTestActivity.this, "SingleTestActivity");
		initView();
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
		audio = (Button)findViewById(R.id.audio);
		audio.setOnClickListener(this);
		tp = (Button)findViewById(R.id.tp);
		tp.setOnClickListener(this);
		camera = (Button)findViewById(R.id.camera);
		camera.setOnClickListener(this);
		test3d = (Button)findViewById(R.id.test3d);
		test3d.setOnClickListener(this);
		rwbgpower = (Button)findViewById(R.id.rwbgpower);
		rwbgpower.setOnClickListener(this);
		sensor = (Button)findViewById(R.id.sensor);
		sensor.setOnClickListener(this);
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
			startActivity(LcdActivity.class);
			break;
		case R.id.flashlight:
			startActivity(FlightActivity.class);
			break;
		case R.id.playvideo:
			startActivity(VideoActivity.class);
			break;
		case R.id.emmc:
			startActivity(EmmcActivity.class);
			break;
		case R.id.i2c:
			
			break;
		case R.id.audio:
			
			break;
		case R.id.tp:
			
			break;
		case R.id.camera:
			startActivity(CameraTestActivity.class);
			break;
		case R.id.test3d:
			startActivity(Test3DActivity.class);
			break;
		case R.id.reboot:
			startActivity(RebootActivity.class);
			break;
		case R.id.rwbgpower:
			startActivity(CpuTestActivity.class);
			break;
		case R.id.sensor:
			
			break;
		case R.id.ddr:
			startActivity(DdrActivity.class);
			break;
		case R.id.sr:
			startActivity(SlpRsmActivity.class);
			break;

		default:
			break;
		}
	}
	
	private void startActivity(Class<?> cls){
		LogUtil.e(cls.getName());
		Intent intent = new Intent(SingleTestActivity.this, cls);
		intent.putExtra(Const.EXTRA_MESSAGE, 20);
		SingleTestActivity.this.startActivity(intent);
	}
}