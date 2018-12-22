package com.goodix.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	private static String mOldMsg; 
	protected static Toast mToast   = null; 
	private static long mOneTime=0; 
	private static long mTwoTime=0; 

	public static void showToast(Context context, String content){     
		if(mToast==null){  
			mToast =Toast.makeText(context, content, Toast.LENGTH_SHORT); 
			mToast.show(); 
			mOneTime=System.currentTimeMillis(); 
		}else{ 
			mTwoTime=System.currentTimeMillis(); 
			if(content.equals(mOldMsg)){ 
				if(mTwoTime-mOneTime>Toast.LENGTH_SHORT){ 
					mToast.show(); 
				} 
			}else{ 
				mOldMsg = content; 
				mToast.setText(content); 
				mToast.show(); 
			}        
		} 
		mOneTime=mTwoTime; 
	} 
	
	public static void showToast(Context context, int resId){    
		showToast(context, context.getString(resId)); 
	} 
	
	public static void cancelToast() {  
//        if (mToast != null) {  
//            mToast.cancel();  
//        }  
    }
}
