
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：支持悬浮功能,log工具
 *当前版本：
 *作 者：wanzhijuan
 *完成日期：2015-4-14
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.android.gallery3d.util;

import android.util.Log;

 public class LogUtil {

     protected static final String TAG = "Gallery2/Photo";
     public static void e(String tag, String msg) {
         /*if (Log.isLoggable(TAG, Log.ERROR)) {
             Log.e(TAG, tag + "---------->" + msg);
         }*/
     }

     public static void i(String tag, String msg) {
         /*if (Log.isLoggable(TAG, Log.INFO)) {
             Log.i(TAG, tag + "---------->" + msg);
         }*/
     }

     public static void w(String tag, String msg) {
         /*if (Log.isLoggable(TAG, Log.WARN)) {
             Log.w(TAG, tag + "---------->" + msg);
         }*/
     }

     public static void v(String tag, String msg) {
         /*if (Log.isLoggable(TAG, Log.VERBOSE)) {
             Log.v(TAG, tag + "---------->" + msg);
         }*/
     }

     public static void d(String tag, String msg) {
         /*if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, tag + "---------->" + msg);
         }*/
     }

     public static void e(String tag, String msg, Throwable t) {
         /*if (Log.isLoggable(TAG, Log.ERROR)) {
             Log.e(TAG, tag + "---------->" + msg, t);
         }*/
     }

     public static void i(String tag, String msg, Throwable t) {
         /*if (Log.isLoggable(TAG, Log.INFO)) {
             Log.i(TAG, tag + "---------->" + msg, t);
         }*/
     }

     public static void w(String tag, String msg, Throwable t) {
         /*if (Log.isLoggable(TAG, Log.WARN)) {
             Log.w(TAG, tag + "---------->" + msg, t);
         }*/
     }

     public static void v(String tag, String msg, Throwable t) {
         /*if (Log.isLoggable(TAG, Log.VERBOSE)) {
             Log.v(TAG, tag + "---------->" + msg, t);
         }*/
     }

     public static void d(String tag, String msg, Throwable t) {
         /*if (Log.isLoggable(TAG, Log.DEBUG)) {
             Log.d(TAG, tag + "---------->" + msg, t);
         }*/
     }

 }

