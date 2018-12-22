package com.android.settings.applock;

import com.android.settings.SubSettings;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.settings.R;
/*-prize-add by lihuangyuan,for applock 2017-10-19-start*/
import com.android.settings.applock.PrizeAppLockManagerActivity;
import com.android.settings.applock.PrizeApplockConfirmLockPassword;
import com.android.settings.applock.PrizeApplockConfirmLockPattern;
import android.database.Cursor;
import com.android.settings.applock.PrizeAppLockCipherMetaData;
import android.content.ContentResolver;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
/*-prize-add by lihuangyuan,for applock 2017-10-19-end*/

public class PrizeAppLockStartActivity extends Activity{	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	 int passwordtype = getApplockPasswordType();		
    	if(-1 == passwordtype)
	{
		startAppLockManagerSetting();
    	}
	else
	{
    		startCheckPwdActivity(passwordtype);
	}
	finish();
    }
    /*-prize-add by lihuangyuan,for applock 2017-10-19-start*/
    public int getApplockPasswordType() 
    {
	        String whereApplockCipher = PrizeAppLockCipherMetaData.CIPHER_STATUS + " ="+PrizeAppLockCipherMetaData.CHIPHER_STATUS_VALID;//effective.
	        String [] selectColumns = new String[]{PrizeAppLockCipherMetaData.CIPHER_TYPE,PrizeAppLockCipherMetaData.CIPHER_STATUS};
	        ContentResolver resolverr = getContentResolver();
	        int hasApplockCipher = -1;
	        if (null != resolverr) 
		 {
	            Cursor cursorApplockCipher = resolverr.query(PrizeAppLockCipherMetaData.CONTENT_URI, selectColumns, whereApplockCipher, null, null);
	            if (null != cursorApplockCipher && cursorApplockCipher.getCount() > 0) 
		     {
		     	       cursorApplockCipher.moveToNext();
	            		hasApplockCipher = cursorApplockCipher.getInt(0);
	            } 
		     
	            if(cursorApplockCipher != null)
		     {
	            		cursorApplockCipher.close();
	            }
	        }
	        return hasApplockCipher;
    }
    public void startAppLockManagerSetting() 
    {	     
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    PrizeAppLockManagerActivity.class.getName());
            startActivity(intent);
    }
    public void startCheckPwdActivity(int passwordType)
    {
    	  Intent intent = new Intent();
	  Log.d("PrizeAppLock", "startCheckPwdActivity passwordType:"+passwordType);
         if(passwordType == PrizeAppLockCipherMetaData.CIPHER_TYPE_NUM
		||passwordType == PrizeAppLockCipherMetaData.CIPHER_TYPE_COMPLEX)
         {
            intent.putExtra("passwordtype",passwordType);
            intent.setClassName("com.android.settings",
                    PrizeApplockConfirmLockPassword.class.getName());
         }
         else if(passwordType == PrizeAppLockCipherMetaData.CIPHER_TYPE_PATTERN)
         {
         	 intent.putExtra("passwordtype",passwordType);
            	 intent.setClassName("com.android.settings",
                    PrizeApplockConfirmLockPattern.class.getName());         	 
         }
        startActivity(intent);
    }
    /*-prize-add by lihuangyuan,for applock 2017-10-19-end*/
}
