package com.freeme.operationManual.util;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;

import com.freeme.operationManual.ui.MainOperationManualActivity;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class Utils {
	public static String getDisplayName(String paramString) {
		if (TextUtils.isEmpty(paramString))
			;
		String[] arrayOfString = paramString.split("\\.");
		if (arrayOfString.length == 1)
			return paramString;
		return arrayOfString[1];
	}

	public static String getLocaleLanguage() {
		Locale localLocale = Locale.getDefault();
		Object[] arrayOfObject = new Object[2];
		arrayOfObject[0] = localLocale.getLanguage();
		arrayOfObject[1] = localLocale.getCountry();
		return String.format("%s-%s", arrayOfObject);
	}

	public static String getSharedPreferences(Context paramContext) {
		return PreferenceManager.getDefaultSharedPreferences(paramContext)
				.getString("ZIP_FILE", "nomanual");
	}
	public static  String getSystemProperties(Context context,String key) throws IllegalArgumentException {  
		  
        String ret= "";  
  
        try{  
  
          ClassLoader cl = context.getClassLoader();   
          @SuppressWarnings("rawtypes")  
          Class SystemProperties = cl.loadClass("android.os.SystemProperties");  
  
          @SuppressWarnings("rawtypes")  
              Class[] paramTypes= new Class[1];  
          paramTypes[0]= String.class;  
  
          Method get = SystemProperties.getMethod("get", paramTypes);  
    
          Object[] params= new Object[1];  
          params[0]= new String(key);  
  
          ret= (String) get.invoke(SystemProperties, params);  
  
        }catch( IllegalArgumentException iAE ){  
            throw iAE;  
        }catch( Exception e ){  
            ret= "";  
            //TODO  
        }  
        return ret;  
  
    }  
	public static boolean isNeutralCustom(Context context){
		String model = getSystemProperties(context,"ro.product.model");
		if(!TextUtils.isEmpty(model)){
			Log.d("snail", "------isNeutralCustom-------custom=="+model);
			if(model.equals("CHARGE_SIGNAL S1") || model.equals("UWEI V1")|| model.equals("CBK S2")|| model.equals("SMARTEGO S1")|| model.equals("YLH S1")){
				return false;
			}else {
				return true;
			}
		}
		return false;
	}

}