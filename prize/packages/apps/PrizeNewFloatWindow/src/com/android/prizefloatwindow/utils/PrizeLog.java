package com.android.prizefloatwindow.utils;

import com.android.prizefloatwindow.application.PrizeFloatApp;

import android.util.Log;

public class PrizeLog {

	public static int d(String tag, String msg) {
        if (PrizeFloatApp.DEBUG) {
            return Log.d(tag, msg+getFunctionName());
        } else {
            return 0;
        }
    }
    public static int i(String tag, String msg) {
        if (PrizeFloatApp.DEBUG) {
            return Log.d(tag, msg+getFunctionName());
        } else {
            return 0;
        }
    }
    
    public static int e(String tag, String msg) {
        if (PrizeFloatApp.DEBUG) {
            return Log.e(tag, msg+getFunctionName());
        } else {
            return 0;
        }
    }
    /**
     * 方便日志定位
     * add by mafei 
     * @param tag 标签
     * @return
     */
    private static String getFunctionName()  
    {  
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();  
        if(sts == null)  
        {  
            return null;  
        }  
        for(StackTraceElement st : sts)  
        {  
            if(st.isNativeMethod())  
            {  
                continue;  
            }  
            if(st.getClassName().equals(Thread.class.getName()))  
            {  
                continue;  
            }  
            if(st.getClassName().equals(PrizeLog.class.getName()))  
            {  
                continue;  
            }  
            return "[" + Thread.currentThread().getName() + ": "  
                    + st.getFileName() + ":" + st.getLineNumber() + " "  
                    + st.getMethodName() + " ]";  
        }  
        return null;  
    }  

}
