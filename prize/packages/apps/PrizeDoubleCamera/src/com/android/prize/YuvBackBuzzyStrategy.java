package com.android.prize;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import android.os.SystemProperties;
import com.android.camera.Log;
import android.view.SurfaceHolder;

/*prize-xuchunming-20171118-bugid:42532-start*/
import com.android.camera.CameraActivity;
import android.os.Handler;
import android.os.Message;
/*prize-xuchunming-20171118-bugid:42532-end*/

public class YuvBackBuzzyStrategy implements IBuzzyStrategy {

	protected static final String TAG = "YuvBackBuzzyStrategy";
	private static final int lSENSOR_MAX_ABSOLUTE_THRESHOLD = 150;//SystemProperties.getInt("persist.prize.yuv_back_value",150);//光感感应最小阀值
	private static final String YUV_BACK_NODE_PATH = "/sys/kernel/spc/spc_r_value";
	private static final String YUV_SUB_OPEN_PATH = "/sys/kernel/spc/spc_r_open";
	private static final String YUV_SUB_CLOSE_PATH = "/sys/kernel/spc/spc_r_close";
	private String proc;
	/*prize-xuchunming-20171118-bugid:42532-start*/
	private static boolean waitExpouseData = true;
	private static int EXPOSEDATA_READY_TIME = 1500;
	private static final int MSG_DATE_READYGO = 0;
	private Handler mHandler = new Handler(){
    	public void dispatchMessage(Message msg) {
    		Log.i(TAG, "dispatchMessage what=" + msg.what);
    		switch (msg.what) {
				case MSG_DATE_READYGO:
					Log.i(TAG, "MSG_DATE_READYGO set waitExpouseData == false");
					waitExpouseData = false;
				break;
			}
    	};
    };
    /*prize-xuchunming-20171118-bugid:42532-end*/
	@Override
	public void openCamera() {
		/*prize-xuchunming-20171118-bugid:42532-start*/
		waitExpouseData = true;
		/*prize-xuchunming-20171118-bugid:42532-end*/
		mHandler.removeMessages(MSG_DATE_READYGO);
		mHandler.sendEmptyMessageDelayed(MSG_DATE_READYGO, EXPOSEDATA_READY_TIME);
		getBGFuzzyNodeValue(YUV_SUB_OPEN_PATH);
	}

	@Override
	public void closeCamera() {
		/*prize-xuchunming-20171118-bugid:42532-start*/
		waitExpouseData = true;
		/*prize-xuchunming-20171118-bugid:42532-end*/
		mHandler.removeMessages(MSG_DATE_READYGO);
		getBGFuzzyNodeValue(YUV_SUB_CLOSE_PATH);
	}

	@Override
	public boolean isOcclusion() {
		/*prize-xuchunming-20171118-bugid:42532-start*/
		if(waitExpouseData == true) {
			Log.w(TAG, "wait ExpouseData return false");
			return false;
		}
		/*prize-xuchunming-20171118-bugid:42532-end*/
		int tempproc = -1;
		try {
			proc = getBGFuzzyNodeValue(YUV_BACK_NODE_PATH);
			tempproc = Integer.parseInt(proc);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "startPreview setBgFuzzyLevel is error");
		}
		Log.d(TAG, "lSENSOR_MAX_ABSOLUTE_THRESHOLD = "+lSENSOR_MAX_ABSOLUTE_THRESHOLD+ 
		    "  getGyFuzeyNodeValue() proc:" + proc + " iproc=" + tempproc);	
		if (tempproc > lSENSOR_MAX_ABSOLUTE_THRESHOLD || tempproc == -1) {
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
