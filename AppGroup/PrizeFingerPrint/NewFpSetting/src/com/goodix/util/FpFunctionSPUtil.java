package com.goodix.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class FpFunctionSPUtil{
	
	private static final String mName = "FpFunction";
	private SharedPreferences mPfc;
    
    public FpFunctionSPUtil(Context context){
    	mPfc = context.getSharedPreferences(mName,Activity.MODE_PRIVATE);
    }
    
    public boolean getFunctionStatus(String key){
        return mPfc.getBoolean(key, false);
    }
    
    public void setFunctionStatus(String key, boolean flag){
        Editor editor = (Editor) mPfc.edit();
        editor.putBoolean(key, flag);
        editor.commit();
    }
}
