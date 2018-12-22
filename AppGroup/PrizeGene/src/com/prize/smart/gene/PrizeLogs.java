
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.prize.smart.gene;
import android.util.Log;

/*
 * i,frank,2013.01.11
 * only implement enable/disable switch
 * 当多个线程同时使用时, 要处理并发.
 * 当多个包使用这个库(类)时,为每个包保存一份log文件 ?
 * 
 * 
 * */
public class PrizeLogs {
	private static boolean kgLogsEnable = false;
	private static boolean kgLogsFileEnable = false;
	private static boolean kgLogsAndroidLogEnable = true; // false;
	
		/* prevent from instantiating */
	private PrizeLogs() {
	}
	
	private static final int LOGSFILE_V = 1;
	private static final int LOGSFILE_D = 2;
	private static final int LOGSFILE_I = 3;
	private static final int LOGSFILE_W = 4;
	private static final int LOGSFILE_E = 5;
	
		/* log into file implementation , i,frank,2013.01.18, how to ? */
	private static boolean kglogsPrepared = false;
	private static boolean kgLogsPrepare() {
		if(kglogsPrepared) {
			return true;
		}
		kglogsPrepared = true;
		return true;
	}
	private static void lgLogsFile(int type, String tag, String msg) {
		if(!kgLogsPrepare())
			return ;
		
	}
	
		/* main body */
    public static int v(String tag, String msg) {
    	if(kgLogsEnable)
    		System.out.println("verbose["+tag+"]:" + msg);
    	if(kgLogsAndroidLogEnable)
    		Log.v(tag, msg);
    	if(kgLogsFileEnable)
    		lgLogsFile(LOGSFILE_V, tag, msg);
    	return 0;
    }

    public static int d(String tag, String msg) {
    	if(kgLogsEnable)
    		System.out.println("debug["+tag+"]:" + msg);
    	if(kgLogsAndroidLogEnable)
    		Log.d(tag, msg);
    	if(kgLogsFileEnable)
    		lgLogsFile(LOGSFILE_D, tag, msg);
    	return 0;
    }

    public static int i(String tag, String msg) {
    	if(kgLogsEnable)
    		System.out.println("info["+tag+"]:" + msg);
    	if(kgLogsAndroidLogEnable)
    		Log.i(tag, msg);
    	if(kgLogsFileEnable)
    		lgLogsFile(LOGSFILE_I, tag, msg);
    	return 0;
    }

    public static int w(String tag, String msg) {
    	if(kgLogsEnable)
    		System.out.println("warning["+tag+"]:" + msg);
    	if(kgLogsAndroidLogEnable)
    		Log.w(tag, msg);
    	if(kgLogsFileEnable)
    		lgLogsFile(LOGSFILE_W, tag, msg);
    	return 0;
    }

    public static int e(String tag, String msg) {
    	if(kgLogsEnable)
    		System.out.println("error["+tag+"]:" + msg);
    	if(kgLogsAndroidLogEnable)
    		Log.e(tag, msg);
    	if(kgLogsFileEnable)
    		lgLogsFile(LOGSFILE_E, tag, msg);
    	return 0;
    }
}
