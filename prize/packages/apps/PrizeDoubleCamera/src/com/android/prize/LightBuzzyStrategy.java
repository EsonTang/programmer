package com.android.prize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.android.camera.Log;
import android.view.SurfaceHolder;

public class LightBuzzyStrategy implements IBuzzyStrategy {

	protected static final String TAG = "GYLog LightBuzzyStrategy";
	private static final int lSENSOR_MIN_ABSOLUTE_THRESHOLD = 3;//光感感应最小阀值
	private static final String ALS_PS1_NODE_PATH = "/sys/bus/platform/drivers/als_ps1/als";
	private String proc;
	@Override
	public void openCamera() {

	}

	@Override
	public void closeCamera() {

	}

	@Override
	public boolean isOcclusion() {
		try {
			proc = getBGFuzzyNodeValue(ALS_PS1_NODE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "startPreview setBgFuzzyLevel is error");
		}
		int tempproc = Integer.parseInt(proc.substring(2),16);
		Log.d(TAG, "getGyFuzeyNodeValue() proc:" + proc + " iproc=" + tempproc);
		if (tempproc < lSENSOR_MIN_ABSOLUTE_THRESHOLD) {
			return true;
		}
		return false;
	}

	@Override
	public void saveMainBmp(byte[] data) {

	}

	@Override
	public void startPreview() {
		
	}
	
	/***
	 * 获取驱动节点值
	 * 
	 * @param 节点路径
	 * @return 节点值
	 */
	private static String getBGFuzzyNodeValue(String path) {
		// Log.i(TAG, "getBGFuzzyNodeValue path: "+path);
		String prop = "0";// 默认值
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			prop = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	@Override
	public int getCheckTime() {
		return 400;
	}

	@Override
	public void attachSurfaceViewLayout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void detachSurfaceViewLayout() {
		// TODO Auto-generated method stub
		
	}

}
