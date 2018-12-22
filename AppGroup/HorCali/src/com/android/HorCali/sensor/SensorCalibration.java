/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 * author by huangdianjun20151212
 */

package com.android.HorCali.sensor;

import android.app.ActionBar;
import android.app.Activity;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.HorCali.R;
//import com.mediatek.xlog.Xlog;
import java.util.Locale;
import com.android.HorCali.emsvr.AFMFunctionCallEx;
import com.android.HorCali.emsvr.FunctionReturn;
import java.util.ArrayList;
import android.util.Log;
import android.content.Intent;
public class SensorCalibration extends Activity implements OnClickListener,SensorEventListener {
    public static final String CALIBRAION_TYPE = "type";
    public static final int GSENSOR = 0;
    public static final int GYROSCOPE = 1;
    private static final String TAG = "SensorCalibration";
    private static final int MSG_DO_CALIBRARION_20 = 0;
    private static final int MSG_GET_CALIBRARION = 3;
    private static final int MSG_SET_SUCCESS = 4;
    private static final int MSG_GET_SUCCESS = 5;
    private static final int MSG_SET_FAILURE = 6;
    private static final int MSG_GET_FAILURE = 7;
	private static final int MSG_UI_UPDATE = 20;
    private static final int TOLERANCE_20 = 2;
    private static final int TOLERANCE_40 = 4;
    private static final String[] SENSOR_NAME = {"GSENSOR", "GYROSCOPE"};

    private Button mSetCalibration20;
	private boolean isNotHorizontal;
	private float mBubbleLocationX;
	private float mBubbleLocationY;
	private float mCircleX;
	private float mCircleY;
	private static final long durations = 20;
	private static final long mReduction = 3;
	private Button mCalibrationButton;
	private HorizontalCalibrationView mHorizontalCalibrationView;
	private TextView mCalibrationNote;
   
    private Toast mToast;

    private int mType;
    private int mSensorType;
    private String mData;
	private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private final HandlerThread mHandlerThread = new HandlerThread("async_handler");
    private Handler mHandler;
    private Handler mUiHandler;


	public static final int RET_SUCCESS = 1;
    public static final int RET_FAILED = 0;

	private float mAccelerationY;
	private float mAccelerationZ;
	private float mAccelerationX;
    private float squareX;
	private float squareY;
	private boolean mSuccess;
	private boolean isCalibrationOn;
	
    private String getSensorName(int type) {
        if (type < 0 || type >= SENSOR_NAME.length) {
            return "Sensor";
        }
        return SENSOR_NAME[type];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horizontal_calibration);
        ActionBar actionBar = getActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
		mHorizontalCalibrationView = (HorizontalCalibrationView) findViewById(R.id.main_horizontalcalibrationview);
		mCalibrationNote = (TextView) findViewById(R.id.calibrationAttention);
        
        mType = getIntent().getIntExtra(CALIBRAION_TYPE, GSENSOR);
        mSuccess = false;
		isCalibrationOn = false;
		
        if (mType == GSENSOR) {
            mSensorType = Sensor.TYPE_ACCELEROMETER;
            setTitle(R.string.sensor_calibration_gsensor);
		}
        mSetCalibration20 = (Button) findViewById(R.id.calibration); 
        mSetCalibration20.setOnClickListener(this);

        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_SET_SUCCESS:
                    //Xlog.d(TAG, "set success");
                    enableButtons(true);
                  //  showToast("Operation succeed");
                    mSuccess = true;
				    if(getIntent().hasExtra("gsensor_factorytest")&&getIntent().getExtras().getBoolean("gsensor_factorytest")){
						Intent intent = new Intent();  
						intent.putExtra("sHorCali", mData); 
						setResult(RESULT_OK, intent);    
					}else{
						setResult(RESULT_CANCELED);  
					}
					isCalibrationOn = true;
                    break;
                case MSG_GET_SUCCESS:
					
                    //Xlog.d(TAG, "get success");
                    break;
                case MSG_SET_FAILURE:
                    //Xlog.d(TAG, "set fail");
                    enableButtons(true);
                 //   showToast("Operation failed");
					mSuccess = false;
                    break;
                case MSG_GET_FAILURE:
                    //Xlog.d(TAG, "get fail");
                    enableButtons(true);
                 //   showToast("Get calibration failed");
                    break;
				case MSG_UI_UPDATE:
					isCalibrationOn = false;
					if(mSuccess){
						finish();
					}
					break;
                default:
                }
            }
        };

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (MSG_GET_CALIBRARION == msg.what) {
                    getCalibration();
                } else {
                    setCalibration(msg.what);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Xlog.d(TAG, String.format("onResume(), type %d", mType));		
		
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(mSensorType);
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, getSensorName(mType) + " was not supported.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }		
       	mHandler.sendEmptyMessage(MSG_GET_CALIBRARION);
    }

    @Override
    public void onPause() {
        //Xlog.d(TAG, String.format("onPause(), type %d", mType));
        //Xlog.d(TAG, "unregisterListener");
        mSensorManager.unregisterListener(this);
        mSensorManager = null;		
        super.onPause();
    }
	
	/*prize-public-bug:add by liuweiquan-20160315-start*/
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub		
		mUiHandler.removeCallbacks(HandlerThread);
		mHandlerThread.quit();
		super.onDestroy();
	}
	/*prize-public-bug:add by liuweiquan-20160315-end*/
    @Override
    public void onClick(View arg0) {
        if (arg0.getId() == mSetCalibration20.getId()) {
			//Xlog.d(TAG, "do calibration 20");
			mHandler.sendEmptyMessage(MSG_DO_CALIBRARION_20);
			mHorizontalCalibrationView.isCalibrating = true;
			mCalibrationNote.setText(getResources().getString(
						R.string.calibration_attention_calibrating));					
			mUiHandler.post(HandlerThread);		
        } 
        enableButtons(false);
    }
    private boolean getCalibration() {
        //Xlog.d(TAG, "getGsensorCalibration()");
        float[] result = new float[3];
        int ret = 0;
        if (mType == GSENSOR) {
            ret = getGsensorCalibration(result);
        } 
        Log.d(TAG, String.format("getGsensorCalibration(), ret %d, values %f, %f, %f",
                ret, result[0], result[1], result[2]));
		if (ret == RET_SUCCESS) {
			//mData = String.format(Locale.ENGLISH, "%+8.4f,%+8.4f,%+8.4f",
			//        result[0], result[1], result[2]);
			//add liup 20171016 recovery cali save data start
			mData = String.format(Locale.ENGLISH, "%+d,%+d,%+d",
					(int)(result[0]*1000), (int)(result[1]*1000), (int)(result[2]*1000));
			mUiHandler.sendEmptyMessage(MSG_GET_SUCCESS);
			//add liup 20171016 recovery cali save data end

			return true;
        } else {
            mData = "";
            mUiHandler.sendEmptyMessage(MSG_GET_FAILURE);
            return false;
        }
    }
    public int clearGsensorCalibration() {
		String[] ret = runCmdInEmSvr(
				AFMFunctionCallEx.FUNCTION_EM_SENSOR_CLEAR_GSENSOR_CALIBRATION, 0);
		if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
			return RET_SUCCESS;
		}
		return RET_FAILED;
    }

    private void setCalibration(int what) {
        int result = 0;
        //Xlog.d(TAG, String.format("setCalibration(), operation %d", what));
        if (mType == GSENSOR) {
            if (MSG_DO_CALIBRARION_20 == what) {
                result = doGsensorCalibration(TOLERANCE_20);
            } 
        } 
        //Xlog.d(TAG, String.format("setCalibration(), ret %d", result));

        if (result == RET_SUCCESS) {
            if(getCalibration()){
				clearGsensorCalibration();
				mUiHandler.sendEmptyMessageDelayed(MSG_SET_SUCCESS, 1200);
			}
        } else {
        	isCalibrationOn = true;
            mUiHandler.sendEmptyMessage(MSG_SET_FAILURE);
        }
    }

    private void enableButtons(boolean enable) {
        mSetCalibration20.setClickable(enable);       
    }

    private void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }
	public int getGsensorCalibration(float[] result) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_GET_GSENSOR_CALIBRATION, 0);
        if (ret.length >= 4 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            try {
                result[0] = Float.parseFloat(ret[1]);
                result[1] = Float.parseFloat(ret[2]);
                result[2] = Float.parseFloat(ret[3]);
                return RET_SUCCESS;
            } catch (NumberFormatException e) {
                return RET_FAILED;
            }
        }
        return RET_FAILED;
    }
	public int doGsensorCalibration(int tolerance) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_DO_GSENSOR_CALIBRATION, 1,
                tolerance);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }
	
	 public String[] runCmdInEmSvr(int index, int paramNum, int... param) {
        ArrayList<String> arrayList = new ArrayList<String>();
        AFMFunctionCallEx functionCall = new AFMFunctionCallEx();
        boolean result = functionCall.startCallFunctionStringReturn(index);
        functionCall.writeParamNo(paramNum);
        for (int i : param) {
            functionCall.writeParamInt(i);
        }
        if (result) {
            FunctionReturn r;
            do {
                r = functionCall.getNextResult();
                if (r.mReturnString.isEmpty()) {
                    break;
                }
                arrayList.add(r.mReturnString);
            } while (r.mReturnCode == AFMFunctionCallEx.RESULT_CONTINUE);
            if (r.mReturnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                //Xlog.d(TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                arrayList.clear();
                arrayList.add("ERROR");
            }
        } else {
            //Xlog.d(TAG, "AFMFunctionCallEx return false");
            arrayList.clear();
            arrayList.add("ERROR");
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }


    @Override
	public void onSensorChanged(SensorEvent event) {

		float[] values = event.values;
		int sensorType = event.sensor.getType();

		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
			mAccelerationX = values[0];
			mAccelerationY = values[1];
			mAccelerationZ = values[2];
			Log.e("zhangjialong","x="+mAccelerationX + " y="+mAccelerationY + " z="+mAccelerationZ);			
			squareX = (float)(mAccelerationX*mAccelerationX);
			squareY = (float)(mAccelerationY*mAccelerationY);				
			squareX=(float)((squareX>96.2)?96.2:squareX);
			squareY=(float)((squareY>96.2)?96.2:squareY);
			
			mCircleX = mHorizontalCalibrationView.bubbleX0;
			mCircleY = mHorizontalCalibrationView.bubbleY0;			
			Log.e("zhangjialong","mCircleX="+mCircleX);
			Log.e("zhangjialong","mCircleY="+mCircleY);
			
			float mDefaultDistance = (float) (mHorizontalCalibrationView.background
					.getWidth()/ 2 *0.8);
			if(mAccelerationX >= 0 && mAccelerationY >=0){
				mBubbleLocationX = (float) (mCircleX + squareX*mDefaultDistance / 96.2);
				mBubbleLocationY = (float) (mCircleY - squareY*mDefaultDistance / 96.2);
			}else if(mAccelerationX >= 0 && mAccelerationY < 0){
				mBubbleLocationX = (float) (mCircleX + squareX*mDefaultDistance / 96.2);
				mBubbleLocationY = (float) (mCircleY + squareY*mDefaultDistance / 96.2);
			}else if(mAccelerationX < 0 && mAccelerationY >= 0){
				mBubbleLocationX = (float) (mCircleX - squareX*mDefaultDistance / 96.2);
				mBubbleLocationY = (float) (mCircleY - squareY*mDefaultDistance / 96.2);
			}else {
				mBubbleLocationX = (float) (mCircleX - squareX*mDefaultDistance / 96.2);
				mBubbleLocationY = (float) (mCircleY + squareY*mDefaultDistance / 96.2);
			}			
		
		}
		
		mHorizontalCalibrationView.bubbleX = mBubbleLocationX;
		mHorizontalCalibrationView.bubbleY = mBubbleLocationY;
		mHorizontalCalibrationView.isBubbleInit = false;
		mHorizontalCalibrationView.postInvalidate();
	}


    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}


    Runnable HandlerThread = new Runnable() {
		@Override
		public void run() {			
			if (mHorizontalCalibrationView.radius > mHorizontalCalibrationView.bubble
					.getWidth() *0.9/ 2) {
				mHorizontalCalibrationView.radius = mHorizontalCalibrationView.radius
						- mReduction;
				mUiHandler.postDelayed(HandlerThread, durations);
			}else if(!isCalibrationOn){
				mUiHandler.postDelayed(HandlerThread, durations);
			}else {
			    resetForCircle();				
			}			
		}
	};

	void resetForCircle(){
	    mUiHandler.removeCallbacks(HandlerThread);		
		mCalibrationNote.setText(getResources().getString(
				R.string.calibration_attention_init));
		mHorizontalCalibrationView.radius =(float)(mHorizontalCalibrationView.background
				.getWidth()*0.9 / 2);
		mHorizontalCalibrationView.isCalibrating = false;

		if(mSuccess){
           showToast(getResources().getString(R.string.sensor_operation_succuss));
		}else{
           showToast(getResources().getString(R.string.sensor_operation_fail));
		}
		mUiHandler.sendEmptyMessage(MSG_UI_UPDATE);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    if(item.getItemId() == android.R.id.home){
	        finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

