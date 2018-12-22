package com.prize.runoldtest.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;

public class DataUtil {
	private static Map<String,Activity> destoryMap = new HashMap<String, Activity>();
	
	 /**
    * 添加到销毁队列
    *
    * @param activity 要销毁的activity
    */

   public static void addDestoryActivity(Activity activity,String activityName) {
       destoryMap.put(activityName,activity);
   }
   /**
   *销毁指定Activity
   */
   public static void destoryActivity() {
      Set<String> keySet=destoryMap.keySet();
       for (String key:keySet){
           destoryMap.get(key).finish();
       }
   }
}
