package com.prize.prizenavigation.utils;

import android.util.Log;

/**
 * 
 * 日志打印工具类
 *
 */
public class LogUtil
{
    public static final String tag = "LogUtil";

    public static final boolean isDebug = true;


    public static void i(String tag, String msg)
    {
        if (isDebug)
        {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg)
    {
        if (isDebug)
        {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg)
    {
        if (isDebug)
        {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg)
    {
        if (isDebug)
        {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg)
    {
        if (isDebug)
        {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg)
    {
        if (isDebug)
        {
            Log.e(tag, msg);
        }
    }

    public static void w(String tag, String msg)
    {
        if (isDebug)
        {
            Log.w(tag, msg);
        }
    }

    public static void w(String msg)
    {
        if (isDebug)
        {
            Log.w(tag, msg);
        }
    }



}
