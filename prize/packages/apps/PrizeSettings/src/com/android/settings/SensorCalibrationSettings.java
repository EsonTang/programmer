/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * 
 * 内容摘要：传感器校准
 * 当前版本：V1.0
 * 作    者：黄典俊
 * 完成日期：2015-04-17
 *

 *********************************************/
package com.android.settings;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;

//add liup 20171016 recovery cali save data start
import java.io.File;
import java.io.IOException; 
import java.io.FileInputStream;
import java.io.InputStreamReader;  
import java.io.FileOutputStream;
//add liup 20171016 recovery cali save data end

public class SensorCalibrationSettings extends SettingsPreferenceFragment implements OnPreferenceClickListener {

    private static final String KEY_PROXIMITY_SENSOR = "proximity_sensor_preference";
	private static final String KEY_HORIZONTAL_cALIBRATION = "horizontal_calibration__preference";
	private final static int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
    
	private AlertDialog.Builder builder;	
    private Preference mProximitySensor;
	private Preference mHorizontalCalibration;
	private boolean isCalibrationSuccess = true;
	private SensorManager mSensorManager;
	private Sensor mPSensor;
	private PSensorListener mPSensorListener;

	//add liup 20171016 recovery cali save data start
	private static final String filePath = "/data/prize_backup/";
	private static final String fileName = "prize_factory_gsensor";
	//add liup 20171016 recovery cali save data end
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.sensor_calibration_settings);	
        initializeAllPreferences();
    }

    /**
     * 方法描述： SensorCalibrationSettings-initial-UI
     * @author huangdianjun
     */
    private void initializeAllPreferences(){

        final PreferenceScreen root = getPreferenceScreen();
        mProximitySensor = root.findPreference(KEY_PROXIMITY_SENSOR);
        mProximitySensor.setOnPreferenceClickListener(this);
		mHorizontalCalibration = root.findPreference(KEY_HORIZONTAL_cALIBRATION);
        mHorizontalCalibration.setOnPreferenceClickListener(this);
        if(mSensorManager == null){
        	mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        }
        mPSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        mPSensorListener = new PSensorListener();
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.proximity_sensor_title));
        builder.setMessage(getResources().getString(R.string.proximity_sensor_context));
        builder.setPositiveButton(getResources().getString(R.string.proximity_sensor_positive), new PositiveListener());
        builder.setNegativeButton(getResources().getString(R.string.proximity_sensor_negative), null);

    }

  @Override
    protected int getMetricsCategory() {
        return 2;
    }
	
    @Override
    public boolean onPreferenceClick(Preference preference) {//zhangjialong modify for horcali 20160128
        if (preference == mProximitySensor){
            builder.show();
		}else if (preference == mHorizontalCalibration){
			final Intent intent = new Intent();
           // intent.setClass(getActivity(), HorizontalCalibrationActivity.class);
			intent.setClassName("com.android.HorCali", "com.android.HorCali.sensor.SensorCalibration");
			//startActivity(intent);
			//modify liup 20171016 recovery cali save data
			intent.putExtra("gsensor_factorytest", true);  
			startActivityForResult(intent, 0);
		}
        return true;
    }
	//add liup 20171016 recovery cali save data start
	@Override  
    public void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
		if(resultCode == -1){  
			String sHorCali = data.getStringExtra("sHorCali");
			creatFile();
			writeFile(sHorCali);
			if(null == sHorCali){
				return;
			}
			try {	
				String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + sHorCali + " > /proc/gsensor_cali"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}     	
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
			FileOutputStream fout = new FileOutputStream(filePath + fileName);
			byte[] bytes = data.getBytes();
			fout.write(bytes);
			fout.flush();				
			fout.close();
			Log.e("liup","writeFile succcess");
		} catch (Exception e) {
		}
	}
	//add liup 20171016 recovery cali save data end
    public class PositiveListener implements
        android.content.DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	if (isCalibrationSuccess) {
            		toast(getResources().getString(R.string.proximity_sensor_success_toast));
				} else {
					toast(getResources().getString(R.string.proximity_sensor_failed_toast));
				}
            }
        }
    public class PSensorListener implements SensorEventListener {

		@Override
		public void onSensorChanged(SensorEvent event) {

			synchronized (this) {
				if (event.sensor.getType() == SENSOR_TYPE) {
					if (event.values[0] == 0) {
						isCalibrationSuccess = false;
					}else {
						isCalibrationSuccess = true;
					}
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

			// TODO Auto-generated method stub

		}
	}
   
	public void toast(String s) {

		if (s == null)
			return;
		Toast.makeText(getActivity(), s, Toast.LENGTH_LONG)
        .show();
	}
	@Override
	public void onResume() {

		super.onResume();
		mSensorManager.registerListener(mPSensorListener,
				mPSensor,
				SENSOR_DELAY);
	}
	@Override
	public void onPause() {

		mSensorManager.unregisterListener(mPSensorListener,mPSensor);
		super.onPause();
	}
	@Override
	public void onDestroy() {

		super.onDestroy();

		if (mSensorManager == null || mPSensorListener == null
				|| mPSensor == null)
			return;
		mSensorManager.unregisterListener(mPSensorListener, mPSensor);
	}
}
